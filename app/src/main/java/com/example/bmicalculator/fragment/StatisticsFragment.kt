package com.example.bmicalculator.fragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.viewmodel.BmiViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class StatisticsFragment : Fragment() {
    private lateinit var lineChart: LineChart

    // 图表X轴日期标签集合
    private val xLabelList = mutableListOf<String>()

    // 原始BMI数据列表
    private var dataList: List<BmiEntity> = emptyList()


    private val viewModel: BmiViewModel by viewModels {
        val db = BmiDatabase.getDatabase(requireContext())
        BmiViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)
        lineChart = view.findViewById(R.id.chart_bmi)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initChartStyle()
        setChartData()


    }

    private fun initChartStyle() {
        lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)

            setScaleEnabled(false)
            isDragXEnabled = true
            isDragYEnabled = false

        }
        val xAxis: XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.gridColor = 0xFFDDDDDD.toInt()
        xAxis.axisLineColor = Color.TRANSPARENT
        xAxis.labelCount = 0

        // 左右留白，避免首尾贴边拖不动
        xAxis.spaceMin = 0.3f
        xAxis.spaceMax = 0.3f

        val leftY: YAxis = lineChart.axisLeft
        leftY.gridColor = 0xFFDDDDDD.toInt()
        leftY.axisLineColor = Color.TRANSPARENT
        leftY.setLabelCount(6, true)

        lineChart.axisRight.isEnabled = false
    }

    private fun renderLineChart(entries: MutableList<Entry>) {

        if (entries.isEmpty()) {
            lineChart.clear()
            lineChart.invalidate()
            return
        }

        val dataSet = LineDataSet(entries, "BMI曲线").apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            lineWidth = 2f
            color = 0xFF2185D0.toInt()

            circleRadius = 5f
            setCircleColor(0xFF2185D0.toInt())
            circleHoleRadius = 2.5f
            circleHoleColor = Color.WHITE

            setDrawFilled(true)
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0x802185D0.toInt(), 0x102185D0)
            )
            fillDrawable = gradient
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(xLabelList)

        //必须先填充数据，再设置显示范围
        val maxShowCount = 6f
        lineChart.setVisibleXRangeMaximum(6f)
        lineChart.setVisibleXRangeMinimum(6f)
        // 定位到最新数据（右侧末尾）
        if (entries.size > maxShowCount) {
            lineChart.moveViewToX(entries.size - maxShowCount)
        }

        lineChart.invalidate()
    }

    private fun setChartData() {
        // 启动协程监听数据库数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chartBmiList.collect { data ->
                    // 1. 拿到最新数据库数据
                    dataList = data
                    // 清空旧数据，防止重复叠加
                    xLabelList.clear()
                    val entries = mutableListOf<Entry>()

                    dataList.forEachIndexed { index, entity ->
                        entries.add(Entry(index.toFloat(), entity.bmiValue))
                        xLabelList.add(entity.timeText)
                    }

                    // 3. 渲染曲线
                    renderLineChart(entries)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 释放图表资源，避免内存泄漏
        lineChart.clear()
        lineChart.data = null
    }
}