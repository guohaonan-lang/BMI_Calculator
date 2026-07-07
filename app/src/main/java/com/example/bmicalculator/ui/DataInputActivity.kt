package com.example.bmicalculator.ui

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bmicalculator.R
import com.example.bmicalculator.databinding.ActivityDataInputBinding
import com.example.bmicalculator.fragment.DataInputFragment

class DataInputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataInputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDataInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 适配系统状态栏/导航栏边距
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            val bar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.fragmentContainer.setPadding(bar.left, 0, bar.right, bar.bottom)
            insets
        }
        // 装载DataInputFragment，只创建一次，避免旋转重复实例
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

//    // 保留Activity独有全局点击收起键盘逻辑（Fragment用根布局touch，这里全局分发）
//    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        if (ev?.action == MotionEvent.ACTION_DOWN) {
//            val focusView = currentFocus
//            if (focusView is EditText) {
//                val rect = Rect()
//                focus.getGlobalVisibleRect(rect)
//                if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
//                    focus.clearFocus()
//                    val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
//                    imm.hideSoftInputFromWindow(focus.windowToken, android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS)
//                }
//            }
//        }
//        return super.dispatchTouchEvent(ev)
//    }
}