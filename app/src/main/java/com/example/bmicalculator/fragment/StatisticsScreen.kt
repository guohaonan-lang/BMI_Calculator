package com.example.bmicalculator.fragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.ui.theme.Background
import com.example.bmicalculator.ui.theme.Black
import com.example.bmicalculator.ui.theme.Blue
import com.example.bmicalculator.ui.theme.Gray
import com.example.bmicalculator.ui.theme.White
import com.example.bmicalculator.util.BmiMarkerView
import com.example.bmicalculator.util.SmartXAxisRenderer
import com.example.bmicalculator.viewmodel.StatisticsFragmentViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter


@Composable
fun StatisticsScreen(viewModel: StatisticsFragmentViewModel) {

    val uiState = viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .background(color = Background)
            .padding(start = 15.dp, end = 15.dp, top = 15.dp)
    ) {
        Text(
            text = stringResource(R.string.statistics),
            fontFamily = FontFamily(Font(R.font.font_extrabold)),
            fontSize = 24.sp,
            color = Black
        )
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            TimeSwitch(
                uiState.value.timeMode,
                { viewModel.processIntent(StatisticsFragmentViewModel.StatisticsIntent.SwitchDay) },
                { viewModel.processIntent(StatisticsFragmentViewModel.StatisticsIntent.SwitchWeek) },
                { viewModel.processIntent(StatisticsFragmentViewModel.StatisticsIntent.SwitchMonth) }
            )
            BmiChartTitle({ viewModel.processIntent(StatisticsFragmentViewModel.StatisticsIntent.InputPage) })
            LineChart(uiState.value.chartData, uiState.value.timeMode)
            WeightChartTitle({ viewModel.processIntent(StatisticsFragmentViewModel.StatisticsIntent.InputPage) })
            LineChart(uiState.value.chartData, uiState.value.timeMode, false)
        }

    }

}

@Composable
fun TimeSwitch(
    tm: StatisticsFragmentViewModel.TimeMode,
    dayClick: () -> Unit,
    weekClick: () -> Unit,
    monthClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp)
            .height(36.dp)
            .background(color = Gray, shape = RoundedCornerShape(36.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.day),
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .background(
                    color = if (tm == StatisticsFragmentViewModel.TimeMode.DAY) White else Gray,
                    shape = RoundedCornerShape(36.dp)
                )
                .wrapContentHeight()
                .alpha(if (tm == StatisticsFragmentViewModel.TimeMode.DAY) 1f else 0.3f)
                .clickable(onClick = dayClick),
            fontFamily = FontFamily(Font(R.font.font_extrabold)),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.week),
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .background(
                    color = if (tm == StatisticsFragmentViewModel.TimeMode.WEEK) White else Gray,
                    shape = RoundedCornerShape(36.dp)
                )
                .wrapContentHeight()
                .alpha(if (tm == StatisticsFragmentViewModel.TimeMode.WEEK) 1f else 0.3f)
                .clickable(onClick = weekClick),
            fontFamily = FontFamily(Font(R.font.font_extrabold)),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.month),
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .background(
                    color = if (tm == StatisticsFragmentViewModel.TimeMode.MONTH) White else Gray,
                    shape = RoundedCornerShape(36.dp)
                )
                .wrapContentHeight()
                .alpha(if (tm == StatisticsFragmentViewModel.TimeMode.MONTH) 1f else 0.3f)
                .clickable(onClick = monthClick),
            fontFamily = FontFamily(Font(R.font.font_extrabold)),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BmiChartTitle(updateClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "BMI",
            fontFamily = FontFamily(Font(R.font.font_extrabold)),
            fontSize = 18.sp
        )
        Text(
            stringResource(R.string.update),
            modifier = Modifier
                .clickable(onClick = updateClick),
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 16.sp,
            color = Blue,
        )
    }
}

@Composable
fun WeightChartTitle(updateClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            stringResource(R.string.statistics_weight),
            fontFamily = FontFamily(Font(R.font.font_extrabold)),
            fontSize = 18.sp
        )
        Text(
            stringResource(R.string.update),
            modifier = Modifier
                .clickable(onClick = updateClick),
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 16.sp,
            color = Blue,
        )
    }
}

