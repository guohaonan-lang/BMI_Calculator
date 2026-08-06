package com.example.bmicalculator.viewmodel

import android.content.Context
import androidx.compose.ui.graphics.Shape
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.util.BmiFileUtil
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingViewModel(private val repository: BmiRepository) : ViewModel() {


    sealed class SettingIntent {
        data class ReadTestData(val context: Context) : SettingIntent()
        object NavToLanguage : SettingIntent()
        object NavToFeedback : SettingIntent()
        object NavToBack : SettingIntent()
        object UserLoad : SettingIntent()
        object UserUnload : SettingIntent()

    }

    fun processIntent(intent: SettingIntent) {
        when (intent) {
            is SettingIntent.ReadTestData -> readTestFile(intent.context)
            is SettingIntent.NavToLanguage -> emitEvent(SettingEvent.NavToLanguage)
            is SettingIntent.NavToFeedback -> emitEvent(SettingEvent.NavToFeedback)
            is SettingIntent.UserLoad -> LoadUser(true)
            is SettingIntent.UserUnload -> LoadUser(false)
            is SettingIntent.NavToBack -> emitEvent(SettingEvent.NavToBack)
        }
    }

    data class SettingState(
        var userLoading: Boolean = false,
        var userName: String = "Cassie",
        var userEmail: String = "cassiexiao@gmail.com",
    )

    private val _state = MutableStateFlow(SettingState())
    val state: StateFlow<SettingState> = _state.asStateFlow()


    private fun LoadUser(load: Boolean) {
        _state.value = _state.value.copy(
            userLoading = load
        )

    }

    sealed class SettingEvent {
        object NavToLanguage : SettingEvent()
        object NavToFeedback : SettingEvent()
        object NavToBack : SettingEvent()
    }

    private val _event = MutableSharedFlow<SettingEvent>()
    val event: SharedFlow<SettingEvent?> = _event.asSharedFlow()

    private fun emitEvent(event: SettingEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

    // 导入测试数据
    fun readTestFile(context: Context) {
        val appContext = context.applicationContext
        val bmiList = BmiFileUtil.readTestFile(appContext)
        viewModelScope.launch {
            for (item in bmiList) {
                item.id = 0
                val count = repository.getBmiByTime(item.createTime)
                if (count == null) {
                    repository.insertBmiRecord(item)
                }
            }
        }

    }

    // 导出数据
    fun exportFile(context: Context, list: List<BmiEntity>) {
        viewModelScope.launch {
            BmiFileUtil.exportBmiToFile(context, list)
        }
    }

    // 导入数据
    fun readFileAndImport(context: Context) {
        viewModelScope.launch {
            val bmiList = BmiFileUtil.readBmiFromFile(context)
            // 清空旧数据
//            repository.clearAll()
            // 写入数据库验证读取结果是否正确
            repository.insertBmiList(bmiList)
        }
    }

    suspend fun getAllList(): List<BmiEntity> {
        return repository.getAllBmiList()
    }

    companion object {
        fun provideFactory(repository: BmiRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    SettingViewModel(repository)
                }
            }
    }
}