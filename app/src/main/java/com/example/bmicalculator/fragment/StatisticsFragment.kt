package com.example.bmicalculator.fragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.ui.MainActivity
import com.example.bmicalculator.util.BmiMarkerView
import com.example.bmicalculator.util.SmartXAxisRenderer
import com.example.bmicalculator.util.WeightMarkerView
import com.example.bmicalculator.viewmodel.BmiViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.util.Calendar

class StatisticsFragment : Fragment() {
    private lateinit var bmiChart: LineChart
    private lateinit var weightChart: LineChart
    private lateinit var dayOfData: TextView
    private lateinit var weekOfData: TextView
    private lateinit var monthOfData: TextView
    private lateinit var thumbTime: TextView
    private lateinit var update1: TextView
    private lateinit var update2: TextView
    private val chartFont by lazy {
        ResourcesCompat.getFont(requireContext(), R.font.font_extrabold)
    }

    // 图表X轴日期标签集合
    private val xLabelList = mutableListOf<String>()

    enum class TimeMode { DAY, WEEK, MONTH }

    private var currentTimeMode = TimeMode.DAY // 默认是天
    private var rawBmiData: List<BmiEntity> = emptyList() // 缓存一份从数据库拿到的原始全量数据

    private var mBaseTimeZero: Long = 0L // 类级别变量
    private val viewModel: BmiViewModel by viewModels {
        val db = BmiDatabase.getDatabase(requireContext())
        BmiViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)
        bmiChart = view.findViewById(R.id.chart_bmi)
        weightChart = view.findViewById(R.id.chart_weight)
        dayOfData = view.findViewById(R.id.switch_time_day)
        weekOfData = view.findViewById(R.id.switch_time_week)
        monthOfData = view.findViewById(R.id.switch_time_month)
        thumbTime = view.findViewById(R.id.selector_thumb_time)

        update1 = view.findViewById(R.id.chart_update1)
        update2 = view.findViewById(R.id.chart_update2)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initChartStyle(bmiChart)
        initChartStyle(weightChart)
        setChartData()

