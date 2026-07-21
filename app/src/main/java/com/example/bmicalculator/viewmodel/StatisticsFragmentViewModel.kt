package com.example.bmicalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class StatisticsFragmentViewModel(repository: BmiRepository) : ViewModel() {

    val chartBmiList: Flow<List<BmiEntity>> = repository.getChartBmi()

    enum class TimeMode { DAY, WEEK, MONTH }

    private val _timeMode = MutableStateFlow<TimeMode>(TimeMode.DAY)
    val timeMode: StateFlow<TimeMode> = _timeMode.asStateFlow()

    // 使用 combine 将两者融合成一个“图表渲染状态流”
    val chartUiState: StateFlow<ChartProcessResult?> =
        combine(chartBmiList, timeMode) { data, mode ->
            if (data.isEmpty()) {
                null // 如果数据为空，返回 null 作为信号
            } else {
                // 直接在后台线程/协程中完成高强度的图表数据计算
                processChartData(data, mode)
            }
        }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    private var currentTimeMode = TimeMode.DAY // 默认是天

    fun setTimeMode(mode: TimeMode) {
        currentTimeMode = mode
        _timeMode.value = mode
    }

    // 输出封装好的图表数据
    data class ChartProcessResult(
        val xLabels: List<String>,
        val bmiEntries: List<Entry>,
        val weightEntries: List<Entry>,
        val baseTimeZero: Long,
        val totalCount: Int
    )

    fun processChartData(
        data: List<BmiEntity>,
        currentTimeMode: TimeMode
    ): ChartProcessResult {
        if (data.isEmpty()) {
            return ChartProcessResult(
                xLabels = emptyList(),
                bmiEntries = emptyList(),
                weightEntries = emptyList(),
                baseTimeZero = 0L,
                totalCount = 0
            )
        }

        val xLabelList = mutableListOf<String>()
        val mBaseTimeZero: Long

        // 1. 基准时间：获取最新一条数据，并抹平内部时分秒为凌晨零点
        val latestTime = data.last().customTime
        val baseCal = Calendar.getInstance().apply {
            timeInMillis = latestTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val totalCount: Int
        val calendarStep: Int
        val calendarUnit: Int

        when (currentTimeMode) {
            TimeMode.DAY -> {
                totalCount = 90
                calendarUnit = Calendar.DAY_OF_YEAR
                calendarStep = -1
            }

            TimeMode.WEEK -> {
                totalCount = 54
                // 为了周模式完美对齐，强制把基准时间挪到该自然周的第一天
                baseCal.set(Calendar.DAY_OF_WEEK, baseCal.firstDayOfWeek)
                calendarUnit = Calendar.WEEK_OF_YEAR
                calendarStep = -1
            }

            TimeMode.MONTH -> {
                totalCount = 60
                // 为了月模式完美对齐，强制把基准时间挪到该月 1 号
                baseCal.set(Calendar.DAY_OF_MONTH, 1)
                calendarUnit = Calendar.MONTH
                calendarStep = -1
            }
        }

        // 2. 从最新时间向前倒推，生成的刻度轴和时间戳轴
        val tempCal = baseCal.clone() as Calendar
        val tempLabelList = mutableListOf<String>()
        val tempTimeList = mutableListOf<Long>()

        for (i in 0 until totalCount) {
            tempTimeList.add(tempCal.timeInMillis)

            // 生成标准 X 轴标签文字
            val label = when (currentTimeMode) {
                TimeMode.DAY -> tempCal.get(Calendar.DAY_OF_MONTH).toString()
                TimeMode.WEEK -> tempCal.get(Calendar.DAY_OF_MONTH).toString()
                TimeMode.MONTH -> (tempCal.get(Calendar.MONTH) + 1).toString()
            }
            tempLabelList.add(label)
            // 向前偏移一个周期
            tempCal.add(calendarUnit, calendarStep)
        }

        // 反转列表：让下标 i=0 映射最早的时间，i=totalCount-1 映射最新时间
        tempTimeList.reverse()
        tempLabelList.reverse()
        xLabelList.addAll(tempLabelList)

        // 3.判定记录落在哪个刻度周期内
        val bmiIndexMap = mutableMapOf<Int, MutableList<Float>>()
        val weightIndexMap = mutableMapOf<Int, MutableList<Float>>()
        val entityCal = Calendar.getInstance()
        data.forEach { entity ->
            entityCal.timeInMillis = entity.customTime
            var targetIndex: Int

            // 区间反查法：计算它与最早时间刻度（tempTimeList[0]）的自然跨度差
            when (currentTimeMode) {
                TimeMode.DAY -> {
                    entityCal.set(Calendar.HOUR_OF_DAY, 0)
                    entityCal.set(Calendar.MINUTE, 0)
                    entityCal.set(Calendar.SECOND, 0)
                    entityCal.set(Calendar.MILLISECOND, 0)
                    val msDiff = entityCal.timeInMillis - tempTimeList[0]
                    targetIndex = (msDiff / (1000L * 60 * 60 * 24)).toInt()
                }

                TimeMode.WEEK -> {
                    entityCal.set(Calendar.DAY_OF_WEEK, entityCal.firstDayOfWeek)
                    entityCal.set(Calendar.HOUR_OF_DAY, 0)
                    entityCal.set(Calendar.MINUTE, 0)
                    entityCal.set(Calendar.SECOND, 0)
                    entityCal.set(Calendar.MILLISECOND, 0)
                    val msDiff = entityCal.timeInMillis - tempTimeList[0]
                    targetIndex = (msDiff / (1000L * 60 * 60 * 24 * 7)).toInt()
                }

                TimeMode.MONTH -> {
                    val startCal = Calendar.getInstance().apply { timeInMillis = tempTimeList[0] }
                    val yearDiff = entityCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
                    val monthDiff = entityCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH)
                    targetIndex = yearDiff * 12 + monthDiff
                }
            }

            if (targetIndex in 0 until totalCount) {
                // BMI 直接存入
                bmiIndexMap.getOrPut(targetIndex) { mutableListOf() }.add(entity.bmiValue)
                // 体重统一转kg存入
                val weightKg = if (entity.weightUnit) entity.weight else entity.weight * 0.45359236f
                weightIndexMap.getOrPut(targetIndex) { mutableListOf() }.add(weightKg)
            }
        }

        // 4. 按下标顺序生成 Entry
        // 生成BMI Entry列表
        val bmiEntries = mutableListOf<Entry>()
        for (i in 0 until totalCount) {
            val numList = bmiIndexMap[i] ?: continue
            val yVal =
                if (currentTimeMode == TimeMode.DAY) numList.last() else numList.average().toFloat()
            bmiEntries.add(Entry(i.toFloat(), yVal))
        }

        // 生成体重Entry列表
        val weightEntries = mutableListOf<Entry>()
        for (i in 0 until totalCount) {
            val numList = weightIndexMap[i] ?: continue
            val yVal =
                if (currentTimeMode == TimeMode.DAY) numList.last() else numList.average().toFloat()
            weightEntries.add(Entry(i.toFloat(), yVal))
        }

        mBaseTimeZero = baseCal.timeInMillis

        // 1. 计算基准时间、生成刻度、匹配下标、单位换算、聚合均值
        // 2. 分别生成bmiEntries、weightEntries、xLabels、baseTimeZero、totalCount
        return ChartProcessResult(
            xLabels = xLabelList,
            bmiEntries = bmiEntries,
            weightEntries = weightEntries,
            baseTimeZero = mBaseTimeZero,
            totalCount = totalCount
        )
    }

    companion object {
        fun provideFactory(repository: BmiRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    StatisticsFragmentViewModel(repository)
                }
            }
    }
}