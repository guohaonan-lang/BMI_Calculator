package com.example.bmicalculator.ui.setting

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.ui.BaseActivity
import com.example.bmicalculator.util.LangHelper
import com.example.bmicalculator.viewmodel.LanguageViewModel
import kotlinx.coroutines.launch

class LanguageActivity : BaseActivity() {
    private val viewModel: LanguageViewModel by viewModels {
        LanguageViewModel.provideFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LanguageScreen(viewModel)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is LanguageViewModel.LanguageEffect.NavToBack -> finish()
                        is LanguageViewModel.LanguageEffect.SwitchChinese -> {
                            switchLanguage(this@LanguageActivity, LangHelper.LANG_ZH)
                        }

                        is LanguageViewModel.LanguageEffect.SwitchEnglish -> {
                            switchLanguage(this@LanguageActivity, LangHelper.LANG_EN)
                        }
                    }
                }
            }
        }


    }

}