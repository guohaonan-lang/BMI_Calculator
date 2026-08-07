package com.example.bmicalculator.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.ui.theme.Background
import com.example.bmicalculator.ui.theme.Black
import com.example.bmicalculator.ui.theme.Blue
import com.example.bmicalculator.ui.theme.GobleColor
import com.example.bmicalculator.ui.theme.Grad2
import com.example.bmicalculator.ui.theme.Red
import com.example.bmicalculator.ui.theme.Setting2Color
import com.example.bmicalculator.ui.theme.White
import com.example.bmicalculator.ui.theme.switchTrackColor
import com.example.bmicalculator.viewmodel.SettingViewModel


@Composable
fun SettingScreen(viewModel: SettingViewModel) {

    val uiState = viewModel.state.collectAsStateWithLifecycle()

    var userLoadBottom by remember { mutableStateOf(false) }
    var autoDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Background
            )
            .padding(horizontal = 15.dp)
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        SettingTitle(backClick = { viewModel.processIntent(SettingViewModel.SettingIntent.NavToBack) })
        UserMessage(
            uiState = uiState,
            userBottomDialog = { userLoadBottom = true },
            autoDataDialog = {
                autoDialog = true
                viewModel.processIntent(SettingViewModel.SettingIntent.ReadTestData(context))
            }
        )
        Setting1(viewModel)
        Setting2(viewModel)

        Text(
            "Version 1.0.0",
            fontSize = 14.sp,
            color = Black,
            fontFamily = FontFamily(Font(R.font.font_regular)),
            modifier = Modifier
                .padding(vertical = 15.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
    UserBottomDialog(
        loading = uiState.value.userLoading,
        bottomStatus = userLoadBottom,
        loadClick = {
            viewModel.processIntent(SettingViewModel.SettingIntent.UserLoad)
            userLoadBottom = false
        },
        unLoadClick = {
            viewModel.processIntent(SettingViewModel.SettingIntent.UserUnload)
            userLoadBottom = false
        },
        cancelClick = { userLoadBottom = false }
    )
    AutoDataDialog(
        autoShow = autoDialog,
        cancelClick = { autoDialog = false }
    )
}

@Composable
fun SettingTitle(backClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(R.drawable.recent_back),
            contentDescription = "back",
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = backClick),
        )
        Text(
            text = stringResource(R.string.setting_me),
            fontFamily = FontFamily(Font(R.font.font_extrabold)),
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
fun UserMessage(
    uiState: State<SettingViewModel.SettingState>,
    userBottomDialog: () -> Unit,
    autoDataDialog: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp)
            .background(
                color = White,
                shape = RoundedCornerShape(15.dp)
            )
            .clickable(onClick = userBottomDialog)
    ) {
        val (userImage, userName, userEmail, userAutoRenew, userGoogle) = createRefs()

        if (uiState.value.userLoading) {
            Image(
                painter = painterResource(R.drawable.user),
                contentDescription = "user",
                modifier = Modifier
                    .size(65.dp)
                    .constrainAs(userImage) {
                        start.linkTo(parent.start, 10.dp)
                        top.linkTo(parent.top, 10.dp)
                        bottom.linkTo(parent.bottom, 10.dp)
                    }
                    .clip(CircleShape)
            )
        }

        Text(
            text = if (uiState.value.userLoading) uiState.value.userName else stringResource(R.string.setting_backup_restore),
            fontFamily = FontFamily(Font(R.font.font_extrabold)),
            fontSize = 16.sp,
            modifier = Modifier.constrainAs(userName) {
                if (uiState.value.userLoading) {
                    start.linkTo(userImage.end, 15.dp)
                    top.linkTo(userImage.top, 5.dp)
                } else {
                    start.linkTo(parent.start, 15.dp)
                    top.linkTo(parent.top, 15.dp)
                }
            }
        )
        Text(
            text = if (uiState.value.userLoading) uiState.value.userEmail else stringResource(R.string.setting_synchronize_your_data),
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 14.sp,
            modifier = Modifier.constrainAs(userEmail) {
                start.linkTo(userName.start)
                top.linkTo(userName.bottom, 5.dp)
                bottom.linkTo(parent.bottom, 20.dp)
            }
        )
        Image(
            painter = painterResource(R.drawable.setting_user_google),
            contentDescription = "google",
            modifier = Modifier.constrainAs(userGoogle) {
                start.linkTo(userName.end, 5.dp)
                top.linkTo(userName.top)
            }
        )
        Image(
            painter = painterResource(R.drawable.ic_autorenew_black),
            contentDescription = "autorenew",
            modifier = Modifier
                .constrainAs(userAutoRenew) {
                    end.linkTo(parent.end, 15.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .clickable(onClick = autoDataDialog)
        )
    }
}

@Composable
fun Setting1(viewModel: SettingViewModel) {
    Column(
        modifier = Modifier.background(
            color = White,
            shape = RoundedCornerShape(15.dp)
        )
    ) {
        SettingMessage(
            backgroundColor = GobleColor,
            ivInt = R.drawable.globe,
            textInt = R.string.setting_language,
            switch = false,
            click = { viewModel.processIntent(SettingViewModel.SettingIntent.NavToLanguage) }
        )
        SepLine()
        SettingMessage(
            textInt = R.string.setting_connect_to_google_fit,
            ivInt = R.drawable.bitmap,
            backgroundColor = White,
            switch = true,
        )
    }

}

@Composable
fun Setting2(viewModel: SettingViewModel) {
    Column(
        modifier = Modifier.background(
            color = White,
            shape = RoundedCornerShape(15.dp)
        )
    ) {

        SettingMessage(
            textInt = R.string.setting_remove_ads,
            ivInt = R.drawable.setting_ad,
            backgroundColor = Setting2Color,
            switch = false,
        )
        SepLine()
        SettingMessage(
            textInt = R.string.setting_rate_us,
            ivInt = R.drawable.setting_star,
            backgroundColor = Setting2Color,
            switch = false,
        )
        SepLine()
        SettingMessage(
            textInt = R.string.setting_feedback,
            ivInt = R.drawable.setting_feedback,
            backgroundColor = Setting2Color,
            switch = false,
            click = { viewModel.processIntent(SettingViewModel.SettingIntent.NavToFeedback) }
        )
        SepLine()
        SettingMessage(
            textInt = R.string.setting_privacy_policy,
            ivInt = R.drawable.setting_privacy,
            backgroundColor = Setting2Color,
            switch = false,
        )
    }
}


// 设置子项模版
@Composable
fun SettingMessage(
    textInt: Int,
    ivInt: Int,
    backgroundColor: Color,
    switch: Boolean,
    click: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 15.dp)
            .wrapContentHeight()
            .clickable(onClick = click),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(ivInt),
            contentDescription = "language",
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(5.dp)
                )
                .padding(6.dp)
        )
        Text(
            text = stringResource(textInt),
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 15.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        if (switch) {
            var checked by rememberSaveable { mutableStateOf(false) }

            Switch(
                checked = checked,
                onCheckedChange = { checked = it },
                Modifier
                    .heightIn(10.dp)
                    .scale(0.7f),
                thumbContent = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Grad2,
                    checkedTrackColor = switchTrackColor,
                    uncheckedThumbColor = backgroundColor,
                    uncheckedTrackColor = Background,
                )
            )
        }

    }
}

