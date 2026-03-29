package com.example.finalproject

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MenuActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var ivAvatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        tvWelcome = findViewById(R.id.tvWelcome)
        ivAvatar = findViewById(R.id.ivMenuAvatar)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnGuide = findViewById<Button>(R.id.btnGuide)
        val btnProfile = findViewById<Button>(R.id.btnProfile)
        val btnMyRecipes = findViewById<Button>(R.id.btnMyRecipes)

        btnMyRecipes.setOnClickListener {
            startActivity(Intent(this, MyRecipesActivity::class.java))
        }

        btnGuide.setOnClickListener {
            startActivity(Intent(this, GuideActivity::class.java))
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnLogout.setOnClickListener {
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isAuthorized", false).apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUserData()
    }

    private fun refreshUserData() {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentUser = sharedPreferences.getString("current_user", "") ?: ""

        if (currentUser.isNotEmpty()) {
            val name = sharedPreferences.getString("${currentUser}_name", "Кухар")
            val surname = sharedPreferences.getString("${currentUser}_surname", "")
            val avatarPath = sharedPreferences.getString("${currentUser}_avatar_path", null)

            tvWelcome.text = "Привіт, $name $surname!"

            // --- ЗМІНИ ТУТ ---
            if (avatarPath != null) {
                val file = File(avatarPath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    ivAvatar.setImageBitmap(bitmap)
                    ivAvatar.setPadding(0, 0, 0, 0)
                } else {
                    // Якщо шлях є, але файл видалено — ставимо логотип
                    setDefaultAvatar()
                }
            } else {
                // Якщо користувач новий і ще не вибрав фото — ставимо логотип сайту
                setDefaultAvatar()
            }
        }
    }

    // Допоміжний метод для встановлення логотипа за замовчуванням
    private fun setDefaultAvatar() {
        // Використовуємо основний логотип додатка
        ivAvatar.setImageResource(R.drawable.my_logo)
        // Прибираємо внутрішні відступи, щоб лого було на весь розмір контейнера
        ivAvatar.setPadding(0, 0, 0, 0)
    }
}