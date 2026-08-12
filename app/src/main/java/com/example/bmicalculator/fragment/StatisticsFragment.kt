package com.example.bmicalculator.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.FragmentStatisticsBinding
import com.example.bmicalculator.ui.MainActivity
import com.example.bmicalculator.viewmodel.StatisticsFragmentViewModel
import kotlinx.coroutines.launch

class StatisticsFragment : Fragment() {
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = checkNotNull(_binding)
    private val viewModel: StatisticsFragmentViewModel by viewModels {
        val db = BmiDatabase.getDatabase(requireContext())
        StatisticsFragmentViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.statisticsCompose.apply {

            setContent {
                StatisticsScreen(viewModel)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is StatisticsFragmentViewModel.StatisticsEffect.InputPageEffect -> {
                            val mainActivity = requireActivity() as MainActivity
//                            mainActivity.binding.mainViewpage2.currentItem = 0
                        }
                    }
                }
            }
        }
    }

}