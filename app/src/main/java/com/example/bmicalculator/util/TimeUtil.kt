package com.example.bmicalculator.util

import com.example.bmicalculator.R
import java.util.Calendar

class TimeUtil() {
    // 月份英文简写 -> Calendar月份索引(0~11)
    private val monthNameToIndex = mapOf(
        R.string.jan to 0,
        R.string.feb to 1,
        R.string.mar to 2,
        R.string.apr to 3,
        R.string.may to 4,
        R.string.june to 5,
        R.string.july to 6,
        R.string.aug to 7,
        R.string.sep to 8,
        R.string.oct to 9,
        R.string.nov to 10,
        R.string.dec to 11
    )

    // Calendar月份索引 -> 资源里的月份文本
    private val indexToMonthName = listOf(
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
        selectMonthInt: Int,
        selectDay: String,
        selectPeriod: Int
    ): Long {
        val calendar = Calendar.getInstance()
        calendar.set(
            selectYear.toInt(),
            monthNameToIndex[selectMonthInt] ?: 0,
            selectDay.toInt(),
            when (selectPeriod) {
                R.string.morning -> 9
                R.string.afternoon -> 14
                R.string.evening -> 19
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
        val month = indexToMonthName[monthIdx]
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val period = when (hour) {
            9 -> R.string.morning
            14 -> R.string.afternoon
            19 -> R.string.evening
            else -> R.string.night
        }
        return TimeParseResult(year, month, day, period)
    }
}

/**
 * 时间解析返回数据模型
 */
data class TimeParseResult(
    val selectYear: String,
    val selectMonthInt: Int,
    val selectDay: String,
    val selectPeriodInt: Int
)