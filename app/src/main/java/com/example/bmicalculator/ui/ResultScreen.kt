package com.example.bmicalculator.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.bmicalculator.R
import com.example.bmicalculator.model.Grade
import com.example.bmicalculator.ui.theme.Background
import com.example.bmicalculator.ui.theme.Black
import com.example.bmicalculator.ui.theme.Grad4
import com.example.bmicalculator.ui.theme.Gray
import com.example.bmicalculator.ui.theme.Red
import com.example.bmicalculator.ui.theme.White
import com.example.bmicalculator.util.BmiColorWheelView

@Preview(showBackground = true, widthDp = 441, heightDp = 891)
@Composable
fun PreResult() {
    ResultScreen()
}

@Composable
fun ResultScreen(modifier: Modifier = Modifier) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        ResultTitle(false)
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            ColorWheel(25, 1, 22f)
            DescribeText(bmiValue = 24.45f)
            GradeList()
            ResultAssessment()
            TimeLine()
            AdText()
        }

    }
}

@Composable
fun ResultTitle(isCalResult: Boolean) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, top = 15.dp, end = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (!isCalResult) {
            Image(
                painter = painterResource(R.drawable.recent_back),
                contentDescription = "back",
                modifier = Modifier.size(24.dp),
            )
            Text(
                stringResource(R.string.dialog_delete_delete),
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 16.sp,
                color = Black
            )
        } else {
            Text(
                stringResource(R.string.result_discard),
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 16.sp,
                color = Black
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
fun DescribeText(modifier: Modifier = Modifier, bmiValue: Float) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Your BMI is...",
            fontFamily = FontFamily(Font(R.font.font_extrabold)),
            fontSize = 18.sp
        )
        Text(
            String.format("%.2f", bmiValue),
            fontFamily = FontFamily(Font(R.font.font_extrabold)),
            fontSize = 64.sp
        )
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(Grad4)
        ) {
            Text(
                text = "normal",
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
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "1403" + " | ",
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 14.sp
            )
            Text(
                "435cm" + " | ",
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 14.sp
            )
            Text(
                "Man" + " | ",
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 14.sp
            )
            Text(
                "53",
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun GradeList(
    modifier: Modifier = Modifier,
    gradeList: List<Grade> = emptyList()
) {
    for (item in gradeList) {
        GradeItem(grade = item)
    }
}

@Composable
fun GradeItem(
    modifier: Modifier = Modifier,
    grade: Grade
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧颜色圆点
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(grade.color))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(text = grade.gradeName)
            Text(
                text = grade.gradeRange,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ResultAssessment(

) {
    Column(
        modifier = Modifier
            .padding(horizontal = 30.dp, vertical = 10.dp)
            .background(Background, RoundedCornerShape(10.dp))
            .padding(horizontal = 15.dp, vertical = 15.dp)
    ) {
        Text(
            "\uD83D\uDE0E Congratulations! You’re in a great place now. Keep up your healthy habits to maintain your healthy weight.",
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 14.sp,
            color = Black
        )
        Text(
            "Normal Weight for your height (180cm):",
            modifier = Modifier.padding(top = 20.dp),
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 14.sp,
            color = Black
        )
        Row(
            modifier = Modifier.padding(top = 3.dp)
        ) {
            Text(
                "13.4 kg - 31.3 kg",
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                fontSize = 14.sp,
                color = Black
            )
            Text(
                "(+24kg)",
                modifier = Modifier.padding(start = 5.dp),
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                fontSize = 14.sp,
                color = Red
            )
        }
    }
}

@Composable
fun TimeLine(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
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
            text = "时间占位",
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
fun AdText(modifier: Modifier = Modifier) {
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
                .size(49.dp)
                .padding(start = 15.dp)
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
                .size(49.dp)
                .padding(start = 15.dp)
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