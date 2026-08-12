package com.example.bmicalculator.ui.setting

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.ui.BaseActivity
import com.example.bmicalculator.viewmodel.FeedbackViewModel
import kotlinx.coroutines.launch

class FeedbackActivity : BaseActivity() {

    private val viewmodel : FeedbackViewModel by viewModels {
        FeedbackViewModel.provideFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FeedbackScreen(viewmodel)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewmodel.effect.collect { effect ->
                    when(effect){
                        is FeedbackViewModel.FeedbackEffect.NavToCommit -> {
                            finish()
                            Toast.makeText(
                                this@FeedbackActivity,
                                getString(R.string.feedback),
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                        is FeedbackViewModel.FeedbackEffect.NavToBack ->{
                            finish()
                        }
                    }
                }
            }
        }
    }
}