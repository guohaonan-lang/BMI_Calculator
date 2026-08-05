package com.example.bmicalculator.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.ActivityRecentBinding
import com.example.bmicalculator.viewmodel.RecentViewModel
import kotlinx.coroutines.launch

class RecentActivity : BaseActivity<ActivityRecentBinding>() {

    override fun inflateBinding(inflater: LayoutInflater): ActivityRecentBinding {
        return ActivityRecentBinding.inflate(inflater)
    }

    //创建viewmodel
    private val viewModel: RecentViewModel by viewModels {
        val db = BmiDatabase.getDatabase(this)
        RecentViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.recentCompose.apply {

            setContent {
                RecentScreen(viewModel)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.event.collect { event ->
                    when(event){
                        is RecentViewModel.RecentEvent.NavToBack -> finish()
                        is RecentViewModel.RecentEvent.NavToResult -> {
                            val intent = Intent(this@RecentActivity, ResultActivity::class.java)
                            intent.putExtra("BMI", event.record)
                            intent.putExtra("Recent", true)
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}