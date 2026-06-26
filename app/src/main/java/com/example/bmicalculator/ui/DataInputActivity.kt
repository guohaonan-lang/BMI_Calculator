package com.example.bmicalculator.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.bmicalculator.R
import com.example.bmicalculator.adapter.InputAgeAdapter
import com.example.bmicalculator.databinding.ActivityDataInputBinding

class DataInputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataInputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDataInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupAgeRecyclerView()
    }

    private fun setupAgeRecyclerView() {
        val ageRecyclerView = binding.mergeDateInput.inputAge
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        ageRecyclerView.layoutManager = layoutManager
        val adapter = InputAgeAdapter(ageRecyclerView){

        }
        ageRecyclerView.adapter = adapter
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(ageRecyclerView)

        // ========== 滚动透明度监听，全部放入函数内 ==========
        ageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private val minAlpha = 0f
            private val maxAlpha = 1f
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lm = recyclerView.layoutManager as LinearLayoutManager
                val rvCenterX = recyclerView.width / 2f
                val firstVisible = lm.findFirstVisibleItemPosition()
                val lastVisible = lm.findLastVisibleItemPosition()

                for (pos in firstVisible-1..lastVisible+1) {
                    val itemView = lm.findViewByPosition(pos) ?: continue
                    val itemCenterX = itemView.left + itemView.width / 2f
                    val distance = kotlin.math.abs(itemCenterX - rvCenterX)
                    val maxDistance = recyclerView.width / 2f
                    var ratio = 1 - (distance / maxDistance)
                    ratio = ratio.coerceAtLeast(0f)

                    // 透明度计算
                    val alpha = minAlpha + maxAlpha * ratio

                    itemView.alpha = alpha
                }
            }
        })

        // 页面加载完成后手动刷新一次透明度（刚进来没滑动时生效）
        ageRecyclerView.post {
            ageRecyclerView.scrollBy(1, 0)
            ageRecyclerView.scrollBy(-1, 0)
        }
    }
}