package com.example.bmicalculator.fragment

import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter
import com.contrarywind.view.WheelView
import com.example.bmicalculator.R
import com.example.bmicalculator.ui.ResultActivity
import com.example.bmicalculator.ui.SettingActivity
import com.example.bmicalculator.ui.theme.BMIComposeTheme
import com.example.bmicalculator.ui.theme.Background
import com.example.bmicalculator.ui.theme.Black
import com.example.bmicalculator.ui.theme.Blue
import com.example.bmicalculator.ui.theme.Gray
import com.example.bmicalculator.ui.theme.White
import com.example.bmicalculator.util.BmiUtil
import com.example.bmicalculator.util.TimeUtil
import com.example.bmicalculator.viewmodel.InputViewModel
import com.example.bmicalculator.viewmodel.InputViewModel.DataInputIntent
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs


@Preview(widthDp = 441, heightDp = 891, showBackground = true)
@Composable
fun PreviewInputScreen() {
    BMIComposeTheme {
//        InputScreen()
    }
}

@Composable
fun InputScreen(
    viewModel: InputViewModel,
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(Background),
) {
    val context = LocalContext.current

    val uiState = viewModel.state.collectAsStateWithLifecycle()
    val eventFlow = viewModel.event
    var showDateBottomSheet by remember { mutableStateOf(false) }
    var showDate2BottomSheet by remember { mutableStateOf(false) }


    val focusManager = LocalFocusManager.current
    val keyboardCtrl = LocalSoftwareKeyboardController.current

    LaunchedEffect(uiState.value.customTime) {
        val timeStr = TimeUtil().parseTimeStamp(uiState.value.customTime)
        viewModel.processIntent(
            DataInputIntent.SetTime1(
                timeStr.selectYear,
                timeStr.selectMonthInt,
                timeStr.selectDay
            )
        )
        viewModel.processIntent(DataInputIntent.SetTime2(timeStr.selectPeriodInt))
    }

    LaunchedEffect(Unit) {
        eventFlow.collect { event ->
            when (event) {
                is InputViewModel.CheckEvent.ShowToast ->
                    event.msgResId?.let { id ->
                        val str = context.getString(id)
                        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
                    }

                is InputViewModel.CheckEvent.NavToResult -> {
                    val intent = Intent(context, ResultActivity::class.java)
                    intent.putExtra("BMI", event.bmiEntity)
                    intent.putExtra("FATHER", event.isFirst)
                    context.startActivity(intent)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures {
                    // 主动清除所有焦点、收起键盘
                    focusManager.clearFocus()
                    keyboardCtrl?.hide()
                    viewModel.processIntent(DataInputIntent.CheckInputValid)
                }
            }
            .verticalScroll(rememberScrollState())

    ) {
        // 标题，个人页面跳转
        TitleText(context)

        // 身高，体重，输入
        WeightAndHeightInput(viewModel, uiState)

        // weight单位转换
        UnitSwitch(viewModel, uiState)

        // 时间选择
        TimeSelect(
            { showDateBottomSheet = true },
            { showDate2BottomSheet = true },
            uiState
        )

        // 年龄选择
        AgeHorizontalPicker(
            25,
            { age ->
                viewModel.processIntent(DataInputIntent.SetAge(age))

            }
        )

        // 性别选择
        genderSelect(viewModel, uiState)

        // 跳转计算结果页
        Button(
            onClick = {
                val bmiLevel =
                    BmiUtil.getBmiFullInfo(
                        uiState.value.age,
                        uiState.value.gender,
                        uiState.value.bmiValue
                    )
                val bmiColor = ContextCompat.getColor(context, bmiLevel.colorInt)
                val custime = TimeUtil().getCustomTimeStamp(
                    uiState.value.timeYear,
                    uiState.value.timeMonthInt,
                    uiState.value.timeDay,
                    uiState.value.timePeriodInt,
                )
                viewModel.processIntent(DataInputIntent.ComputeFullBmi(bmiColor, custime))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp, start = 20.dp, end = 20.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(Blue)
        ) {
            Text(
                stringResource(R.string.input_calculate),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                color = White
            )
        }
    }
    // 时间选择窗口
    DatePickerBottomSheet(
        show = showDateBottomSheet,
        onDismiss = {
            // 关闭弹窗：重置状态
            showDateBottomSheet = false
        },
        onConfirm = { year, month, day ->
            // 选中日期回调逻辑
            viewModel.processIntent(DataInputIntent.SetTime1(year, month, day))
        }
    )
    PeriodPickerBottomSheet(
        show = showDate2BottomSheet,
        onDismiss = {
            // 关闭弹窗：重置状态
            showDate2BottomSheet = false
        },
        onConfirm = { selectPeriod ->
            viewModel.processIntent(DataInputIntent.SetTime2(selectPeriod))
        }
    )
}

@Composable
fun TitleText(context: Context) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            stringResource(R.string.title_calculate),
            fontFamily = FontFamily(Font(R.font.font_extrabold)),
            fontSize = 24.sp,
            color = Black,
            modifier = Modifier.padding(start = 15.dp, top = 18.dp)
        )
        Image(
            painter = painterResource(R.drawable.settings_user),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 15.dp, top = 18.dp)
                .padding(end = 15.dp)
                .size(30.dp)
                .clickable(
                    onClick = {
                        val intent = Intent(context, SettingActivity::class.java)
                        context.startActivity(intent)
                    }
                )
        )
    }
}


