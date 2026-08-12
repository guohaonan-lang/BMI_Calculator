package com.example.bmicalculator.ui

import android.content.Intent
import android.os.Bundle
import android.view.animation.PathInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.ActivitySplashBinding
import com.example.bmicalculator.ui.Home.MainActivity
import com.example.bmicalculator.util.LangHelper
import com.example.bmicalculator.viewmodel.SplashViewModel
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels {
        val db = BmiDatabase.getDatabase(this)
        SplashViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.navigationBarColor = ContextCompat.getColor(this, R.color.blue)//导航栏颜色

        // 初始化语言
        val savedLang = LangHelper.getSavedLang(this)
        LangHelper.setLanguage(this, savedLang)


        // ========== 方案A：初始化Compose容器 ==========
        binding.composeView.setContent {
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