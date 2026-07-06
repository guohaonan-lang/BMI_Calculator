package com.example.bmicalculator.util

import android.graphics.Canvas
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler
import java.util.Calendar
import kotlin.math.roundToInt

class CustomXAxisRenderer(
    viewPortHandler: ViewPortHandler,
    xAxis: XAxis,
    trans: Transformer,
    private val baseTimeZero: Long
) : XAxisRenderer(viewPortHandler, xAxis, trans) {

    private val mCalendar = Calendar.getInstance()

    override fun drawLabel(
        c: Canvas?,
        formattedLabel: String?,
        x: Float,
        y: Float,
        anchor: MPPointF?,
        angleDegrees: Float
    ) {
        // 如果当前绘制的是顶部的标签（y 坐标小于图表中心，说明在上方）
        if (y < mViewPortHandler.contentTop() + mXAxis.textSize) {

            // 创建临时坐标数组并进行像素到物理值的精确转换
            val positions = floatArrayOf(x, 0f)
            mTrans.pixelsToValue(positions)

            val dayOffset = positions[0].roundToInt()
            mCalendar.timeInMillis = baseTimeZero + (dayOffset.toLong() * 1000 * 60 * 60 * 24)
            val month = mCalendar.get(Calendar.MONTH) + 1
            val day = mCalendar.get(Calendar.DAY_OF_MONTH)

            // 只有在这个点是 1 号，或者是整张图表的绝对起点时，顶部绘制 "X月"
            if (day == 1 || dayOffset == 0) {
                val monthLabel = "${month}月"
                super.drawLabel(c, monthLabel, x, y, anchor, angleDegrees)
            }
        } else {
            // 如果是底部的标签，保持原样绘制
            super.drawLabel(c, formattedLabel, x, y, anchor, angleDegrees)
        }
    }
}