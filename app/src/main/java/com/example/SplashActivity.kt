package com.example

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.data.repository.UserManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val contentLayout = findViewById<android.view.View>(R.id.layout_splash_content)
        contentLayout.alpha = 0f
        contentLayout.scaleX = 0.85f
        contentLayout.scaleY = 0.85f

        contentLayout.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(900L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        lifecycleScope.launch {
            delay(1400L)
            val userManager = UserManager.getInstance(this@SplashActivity)
            if (userManager.isOnboardingCompleted()) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, OnboardingActivity::class.java))
            }
            finish()
        }
    }
}
