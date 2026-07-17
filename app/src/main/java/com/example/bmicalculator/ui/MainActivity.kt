package com.example.bmicalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.bmicalculator.R
import com.example.bmicalculator.adapter.HomeAdapter
import com.example.bmicalculator.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        viewPager2.setCurrentItem(1, false)
    }

    private fun setupViewPage2() {
        val adapter = HomeAdapter(this)
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
                    tvText.text = getString(R.string.title_calculate)
                    ivIcon.setImageResource(R.drawable.tab_calculator)
                }
                1 -> {
                    tvText.text = "BMI"
                    ivIcon.setImageResource(R.drawable.tab_bmi)
                }
                2 -> {
                    tvText.text = getString(R.string.statistics)
                    ivIcon.setImageResource(R.drawable.tab_discover)
                }
            }
            tab.customView = tabView
        }.attach()
        // 监听Tab选中切换图标
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectView = tab?.customView
                val icon = selectView?.findViewById<ImageView>(R.id.tab_iv)
                icon?.alpha = 1f
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val unSelectView = tab?.customView
                val icon = unSelectView?.findViewById<ImageView>(R.id.tab_iv)
                icon?.alpha = 0.5f
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}