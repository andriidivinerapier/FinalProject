package com.example.finalproject

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
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
    private lateinit var layoutPaginationNumbers: LinearLayout

    private var fullList = mutableListOf<RecipeItem>()
    private var currentPage = 0
    private val pageSize = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_recipes)

        val btnBack = findViewById<ImageView>(R.id.btnBackRecipes)
        val btnAdd = findViewById<ImageView>(R.id.btnAddRecipe)
        rvRecipes = findViewById(R.id.rvMyRecipes)
        tvEmpty = findViewById(R.id.tvEmptyMessage)
        layoutPaginationNumbers = findViewById(R.id.layoutPaginationNumbers)

        btnBack.setOnClickListener { finish() }
        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadRecipes()
    }

    private fun setupRecyclerView() {
        rvRecipes.layoutManager = LinearLayoutManager(this)
        adapter = RecipeAdapter(mutableListOf(), true) { deleteRecipe(it) }
        rvRecipes.adapter = adapter
    }

    private fun loadRecipes() {
        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentUser = sharedPrefs.getString("current_user", "") ?: ""
        val recipesJson = sharedPrefs.getString("${currentUser}_my_recipes", null)

        fullList.clear()
        if (recipesJson != null) {
            val type = object : TypeToken<MutableList<RecipeItem>>() {}.type
            val loaded: MutableList<RecipeItem> = Gson().fromJson(recipesJson, type)
            fullList.addAll(loaded)
        }
        updatePage()
    }

    private fun updatePage() {
        if (fullList.isEmpty()) {
            currentPage = 0
            rvRecipes.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            layoutPaginationNumbers.visibility = View.GONE
            return
        }

        rvRecipes.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        val start = currentPage * pageSize
        if (start >= fullList.size && currentPage > 0) {
            currentPage--
            updatePage()
            return
        }

        val end = minOf(start + pageSize, fullList.size)
        val pageItems = fullList.subList(start, end)
        adapter.updateData(pageItems.toList())

        // Малюємо цифри 1, 2, 3...
        renderPagination()
    }

    private fun renderPagination() {
        layoutPaginationNumbers.removeAllViews()
        val totalPages = Math.ceil(fullList.size.toDouble() / pageSize).toInt()

        if (totalPages <= 1) {
            layoutPaginationNumbers.visibility = View.GONE
            return
        }

        layoutPaginationNumbers.visibility = View.VISIBLE

        for (i in 0 until totalPages) {
            val tv = TextView(this)
            tv.text = (i + 1).toString()
            tv.textSize = 20f
            tv.gravity = Gravity.CENTER
            tv.setPadding(35, 15, 35, 15)

            if (i == currentPage) {
                tv.setTextColor(Color.WHITE)
                tv.setTypeface(null, Typeface.BOLD)
            } else {
                tv.setTextColor(Color.GRAY)
                tv.setTypeface(null, Typeface.NORMAL)
            }

            tv.setOnClickListener {
                currentPage = i
                updatePage()
            }

            layoutPaginationNumbers.addView(tv)
        }
    }

    private fun deleteRecipe(recipe: RecipeItem) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Видалення")
        builder.setMessage("Видалити рецепт '${recipe.title}'?")
        builder.setPositiveButton("Видалити") { _, _ ->
            fullList.removeAll { it.id == recipe.id }
            val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val currentUser = sharedPrefs.getString("current_user", "") ?: ""
            sharedPrefs.edit().putString("${currentUser}_my_recipes", Gson().toJson(fullList)).apply()

            // Видалення з глобального
            val globalJson = sharedPrefs.getString("global_recipes", null)
            if (globalJson != null) {
                val type = object : TypeToken<MutableList<RecipeItem>>() {}.type
                val globalList: MutableList<RecipeItem> = Gson().fromJson(globalJson, type)
                globalList.removeAll { it.id == recipe.id }
                sharedPrefs.edit().putString("global_recipes", Gson().toJson(globalList)).apply()
            }

            Toast.makeText(this, "Видалено 🗑️", Toast.LENGTH_SHORT).show()
            updatePage()
        }
        builder.setNegativeButton("Скасувати", null)
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
    }
}