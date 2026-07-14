package com.example.bmicalculator.fragment

import android.annotation.SuppressLint
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
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
import com.example.bmicalculator.ui.ResultActivity
import com.example.bmicalculator.ui.SettingActivity
import com.example.bmicalculator.viewmodel.InputViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs

@SuppressLint("DefaultLocale", "SetTextI18n")
class DataInputFragment : Fragment() {

    private var _binding: FragmentDataInputBinding? = null
    private val binding get() = checkNotNull(_binding)


    //XML控件
    private lateinit var edtWeight: EditText
    private lateinit var edtHeight: EditText

    private lateinit var dayInput: TextView
    private lateinit var timeInput: TextView
    private lateinit var ageRecyclerView: RecyclerView
    private lateinit var ageAdapter: InputAgeAdapter
    private lateinit var sheetDialog: BottomSheetDialog
    private lateinit var sheetDialog2: BottomSheetDialog

    private var isFirstData = true

    private val viewModel: InputViewModel by viewModels {
        val db = BmiDatabase.getDatabase(requireContext())
        InputViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility", "DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTime()
        initAgeRecyclerView()
        initWeightAndHeightEdit()
        initConvertWeightAndHeight()
        initGender()
        viewLifecycleOwner.lifecycleScope.launch {
            // 挂起函数正常await等待查询完成
            val latestBmi = viewModel.getLatestBmi()
            if (latestBmi != null) {
                viewModel.inputBmiRecord = latestBmi.copy(id = 0)
                isFirstData = false
            } else isFirstData = true
            // 只初始化一次UI
            setupGenderView()
            setupWeightAndHeight()
            ageRecyclerView.layoutManager?.scrollToPosition(viewModel.inputBmiRecord.age )
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
            if (!checkNumberValid()) {
                return@setOnClickListener
            }

            val finalBmi = viewModel.computeFullBmi(requireContext())
            val intent = Intent(requireContext(), ResultActivity::class.java)
            intent.putExtra("BMI", finalBmi)
            intent.putExtra("FATHER", isFirstData)
            startActivity(intent)
        }

        // 跳转设置页面
        binding.settingsUser.setOnClickListener {
            val intent = Intent(requireContext(), SettingActivity::class.java)
            startActivity(intent)
        }
    }

    // 选择性别
    private fun initGender() {
        val male = binding.mergeDateInput.cardMale
        val female = binding.mergeDateInput.cardFemale
        male.setOnClickListener {
            viewModel.inputBmiRecord.gender = 1
            setupGenderView()

        }
        female.setOnClickListener {
            viewModel.inputBmiRecord.gender = 0
            setupGenderView()
        }
    }

