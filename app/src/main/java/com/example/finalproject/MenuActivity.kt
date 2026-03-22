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
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("name", "Кухар")
        val surname = sharedPreferences.getString("surname", "")

        tvWelcome.text = "Привіт, $name $surname!"

        // Кнопка ВИЙТИ
        btnLogout.setOnClickListener {
            sharedPreferences.edit().putBoolean("isAuthorized", false).apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}