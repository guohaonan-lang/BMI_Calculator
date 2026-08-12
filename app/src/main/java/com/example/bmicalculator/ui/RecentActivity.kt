package com.example.bmicalculator.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.viewmodel.RecentViewModel
import kotlinx.coroutines.launch

class RecentActivity : BaseActivity() {

    //创建viewmodel
    private val viewModel: RecentViewModel by viewModels {
        val db = BmiDatabase.getDatabase(this)
        RecentViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RecentScreen(viewModel)
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is RecentViewModel.RecentEffect.NavToBack -> finish()
                        is RecentViewModel.RecentEffect.NavToResult -> {
                            val intent = Intent(this@RecentActivity, ResultActivity::class.java)
                            intent.putExtra("BMI", effect.record)
                            intent.putExtra("Recent", true)
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}