package com.example.finalproject
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etLogin = findViewById<EditText>(R.id.etLogin)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        // Перехід на екран реєстрації
        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Кнопка УВІЙТИ
        btnLogin.setOnClickListener {
            val inputLogin = etLogin.text.toString().trim()
            val inputPassword = etPassword.text.toString().trim()

            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

            // Дістаємо пароль, який належить саме цьому логіну
            val savedPassword = sharedPreferences.getString("${inputLogin}_password", null)

            // Якщо такий логін існує і паролі збігаються
            if (savedPassword != null && inputPassword == savedPassword) {

                // Вхід успішний: зберігаємо сесію і ТОГО, ХТО УВІЙШОВ
                sharedPreferences.edit()
                    .putBoolean("isAuthorized", true)
                    .putString("current_user", inputLogin)
                    .apply()

                Toast.makeText(this, "Вхід успішний!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Невірний логін або пароль!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}