package com.example.bmicalculator.util
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.example.bmicalculator.R

class BmiColorWheelView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // 外部传入数据
    var age: Int = 18
        set(value) {
            field = value
            updateRangeAndColor()
            invalidate()
        }
    var gender: Int = 1 // 0女 1男
        set(value) {
            field = value
            updateRangeAndColor()
            invalidate()
        }
    var currentBmi = 20f
        set(value) {
            field = value.coerceIn(minBmi, maxBmi)
            invalidate()
        }

    // 动态变量（由age+gender自动更新）
    private var minBmi = 15.0f
    private var maxBmi = 41.0f
    private var totalRange = maxBmi - minBmi
    private var bmiRanges = floatArrayOf(15f, 16f, 18.5f, 25f, 30f, 35f, 40f, 41f)
    private var colors = intArrayOf()

    // 画笔不变
    private val paintArc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#000000")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(ResourcesCompat.getFont(context, R.font.font_extrabold), Typeface.NORMAL)
    }
    private val paintPointer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
        style = Paint.Style.FILL
    }
    private val rectF = RectF()

    init {
        // 初始化一次区间与颜色
        updateRangeAndColor()
    }

    /** 核心：根据年龄性别更新表盘刻度区间、对应色值 */
    private fun updateRangeAndColor() {
        // 分支1：成年人 >=18，8档完整区间
        if (age >= 18) {
            minBmi = 15f
            maxBmi = 41f
            bmiRanges = floatArrayOf(15f, 16f, 17f, 18.5f, 25f, 30f, 35f, 40f, 41f)
            colors = intArrayOf(
                Color.parseColor("#4343B8"),
                Color.parseColor("#1258E1"),
                Color.parseColor("#0099F2"),
                Color.parseColor("#54A529"),
                Color.parseColor("#FECD2E"),
                Color.parseColor("#FFA100"),
                Color.parseColor("#FF7137"),
                Color.parseColor("#D3333B")
            )
        } else {
            // 分支2：未成年2~20，仅4档 Under/Normal/Over/ObeseI
            // 读取对应性别年龄阈值
            val teenRange = if (gender == 0) {
                BmiUtil.femaleTeenTable.first { it.age == age }
            } else {
                BmiUtil.maleTeenTable.first { it.age == age }
            }
            minBmi = 13f
            maxBmi = 33f
            // 四段分界：min | underMax | normalMax | overMax | max
            bmiRanges = floatArrayOf(
                minBmi,
                teenRange.underweightMax,
                teenRange.normalMax,
                teenRange.overweightMax,
                maxBmi
            )
            // 未成年只用前4个色系
            colors = intArrayOf(
                Color.parseColor("#0099F2"),
                Color.parseColor("#54A529"),
                Color.parseColor("#FECD2E"),
                Color.parseColor("#FFA100")
            )
        }
        totalRange = maxBmi - minBmi
        // 限制指针不越界
        currentBmi = currentBmi.coerceIn(minBmi, maxBmi)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val defaultWidth = dpToPx(260f).toInt()
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(defaultWidth, widthSize)
            else -> defaultWidth
        }
        val textPadding = dpToPx(28f)
        val height = (width / 2) + textPadding.toInt()
        setMeasuredDimension(width, height + paddingTop + paddingBottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rawW = width.toFloat() - paddingLeft - paddingRight
        if (rawW <= 0) return
        val w = rawW * 0.8f
        val extraOffset = (rawW - w) / 2f
        val centerX = paddingLeft + w / 2f + extraOffset
        val strokeW = dpToPx(80f)
        paintArc.strokeWidth = strokeW
        val textMargin = dpToPx(24f)
        val radius = (w - strokeW) / 2f
        val centerY = paddingTop + textMargin + radius + strokeW / 2f
        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // 绘制分段彩色圆弧（动态区间、动态颜色）
        var currentStartAngle = 180.0f
        val angleList = ArrayList<Float>()
        angleList.add(currentStartAngle)
        for (i in 0 until bmiRanges.size - 1) {
            val startVal = bmiRanges[i]
            val endVal = bmiRanges[i + 1]
            val sweepAngle = ((endVal - startVal) / totalRange) * 180.0f
            paintArc.color = colors[i]
            canvas.drawArc(rectF, currentStartAngle, sweepAngle, false, paintArc)
            currentStartAngle += sweepAngle
            angleList.add(currentStartAngle)
        }

        // 绘制刻度数字
        paintText.textSize = dpToPx(11f)
        val textDistance = radius + (strokeW / 2f) + (w * 0.04f)
        for (i in bmiRanges.indices) {
            val angle = angleList[i]
            val bmiVal = bmiRanges[i]
            val text = if (bmiVal % 1f == 0f) bmiVal.toInt().toString() else bmiVal.toString()
            canvas.save()
            canvas.translate(centerX, centerY)
            canvas.rotate(angle + 90f)
            val fontMetrics = paintText.fontMetrics
            val baseline = -textDistance - fontMetrics.ascent
            canvas.drawText(text, 0f, baseline, paintText)
            canvas.restore()
        }

        // 绘制指针
        canvas.save()
        canvas.translate(centerX, centerY)
        val pointerAngle = ((currentBmi - minBmi) / totalRange) * 180f
        canvas.rotate(180f + pointerAngle)
        val pathPointer = Path()
        val pointerLength = radius - (strokeW / 2f) + dpToPx(8f)
        val pointerWidth = dpToPx(20f)
        val cornerRadius = dpToPx(4f)
        pathPointer.moveTo(pointerLength, 0f)
        pathPointer.lineTo(0f, -pointerWidth / 2f)
        pathPointer.lineTo(0f, pointerWidth / 2f)
        pathPointer.close()
        paintPointer.pathEffect = android.graphics.CornerPathEffect(cornerRadius)
        canvas.drawPath(pathPointer, paintPointer)
        paintPointer.pathEffect = null
        canvas.drawCircle(0f, 0f, dpToPx(10f), paintPointer)
        canvas.restore()
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}