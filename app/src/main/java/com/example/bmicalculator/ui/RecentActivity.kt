package com.example.bmicalculator.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bmicalculator.R
import com.example.bmicalculator.adapter.RecentAdapter
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.ActivityRecentBinding
import com.example.bmicalculator.viewmodel.BmiViewModel
import kotlinx.coroutines.launch

class RecentActivity : BaseActivity<ActivityRecentBinding>() {

    override fun inflateBinding(inflater: LayoutInflater): ActivityRecentBinding {
        return ActivityRecentBinding.inflate(inflater)
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecentAdapter

    //创建viewmodel
    private val viewModel: BmiViewModel by viewModels {
        val db = BmiDatabase.getDatabase(this)
        BmiViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val back = binding.recentBack
        back.setOnClickListener {
            finish()
        }
        setupRecyclerView()

    }

    //初始化列表
    private fun setupRecyclerView() {
        recyclerView = binding.recentRv

        adapter = RecentAdapter(emptyList())
        lifecycleScope.launch {
            viewModel.allBmiList.collect { data ->
                if (data.isEmpty()) {
                    val intent = Intent(this@RecentActivity, DataInputActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                    return@collect
                }
                adapter.update(data)
            }
        }
        adapter.setOnItemClick { item ->
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("BMI", item)
            intent.putExtra("Recent", true)
            startActivity(intent)
        }

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }
}