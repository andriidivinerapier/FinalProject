package com.example.finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MyRecipesActivity : AppCompatActivity() {

    private lateinit var rvRecipes: RecyclerView
    private lateinit var tvEmpty: TextView
    private var myRecipesList = mutableListOf<Recipe>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_recipes)

        val btnBack = findViewById<ImageView>(R.id.btnBackRecipes)
        val btnAdd = findViewById<ImageView>(R.id.btnAddRecipe)
        rvRecipes = findViewById(R.id.rvMyRecipes)
        tvEmpty = findViewById(R.id.tvEmptyMessage)

        // 1. Кнопка НАЗАД
        btnBack.setOnClickListener { finish() }

        // 2. Кнопка ДОДАТИ (перехід на AddRecipeActivity)
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
        }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadRecipes() // Щоразу оновлюємо список, коли повертаємось на цей екран
    }

    private fun setupRecyclerView() {
        rvRecipes.layoutManager = LinearLayoutManager(this)
        // Тут ми підключимо адаптер, як тільки він буде готовий
    }

    private fun loadRecipes() {
        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentUser = sharedPrefs.getString("current_user", "") ?: ""

        // Дістаємо збережений рядок рецептів (ми будемо зберігати їх як JSON)
        val recipesJson = sharedPrefs.getString("${currentUser}_my_recipes", null)

        if (recipesJson != null) {
            val type = object : TypeToken<List<Recipe>>() {}.type
            val loadedList: List<Recipe> = Gson().fromJson(recipesJson, type)

            myRecipesList.clear()
            myRecipesList.addAll(loadedList)
        }

        // Керуємо видимістю тексту "Порожньо"
        if (myRecipesList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvRecipes.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvRecipes.visibility = View.VISIBLE
            // Тут буде оновлення адаптера: adapter.notifyDataSetChanged()
        }
    }
}