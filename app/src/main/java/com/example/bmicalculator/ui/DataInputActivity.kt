package com.example.bmicalculator.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.ActivityDataInputBinding
import com.example.bmicalculator.ui.Home.InputScreen
import com.example.bmicalculator.viewmodel.InputFragmentViewModel
import kotlinx.coroutines.launch

class DataInputActivity : BaseActivity<ActivityDataInputBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): ActivityDataInputBinding {
        return ActivityDataInputBinding.inflate(inflater)
    }

    private val viewModel: InputFragmentViewModel by viewModels {
        val db = BmiDatabase.getDatabase(this)
        InputFragmentViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            val bar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            insets
        }
        binding.inputCompose.apply {
            setContent {
                InputScreen(viewModel)
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is InputFragmentViewModel.InputEffect.ShowToast ->
                            effect.msgResId?.let { id ->
                                val str = this@DataInputActivity.getString(id)
                                Toast.makeText(this@DataInputActivity, str, Toast.LENGTH_SHORT)
                                    .show()
                            }

                        is InputFragmentViewModel.InputEffect.NavToResult -> {
                            val intent = Intent(this@DataInputActivity, ResultActivity::class.java)
                            intent.putExtra("BMI", effect.bmiEntity)
                            intent.putExtra("FATHER", effect.isFirst)
                            this@DataInputActivity.startActivity(intent)
                        }
                    }
                }
            }
        }
    }


}