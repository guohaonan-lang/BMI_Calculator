package com.example.bmicalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.databinding.ActivityLanguageBinding
import com.example.bmicalculator.util.LangHelper
import com.example.bmicalculator.viewmodel.LanguageActivityViewModel
import kotlinx.coroutines.launch

class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    private val viewModel: LanguageActivityViewModel by viewModels {
        LanguageActivityViewModel.provideFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        initDataFlow()

    }

    private fun initDataFlow() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedLanguage.collect { code ->
                    switchLanguage(this@LanguageActivity,code)
                    when (code) {
                        LangHelper.LANG_EN -> {
                            binding.chineseCheck.visibility = View.GONE
                            binding.englishCheck.visibility = View.VISIBLE
                        }

                        LangHelper.LANG_ZH -> {
                            binding.englishCheck.visibility = View.GONE
                            binding.chineseCheck.visibility = View.VISIBLE
                        }
                    }
                }
            }

        }
    }

    override fun inflateBinding(inflater: LayoutInflater): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(inflater)
    }

    private fun initView() {
        viewModel.loadSavedLang(this)
        binding.languageEnglish.setOnClickListener {
            binding.chineseCheck.visibility = View.GONE
            binding.englishCheck.visibility = View.VISIBLE
            viewModel.setLanguage(LangHelper.LANG_EN)
        }

        binding.languageChinese.setOnClickListener {
            binding.englishCheck.visibility = View.GONE
            binding.chineseCheck.visibility = View.VISIBLE
            viewModel.setLanguage(LangHelper.LANG_ZH)
        }

        binding.languageBack.setOnClickListener {
            finish()
        }

    }

}