@Composable
fun LineChart(
    chartData: StatisticsFragmentViewModel.ChartProcessResult?,
    timeMode: StatisticsFragmentViewModel.TimeMode,
    isBmiChart: Boolean = true
) {
    // 缓存上一次渲染的数据，避免无意义刷新（对应 Fragment lastRenderedData）
    val lastData = remember { mutableStateOf<List<Entry>?>(null) }
    // 缓存X轴标签、基准时间
    val xLabelList = remember { mutableStateListOf<String>() }
    var baseTimeZero by remember { mutableStateOf(0L) }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        // factory：仅初始化执行1次，创建LineChart + 全局样式
        factory = { context ->
            LineChart(context).apply {
                if (isBmiChart) background =
                    ContextCompat.getDrawable(context, R.drawable.chart_bmi_bg)
                else background = ContextCompat.getDrawable(context, R.drawable.chart_weight_bg)
                // 等价 initChartStyle()
                val chartFont = ResourcesCompat.getFont(context, R.font.font_extrabold)
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setDragEnabled(true)
                setScaleEnabled(false)
                isDragXEnabled = true
                isDragYEnabled = false

                extraLeftOffset = 15f
                extraBottomOffset = 15f
                extraTopOffset = 25f
                extraRightOffset = 15f

                // X轴配置
                val xAxis: XAxis = this.xAxis
                xAxis.position = XAxis.XAxisPosition.BOTH_SIDED
                xAxis.gridColor = 0xFFDDDDDD.toInt()
                xAxis.axisLineColor = Color.TRANSPARENT
                xAxis.labelCount = 8
                xAxis.granularity = 1f
                xAxis.isGranularityEnabled = true
                xAxis.typeface = chartFont
                xAxis.textSize = 12f
                xAxis.textColor = ContextCompat.getColor(context, R.color.white)

                // Y轴左轴
                val leftY: YAxis = axisLeft
                leftY.gridColor = 0xFFDDDDDD.toInt()
                leftY.axisLineColor = Color.TRANSPARENT
                leftY.setLabelCount(6, true)
                leftY.setDrawGridLines(false)
                leftY.typeface = chartFont
                leftY.textSize = 12f
                leftY.textColor = ContextCompat.getColor(context, R.color.white)

                axisRight.isEnabled = false
            }
        },
        // update：Composable重组就执行，在这里更新数据、刷新图表
        update = { lineChart ->
            val data = chartData
            // 空数据清空图表
            if (data == null) {
                lineChart.clear()
                lineChart.invalidate()
                lastData.value = null
                return@AndroidView
            }

            // 数据完全没变化，直接跳过渲染，避免重复绘制
            if (lastData.value == data.bmiEntries) return@AndroidView

            // 更新缓存标记
            lastData.value = data.bmiEntries
            xLabelList.clear()
            xLabelList.addAll(data.xLabels)
            baseTimeZero = data.baseTimeZero

            // 渲染曲线（对应 renderBmiChart）
            renderChart(
                chart = lineChart,
                entries = if (isBmiChart) data.bmiEntries else data.weightEntries,
                timeMode = timeMode,
                xLabels = xLabelList,
                baseTimeZero = baseTimeZero,
                totalCount = data.totalCount
            )
        }
    )
}

/** 封装渲染逻辑，等同原来 renderBmiChart */
private fun renderChart(
    chart: LineChart,
    entries: List<Entry>,
    timeMode: StatisticsFragmentViewModel.TimeMode,
    xLabels: List<String>,
    baseTimeZero: Long,
    totalCount: Int
) {
    if (entries.isEmpty()) {
        chart.clear()
        chart.invalidate()
        return
    }

    // 绑定自定义 MarkerView（对应 BmiMarkerView）
    val marker = BmiMarkerView(chart.context)
    marker.chartView = chart
    chart.marker = marker

    // 构建曲线数据集
    val dataSet = LineDataSet(entries, "BMI曲线").apply {
        setDrawHighlightIndicators(true)
        setDrawVerticalHighlightIndicator(false)
        setDrawHorizontalHighlightIndicator(false)
        mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        lineWidth = 1f
        color = Color.WHITE
        circleRadius = 3f
        setCircleColor(Color.WHITE)
        setDrawFilled(true)

        // 渐变填充
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(0x80FFFFFF.toInt(), 0x08FFFFFF)
        )
        fillDrawable = gradient
        setDrawValues(false)
    }

    chart.data = LineData(dataSet)

    // X轴标签格式化
    chart.xAxis.valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val idx = value.toInt()
            return if (idx in xLabels.indices) xLabels[idx] else ""
        }
    }

    // 自定义X轴渲染器 SmartXAxisRenderer
    chart.setXAxisRenderer(
        SmartXAxisRenderer(
            chart.viewPortHandler,
            chart.xAxis,
            chart.getTransformer(YAxis.AxisDependency.LEFT),
            { baseTimeZero },
            { timeMode },
            chart.context
        )
    )

    val maxShowCount = 8f
    chart.setVisibleXRangeMaximum(maxShowCount)
    chart.xAxis.apply {
        isGranularityEnabled = true
        granularity = 1f
        labelCount = maxShowCount.toInt()
        axisMinimum = 0f
        axisMaximum = totalCount.toFloat()
    }

    // 刷新图表
    chart.notifyDataSetChanged()
    chart.invalidate()

    // 滑动到最新数据
    chart.post {
        chart.viewPortHandler.matrixTouch.reset()
        chart.setVisibleXRangeMaximum(maxShowCount)
        chart.setVisibleXRangeMinimum(maxShowCount)
        chart.moveViewToX(chart.xAxis.axisMaximum - maxShowCount + 1f)
    }
}