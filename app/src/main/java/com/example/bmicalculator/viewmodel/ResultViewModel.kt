package com.example.bmicalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.model.Grade
import com.example.bmicalculator.util.BmiUtil
import com.example.bmicalculator.util.TimeUtil
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class ResultViewModel(private val repository: BmiRepository) : ViewModel() {


    sealed class ResultIntent {
        data class UpdateRecord(val newRecord: BmiEntity) : ResultIntent()
        data class UpdateStatus(val isFirst: Boolean, val isRecent: Boolean) : ResultIntent()

        object SaveResult : ResultIntent()
        object DeleteResult : ResultIntent()
        object BackPage : ResultIntent()
    }

    fun processIntent(intent: ResultIntent) {
        when (intent) {
            is ResultIntent.UpdateRecord -> updateRecord(intent.newRecord)
            is ResultIntent.UpdateStatus -> updateStatus(intent.isFirst, intent.isRecent)
            is ResultIntent.SaveResult -> saveResult()
            is ResultIntent.DeleteResult -> deleteResult()
            is ResultIntent.BackPage -> backPage()
        }
    }

    data class ResultUiState(
        val bmiData: BmiEntity? = null,
        val isRecent: Boolean = false,
        val isFirst: Boolean = false,
        val levelNameInt: Int = R.string.load,
        val weightText: String = "",
        val heightText: String = "",
        val genderTextInt: Int = R.string.load,
        val ageText: String = "",
        val assessment1Int: Int = R.string.load,
        val assessment2Text: String = "",
        val isAssessmentNormalHidden: Boolean = false, // 正常状态下隐藏部分UI
        val normalRangeText: String = "",
        val differenceText: String = "",
        val timeYear: String = "",
        val timeMonthInt: Int = R.string.load,
        val timeDay: String = "",
        val timePeriodInt: Int = R.string.load,
        val gradeList: List<Grade> = emptyList(),
        val baseTextInt: Int = R.string.load,
        val buttonColor:Int = R.color.blue
    )

    sealed class ResultEvent {
        object NavToBack : ResultEvent()
        object NavToStart : ResultEvent()
        object NavToMain : ResultEvent()
    }

    private val _effect = MutableSharedFlow<ResultEvent>()
    val effect = _effect.asSharedFlow()

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState = _uiState.asStateFlow()

    private fun updateRecord(newRecord: BmiEntity) {
        _uiState.update { it.copy(bmiData = newRecord) }
    }

    private fun updateStatus(first: Boolean, recent: Boolean) {
        _uiState.update { it.copy(isFirst = first, isRecent = recent) }
    }

    private fun saveResult() {
        viewModelScope.launch {
            _uiState.value.bmiData?.let {
                repository.insertBmiRecord(it)
            }
            val count = repository.countBmiRecord()
            if(count == 1L)emitEvent(ResultEvent.NavToMain)
            else emitEvent(ResultEvent.NavToBack)
        }
    }

    private fun deleteResult() {
        viewModelScope.launch {
            _uiState.value.bmiData?.let {
                repository.deleteBmiRecord(it)
                val count = repository.countBmiRecord()
                if (count == 0L) {
                    emitEvent(ResultEvent.NavToStart)
                }else emitEvent(ResultEvent.NavToBack)
            }
        }
    }

    private fun backPage() {
        emitEvent(ResultEvent.NavToBack)
    }



    private fun emitEvent(event: ResultEvent) {
        viewModelScope.launch {
            _effect.emit(event)
        }
    }


    fun initDataFromIntent(
        record: BmiEntity?,
    ) {
        if (record == null) {
            return
        }

        // === 1. 基础业务字段格式化 ===
        val weightStr = if (record.weightUnit) "${record.weight} kg" else "${record.weight} lb"
        val heightStr =
            if (record.heightUnit) "${record.height} cm" else "${record.heightFt}ft ${record.heightIn}in"
        val genderStrInt =
            if (record.gender == 1) R.string.male else R.string.female

        val bmiInfo = BmiUtil.getBmiFullInfo(record.age, record.gender, record.bmiValue)
        val isNormal = bmiInfo.levelNameInt == R.string.adults_bmi_normal

        // === 2. 评估模块逻辑判定 ===
        val baseTextInt = R.string.result_assessment_weight
        val assessment2Str =
            if (record.heightUnit) " ${record.height} cm" else " (${record.heightFt}ft ${record.heightIn}in):"

        // 计算范围
        val normalRange = calculatorNormalRange()
        val rangeStr = "%.1f %s - %.1f %s".format(
            normalRange.min,
            normalRange.unit,
            normalRange.max,
            normalRange.unit
        )
        val diffStr =
            "(%s%.1f %s)".format(normalRange.sign, normalRange.difference, normalRange.unit)

        // === 3. 列表状态处理 ===
        val gradeList = BmiUtil.getGradeList(record.age, record.gender)
        val levelIndex = if (record.age > 20) BmiUtil.getGradeIndex(bmiInfo.levelNameInt) - 1
        else BmiUtil.getGradeIndex(bmiInfo.levelNameInt) - 3
        if (levelIndex in gradeList.indices) {
            gradeList[levelIndex].isSelect = true
        }

        // === 4. 时间与页面布局模式判定 (合并 initChangePage 逻辑) ===
        val timeText = TimeUtil().parseTimeStamp(record.customTime)


        _uiState.update {
            it.copy(
                levelNameInt = bmiInfo.levelNameInt,
                weightText = weightStr,
                heightText = heightStr,
                genderTextInt = genderStrInt,
                assessment1Int = bmiInfo.assessmentInt,
                baseTextInt = baseTextInt,
                assessment2Text = assessment2Str,
                isAssessmentNormalHidden = isNormal,
                normalRangeText = rangeStr,
                differenceText = diffStr,
                timeYear = timeText.selectYear,
                timeMonthInt = timeText.selectMonthInt,
                timeDay = timeText.selectDay,
                timePeriodInt = timeText.selectPeriodInt,
                gradeList = gradeList,
                buttonColor = bmiInfo.colorInt
            )
        }

    }

    data class NormalBmiRange(
        val max: Float,
        val min: Float,
        val difference: Float,
        val sign: String,
        val unit: String
    )

    fun calculatorNormalRange(): NormalBmiRange {
        var unit = ""
        var minBmi: Float
        var maxBmi: Float

        var maxSum = 0f
        var minSum = 0f
        var difference = 0f
        var sign = ""

        _uiState.value.bmiData?.let { record ->

            if (record.age <= 20) {
                val teenRange = if (record.gender == 0) {
                    BmiUtil.femaleTeenTable.firstOrNull { it.age == record.age }
                } else {
                    BmiUtil.maleTeenTable.firstOrNull { it.age == record.age }
                }
                minBmi = teenRange?.underweightMax ?: 0f
                maxBmi = teenRange?.normalMax ?: 0f
            } else {
                minBmi = 18.5f
                maxBmi = 24.9f
            }

            val h: Float = (if (record.heightUnit) {
                record.height / 100f
            } else (record.heightFt * 12f + record.heightIn) * 2.54f / 100f)


            minSum = minBmi * h * h
            maxSum = maxBmi * h * h
            var diff1: Float
            var diff2: Float

            if (record.weightUnit) {
                diff1 = record.weight - minSum
                diff2 = record.weight - maxSum
            } else {
                diff1 = (record.weight * 0.45359236f) - minSum
                diff2 = (record.weight * 0.45359236f) - maxSum
            }
            difference = if (diff1 > 0) min(diff1, diff2)
            else max(diff1, diff2)
            sign = if (diff1 > 0) "+"
            else ""
            if (record.weightUnit) {
                unit = "kg"
            } else {
                unit = "lb"
                minSum /= 0.45359236f
                maxSum /= 0.45359236f
                difference /= 0.45359236f
            }

        }
        return NormalBmiRange(
            max = maxSum,
            min = minSum,
            difference,
            sign,
            unit
        )
    }

    companion object {
        fun provideFactory(repository: BmiRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ResultViewModel(repository)
                }
            }
    }
}