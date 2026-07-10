package com.example.bmicalculator.util

import android.content.Context
import android.graphics.Canvas
import com.example.bmicalculator.R
import com.example.bmicalculator.fragment.StatisticsFragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler
import java.util.Calendar
import kotlin.math.roundToInt

class SmartXAxisRenderer(
    viewPortHandler: ViewPortHandler,
    xAxis: XAxis,
    trans: Transformer,
    private val getBaseTimeZero: () -> Long,
    private val getCurrentMode: () -> StatisticsFragment.TimeMode,
    private val context: Context
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
        // 保持原样判断：如果当前绘制的是顶部的标签
        if (y < mViewPortHandler.contentTop() + mXAxis.textSize) {
            val baseTs = getBaseTimeZero()
            val mode = getCurrentMode()
            if (baseTs == 0L) return

            // 精准提取当前刻度的物理索引（0 到 totalCount-1）
            val positions = floatArrayOf(x, 0f)
            mTrans.pixelsToValue(positions)
            val index = positions[0].roundToInt()

            // 新倒推架构各个模式的总格数
            val totalCount = when (mode) {
                StatisticsFragment.TimeMode.DAY -> 90
                StatisticsFragment.TimeMode.WEEK -> 54
                StatisticsFragment.TimeMode.MONTH -> 60
            }
            if (index !in 0 until totalCount) return

            // 计算当前格子需要从最新的那天（末尾）倒退多少天/周/月
            val offsetFromLatest = (totalCount - 1) - index
            mCalendar.timeInMillis = baseTs

            when (mode) {
                StatisticsFragment.TimeMode.DAY -> {
                    mCalendar.add(Calendar.DAY_OF_YEAR, -offsetFromLatest)
                    val month = mCalendar.get(Calendar.MONTH) + 1
                    val day = mCalendar.get(Calendar.DAY_OF_MONTH)
                    if (day == 1 || index == 0) {
                        val s = context.getString(R.string.month)
                        super.drawLabel(c, "${month}$s", x, y, anchor, angleDegrees)
                    }
                }
                StatisticsFragment.TimeMode.WEEK -> {
                    mCalendar.add(Calendar.WEEK_OF_YEAR, -offsetFromLatest)
                    val day = mCalendar.get(Calendar.DAY_OF_MONTH)
                    val month = mCalendar.get(Calendar.MONTH) + 1
                    if ((day in 1..7) || index == 0) {
                        val s = context.getString(R.string.month)
                        super.drawLabel(c, "${month}$s", x, y, anchor, angleDegrees)
                    }
                }
                StatisticsFragment.TimeMode.MONTH -> {
                    mCalendar.add(Calendar.MONTH, -offsetFromLatest)
                    val currYear = mCalendar.get(Calendar.YEAR)
                    // 首刻度直接绘制，不用走对比逻辑
                    if (index == 0) {
                        super.drawLabel(c, "$currYear", x, y, anchor, angleDegrees)
                        return
                    }
                    // 对比上一月年份是否跨年
                    val prevCal = Calendar.getInstance().apply { timeInMillis = baseTs }
                    val prevOff = (totalCount - 1) - (index - 1)
                    prevCal.add(Calendar.MONTH, -prevOff)
                    val prevYear = prevCal.get(Calendar.YEAR)
                    if (currYear != prevYear) {
                        super.drawLabel(c, "$currYear", x, y, anchor, angleDegrees)
                    }
                }
            }
        } else {
            // 如果是底部的标签，保持原本数据映射（日/周/月）原样绘制
            super.drawLabel(c, formattedLabel, x, y, anchor, angleDegrees)
        }
    }
}