@Composable
fun WeightAndHeightInput(viewModel: InputViewModel, uiState: State<InputViewModel.UserListState>) {
    // 身高，体重-标题
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        Text(
            stringResource(R.string.input_weight),
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 14.sp,
        )
        Text(
            stringResource(R.string.input_height),
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 14.sp,
        )
    }
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        InputText(
            value = uiState.value.weight,
            valueBack = { text ->
                // 1. 只保留数字与小数点
                var temp = text.replace(Regex("[^0-9.]"), "")
                // 2. 多个小数点：只保留第一个
                if (temp.count { it == '.' } > 1) {
                    temp =
                        temp.substringBefore('.') + "." + temp.substring(temp.indexOf('.') + 1)
                            .replace(".", "")
                }
                // 3. 如果存在小数点，截断小数部分，最多保留2位
                val dotPosition = temp.indexOf('.')
                if (dotPosition != -1) {
                    val integerPart = temp.substring(0, dotPosition)
                    val decimalPart = temp.substring(dotPosition + 1).take(2)
                    temp = "$integerPart.$decimalPart"
                }
                viewModel.processIntent(DataInputIntent.SetWeight(temp))
            },
            modifier = Modifier
                .weight(1f)
                .height(65.dp)
                .padding(
                    start = 20.dp,
                    end = 8.dp
                )
                .background(
                    color = White,
                    shape = RoundedCornerShape(15.dp)
                ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),

            )
        if (uiState.value.heightUnit) {
            InputText(
                value = uiState.value.height,
                valueBack = { text ->
                    // 1. 只保留数字与小数点
                    var temp = text.replace(Regex("[^0-9.]"), "")
                    // 2. 多个小数点：只保留第一个
                    if (temp.count { it == '.' } > 1) {
                        temp =
                            temp.substringBefore('.') + "." + temp.substring(temp.indexOf('.') + 1)
                                .replace(".", "")
                    }
                    // 3. 如果存在小数点，截断小数部分，最多保留2位
                    val dotPosition = temp.indexOf('.')
                    if (dotPosition != -1) {
                        val integerPart = temp.substring(0, dotPosition)
                        val decimalPart = temp.substring(dotPosition + 1).take(2)
                        temp = "$integerPart.$decimalPart"
                    }
                    viewModel.processIntent(DataInputIntent.SetHeight(temp))
                },
                modifier = Modifier
                    .weight(1f)
                    .height(65.dp)
                    .padding(
                        start = 8.dp,
                        end = 20.dp,
                    )
                    .background(
                        color = White,
                        shape = RoundedCornerShape(15.dp)
                    ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        } else {
            Row(
                modifier = Modifier
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                InputText(
                    value = uiState.value.heightFt + "'",
                    valueBack = { rawText ->
                        val text = rawText.filter { it.isDigit() }.take(1)
                        viewModel.processIntent(DataInputIntent.SetHeightFt(text))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(65.dp)
                        .padding(
                            start = 8.dp,
                            end = 5.dp,
                        )
                        .background(
                            color = White,
                            shape = RoundedCornerShape(15.dp)
                        ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                InputText(
                    value = uiState.value.heightIn.toString() + "\"",
                    valueBack = { rawText ->
                        val text = rawText.filter { it.isDigit() }.take(2)
                        viewModel.processIntent(DataInputIntent.SetHeightIn(text))
                    },
                    modifier = Modifier
                        .weight(1.4f)
                        .height(65.dp)
                        .padding(
                            start = 5.dp,
                            end = 20.dp,
                        )
                        .background(
                            color = White,
                            shape = RoundedCornerShape(15.dp)
                        ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        }
    }
}

@Composable
fun InputText(
    modifier: Modifier = Modifier,
    value: String,
    valueBack: (String) -> Unit,
    keyboardOptions: KeyboardOptions
) {
    BasicTextField(
        value = value,
        onValueChange = { text ->
            valueBack(text)
        },
        modifier = modifier,
        textStyle = TextStyle(
            color = Black,
            fontFamily = FontFamily(Font(R.font.montserrat_extrabold)),
            fontSize = 28.sp,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = keyboardOptions,
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                innerTextField()
            }
        },
    )
}

@Composable
fun UnitSwitch(viewModel: InputViewModel, uiState: State<InputViewModel.UserListState>) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = 20.dp,
                    end = 8.dp,
                )
                .fillMaxWidth()
                .height(36.dp)
                .background(
                    color = Gray,
                    shape = RoundedCornerShape(36.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(15.dp))
                    .clickable {
                        viewModel.processIntent(DataInputIntent.SwitchWeightUnitToLb)
                    }
                    .background(
                        color = if (!uiState.value.weightUnit) White else Gray,
                    )
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "lb",
                    fontFamily = FontFamily(Font(R.font.font_extrabold)),
                    modifier = Modifier.alpha(if (!uiState.value.weightUnit) 1f else 0.3f)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(15.dp))
                    .clickable {
                        viewModel.processIntent(DataInputIntent.SwitchWeightUnitToKg)
                    }
                    .background(
                        color = if (uiState.value.weightUnit) White else Gray,
                    )
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "kg",
                    fontFamily = FontFamily(Font(R.font.font_extrabold)),
                    modifier = Modifier.alpha(if (uiState.value.weightUnit) 1f else 0.3f)
                )
            }
        }
        // height单位转换
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = 8.dp,
                    end = 20.dp,
                )
                .fillMaxWidth()
                .height(36.dp)
                .background(
                    color = Gray,
                    shape = RoundedCornerShape(36.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(15.dp))
                    .clickable {
                        viewModel.processIntent(DataInputIntent.SwitchHeightUnitToFtIn)
                    }
                    .background(
                        color = if (!uiState.value.heightUnit) White else Gray,
                    )
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "ft·in",
                    fontFamily = FontFamily(Font(R.font.font_extrabold)),
                    modifier = Modifier.alpha(if (!uiState.value.heightUnit) 1f else 0.3f)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(15.dp))
                    .clickable {
                        viewModel.processIntent(DataInputIntent.SwitchHeightUnitToCm)
                    }
                    .background(
                        color = if (uiState.value.heightUnit) White else Gray,
                    )
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "cm",
                    fontFamily = FontFamily(Font(R.font.font_extrabold)),
                    modifier = Modifier.alpha(if (uiState.value.heightUnit) 1f else 0.3f)
                )
            }
        }
    }
}

