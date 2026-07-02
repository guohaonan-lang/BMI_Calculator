package com.example.bmicalculator.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bmicalculator.databinding.FragmentBmiBinding
import com.example.bmicalculator.ui.MainActivity
import com.example.bmicalculator.ui.RecentActivity

class BmiFragment : Fragment() {

    private var _binding: FragmentBmiBinding? = null
    private val binding get() = checkNotNull(_binding) { "视图已销毁，禁止访问Binding" }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBmiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resultContent.setOnClickListener {
            val mainActivity = requireActivity() as MainActivity
            mainActivity.binding.mainViewpage2.currentItem = 0
        }

        binding.recent.setOnClickListener {
            val intent = Intent(requireContext(), RecentActivity::class.java)
            startActivity(intent)
        }
    }

}