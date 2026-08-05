package com.example.bmicalculator.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.model.Grade
import com.example.bmicalculator.ui.theme.Black
import com.example.bmicalculator.ui.theme.Blue
import com.example.bmicalculator.ui.theme.Grad4
import com.example.bmicalculator.ui.theme.Gray
import com.example.bmicalculator.ui.theme.Red
import com.example.bmicalculator.ui.theme.White
import com.example.bmicalculator.util.BmiColorWheelView
import com.example.bmicalculator.viewmodel.ResultViewModel

//@Preview(showBackground = true, widthDp = 441, heightDp = 891)
//@Composable
//fun PreResult() {
//    ResultScreen()
//}

@Composable
fun ResultScreen(viewModel: ResultViewModel) {

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    var bottomShow by remember { mutableStateOf(false) }
    var deleteShow by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        ResultTitle(
            uiState.value.isRecent,
            { viewModel.processIntent(ResultViewModel.ResultIntent.BackPage) },
            { deleteShow = true })
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f)
        ) {
            ColorWheel(
                uiState.value.bmiData?.age ?: 25,
                uiState.value.bmiData?.gender ?: 1,
                uiState.value.bmiData?.bmiValue ?: 1f
            )
            DescribeText(
                bmiValue = uiState.value.bmiData?.bmiValue ?: 1f,
                bmiLevel = stringResource(uiState.value.levelNameInt),
                weightText = uiState.value.weightText,
                heightText = uiState.value.heightText,
                genderText = stringResource(uiState.value.genderTextInt),
                ageText = uiState.value.bmiData?.age.toString(),
                buttonColor = uiState.value.buttonColor,
            ) { bottomShow = true }
            if (uiState.value.isFirst) GradeList(gradeList = uiState.value.gradeList)
            ResultAssessment(
                assessment1Text = stringResource(uiState.value.assessment1Int),
                assessment2Text = stringResource(uiState.value.baseTextInt)+ uiState.value.assessment2Text,
                rangeText = uiState.value.normalRangeText,
                differenceText = uiState.value.differenceText
            )
            val timeTagText =
                "${stringResource(uiState.value.timeMonthInt)} ${uiState.value.timeDay}, ${uiState.value.timeYear} ${
                    stringResource(uiState.value.timePeriodInt)
                }"

            TimeLine(timeTagText)
            if (!uiState.value.isFirst) AdText()

        }
        if (!uiState.value.isRecent) Button(
            onClick = {
                viewModel.processIntent(ResultViewModel.ResultIntent.SaveResult)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(Blue)
        ) {
            Text(
                stringResource(R.string.result_save),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                color = White
            )
        }
        BmiLevelBottom(bottomShow, { bottomShow = false }, uiState)

        BackHandler(enabled = !uiState.value.isRecent) {
            deleteShow = !deleteShow
        }
        if (deleteShow) {
            ShowDeleteDialog(
                { deleteShow = false },
                {
                    deleteShow = false
                    viewModel.processIntent(ResultViewModel.ResultIntent.DeleteResult)
                })
        }
    }
}

@Composable
fun ResultTitle(isRecent: Boolean, function: () -> Unit, function1: () -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, top = 15.dp, end = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isRecent) {
            Image(
                painter = painterResource(R.drawable.recent_back),
                contentDescription = "back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        onClick = function
                    ),
            )
            Text(
                stringResource(R.string.dialog_delete_delete),
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 16.sp,
                color = Black,
                modifier = Modifier.clickable(
                    onClick = function1
                )
            )
        } else {
            Text(
                stringResource(R.string.result_discard),
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 16.sp,
                color = Black,
                modifier = Modifier.clickable(
                    onClick = function1
                )
            )
        }

    }
}

@Composable
fun ColorWheel(newAge: Int, newGender: Int, bmiValue: Float) {
    AndroidView(
        factory = { context ->
            // 初始化原生自定义View，等价于XML inflate
            BmiColorWheelView(context).apply {
                age = newAge
                gender = newGender
                currentBmi = bmiValue
            }
        },
        modifier = Modifier
            .fillMaxWidth(),
        update = { view ->
            // 【重要】状态变更时回调，刷新控件
            view.age = newAge
            view.gender = newGender
            view.currentBmi = bmiValue
        }
    )
}

