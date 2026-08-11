package com.example.bmicalculator.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bmicalculator.R
import com.example.bmicalculator.ui.theme.Black
import com.example.bmicalculator.ui.theme.Grad1
import com.example.bmicalculator.ui.theme.Grad4
import com.example.bmicalculator.ui.theme.Grad5
import com.example.bmicalculator.ui.theme.Grad8
import com.example.bmicalculator.ui.theme.White

@Preview
@Composable
fun Per() {
    BmiColorWheelScreen(
        age = 25,
        gender = 1,
        currentBmi = 34.8f
    )
}

@Composable
fun BmiColorWheelScreen(
    age: Int,
    gender: Int,
    currentBmi: Float = 0f,
    showPointer: Boolean = true
) {

    val textMeasurer = rememberTextMeasurer()
    val pathPointer = Path()

    var minBmi = 15f
    var maxBmi = 41f
    var bmiRange: FloatArray
    var colorRange: List<Color>

    if (age > 20) {
        bmiRange = floatArrayOf(15f, 16f, 17f, 18.5f, 25f, 30f, 35f, 40f, 41f)
        colorRange = listOf(
            colorResource(R.color.band1),
            colorResource(R.color.band2),
            colorResource(R.color.band3),
            colorResource(R.color.band4),
            colorResource(R.color.band5),
            colorResource(R.color.band6),
            colorResource(R.color.band7),
            colorResource(R.color.band8)
        )

    } else {
        val teenRange = if (gender == 0) {
            BmiUtil.femaleTeenTable.firstOrNull { it.age == age }
        } else {
            BmiUtil.maleTeenTable.firstOrNull { it.age == age }
        }
        minBmi = teenRange?.underweightMax?.minus(1f) ?: 13f
        maxBmi = teenRange?.overweightMax?.plus(1f) ?: 33f
        bmiRange = if (teenRange != null) {
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
        colorRange = listOf(
            colorResource(R.color.band3),
            colorResource(R.color.band4),
            colorResource(R.color.band5),
            colorResource(R.color.band6)
        )
    }
    val totalRange = maxBmi - minBmi
    // 预计算角度列表 angleList
    val angleList = mutableListOf<Float>()
    var currentStartAngle = 180f
    angleList.add(currentStartAngle)
    for (i in 0 until bmiRange.size - 1) {
        val startVal = bmiRange[i]
        val endVal = bmiRange[i + 1]
        val sweepAngle = ((endVal - startVal) / totalRange) * 180f
        currentStartAngle += sweepAngle
        angleList.add(currentStartAngle)
    }


    val animatedBmi = remember { Animatable(minBmi) }

    LaunchedEffect(currentBmi) {
        animatedBmi.snapTo(minBmi)

        animatedBmi.animateTo(
            targetValue = currentBmi.coerceIn(minBmi, maxBmi),
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            ),
        )
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(White)
            .padding(15.dp),
        contentDescription = ""
    ) {

        val rawW = size.width
        val centerX = rawW / 2
        val strokeW = 80.dp.toPx()

        val textMargin = 6.dp.toPx()
        val radius: Float = (0.95f * rawW - strokeW) / 2
        val centerY = textMargin + rawW / 2


        // 绘制色轮图
        if (angleList.size >= bmiRange.size) {
            for (i in 0 until bmiRange.size - 1) {
                val startAngle = angleList[i]
                val sweepAngle = angleList[i + 1] - startAngle
                drawArc(
                    color = colorRange[i],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(strokeW),
                )
            }
        }

        // 绘制Bmi数字
        if (angleList.size >= bmiRange.size) {
            for (i in bmiRange.indices) {
                if (i == 0 || i == bmiRange.lastIndex) continue

                val angel = angleList[i]
                val bmiVal = bmiRange[i]
                val text = if (bmiVal % 1f == 0f) bmiVal.toInt().toString() else bmiVal.toString()
                translate(centerX, centerY) {
                    rotate(
                        degrees = angel + 90,
                        pivot = Offset(0f, 0f)
                    ) {

                        val measured = textMeasurer.measure(
                            AnnotatedString(text), TextStyle(
                                color = Black,
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                            )
                        )
                        val textW = measured.size.width

                        drawText(
                            textMeasurer = textMeasurer,
                            text = AnnotatedString(text),
                            topLeft = Offset((-textW / 2f), -centerY),
                            style = TextStyle(
                                color = Black,
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                            ),
                            overflow = TextOverflow.Visible,
                            softWrap = false,
                            maxLines = 1,
                        )
                    }
                }

            }
        }
        // 绘制指针
        if (showPointer) {
            translate(centerX, centerY) {
                val pointerAngle = (animatedBmi.value - minBmi) / totalRange * 180f
                rotate(
                    pointerAngle + 180,
                    pivot = Offset(0f, 0f)
                ) {

                    pathPointer.reset()
                    val pointerLength = radius - (strokeW / 3f)
                    val pointerWidth = 25.dp.toPx()
                    val headRadius = 2.dp.toPx()

                    pathPointer.moveTo(0f, -pointerWidth / 2f)
                    pathPointer.quadraticTo(
                        pointerLength * 0.7f,
                        -pointerWidth * 0.2f,
                        pointerLength - headRadius,
                        -headRadius
                    )
                    pathPointer.arcTo(
                        rect = Rect(
                            pointerLength - 2 * headRadius,
                            -headRadius,
                            pointerLength,
                            headRadius,
                        ),
                        -90f,
                        180f,
                        false
                    )
                    pathPointer.quadraticTo(
                        pointerLength * 0.7f,
                        pointerWidth * 0.2f,
                        0f,
                        pointerWidth / 2f
                    )
                    pathPointer.close()

                    drawCircle(
                        color = Black,
                        radius = 12.5.dp.toPx(),
                        center = Offset(0f, 0f),
                        style = Fill,
                    )
                    drawPath(
                        pathPointer,
                        color = Black,
                        style = Fill,
                    )
                }
            }
        }

    }
}


