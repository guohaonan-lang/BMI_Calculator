package com.example.bmicalculator.fragment

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter
import com.contrarywind.view.WheelView
import com.example.bmicalculator.R
import com.example.bmicalculator.adapter.InputAgeAdapter
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.FragmentDataInputBinding
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.ui.ResultActivity
import com.example.bmicalculator.ui.SettingActivity
import com.example.bmicalculator.util.BmiUtil
import com.example.bmicalculator.viewmodel.BmiViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.getValue
import kotlin.math.abs

class DataInputFragment : Fragment() {

    private var _binding: FragmentDataInputBinding? = null
    private val binding get() = checkNotNull(_binding)

    var bmiRecord = BmiEntity(
        height = 170f,
        heightUnit = false,
        weight = 140f,
        weightUnit = false,
        bmiValue = 21.9f,
        bmiColor = 0xFF888888.toInt(), // 临时占位色
        bmiGrade = "Normal",
        age = 25,
        gender = 1,
        createTime = System.currentTimeMillis(),
        customTime = 0L,
        timeText = ""
    )
    private var weightPair: Pair<String, String> = "140.00" to "63.50"
    private var heightPair: Pair<Int, String> = 67 to "170.0"

    private var heightIn: Int = 7
    private var heightFt: Int = 5
    private var selectMonth: String = "June"
    private var selectDay: String = "21"
    private var selectYear: String = "2018"
    private var selectPeriod: String = "Morning"


    //XML控件
    private lateinit var edtWeight: EditText
    private lateinit var edtHeight: EditText

    private lateinit var dayInput: TextView
    private lateinit var timeInput: TextView
    private lateinit var ageRecyclerView: RecyclerView
    private lateinit var ageAdapter: InputAgeAdapter
    private lateinit var sheetDialog: BottomSheetDialog
    private lateinit var sheetDialog2: BottomSheetDialog