@Composable
fun DescribeText(
    bmiValue: Float,
    bmiLevel: String,
    weightText: String,
    heightText: String,
    genderText: String,
    ageText: String,
    buttonColor: Int,
    function: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Your BMI is...",
            fontFamily = FontFamily(Font(R.font.font_extrabold)),
            fontSize = 18.sp
        )
        Text(
            String.format("%.1f", bmiValue),
            fontFamily = FontFamily(Font(R.font.font_extrabold)),
            fontSize = 64.sp
        )
        Button(
            onClick = function,
            colors = ButtonDefaults.buttonColors(colorResource(buttonColor))
        ) {
            Text(
                text = bmiLevel,
                modifier = Modifier.padding(end = 5.dp),
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                fontSize = 18.sp
            )
            Image(
                painter = painterResource(R.drawable.help_circle),
                modifier = Modifier.size(18.dp),
                contentDescription = ""
            )
        }
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .alpha(0.5f),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                "$weightText | ",
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 14.sp
            )
            Text(
                "$heightText | ",
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 14.sp
            )
            Text(
                "$genderText | ",
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 14.sp
            )
            Text(
                text = ageText + stringResource(R.string.years_old),
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun GradeList(
    gradeList: List<Grade> = emptyList()
) {
    for (item in gradeList) {
        GradeItem(grade = item)
    }
}

@Composable
fun GradeItem(
    grade: Grade
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .padding(horizontal = 16.dp)
            .background(
                color = (if (grade.isSelect) colorResource(grade.color) else White),
                shape = RoundedCornerShape(15.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        // 左侧颜色圆点
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background((if (grade.isSelect) White else colorResource(grade.color)))
                .padding(start = 10.dp)
        )


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(grade.gradeNameInt),
                fontFamily = FontFamily(
                    Font(
                        if (grade.isSelect) R.font.font_extrabold
                        else R.font.font_regular
                    )
                ),
                color = (if (grade.isSelect) White else Black),
                fontSize = 14.sp,
                modifier = Modifier.alpha(if (grade.isSelect) 1f else 0.7f)
            )
            Text(
                text = grade.gradeRange,
                fontFamily = FontFamily(
                    Font(
                        if (grade.isSelect) R.font.font_extrabold
                        else R.font.font_regular
                    )
                ),
                fontSize = 14.sp,
                color = (if (grade.isSelect) White else Black),
                modifier = Modifier.alpha(if (grade.isSelect) 1f else 0.7f)
            )
        }
    }
}

@Composable
fun ResultAssessment(
    assessment1Text: String,
    assessment2Text: String,
    rangeText: String,
    differenceText: String
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 30.dp, vertical = 10.dp)
            .background(Gray, RoundedCornerShape(10.dp))
            .padding(horizontal = 15.dp, vertical = 15.dp)
    ) {
        Text(
            text = assessment1Text,
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 14.sp,
            color = Black
        )
        Text(
            text = assessment2Text,
            modifier = Modifier.padding(top = 20.dp),
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 14.sp,
            color = Black
        )
        Row(
            modifier = Modifier.padding(top = 3.dp)
        ) {
            Text(
                text = rangeText,
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                fontSize = 14.sp,
                color = Black
            )
            Text(
                text = differenceText,
                modifier = Modifier.padding(start = 5.dp),
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                fontSize = 14.sp,
                color = Red
            )
        }
    }
}

@Composable
fun TimeLine(timeTagText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically // 等价 android:gravity="center_vertical"
    ) {
        // 左侧分割线
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color.Black.copy(alpha = 0.3f))
        )

        Text(
            text = timeTagText,
            modifier = Modifier.padding(horizontal = 10.dp),
            fontSize = 12.sp,
            color = Color.Black.copy(alpha = 0.5f),
            fontFamily = FontFamily(Font(R.font.font_extrabold))
        )

        // 右侧分割线
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color.Black.copy(alpha = 0.3f))
        )
    }
}

