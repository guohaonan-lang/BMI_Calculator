package com.example.bmicalculator.ui.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bmicalculator.R
import com.example.bmicalculator.ui.theme.Background
import com.example.bmicalculator.ui.theme.Blue
import com.example.bmicalculator.viewmodel.FeedbackViewModel


@Composable
fun FeedbackScreen(viewmodel: FeedbackViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 15.dp, top = 10.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(R.drawable.recent_back),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = { viewmodel.processIntent(FeedbackViewModel.FeedbackIntent.NavToBack) })
            )
            Text(
                stringResource(R.string.setting_feedback),
                fontFamily = FontFamily(Font(R.font.font_bold_extrabold)),
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
        var inputText by remember { mutableStateOf("") }
        TextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier
                .padding(15.dp)
                .weight(1f)
                .fillMaxWidth(),
            textStyle = TextStyle(
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 16.sp
            ),
            placeholder = { Text(text = stringResource(R.string.feedback_or_suggestion)) },
            shape = RoundedCornerShape(15.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,

                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
            )
        )
        Button(
            onClick = {
                viewmodel.processIntent(FeedbackViewModel.FeedbackIntent.CommitFeedback)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .height(60.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                Blue
            ),
        ) {
            Text(
                stringResource(R.string.result_save),
                fontFamily = FontFamily(Font(R.font.font_bold_extrabold)),
                fontSize = 20.sp
            )
        }

    }
}