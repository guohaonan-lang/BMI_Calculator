package com.example.bmicalculator.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bmicalculator.R
import com.example.bmicalculator.adapter.RecentAdapter
import com.example.bmicalculator.model.BmiEntity

class RecentActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecentAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recent)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        setupRecyclerView()

    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recent_rv)

        adapter = RecentAdapter(
            listOf(
                BmiEntity(
                    id = 1,
                    height = 170f,
                    weight = 52f,
                    bmiValue = 17.9f,

                    age = 22,
                    gender = 1,
                    customTime = System.currentTimeMillis(),
                    timeText = "2026-06-28"
                ),
                BmiEntity(
                    id = 2,
                    height = 170f,
                    weight = 62f,
                    bmiValue = 21.4f,
                    age = 22,
                    gender = 1,
                    customTime = System.currentTimeMillis(),
                    timeText = "2026-06-29"
                ),
                BmiEntity(
                    id = 3,
                    height = 170f,
                    weight = 82f,
                    bmiValue = 28.4f,

                    age = 22,
                    gender = 1,
                    customTime = System.currentTimeMillis(),
                    timeText = "2026-06-30"
                )
            )
        )

        adapter.setOnItemClick { item ->
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("BMI",item)
            intent.putExtra("Save",false)
            startActivity(intent)
        }

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }
}