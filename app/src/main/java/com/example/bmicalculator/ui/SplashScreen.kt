package com.example.bmicalculator.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.ui.theme.Blue
import com.example.bmicalculator.viewmodel.SplashViewModel
import kotlinx.coroutines.launch


@Composable
fun SplashScreen(viewModel: SplashViewModel) {

    val uiState = viewModel.state.collectAsStateWithLifecycle()

    var parentHeightPx by remember { mutableIntStateOf(0) }
    val animatedOffsetY = remember { Animatable(0f) }

    val density = LocalDensity.current

    val rotationAngle = remember { Animatable(-30f) }

    val animateAlpha = remember { Animatable(0.3f) }
    LaunchedEffect(Unit) {
        launch {
            rotationAngle.animateTo(
                targetValue = 30f,
                animationSpec = tween(
                    durationMillis = 700,
                    easing = CubicBezierEasing(0.25f, 0f, 0.1f, 0.1f)
                )
            )
            rotationAngle.animateTo(
                targetValue = -30f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = CubicBezierEasing(0.25f, 0f, 0.1f, 0.1f)
                )
            )
            if (uiState.value.recordCount > 0) viewModel.processIntent(SplashViewModel.SplashIntent.NavToInputFra)
            else viewModel.processIntent(SplashViewModel.SplashIntent.NavToInputAct)
        }
        launch {
            animateAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1500,
                    easing = LinearEasing
                ),
            )
        }

    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Blue)
            .onGloballyPositioned { coordinates ->
                parentHeightPx = coordinates.size.height
            }
            .systemBarsPadding(),
    ) {
        val (colorWheel, pointer, appName, brand) = createRefs()

        LaunchedEffect(parentHeightPx) {
            if (parentHeightPx <= 0) return@LaunchedEffect

            val startY = parentHeightPx * 0.1f
            val targetY = parentHeightPx * 0.55f

            animatedOffsetY.snapTo(startY)

            // 执行动画平滑过度到目标位置
            animatedOffsetY.animateTo(
                targetValue = targetY,
                // 设置补间动画
                animationSpec = tween(
                    durationMillis = 1500,
                    easing = LinearEasing
                )
            )
        }


        val animatedOffsetYDp = with(density) {
            animatedOffsetY.value.toDp()
        }

        Box(
            modifier = Modifier
                .constrainAs(colorWheel) {
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                }
                .offset(30.dp, -animatedOffsetYDp),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(R.drawable.splash6),
                contentDescription = null,
                modifier = Modifier
                    .alpha(animateAlpha.value)
            )
            Image(
                painter = painterResource(R.drawable.splash3),
                contentDescription = null,
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = rotationAngle.value // 应用动画角度
                        // 设置旋转中心为指针底部中心（X: 50%, Y: 90%）
                        transformOrigin = TransformOrigin(0.5f, 0.9f)
                    }
                    .alpha(animateAlpha.value)
            )
        }
        Image(
            painter = painterResource(R.drawable.splash4),
            contentDescription = null,
            modifier = Modifier
                .offset(30.dp, -animatedOffsetYDp)
                .constrainAs(appName) {
                    start.linkTo(colorWheel.start)
                    top.linkTo(colorWheel.bottom)
                }
                .alpha(animateAlpha.value)

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