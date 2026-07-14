package com.example.bmicalculator.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bmicalculator.R
import com.example.bmicalculator.databinding.ActivityLanguageBinding
import com.example.bmicalculator.util.LangHelper

class LanguageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLanguageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val savedLang = LangHelper.getSavedLang(this)
        when (savedLang) {
            LangHelper.LANG_EN -> {
                binding.chineseCheck.visibility = View.GONE
                binding.englishCheck.visibility = View.VISIBLE
            }
            LangHelper.LANG_ZH -> {
                binding.englishCheck.visibility = View.GONE
                binding.chineseCheck.visibility = View.VISIBLE
            }
        }

        binding.languageEnglish.setOnClickListener {
            binding.chineseCheck.visibility = View.GONE
            binding.englishCheck.visibility = View.VISIBLE
            LangHelper.setLanguage(this, LangHelper.LANG_EN)

            val intent = this.packageManager.getLaunchIntentForPackage(this.packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            this.finish()

        }
        binding.languageChinese.setOnClickListener {
            binding.englishCheck.visibility = View.GONE
            binding.chineseCheck.visibility = View.VISIBLE
            LangHelper.setLanguage(this, LangHelper.LANG_ZH)
            
            val intent = this.packageManager.getLaunchIntentForPackage(this.packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            this.finish()
        }
    }
}