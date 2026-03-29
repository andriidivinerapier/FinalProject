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
        // ЗАМІНІТЬ com.example.finalproject НА ВАШ РЕАЛЬНИЙ ПАКЕТ, ЯКЩО ВІН ВІДРІЗНЯЄТЬСЯ
        val packageName = "com.example.finalproject"

        allRecipes.addAll(listOf(
            RecipeItem(1, "Панкейки з бананом", "Сніданок", 20, "Легка", "Система",
                "android.resource://$packageName/${R.drawable.pankeyki}",
                "Банан - 1 шт\nЯйце - 1 шт\nБорошно - 2 ст.л.\nМолоко - 50 мл",
                "Розімніть банан виделкою.\nДодайте яйце та молоко, перемішайте.\nВсипте борошно.\nСмажте на сухій сковорідці до золотистої скоринки."),

            RecipeItem(2, "Класичний Борщ", "Супи", 90, "Важка", "Система",
                "android.resource://$packageName/${R.drawable.classic}",
                "Яловичина - 500г\nБуряк - 2 шт\nКапуста - 300г\nКартопля - 3 шт\nМорква - 1 шт",
                "Зваріть м'ясний бульйон.\nДодайте нарізану картоплю.\nОбсмажте натерті буряк та моркву.\nДодайте засмажку та капусту в каструлю.\nВаріть до готовності капусти."),

            RecipeItem(3, "Салат Цезар", "Салати", 15, "Середня", "Система",
                "android.resource://$packageName/${R.drawable.salat}",
                "Куряче філе - 200г\nЛистя салату - 1 пучок\nСухарики - 50г\nПармезан - 30г\nСоус Цезар - 2 ст.л.",
                "Обсмажте куряче філе до готовності.\nПорвіть листя салату в миску.\nДодайте курку та сухарики.\n4Полийте соусом та посипте тертим сиром."),

            RecipeItem(4, "Паста Карбонара", "Обід", 20, "Середня", "Система",
                "android.resource://$packageName/${R.drawable.pasta}",
                "Спагеті - 200г\nБекон - 100г\nЯйця (жовтки) - 3 шт\nСир Пармезан - 50г",
                "Відваріть спагеті.\nОбсмажте бекон на сковорідці.\nЗмішайте жовтки з тертим сиром.\nЗ'єднайте спагеті з беконом та швидко влийте яєчну суміш, постійно помішуючи."),

            RecipeItem(5, "Гарбузовий суп-пюре", "Супи", 40, "Легка", "Система",
                "android.resource://$packageName/${R.drawable.garbuzovuikremsup}",
                "Гарбуз - 600г\nВершки - 100 мл\nЦибуля - 1 шт\nЧасник - 2 зубчики",
                "Наріжте гарбуз та цибулю кубиками.\nВідваріть овочі до м'якості.\nЗбийте блендером до однорідності.\nДодайте вершки та прогрійте 2 хвилини."),

            RecipeItem(6, "Домашня Піца", "Закуски", 35, "Середня", "Система",
                "android.resource://$packageName/${R.drawable.pizza}",
                "Тісто для піци - 300г\nТоматний соус - 3 ст.л.\nМоцарела - 150г\nКовбаса салямі - 50г",
                "Розкачайте тісто.\nЗмастіть соусом.\nВикладіть сир та ковбасу.\nВипікайте при 220 градусах 12-15 хвилин."),

            RecipeItem(7, "Смузі Боул", "Сніданок", 10, "Легка", "Система",
                "android.resource://$packageName/${R.drawable.smuziboul}",
                "Заморожена ягода - 150г\nБанан - 1 шт\nЙогурт - 100 мл\nГоріхи та насіння - для декору",
                "Збийте ягоди, банан та йогурт у блендері.\nПерелийте у глибоку миску.\nПрикрасьте горіхами та свіжими фруктами."),

            RecipeItem(8, "Овочі на грилі", "Веганські", 25, "Легка", "Система",
                "android.resource://$packageName/${R.drawable.grill}",
                "Кабачок - 1 шт\nБолгарський перець - 2 шт\nПечериці - 200г\nОливкова олія - 2 ст.л.",
                "Наріжте овочі скибочками.\nЗмастіть олією та спеціями.\nОбсмажте на грилі по 4-5 хвилин з кожного боку."),

            RecipeItem(9, "Лимонад з м'ятою", "НАПОЇ", 10, "Легка", "Система",
                "android.resource://$packageName/${R.drawable.lumonad}",
                "Лимон - 2 шт\nМ'ята - 1 пучок\nЦукор - 3 ст.л.\nВода газована - 1 л",
                "Вичавіть сік з лимонів.\nРозітріть м'яту з цукром.\nЗмішайте сік, м'яту та воду.\nДодайте лід."),

            RecipeItem(10, "Шоколадний Фондан", "Десерти", 25, "Важка", "Система", "android.resource://$packageName/${R.drawable.fondan}",
                "Чорний шоколад - 100г\nВершкове масло - 50г\nЯйця - 2 шт\nБорошно - 2 ст.л.",
                "Розтопіть шоколад з маслом.\nЗбийте яйця з цукром.\nЗ'єднайте суміші та додайте борошно.\n Випікайте рівно 8 хвилин при 200 градусах.")
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