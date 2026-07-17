package com.example.bmicalculator.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.util.BmiUtil
import com.example.bmicalculator.util.TimeUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
class InputViewModel(private val repository: BmiRepository) : ViewModel() {

    private val _weight = MutableStateFlow("170.0")
    val weightFlow: StateFlow<String> = _weight.asStateFlow()

    private val _height = MutableStateFlow(170f)
    val heightFlow: StateFlow<Float> = _height.asStateFlow()

    private val _heightFt = MutableStateFlow(5)
    val heightFtFlow: StateFlow<Int> = _heightFt.asStateFlow()
    private val _heightIn = MutableStateFlow(7)
    val heightInFlow: StateFlow<Int> = _heightIn.asStateFlow()

    private val _time1 = MutableStateFlow("170.0")
    val time1: StateFlow<String> = _time1.asStateFlow()
    private val _time2 = MutableStateFlow("170.0")
    val time2: StateFlow<String> = _time2.asStateFlow()

    private val _gender = MutableStateFlow(1)
    val genderFlow: StateFlow<Int> = _gender.asStateFlow()

    private val _weightUnit = MutableStateFlow(false)
    val weightUnitFlow: StateFlow<Boolean> = _weightUnit.asStateFlow()
    private val _heightUnit = MutableStateFlow(false)
    val heightUnitFlow: StateFlow<Boolean> = _heightUnit.asStateFlow()

    private val _age = MutableStateFlow(1)
    val ageFlow: StateFlow<Int> = _age.asStateFlow()

    var inputBmiRecord = BmiEntity()
    var selectMonth: String = "June"
    var selectDay: String = "21"
    var selectYear: String = "2018"
    var selectPeriod: String = "Morning"
    private var weightPair: Pair<String, String> = "140.00" to "63.50"
    private var heightPair: Pair<Int, String> = 67 to "170.0"
    var isFirstData = true

    // 初始化赋值
    fun initRecord(record: BmiEntity) {
        if (record.createTime == inputBmiRecord.createTime) return
        else inputBmiRecord.createTime = record.createTime
        setGender(record.gender)
        setWeightThumb(record.weightUnit)
        setWeight(record.weight)
        setHeightThumb(record.heightUnit)
        setHeight(record.height)
        setHeightFt(record.heightFt)
        setHeightIn(record.heightIn)
        setAge(record.age)
    }

    fun setAge(age: Int) {
        inputBmiRecord.age = age
        _age.value = age
    }

    fun setTime1(year: String, month: String, day: String) {
        selectYear = year
        selectMonth = month
        selectDay = day
        _time1.value = "$month $day, $year"
    }

    fun setTime2(period: String) {
        selectPeriod = period
        _time2.value = period
    }

    fun setWeight(weight: Float) {
        inputBmiRecord.weight = weight
        _weight.value = String.format("%.2f", inputBmiRecord.weight)
    }

    fun setHeight(height: Float) {
        inputBmiRecord.height = height
        _height.value = height
    }

    fun setHeightFt(height: Int) {
        inputBmiRecord.heightFt = height
        _heightFt.value = height
    }

    fun setHeightIn(height: Int) {
        inputBmiRecord.heightIn = height
        _heightIn.value = height
    }

    fun setGender(gender: Int) {
        inputBmiRecord.gender = gender
        _gender.value = gender
    }

    fun setWeightThumb(unit: Boolean) {
        if (inputBmiRecord.weightUnit != unit) {
            inputBmiRecord.weightUnit = unit
        }
        _weightUnit.value = unit

    }

    fun setHeightThumb(unit: Boolean) {
        if (inputBmiRecord.heightUnit != unit) {
            inputBmiRecord.heightUnit = unit
        }
        _heightUnit.value = unit
    }

