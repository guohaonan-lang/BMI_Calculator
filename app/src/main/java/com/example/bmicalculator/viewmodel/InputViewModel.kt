package com.example.bmicalculator.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.util.BmiUtil
import com.example.bmicalculator.util.TimeUtil

@SuppressLint("DefaultLocale")
class InputViewModel(private val repository: BmiRepository) : ViewModel() {


    var inputBmiRecord = BmiEntity(
        height = 170f,
        heightFt = 5,
        heightIn = 7,
        heightUnit = false,
        weight = 140f,
        weightUnit = false,
        bmiValue = 21.9f,
        bmiColor = 0xFF888888.toInt(), // 临时占位色
        age = 25,
        gender = 1,
        createTime = System.currentTimeMillis(),
        customTime = 0L,
    )
    var selectMonth: String = "June"
    var selectDay: String = "21"
    var selectYear: String = "2018"
    var selectPeriod: String = "Morning"
    private var weightPair: Pair<String, String> = "140.00" to "63.50"
    private var heightPair: Pair<Int, String> = 67 to "170.0"


    // 1. 体重单位切换逻辑：lb <-> kg
    fun switchWeightUnitToKg(): String {
        var showText = String.format("%.2f", inputBmiRecord.weight)
        if (showText == weightPair.first) {
            inputBmiRecord.weight = weightPair.second.toFloat()
            showText = weightPair.second
        } else {
            val originWeight = String.format("%.2f", inputBmiRecord.weight)
            inputBmiRecord.weight *= 0.4536f
            showText = String.format("%.2f", inputBmiRecord.weight)
            val newPair = originWeight to showText
            weightPair = newPair
        }
        return showText
    }

    fun switchWeightUnitToLb(): String {
        var showText = String.format("%.2f", inputBmiRecord.weight)

        if (showText == weightPair.second) {
            inputBmiRecord.weight = weightPair.first.toFloat()
            showText = weightPair.first
        } else {
            val originWeight = String.format("%.2f", inputBmiRecord.weight)
            inputBmiRecord.weight /= 0.4536f
            showText = String.format("%.2f", inputBmiRecord.weight)
            val newPair = showText to originWeight
            weightPair = newPair

        }
        return showText
    }

    // 2. 身高单位切换逻辑：cm <-> ft·in
    fun switchHeightUnitToFtIn() {
        val showText = String.format("%.1f", inputBmiRecord.height)
        if (showText == heightPair.second) {
            inputBmiRecord.heightFt = heightPair.first / 12
            inputBmiRecord.heightIn = heightPair.first % 12
        } else {

            val originHeight = String.format("%.1f", inputBmiRecord.height)

            val totalInch = (inputBmiRecord.height / 2.54f).toInt()
            inputBmiRecord.heightFt = totalInch / 12
            inputBmiRecord.heightIn = totalInch % 12
            val newPair = totalInch to originHeight
            heightPair = newPair

        }
    }


    fun switchHeightUnitToCm(): String {
        var showText: String
        val totalInch = inputBmiRecord.heightFt * 12 + inputBmiRecord.heightIn
        if (totalInch == heightPair.first) {
            showText = heightPair.second
        } else {
            showText = String.format(
                "%.1f",
                ((inputBmiRecord.heightFt * 12) + inputBmiRecord.heightIn) * 2.54f
            )
            inputBmiRecord.height = showText.toFloat()
            val newPair = totalInch to showText
            heightPair = newPair

        }
        inputBmiRecord.height = ((inputBmiRecord.heightFt * 12) + inputBmiRecord.heightIn) * 2.54f
        return showText
    }

    //检查数值合法
    data class CheckResult(
        val pass: Boolean,
        val toastMsgRes: Int?, // 提示文案资源ID
        val resetWeight: Float? = null,
        val resetFt: Int? = null,
        val resetInch: Int? = null,
        val resetHeight: Float? = null
    )

    fun checkInputValid(): CheckResult {
        val record = inputBmiRecord
        // 校验体重
        if (!record.weightUnit) {
            // LB模式
            if (record.weight !in 2f..551f) {
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.weight_out_of_range_2_551_lb,
                    resetWeight = 551f
                )
            }
        } else {
            // KG模式
            if (record.weight !in 1f..250f) {
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.weight_out_of_range_2_250_kg,
                    resetWeight = 250f
                )
            }
        }

        // 校验身高
        if (!record.heightUnit) {
            // 英制 ft/in
            if (record.heightFt !in 1..8) {
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.height_out_of_range_1_8_ft,
                    resetFt = 8
                )
            }
            if (record.heightIn !in 0..11) {
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.height_out_of_range_1_11_in,
                    resetInch = 11
                )
            }
        } else {
            // 公制 cm
            if (record.height !in 1f..250f) {
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.height_out_of_range_1_250_cm,
                    resetHeight = 150f
                )
            }
        }
        return CheckResult(pass = true, toastMsgRes = null)
    }

    // 4. 完整计算BMI、填充实体信息（原calculate按钮内全部计算逻辑迁移）
    fun computeFullBmi(context: Context): BmiEntity {
        if (inputBmiRecord.heightUnit) {
            val showText = String.format("%.1f", inputBmiRecord.height)
            if (showText == heightPair.second) {
                inputBmiRecord.heightFt = heightPair.first / 12
                inputBmiRecord.heightIn = heightPair.first % 12
            } else {
                val totalInch = (inputBmiRecord.height / 2.54f).toInt()
                inputBmiRecord.heightFt = totalInch / 12
                inputBmiRecord.heightIn = totalInch % 12
            }
        } else {
            var showText = String.format("%.1f", inputBmiRecord.height)
            val totalInch = inputBmiRecord.heightFt * 12 + inputBmiRecord.heightIn
            if (totalInch == heightPair.first) {
                showText == heightPair.second
            } else {
                showText = String.format(
                    "%.1f",
                    ((inputBmiRecord.heightFt * 12) + inputBmiRecord.heightIn) * 2.54f
                )
                inputBmiRecord.height = showText.toFloat()

            }
            inputBmiRecord.height =
                ((inputBmiRecord.heightFt * 12) + inputBmiRecord.heightIn) * 2.54f
        }

        var weightKg = inputBmiRecord.weight
        if (!inputBmiRecord.weightUnit) weightKg = inputBmiRecord.weight * 0.45359236f
        var heightM = inputBmiRecord.height / 100f
        if (!inputBmiRecord.heightUnit) heightM =
            ((inputBmiRecord.heightFt * 12) + inputBmiRecord.heightIn) * 2.54f / 100f

        val bmi = weightKg / (heightM * heightM)

        val bmiLevel =
            BmiUtil.getBmiFullInfo(context, inputBmiRecord.age, inputBmiRecord.gender, bmi)
        inputBmiRecord.bmiColor = ContextCompat.getColor(context, bmiLevel.colorInt)



        inputBmiRecord.apply {
            bmiValue = bmi
            createTime = System.currentTimeMillis()
            customTime = TimeUtil(context).getCustomTimeStamp(
                selectYear,
                selectMonth,
                selectDay,
                selectPeriod
            )
        }
        return inputBmiRecord
    }

    suspend fun getLatestBmi(): BmiEntity? {
        return repository.getLatestBmi()
    }


    companion object {
        fun provideFactory(
            repository: BmiRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                InputViewModel(repository)
            }
        }
    }
}