    private val viewModel: BmiViewModel by viewModels {
        val db = BmiDatabase.getDatabase(requireContext())
        BmiViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAgeRecyclerView()
        initWeightAndHeightEdit()
        initConvertWeightAndHeight()
        initGender()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 挂起函数正常await等待查询完成
                val latestBmi = viewModel.getLatestBmi()
                latestBmi?.let {
                    bmiRecord = it
                }
                // 只初始化一次UI
                setupTime()
                setupGenderView()
                setupWeightAndHeight()
                ageRecyclerView.layoutManager?.scrollToPosition(bmiRecord.age - 2)

            }

        }


        // 根布局监听触摸，仅当前输入页生效
        binding.mergeDateInput.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) { // 改用UP，不会被子控件抢占DOWN
                val focusEt = view.findFocus() as? EditText ?: return@setOnTouchListener false

                // 获取EditText在根布局内的局部坐标
                val loc = IntArray(2)
                focusEt.getLocationInWindow(loc)
                val etLeft = loc[0]
                val etTop = loc[1]
                val etRight = etLeft + focusEt.width
                val etBottom = etTop + focusEt.height

                // 触摸全局坐标
                val x = event.rawX
                val y = event.rawY

                // 判断点击是否在EditText范围外
                val outSide = x < etLeft || x > etRight || y < etTop || y > etBottom
                if (outSide) {
                    focusEt.clearFocus()
                    hideSoftKeyboard(focusEt)
                }
            }
            false
        }

        // 跳转结果页面
        binding.dataInputCalculate.setOnClickListener {
            var weightKg = bmiRecord.weight
            if (!bmiRecord.weightUnit) weightKg = bmiRecord.weight * 0.45359236f
            var heightM = bmiRecord.height / 100f
            if (!bmiRecord.heightUnit) heightM = ((heightFt * 12) + heightIn) * 2.54f / 100f

            val bmi = weightKg / (heightM * heightM)

            val bmiLevel = BmiUtil.getBmiFullInfo(bmiRecord.age, bmiRecord.gender, bmi)
            bmiRecord.bmiColor = ContextCompat.getColor(requireContext(), bmiLevel.colorInt)

            bmiRecord.apply {
                height = heightM * 100f
                weight = weightKg
                bmiValue = bmi
                bmiGrade = bmiLevel.levelName
                createTime = System.currentTimeMillis()
                customTime = getCustomTimeStamp()
                timeText = "$selectMonth $selectDay,$selectYear $selectPeriod"
            }
            val intent = Intent(requireContext(), ResultActivity::class.java)
            intent.putExtra("BMI", bmiRecord)
            intent.putExtra("FATHER", "InputActivity")
            startActivity(intent)
        }

        // 跳转设置页面
        binding.mergeDateInput.settingsUser.setOnClickListener {
            val intent = Intent(requireContext(), SettingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getCustomTimeStamp(): Long {
        val monthMap = mapOf(
            "Jan" to 0, "Feb" to 1, "Mar" to 2, "Apr" to 3, "May" to 4, "June" to 5,
            "July" to 6, "Aug" to 7, "Sep" to 8, "Oct" to 9, "Nov" to 10, "Dec" to 11
        )
        val calendar = Calendar.getInstance()
        calendar.set(
            selectYear.toInt(),
            monthMap[selectMonth] ?: 0,
            selectDay.toInt(),
            when (selectPeriod) {
                "Morning" -> 9
                "Afternoon" -> 14
                "Evening" -> 19
                else -> 23 // Night
            }, 0, 0
        )
        return calendar.timeInMillis
    }

    // 选择性别
    private fun initGender() {
        val male = binding.mergeDateInput.cardMale
        val female = binding.mergeDateInput.cardFemale
        male.setOnClickListener {
            bmiRecord.gender = 1
            setupGenderView()

        }
        female.setOnClickListener {
            bmiRecord.gender = 0
            setupGenderView()
        }
    }

    // 性别控件
    private fun setupGenderView() {
        var m: Float
        var f: Float

        if (bmiRecord.gender == 1) {
            m = 1f
            f = 0.7f
        } else {
            m = 0.7f
            f = 1f
        }
        binding.mergeDateInput.cardMale.alpha = m
        binding.mergeDateInput.tvMale.alpha = m
        binding.mergeDateInput.ivMaleIcon.alpha = m
        if (bmiRecord.gender == 1) {
            binding.mergeDateInput.ivMaleCheck.visibility = View.VISIBLE
            binding.mergeDateInput.ivFemaleCheck.visibility = View.GONE
        } else {
            binding.mergeDateInput.ivMaleCheck.visibility = View.GONE
            binding.mergeDateInput.ivFemaleCheck.visibility = View.VISIBLE
        }

        binding.mergeDateInput.cardFemale.alpha = f
        binding.mergeDateInput.tvFemale.alpha = f
        binding.mergeDateInput.ivFemaleIcon.alpha = f

    }

    //选择身高体重单位
    private fun initConvertWeightAndHeight() {
        val lb = binding.mergeDateInput.switchWeightLb
        val kg = binding.mergeDateInput.switchWeightKg
        val cm = binding.mergeDateInput.switchHeightCm
        val ft = binding.mergeDateInput.switchHeightFt

        val density = resources.displayMetrics.density
        val movePx = -(76 * density)

        lb.setOnClickListener {
            if (bmiRecord.weightUnit) {
                bmiRecord.weightUnit = false

                binding.mergeDateInput.selectorThumbWeight.animate()
                    .translationX(0f)
                    .withLayer()
                    .start()

                var showText =  String.format("%.2f", bmiRecord.weight)
                binding.mergeDateInput.selectorThumbWeight.text = "lb"
                if(showText == weightPair.second) {
                    bmiRecord.weight = weightPair.first.toFloat()
                    showText = weightPair.first
                } else {
                    val originWeight = String.format("%.2f", bmiRecord.weight)
                    bmiRecord.weight /= 0.4536f
                    showText = String.format("%.2f", bmiRecord.weight)
                    val newPair = showText to originWeight
                    weightPair = newPair

                }
                edtWeight.setText(showText)
            }
        }
        kg.setOnClickListener {
            if (!bmiRecord.weightUnit) {
                bmiRecord.weightUnit = true
                binding.mergeDateInput.selectorThumbWeight.animate()
                    .translationX(-movePx)
                    .withLayer()
                    .start()
                binding.mergeDateInput.selectorThumbWeight.text = "kg"

                var showText = String.format("%.2f", bmiRecord.weight)
                if(showText == weightPair.first){
                    bmiRecord.weight = weightPair.second.toFloat()
                    showText = weightPair.second
                }else{
                    val originWeight = String.format("%.2f", bmiRecord.weight)
                    bmiRecord.weight *= 0.4536f
                    showText = String.format("%.2f", bmiRecord.weight)
                    val newPair = originWeight to showText
                    weightPair = newPair
                }
                edtWeight.setText(showText)
            }
        }

        ft.setOnClickListener {
            if (bmiRecord.heightUnit) {
                bmiRecord.heightUnit = false
                binding.mergeDateInput.selectorThumbHeight.animate()
                    .translationX(0f)
                    .withLayer()
                    .start()
                binding.mergeDateInput.selectorThumbHeight.text = "ft·in"

                binding.mergeDateInput.inputHeight.visibility = View.GONE
                binding.mergeDateInput.inputHeightFt.visibility = View.VISIBLE
                binding.mergeDateInput.inputHeightIn.visibility = View.VISIBLE
                binding.mergeDateInput.inputHeightFt1.visibility = View.VISIBLE
                binding.mergeDateInput.inputHeightIn1.visibility = View.VISIBLE


                val showText = String.format("%.1f", bmiRecord.height)
                Toast.makeText(requireContext(),"$showText 为什么不等于 ${heightPair.second}", Toast.LENGTH_SHORT).show()
                if(showText == heightPair.second){
                    heightFt = heightPair.first / 12
                    heightIn = heightPair.first % 12
                }else{

                    val originHeight = String.format("%.1f", bmiRecord.height)

                    val totalInch = (bmiRecord.height / 2.54f).toInt()
                    heightFt = totalInch / 12
                    heightIn = totalInch % 12
                    val newPair = totalInch to originHeight
                    heightPair = newPair

                }

                binding.mergeDateInput.inputHeightFt.setText(heightFt.toString())
                binding.mergeDateInput.inputHeightIn.setText(heightIn.toString())

            }
        }

        cm.setOnClickListener {
            if (!bmiRecord.heightUnit) {
                bmiRecord.heightUnit = true
                binding.mergeDateInput.selectorThumbHeight.animate()
                    .translationX(-movePx)
                    .withLayer()
                    .start()
                binding.mergeDateInput.selectorThumbHeight.text = "cm"

                binding.mergeDateInput.inputHeight.visibility = View.VISIBLE
                binding.mergeDateInput.inputHeightFt.visibility = View.GONE
                binding.mergeDateInput.inputHeightIn.visibility = View.GONE
                binding.mergeDateInput.inputHeightFt1.visibility = View.GONE
                binding.mergeDateInput.inputHeightIn1.visibility = View.GONE




                var showText = String.format("%.1f", bmiRecord.height)
                val totalInch = heightFt*12 +heightIn
                if(totalInch == heightPair.first){
                    showText == heightPair.second
                }else{

                    showText = String.format("%.1f", ((heightFt * 12) + heightIn) * 2.54f)
                    bmiRecord.height = showText.toFloat()
                    val newPair = totalInch to showText
                    heightPair = newPair

                }
                bmiRecord.height = ((heightFt * 12) + heightIn) * 2.54f

                binding.mergeDateInput.inputHeight.setText(showText)
            }
        }

    }

    private fun setupWeightAndHeight() {
        val density = resources.displayMetrics.density
        val movePx = -(76 * density)

        if (bmiRecord.weightUnit) {
            bmiRecord.weightUnit = true
            binding.mergeDateInput.selectorThumbWeight.animate()
                .translationX(-movePx)
                .withLayer()
                .start()
            binding.mergeDateInput.selectorThumbWeight.text = "kg"
        }
        if (bmiRecord.heightUnit) {
            bmiRecord.heightUnit = true
            binding.mergeDateInput.selectorThumbHeight.animate()
                .translationX(-movePx)
                .withLayer()
                .start()
            binding.mergeDateInput.selectorThumbHeight.text = "cm"

            binding.mergeDateInput.inputHeight.visibility = View.VISIBLE
            binding.mergeDateInput.inputHeightFt.visibility = View.GONE
            binding.mergeDateInput.inputHeightIn.visibility = View.GONE
            binding.mergeDateInput.inputHeightFt1.visibility = View.GONE
            binding.mergeDateInput.inputHeightIn1.visibility = View.GONE
        }

        edtWeight.setText(String.format("%.2f", bmiRecord.weight))
        edtHeight.setText(bmiRecord.height.toString())

        val edtHeightFt = binding.mergeDateInput.inputHeightFt
        val edtHeightIn = binding.mergeDateInput.inputHeightIn
        edtHeightFt.setText(heightFt.toString())
        edtHeightIn.setText(heightIn.toString())
    }

    //选择时间
    private fun setupTime() {
        initDatePickerBottomSheet()
        initDate2PickerBottomSheet()
        dayInput = binding.mergeDateInput.inputTime1
        timeInput = binding.mergeDateInput.inputTime2

        dayInput.setOnClickListener {
            sheetDialog.show()

        }
        timeInput.setOnClickListener {
            sheetDialog2.show()
        }
    }

    // 打开日期选择弹窗入口1
    fun initDatePickerBottomSheet() {
        sheetDialog = BottomSheetDialog(requireContext())
        val rootView =
            LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_date_picker, null)
        sheetDialog.setContentView(rootView)

        val wheelMonth: WheelView = rootView.findViewById(R.id.wheel_month)
        val wheelDay: WheelView = rootView.findViewById(R.id.wheel_day)
        val wheelYear: WheelView = rootView.findViewById(R.id.wheel_year)

        // 1. 基础常量数据源
        val monthData = listOf(
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "June",
            "July",
            "Aug",
            "Sep",
            "Oct",
            "Nov",
            "Dec"
        )
        val yearData = (1970..2036).map { it.toString() }
        // ===== 获取当前系统日期，计算对应滚轮下标 =====
        val nowCalendar = Calendar.getInstance()
        val currentYear = nowCalendar.get(Calendar.YEAR)
        val currentMonth = nowCalendar.get(Calendar.MONTH) // 0=Jan 刚好和monthData下标对应
        val currentDay = nowCalendar.get(Calendar.DAY_OF_MONTH)

        // 年份下标，偏移量 = 当前年 - 1970
        val yearSelectIndex = currentYear - 1970
        selectYear = currentYear.toString()
        // 月份下标等于Calendar.MONTH
        val monthSelectIndex = currentMonth
        selectMonth = monthData[currentMonth]
        // 日期初始下标 = 当前日 - 1（列表从1开始，下标0对应1号）
        val daySelectIndex = currentDay - 1
        selectDay = currentDay.toString()

        val boldTypeface: Typeface? =
            ResourcesCompat.getFont(requireContext(), R.font.font_bold_extrabold)

        // 2. 局部通用初始化滚轮方法
        fun initWheel(
            wheel: WheelView,
            dataList: List<String>,
            selectIndex: Int,
            typeface: Typeface?
        ) {
            wheel.adapter = ArrayWheelAdapter(dataList)
            wheel.currentItem = selectIndex
            wheel.setTypeface(typeface)
            wheel.setCyclic(false)
            wheel.setLineSpacingMultiplier(2f)
            wheel.setAlphaGradient(true)
            wheel.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.parent?.requestDisallowInterceptTouchEvent(false)
                        v.performClick()
                    }
                }
                false
            }
            wheel.setTextSize(16f)

        }

        // ========== 核心：动态生成当月天数函数 ==========
        fun getDayList(yearPos: Int, monthPos: Int): MutableList<String> {
            val targetYear = yearData[yearPos].toInt()
            // Calendar月份0=Jan，滚轮下标0正好对应Jan，不用偏移
            val targetMonth = monthPos
            val calendar = Calendar.getInstance()
            calendar.set(targetYear, targetMonth, 1)
            // 获取当月总天数
            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            // 生成 1~maxDay 字符串列表
            return (1..maxDay).map { it.toString() }.toMutableList()
        }

        // 3. 初始化年份、月份滚轮
        initWheel(wheelMonth, monthData, monthSelectIndex, boldTypeface)
        initWheel(wheelYear, yearData, yearSelectIndex, boldTypeface)

        // 4. 初始化日期（首次根据默认下标动态生成天数）
        var dayList = getDayList(yearSelectIndex, monthSelectIndex)
        initWheel(wheelDay, dayList, daySelectIndex, boldTypeface)


        // ========== 联动监听：切换月份/年份，自动刷新日期 ==========
        // 月份切换监听
        wheelMonth.setOnItemSelectedListener { _ ->
            val yearCur = wheelYear.currentItem
            val monthCur = wheelMonth.currentItem
            dayList = getDayList(yearCur, monthCur)
            // 刷新日期适配器
            // 防止原来选中的天数超过当月最大天数，自动修正下标
            Toast.makeText(
                requireContext(),
                "${wheelDay.currentItem} ${dayList.size}",
                Toast.LENGTH_SHORT
            )
                .show()
            if (wheelDay.currentItem >= dayList.size) {
                wheelDay.currentItem = dayList.size - 1

            }
            wheelDay.adapter = ArrayWheelAdapter(dayList)
            wheelDay.invalidate()
            wheelDay.setTypeface(boldTypeface)
        }

        // 年份切换监听（闰年2月天数变化）
        wheelYear.setOnItemSelectedListener { _ ->
            val yearCur = wheelYear.currentItem
            val monthCur = wheelMonth.currentItem
            dayList = getDayList(yearCur, monthCur)
            wheelDay.adapter = ArrayWheelAdapter(dayList)
            if (wheelDay.currentItem >= dayList.size) {
                wheelDay.currentItem = dayList.size - 1
            }
            wheelDay.setTypeface(boldTypeface)
        }
        rootView.findViewById<Button>(R.id.btn_cancel).setOnClickListener { sheetDialog.dismiss() }
        rootView.findViewById<Button>(R.id.btn_done).setOnClickListener {
            selectMonth = monthData[wheelMonth.currentItem]
            selectDay = dayList[wheelDay.currentItem]
            selectYear = yearData[wheelYear.currentItem]

            dayInput.text = "$selectMonth $selectDay, $selectYear"
            // 业务逻辑：回调日期
            sheetDialog.dismiss()
        }
    }

    // 打开日期选择弹窗入口2
    private fun initDate2PickerBottomSheet() {
        sheetDialog2 = BottomSheetDialog(requireContext())
        val rootView =
            LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_date2_picker, null)
        sheetDialog2.setContentView(rootView)

        val wheelPeriod: WheelView = rootView.findViewById(R.id.wheel_time)

