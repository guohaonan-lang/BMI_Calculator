package com.example.bmicalculator.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bmicalculator.fragment.BmiFragment
import com.example.bmicalculator.fragment.DataInputFragment
import com.example.bmicalculator.fragment.StatisticsFragment
import com.example.bmicalculator.ui.MainActivity

class HomeAdapter(activity: MainActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> DataInputFragment()
            1 -> BmiFragment()
            2 -> StatisticsFragment()
            else -> BmiFragment()
        }
    }
}