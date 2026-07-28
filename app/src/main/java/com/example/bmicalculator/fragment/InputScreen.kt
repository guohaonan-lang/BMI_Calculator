package com.example.bmicalculator.fragment

import android.view.MotionEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.core.content.res.ResourcesCompat
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter
import com.contrarywind.view.WheelView
import com.example.bmicalculator.R
import com.example.bmicalculator.ui.theme.BMIComposeTheme
import com.example.bmicalculator.ui.theme.Background
import com.example.bmicalculator.ui.theme.Black
import com.example.bmicalculator.ui.theme.Blue
import com.example.bmicalculator.ui.theme.Gray
import com.example.bmicalculator.ui.theme.White
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs


@Preview(widthDp = 441, heightDp = 891, showBackground = true)
@Composable
fun PreviewInputScreen() {
    BMIComposeTheme {
        InputScreen()
    }

}

@Composable
fun InputScreen(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(Background),
) {
    var heightInput by remember { mutableStateOf("140") }
    var showDateBottomSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
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
            )
        }
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
                stringResource(R.string.input_weight),
                fontFamily = FontFamily(Font(R.font.font_regular)),
                fontSize = 14.sp,
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            TextField(
                value = "140",
                onValueChange = {
                    heightInput = it
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = 20.dp,
                        end = 8.dp,
                    ),
                textStyle = TextStyle(
                    color = Black,
                    fontFamily = FontFamily(Font(R.font.montserrat_extrabold)),
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center
                ),
                suffix = {
                    Text("")
                },
                isError = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                interactionSource = remember { MutableInteractionSource() },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    disabledContainerColor = White,

                    focusedIndicatorColor = Background,
                    unfocusedIndicatorColor = Background,
                    disabledIndicatorColor = Background,
                )
            )
            TextField(
                value = "140",
                onValueChange = {
                    heightInput = it
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = 8.dp,
                        end = 20.dp,
                    ),
                textStyle = TextStyle(
                    color = Black,
                    fontFamily = FontFamily(Font(R.font.montserrat_extrabold)),
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center
                ),
                suffix = {
                    Text("")
                },
                isError = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                interactionSource = remember { MutableInteractionSource() },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    disabledContainerColor = White,

                    focusedIndicatorColor = Background,
                    unfocusedIndicatorColor = Background,
                    disabledIndicatorColor = Background,
                )
            )
        }
        // weight单位转换
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

                        }
                        .background(
                            color = White,
                        )
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "lb",
                        fontFamily = FontFamily(Font(R.font.font_extrabold))
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(15.dp))
                        .clickable {

                        }
                        .background(
                            color = White,
                        )
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "kg",
                        fontFamily = FontFamily(Font(R.font.font_extrabold))
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

                        }
                        .background(
                            color = White,
                        )
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "ft·in",
                        fontFamily = FontFamily(Font(R.font.font_extrabold))
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(15.dp))
                        .clickable {

                        }
                        .background(
                            color = White,
                        )
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "cm",
                        fontFamily = FontFamily(Font(R.font.font_extrabold))
                    )
                }
            }
        }
        Text(
            stringResource(R.string.input_time),
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 14.sp,
            modifier = Modifier
                .padding(top = 30.dp)
                .align(Alignment.CenterHorizontally),
            color = Black,
        )
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
                    .clickable {
                        showDateBottomSheet = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "July 25,2026",
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
                    .clickable {
                        showDateBottomSheet = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Morning",
                    color = Black,
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.font_extrabold)),
                    softWrap = false,
                    maxLines = 1
                )
            }
        }

        Text(
            stringResource(R.string.input_age),
            fontFamily = FontFamily(Font(R.font.font_regular)),
            fontSize = 14.sp,
            modifier = Modifier
                .padding(top = 30.dp)
                .align(Alignment.CenterHorizontally),
            color = Black,
        )
        AgeHorizontalPicker(
            25,
            {}
        )

        // 性别选择
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GenderSelectCard(
                iconRes = R.drawable.ic_male,
                text = stringResource(R.string.male),
                selected = false,
                onClick = { },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .height(90.dp)
            )

            GenderSelectCard(
                iconRes = R.drawable.ic_female,
                text = stringResource(R.string.female),
                selected = true,
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .height(90.dp)
            )
        }
        Button(
            onClick = { },
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
    DatePickerBottomSheet(
        show = showDateBottomSheet,
        onDismiss = {
            // 关闭弹窗：重置状态
            showDateBottomSheet = false
        },
        onConfirm = { year, month, day ->
            // 选中日期回调逻辑
            // TODO 更新文本显示 "July 25,2026"
            showDateBottomSheet = false
        }
    )
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
    onConfirm: (String, String, String) -> Unit
) {
    if (!show) return

    val wheelMonthRef = remember { mutableStateOf<WheelView?>(null) }
    val wheelDayRef = remember { mutableStateOf<WheelView?>(null) }
    val wheelYearRef = remember { mutableStateOf<WheelView?>(null) }

    // 基础数据源（和旧代码保持一致）
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

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
            // 弹窗关闭释放引用，防止内存泄漏
            wheelMonthRef.value = null
            wheelDayRef.value = null
            wheelYearRef.value = null
        },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = White
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
                        val selMonth = monthData[wMonth.currentItem]
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

@Composable
fun AgeHorizontalPicker(
    initSelectedIndex: Int,
    onSelectChanged: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val ageList = (1..120).map { it.toString() }
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
                                // ✅ 带上scrollOffset，实现居中滚动
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
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.font_extrabold)),
                        color = Black
                    )
                }
            }
        }
        LaunchedEffect(lazyListState) {
            snapshotFlow { lazyListState.layoutInfo }
                .collect { layoutInfo ->
                    if (!lazyListState.isScrollInProgress) {
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
                        onSelectChanged(centerIndex)
                    }
                }
        }

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
