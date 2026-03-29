package com.example.finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ПІДКЛЮЧАЄМО МАКЕТ З ЛОГОТИПОМ
        setContentView(R.layout.activity_splash)

        // ВСТАНОВЛЮЄМО ЗАТРИМКУ 2 СЕКУНДИ (2000 мс)
        Handler(Looper.getMainLooper()).postDelayed({

            // ПЕРЕВІРЯЄМО СЕСІЮ (чи залогінений юзер)
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val isAuthorized = sharedPreferences.getBoolean("isAuthorized", false)

            // ВИЗНАЧАЄМО НАСТУПНИЙ ЕКРАН
            val intent = if (isAuthorized) {
                Intent(this, MenuActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }

            // ПЕРЕХОДИМО ТА ЗАКРИВАЄМО SPLASH
            startActivity(intent)
            finish()

        }, 2000) // Рівно 2 секунди заставки
    }
}