    // 性别控件
    private fun setupGenderView() {
        var m: Float
        var f: Float

        if (viewModel.inputBmiRecord.gender == 1) {
            m = 1f
            f = 0.7f
        } else {
            m = 0.7f
            f = 1f
        }
        binding.mergeDateInput.cardMale.alpha = m
        binding.mergeDateInput.tvMale.alpha = m
        binding.mergeDateInput.ivMaleIcon.alpha = m
        if (viewModel.inputBmiRecord.gender == 1) {
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

        // kg -> lb
        lb.setOnClickListener {
            if (!checkNumberValid()) {
                return@setOnClickListener
            }
            if (viewModel.inputBmiRecord.weightUnit) {
                viewModel.inputBmiRecord.weightUnit = false

                binding.mergeDateInput.selectorThumbWeight.animate()
                    .translationX(0f)
                    .withLayer()
                    .start()
                binding.mergeDateInput.selectorThumbWeight.text = "lb"

                val showText = viewModel.switchWeightUnitToLb()
                edtWeight.setText(showText)
            }
        }

        // lb -> kg
        kg.setOnClickListener {
            if (!checkNumberValid()) {
                return@setOnClickListener
            }
            if (!viewModel.inputBmiRecord.weightUnit) {
                viewModel.inputBmiRecord.weightUnit = true
                binding.mergeDateInput.selectorThumbWeight.animate()
                    .translationX(-movePx)
                    .withLayer()
                    .start()
                binding.mergeDateInput.selectorThumbWeight.text = "kg"

                val showText = viewModel.switchWeightUnitToKg()
                edtWeight.setText(showText)
            }
        }

        // cm -> ft
        ft.setOnClickListener {
            if (!checkNumberValid()) {
                return@setOnClickListener
            }
            if (viewModel.inputBmiRecord.heightUnit) {
                viewModel.inputBmiRecord.heightUnit = false
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


                viewModel.switchHeightUnitToFtIn()

                binding.mergeDateInput.inputHeightFt.setText(viewModel.inputBmiRecord.heightFt.toString())
                binding.mergeDateInput.inputHeightIn.setText(viewModel.inputBmiRecord.heightIn.toString())

            }
        }

        // ft -> cm
        cm.setOnClickListener {
            if (!checkNumberValid()) {
                return@setOnClickListener
            }
            if (!viewModel.inputBmiRecord.heightUnit) {
                viewModel.inputBmiRecord.heightUnit = true
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

                val showText = viewModel.switchHeightUnitToCm()
                binding.mergeDateInput.inputHeight.setText(showText)
            }
        }

    }


    private fun setupWeightAndHeight() {
        val density = resources.displayMetrics.density
        val movePx = -(76 * density)

        if (viewModel.inputBmiRecord.weightUnit) {
            viewModel.inputBmiRecord.weightUnit = true
            binding.mergeDateInput.selectorThumbWeight.animate()
                .translationX(-movePx)
                .withLayer()
                .start()
            binding.mergeDateInput.selectorThumbWeight.text = "kg"
        }
        if (viewModel.inputBmiRecord.heightUnit) {
            viewModel.inputBmiRecord.heightUnit = true
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

        edtWeight.setText(String.format("%.2f", viewModel.inputBmiRecord.weight))
        edtHeight.setText(viewModel.inputBmiRecord.height.toString())

        val edtHeightFt = binding.mergeDateInput.inputHeightFt
        val edtHeightIn = binding.mergeDateInput.inputHeightIn
        edtHeightFt.setText(viewModel.inputBmiRecord.heightFt.toString())
        edtHeightIn.setText(viewModel.inputBmiRecord.heightIn.toString())
    }

    //选择时间
    private fun setupTime() {
        dayInput = binding.mergeDateInput.inputTime1
        timeInput = binding.mergeDateInput.inputTime2

        initDatePickerBottomSheet()
        initDate2PickerBottomSheet()

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
            LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_date_picker, binding.root, false)
        sheetDialog.setContentView(rootView)

        val wheelMonth: WheelView = rootView.findViewById(R.id.wheel_month)
        val wheelDay: WheelView = rootView.findViewById(R.id.wheel_day)
        val wheelYear: WheelView = rootView.findViewById(R.id.wheel_year)

        // 1. 基础常量数据源
        val monthData = resources.getStringArray(R.array.month_short_names).toList()
        val yearData = (1970..2036).map { it.toString() }
        // ===== 获取当前系统日期，计算对应滚轮下标 =====
        val nowCalendar = Calendar.getInstance()
        val currentYear = nowCalendar.get(Calendar.YEAR)
        val currentMonth = nowCalendar.get(Calendar.MONTH) // 0=Jan 刚好和monthData下标对应
        val currentDay = nowCalendar.get(Calendar.DAY_OF_MONTH)

        // 下标，偏移量
        val yearSelectIndex = currentYear - 1970
        viewModel.selectYear = currentYear.toString()
        val monthSelectIndex = currentMonth
        viewModel.selectMonth = monthData[currentMonth]
        val daySelectIndex = currentDay - 1
        viewModel.selectDay = currentDay.toString()

        dayInput.text = "${viewModel.selectMonth} ${viewModel.selectDay}, ${viewModel.selectYear}"

        val boldTypeface: Typeface? =
            ResourcesCompat.getFont(requireContext(), R.font.font_bold_extrabold)

        // 2. 局部通用初始化滚轮方法
        @SuppressLint("ClickableViewAccessibility")
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
            viewModel.selectMonth = monthData[wheelMonth.currentItem]
            viewModel.selectDay = dayList[wheelDay.currentItem]
            viewModel.selectYear = yearData[wheelYear.currentItem]

            dayInput.text =
                "${viewModel.selectMonth} ${viewModel.selectDay}, ${viewModel.selectYear}"
            // 业务逻辑：回调日期
            sheetDialog.dismiss()
        }
    }

    // 打开日期选择弹窗入口2
    private fun initDate2PickerBottomSheet() {
        sheetDialog2 = BottomSheetDialog(requireContext())
        val rootView =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_date2_picker, binding.root, false)
        sheetDialog2.setContentView(rootView)

        val wheelPeriod: WheelView = rootView.findViewById(R.id.wheel_time)

        // 1. 单列数据源
        val periodData = listOf(
            requireContext().getString(R.string.morning),
            requireContext().getString(R.string.afternoon),
            requireContext().getString(R.string.evening),
            requireContext().getString(R.string.night)
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

        viewModel.selectPeriod = periodData[defaultSelectIndex]
        binding.mergeDateInput.inputTime2.text = viewModel.selectPeriod
        val boldTypeface: Typeface? =
            ResourcesCompat.getFont(requireContext(), R.font.font_bold_extrabold)

        // 2. 复用滚轮初始化
        @SuppressLint("ClickableViewAccessibility")
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
            viewModel.selectPeriod = periodData[wheelPeriod.currentItem]
            timeInput.text = viewModel.selectPeriod
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
            viewModel.inputBmiRecord.age = selectedAgeInt
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
                        if (viewModel.inputBmiRecord.age != centerAge) {
                            viewModel.inputBmiRecord.age = centerAge
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
                        edtWeight -> viewModel.inputBmiRecord.weight = text.toFloat()
                        edtHeight -> viewModel.inputBmiRecord.height = text.toFloat()
                        edtHeightFt -> viewModel.inputBmiRecord.heightFt = text.toInt()
                        edtHeightIn -> viewModel.inputBmiRecord.heightIn = text.toInt()
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
        val checkRes = viewModel.checkInputValid()
        if (!checkRes.pass) {
            checkRes.toastMsgRes?.let {
                Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()
            }
            // 按校验结果重置输入框
            checkRes.resetWeight?.let { edtWeight.setText(String.format("%.2f", it)) }
            checkRes.resetFt?.let { binding.mergeDateInput.inputHeightFt.setText(it.toString()) }
            checkRes.resetInch?.let { binding.mergeDateInput.inputHeightIn.setText(it.toString()) }
            checkRes.resetHeight?.let { edtHeight.setText(String.format("%.1f", it)) }
        }
        return checkRes.pass
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