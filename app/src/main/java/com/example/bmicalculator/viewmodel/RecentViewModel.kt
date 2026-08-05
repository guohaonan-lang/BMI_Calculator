package com.example.bmicalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.util.BmiUtil
import com.example.bmicalculator.util.TimeParseResult
import com.example.bmicalculator.util.TimeUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecentViewModel(repository: BmiRepository) :
    ViewModel() {

    sealed class RecentIntent{
        object BackPage : RecentIntent()
        data class ResultPage(val record: BmiEntity) : RecentIntent()
    }
    fun process(intent: RecentIntent){
        when(intent){
            is RecentIntent.BackPage -> emitEvent( RecentEvent.NavToBack)
            is RecentIntent.ResultPage -> emitEvent( RecentEvent.NavToResult(intent.record))
        }

    }

    val allBmiList : Flow<List<BmiEntity>> = repository.getAllBmiRecords()
    init {
        // 在init块中收集flow，viewModelScope自动跟随ViewModel生命周期销毁
        allBmiList
            // 对每一次数据库返回的list做转换，entity → BmiRecordUi
            .map { entityList ->
                entityList.map { entity ->
                    // 逐条处理每一条BmiEntity
                    val bmiDisplay = "%.1f".format(entity.bmiValue)
                    val (levelName, colorRes) = BmiUtil.getBmiFullInfo(entity.age, entity.gender, entity.bmiValue)
                    val timeShow = TimeUtil().parseTimeStamp(entity.customTime)

                    BmiRecordUi(
                        bmiRecord = entity,
                        bmiDisplayText = bmiDisplay,
                        levelTextInt = levelName,
                        colorResId = colorRes,
                        timeDisplayText = timeShow,
                        id = entity.id
                    )
                }
            }
            // 收到转换完成的ui列表，更新uiState
            .onEach { uiItemList ->
                _uiState.update {
                    it.copy(recordUiList = uiItemList)
                }
            }
            .launchIn(viewModelScope) // 使用viewModelScope，ViewModel销毁自动取消订阅
    }
    // UI展示模型
    data class BmiRecordUi(
        val id: Long,
        val bmiDisplayText: String,
        val levelTextInt: Int,
        val colorResId: Int,
        val timeDisplayText: TimeParseResult,
        val bmiRecord: BmiEntity
    )

    data class BmiUiState(
        val recordUiList: List<BmiRecordUi> = emptyList(),
    )
    // UI状态，存放处理完成后的UI展示列表
    private val _uiState = MutableStateFlow(BmiUiState())
    val uiState: StateFlow<BmiUiState> = _uiState


    sealed class RecentEvent{
        object NavToBack : RecentEvent()
        data class NavToResult(val record: BmiEntity) : RecentEvent()
    }
    private val _event = MutableSharedFlow<RecentEvent>()
    val event = _event.asSharedFlow()

    private fun emitEvent(event: RecentEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

    companion object {
        fun provideFactory(
            repository: BmiRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RecentViewModel(repository)
            }
        }
    }
}