        initSwitchTime()
        initUpdate()
    }

    private fun initUpdate() {
        update1.setOnClickListener {
            val mainActivity = requireActivity() as MainActivity
            mainActivity.binding.mainViewpage2.currentItem = 0
        }
        update2.setOnClickListener {
            val mainActivity = requireActivity() as MainActivity
            mainActivity.binding.mainViewpage2.currentItem = 0
        }
    }

    // 切换时间周期
    private fun initSwitchTime() {
        val density = resources.displayMetrics.density
        val movePx = -(115 * density)
        dayOfData.setOnClickListener {
            thumbTime.animate()
                .translationX(0f)
                .withLayer()
                .start()
            thumbTime.text = "Day"

            currentTimeMode = TimeMode.DAY
            processAndRenderData(rawBmiData, bmiChart)
            processAndRenderData(rawBmiData, weightChart)
        }
        weekOfData.setOnClickListener {
            thumbTime.animate()
                .translationX(-movePx)
                .withLayer()
                .start()
            thumbTime.text = "Week"
            currentTimeMode = TimeMode.WEEK
            processAndRenderData(rawBmiData, bmiChart)
            processAndRenderData(rawBmiData, weightChart)
        }
        monthOfData.setOnClickListener {
            thumbTime.animate()
                .translationX(-movePx * 2)
                .withLayer()
                .start()
            thumbTime.text = "Month"
            currentTimeMode = TimeMode.MONTH
            processAndRenderData(rawBmiData, bmiChart)
            processAndRenderData(rawBmiData, weightChart)
        }
    }

    // 初始化表格样式
    private fun initChartStyle(lineChart: LineChart) {
        lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)

            // 滑动
            setScaleEnabled(false)
            isDragXEnabled = true
            isDragYEnabled = false

            // 边距
            extraLeftOffset = 15f
            extraBottomOffset = 15f
            extraTopOffset = 25f
            extraRightOffset = 15f

        }
        val xAxis: XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTH_SIDED
        xAxis.gridColor = 0xFFDDDDDD.toInt()
        xAxis.axisLineColor = Color.TRANSPARENT
        xAxis.labelCount = 8

        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        // 字体
        xAxis.typeface = chartFont
        xAxis.textSize = 12f
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.white)


        val leftY: YAxis = lineChart.axisLeft
        leftY.gridColor = 0xFFDDDDDD.toInt()
        leftY.axisLineColor = Color.TRANSPARENT
        leftY.setLabelCount(6, true)
        leftY.setDrawGridLines(false)

        leftY.typeface = chartFont
        leftY.textSize = 12f
        leftY.textColor = ContextCompat.getColor(requireContext(), R.color.white)

        lineChart.axisRight.isEnabled = false
    }

    //  BMI表格
    private fun renderBmiChart(entries: MutableList<Entry>) {
        if (entries.isEmpty()) {
            bmiChart.clear()
            bmiChart.invalidate()
            return
        }

        val marker = BmiMarkerView(requireContext())
        marker.chartView = bmiChart // 必须设置，边缘自动防裁切
        bmiChart.marker = marker

        val dataSet = LineDataSet(entries, "BMI曲线").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            lineWidth = 1f
            color = Color.WHITE

            circleRadius = 3f
            setCircleColor(Color.WHITE)
            circleHoleRadius = 1f
            circleHoleColor = Color.WHITE

            setDrawFilled(true)
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    0x80FFFFFF.toInt(),
                    0x08FFFFFF
                )
            )
            fillDrawable = gradient
            setDrawValues(false)
        }

        bmiChart.data = LineData(dataSet)

        // ===== 1. 绑定底部的标签转换 =====
        bmiChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index in xLabelList.indices) {
                    xLabelList[index]
                } else {
                    ""
                }
            }
        }

        // 注入顶部渲染管线
        bmiChart.setXAxisRenderer(
            SmartXAxisRenderer(
                bmiChart.viewPortHandler,
                bmiChart.xAxis,
                bmiChart.getTransformer(YAxis.AxisDependency.LEFT),
                { mBaseTimeZero },
                { currentTimeMode }
            )
        )

        val maxShowCount = 8f

        // 横向可见最大格数
        bmiChart.setVisibleXRangeMaximum(maxShowCount)

        bmiChart.xAxis.apply {
            isGranularityEnabled = true
            granularity = 1f
            labelCount = maxShowCount.toInt()
//            setForceLabelsEnabled(false)
        }

        bmiChart.data?.dataSets?.forEach { dataSet ->
            dataSet.valueTypeface = chartFont
        }

        // 通知图表刷新其内部基础状态并执行统一单次重绘
        bmiChart.notifyDataSetChanged()
        bmiChart.invalidate()

        // ===== 3. 将清除放大矩阵、重新卡死边界与右移安全抛入 post 队列 =====
        bmiChart.post {
            bmiChart.viewPortHandler.matrixTouch.reset()

            bmiChart.setVisibleXRangeMaximum(maxShowCount)
            bmiChart.setVisibleXRangeMinimum(maxShowCount)

            // 完美滚动到最新一页
            bmiChart.moveViewToX(bmiChart.xAxis.axisMaximum - maxShowCount + 1f)
        }
    }


    //  体重表格
    private fun renderWeightChart(entries: MutableList<Entry>) {
        if (entries.isEmpty()) {
            weightChart.clear()
            weightChart.invalidate()
            return
        }

        val marker = WeightMarkerView(requireContext())
        marker.chartView = weightChart // 必须设置，边缘自动防裁切
        weightChart.marker = marker

        val dataSet = LineDataSet(entries, "BMI曲线").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            lineWidth = 1f
            color = Color.WHITE

            circleRadius = 3f
            setCircleColor(Color.WHITE)
            circleHoleRadius = 1f
            circleHoleColor = Color.WHITE

            setDrawFilled(true)
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    0x80FFFFFF.toInt(),
                    0x08FFFFFF
                )
            )
            fillDrawable = gradient
            setDrawValues(false)
        }

        weightChart.data = LineData(dataSet)

        // ===== 1. 绑定底部的标签转换 =====
        weightChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index in xLabelList.indices) {
                    xLabelList[index]
                } else {
                    ""
                }
            }
        }

        // ===== 🌟【核心修复：重新注入顶部渲染管线】 =====
        // 必须在设置完 data 之后、调用 invalidate 之前注入，否则顶部不会触发 drawLabel
        weightChart.setXAxisRenderer(
            SmartXAxisRenderer(
                weightChart.viewPortHandler,
                weightChart.xAxis,
                weightChart.getTransformer(YAxis.AxisDependency.LEFT),
                { mBaseTimeZero },
                { currentTimeMode }
            )
        )

        // ===== 2. 根据当前的模式，确定首屏看多少天/周/月 =====
        val maxShowCount = 8f

        // 锁死横向可见最大格数
        weightChart.setVisibleXRangeMaximum(maxShowCount)

        weightChart.xAxis.apply {
            isGranularityEnabled = true
            granularity = 1f
            labelCount = maxShowCount.toInt()
        }



        weightChart.data?.dataSets?.forEach { dataSet ->
            dataSet.valueTypeface = chartFont
        }

        // 通知图表刷新其内部基础状态并执行统一单次重绘
        weightChart.notifyDataSetChanged()
        weightChart.invalidate()

        // ===== 3. 将清除放大矩阵、重新卡死边界与右移安全抛入 post 队列 =====
        weightChart.post {
            weightChart.viewPortHandler.matrixTouch.reset()

            weightChart.setVisibleXRangeMaximum(maxShowCount)
            weightChart.setVisibleXRangeMinimum(maxShowCount)
            // 完美滚动到最新一页
            weightChart.moveViewToX(weightChart.xAxis.axisMaximum - maxShowCount + 1f)
        }
    }

    // 监听数据
    private fun setChartData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chartBmiList.collect { data ->
                    rawBmiData = data
                    processAndRenderData(rawBmiData, bmiChart)
                    processAndRenderData(rawBmiData, weightChart)
                }
            }
        }
    }

    // 处理数据
    private fun processAndRenderData(data: List<BmiEntity>, lineChart: LineChart) {
        if (data.isEmpty()) {
            lineChart.clear()
            lineChart.invalidate()
            return
        }

        xLabelList.clear()
        val entries = mutableListOf<Entry>()
        val valueIndexMap = mutableMapOf<Int, Float>() // 下标 i -> BMI/体重数值

        // 1. 基准时间：获取最新一条数据，并【严格抹平内部时分秒为凌晨零点】！
        val latestTime = data.last().customTime
        val baseCal = Calendar.getInstance().apply {
            timeInMillis = latestTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val totalCount: Int
        val calendarStep: Int
        val calendarUnit: Int

        when (currentTimeMode) {
            TimeMode.DAY -> {
                totalCount = 90
                calendarUnit = Calendar.DAY_OF_YEAR
                calendarStep = -1
            }

            TimeMode.WEEK -> {
                totalCount = 54
                // 为了周模式完美对齐，强制把基准时间挪到该自然周的第一天（如周一）
                baseCal.set(Calendar.DAY_OF_WEEK, baseCal.firstDayOfWeek)
                calendarUnit = Calendar.WEEK_OF_YEAR
                calendarStep = -1
            }

            TimeMode.MONTH -> {
                totalCount = 60
                // 为了月模式完美对齐，强制把基准时间挪到该月 1 号
                baseCal.set(Calendar.DAY_OF_MONTH, 1)
                calendarUnit = Calendar.MONTH
                calendarStep = -1
            }
        }

        // 2. 从最新时间向前倒推，生成【严格无副作用】的刻度轴和时间戳轴
        val tempCal = baseCal.clone() as Calendar
        val tempLabelList = mutableListOf<String>()
        val tempTimeList = mutableListOf<Long>()

        for (i in 0 until totalCount) {
            tempTimeList.add(tempCal.timeInMillis)

            // 生成标准 X 轴标签文字
            val label = when (currentTimeMode) {
                TimeMode.DAY -> tempCal.get(Calendar.DAY_OF_MONTH).toString()
                TimeMode.WEEK -> "${tempCal.get(Calendar.DAY_OF_MONTH)}"
                TimeMode.MONTH -> "${tempCal.get(Calendar.MONTH) + 1}"
            }
            tempLabelList.add(label)

            // 向前偏移一个周期
            tempCal.add(calendarUnit, calendarStep)
        }

        // 反转列表：让下标 i=0 映射最早的时间，i=totalCount-1 映射最新时间
        tempTimeList.reverse()
        tempLabelList.reverse()
        xLabelList.addAll(tempLabelList)

        // 3. 【高性能硬核匹配】：判定记录落在哪个刻度周期内（消灭 O(N^2) 嵌套循环）
        val entityCal = Calendar.getInstance()
        data.forEach { entity ->
            entityCal.timeInMillis = entity.customTime
            var targetIndex: Int

            // 高性能、100% 精准的区间反查法：计算它与最早时间刻度（tempTimeList[0]）的自然跨度差
            when (currentTimeMode) {
                TimeMode.DAY -> {
                    entityCal.set(Calendar.HOUR_OF_DAY, 0); entityCal.set(
                        Calendar.MINUTE,
                        0
                    ); entityCal.set(Calendar.SECOND, 0); entityCal.set(Calendar.MILLISECOND, 0)
                    val msDiff = entityCal.timeInMillis - tempTimeList[0]
                    targetIndex = (msDiff / (1000L * 60 * 60 * 24)).toInt()
                }

                TimeMode.WEEK -> {
                    entityCal.set(Calendar.DAY_OF_WEEK, entityCal.firstDayOfWeek)
                    entityCal.set(Calendar.HOUR_OF_DAY, 0); entityCal.set(
                        Calendar.MINUTE,
                        0
                    ); entityCal.set(Calendar.SECOND, 0); entityCal.set(Calendar.MILLISECOND, 0)
                    val msDiff = entityCal.timeInMillis - tempTimeList[0]
                    targetIndex = (msDiff / (1000L * 60 * 60 * 24 * 7)).toInt()
                }

                TimeMode.MONTH -> {
                    val startCal = Calendar.getInstance().apply { timeInMillis = tempTimeList[0] }
                    val yearDiff = entityCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
                    val monthDiff = entityCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH)
                    targetIndex = yearDiff * 12 + monthDiff
                }
            }

            // 同一周期内如果存在多条数据，直接用最新值覆盖（一天/一周/一月只留一个终点值）
            if (targetIndex in 0 until totalCount) {
                val value: Float = if (lineChart == bmiChart) entity.bmiValue
                else {
                    if (entity.weightUnit) entity.weight
                    else entity.weight * 0.45359236f
                }
                valueIndexMap[targetIndex] = value
            }
        }

        // 4. 按下标顺序生成 Entry，x=i，实现点位和标签的绝对死锁
        for (i in 0 until totalCount) {
            val yValue = valueIndexMap[i]
            if (yValue != null) {
                entries.add(Entry(i.toFloat(), yValue))
            }
        }

        // 🌟 5. 【核心修复 2】：锁死图表的绝对物理渲染范围
        // 必须强行告诉图表：你的 X 轴物理空间就是从 0 到 totalCount-1。
        // 哪怕 entries 里面只有最后 3 个点，图表也必须严格画在右侧，绝不发生拉伸错位！
        lineChart.xAxis.apply {
            axisMinimum = 0f
            axisMaximum = (totalCount - 1).toFloat()
        }

        mBaseTimeZero = baseCal.timeInMillis

        // 6. 渲染对应图表
        if (lineChart == bmiChart) {
            renderBmiChart(entries)
        } else {
            renderWeightChart(entries)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 释放图表资源，避免内存泄漏
        bmiChart.clear()
        weightChart.clear()
        weightChart.data = null
        bmiChart.data = null
    }
}