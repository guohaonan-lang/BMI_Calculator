package com.example.bmicalculator.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.ui.theme.Background
import com.example.bmicalculator.ui.theme.Black
import com.example.bmicalculator.ui.theme.White
import com.example.bmicalculator.viewmodel.RecentViewModel


@Composable
fun RecentScreen(viewModel: RecentViewModel) {

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Background
            )
            .padding(top = 10.dp)
    ) {
        Title({ viewModel.process(RecentViewModel.RecentIntent.BackPage) })
        BmiRecords(uiState.value.recordUiList, { clickedRecord ->
            viewModel.process(RecentViewModel.RecentIntent.ResultPage(record = clickedRecord))
        })
    }
}

@Composable
fun Title(backPage: () -> Unit) {
    Row(
        modifier = Modifier.padding(start = 15.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.recent_back),
            contentDescription = "back",
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = backPage)
        )
        Text(
            stringResource(R.string.recent),
            fontFamily = FontFamily(Font(R.font.font_extrabold)),
            fontSize = 20.sp,
            color = Black,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
fun BmiRecords(records: List<RecentViewModel.BmiRecordUi>, resultPageClick: (BmiEntity) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .padding(top = 10.dp)
    ) {
        items(items = records) { item ->
            BmiRecord(item,resultPageClick)
        }
    }
}

@Composable
fun BmiRecord(record: RecentViewModel.BmiRecordUi, resultPageClick: (BmiEntity) -> Unit) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 7.5.dp)
            .background(
                color = White,
                shape = RoundedCornerShape(15.dp)
            )
            .clickable(onClick = { resultPageClick(record.bmiRecord) }),
    ) {
        val (bmiValue, bmiColor, bmiLevel, bmiTime, rightIv) = createRefs()
        Text(
            text = record.bmiDisplayText,
            modifier = Modifier.constrainAs(bmiValue) {
                start.linkTo(parent.start, 20.dp)
                top.linkTo(parent.top, 15.dp)
            },
            fontFamily = FontFamily(Font(R.font.font_bold_extrabold)),
            fontSize = 27.sp,
            color = Black
        )
        Box(
            modifier = Modifier
                .constrainAs(bmiColor) {
                    start.linkTo(bmiValue.start)
                    top.linkTo(bmiValue.bottom, 5.dp)
                }
                .size(14.dp)
                .clip(CircleShape)
                .background(colorResource(record.colorResId))
        )
        Text(
            text = stringResource(record.levelTextInt),
            modifier = Modifier.constrainAs(bmiLevel) {
                start.linkTo(bmiColor.end, 5.dp)
                top.linkTo(bmiValue.bottom)
                bottom.linkTo(parent.bottom, 15.dp)
            },
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 16.sp,
            color = Black
        )
        Text(
            text = "${stringResource(record.timeDisplayText.selectMonthInt)} ${record.timeDisplayText.selectDay} ${record.timeDisplayText.selectYear} ${
                stringResource(
                    record.timeDisplayText.selectPeriodInt
                )
            }",
            modifier = Modifier
                .constrainAs(bmiTime) {
                    end.linkTo(rightIv.start, 15.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .width(100.dp),
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 15.sp,
            color = Black
        )

        Image(
            painter = painterResource(R.drawable.recent_right),
            contentDescription = "right",
            modifier = Modifier.constrainAs(rightIv) {
                end.linkTo(parent.end, 15.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        )
    }
}