@Composable
fun TimeSelect(
    onFirstBoxClick: () -> Unit,
    onSecondBoxClick: () -> Unit,
    uiState: State<InputViewModel.UserListState>
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        Text(
            stringResource(R.string.input_time),
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 14.sp,
            color = Black,
        )
    }

    Row(
        modifier = Modifier
            .padding(
                top = 30.dp
            )
            .height(60.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                .padding(start = 20.dp, end = 8.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(White)
                .clickable(onClick = onFirstBoxClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${stringResource(uiState.value.timeMonthInt)} ${uiState.value.timeDay}, ${uiState.value.timeYear}",
                color = Black,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                softWrap = false,
                maxLines = 1,

                )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                .padding(start = 8.dp, end = 20.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(White)
                .clickable(onClick = onSecondBoxClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(uiState.value.timePeriodInt),
                color = Black,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                softWrap = false,
                maxLines = 1
            )
        }
    }
}

/**
 * 日期三滚轮 BottomSheet
 * @param show 是否显示弹窗
 * @param onDismiss 关闭回调
 * @param onConfirm 确认选中日期回调 (month,day,year)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerBottomSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    if (!show) return

    val wheelMonthRef = remember { mutableStateOf<WheelView?>(null) }
    val wheelDayRef = remember { mutableStateOf<WheelView?>(null) }
    val wheelYearRef = remember { mutableStateOf<WheelView?>(null) }

    // 基础数据源（和旧代码保持一致）
    val monthArrayInt = listOf(
        R.string.jan,
        R.string.feb,
        R.string.mar,
        R.string.apr,
        R.string.may,
        R.string.june,
        R.string.july,
        R.string.aug,
        R.string.sep,
        R.string.oct,
        R.string.nov,
        R.string.dec
    )
    val monthArray = stringArrayResource(id = R.array.month_short_names)
    val monthData = remember(monthArray) {
        monthArray.toList()
    }
    val yearData = remember {
        (1970..2036).map { it.toString() }
    }

    // 初始选中日期
    val initCalendar = remember { Calendar.getInstance() }
    val initYearIdx = initCalendar.get(Calendar.YEAR) - 1970
    val initMonthIdx = initCalendar.get(Calendar.MONTH)
    val initDayIdx = initCalendar.get(Calendar.DAY_OF_MONTH) - 1

    // 通用方法：根据年下标、月下标获取当月天数列表（照搬原有逻辑）
    fun getDayList(yearPos: Int, monthPos: Int): MutableList<String> {
        val targetYear = yearData[yearPos].toInt()
        val targetMonth = monthPos
        val calendar = Calendar.getInstance()
        calendar.set(targetYear, targetMonth, 1)
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (1..maxDay).map { it.toString() }.toMutableList()
    }

    // 刷新日期滚轮统一函数（年月切换时调用）
    fun refreshDayWheel() {
        val wheelYear = wheelYearRef.value ?: return
        val wheelMonth = wheelMonthRef.value ?: return
        val wheelDay = wheelDayRef.value ?: return

        val yearCur = wheelYear.currentItem
        val monthCur = wheelMonth.currentItem
        val newDayList = getDayList(yearCur, monthCur)

        // 选中值越界修正
        if (wheelDay.currentItem >= newDayList.size) {
            wheelDay.currentItem = newDayList.size - 1
        }
        wheelDay.adapter = ArrayWheelAdapter(newDayList)
        wheelDay.invalidate()
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
            // 弹窗关闭释放引用，防止内存泄漏
            wheelMonthRef.value = null
            wheelDayRef.value = null
            wheelYearRef.value = null
        },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = White,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 16.dp)
        ) {
            // 标题
            Text(
                stringResource(R.string.date_picker_date),
                fontSize = 28.sp,
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                color = Black,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // 三栏滚轮 Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // ========== 月份滚轮 ==========
                AndroidView(
                    factory = { context ->
                        val boldTypeface = ResourcesCompat.getFont(context, R.font.font_extrabold)
                        WheelView(context).apply {
                            adapter = ArrayWheelAdapter(monthData)
                            currentItem = initMonthIdx
                            setTypeface(boldTypeface)
                            setCyclic(false)
                            setLineSpacingMultiplier(2f)
                            setAlphaGradient(true)
                            setTextSize(16f)

                            setDividerColor(android.graphics.Color.LTGRAY)
                            setTextColorCenter(android.graphics.Color.BLACK)

                            setOnTouchListener { v, event ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                        v.parent?.requestDisallowInterceptTouchEvent(true)
                                    }

                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                        v.parent?.requestDisallowInterceptTouchEvent(false)
                                        v.performClick()
                                    }
                                }
                                false
                            }


                            // 月份切换监听 → 刷新日期滚轮
                            setOnItemSelectedListener {
                                refreshDayWheel()
                            }
                        }
                    },
                    update = { wheel ->
                        wheelMonthRef.value = wheel
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                // ========== 日期滚轮 ==========
                AndroidView(
                    factory = { context ->
                        val boldTypeface = ResourcesCompat.getFont(context, R.font.font_extrabold)
                        val initDays = getDayList(initYearIdx, initMonthIdx)
                        WheelView(context).apply {
                            adapter = ArrayWheelAdapter(initDays)
                            currentItem = initDayIdx.coerceAtMost(initDays.size - 1)
                            setTypeface(boldTypeface)
                            setCyclic(false)
                            setLineSpacingMultiplier(2f)
                            setAlphaGradient(true)
                            setTextSize(16f)

                            setDividerColor(android.graphics.Color.LTGRAY)
                            setTextColorCenter(android.graphics.Color.BLACK)

                            setOnTouchListener { v, event ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                        v.parent?.requestDisallowInterceptTouchEvent(true)
                                    }

                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                        v.parent?.requestDisallowInterceptTouchEvent(false)
                                        v.performClick()
                                    }
                                }
                                false
                            }
                        }
                    },
                    update = { wheel ->
                        wheelDayRef.value = wheel
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                // ========== 年份滚轮 ==========
                AndroidView(
                    factory = { context ->
                        val boldTypeface = ResourcesCompat.getFont(context, R.font.font_extrabold)
                        WheelView(context).apply {
                            adapter = ArrayWheelAdapter(yearData)
                            currentItem = initYearIdx
                            setTypeface(boldTypeface)
                            setCyclic(false)
                            setLineSpacingMultiplier(2f)
                            setAlphaGradient(true)
                            setTextSize(16f)

                            setDividerColor(android.graphics.Color.LTGRAY)
                            setTextColorCenter(android.graphics.Color.BLACK)

                            setOnTouchListener { v, event ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                        v.parent?.requestDisallowInterceptTouchEvent(true)
                                    }

                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                        v.parent?.requestDisallowInterceptTouchEvent(false)
                                        v.performClick()
                                    }
                                }
                                false
                            }

                            // 年份切换监听 → 刷新日期滚轮（处理闰年）
                            setOnItemSelectedListener {
                                refreshDayWheel()
                            }
                        }
                    },
                    update = { wheel ->
                        wheelYearRef.value = wheel
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }

            // 底部按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.size(120.dp, 56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gray),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        stringResource(R.string.data_cancel),
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.font_extrabold)),
                        color = Black
                    )
                }

                Button(
                    onClick = {
                        // 在这里读取滚轮选中值，向上回调
                        val wYear = wheelYearRef.value ?: return@Button
                        val wMonth = wheelMonthRef.value ?: return@Button
                        val wDay = wheelDayRef.value ?: return@Button

                        val selYear = yearData[wYear.currentItem]
                        val selMonth = monthArrayInt[wMonth.currentItem]
                        val selDay =
                            getDayList(wYear.currentItem, wMonth.currentItem)[wDay.currentItem]
                        onConfirm(selYear, selMonth, selDay)
                        onDismiss()
                    },
                    modifier = Modifier.size(120.dp, 56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2962FF)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        stringResource(R.string.data_done),
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.font_extrabold)),
                        color = White
                    )
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodPickerBottomSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    if (!show) return

    val wheelRef = remember { mutableStateOf<WheelView?>(null) }

    // 时段数据源
    val periodListInt =
        listOf(
            R.string.morning,
            R.string.afternoon,
            R.string.evening,
            R.string.night
        )
    // 时段数据源（多语言）
    val periodList =
        listOf(
            stringResource(R.string.morning),
            stringResource(R.string.afternoon),
            stringResource(R.string.evening),
            stringResource(R.string.night)
        )
    val periodData = remember {
        periodList
    }

    // 根据当前小时自动计算默认选中下标
    val defaultSelectIndex = remember {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 6..11 -> 0
            in 12..17 -> 1
            in 18..22 -> 2
            else -> 3
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
            wheelRef.value = null
        },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = White,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 16.dp)
        ) {
            Text(
                stringResource(R.string.date_picker_date), // 你自行替换对应标题string
                fontSize = 28.sp,
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                color = Black,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // 单列滚轮区域
            AndroidView(
                factory = { context ->
                    val boldTypeface = ResourcesCompat.getFont(context, R.font.font_bold_extrabold)
                    WheelView(context).apply {
                        adapter = ArrayWheelAdapter(periodData)
                        currentItem = defaultSelectIndex
                        setTypeface(boldTypeface)
                        setCyclic(false)
                        setLineSpacingMultiplier(2f)
                        setAlphaGradient(true)
                        setTextSize(16f)
                        setDividerColor(android.graphics.Color.LTGRAY)
                        setTextColorCenter(android.graphics.Color.BLACK)

                        // 触摸拦截，解决BottomSheet滑动冲突（和你原始逻辑完全一致）
                        setOnTouchListener { v, event ->
                            when (event.action) {
                                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                    v.parent?.requestDisallowInterceptTouchEvent(true)
                                }

                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                    v.parent?.requestDisallowInterceptTouchEvent(false)
                                    v.performClick()
                                }
                            }
                            false
                        }
                    }
                },
                update = { wheel ->
                    wheelRef.value = wheel
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            // 底部按钮栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.size(120.dp, 56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gray),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        stringResource(R.string.data_cancel),
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.font_extrabold)),
                        color = Black
                    )
                }

                Button(
                    onClick = {
                        val wheel = wheelRef.value ?: return@Button
                        val selectedInt = periodListInt[wheel.currentItem]
                        onConfirm(selectedInt)
                        onDismiss()
                    },
                    modifier = Modifier.size(120.dp, 56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2962FF)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        stringResource(R.string.data_done),
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.font_extrabold)),
                        color = White
                    )
                }
            }
        }
    }
}


@Composable
fun AgeHorizontalPicker(
    initSelectedIndex: Int,
    onSelectChanged: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        Text(
            stringResource(R.string.input_age),
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 14.sp,
            modifier = Modifier
                .padding(top = 30.dp),
            color = Black,
        )
    }

    val ageList = (2..100).map { it.toString() }
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = initSelectedIndex - 2)
    val snapFling = rememberSnapFlingBehavior(lazyListState = lazyListState)
    val scope = rememberCoroutineScope()


    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val viewportWidth = maxWidth - 30.dp
        val itemWidth = maxWidth / 6
        LazyRow(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 15.dp,
                    end = 15.dp
                )
                .background(
                    color = White,
                    shape = RoundedCornerShape(15.dp)
                ),
            flingBehavior = snapFling,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(
                start = viewportWidth / 2 - itemWidth / 2,
                end = viewportWidth / 2 - itemWidth / 2
            )
        ) {
            items(ageList.size, key = { it }) { index ->
                val ageText = ageList[index]
                Box(
                    modifier = Modifier
                        .height(60.dp)
                        .width(itemWidth)
                        .clickable {
                            scope.launch {
                                lazyListState.animateScrollToItem(
                                    index = index,
                                )
                            }
                        }
                        .graphicsLayer {
                            val layoutInfo = lazyListState.layoutInfo
                            val viewportCenter =
                                layoutInfo.viewportStartOffset + layoutInfo.viewportSize.width / 2f
                            val visibleItemInfo =
                                layoutInfo.visibleItemsInfo.find { it.index == index }
                            visibleItemInfo?.let { itemInfo ->
                                val itemCenter = itemInfo.offset + itemInfo.size / 2f
                                val distance = abs(itemCenter - viewportCenter)
                                val maxDistance = layoutInfo.viewportSize.width / 2f
                                var ratio = 1f - (distance / maxDistance)
                                ratio = ratio.coerceIn(0f, 1f)
                                alpha = ratio
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ageText,
                        fontSize = 28.sp,
                        fontFamily = FontFamily(Font(R.font.font_extrabold)),
                        color = Black
                    )
                }
            }
        }
        Image(
            painter = painterResource(R.drawable.input_age_indicator),
            contentDescription = null,
            modifier = Modifier
                .size(width = 15.dp, height = 10.dp)
                .align(Alignment.BottomCenter)
        )
        LaunchedEffect(lazyListState) {
            val scrollState = derivedStateOf { lazyListState.isScrollInProgress }
            snapshotFlow { scrollState.value }
                .distinctUntilChanged() // 只有滚动状态发生变化才发射（true ↔ false）
                .collect { scrolling ->
                    if (!scrolling) {
                        // 滚动完全停止，计算中心条目
                        val layoutInfo = lazyListState.layoutInfo
                        val viewportCenter =
                            layoutInfo.viewportStartOffset + layoutInfo.viewportSize.width / 2f
                        var minDist = Float.MAX_VALUE
                        var centerIndex = 0
                        layoutInfo.visibleItemsInfo.forEach { item ->
                            val itemCenter = item.offset + item.size / 2f
                            val dist = abs(itemCenter - viewportCenter)
                            if (dist < minDist) {
                                minDist = dist
                                centerIndex = item.index
                            }
                        }
                        onSelectChanged(centerIndex + 2)
                    }
                }
        }


    }
}

@Composable
fun genderSelect(viewModel: InputViewModel, uiState: State<InputViewModel.UserListState>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val selectGender = uiState.value.gender == 1
        GenderSelectCard(
            iconRes = R.drawable.ic_male,
            text = stringResource(R.string.male),
            selected = selectGender,
            onClick = { viewModel.processIntent(DataInputIntent.SetGender(1)) },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .height(90.dp)
        )

        GenderSelectCard(
            iconRes = R.drawable.ic_female,
            text = stringResource(R.string.female),
            selected = !selectGender,
            onClick = { viewModel.processIntent(DataInputIntent.SetGender(0)) },
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .height(90.dp)
        )
    }
}

// 性别选择（）
@Composable
fun GenderSelectCard(
    iconRes: Int,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(90.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(White)
            .graphicsLayer {
                alpha = if (selected) 1f else 0.7f
            }
            .clickable(onClick = onClick),
    ) {
        // 中间垂直布局：图标 + 文字
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                alpha = if (selected) 1f else 0.7f
            )
            Text(
                text = text,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.font_extrabold)),
                color = Black,
                modifier = Modifier.padding(top = 1.dp)
            )
        }

        // 右上角对勾，选中才显示
        if (selected) {
            Image(
                painter = painterResource(R.drawable.ic_check_circle_blue),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 8.dp, end = 8.dp)
                    .size(18.dp)
                    .align(Alignment.TopEnd)
            )
        }
    }
}
