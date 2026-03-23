package com.example.finalproject

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GuideActivity : AppCompatActivity() {

    private lateinit var adapter: RecipeAdapter
    private lateinit var paginationContainer: LinearLayout
    private var allRecipes = listOf<RecipeItem>() // Уся база

    // НАЛАШТУВАННЯ ПАГІНАЦІЇ
    private var currentPage = 1
    private val itemsPerPage = 4 // Скільки карток показувати на одній сторінці

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        // 1. Готуємо дані (предустановлений список)
        generateDummyData()

        // 2. Ініціалізуємо список (RecyclerView)
        val recyclerView = findViewById<RecyclerView>(R.id.rvRecipes)
        paginationContainer = findViewById(R.id.paginationContainer)

        recyclerView.layoutManager = LinearLayoutManager(this)
        // Спочатку адаптер порожній
        adapter = RecipeAdapter(emptyList())
        recyclerView.adapter = adapter

        // 3. Завантажуємо першу сторінку
        updatePage()
    }

    private fun generateDummyData() {
        // Тестова база на 14 рецептів (щоб було що пагінувати)
        allRecipes = listOf(
            RecipeItem(1, "Піца Маргарита чотка", "Піца", 25),
            RecipeItem(2, "Бургер По-Багатому", "Стріт Фуд", 15),
            RecipeItem(3, "Суп з лобстерами (мрія)", "Супи", 40),
            RecipeItem(4, "Паста Карбонара класична", "Паста", 20),
            RecipeItem(5, "Салат Цезар з куркою", "Салати", 15),
            RecipeItem(6, "Роли Філадельфія", "Азія", 30),
            RecipeItem(7, "Стейк Рібай (медіум)", "М'ясо", 25),
            RecipeItem(8, "Піца 4 Сири (для гурманів)", "Піца", 20),
            RecipeItem(9, "Крем-суп грибний", "Супи", 30),
            RecipeItem(10, "Шаурма По-Київськи", "Стріт Фуд", 10),
            RecipeItem(11, "Яблучний Штрудель", "Десерти", 60),
            RecipeItem(12, "Тірамісу", "Десерти", 40),
            RecipeItem(13, "Рамен з качкою", "Азія", 50),
            RecipeItem(14, "Різотто з морепродуктами", "Паста", 35)
        )
    }

    // ЛОГІКА ОНОВЛЕННЯ СТОРІНКИ
    private fun updatePage() {
        val totalItems = allRecipes.size
        // Рахуємо індекси для вирізання шматка списку
        val startIndex = (currentPage - 1) * itemsPerPage
        var endIndex = startIndex + itemsPerPage

        if (endIndex > totalItems) endIndex = totalItems

        // Вирізаємо дані саме для цієї сторінки
        val pageData = allRecipes.subList(startIndex, endIndex)

        // Оновлюємо список
        adapter.updateData(pageData)

        // Оновлюємо кнопки пагінації
        updatePaginationButtons()
    }

    // ДИНАМІЧНЕ СТВОРЕННЯ КНОПОК ПАГІНАЦІЇ
    private fun updatePaginationButtons() {
        paginationContainer.removeAllViews() // Очищаємо стару пагінацію

        val totalItems = allRecipes.size
        val totalPages = Math.ceil(totalItems.toDouble() / itemsPerPage).toInt()

        // Малюємо кнопки [1], [2], [3]...
        for (i in 1..totalPages) {
            val btn = Button(this)
            btn.text = i.toString()
            btn.gravity = Gravity.CENTER

            // Задаємо розмір (квадратні)
            val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45f, resources.displayMetrics).toInt()
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(6, 0, 6, 0)
            btn.layoutParams = params
            btn.setPadding(0,0,0,0)

            // Стилізуємо: якщо це поточна сторінка - оранжева, якщо ні - темна
            if (i == currentPage) {
                btn.setBackgroundResource(R.drawable.bg_button) // Наш оранжевий фон
                btn.setTextColor(Color.WHITE)
            } else {
                btn.setBackgroundResource(R.drawable.bg_input) // Наш темний фон
                btn.setTextColor(Color.parseColor("#8A94A6"))
            }

            // Логіка кліку
            btn.setOnClickListener {
                if (currentPage != i) {
                    currentPage = i
                    updatePage() // Перемальовуємо дані для нової сторінки
                }
            }

            paginationContainer.addView(btn) // Додаємо кнопку на екран
        }
    }
}