package com.example.bmicalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.adapter.HomeAdapter
import com.example.bmicalculator.databinding.ActivityMainBinding
import com.example.bmicalculator.viewmodel.MainViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.provideFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            //顶部状态栏：不要给top加 padding，否则背景上不去
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        setupViewPage2()
        setupTableLayout()
        initDataFlow()

    }

    private fun initDataFlow() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bmi.collect { item ->
                    binding.mainViewpage2.setCurrentItem(item, false)
                    refreshTabIconState(item)
                }
            }
        }
    }

    private fun setupViewPage2() {
        val homeAdapter = HomeAdapter(this)
        binding.mainViewpage2.apply {
            adapter = homeAdapter
            offscreenPageLimit = 1
            isUserInputEnabled = false
        }
    }

    private fun setupTableLayout() {
        TabLayoutMediator(binding.mainTable, binding.mainViewpage2) { tab, position ->
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
        binding.mainTable.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: return
                // 更新viewmodel的bmi
                viewModel.setBmi(position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    private fun refreshTabIconState(selectedPos: Int) {
        for (i in 0 until binding.mainTable.tabCount) {
            val tab = binding.mainTable.getTabAt(i) ?: continue
            val iv = tab.customView?.findViewById<ImageView>(R.id.tab_iv)
            if (i == selectedPos) {
                iv?.alpha = 1f
            } else {
                iv?.alpha = 0.5f
            }
        }
    }

}