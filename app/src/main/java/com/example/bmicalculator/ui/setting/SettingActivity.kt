package com.example.bmicalculator.ui.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.ActivitySettingBinding
import com.example.bmicalculator.ui.BaseActivity
import com.example.bmicalculator.ui.setting.FeedbackActivity
import com.example.bmicalculator.ui.setting.LanguageActivity
import com.example.bmicalculator.viewmodel.SettingViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class SettingActivity : BaseActivity<ActivitySettingBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(inflater)
    }

    private val viewModel: SettingViewModel by viewModels {
        val db = BmiDatabase.Companion.getDatabase(this)
        SettingViewModel.Companion.provideFactory(BmiRepository(db.bmiDao()))
    }
    private lateinit var userBottomSheetDialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.settingCompose.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                SettingScreen(viewModel)
            }
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