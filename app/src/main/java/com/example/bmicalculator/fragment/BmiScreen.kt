package com.example.bmicalculator.fragment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.ui.DescribeText
import com.example.bmicalculator.ui.GradeList
import com.example.bmicalculator.ui.theme.Blue
import com.example.bmicalculator.ui.theme.White
import com.example.bmicalculator.util.BmiColorWheelScreen
import com.example.bmicalculator.viewmodel.BmiFragmentViewModel


@Composable
fun BmiScreen(viewModel: BmiFragmentViewModel) {

    val uiState = viewModel.status.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = White
            )
    ) {
        Title("${stringResource(uiState.value.timeMonthInt)} ${uiState.value.timeDay}, ${uiState.value.timeYear}",
            {viewModel.processIntent(intent = BmiFragmentViewModel.BmiIntent.NavToRecent)})
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f)
                .clickable(
                    onClick = { viewModel.processIntent(intent = BmiFragmentViewModel.BmiIntent.NavToInput) }
                )
        ) {
            BmiColorWheelScreen(uiState.value.age,
                uiState.value.gender,
                uiState.value.bmiValue)
            DescribeText(
                bmiValue = uiState.value.bmiValue,
                bmiLevel = stringResource(uiState.value.bmiLevelStrInt),
                weightText = uiState.value.weightStr,
                heightText = uiState.value.heightStr,
                genderText = stringResource(uiState.value.genderStrInt),
                ageText = uiState.value.age.toString(),
                buttonColor = uiState.value.buttonColor
            ) { }
            GradeList(gradeList = uiState.value.gradeList)
        }
    }
}

@Composable
fun Title(timeStr: String, navToRecent: () -> Unit) {
    Column() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp, top = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "BMI",
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                fontSize = 24.sp
            )
            Text(
                stringResource(R.string.recent),
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 16.sp,
                modifier = Modifier.clickable(onClick = navToRecent),
                color = Blue
            )
        }
        Text(
            timeStr,
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 15.dp)
        )
    }
}