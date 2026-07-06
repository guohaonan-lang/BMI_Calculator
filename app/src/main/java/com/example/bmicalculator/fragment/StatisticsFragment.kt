package com.example.bmicalculator.fragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.bmicalculator.util.CustomXAxisRenderer
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
    private val chartFont by lazy {
        ResourcesCompat.getFont(requireContext(), R.font.font_extrabold)
    }

    // 图表X轴日期标签集合
    private val xLabelList = mutableListOf<String>()

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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initChartStyle(bmiChart)
        initChartStyle(weightChart)
        setChartData()

    }

    // 初始化表格样式
    private fun initChartStyle(chart: LineChart) {
        chart.apply {
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
            extraTopOffset = 20f
            extraRightOffset = 10f

        }
        val xAxis: XAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTH_SIDED
        xAxis.gridColor = 0xFFDDDDDD.toInt()
        xAxis.axisLineColor = Color.TRANSPARENT
        xAxis.labelCount = 8
        // 字体
        xAxis.typeface = chartFont
        xAxis.textSize = 12f
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.white)

        // 左右留白，避免首尾贴边拖不动
        xAxis.spaceMin = 0.3f
        xAxis.spaceMax = 0.3f

        val leftY: YAxis = chart.axisLeft
        leftY.gridColor = 0xFFDDDDDD.toInt()
        leftY.axisLineColor = Color.TRANSPARENT
        leftY.setLabelCount(6, true)
        leftY.setDrawGridLines(false)

        leftY.typeface = chartFont
        leftY.textSize = 12f
        leftY.textColor = ContextCompat.getColor(requireContext(), R.color.white)

        chart.axisRight.isEnabled = false
    }

    //  BMI表格
    private fun renderBmiChart(entries: MutableList<Entry>) {

        if (entries.isEmpty()) {
            bmiChart.clear()
            bmiChart.invalidate()
            return
        }

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
                    0x80FFFFFF.toInt(),    // 顶部纯白
                    0x08FFFFFF.toInt()
                )
            )
            fillDrawable = gradient
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)
        bmiChart.data = lineData

        // ===== 核心重构 1：安全平稳的底部日期转换（绝对不越界崩溃） =====
        bmiChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = Math.round(value)
                return if (index in xLabelList.indices) {
                    xLabelList[index] // 底部永远只返回纯日期，如 "30", "1", "2"
                } else {
                    ""
                }
            }
        }

        // ===== 核心重构 2：在数据填充后，向图表注入自定义渲染器 =====
        // 必须在 lineChart.data = ... 之后设置，此时图表的 Transformer 已经完全初始化完毕
        bmiChart.setXAxisRenderer(
            CustomXAxisRenderer(
                bmiChart.viewPortHandler,
                bmiChart.xAxis,
                bmiChart.getTransformer(YAxis.AxisDependency.LEFT),
                mBaseTimeZero
            )
        )


        //必须先填充数据，再设置显示范围
        val maxShowCount = 8f
        bmiChart.setVisibleXRangeMaximum(8f)
        bmiChart.setVisibleXRangeMinimum(8f)
        // 定位到最新数据（右侧末尾）
        if (entries.isNotEmpty() && entries.last().x > maxShowCount) {
            bmiChart.moveViewToX(entries.last().x - maxShowCount + 0.5f)
        }

        bmiChart.data?.dataSets?.forEach { dataSet ->
            dataSet.valueTypeface = chartFont
        }
        bmiChart.invalidate()
    }

    // . 体重表格
    private fun renderWeightChart(entries: MutableList<Entry>) {

        if (entries.isEmpty()) {
            weightChart.clear()
            weightChart.invalidate()
            return
        }

        val dataSet = LineDataSet(entries, "weight曲线").apply {
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
                    0x80FFFFFF.toInt(),    // 顶部纯白
                    0x08FFFFFF.toInt()
                )
            )
            fillDrawable = gradient
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)
        weightChart.data = lineData

        // ===== 核心重构 1：安全平稳的底部日期转换（绝对不越界崩溃） =====
        weightChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = Math.round(value)
                return if (index in xLabelList.indices) {
                    xLabelList[index] // 底部永远只返回纯日期，如 "30", "1", "2"
                } else {
                    ""
                }
            }
        }

        // ===== 核心重构 2：在数据填充后，向图表注入自定义渲染器 =====
        // 必须在 lineChart.data = ... 之后设置，此时图表的 Transformer 已经完全初始化完毕
        weightChart.setXAxisRenderer(
            CustomXAxisRenderer(
                weightChart.viewPortHandler,
                weightChart.xAxis,
                weightChart.getTransformer(YAxis.AxisDependency.LEFT),
                mBaseTimeZero
            )
        )


        //必须先填充数据，再设置显示范围
        val maxShowCount = 8f
        weightChart.setVisibleXRangeMaximum(8f)
        weightChart.setVisibleXRangeMinimum(8f)
        // 定位到最新数据（右侧末尾）
        if (entries.isNotEmpty() && entries.last().x > maxShowCount) {
            weightChart.moveViewToX(entries.last().x - maxShowCount + 0.5f)
        }

        weightChart.data?.dataSets?.forEach { dataSet ->
            dataSet.valueTypeface = chartFont
        }
        weightChart.invalidate()
    }

    private fun setChartData() {
        // 监听数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chartBmiList.collect { data ->
                    if (data.isEmpty()) {
                        bmiChart.clear()
                        weightChart.clear()
                        return@collect
                    }

                    xLabelList.clear()
                    val bmiEntries = mutableListOf<Entry>()
                    val weightEntries = mutableListOf<Entry>()

                    // 1. 获取第一条数据的绝对“零点”时间戳
                    val baseCalendar = Calendar.getInstance().apply {
                        timeInMillis = data.first().customTime
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    mBaseTimeZero = baseCalendar.timeInMillis

                    val dayCheckMap = mutableMapOf<Int, Int>()
                    val targetCalendar = Calendar.getInstance()

                    data.forEach { entity ->
                        val dayOffset =
                            ((entity.customTime - mBaseTimeZero) / (1000 * 60 * 60 * 24)).toInt()

                        if (dayCheckMap.containsKey(dayOffset)) {
                            val existingIndex = dayCheckMap[dayOffset]!!
                            bmiEntries[existingIndex] = Entry(dayOffset.toFloat(), entity.bmiValue)
                            weightEntries[existingIndex] = Entry(dayOffset.toFloat(), entity.weight)

                        } else {
                            bmiEntries.add(Entry(dayOffset.toFloat(), entity.bmiValue))
                            weightEntries.add(Entry(dayOffset.toFloat(), entity.weight))
                            dayCheckMap[dayOffset] = bmiEntries.size - 1
                        }
                    }

                    // 2. 动态构建底部纯日期标签，保证 xLabelList.size 严格等同于最大天数跨度，消灭越界
                    val totalDays =
                        ((data.last().customTime - mBaseTimeZero) / (1000 * 60 * 60 * 24)).toInt()
                    for (i in 0..totalDays) {
                        targetCalendar.timeInMillis =
                            mBaseTimeZero + (i.toLong() * 1000 * 60 * 60 * 24)
                        xLabelList.add(targetCalendar.get(Calendar.DAY_OF_MONTH).toString())
                    }

                    renderBmiChart(bmiEntries)
                    renderWeightChart(weightEntries)
                }
            }
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