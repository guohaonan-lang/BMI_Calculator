package com.example.bmicalculator.ui.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.ui.theme.Background
import com.example.bmicalculator.ui.theme.Black
import com.example.bmicalculator.ui.theme.White
import com.example.bmicalculator.util.LangHelper
import com.example.bmicalculator.viewmodel.LanguageViewModel


@Composable
fun LanguageScreen(viewModel: LanguageViewModel) {

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    val ctx = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.processIntent(LanguageViewModel.LanguageIntent.LoadLanguage(ctx))
    }

    Column(
        modifier = Modifier
            .background(Background)
            .padding(top = 10.dp, start = 15.dp, end = 15.dp)
    ) {
        LanguageTitle{ viewModel.processIntent(LanguageViewModel.LanguageIntent.NavToBack) }
        Column(
            modifier = Modifier
                .padding(top = 30.dp)
                .background(
                    color = White,
                    shape = RoundedCornerShape(15.dp)
                )
                .padding(vertical = 10.dp)
        ) {
            LanguageItem(
                "Chinese",
                uiState.value == LangHelper.LANG_ZH
            ){ viewModel.processIntent(LanguageViewModel.LanguageIntent.SwitchChinese) }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 15.dp)
                    .background(
                        color = Black.copy(alpha = 0.2f)
                    )
            )
            LanguageItem(
                "English",
                uiState.value == LangHelper.LANG_EN
            ){ viewModel.processIntent(LanguageViewModel.LanguageIntent.SwitchEnglish) }
        }
    }
}

@Composable
fun LanguageTitle(function: () -> Unit) {
    Row {
        Image(
            painter = painterResource(R.drawable.recent_back),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = function)
        )
        Text(
            stringResource(R.string.language_options),
            fontFamily = FontFamily(Font(R.font.font_bold_extrabold)),
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}


@Composable
fun LanguageItem(languageName: String, isSelect: Boolean, switchLanguage: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 15.dp)
            .clickable(onClick = switchLanguage),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            languageName,
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 16.sp,
        )
        if (isSelect) Image(
            painter = painterResource(R.drawable.check),
            contentDescription = null,
        )
    }
}