package com.example.bmicalculator.util

import android.content.Context
import com.example.bmicalculator.R
import java.util.Calendar

class TimeUtil(private val context: Context) {
    // 月份英文简写 -> Calendar月份索引(0~11)
    private val monthNameToIndex = mapOf(
        context.getString(R.string.jan) to 0,
        context.getString(R.string.feb) to 1,
        context.getString(R.string.mar) to 2,
        context.getString(R.string.apr) to 3,
        context.getString(R.string.may) to 4,
        context.getString(R.string.june) to 5,
        context.getString(R.string.july) to 6,
        context.getString(R.string.aug) to 7,
        context.getString(R.string.sep) to 8,
        context.getString(R.string.oct) to 9,
        context.getString(R.string.nov) to 10,
        context.getString(R.string.dec) to 11
    )

    // Calendar月份索引 -> 资源里的月份文本
    private val indexToMonthName = mapOf(
        0 to context.getString(R.string.jan),
        1 to context.getString(R.string.feb),
        2 to context.getString(R.string.mar),
        3 to context.getString(R.string.apr),
        4 to context.getString(R.string.may),
        5 to context.getString(R.string.june),
        6 to context.getString(R.string.july),
        7 to context.getString(R.string.aug),
        8 to context.getString(R.string.sep),
        9 to context.getString(R.string.oct),
        10 to context.getString(R.string.nov),
        11 to context.getString(R.string.dec)
    )

    /**
     * 正向：年月日时段 → 时间戳
     * @param selectYear 年份字符串
     * @param selectMonth Jan/Feb...
     * @param selectDay 日期字符串
     * @param selectPeriod Morning/Afternoon/Evening/Night
     * @return 毫秒时间戳
     */
    fun getCustomTimeStamp(
        selectYear: String,
        selectMonth: String,
        selectDay: String,
        selectPeriod: String
    ): Long {
        val calendar = Calendar.getInstance()
        calendar.set(
            selectYear.toInt(),
            monthNameToIndex[selectMonth] ?: 0,
            selectDay.toInt(),
            when (selectPeriod) {
                context.getString(R.string.morning) -> 9
                context.getString(R.string.afternoon) -> 14
                context.getString(R.string.evening) -> 19
                else -> 23
            },
            0,
            0
        )
        return calendar.timeInMillis
    }

    /**
     * 反向：时间戳 → 年月日时段实体
     */
    fun parseTimeStamp(timestamp: Long): TimeParseResult {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        val year = calendar.get(Calendar.YEAR).toString()
        val monthIdx = calendar.get(Calendar.MONTH)
        val month = indexToMonthName[monthIdx] ?: context.getString(R.string.jan)
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val period = when (hour) {
            9 -> context.getString(R.string.morning)
            14 -> context.getString(R.string.afternoon)
            19 -> context.getString(R.string.evening)
            else -> context.getString(R.string.night)
        }
        return TimeParseResult(year, month, day, period)
    }
}

/**
 * 时间解析返回数据模型
 */
data class TimeParseResult(
    val selectYear: String,
    val selectMonth: String,
    val selectDay: String,
    val selectPeriod: String
)