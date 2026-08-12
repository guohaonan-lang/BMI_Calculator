package com.example.bmicalculator.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.FragmentBmiBinding
import com.example.bmicalculator.ui.MainActivity
import com.example.bmicalculator.ui.RecentActivity
import com.example.bmicalculator.viewmodel.BmiFragmentViewModel
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n", "DefaultLocale")
class BmiFragment : Fragment() {

    private var _binding: FragmentBmiBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val viewModel: BmiFragmentViewModel by viewModels {
        val db = BmiDatabase.getDatabase(requireContext())
        BmiFragmentViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBmiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.BmiCompose.apply {

            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                BmiScreen(viewModel)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is BmiFragmentViewModel.BmiEffect.NavToInput -> {
                            val mainActivity = requireActivity() as MainActivity
//                            mainActivity.binding.mainViewpage2.currentItem = 0
                        }

                        is BmiFragmentViewModel.BmiEffect.NavToRecent -> {
                            val intent = Intent(requireContext(), RecentActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}