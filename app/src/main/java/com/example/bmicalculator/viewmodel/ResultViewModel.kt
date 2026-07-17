package com.example.bmicalculator.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.model.Grade
import com.example.bmicalculator.util.BmiUtil
import com.example.bmicalculator.util.TimeUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.Long
import kotlin.math.max
import kotlin.math.min

class ResultViewModel(private val repository: BmiRepository) : ViewModel() {

    var resultBmiRecord = BmiEntity()

    data class ResultUiState(
        val age: Int = 25,
        val bmiValue: Float = 0f,
        val weightText: String = "",
        val heightText: String = "",
        val ageText: String = "",
        val genderText: String = "",
        val levelName: String = "",
        val bmiColor: Int = 0,
        val assessment1: String = "",
        val assessment2Text: String = "",
        val isAssessmentNormalHidden: Boolean = false, // 正常状态下隐藏部分UI
        val normalRangeText: String = "",
        val differenceText: String = "",
        val timeTagText: String = "",
        // 下面是页面底部的“模式”判定状态（基于statusRecent和statusFirst）
        val isGradeRvVisible: Boolean = true,
        val isSaveBtnVisible: Boolean = true,
        val isDeleteBtnVisible: Boolean = true,
        val isRecentDeleteVisible: Boolean = false,
        val isRecentBackVisible: Boolean = false,
        val isMergeAdVisible: Boolean = true,
        val isTimeTagVisible: Boolean = true,
        val hasHelpIcon: Boolean = false,
        val gradeList: List<Grade> = emptyList() // 假设你的Adapter项叫GradeItem
    )

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState = _uiState.asStateFlow()

    fun initDataFromIntent(
        context: Context,
        record: BmiEntity?,
        statusFirst: Boolean,
        statusRecent: Boolean
    ) {
        if (record == null) {
            return
        }


        // === 1. 基础业务字段格式化 ===
        val weightStr = if (record.weightUnit) "${record.weight} kg" else "${record.weight} lb"
        val heightStr =
            if (record.heightUnit) "${record.height} cm" else "${record.heightFt}ft ${record.heightIn}in"
        val genderStr =
            if (record.gender == 1) context.getString(R.string.male) else context.getString(R.string.female)

        val bmiInfo = BmiUtil.getBmiFullInfo(context, record.age, record.gender, record.bmiValue)
        val isNormal = bmiInfo.levelName == context.getString(R.string.adults_bmi_normal)

        // === 2. 评估模块逻辑判定 ===
        val baseText = context.getString(R.string.result_assessment_weight)
        val assessment2Str =
            if (record.heightUnit) "$baseText ${record.height} cm" else "$baseText (${record.heightFt}ft ${record.heightIn}in):"

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
        val gradeList = BmiUtil.getGradeList(context, record.age, record.gender)
        val levelIndex = if (record.age > 20) BmiUtil.getGradeIndex(context, bmiInfo.levelName) - 1
        else BmiUtil.getGradeIndex(context, bmiInfo.levelName) - 3
        if (levelIndex in gradeList.indices) {
            gradeList[levelIndex].isSelect = true
        }

        // === 4. 时间与页面布局模式判定 (合并 initChangePage 逻辑) ===
        val timeText = TimeUtil(context).parseTimeStamp(record.customTime ?: 0)
        val timeTagStr =
            "${timeText.selectMonth} ${timeText.selectDay} ${timeText.selectYear}  ${timeText.selectPeriod}"

        // 根据入参组合出最终的 UI 按钮可见性
        val stateBuilder = ResultUiState(
            age = record.age,
            bmiValue = record.bmiValue,
            weightText = weightStr,
            heightText = heightStr,
            ageText = record.age.toString(),
            genderText = genderStr,
            levelName = bmiInfo.levelName,
            bmiColor = record.bmiColor,
            assessment1 = bmiInfo.assessment,
            assessment2Text = assessment2Str,
            isAssessmentNormalHidden = isNormal,
            normalRangeText = rangeStr,
            differenceText = diffStr,
            timeTagText = timeTagStr,
            gradeList = gradeList
        )

        // 核心：抹平 statusRecent 和 statusFirst 的 if-else 嵌套
        val finalState = if (statusRecent) {
            stateBuilder.copy(
                isGradeRvVisible = false, isSaveBtnVisible = false, isDeleteBtnVisible = false,
                isRecentDeleteVisible = true, isRecentBackVisible = true, hasHelpIcon = true
            )
        } else {
            if (statusFirst) {
                stateBuilder.copy(isMergeAdVisible = false)
            } else {
                stateBuilder.copy(
                    isGradeRvVisible = false,
                    isTimeTagVisible = false,
                    hasHelpIcon = true
                )
            }
        }

        _uiState.value = finalState
    }


    suspend fun insertBmiRecord(bmi: BmiEntity) {
        repository.insertBmiRecord(bmi)
    }

    suspend fun deleteBmiRecord(bmi: BmiEntity) {
        repository.deleteBmiRecord(bmi)
    }

    suspend fun countBmiRecord(): Long {
        return repository.countBmiRecord()
    }

    data class NormalBmiRange(
        val max: Float,
        val min: Float,
        val difference: Float,
        val sign: String,
        val unit: String
    )

    fun calculatorNormalRange(): NormalBmiRange {
        val unit: String
        var minBmi: Float
        var maxBmi: Float
        if (resultBmiRecord.age <= 20) {
            val teenRange = if (resultBmiRecord.gender == 0) {
                BmiUtil.femaleTeenTable.firstOrNull { it.age == resultBmiRecord.age }
            } else {
                BmiUtil.maleTeenTable.firstOrNull { it.age == resultBmiRecord.age }
            }
            minBmi = teenRange?.underweightMax ?: 0f
            maxBmi = teenRange?.normalMax ?: 0f
        } else {
            minBmi = 18.5f
            maxBmi = 24.9f
        }

        val h: Float = (if (resultBmiRecord.heightUnit) {
            resultBmiRecord.height / 100f
        } else (resultBmiRecord.heightFt * 12f + resultBmiRecord.heightIn) * 2.54f / 100f)


        var minSum = minBmi * h * h
        var maxSum = maxBmi * h * h
        var diff1: Float
        var diff2: Float

        if (resultBmiRecord.weightUnit) {
            diff1 = resultBmiRecord.weight - minSum
            diff2 = resultBmiRecord.weight - maxSum
        } else {
            diff1 = (resultBmiRecord.weight * 0.45359236f) - minSum
            diff2 = (resultBmiRecord.weight * 0.45359236f) - maxSum
        }
        var difference: Float = if (diff1 > 0) min(diff1, diff2)
        else max(diff1, diff2)
        val sign = if (diff1 > 0) "+"
        else ""
        if (resultBmiRecord.weightUnit) {
            unit = "kg"
        } else {
            unit = "lb"
            minSum /= 0.45359236f
            maxSum /= 0.45359236f
            difference /= 0.45359236f
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