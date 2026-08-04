package com.example.bmicalculator.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
class InputViewModel(private val repository: BmiRepository) : ViewModel() {

    sealed class DataInputIntent {
        data class SetAge(val age: Int) : DataInputIntent()
        data class SetTime1(val year: String, val month: Int, val day: String) :
            DataInputIntent()

        data class SetTime2(val period: Int) : DataInputIntent()
        data class SetWeight(val weight: String) : DataInputIntent()
        data class SetHeight(val height: String) : DataInputIntent()
        data class SetHeightFt(val height: String) : DataInputIntent()
        data class SetHeightIn(val height: String) : DataInputIntent()
        data class SetGender(val gender: Int) : DataInputIntent()
        data class ComputeFullBmi(val color: Int, val cusTime: Long) : DataInputIntent()
        object SwitchWeightUnitToKg : DataInputIntent()
        object SwitchWeightUnitToLb : DataInputIntent()
        object SwitchHeightUnitToFtIn : DataInputIntent()
        object SwitchHeightUnitToCm : DataInputIntent()
        object CheckInputValid : DataInputIntent()
    }

    // 处理意图
    // 统一入口：View 发送的所有意图都走这里
    fun processIntent(intent: DataInputIntent) {
        when (intent) {
            is DataInputIntent.SetAge -> setAge(intent.age)
            is DataInputIntent.SetTime1 -> setTime1(intent.year, intent.month, intent.day)
            is DataInputIntent.SetTime2 -> setTime2(intent.period)
            is DataInputIntent.SetWeight -> setWeight(intent.weight)
            is DataInputIntent.SetHeight -> setHeight(intent.height)
            is DataInputIntent.SetHeightFt -> setHeightFt(intent.height)
            is DataInputIntent.SetHeightIn -> setHeightIn(intent.height)
            is DataInputIntent.SetGender -> setGender(intent.gender)
            is DataInputIntent.ComputeFullBmi -> computeFullBmi(intent.color, intent.cusTime)
            is DataInputIntent.SwitchWeightUnitToKg -> switchWeightUnitToKg()
            is DataInputIntent.SwitchWeightUnitToLb -> switchWeightUnitToLb()
            is DataInputIntent.SwitchHeightUnitToFtIn -> switchHeightUnitToFtIn()
            is DataInputIntent.SwitchHeightUnitToCm -> switchHeightUnitToCm()
            is DataInputIntent.CheckInputValid -> checkInputValid()
        }
    }

    // 用户列表状态
    data class UserListState(
        var weight: String = "140.00",
        var weightUnit: Boolean = false,

        var height: String = "170.0",
        var heightFt: String = "5",
        var heightIn: String = "7",
        var heightUnit: Boolean = false,

        // BMI 计算结果
        var bmiColor: Int = 0,//对应颜色
        // 年龄
        var age: Int = 25,
        // 性别 0女 / 1男
        var gender: Int = 1,

        // 自定义记录时间（用户手动选择的日期时间戳）
        var customTime: Long = 0,
        var timeYear: String = "114514",
        var timeMonthInt: Int = R.string.load,
        var timeDay: String = "114514",

        var timePeriodInt: Int = R.string.load,
        var isFirstData: Boolean = true,
    ) {
        val bmiValue: Float
            get() {
                // 1. 统一转换成 kg
                val weightKg = if (!weightUnit) {
                    weight.toFloat() * 0.45359237f // 磅转kg
                } else {
                    weight.toFloat()
                }

                // 2. 统一转换成 m
                val heightM = if (!heightUnit) {
                    // ft + in → cm → m
                    val cm = (heightFt.toIntOrNull() ?: 1) * 30.48f + (heightFt.toIntOrNull()
                        ?: 0) * 2.54f
                    cm / 100f
                } else {
                    height.toFloat() / 100f
                }

                // 防止除0（身高0异常保护）
                if (heightM <= 0f) return 0f

                return weightKg / (heightM * heightM)
            }
    }

    // 初始状态
    private val initialState = UserListState()

    // 状态流
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<UserListState> = _state.asStateFlow()


    sealed class CheckEvent {
        // Toast 提示
        data class ShowToast(val msgResId: Int?) : CheckEvent()

        // 跳转结果页
        data class NavToResult(val bmiEntity: BmiEntity, val isFirst: Boolean) : CheckEvent()
    }

    // 一次性事件，SharedFlow 避免重组重复消费
    private val _event = MutableSharedFlow<CheckEvent>()
    val event = _event.asSharedFlow()


    var createTime: Long = 0
    private var weightPair: Pair<String, String> = "140.00" to "63.50"
    private var heightPair: Pair<Int, String> = 67 to "170.00"


    // 初始化赋值
    private fun initRecord(record: BmiEntity) {
        if (record.createTime == createTime) return
        else createTime = record.createTime
        setGender(record.gender)
        setWeightThumb(record.weightUnit)
        setWeight(record.weight.toString())
        setHeightThumb(record.heightUnit)
        setHeight(record.height.toString())
        setHeightFt(record.heightFt.toString())
        setHeightIn(record.heightIn.toString())
        setAge(record.age)
        setCustomTime(record.customTime)
    }

    // 用户列表意图

    private fun setCustomTime(t: Long) {
        _state.update { it.copy(customTime = t) }
    }

    private fun setAge(newAge: Int) {
        _state.update { it.copy(age = newAge) }
    }

    private fun setTime1(year: String, month: Int, day: String) {
        _state.update {
            it.copy(
                timeYear = year,
                timeMonthInt = month,
                timeDay = day
            )
        }
    }

    private fun setTime2(period: Int) {
        _state.update { it.copy(timePeriodInt = period) }
    }

    private fun setWeight(weight: String) {
        _state.update { it.copy(weight = weight) }
    }

