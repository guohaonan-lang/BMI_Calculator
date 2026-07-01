package com.example.bmicalculator.ui

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.bmicalculator.R
import com.example.bmicalculator.adapter.Homeadapter
import com.example.bmicalculator.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            //顶部状态栏：不要给top加 padding，否则背景上不去
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        tabLayout = findViewById(R.id.main_table)
        viewPager2 = findViewById(R.id.main_viewpage2)

        setupViewPage2()
        setupTableLayout()
    }

    private fun setupViewPage2() {
        val adapter = Homeadapter(this)
        viewPager2.adapter = adapter
        viewPager2.offscreenPageLimit = 1
        viewPager2.isUserInputEnabled = false

    }

    private fun setupTableLayout() {
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            val tabView = layoutInflater.inflate(R.layout.item_tab, null)
            val ivIcon = tabView.findViewById<ImageView>(R.id.tab_iv)
            val tvText = tabView.findViewById<TextView>(R.id.tab_text)

            when (position) {
                0 -> {
                    tvText.text = "Calculator"
                    ivIcon.setImageResource(R.drawable.tab_calculator)
                }
                1 -> {
                    tvText.text = "BMI"
                    ivIcon.setImageResource(R.drawable.tab_bmi)
                }
                2 -> {
                    tvText.text = "Statistics"
                    ivIcon.setImageResource(R.drawable.tab_discover)
                }
            }
            tab.customView = tabView
            // 监听Tab选中切换图标
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val pos = tab?.position ?: 0
                    val selectView = tab?.customView
                    val icon = selectView?.findViewById<ImageView>(R.id.tab_iv)
                    icon?.alpha = 1f
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    val pos = tab?.position ?: 0
                    val unSelectView = tab?.customView
                    val icon = unSelectView?.findViewById<ImageView>(R.id.tab_iv)
                    icon?.alpha = 0.5f
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })

        }.attach()
    }
}