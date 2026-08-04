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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BmiFragmentViewModel(private val repository: BmiRepository) : ViewModel() {

    sealed class BmiIntent{
        object NavToInput: BmiIntent()
        object NavToRecent: BmiIntent()
    }
    fun processIntent(intent: BmiIntent){
        when(intent){
            is BmiIntent.NavToInput -> emitEvent(BmiEvent.NavToInput)
            is BmiIntent.NavToRecent -> emitEvent(BmiEvent.NavToRecent)
        }
    }

    data class BmiStatus(
        var age: Int = 25,
        var gender: Int = 1,
        var bmiValue: Float = 23f,
        var bmiLevelStrInt: Int = R.string.load,
        var weightStr: String = "",
        var heightStr: String = "",
        var genderStrInt: Int = R.string.load,
        val timeYear: String = "",
        val timeMonthInt: Int = R.string.load,
        val timeDay: String = "",
        var gradeList: List<Grade> = emptyList()
    )
    private val _status = MutableStateFlow(BmiStatus())
    val status: StateFlow<BmiStatus> = _status.asStateFlow()

    private fun initData(record: BmiEntity?) {

        record?.let {
            val weightStr = if (record.weightUnit) "${record.weight} kg" else "${record.weight} lb"
            val heightStr =
                if (record.heightUnit) "${record.height} cm" else "${record.heightFt}ft ${record.heightIn}in"
            val genderStrInt =
                if (record.gender == 1) R.string.male else R.string.female
            val bmiInfo = BmiUtil.getBmiFullInfo(record.age, record.gender, record.bmiValue)


            // 列表状态处理
            val gradeList = BmiUtil.getGradeList(record.age, record.gender)
            val levelIndex = if (record.age > 20) BmiUtil.getGradeIndex(bmiInfo.levelNameInt) - 1
            else BmiUtil.getGradeIndex(bmiInfo.levelNameInt) - 3
            if (levelIndex in gradeList.indices) {
                gradeList[levelIndex].isSelect = true
            }
            val timeText = TimeUtil().parseTimeStamp(record.customTime)

            _status.update { it.copy(
                age = record.age,
                gender = record.gender,
                bmiValue = record.bmiValue,
                bmiLevelStrInt = bmiInfo.levelNameInt,
                weightStr = weightStr,
                heightStr = heightStr,
                genderStrInt = genderStrInt,
                timeYear = timeText.selectYear,
                timeMonthInt = timeText.selectMonthInt,
                timeDay = timeText.selectDay,
                gradeList = gradeList
            ) }
        }
    }

    sealed class BmiEvent{
        object NavToInput : BmiEvent()
        object NavToRecent : BmiEvent()
    }

    private val _event = MutableSharedFlow<BmiEvent>()
    val event: SharedFlow<BmiEvent> = _event.asSharedFlow()

    private fun emitEvent(event: BmiEvent){
        viewModelScope.launch {
            _event.emit(event)
        }
    }

    init {
        // 全局监听数据库，数据变化自动更新缓存
        viewModelScope.launch {
            repository.getLatestBmi().collect { entity ->
                initData(entity)
            }
        }
    }

    companion object {
        fun provideFactory(repository: BmiRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    BmiFragmentViewModel(repository)
                }
            }
    }
}