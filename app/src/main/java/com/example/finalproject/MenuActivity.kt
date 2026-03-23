package com.example.finalproject
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.R

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Зчитуємо дані користувача
        // Дізнаємося, який логін зараз активний
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentUser = sharedPreferences.getString("current_user", "")
        val btnGuide = findViewById<Button>(R.id.btnGuide)

        btnGuide.setOnClickListener {
            startActivity(Intent(this, GuideActivity::class.java))
        }
        // Дістаємо ім'я та прізвище саме цього користувача
        val name = sharedPreferences.getString("${currentUser}_name", "Кухар")
        val surname = sharedPreferences.getString("${currentUser}_surname", "")

        tvWelcome?.text = "Привіт, $name $surname!"

        // Кнопка ВИЙТИ
        btnLogout.setOnClickListener {
            sharedPreferences.edit().putBoolean("isAuthorized", false).apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}