package com.example.bmicalculator.ui

import android.content.Intent
import android.os.Bundle
import android.view.animation.PathInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.bmicalculator.R
import com.example.bmicalculator.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.navigationBarColor = "#3659CF".toColorInt() //导航栏颜色


        // 确保在布局完全测量并绘制到屏幕后，再触发动画
        binding.main.post {

            val density = resources.displayMetrics.density
            val movePx = -(150 * density)
            val bezierInterpolator = PathInterpolator(0.25f, 0f, 0.1f, 0.1f)
            val moveDuration = 1000L

            listOf(binding.splash2, binding.splash3, binding.splash4).forEach { view ->
                view.animate()
                    .translationY(movePx)
                    .alpha(1f)
                    .setDuration(moveDuration)
                    .withLayer()
                    .start()
            }
            val pointer = binding.splash2

            // 设置旋转支点
            pointer.pivotX = pointer.width / 2f
            pointer.pivotY = pointer.height.toFloat() * 0.9f

            binding.splash2.animate()
                .rotation(50f)
                .setDuration(moveDuration)
                .setInterpolator(bezierInterpolator)
                .withLayer()
                .withEndAction {
                    // 第二段：1~2s 回弹 -60°，总时长1000ms
                    binding.splash2.animate()
                        .rotation(-30f) // 转回初始位置
                        .setDuration(moveDuration)
                        .setInterpolator(bezierInterpolator)
                        .withLayer()
                        .start()
                }
                .start()

        }

        // 延迟2秒跳转，同时执行初始化
        lifecycleScope.launch {
            delay(2000)
            //判断是否是初次打开，选择跳转的页面

            val intent = Intent(this@SplashActivity, DataInputActivity::class.java)
            startActivity(intent)
            finish()
            // 跳转主页面
//            val intent = Intent(this@SplashActivity, MainActivity::class.java)
//            startActivity(intent)
//            finish() // 销毁启动页，返回键不会回到闪屏
        }
    }
}