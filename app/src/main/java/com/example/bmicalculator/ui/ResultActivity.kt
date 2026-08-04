package com.example.bmicalculator.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.ActivityResultBinding
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.viewmodel.ResultViewModel
import kotlinx.coroutines.launch

class ResultActivity : BaseActivity<ActivityResultBinding>() {

    override fun inflateBinding(inflater: LayoutInflater): ActivityResultBinding {
        return ActivityResultBinding.inflate(inflater)
    }

    private lateinit var alertDialog: AlertDialog

    private val viewModel: ResultViewModel by viewModels {
        val db = BmiDatabase.getDatabase(this)
        ResultViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }
    private var bmiRecord: BmiEntity? = null
    private var statusFirst: Boolean = false
    private var statusRecent: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initData()

        binding.resultCompose.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                ResultScreen(viewModel)

            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is ResultViewModel.ResultEvent.NavToBack -> {
                            finish()
                        }

                        is ResultViewModel.ResultEvent.NavToMain -> {
                            val intent =
                                Intent(this@ResultActivity, MainActivity::class.java).apply {
                                    flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            startActivity(intent)
                            finish()
                        }

                        is ResultViewModel.ResultEvent.NavToStart -> {
                            val intent =
                                Intent(this@ResultActivity, DataInputActivity::class.java).apply {
                                    flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }


    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun initData() {
        // 1.读取数据
        bmiRecord = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("BMI", BmiEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("BMI")
        }
        if (bmiRecord != null) {
            viewModel.processIntent(ResultViewModel.ResultIntent.UpdateRecord(bmiRecord!!))
        }
        statusFirst = intent.getBooleanExtra("FATHER", false)
        statusRecent = intent.getBooleanExtra("Recent", false)

        viewModel.processIntent(
            ResultViewModel.ResultIntent.UpdateStatus(
                statusFirst,
                statusRecent
            )
        )
        viewModel.initDataFromIntent(
            record = bmiRecord,
        )
    }


    //返回监听，触发delete弹窗
    val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            alertDialog.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 注销返回键监听
        onBackPressedCallback.isEnabled = false
    }
}