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

    private val _weight = MutableStateFlow("140.0")
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

    var createTime: Long = 0
    var selectMonth: String = "June"
    var selectDay: String = "21"
    var selectYear: String = "2018"
    var selectPeriod: String = "Morning"
    private var weightPair: Pair<String, String> = "140.00" to "63.50"
    private var heightPair: Pair<Int, String> = 67 to "170.0"
    var isFirstData = true

    // 初始化赋值
    fun initRecord(record: BmiEntity) {
        if (record.createTime == createTime) return
        else createTime = record.createTime
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
        _weight.value = String.format("%.2f", weight)
    }

    fun setHeight(height: Float) {
        _height.value = height
    }

    fun setHeightFt(height: Int) {
        _heightFt.value = height
    }

    fun setHeightIn(height: Int) {
        _heightIn.value = height
    }

    fun setGender(gender: Int) {
        _gender.value = gender
    }

    fun setWeightThumb(unit: Boolean) {
        _weightUnit.value = unit
    }

    fun setHeightThumb(unit: Boolean) {
        _heightUnit.value = unit
    }

    // 1. 体重单位切换逻辑：lb <-> kg
    fun switchWeightUnitToKg() {
        var weight = _weight.value.toFloat()
        if (_weight.value != weightPair.first) {
            val originWeight = _weight.value
            weight *= 0.4536f
            _weight.value = String.format("%.2f", weight)
            val newPair = originWeight to _weight.value
            weightPair = newPair
        } else _weight.value = weightPair.second
    }

    fun switchWeightUnitToLb() {
        var weight = _weight.value.toFloat()
        if (_weight.value != weightPair.second) {
            val originWeight = _weight.value
            weight /= 0.4536f
            _weight.value = String.format("%.2f", weight)
            val newPair = _weight.value to originWeight
            weightPair = newPair
        } else _weight.value = weightPair.first
    }

    // 2. 身高单位切换逻辑：cm <-> ft·in
    fun switchHeightUnitToFtIn() {
        val showText = String.format("%.1f", _height.value)
        if (showText != heightPair.second) {
            val originHeight = String.format("%.1f", _height.value)
            val totalInch = (_height.value / 2.54f).toInt()
            _heightFt.value = totalInch / 12
            _heightIn.value = totalInch % 12
            val newPair = totalInch to originHeight
            heightPair = newPair
        }
    }


    fun switchHeightUnitToCm() {
        var showText: String
        val totalInch = _heightFt.value * 12 + _heightIn.value
        if (totalInch != heightPair.first) {
            showText = String.format(
                "%.1f",
                ((_heightFt.value * 12) + _heightIn.value) * 2.54f
            )
            _height.value = showText.toFloat()
            val newPair = totalInch to showText
            heightPair = newPair
        }
    }

    //检查数值合法
    data class CheckResult(
        val pass: Boolean,
        val toastMsgRes: Int?, // 提示文案资源ID
    )

    fun checkInputValid(): CheckResult {
        // 校验体重
        if (!_weightUnit.value) {
            // LB模式
            if (_weight.value.toFloat() !in 2f..551f) {
                _weight.value = "551.00"
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.weight_out_of_range_2_551_lb,
                )
            }
        } else {
            // KG模式
            if (_weight.value.toFloat() !in 1f..250f) {
                _weight.value = "250.00"
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.weight_out_of_range_2_250_kg,
                )
            }
        }

        // 校验身高
        if (!_heightUnit.value) {
            // 英制 ft/in
            if (_heightFt.value !in 1..8) {
                _heightFt.value = 8
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.height_out_of_range_1_8_ft,
                )
            }
            if (_heightIn.value !in 0..11) {
                _heightIn.value = 11
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.height_out_of_range_1_11_in,
                )
            }
        } else {
            // 公制 cm
            if (_height.value !in 1f..250f) {
                _height.value = 170f
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.height_out_of_range_1_250_cm,
                )
            }
        }
        return CheckResult(pass = true, toastMsgRes = null)
    }

    // 4. 完整计算BMI、填充实体信息
    fun computeFullBmi(context: Context): BmiEntity {
        if (_heightUnit.value) {
            val showText = String.format("%.1f", _height.value)
            if (showText == heightPair.second) {
                _heightFt.value = heightPair.first / 12
                _heightIn.value = heightPair.first % 12
            } else {
                val totalInch = (_height.value / 2.54f).toInt()
                _heightFt.value = totalInch / 12
                _heightIn.value = totalInch % 12
            }
        } else {
            _height.value =
                ((_heightFt.value * 12) + _heightIn.value) * 2.54f
        }

        var weightKg = _weight.value.toFloat()
        if (!_weightUnit.value) weightKg *= 0.45359236f
        var heightM = _height.value / 100f
        if (!_heightUnit.value) heightM =
            ((_heightFt.value * 12) + _heightIn.value) * 2.54f / 100f

        val bmi = weightKg / (heightM * heightM)

        val bmiLevel =
            BmiUtil.getBmiFullInfo(context, _age.value, _gender.value, bmi)
        val bmiColor = ContextCompat.getColor(context, bmiLevel.colorInt)

        return BmiEntity(
            id = 0,
            weight = _weight.value.toFloat(),
            weightUnit = _weightUnit.value,
            height = _height.value,
            heightFt = _heightFt.value,
            heightIn = _heightIn.value,
            heightUnit = _heightUnit.value,
            bmiValue = bmi,
            bmiColor = bmiColor,
            age = _age.value,
            gender = _gender.value,
            createTime = System.currentTimeMillis(),
            customTime = TimeUtil(context).getCustomTimeStamp(
                selectYear,
                selectMonth,
                selectDay,
                selectPeriod
            )
        )
    }

    init {
        // 全局监听数据库，数据变化自动更新缓存
        viewModelScope.launch {
            repository.getLatestBmi().collect { entity ->
                if (entity != null) {
                    initRecord(entity)
                } else {
                    setAge(25)
                }
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