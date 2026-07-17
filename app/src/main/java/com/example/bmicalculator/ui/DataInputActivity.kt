package com.example.bmicalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bmicalculator.R
import com.example.bmicalculator.databinding.ActivityDataInputBinding
import com.example.bmicalculator.fragment.DataInputFragment

class DataInputActivity : BaseActivity<ActivityDataInputBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): ActivityDataInputBinding {
        return ActivityDataInputBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            val bar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.fragmentContainer.setPadding(bar.left, 0, bar.right, bar.bottom)
            insets
        }
        // 装载DataInputFragment
        loadInputFragment()
    }

    private fun loadInputFragment() {
        val fragmentManager = supportFragmentManager
        // 查询容器中是否已有Fragment，防止横竖屏重建重复创建
        var fragment = fragmentManager.findFragmentById(R.id.fragment_container) as DataInputFragment?
        if (fragment == null) {
            fragment = DataInputFragment()
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow()
        }
    }
}