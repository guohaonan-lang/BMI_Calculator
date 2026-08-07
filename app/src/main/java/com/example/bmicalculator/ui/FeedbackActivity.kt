package com.example.bmicalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.databinding.ActivityFeedbackBinding
import com.example.bmicalculator.viewmodel.FeedbackViewModel
import kotlinx.coroutines.launch

class FeedbackActivity : BaseActivity<ActivityFeedbackBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): ActivityFeedbackBinding {
        return ActivityFeedbackBinding.inflate(inflater)
    }

    private val viewmodel : FeedbackViewModel by viewModels{
        FeedbackViewModel.provideFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.feedbackCompose.apply {

            setContent {
                FeedbackScreen(viewmodel)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewmodel.event.collect { event ->
                    when(event){
                        is FeedbackViewModel.FeedbackEvent.NavToCommit -> {
                            finish()
                            Toast.makeText(
                                this@FeedbackActivity,
                                getString(R.string.feedback),
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                        is FeedbackViewModel.FeedbackEvent.NavToBack ->{
                            finish()
                        }
                    }
                }
            }
        }
    }
}