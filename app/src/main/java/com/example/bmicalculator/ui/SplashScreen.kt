package com.example.bmicalculator.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.bmicalculator.R
import com.example.bmicalculator.ui.theme.Blue


@Preview
@Composable
fun PreSplash() {
    SplashScreen()
}

@Composable
fun SplashScreen() {

    var parentHeightPx by remember { mutableStateOf(0) }
    val animatedOffsetY = remember { Animatable(0f) }
    val density = LocalDensity.current

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Blue)
            .onGloballyPositioned { coordinates ->
                parentHeightPx = coordinates.size.height
            },
    ) {
        val (colorWheel, pointer, appName, brand) = createRefs()

        LaunchedEffect(parentHeightPx) {
            if (parentHeightPx <= 0) return@LaunchedEffect

            val startY = parentHeightPx * 0.25f
            // 终点：屏幕高度的 1/2 (50%，即垂直居中)
            val targetY = parentHeightPx * 0.5f

            // 【关键】：先将动画瞬间跳到起点位置
            animatedOffsetY.snapTo(startY)

            // 【关键】：执行动画平滑过度到目标位置
            animatedOffsetY.animateTo(
                targetValue = targetY,
                // 设置补间动画规格：持续 1秒，使用 FastOutSlowInEasing 缓动曲线
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = LinearEasing
                )
            )
        }



        val animatedOffsetYDp = with(density) {
            animatedOffsetY.value.toDp()
        }

        Image(
            painter = painterResource(R.drawable.splash6),
            contentDescription = null,
            modifier = Modifier
                .constrainAs(colorWheel) {
                    start.linkTo(parent.start, animatedOffsetYDp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .alpha(0.3f)
        )
        Image(
            painter = painterResource(R.drawable.splash4),
            contentDescription = null,
            modifier = Modifier
                .constrainAs(appName) {
                    start.linkTo(colorWheel.start)
                    top.linkTo(colorWheel.bottom)
                }
                .alpha(0.3f)
        )
        Image(
            painter = painterResource(R.drawable.splash3),
            contentDescription = null,
            modifier = Modifier
                .constrainAs(pointer) {
                    start.linkTo(colorWheel.start)
                    top.linkTo(colorWheel.top)
                    end.linkTo(colorWheel.end)
                    bottom.linkTo(colorWheel.bottom)
                }
                .alpha(0.3f)
        )



        Image(
            painter = painterResource(R.drawable.splash2),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .constrainAs(brand) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
    }


}