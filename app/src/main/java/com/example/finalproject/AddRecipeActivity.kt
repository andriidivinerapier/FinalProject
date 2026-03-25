package com.example.finalproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
class AddRecipeActivity : AppCompatActivity() {

    private val difficulties = arrayOf("Легка", "Середня", "Важка")
    private val categories = arrayOf(
        "Сніданок",
        "Обід",
        "Вечеря",
        "Супи",
        "Салати",
        "Десерти",
        "Закуски",
        "НАПОЇ",
        "Веганські"
    )

    private var selectedImageUri: Uri? = null
    private lateinit var tvFileName: TextView

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                tvFileName.text = "Фото додано ✅"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        val containerIngredients = findViewById<LinearLayout>(R.id.containerIngredients)
        val containerSteps = findViewById<LinearLayout>(R.id.containerSteps)
        val etTitle = findViewById<EditText>(R.id.etRecipeTitle)
        val etTime = findViewById<EditText>(R.id.etRecipeTime)
        val spinDifficulty = findViewById<Spinner>(R.id.spinDifficulty)
        val spinCategory = findViewById<Spinner>(R.id.spinCategory)
        val btnPublish = findViewById<Button>(R.id.btnPublish)
        val btnClose = findViewById<ImageButton>(R.id.btnClose)
        tvFileName = findViewById(R.id.tvFileName)

        // Налаштування Spinner-ів (білий текст через твій item_spinner)
        val adapterParams = ArrayAdapter(this, R.layout.item_spinner, difficulties)
        adapterParams.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinDifficulty.adapter = adapterParams

        val adapterCat = ArrayAdapter(this, R.layout.item_spinner, categories)
        adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinCategory.adapter = adapterCat

        // Кнопка закриття
        btnClose.setOnClickListener { finish() }

        // Кнопка вибору фото
        findViewById<Button>(R.id.btnSelectFile).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        // Додавання інгредієнтів
        findViewById<Button>(R.id.btnAddIngredient).setOnClickListener {
            addDynamicField(containerIngredients, "Інгредієнт (напр. Цукор - 100г)")
        }

        // Додавання етапів
        findViewById<Button>(R.id.btnAddStep).setOnClickListener {
            addDynamicField(containerSteps, "Опишіть крок приготування...", true)
        }

        // Авто-додавання перших полів, щоб не було пусто
        if (containerIngredients.childCount == 0) addDynamicField(
            containerIngredients,
            "Інгредієнт (напр. Цукор - 100г)"
        )
        if (containerSteps.childCount == 0) addDynamicField(
            containerSteps,
            "Опишіть крок приготування...",
            true
        )

        // Кнопка "Опублікувати"
        btnPublish.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val timeStr = etTime.text.toString().trim()
            val category = spinCategory.selectedItem.toString()

            // --- НОВА ЗМІНА: ПЕРЕВІРКА НА ФОТО ---
            if (selectedImageUri == null) {
                Toast.makeText(
                    this,
                    "Будь ласка, обов'язково додайте фото страви! 📸",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener // Зупиняємо публікацію
            }

            // Перевірка назви та часу
            if (title.isEmpty() || timeStr.isEmpty()) {
                Toast.makeText(this, "Заповніть назву та час!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ingredients = collectData(containerIngredients)
            val steps = collectData(containerSteps)

            saveRecipe(title, timeStr.toInt(), category, ingredients, steps)
        }
    }

    private fun addDynamicField(
        container: LinearLayout,
        hint: String,
        isMultiLine: Boolean = false
    ) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_add_dynamic, container, false)
        val et = view.findViewById<EditText>(R.id.etDynamicInput)
        et.hint = hint
        if (isMultiLine) {
            et.minLines = 2
            et.inputType =
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        view.findViewById<ImageView>(R.id.btnRemoveItem)
            .setOnClickListener { container.removeView(view) }
        container.addView(view)
    }

    private fun collectData(container: LinearLayout): String {
        val list = mutableListOf<String>()
        for (i in 0 until container.childCount) {
            val et = container.getChildAt(i).findViewById<EditText>(R.id.etDynamicInput)
            if (et.text.isNotEmpty()) list.add(et.text.toString())
        }
        return list.joinToString("\n")
    }

    private fun saveRecipe(
        title: String,
        time: Int,
        category: String,
        ingredients: String,
        steps: String
    ) {
        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentUser = sharedPrefs.getString("current_user", "") ?: ""
        val authorName = sharedPrefs.getString("${currentUser}_name", currentUser) ?: currentUser

        // --- НОВА ЛОГІКА ЗБЕРЕЖЕННЯ ФОТО ---
        var finalImagePath: String? = null

        selectedImageUri?.let { uri ->
            // Намагаємося скопіювати фото у внутрішню пам'ять додатка
            try {
                finalImagePath = copyImageToInternalStorage(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                // Якщо копіювання не вдалося, використовуємо тимчасовий URI (хоча це навряд чи спрацює в довіднику)
                finalImagePath = uri.toString()
            }
        }
        // ------------------------------------

        val newRecipe = RecipeItem(
            id = System.currentTimeMillis(),
            title = title,
            category = category,
            timeMins = time,
            author = authorName,
            imagePath = finalImagePath, // Використовуємо постійний шлях
            ingredients = ingredients,
            instructions = steps
        )

        val gson = Gson()
        val type = object : TypeToken<MutableList<RecipeItem>>() {}.type

        // 1. Зберігаємо в "Мої рецепти"
        val myJson = sharedPrefs.getString("${currentUser}_my_recipes", null)
        val myList: MutableList<RecipeItem> = if (myJson != null) {
            try {
                gson.fromJson(myJson, type)
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
        myList.add(newRecipe)
        sharedPrefs.edit().putString("${currentUser}_my_recipes", gson.toJson(myList)).apply()

        // 2. Зберігаємо в "Кулінарний довідник" (global_recipes)
        val globalJson = sharedPrefs.getString("global_recipes", null)
        val globalList: MutableList<RecipeItem> = if (globalJson != null) {
            try {
                gson.fromJson(globalJson, type)
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
        globalList.add(newRecipe)
        sharedPrefs.edit().putString("global_recipes", gson.toJson(globalList)).apply()

        Toast.makeText(this, "Рецепт опубліковано! 🚀", Toast.LENGTH_SHORT).show()
        finish()
    }

    // --- НОВА ФУНКЦІЯ КОПІЮВАННЯ ФАЙЛУ ---
    private fun copyImageToInternalStorage(uri: Uri): String? {
        // Створюємо унікальне ім'я для файлу на основі поточного часу
        val fileName = "recipe_${System.currentTimeMillis()}.jpg"

        // Отримуємо посилання на папку 'files' всередині додатка
        val internalDir = filesDir
        val outputFile = File(internalDir, fileName)

        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            // Відкриваємо потік читання з галереї
            inputStream = contentResolver.openInputStream(uri)
            // Відкриваємо потік запису у внутрішню папку
            outputStream = FileOutputStream(outputFile)

            if (inputStream == null) return null

            // Копіюємо дані байт за байтом
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            // Повертаємо абсолютний шлях до нового файлу
            return outputFile.absolutePath

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            // Обов'язково закриваємо потоки
            inputStream?.close()
            outputStream?.close()
        }
    }
}