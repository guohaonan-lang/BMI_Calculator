package com.example.bmicalculator.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.compose.ui.text.font.Font
import androidx.core.content.res.ResourcesCompat
import com.example.bmicalculator.R

class BmiColorWheelView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val minBmi = 15.0f
    private val maxBmi = 41.0f
    private val totalRange = maxBmi - minBmi

    // 8个区域的边界点
    private val bmiRanges = floatArrayOf(15.0f, 16.0f, 18.5f, 20.0f, 25.0f, 28.0f, 34.0f, 40.0f, 41.0f)

    // 8个区域的颜色
    private val colors = intArrayOf(
        Color.parseColor("#286DE6"), Color.parseColor("#349CEA"),
        Color.parseColor("#5BB1F5"), Color.parseColor("#A8C526"),
        Color.parseColor("#FECD2E"), Color.parseColor("#FD9845"),
        Color.parseColor("#F67D3C"), Color.parseColor("#F04E46")
    )

    private val paintArc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT // 边缘平齐，防止平滑圆头导致区域重叠
    }

    // 绘制倾斜数字的画笔
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#000000") // 暗灰色字体
        textAlign = Paint.Align.CENTER     // 居中对齐，保证旋转后居中
        typeface = Typeface.create(ResourcesCompat.getFont(context, R.font.font_extrabold), Typeface.NORMAL)
    }

    var currentBmi = 34.0f
        set(value) {
            // 数据输入的源头锁死边界，确保绝对不越界
            field = value.coerceIn(minBmi, maxBmi)
            invalidate() // 赋值时自动触发刷新重绘
        }

    // 绘制指针的画笔
    private val paintPointer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333") // 指针深灰色
        style = Paint.Style.FILL            // 实心
    }

    private val rectF = RectF()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        // 默认宽度改为通过 DP 转换（例如 260dp），防止在低分辨率手机上越界
        val defaultWidth = dpToPx(260f).toInt()

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> Math.min(defaultWidth, widthSize)
            else -> defaultWidth
        }

        // 动态计算顶部和四周文字所需的绝对安全边距（转换为px）
        val textPadding = dpToPx(28f)
        val height = (width / 2) + textPadding.toInt()

        // 核心修正：必须考虑 Padding 带来的损耗，防止内容越界被父布局强行裁剪
        setMeasuredDimension(width, height + paddingTop + paddingBottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val rawW = width.toFloat() - paddingLeft - paddingRight
        if (rawW <= 0) return

        // 在内部将绘制使用的有效宽度缩减为 80%
        val w = rawW * 0.8f

// 此时由于 View 实际宽度没变，计算圆心时需要把缩减的 20% 居中平分补回来
        val extraOffset = (rawW - w) / 2f
        val centerX = paddingLeft + w / 2f + extraOffset // 加上偏移量保证依然居中


        // 圆环厚度
        val strokeW = dpToPx(80f)
        paintArc.strokeWidth = strokeW

        // 预留最上方的文字高度，圆心向下平移
        val textMargin = dpToPx(24f)

        val radius = (w - strokeW) / 2f
        val centerY = paddingTop + textMargin + radius + strokeW / 2f

        // 圆弧的外切矩形
        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // 1. 先绘制 8 个彩色圆弧区域
        var currentStartAngle = 180.0f
        val angleList = ArrayList<Float>() // 用来记录所有交界处的绝对角度
        angleList.add(currentStartAngle)   // 记录第一个点 15 的起始角度 (180°)

        for (i in 0 until bmiRanges.size - 1) {
            val startVal = bmiRanges[i]
            val endVal = bmiRanges[i + 1]
            val sweepAngle = ((endVal - startVal) / totalRange) * 180.0f

            paintArc.color = colors[i]
            canvas.drawArc(rectF, currentStartAngle, sweepAngle, false, paintArc)

            currentStartAngle += sweepAngle
            angleList.add(currentStartAngle) // 依次记录各个交界处的断点角度
        }

        // 2. 动态旋转画布，在外交界处绘制随弧度倾斜的数字
        paintText.textSize = dpToPx(11f)

        // 数字相对于圆弧外边缘的外扩距离 (圆弧半径 + 半个圆弧厚度 + 额外间距)
        val textDistance = radius + (strokeW / 2f) + (w * 0.04f)

        for (i in bmiRanges.indices) {
            val angle = angleList[i]       // 当前交界处的绝对角度
            val bmiValue = bmiRanges[i]    // 当前对应的数字（如 15.0, 16.0 ...）

            // 将格式转化为去掉多余小数点的字符串（如 15.0 -> "15", 18.5 -> "18.5"）
            val text = if (bmiValue % 1f == 0f) bmiValue.toInt().toString() else bmiValue.toString()

            canvas.save() // 保存当前画布状态

            // 核心步骤 A：将画布原点平移到圆心
            canvas.translate(centerX, centerY)

            // 核心步骤 B：将画布旋转到与当前交界线平行的方向
            // 在 Android 坐标系中，顺时针旋转。由于原点在右，我们需要让字头朝上，因此旋转 (angle + 90) 度
            canvas.rotate(angle + 90f)

            // 核心步骤 C：由于已经完成了平移和旋转，现在直接在局部坐标系的 Y 轴负方向上写字即可
            // (0, -textDistance) 代表从圆心出发，往文字轨道的正上方写字
            // Paint.FontMetrics 用于微调文字垂直居中顶对齐
            val fontMetrics = paintText.fontMetrics
            val baseline = -textDistance - fontMetrics.ascent

            canvas.drawText(text, 0f, baseline, paintText)




            canvas.restore() // 恢复画布状态，避免干扰下一次循环旋转
        }
        canvas.save()

        // 1. 平移画布原点到色轮中心
        canvas.translate(centerX, centerY)

        // 2. 严密映射：将当前 BMI 值（15~41）完美线性映射到 0° 到 180° 的物理指针角度
        val pointerAngle = ((currentBmi - minBmi) / totalRange) * 180f

        // 3. 旋转画布。因为半圆起点在正左方（Android坐标系的180°位置），
        // 旋转 180° + pointerAngle 即可让指针精准指向对应的数值
        canvas.rotate(180f + pointerAngle)

        // 4. 构建指针三角形路径
        val pathPointer = android.graphics.Path()

        // 指针尺寸
        val pointerLength = radius - (strokeW / 2f) + dpToPx(8f)
        val pointerWidth = dpToPx(20f) // 指针底座宽度
        //指针头部圆角的半径
        val cornerRadius = dpToPx(4f)

        // 绘制一个沿当前旋转方向正右方延伸的锐角三角形
        pathPointer.moveTo(pointerLength, 0f)                     // 尖端点
        pathPointer.lineTo(0f, -pointerWidth / 2f)                // 上翼点
        pathPointer.lineTo(0f, pointerWidth / 2f)                 // 下翼点
        pathPointer.close()


        // 【崩溃修正】：直接将 CornerPathEffect 赋值给 pathEffect，不使用 ComposePathEffect
        paintPointer.pathEffect = android.graphics.CornerPathEffect(cornerRadius)
        paintPointer.style = Paint.Style.FILL

        // 绘出头部圆润的指针
        canvas.drawPath(pathPointer, paintPointer)

        // 重要：绘制完指针后，必须立即恢复为 null，避免干扰后续其他地方的绘制（如轴心圆点）
        paintPointer.pathEffect = null

        // 5. 绘制中心轴小圆点，使其更具仪表盘的实体质感
        canvas.drawCircle(0f, 0f, dpToPx(10f), paintPointer)

        canvas.restore()
    }
    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}