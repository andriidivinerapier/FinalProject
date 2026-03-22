package com.example.finalproject
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Перевіряємо сесію
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isAuthorized = sharedPreferences.getBoolean("isAuthorized", false)

        // Куди йдемо далі?
        if (isAuthorized) {
            startActivity(Intent(this, MenuActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Закриваємо Splash, щоб він не висів у фоні
        finish()
    }
}