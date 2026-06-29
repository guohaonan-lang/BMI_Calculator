package com.example.bmicalculator.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter
import com.contrarywind.view.WheelView
import com.example.bmicalculator.R
import com.example.bmicalculator.adapter.InputAgeAdapter
import com.example.bmicalculator.databinding.ActivityDataInputBinding
import com.example.bmicalculator.model.BmiEntity
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Calendar

class DataInputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataInputBinding

    private var selectMonth: String = "June"
    private var selectDay: String = "21"
    private var selectYear: String = "2018"
    private lateinit var dayinput: TextView
    private lateinit var timeinput: TextView
    private var data: BmiEntity = BmiEntity(1, 140.00f, 65.00f, 33f, 25, 0, 436, 5325325)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDataInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupWeightAndHeight()
        setupTime()
        setupAgeRecyclerView()

        binding.dataInputCalculate.setOnClickListener {
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupTime() {
        dayinput = binding.mergeDateInput.inputTime1
        timeinput = binding.mergeDateInput.inputTime2

        dayinput.setOnClickListener {
            showDatePickerBottomSheet()

        }
    }

    // 打开日期选择弹窗入口（按钮点击调用此方法）
    fun showDatePickerBottomSheet() {
        val sheetDialog = BottomSheetDialog(this)
        val rootView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_date_picker, null)
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
        // 月份下标等于Calendar.MONTH
        val monthSelectIndex = currentMonth
        // 日期初始下标 = 当前日 - 1（列表从1开始，下标0对应1号）
        val daySelectIndex = currentDay - 1

        val boldTypeface: Typeface? = ResourcesCompat.getFont(this, R.font.font_bold_extrabold)

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
            Toast.makeText(this,"${wheelDay.currentItem} ${dayList.size}",Toast.LENGTH_SHORT).show()
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

            dayinput.text = selectMonth + " " + selectDay + ", " + selectYear
            // 业务逻辑：回调日期
            sheetDialog.dismiss()
        }

        sheetDialog.show()

    }

    private fun setupWeightAndHeight() {
        val edtWeight = binding.mergeDateInput.inputWeight
        val edtHeight = binding.mergeDateInput.inputHeight

        val heightFloat: Float = edtHeight.text.toString().toFloat()
        val weightFloat: Float = edtHeight.text.toString().toFloat()

        data.height = heightFloat
        data.weight = weightFloat
    }

    private fun setupAgeRecyclerView() {
        val ageRecyclerView = binding.mergeDateInput.inputAge
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        ageRecyclerView.layoutManager = layoutManager
        val adapter = InputAgeAdapter(ageRecyclerView) {

        }
        ageRecyclerView.adapter = adapter
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(ageRecyclerView)

        // ========== 滚动透明度监听，全部放入函数内 ==========
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
                    val distance = kotlin.math.abs(itemCenterX - rvCenterX)
                    val maxDistance = recyclerView.width / 2f
                    var ratio = 1 - (distance / maxDistance)
                    ratio = ratio.coerceAtLeast(0f)

                    // 透明度计算
                    val alpha = minAlpha + maxAlpha * ratio

                    itemView.alpha = alpha
                }
            }
        })

        // 页面加载完成后手动刷新一次透明度（刚进来没滑动时生效）
        ageRecyclerView.post {
            ageRecyclerView.scrollBy(1, 0)
            ageRecyclerView.scrollBy(-1, 0)
        }
    }
}