package com.example.finalproject

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GuideActivity : AppCompatActivity() {

    private lateinit var adapter: RecipeAdapter
    private lateinit var paginationContainer: LinearLayout
    // Створюємо порожній список відразу
    private var allRecipes = mutableListOf<RecipeItem>()
    private var filteredRecipes = listOf<RecipeItem>()

    private var currentPage = 1
    private val itemsPerPage = 4
    private var currentCategory = "Усі"
    private var currentSearchText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        // ОЧИЩАЄМО ТА ЗАПОВНЮЄМО
        allRecipes.clear()
        generateDummyData() // Спочатку тестові
        loadGlobalRecipes()  // Потім додаємо з пам'яті

        val recyclerView = findViewById<RecyclerView>(R.id.rvRecipes)
        paginationContainer = findViewById(R.id.paginationContainer)
        val btnCategories = findViewById<Button>(R.id.btnCategories)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecipeAdapter(emptyList<RecipeItem>()) { /* Видалення не потрібне */ }
        recyclerView.adapter = adapter

        // Виправив "Сніданки" на "Сніданок", щоб збігалося з AddRecipeActivity
        val categories = arrayOf("Усі", "Сніданок", "Обід", "Вечеря", "Супи", "Салати", "Десерти", "Закуски", "НАПОЇ", "Веганські")

        btnCategories.setOnClickListener {
            showCategoryDialog(categories, btnCategories)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearchText = s.toString().trim().lowercase()
                applyFilters()
            }
        })

        applyFilters()
    }

    private fun loadGlobalRecipes() {
        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPrefs.getString("global_recipes", null)
        if (json != null) {
            try {
                val type = object : TypeToken<MutableList<RecipeItem>>() {}.type
                val userRecipes: MutableList<RecipeItem> = gson.fromJson(json, type)
                // ВИКОРИСТОВУЄМО addAll, щоб не видалити DummyData
                allRecipes.addAll(userRecipes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun applyFilters() {
        currentPage = 1
        filteredRecipes = allRecipes.filter { recipe ->
            val matchCategory = if (currentCategory == "Усі") true else recipe.category == currentCategory
            val matchSearch = recipe.title.lowercase().contains(currentSearchText)
            matchCategory && matchSearch
        }
        updatePage()
    }

    private fun generateDummyData() {
        // Використовуємо addAll, щоб додати до існуючого списку
        allRecipes.addAll(listOf(
            RecipeItem(1, "Панкейки з бананом", "Сніданок", 20, "Система"),
            RecipeItem(2, "Сирники з ваніллю", "Сніданок", 25, "Система"),
            RecipeItem(4, "Бургер По-Багатому", "Обід", 15, "Система"),
            RecipeItem(13, "Яблучний Штрудель", "Десерти", 60, "Система")
        ))
    }

    private fun updatePage() {
        val totalItems = filteredRecipes.size
        if (totalItems == 0) {
            adapter.updateData(emptyList<RecipeItem>())
            paginationContainer.removeAllViews()
            return
        }

        val startIndex = (currentPage - 1) * itemsPerPage
        var endIndex = startIndex + itemsPerPage
        if (endIndex > totalItems) endIndex = totalItems

        val pageData = filteredRecipes.subList(startIndex, endIndex)
        adapter.updateData(pageData)
        updatePaginationButtons()
    }

    private fun showCategoryDialog(categories: Array<String>, btn: Button) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_categories)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        val container = dialog.findViewById<LinearLayout>(R.id.llCategoriesContainer)
        container.removeAllViews()

        categories.forEach { category ->
            val tv = TextView(this)
            tv.text = category
            if (category == currentCategory) tv.setTextColor(Color.parseColor("#FF7A45")) else tv.setTextColor(Color.WHITE)
            tv.textSize = 18f
            tv.setPadding(0, 40, 0, 40)
            tv.gravity = Gravity.CENTER
            tv.setOnClickListener {
                currentCategory = category
                btn.text = currentCategory
                applyFilters()
                dialog.dismiss()
            }
            container.addView(tv)
        }
        dialog.show()
    }

    private fun updatePaginationButtons() {
        paginationContainer.removeAllViews()
        val totalPages = Math.ceil(filteredRecipes.size.toDouble() / itemsPerPage).toInt()
        for (i in 1..totalPages) {
            val btn = Button(this)
            btn.text = i.toString()
            val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45f, resources.displayMetrics).toInt()
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(6, 0, 6, 0)
            btn.layoutParams = params
            if (i == currentPage) {
                btn.setBackgroundResource(R.drawable.bg_button)
                btn.setTextColor(Color.WHITE)
            } else {
                btn.setBackgroundResource(R.drawable.bg_input)
                btn.setTextColor(Color.parseColor("#8A94A6"))
            }
            btn.setOnClickListener { if (currentPage != i) { currentPage = i; updatePage() } }
            paginationContainer.addView(btn)
        }
    }
}