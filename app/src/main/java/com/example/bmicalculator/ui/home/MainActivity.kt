package com.example.bmicalculator.ui.home

import android.os.Bundle
import androidx.activity.compose.setContent
import com.example.bmicalculator.ui.BaseActivity

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainScreen()
        }

    }

}