@Composable
fun SepLine() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 62.dp, end = 15.dp)
                .background(
                    color = Black.copy(alpha = 0.2f)
                )
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserBottomDialog(
    loadClick: () -> Unit,
    bottomStatus: Boolean,
    cancelClick: () -> Unit,
    loading: Boolean,
    unLoadClick: () -> Unit
) {

    if (!bottomStatus) return
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = cancelClick,
        modifier = Modifier.fillMaxWidth(),
        sheetState = bottomSheetState,
        shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
        containerColor = Background,
    ) {
        ConstraintLayout(

        ) {
            val (userIv, userName, userEmail, googleIv, closeIv, logButton, cancelButton) = createRefs()
            Image(
                painter = painterResource(R.drawable.user),
                contentDescription = null,
                modifier = Modifier
                    .size(65.dp)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .constrainAs(userIv) {
                        start.linkTo(parent.start, 15.dp)
                        top.linkTo(parent.top, 15.dp)
                    },

                )
            Text(
                text = "Cassie",
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                fontSize = 16.sp,
                color = Black,
                modifier = Modifier.constrainAs(userName) {
                    start.linkTo(userIv.end, 10.dp)
                    top.linkTo(userIv.top, 5.dp)
                }
            )
            Text(
                text = "cassiexiao@gmail.com",
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 16.sp,
                color = Black.copy(alpha = 0.8f),
                modifier = Modifier.constrainAs(userEmail) {
                    start.linkTo(userIv.end, 10.dp)
                    top.linkTo(userName.bottom, 5.dp)
                }
            )
            Image(
                painter = painterResource(R.drawable.setting_user_google),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(googleIv) {
                        start.linkTo(userName.end, 5.dp)
                        top.linkTo(userName.top)
                    }
            )
            Image(
                painter = painterResource(R.drawable.x),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .constrainAs(closeIv) {
                        end.linkTo(parent.end, 15.dp)
                        top.linkTo(parent.top, 15.dp)
                    }
                    .clickable(onClick = cancelClick)
            )

            Button(
                onClick = {
                    if (loading) unLoadClick() else loadClick()
                },
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .fillMaxWidth()
                    .height(65.dp)
                    .constrainAs(logButton) {
                        top.linkTo(userIv.bottom, 20.dp)
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = White,
                    contentColor = Black,
                ),
            ) {
                Text(
                    if (loading) stringResource(R.string.log_out) else stringResource(
                        R.string.log_in
                    ),
                    fontFamily = FontFamily(Font(R.font.font_extrabold)),
                    fontSize = 20.sp,
                    color = if (loading) Red else Black
                )
            }
            Button(
                onClick = cancelClick,
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .fillMaxWidth()
                    .height(65.dp)
                    .constrainAs(cancelButton) {
                        top.linkTo(logButton.bottom, 20.dp)
                        bottom.linkTo(parent.bottom, 20.dp)
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = White,
                    contentColor = Black,
                ),
            ) {
                Text(
                    stringResource(R.string.dialog_delete_cancel_text),
                    fontFamily = FontFamily(Font(R.font.font_extrabold)),
                    fontSize = 20.sp
                )
            }

        }
    }
}


@Composable
fun AutoDataDialog(autoShow: Boolean, cancelClick: () -> Unit) {

    if (!autoShow) return


    Dialog(
        onDismissRequest = cancelClick,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(15.dp),
            color = White,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(15.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.sorry),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )
                Text(
                    stringResource(R.string.sorry_for_the_inconvenience),
                    fontFamily = FontFamily(Font(R.font.font_extrabold)),
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 0.dp)
                )
                Text(
                    stringResource(R.string.auto_text1),
                    fontFamily = FontFamily(Font(R.font.font_regular)),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(30.dp)
                )
                Text(
                    stringResource(R.string.auto_text2),
                    fontFamily = FontFamily(Font(R.font.font_regular)),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 30.dp)
                )
                Button(
                    onClick = cancelClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(Blue)
                ) {
                    Text(
                        stringResource(R.string.data_done),
                        fontFamily = FontFamily(Font(R.font.font_extrabold)),
                        fontSize = 20.sp
                    )
                }

            }
        }
    }
}