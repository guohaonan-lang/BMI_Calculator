package com.example.bmicalculator.viewmodel

import android.content.Context
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
            is SettingIntent.NavToLanguage -> emitEvent(SettingEffect.NavToLanguage)
            is SettingIntent.NavToFeedback -> emitEvent(SettingEffect.NavToFeedback)
            is SettingIntent.UserLoad -> loadUser(true)
            is SettingIntent.UserUnload -> loadUser(false)
            is SettingIntent.NavToBack -> emitEvent(SettingEffect.NavToBack)
        }
    }

    data class SettingState(
        var userLoading: Boolean = false,
        var userName: String = "Cassie",
        var userEmail: String = "cassiexiao@gmail.com",
    )

    sealed class SettingEffect {
        object NavToLanguage : SettingEffect()
        object NavToFeedback : SettingEffect()
        object NavToBack : SettingEffect()
    }

    private val _effect = MutableSharedFlow<SettingEffect>()
    val effect: SharedFlow<SettingEffect> = _effect.asSharedFlow()
    private val _state = MutableStateFlow(SettingState())
    val state: StateFlow<SettingState> = _state.asStateFlow()


    private fun loadUser(load: Boolean) {
        _state.value = _state.value.copy(
            userLoading = load
        )

    }

    private fun emitEvent(event: SettingEffect) {
        viewModelScope.launch {
            _effect.emit(event)
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