@Composable
fun AdText() {
    // ====== 标题文本：ad_apps_you_might_need
    Text(
        text = stringResource(R.string.ad_apps_you_might_need),
        modifier = Modifier
            .padding(start = 15.dp, top = 10.dp),
        fontSize = 16.sp,
        color = Black,
        fontFamily = FontFamily(Font(R.font.font_extrabold))
    )

    // ====== 广告卡片 ad1
    ConstraintLayout(
        modifier = Modifier
            .padding(top = 20.dp, start = 20.dp, end = 20.dp)
            .fillMaxWidth()
            .height(80.dp)
            .background(
                color = Gray,
                shape = RoundedCornerShape(15.dp)
            ),
    ) {
        // 创建引用id，对应xml各个控件
        val (ivIcon, tvTitle, tvDesc, tagAd, rowStar, tvScore) = createRefs()

        // 左侧图标 ad1_iv1
        Image(
            painter = painterResource(R.drawable.ad1),
            contentDescription = "app icon",
            modifier = Modifier
                .padding(start = 15.dp)
                .size(49.dp)
                .constrainAs(ivIcon) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        )

        // 第一行文字 ad1_text1
        Text(
            text = stringResource(R.string.ad1_text1),
            modifier = Modifier
                .width(240.dp)
                .constrainAs(tvTitle) {
                    start.linkTo(ivIcon.end, margin = 15.dp)
                    top.linkTo(ivIcon.top)
                },
            fontSize = 14.sp,
            color = Black,
            fontFamily = FontFamily(Font(R.font.font_regular))
        )

        // 第二行文字 ad1_text2
        Text(
            text = stringResource(R.string.ad1_text2),
            modifier = Modifier
                .width(240.dp)
                .padding(top = 2.dp)
                .constrainAs(tvDesc) {
                    start.linkTo(tvTitle.start)
                    top.linkTo(tvTitle.bottom)
                },
            fontSize = 12.sp,
            color = Black.copy(alpha = 0.8f),
            fontFamily = FontFamily(Font(R.font.font_regular))
        )

        // 右上角 AD标签
        Text(
            text = "AD",
            modifier = Modifier
                .width(30.dp)
                .height(15.dp)
                .background(
                    color = Grad4.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 10.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 0.dp
                    ),
                )
                .constrainAs(tagAd) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                },
            fontSize = 10.sp,
            color = White,
            textAlign = TextAlign.Center,
        )

        // 五星横向布局（替代5个约束ImageView，效果完全一致，代码精简）
        Row(
            modifier = Modifier
                .padding(top = 3.dp)
                .constrainAs(rowStar) {
                    start.linkTo(tvDesc.start)
                    top.linkTo(tvDesc.bottom)
                },
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(5) {
                Image(
                    painter = painterResource(R.drawable.ad_star),
                    contentDescription = "star",
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // 评分 4.8
        Text(
            text = "4.8",
            modifier = Modifier
                .padding(start = 5.dp, top = 1.5.dp)
                .constrainAs(tvScore) {
                    start.linkTo(rowStar.end)
                    top.linkTo(tvDesc.bottom)
                },
            fontSize = 12.sp,
            color = Black,
            fontFamily = FontFamily(Font(R.font.font_regular))
        )
    }

    // ====== 广告卡片 ad2
    ConstraintLayout(
        modifier = Modifier
            .padding(top = 10.dp, start = 20.dp, end = 20.dp)
            .fillMaxWidth()
            .height(80.dp)
            .background(
                color = Gray,
                shape = RoundedCornerShape(15.dp)
            ),
    ) {
        // 创建引用id，对应xml各个控件
        val (ivIcon, tvTitle, tvDesc, tagAd, rowStar, tvScore) = createRefs()

        // 左侧图标 ad1_iv1
        Image(
            painter = painterResource(R.drawable.ad2),
            contentDescription = "app icon",
            modifier = Modifier
                .padding(start = 15.dp)
                .size(49.dp)
                .constrainAs(ivIcon) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        )

        // 第一行文字 ad1_text1
        Text(
            text = stringResource(R.string.ad2_text1),
            modifier = Modifier
                .width(240.dp)
                .constrainAs(tvTitle) {
                    start.linkTo(ivIcon.end, margin = 15.dp)
                    top.linkTo(ivIcon.top)
                },
            fontSize = 14.sp,
            color = Black,
            fontFamily = FontFamily(Font(R.font.font_regular))
        )

        // 第二行文字 ad2_text2
        Text(
            text = stringResource(R.string.ad2_text2),
            modifier = Modifier
                .width(240.dp)
                .padding(top = 2.dp)
                .constrainAs(tvDesc) {
                    start.linkTo(tvTitle.start)
                    top.linkTo(tvTitle.bottom)
                },
            fontSize = 12.sp,
            color = Black.copy(alpha = 0.8f),
            fontFamily = FontFamily(Font(R.font.font_regular))
        )

        // 右上角 AD标签
        Text(
            text = "AD",
            modifier = Modifier
                .width(30.dp)
                .height(15.dp)
                .background(
                    color = Grad4.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 10.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 0.dp
                    ),
                )
                .constrainAs(tagAd) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                },
            fontSize = 10.sp,
            color = White,
            textAlign = TextAlign.Center,
        )

        // 五星横向布局（替代5个约束ImageView，效果完全一致，代码精简）
        Row(
            modifier = Modifier
                .padding(top = 3.dp)
                .constrainAs(rowStar) {
                    start.linkTo(tvDesc.start)
                    top.linkTo(tvDesc.bottom)
                },
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(5) {
                Image(
                    painter = painterResource(R.drawable.ad_star),
                    contentDescription = "star",
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // 评分 4.8
        Text(
            text = "4.9",
            modifier = Modifier
                .padding(start = 5.dp, top = 1.5.dp)
                .constrainAs(tvScore) {
                    start.linkTo(rowStar.end)
                    top.linkTo(tvDesc.bottom)
                },
            fontSize = 12.sp,
            color = Black,
            fontFamily = FontFamily(Font(R.font.font_regular))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmiLevelBottom(
    bottomShow: Boolean,
    function: () -> Unit,
    uiState: State<ResultViewModel.ResultUiState>
) {
    if (!bottomShow) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = function,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = White,
        dragHandle = {},
        sheetState = sheetState
    ) {
        Column {
            Text(
                stringResource(R.string.bottot_grade_bmi_wheel),
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 15.dp, bottom = 10.dp, start = 15.dp)
            )
            ColorWheel(
                uiState.value.bmiData?.age ?: 25,
                uiState.value.bmiData?.gender ?: 1,
                uiState.value.bmiData?.bmiValue ?: 1f
            )
            GradeList(gradeList = uiState.value.gradeList)
            Button(
                onClick = function,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(Blue)
            ) {
                Text(
                    stringResource(R.string.bottomsheet_got_it),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.font_extrabold)),
                    color = White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDeleteDialog(function: () -> Unit, deleteFun: () -> Unit) {
    BasicAlertDialog(
        onDismissRequest = function,
        modifier = Modifier,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = White,
            tonalElevation = 4.dp,
            modifier = Modifier.width(300.dp)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.dialog_delete_confirm),
                    fontFamily = FontFamily(Font(R.font.font_extrabold)),
                    fontSize = 16.sp
                )
                Text(
                    text = stringResource(R.string.dialog_delete_text),
                    fontFamily = FontFamily(Font(R.font.font_regular)),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp, bottom = 10.dp), // 【关键！】必须铺满宽度
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = stringResource(R.string.dialog_delete_cancel_text),
                        fontFamily = FontFamily(Font(R.string.dialog_delete_cancel_text)),
                        fontSize = 16.sp,
                        color = Blue,
                        modifier = Modifier
                            .clickable(onClick = function)
                            .padding(end = 10.dp)
                    )
                    Text(
                        text = stringResource(R.string.dialog_delete_delete),
                        fontFamily = FontFamily(Font(R.string.dialog_delete_cancel_text)),
                        fontSize = 16.sp,
                        color = Blue,
                        modifier = Modifier
                            .clickable(onClick = deleteFun)
                            .padding(end = 10.dp)
                    )
                }
            }
        }
    }
}