// 1. 单列数据源
        val periodData = listOf(
            "Morning",
            "Afternoon",
            "Evening",
            "Night"
        )

// 根据当前小时自动匹配对应时段下标
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val defaultSelectIndex = when (// Morning 6:00 ~ 11:59
            hour) {
            in 6..11 -> 0
            // Afternoon 12:00 ~ 17:59
            in 12..17 -> 1
            // Evening 18:00 ~ 22:59
            in 18..22 -> 2
            // Night 23:00 ~ 5:59
            else -> 3
        }

        selectPeriod = periodData[defaultSelectIndex]

        val boldTypeface: Typeface? =
            ResourcesCompat.getFont(requireContext(), R.font.font_bold_extrabold)

        // 2. 复用你原来通用滚轮初始化方法
        fun initWheel(
            wheel: WheelView,
            dataList: List<String>,
            selectIndex: Int,
            typeface: Typeface?
        ) {
            wheel.adapter = ArrayWheelAdapter(dataList)
            wheel.currentItem = selectIndex
            wheel.setTypeface(typeface)
            wheel.setCyclic(false)
            wheel.setLineSpacingMultiplier(2f)
            wheel.setAlphaGradient(true)
            wheel.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.parent?.requestDisallowInterceptTouchEvent(false)
                        v.performClick()
                    }
                }
                false
            }
            wheel.setTextSize(16f)
        }

        // 3. 初始化时段滚轮
        initWheel(wheelPeriod, periodData, defaultSelectIndex, boldTypeface)

        // 取消、确认按钮逻辑
        rootView.findViewById<Button>(R.id.btn_cancel).setOnClickListener { sheetDialog2.dismiss() }
        rootView.findViewById<Button>(R.id.btn_done).setOnClickListener {
            selectPeriod = periodData[wheelPeriod.currentItem]
            timeInput.text = selectPeriod
            sheetDialog2.dismiss()
        }
    }

    // 年龄选择
    private fun initAgeRecyclerView() {
        ageRecyclerView = binding.mergeDateInput.inputAge
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        ageRecyclerView.layoutManager = layoutManager

        ageAdapter = InputAgeAdapter(ageRecyclerView) { selectedAgeInt ->
            bmiRecord.age = selectedAgeInt
        }
        ageRecyclerView.adapter = ageAdapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(ageRecyclerView)

        // ========== 滚动透明度监听 ==========
        ageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private val minAlpha = 0f
            private val maxAlpha = 1f
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as LinearLayoutManager
                val rvCenterX = recyclerView.width / 2f
                val firstVisible = lm.findFirstVisibleItemPosition()
                val lastVisible = lm.findLastVisibleItemPosition()

                for (pos in firstVisible - 1..lastVisible + 1) {
                    val itemView = lm.findViewByPosition(pos) ?: continue
                    val itemCenterX = itemView.left + itemView.width / 2f
                    val distance = abs(itemCenterX - rvCenterX)
                    val maxDistance = recyclerView.width / 2f
                    var ratio = 1 - (distance / maxDistance)
                    ratio = ratio.coerceAtLeast(0f)
                    val alpha = minAlpha + maxAlpha * ratio
                    itemView.alpha = alpha
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // 当列表停止滚动时（SCROLL_STATE_IDLE）
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val lm = recyclerView.layoutManager as LinearLayoutManager
                    // 利用 SnapHelper 精准找到当前居中的 View
                    val centerView = snapHelper.findSnapView(lm)
                    if (centerView != null) {
                        val centerAge = lm.getPosition(centerView) + 2
                        if (bmiRecord.age != centerAge) {
                            bmiRecord.age = centerAge
                        }
                    }
                }
            }
        })


    }

    // 身高体重
    private fun initWeightAndHeightEdit() {
        edtWeight = binding.mergeDateInput.inputWeight
        edtHeight = binding.mergeDateInput.inputHeight

        val edtHeightFt = binding.mergeDateInput.inputHeightFt
        val edtHeightIn = binding.mergeDateInput.inputHeightIn

        val editTexts = listOf(edtHeightFt, edtHeightIn, edtWeight, edtHeight)
        editTexts.forEach { et ->
            et.doAfterTextChanged { editable ->
                val text = editable.toString()
                // 空输入直接跳过，避免数字转换崩溃
                if (text.isBlank()) return@doAfterTextChanged
                try {
                    when (et) {
                        edtWeight -> {
                            bmiRecord.weight = text.toFloat()
                        }

                        edtHeight -> {
                            bmiRecord.height = text.toFloat()
                        }

                        edtHeightFt -> {
                            heightFt = text.toInt()
                        }

                        edtHeightIn -> {
                            heightIn = text.toInt()
                        }
                    }
                } catch (_: NumberFormatException) {
                }
            }
            et.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    // 失焦：校验当前这个EditText
                    checkNumberValid()
                } else {
                    // 获焦：清空错误提示
                    et.error = null
                }
            }

            // 回车：清除焦点
            et.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideSoftKeyboard(et)
                    et.clearFocus() // 清除焦点，自动走onFocusChange校验
                    return@setOnEditorActionListener true
                }
                false
            }
        }
    }

    //检查数值合法
    private fun checkNumberValid(): Boolean {
        //  1. 体重安全判定（防御空输入闪退）
        val weightInputStr = edtWeight.text.toString()
        if (weightInputStr.isBlank()) {
            Toast.makeText(requireContext(), "请输入体重", Toast.LENGTH_SHORT).show()
            edtWeight.setText("140.00")
            return false // 严格拦截，不放行
        }

        // 使用 OrNull 兜底，防止输入带有非法字符时崩溃
        val inputWeight = weightInputStr.toFloatOrNull() ?: 0f

        if (!bmiRecord.weightUnit) {
            // ----- LB 磅模式验证 -----
            if (inputWeight !in 2f..551f) {
                edtWeight.setText("551.00")

                bmiRecord.weight = 551f * 0.45359236f
                Toast.makeText(requireContext(), "体重超出范围( 2 - 551 lb)", Toast.LENGTH_SHORT)
                    .show()
                return false // 拦截
            }
        } else {
            // ----- KG 公斤模式验证 -----
            if (inputWeight !in 1f..250f) {
                edtWeight.setText("250.00")
                bmiRecord.weight = 250f // 纯正标准 kg 赋值
                Toast.makeText(requireContext(), "体重超出范围( 1 - 250 kg)", Toast.LENGTH_SHORT)
                    .show()
                return false
            }
        }

        //  2. 身高安全判定
        if (!bmiRecord.heightUnit) {
            // ----- FT·IN 英制模式验证 -----
            val edtHeightFt = binding.mergeDateInput.inputHeightFt
            val edtHeightIn = binding.mergeDateInput.inputHeightIn

            // 安全捕获整型数值，输入框为空则默认为 0
            val ft = edtHeightFt.text.toString().toIntOrNull() ?: 0
            val inch = edtHeightIn.text.toString().toIntOrNull() ?: 0

            if (ft !in 1..8) {
                edtHeightFt.setText("8")
                heightFt = 8
                Toast.makeText(requireContext(), "身高超出范围( 1 - 8 ft)", Toast.LENGTH_SHORT)
                    .show()
                return false
            }

            if (inch !in 0..11) {
                edtHeightIn.setText("11")
                heightIn = 11
                Toast.makeText(requireContext(), "身高超出范围( 0 - 11 in)", Toast.LENGTH_SHORT)
                    .show()
                return false
            }

        } else {
            // ----- CM 公制模式验证 -----
            val heightInputStr = edtHeight.text.toString()
            if (heightInputStr.isBlank()) {
                Toast.makeText(requireContext(), "请输入身高", Toast.LENGTH_SHORT).show()
                return false
            }
            val inputHeight = heightInputStr.toFloatOrNull() ?: 0f

            if (inputHeight !in 1f..250.0f) {
                edtHeight.setText("150.0")
                bmiRecord.height = 150f
                Toast.makeText(requireContext(), "身高超出范围( 1 - 250 cm)", Toast.LENGTH_SHORT)
                    .show()
                return false
            }
        }

        // 只有全部验证通过，且底层变量成功被公斤、厘米归一化覆盖后，才大方放行 true 允许写入数据库
        return true
    }


    //隐藏软键盘
    private fun hideSoftKeyboard(view: View) {
        val imm = getSystemService(view.context, InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}