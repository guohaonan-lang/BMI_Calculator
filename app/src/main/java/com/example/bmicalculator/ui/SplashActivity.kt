package com.example.bmicalculator.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.ui.home.MainActivity
import com.example.bmicalculator.util.LangHelper
import com.example.bmicalculator.viewmodel.SplashViewModel
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {
    private val viewModel: SplashViewModel by viewModels {
        val db = BmiDatabase.getDatabase(this)
        SplashViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.navigationBarColor = ContextCompat.getColor(this, R.color.blue)//导航栏颜色

        // 初始化语言
        val savedLang = LangHelper.getSavedLang(this)
        LangHelper.setLanguage(this, savedLang)


        setContent {
            MaterialTheme() {
                SplashScreen(viewModel)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {

                        is SplashViewModel.SplashEffect.NavInputActEffect -> {
                            val intent = Intent(this@SplashActivity, DataInputActivity::class.java)
                            startActivity(intent)
                            finish()
                        }

                        is SplashViewModel.SplashEffect.NavInputFraEffect -> {
                            val intent = Intent(this@SplashActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }
}