    // 1. 体重单位切换逻辑：lb <-> kg
    fun switchWeightUnitToKg() {
        var showText = String.format("%.2f", inputBmiRecord.weight)
        if (showText == weightPair.first) {
            inputBmiRecord.weight = weightPair.second.toFloat()
        } else {
            val originWeight = String.format("%.2f", inputBmiRecord.weight)
            inputBmiRecord.weight *= 0.4536f
            showText = String.format("%.2f", inputBmiRecord.weight)
            val newPair = originWeight to showText
            weightPair = newPair
        }
        _weight.value = String.format("%.2f", inputBmiRecord.weight)
    }

    fun switchWeightUnitToLb() {
        var showText = String.format("%.2f", inputBmiRecord.weight)
        if (showText == weightPair.second) {
            inputBmiRecord.weight = weightPair.first.toFloat()
        } else {
            val originWeight = String.format("%.2f", inputBmiRecord.weight)
            inputBmiRecord.weight /= 0.4536f
            showText = String.format("%.2f", inputBmiRecord.weight)
            val newPair = showText to originWeight
            weightPair = newPair
        }
        _weight.value = String.format("%.2f", inputBmiRecord.weight)
    }

    // 2. 身高单位切换逻辑：cm <-> ft·in
    fun switchHeightUnitToFtIn() {
        val showText = String.format("%.1f", inputBmiRecord.height)
        if (showText != heightPair.second) {
            val originHeight = String.format("%.1f", inputBmiRecord.height)
            val totalInch = (inputBmiRecord.height / 2.54f).toInt()
            inputBmiRecord.heightFt = totalInch / 12
            inputBmiRecord.heightIn = totalInch % 12
            val newPair = totalInch to originHeight
            heightPair = newPair
        }
        _heightFt.value = inputBmiRecord.heightFt
        _heightIn.value = inputBmiRecord.heightIn
    }


    fun switchHeightUnitToCm() {
        var showText: String
        val totalInch = inputBmiRecord.heightFt * 12 + inputBmiRecord.heightIn
        if (totalInch != heightPair.first) {
            showText = String.format(
                "%.1f",
                ((inputBmiRecord.heightFt * 12) + inputBmiRecord.heightIn) * 2.54f
            )
            inputBmiRecord.height = showText.toFloat()
            val newPair = totalInch to showText
            heightPair = newPair
        }
        _height.value = inputBmiRecord.height
    }



    //检查数值合法
    data class CheckResult(
        val pass: Boolean,
        val toastMsgRes: Int?, // 提示文案资源ID
    )

    fun checkInputValid(): CheckResult {
        val record = inputBmiRecord
        // 校验体重
        if (!record.weightUnit) {
            // LB模式
            if (record.weight !in 2f..551f) {
                _weight.value = "551.00"
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.weight_out_of_range_2_551_lb,
                )
            }else _weight.value = String.format("%.2f", inputBmiRecord.weight)
        } else {
            // KG模式
            if (record.weight !in 1f..250f) {
                _weight.value = "250.00"
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.weight_out_of_range_2_250_kg,
                )
            }else _weight.value = String.format("%.2f", inputBmiRecord.weight)
        }

        // 校验身高
        if (!record.heightUnit) {
            // 英制 ft/in
            if (record.heightFt !in 1..8) {
                _heightFt.value = 8
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.height_out_of_range_1_8_ft,
                )
            }else _heightFt.value = record.heightFt
            if (record.heightIn !in 0..11) {
                _heightIn.value = 11
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.height_out_of_range_1_11_in,
                )
            }else _heightIn.value = record.heightIn
        } else {
            // 公制 cm
            if (record.height !in 1f..250f) {
                _height.value = 170f
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.height_out_of_range_1_250_cm,
                )
            }else _height.value = record.height
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

    // 缓存最新BMI记录，给UI监听
    private val _latestBmiRecord = MutableStateFlow<BmiEntity?>(null)
    val latestBmiRecord: StateFlow<BmiEntity?> = _latestBmiRecord

    init {
        // 全局监听数据库，数据变化自动更新缓存
        viewModelScope.launch {
            repository.getLatestBmi().collect { entity ->
                _latestBmiRecord.value = entity
            }
        }
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