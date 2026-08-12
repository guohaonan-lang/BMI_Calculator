package com.example.bmicalculator.ui.Home

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bmicalculator.R
import com.example.bmicalculator.databinding.ActivityMainBinding
import com.example.bmicalculator.ui.BaseActivity
import com.example.bmicalculator.viewmodel.MainViewModel

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Companion.provideFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            //顶部状态栏：不要给top加 padding，否则背景上不去
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        binding.mainCompose.apply {
            setContent {
                MainScreen()
            }
        }

    }

}