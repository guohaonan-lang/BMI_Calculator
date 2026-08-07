package com.example.bmicalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.databinding.ActivityLanguageBinding
import com.example.bmicalculator.util.LangHelper
import com.example.bmicalculator.viewmodel.LanguageViewModel
import kotlinx.coroutines.launch

class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    private val viewModel: LanguageViewModel by viewModels {
        LanguageViewModel.provideFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.languageCompose.apply {

            setContent {
                LanguageScreen(viewModel)
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.event.collect {event ->
                    when(event){
                        is LanguageViewModel.LanguageEvent.NavToBack -> finish()
                        is LanguageViewModel.LanguageEvent.SwitchChinese -> {
                            switchLanguage(this@LanguageActivity, LangHelper.LANG_ZH)
                        }
                        is LanguageViewModel.LanguageEvent.SwitchEnglish -> {
                            switchLanguage(this@LanguageActivity, LangHelper.LANG_EN)
                        }
                    }
                }
            }
        }


    }

    override fun inflateBinding(inflater: LayoutInflater): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(inflater)
    }


}