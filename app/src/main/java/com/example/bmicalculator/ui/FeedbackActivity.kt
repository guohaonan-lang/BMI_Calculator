package com.example.bmicalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bmicalculator.R
import com.example.bmicalculator.databinding.ActivityFeedbackBinding

class FeedbackActivity : BaseActivity<ActivityFeedbackBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): ActivityFeedbackBinding {
        return ActivityFeedbackBinding.inflate(inflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.feedbackBack.setOnClickListener {
            finish()
        }
        binding.resultSave.setOnClickListener {
            finish()
            Toast.makeText(
                this,
                getString(R.string.feedback),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}