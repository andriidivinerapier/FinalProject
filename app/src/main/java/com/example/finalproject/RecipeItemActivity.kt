package com.example.finalproject

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import android.graphics.BitmapFactory

class RecipeItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_item)

        // Отримуємо об'єкт рецепта, який ми передали через Intent
        val recipe = intent.getSerializableExtra("recipe") as? RecipeItem

        recipe?.let {
            findViewById<TextView>(R.id.tvRecipeTitle).text = it.title
            // Тут додай решту полів (інгредієнти, кроки тощо)
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }
}