@Preview
@Composable
fun PrePP(modifier: Modifier = Modifier) {
    Sample()
}

@Composable
fun Sample() {
    val textMeasurer = rememberTextMeasurer()

    val result = textMeasurer.measure(
        text = "hello",
        style = TextStyle(
            color = Black,
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(R.font.font_extrabold)),
            textAlign = TextAlign.Center
        ),
        overflow = TextOverflow.Visible,
        softWrap = false,
        maxLines = 1,
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(White),
        contentDescription = ""
    ) {

        drawCircle(
            color = Grad5,
            radius = size.minDimension / 2,
            center = center,
            alpha = 1f,
            style = Stroke(width = 5.dp.toPx())
        )
        drawLine(
            color = Grad1,
            start = Offset(5.dp.toPx(), size.height / 3),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 5.dp.toPx(),
        )

        drawArc(
            color = Grad4,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(size.width / 2, size.height / 2),
            size = Size(250f, 250f),
            style = Stroke(width = 5.dp.toPx()),
        )
        rotate(
            degrees = 40f,
            pivot = center
        ) {
            drawText(
                textLayoutResult = result,
                brush = SolidColor(Grad8),
                topLeft = Offset(size.width / 2, size.height / 2),
                alpha = 1f,
                textDecoration = TextDecoration.None,
                drawStyle = Fill,
                blendMode = BlendMode.SrcOver
            )
        }

        drawText(
            textMeasurer = textMeasurer,
            text = AnnotatedString("ds"),
            topLeft = Offset(size.width / 2 + 50f, size.height / 2),
            style = TextStyle(
                color = Black,
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                textAlign = TextAlign.Center
            ),
            overflow = TextOverflow.Visible,
            softWrap = false,
            maxLines = 1,
        )
    }
}