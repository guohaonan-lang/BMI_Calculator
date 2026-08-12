package com.example.bmicalculator.ui.setting

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.ui.BaseActivity
import com.example.bmicalculator.viewmodel.SettingViewModel
import kotlinx.coroutines.launch

class SettingActivity : BaseActivity() {
    private val viewModel: SettingViewModel by viewModels {
        val db = BmiDatabase.getDatabase(this)
        SettingViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SettingScreen(viewModel)
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is SettingViewModel.SettingEffect.NavToLanguage -> {
                            val intent = Intent(this@SettingActivity, LanguageActivity::class.java)
                            startActivity(intent)
                        }

                        is SettingViewModel.SettingEffect.NavToFeedback -> {
                            val intent = Intent(this@SettingActivity, FeedbackActivity::class.java)
                            startActivity(intent)
                        }

                        is SettingViewModel.SettingEffect.NavToBack -> {
                            finish()
                        }
                    }
                }
            }
        }

    }
}