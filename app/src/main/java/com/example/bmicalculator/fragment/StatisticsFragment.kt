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
import com.example.bmicalculator.databinding.FragmentStatisticsBinding
import com.example.bmicalculator.ui.MainActivity
import com.example.bmicalculator.util.BmiMarkerView
import com.example.bmicalculator.util.SmartXAxisRenderer
import com.example.bmicalculator.util.WeightMarkerView
import com.example.bmicalculator.viewmodel.StatisticsFragmentViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.launch

class StatisticsFragment : Fragment() {
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = checkNotNull(_binding)

    private var lastRenderedData: List<Entry>? = null

    // 图表X轴日期标签集合
    private val xLabelList = mutableListOf<String>()
    private var mBaseTimeZero: Long = 0L // 类级别变量
    private val viewModel: StatisticsFragmentViewModel by viewModels {
        val db = BmiDatabase.getDatabase(requireContext())
        StatisticsFragmentViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initChartStyle(binding.chartBmi)
        initChartStyle(binding.chartWeight)
        setChartDataFlow()

        initSwitchTime()
        initUpdate()
    }

    private fun initUpdate() {
        binding.chartUpdate1.setOnClickListener {
            val mainActivity = requireActivity() as MainActivity
            mainActivity.binding.mainViewpage2.currentItem = 0
        }
        binding.chartUpdate2.setOnClickListener {
            val mainActivity = requireActivity() as MainActivity
            mainActivity.binding.mainViewpage2.currentItem = 0
        }
    }

    // 切换时间周期
    private fun initSwitchTime() {
        val density = resources.displayMetrics.density
        val movePx = -(115 * density)
        binding.switchTimeDay.setOnClickListener {
            binding.selectorThumbTime.animate()
                .translationX(0f)
                .withLayer()
                .start()
            binding.selectorThumbTime.text = getString(R.string.day)
            viewModel.setTimeMode(StatisticsFragmentViewModel.TimeMode.DAY)
        }
        binding.switchTimeWeek.setOnClickListener {
            binding.selectorThumbTime.animate()
                .translationX(-movePx)
                .withLayer()
                .start()
            binding.selectorThumbTime.text = getString(R.string.week)
            viewModel.setTimeMode(StatisticsFragmentViewModel.TimeMode.WEEK)
        }
        binding.switchTimeMonth.setOnClickListener {
            binding.selectorThumbTime.animate()
                .translationX(-movePx * 2)
                .withLayer()
                .start()
            binding.selectorThumbTime.text = getString(R.string.month)
            viewModel.setTimeMode(StatisticsFragmentViewModel.TimeMode.MONTH)
        }
    }

    // 初始化表格样式
    private fun initChartStyle(lineChart: LineChart) {

        val chartFont = ResourcesCompat.getFont(requireContext(), R.font.font_extrabold)
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

    //  表格
    private fun renderBmiChart(bmiChart: LineChart,entries: MutableList<Entry>,currentTimeMode: StatisticsFragmentViewModel.TimeMode) {
        if (entries.isEmpty()) {
            bmiChart.clear()
            bmiChart.invalidate()
            return
        }

        if(bmiChart == binding.chartBmi){
            val marker = BmiMarkerView(requireContext())
            marker.chartView = bmiChart // 边缘自动防裁切
            bmiChart.marker = marker
        }else{
            val marker = WeightMarkerView(requireContext())
            marker.chartView = bmiChart // 必须设置，边缘自动防裁切
            bmiChart.marker = marker
        }

        bmiChart.isHighlightPerDragEnabled = true
        bmiChart.isHighlightPerTapEnabled = true
        val dataSet = LineDataSet(entries, "BMI曲线").apply {
            setDrawHighlightIndicators(true)
            // 2. 关闭原生十字线（需求：不要辅助线）
            setDrawVerticalHighlightIndicator(false)
            setDrawHorizontalHighlightIndicator(false)
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            lineWidth = 1f
            color = Color.WHITE

            circleRadius = 3f
            setCircleColor(Color.WHITE)
//            circleHoleRadius = 1f
//            circleHoleColor = Color.WHITE

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
        bmiChart.setOnClickListener {  }

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
                { currentTimeMode },
                requireContext()
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
            dataSet.valueTypeface = ResourcesCompat.getFont(requireContext(), R.font.font_extrabold)
        }

        // 通知图表刷新其内部基础状态并执行统一单次重绘
        bmiChart.notifyDataSetChanged()
        bmiChart.invalidate()

        // =====  post 队列 清除放大矩阵、重新设置边界与右移 =====
        bmiChart.post {
            bmiChart.viewPortHandler.matrixTouch.reset()

            bmiChart.setVisibleXRangeMaximum(maxShowCount)
            bmiChart.setVisibleXRangeMinimum(maxShowCount)

            // 滚动到最新一页
            bmiChart.moveViewToX(bmiChart.xAxis.axisMaximum - maxShowCount + 1f)
        }
    }

    // 监听数据
    private fun setChartDataFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 监听合并后的流
                viewModel.chartUiState.collect { result ->
                    if (result == null) {
                        listOf(binding.chartBmi, binding.chartWeight).forEach { chart ->
                            chart.clear()
                            chart.invalidate()
                        }
                        lastRenderedData = null
                        return@collect
                    }

                    if (lastRenderedData == result.bmiEntries) {
                        return@collect
                    }
                    lastRenderedData = result.bmiEntries

                    // 页面缓存 UI 所需数据
                    xLabelList.clear()
                    xLabelList.addAll(result.xLabels)
                    mBaseTimeZero = result.baseTimeZero

                    // 传入当前的时间模式给渲染函数（假设 result 内部也包了当前 mode，或者直接读 viewModel.timeMode.value）
                    val currentMode = viewModel.timeMode.value

                    // 分别渲染两张图表
                    renderBmiChart(binding.chartBmi, result.bmiEntries as MutableList<Entry>, currentMode)
                    renderBmiChart(binding.chartWeight, result.weightEntries as MutableList<Entry>, currentMode)

                    // X 轴区间配置
                    listOf(binding.chartBmi, binding.chartWeight).forEach { chart ->
                        chart.xAxis.apply {
                            axisMinimum = 0f
                            axisMaximum = result.totalCount.toFloat()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 释放图表资源，避免内存泄漏
        binding.chartBmi.clear()
        binding.chartWeight.clear()
        binding.chartBmi.data = null
        binding.chartWeight.data = null
    }
}