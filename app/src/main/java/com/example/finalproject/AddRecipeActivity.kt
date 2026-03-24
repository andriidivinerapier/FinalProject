package com.example.finalproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddRecipeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        val containerIngredients = findViewById<LinearLayout>(R.id.containerIngredients)
        val btnAddIngredient = findViewById<Button>(R.id.btnAddIngredient)
        val btnClose = findViewById<ImageButton>(R.id.btnClose)

        btnClose.setOnClickListener { finish() }

        // Функція для додавання нового поля інгредієнта
        btnAddIngredient.setOnClickListener {
            val view = LayoutInflater.from(this).inflate(R.layout.item_add_dynamic, containerIngredients, false)
            val etInput = view.findViewById<EditText>(R.id.etDynamicInput)
            val btnRemove = view.findViewById<ImageView>(R.id.btnRemoveItem)

            etInput.hint = "Наприклад: Спагетті - 300г"
            btnRemove.setOnClickListener { containerIngredients.removeView(view) }

            containerIngredients.addView(view)
        }

        // Початково додаємо по одному полю
        btnAddIngredient.performClick()
    }
}