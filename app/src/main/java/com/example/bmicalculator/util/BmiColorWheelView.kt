package com.example.bmicalculator.util

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.bmicalculator.R
import androidx.core.content.withStyledAttributes

class BmiColorWheelView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 外部传入数据
    var age: Int = 20
        set(value) {
            field = value
            updateRangeAndColor()
            // 属性改变时，指针从头重新跑一次动画
            if (showPointer) {
                startPointerAnimation()
            }
        }
    var gender: Int = 1 // 0女 1男
        set(value) {
            field = value
            updateRangeAndColor()
            if (showPointer) {
                startPointerAnimation()
            }
        }
    var currentBmi = 20f
        set(value) {
            field = value.coerceIn(minBmi, maxBmi)
            // 只有开启指针时才执行动画
            if (showPointer) {
                startPointerAnimation()
            }
        }
    // 是否显示指针 true显示 / false隐藏
    var showPointer = true
        set(value) {
            field = value
            invalidate()
        }


    // 动态变量
    private var minBmi = 15.0f
    private var maxBmi = 41.0f
    private var totalRange = maxBmi - minBmi
    private var bmiRanges = floatArrayOf(15f, 16f, 18.5f, 25f, 30f, 35f, 40f, 41f)
    private var colors = intArrayOf()

    // 关键：将角度列表提取为全局成员变量，与 bmiRanges 长度保持实时同步
    private val angleList = ArrayList<Float>()

    // 动画控制变量
    private var animatedBmi = minBmi
    private var pointerAnimator: ValueAnimator? = null

    // 画笔
    private val paintArc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.black)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(
            ResourcesCompat.getFont(context, R.font.font_extrabold),
            Typeface.NORMAL
        )
    }
    private val paintPointer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.point)
        style = Paint.Style.FILL
    }
    private val rectF = RectF()

    init {
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.BmiColorWheelView) {
                showPointer = getBoolean(R.styleable.BmiColorWheelView_showPointer, true)
            }
        }
        updateRangeAndColor()
    }

    /** 核心：根据年龄性别更新表盘刻度区间、对应色值，并预计算好角度 */
    private fun updateRangeAndColor() {
        if (age > 20) {
            minBmi = 15f
            maxBmi = 41f
            bmiRanges = floatArrayOf(15f, 16f, 17f, 18.5f, 25f, 30f, 35f, 40f, 41f)
            colors = intArrayOf(
                ContextCompat.getColor(context, R.color.band1),
                ContextCompat.getColor(context, R.color.band2),
                ContextCompat.getColor(context, R.color.band3),
                ContextCompat.getColor(context, R.color.band4),
                ContextCompat.getColor(context, R.color.band5),
                ContextCompat.getColor(context, R.color.band6),
                ContextCompat.getColor(context, R.color.band7),
                ContextCompat.getColor(context, R.color.band8)
            )
        } else {
            val teenRange = if (gender == 0) {
                BmiUtil.femaleTeenTable.firstOrNull { it.age == age }
            } else {
                BmiUtil.maleTeenTable.firstOrNull { it.age == age }
            }
            minBmi = teenRange?.underweightMax?.minus(1f) ?: 13f
            maxBmi = teenRange?.overweightMax?.plus(1f) ?: 33f
            bmiRanges = if (teenRange != null) {
                floatArrayOf(
                    minBmi,
                    teenRange.underweightMax,
                    teenRange.normalMax,
                    teenRange.overweightMax,
                    maxBmi
                )
            } else {
                floatArrayOf(13f, 15f, 20f, 25f, 33f)
            }
            colors = intArrayOf(
                ContextCompat.getColor(context, R.color.band3),
                ContextCompat.getColor(context, R.color.band4),
                ContextCompat.getColor(context, R.color.band5),
                ContextCompat.getColor(context, R.color.band6)
            )
        }
        totalRange = maxBmi - minBmi

        // 数据更新时，立即重新计算并清空重组全局角度缓存，防止 onDraw 越界
        angleList.clear()
        var currentStartAngle = 180.0f
        angleList.add(currentStartAngle)
        for (i in 0 until bmiRanges.size - 1) {
            val startVal = bmiRanges[i]
            val endVal = bmiRanges[i + 1]
            val sweepAngle = ((endVal - startVal) / totalRange) * 180.0f
            currentStartAngle += sweepAngle
            angleList.add(currentStartAngle)
        }
    }

    /** 启动指针旋转动画 */
    private fun startPointerAnimation() {
        // 先安全的限制当前目标 BMI 边界
        val targetBmi = currentBmi.coerceIn(minBmi, maxBmi)

        // 如果已有动画正在运行，先取消
        pointerAnimator?.cancel()

        // 动画从最左侧起点(minBmi) 逐步增加到 目标值(targetBmi)
        pointerAnimator = ValueAnimator.ofFloat(minBmi, targetBmi).apply {
            duration = 1000 // 动画时长 1000 毫秒（1秒）
            interpolator = DecelerateInterpolator() // 减速插值器，让指针结尾落地更平滑
            addUpdateListener { animator ->
                animatedBmi = animator.animatedValue as Float
                invalidate() // 触发重绘
            }
            start()
        }
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
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        // 1. 绘制分段彩色圆弧（使用预存的全局 angleList 安全绘制）
        if (angleList.size >= bmiRanges.size) {
            for (i in 0 until bmiRanges.size - 1) {
                val startAngle = angleList[i]
                val sweepAngle = angleList[i + 1] - startAngle
                paintArc.color = colors[i]
                canvas.drawArc(rectF, startAngle, sweepAngle, false, paintArc)
            }
        }

        // 2. 绘制刻度数字（此时 angleList 数据安全可靠）
        paintText.textSize = dpToPx(11f)
        val textDistance = radius + (strokeW / 2f) + (w * 0.04f)
        if (angleList.size >= bmiRanges.size) {
            for (i in bmiRanges.indices) {
                if (i == 0 || i == bmiRanges.lastIndex) continue
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
        }

        // 3. 绘制带有动画效果的指针（核心：使用已动画化的 animatedBmi 计算角度）
        if (showPointer) {
            canvas.save()
            canvas.translate(centerX, centerY)

            val pointerAngle = ((animatedBmi - minBmi) / totalRange) * 180f
            canvas.rotate(180f + pointerAngle)

            val pathPointer = Path()
            val pointerLength = radius - (strokeW / 2f) + dpToPx(8f)
            val pointerWidth = dpToPx(20f)
            val headRadius = dpToPx(2f)

            pathPointer.reset()
            pathPointer.moveTo(0f, -pointerWidth / 2f)
            pathPointer.quadTo(pointerLength * 0.7f, -pointerWidth * 0.2f, pointerLength - headRadius, -headRadius)
            pathPointer.arcTo(
                pointerLength - 2 * headRadius, -headRadius,
                pointerLength, headRadius,
                -90f, 180f, false
            )
            pathPointer.quadTo(pointerLength * 0.7f, pointerWidth * 0.15f, 0f, pointerWidth / 2f)
            pathPointer.close()

            paintPointer.pathEffect = null
            canvas.drawPath(pathPointer, paintPointer)

            // 绘制轴心圆
            canvas.drawCircle(0f, 0f, dpToPx(10f), paintPointer)
            canvas.restore()
        }
    }

    override fun onDetachedFromWindow() {
        // 防止内存泄漏：当 View 销毁时，及时停止并释放动画
        pointerAnimator?.cancel()
        super.onDetachedFromWindow()
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}
