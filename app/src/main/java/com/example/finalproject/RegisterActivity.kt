package com.example.finalproject
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.R
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etName)
        val etSurname = findViewById<EditText>(R.id.etSurname)
        val tvDob = findViewById<TextView>(R.id.tvDob)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etLogin = findViewById<EditText>(R.id.etLogin)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etRepeatPassword = findViewById<EditText>(R.id.etRepeatPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)
        tvGoToLogin.setOnClickListener {
            finish()
        }
        // Календар для дати народження
        tvDob.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dpd = DatePickerDialog(
                this,
                { _, year, month, day ->
                    tvDob.text = "$day/${month + 1}/$year"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show()
        }

        // Кнопка ЗАРЕЄСТРУВАТИСЯ
        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val surname = etSurname.text.toString().trim()
            val dob = tvDob.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val login = etLogin.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val repeatPassword = etRepeatPassword.text.toString().trim()

            // Базова перевірка
            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || login.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(this, "Заповніть усі поля!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!email.contains("@") || !email.contains(".")) {
                Toast.makeText(this, "Неправильний формат Email!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Пароль мінімум 6 символів!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != repeatPassword) {
                Toast.makeText(this, "Паролі не співпадають!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

            // 1. ПЕРЕВІРКА: ЧИ Є ВЖЕ ТАКИЙ ЛОГІН АБО ПОШТА?
            if (sharedPreferences.contains("${login}_password")) {
                Toast.makeText(this, "Цей логін вже зайнятий!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (sharedPreferences.contains("email_$email")) {
                Toast.makeText(this, "Ця пошта вже зареєстрована!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. ЗБЕРЕЖЕННЯ (Кожен юзер має свої унікальні ключі)
            sharedPreferences.edit().apply {
                putString("${login}_name", name)
                putString("${login}_surname", surname)
                putString("${login}_dob", dob)
                putString("${login}_email", email)
                putString("${login}_password", password)

                // Ставимо окрему мітку, що ця пошта тепер зайнята
                putBoolean("email_$email", true)
                apply()
            }

            Toast.makeText(this, "Реєстрація успішна!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}