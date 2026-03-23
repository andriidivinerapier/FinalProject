package com.example.finalproject

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GuideActivity : AppCompatActivity() {

    private lateinit var adapter: RecipeAdapter
    private lateinit var paginationContainer: LinearLayout
    private var allRecipes = listOf<RecipeItem>()
    private var filteredRecipes = listOf<RecipeItem>()

    private var currentPage = 1
    private val itemsPerPage = 4

    // Зберігаємо поточний стан фільтрів
    private var currentCategory = "Усі"
    private var currentSearchText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        generateDummyData()

        val recyclerView = findViewById<RecyclerView>(R.id.rvRecipes)
        paginationContainer = findViewById(R.id.paginationContainer)
        val btnCategories = findViewById<Button>(R.id.btnCategories)
        val etSearch = findViewById<EditText>(R.id.etSearch) // Знаходимо наш пошук
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Ця команда просто закриває поточний екран і повертає на попередній (в Меню)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecipeAdapter(emptyList())
        recyclerView.adapter = adapter

        val categories = arrayOf("Усі", "Сніданки", "Обід", "Вечеря", "Супи", "Салати", "Десерти", "Закуски", "Напої", "Веганські", "Тістечка")

        // 1. ЛОГІКА ДЛЯ КНОПКИ КАТЕГОРІЙ (КАСТОМНИЙ ДИЗАЙН)
        btnCategories.setOnClickListener {
            val dialog = android.app.Dialog(this)
            dialog.setContentView(R.layout.dialog_categories)

            // Робимо стандартний фон вікна прозорим, щоб було видно наші закруглені кути
            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))

            // Задаємо ширину з відступами по краях екрану
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
            dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

            val container = dialog.findViewById<LinearLayout>(R.id.llCategoriesContainer)

            categories.forEach { category ->
                // Створюємо кнопку для кожної категорії
                val tv = android.widget.TextView(this)
                tv.text = category

                // Якщо категорія зараз активна - робимо її оранжевою, якщо ні - білою
                if (category == currentCategory) {
                    tv.setTextColor(Color.parseColor("#FF7A45"))
                    tv.setTypeface(null, android.graphics.Typeface.BOLD)
                } else {
                    tv.setTextColor(Color.WHITE)
                }

                tv.textSize = 18f
                tv.setPadding(0, 40, 0, 40)
                tv.gravity = Gravity.CENTER

                // Клік по категорії
                tv.setOnClickListener {
                    currentCategory = category
                    btnCategories.text = currentCategory
                    applyFilters()
                    dialog.dismiss() // Закриваємо вікно після вибору
                }
                container.addView(tv)

                // Додаємо тонку напівпрозору лінію-розділювач між категоріями
                val divider = android.view.View(this)
                divider.layoutParams = LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 2)
                divider.setBackgroundColor(Color.parseColor("#1AFFFFFF"))
                container.addView(divider)
            }

            dialog.show()
        }

        // 2. ЛОГІКА ДЛЯ ПОЛЯ ПОШУКУ (Слухаємо кожну введену букву)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                currentSearchText = s.toString().trim().lowercase()
                applyFilters() // Запускаємо перевірку обох фільтрів
            }
        })

        // Запуск при відкритті екрану
        applyFilters()
    }

    // ГОЛОВНИЙ ФІЛЬТР: Перевіряє і текст пошуку, і категорію одночасно
    private fun applyFilters() {
        currentPage = 1 // Завжди повертаємось на 1 сторінку при пошуку

        filteredRecipes = allRecipes.filter { recipe ->
            // 1. Чи підходить під категорію?
            val matchCategory = if (currentCategory == "Усі") true else recipe.category == currentCategory

            // 2. Чи є такий текст у назві? (ігноруємо великі/малі літери)
            val matchSearch = recipe.title.lowercase().contains(currentSearchText)

            // Залишаємо рецепт тільки якщо він пройшов обидві перевірки
            matchCategory && matchSearch
        }

        updatePage()
    }

    private fun generateDummyData() {
        allRecipes = listOf(
            RecipeItem(1, "Панкейки з бананом", "Сніданки", 20),
            RecipeItem(2, "Сирники з ваніллю", "Сніданки", 25),
            RecipeItem(3, "Вівсянка з ягодами", "Сніданки", 15),
            RecipeItem(4, "Бургер По-Багатому", "Обід", 15),
            RecipeItem(5, "Шаурма По-Київськи", "Обід", 10),
            RecipeItem(6, "Різотто з морепродуктами", "Обід", 35),
            RecipeItem(7, "Піца Маргарита", "Вечеря", 25),
            RecipeItem(8, "Паста Карбонара", "Вечеря", 20),
            RecipeItem(9, "Стейк Рібай", "Вечеря", 25),
            RecipeItem(10, "Суп з лобстерами", "Супи", 40),
            RecipeItem(11, "Крем-суп грибний", "Супи", 30),
            RecipeItem(12, "Салат Цезар з куркою", "Салати", 15),
            RecipeItem(13, "Яблучний Штрудель", "Десерти", 60),
            RecipeItem(14, "Тірамісу", "Десерти", 40),
            RecipeItem(15, "Роли Філадельфія", "Закуски", 30),
            RecipeItem(16, "Смузі манго-банан", "Напої", 10),
            RecipeItem(17, "Веганський боул", "Веганські", 20),
            RecipeItem(18, "Еклери з кремом", "Тістечка", 45)
        )
    }

    private fun updatePage() {
        val totalItems = filteredRecipes.size

        if (totalItems == 0) {
            adapter.updateData(emptyList())
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

    private fun updatePaginationButtons() {
        paginationContainer.removeAllViews()
        val totalItems = filteredRecipes.size
        val totalPages = Math.ceil(totalItems.toDouble() / itemsPerPage).toInt()

        for (i in 1..totalPages) {
            val btn = Button(this)
            btn.text = i.toString()
            btn.gravity = Gravity.CENTER

            val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45f, resources.displayMetrics).toInt()
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(6, 0, 6, 0)
            btn.layoutParams = params
            btn.setPadding(0,0,0,0)

            if (i == currentPage) {
                btn.setBackgroundResource(R.drawable.bg_button)
                btn.setTextColor(Color.WHITE)
            } else {
                btn.setBackgroundResource(R.drawable.bg_input)
                btn.setTextColor(Color.parseColor("#8A94A6"))
            }

            btn.setOnClickListener {
                if (currentPage != i) {
                    currentPage = i
                    updatePage()
                }
            }
            paginationContainer.addView(btn)
        }
    }
}