    private fun setHeight(height: String) {
        _state.update { it.copy(height = height) }
    }

    private fun setHeightFt(height: String) {
        _state.update { it.copy(heightFt = height) }
    }

    private fun setHeightIn(height: String) {
        _state.update { it.copy(heightIn = height) }
    }

    private fun setGender(gender: Int) {
        _state.update { it.copy(gender = gender) }
    }

    private fun setWeightThumb(unit: Boolean) {
        _state.update { it.copy(weightUnit = unit) }
    }

    private fun setHeightThumb(unit: Boolean) {
        _state.update { it.copy(heightUnit = unit) }
    }

    // 1. 体重单位切换逻辑：lb <-> kg
    private fun switchWeightUnitToKg() {

        if (_state.value.weightUnit) return
        var weight = _state.value.weight.toFloat()
        if (_state.value.weight != weightPair.first) {
            val originWeight = _state.value.weight
            weight *= 0.4536f
            _state.value.weight = String.format("%.2f", weight)
            val newPair = originWeight to _state.value.weight
            weightPair = newPair
        } else _state.value.weight = weightPair.second
        setWeightThumb(true)
    }


    private fun switchWeightUnitToLb() {
        if (!_state.value.weightUnit) return
        var weight = _state.value.weight.toFloat()
        if (_state.value.weight != weightPair.second) {
            val originWeight = _state.value.weight
            weight /= 0.4536f
            _state.value.weight = String.format("%.2f", weight)
            val newPair = _state.value.weight to originWeight
            weightPair = newPair
        } else _state.value.weight = weightPair.first
        setWeightThumb(false)

    }

    // 2. 身高单位切换逻辑：cm <-> ft·in
    private fun switchHeightUnitToFtIn() {

        if (!_state.value.heightUnit) return
        val showText = _state.value.height
        if (showText != heightPair.second) {
            val originHeight = _state.value.height
            val totalInch = (_state.value.height.toFloat() / 2.54f).toInt()
            _state.value.heightFt = (totalInch / 12).toString()
            _state.value.heightIn = (totalInch % 12).toString()
            val newPair = totalInch to originHeight
            heightPair = newPair
        }
        setHeightThumb(false)
    }


    private fun switchHeightUnitToCm() {
        if (_state.value.heightUnit) return
        var showText: String
        val hft = (_state.value.heightFt.toIntOrNull() ?: 1)
        val hin = (_state.value.heightIn.toIntOrNull() ?: 0)
        val totalInch = hft * 12 + hin
        if (totalInch != heightPair.first) {
            showText = String.format(
                "%.1f",
                ((hft * 12) + hin) * 2.54f
            )
            _state.value.height = showText
            val newPair = totalInch to showText
            heightPair = newPair
        }
        setHeightThumb(true)

    }

    //检查数值合法
    data class CheckResult(
        val pass: Boolean,
        val toastMsgRes: Int?, // 提示文案资源ID
    )

    private fun sendEvent(event: CheckEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

    fun checkInputValid(): CheckResult {
        // 校验体重
        val w = (_state.value.weight.toFloatOrNull() ?: 0f)
        if (!_state.value.weightUnit) {
            // LB模式
            if (w !in 2f..551f) {
                _state.update { it.copy(weight = "551.00") }
                sendEvent(CheckEvent.ShowToast(msgResId = R.string.weight_out_of_range_2_551_lb))
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.weight_out_of_range_2_551_lb,
                )
            }
        } else {
            // KG模式
            if (w !in 1f..250f) {
                _state.update { it.copy(weight = "250.00") }
                sendEvent(CheckEvent.ShowToast(msgResId = R.string.weight_out_of_range_2_250_kg))
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.weight_out_of_range_2_250_kg,
                )
            }
        }

        // 校验身高
        val hft = (_state.value.heightFt.toIntOrNull() ?: 0)
        val hin = (_state.value.heightIn.toIntOrNull() ?: 0)
        if (!_state.value.heightUnit) {
            // 英制 ft/in
            if (hft !in 1..8) {
                _state.update { it.copy(heightFt = "8") }
                sendEvent(CheckEvent.ShowToast(R.string.height_out_of_range_1_8_ft))
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.height_out_of_range_1_8_ft,
                )
            }
            if (hin !in 0..11) {
                _state.update { it.copy(heightIn = "11") }
                sendEvent(CheckEvent.ShowToast(R.string.height_out_of_range_1_11_in))
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.height_out_of_range_1_11_in,
                )
            }
        } else {
            // 公制 cm
            if (_state.value.height.toFloat() !in 1f..250f) {
                _state.update { it.copy(height = "170.0") }
                sendEvent(CheckEvent.ShowToast(R.string.height_out_of_range_1_250_cm))
                return CheckResult(
                    pass = false,
                    toastMsgRes = R.string.height_out_of_range_1_250_cm,
                )
            }
        }
        return CheckResult(pass = true, toastMsgRes = null)
    }

    // 4. 完整计算BMI、填充实体信息
    private fun computeFullBmi(bmiColor: Int, customTime: Long) {
        val bmi = _state.value.bmiValue


        val bmiData =
            BmiEntity(
                id = 0,
                weight = _state.value.weight.toFloat(),
                weightUnit = _state.value.weightUnit,
                height = _state.value.height.toFloat(),
                heightFt = _state.value.heightFt.toIntOrNull()?:1,
                heightIn = _state.value.heightIn.toIntOrNull()?:0,
                heightUnit = _state.value.heightUnit,
                bmiValue = bmi,
                bmiColor = bmiColor,
                age = _state.value.age,
                gender = _state.value.gender,
                createTime = System.currentTimeMillis(),
                customTime = customTime
            )
        sendEvent(CheckEvent.NavToResult(bmiData, _state.value.isFirstData))
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