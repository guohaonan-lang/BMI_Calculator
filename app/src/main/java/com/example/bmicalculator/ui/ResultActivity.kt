package com.example.bmicalculator.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.ui.home.MainActivity
import com.example.bmicalculator.viewmodel.ResultViewModel
import kotlinx.coroutines.launch

class ResultActivity : BaseActivity() {
    private val viewModel: ResultViewModel by viewModels {
        val db = BmiDatabase.getDatabase(this)
        ResultViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initData()

        setContent {
            ResultScreen(viewModel)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is ResultViewModel.ResultEvent.NavToBack -> {
                            finish()
                        }

                        is ResultViewModel.ResultEvent.NavToMain -> {
                            val intent =
                                Intent(this@ResultActivity, MainActivity::class.java).apply {
                                    flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            startActivity(intent)
                            finish()
                        }

                        is ResultViewModel.ResultEvent.NavToStart -> {
                            val intent =
                                Intent(this@ResultActivity, DataInputActivity::class.java).apply {
                                    flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }


    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun initData() {
        // 1.读取数据
        val bmiRecord = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("BMI", BmiEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("BMI")
        }
        if (bmiRecord != null) {
            viewModel.processIntent(ResultViewModel.ResultIntent.UpdateRecord(bmiRecord!!))
        }
        val statusFirst = intent.getBooleanExtra("FATHER", false)
        val statusRecent = intent.getBooleanExtra("Recent", false)

        viewModel.processIntent(
            ResultViewModel.ResultIntent.UpdateStatus(
                statusFirst,
                statusRecent
            )
        )
        viewModel.initDataFromIntent(
            record = bmiRecord,
        )
    }

}