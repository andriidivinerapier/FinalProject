package com.example.finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MyRecipesActivity : AppCompatActivity() {

    private lateinit var rvRecipes: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: RecipeAdapter
    private var myRecipesList = mutableListOf<RecipeItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_recipes)

        val btnBack = findViewById<ImageView>(R.id.btnBackRecipes)
        val btnAdd = findViewById<ImageView>(R.id.btnAddRecipe)
        rvRecipes = findViewById(R.id.rvMyRecipes)
        tvEmpty = findViewById(R.id.tvEmptyMessage)

        btnBack.setOnClickListener { finish() }

        btnAdd.setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
        }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadRecipes()
    }

    private fun setupRecyclerView() {
        rvRecipes.layoutManager = LinearLayoutManager(this)

        // ЗМІНА: Передаємо 'true', щоб адаптер показав іконки смітника та редагування
        adapter = RecipeAdapter(myRecipesList, true) { recipeToDelete ->
            deleteRecipe(recipeToDelete)
        }
        rvRecipes.adapter = adapter
    }

    private fun loadRecipes() {
        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentUser = sharedPrefs.getString("current_user", "") ?: ""
        val recipesJson = sharedPrefs.getString("${currentUser}_my_recipes", null)

        myRecipesList.clear()
        if (recipesJson != null) {
            val type = object : TypeToken<MutableList<RecipeItem>>() {}.type
            val loadedList: MutableList<RecipeItem> = Gson().fromJson(recipesJson, type)
            myRecipesList.addAll(loadedList)
        }

        updateUI()
    }

    private fun updateUI() {
        if (myRecipesList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvRecipes.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvRecipes.visibility = View.VISIBLE
        }
        adapter.notifyDataSetChanged()
    }

    // ЗМІНА: Логіка видалення з обох списків (свого та загального)
    private fun deleteRecipe(recipe: RecipeItem) {
        // Створюємо діалогове вікно
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Видалення рецепта")
        builder.setMessage("Ви впевнені, що хочете видалити '${recipe.title}'? Його неможливо буде відновити.")

        // Кнопка підтвердження
        builder.setPositiveButton("Видалити") { dialog, _ ->
            performFullDeletion(recipe) // Викликаємо саму логіку видалення
            dialog.dismiss()
        }

        // Кнопка відміни
        builder.setNegativeButton("Скасувати") { dialog, _ ->
            dialog.dismiss() // Просто закриваємо вікно без дій
        }

        // Робимо діалог стильнішим (червона кнопка видалення)
        val alertDialog = builder.create()
        alertDialog.show()

        // Опціонально: змінюємо колір кнопки видалення на червоний після показу
        alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            .setTextColor(android.graphics.Color.parseColor("#FF5252"))
    }

    // Виносимо логіку видалення в окремий метод для чистоти коду
    private fun performFullDeletion(recipe: RecipeItem) {
        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentUser = sharedPrefs.getString("current_user", "") ?: ""
        val gson = Gson()
        val type = object : TypeToken<MutableList<RecipeItem>>() {}.type

        // 1. Видаляємо з локального списку
        myRecipesList.remove(recipe)

        // 2. Оновлюємо "Мої рецепти"
        sharedPrefs.edit().putString("${currentUser}_my_recipes", gson.toJson(myRecipesList))
            .apply()

        // 3. Видаляємо з "Загального довідника"
        val globalJson = sharedPrefs.getString("global_recipes", null)
        if (globalJson != null) {
            val globalList: MutableList<RecipeItem> = gson.fromJson(globalJson, type)
            globalList.removeAll { it.id == recipe.id }
            sharedPrefs.edit().putString("global_recipes", gson.toJson(globalList)).apply()
        }

        Toast.makeText(this, "Рецепт видалено 🗑️", Toast.LENGTH_SHORT).show()
        updateUI()
    }
}