package com.example.finalproject

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class AddRecipeActivity : AppCompatActivity() {

    // Списки для випадаючих меню (Spinner)
    private val difficulties = arrayOf("Легка", "Середня", "Важка")
    private val categories = arrayOf(
        "Сніданок", "Обід", "Вечеря", "Супи",
        "Салати", "Десерти", "Закуски", "НАПОЇ", "Веганські"
    )

    // Змінна для зберігання посилання на вибране фото
    private var selectedImageUri: Uri? = null
    private lateinit var tvFileName: TextView // Текст "Файл не обраний"

    // --- 1. ЛОГІКА ВИБОРУ ФОТО З ГАЛЕРЕЇ ---
    // Це сучасний спосіб отримати результат з іншого вікна (галереї)
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Отримуємо Uri (адресу) вибраного зображення
            selectedImageUri = result.data?.data
            if (selectedImageUri != null) {
                // Оновлюємо текст, щоб користувач бачив, що фото вибрано
                tvFileName.text = "Фото успішно додано ✅"
                tvFileName.setTextColor(resources.getColor(R.color.white)) // Робимо текст білим
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        // --- Ініціалізація в'юшок з XML ---
        val btnClose = findViewById<ImageButton>(R.id.btnClose)
        val btnPublish = findViewById<Button>(R.id.btnPublish)
        val spinDifficulty = findViewById<Spinner>(R.id.spinDifficulty)
        val spinCategory = findViewById<Spinner>(R.id.spinCategory)

        // Для фото
        val btnSelectFile = findViewById<Button>(R.id.btnSelectFile)
        tvFileName = findViewById(R.id.tvFileName)

        // Для інгредієнтів (вже має працювати, якщо логіка була)
        val containerIngredients = findViewById<LinearLayout>(R.id.containerIngredients)
        val btnAddIngredient = findViewById<Button>(R.id.btnAddIngredient)

        // Для етапів
        val containerSteps = findViewById<LinearLayout>(R.id.containerSteps)
        val btnAddStep = findViewById<Button>(R.id.btnAddStep)


        // --- 2. НАЛАШТУВАННЯ SPINNER-ІВ (БІЛИЙ ТЕКСТ) ---
        // Використовуємо твій R.layout.item_spinner для білого кольору
        val diffAdapter = ArrayAdapter(this, R.layout.item_spinner, difficulties)
        diffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinDifficulty.adapter = diffAdapter

        val catAdapter = ArrayAdapter(this, R.layout.item_spinner, categories)
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinCategory.adapter = catAdapter


        // --- 3. ОБРОБКА КНОПКИ "ВИБЕРІТЬ ФАЙЛ" ---
        btnSelectFile.setOnClickListener {
            // Створюємо намір (Intent) для відкриття галереї
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            // Запускаємо лоунчер
            pickImageLauncher.launch(intent)
        }


        // --- 4. ЛОГІКА ДОДАВАННЯ ІНГРЕДІЄНТІВ (Приклад) ---
        btnAddIngredient.setOnClickListener {
            // Роздуваємо макет одного рядка інгредієнта
            val view = LayoutInflater.from(this).inflate(R.layout.item_add_dynamic, containerIngredients, false)
            val etInput = view.findViewById<EditText>(R.id.etDynamicInput)
            val btnRemove = view.findViewById<ImageView>(R.id.btnRemoveItem)

            etInput.hint = "Наприклад: Борошно - 300г"
            // Логіка видалення цього рядка
            btnRemove.setOnClickListener { containerIngredients.removeView(view) }

            // Додаємо в контейнер
            containerIngredients.addView(view)
        }
        // Додаємо одне поле автоматично при старті
        if (containerIngredients.childCount == 0) btnAddIngredient.performClick()


        // --- 5. ЛОГІКА ДОДАВАННЯ ЕТАПІВ ПРИГОТУВАННЯ (НОВЕ) ---
        btnAddStep.setOnClickListener {
            // Використовуємо той самий макет R.layout.item_add_dynamic
            val view = LayoutInflater.from(this).inflate(R.layout.item_add_dynamic, containerSteps, false)
            val etInput = view.findViewById<EditText>(R.id.etDynamicInput)
            val btnRemove = view.findViewById<ImageView>(R.id.btnRemoveItem)

            // Налаштовуємо поле для довгого тексту
            etInput.hint = "Опишіть крок приготування..."
            // Робимо поле багаторядковим, щоб зручно було писати
            etInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            etInput.minLines = 2 // Мінімальна висота - 2 рядки

            // Логіка видалення цього етапу
            btnRemove.setOnClickListener { containerSteps.removeView(view) }

            // Додаємо в контейнер етапів
            containerSteps.addView(view)
        }
        // Додаємо перший етап автоматично при старті
        if (containerSteps.childCount == 0) btnAddStep.performClick()


        // --- Стандартні кнопки ---
        btnClose.setOnClickListener { finish() } // Закрити вікно

        btnPublish.setOnClickListener {
            // ТУТ БУДЕ ЛОГІКА ЗБЕРЕЖЕННЯ РЕЦЕПТА (наступний етап)
            Toast.makeText(this, "Зберігаємо рецепт...", Toast.LENGTH_SHORT).show()
        }
    }
}