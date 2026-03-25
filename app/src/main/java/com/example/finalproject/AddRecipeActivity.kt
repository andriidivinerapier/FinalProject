package com.example.finalproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AddRecipeActivity : AppCompatActivity() {

    // Змінна для зберігання ID рецепта, який ми редагуємо
    private var editingRecipeId: Long? = null
    // Шлях до існуючого фото (якщо ми в режимі редагування)
    private var currentImagePath: String? = null

    private val difficulties = arrayOf("Легка", "Середня", "Важка")
    private val categories = arrayOf(
        "Сніданок", "Обід", "Вечеря", "Супи", "Салати",
        "Десерти", "Закуски", "НАПОЇ", "Веганські"
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

        // Налаштування Spinner-ів
        val adapterParams = ArrayAdapter(this, R.layout.item_spinner, difficulties)
        adapterParams.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinDifficulty.adapter = adapterParams

        val adapterCat = ArrayAdapter(this, R.layout.item_spinner, categories)
        adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinCategory.adapter = adapterCat

        // --- ПЕРЕВІРКА НА РЕЖИМ РЕДАГУВАННЯ ---
        val recipeToEdit = intent.getSerializableExtra("edit_recipe") as? RecipeItem
        if (recipeToEdit != null) {
            editingRecipeId = recipeToEdit.id
            currentImagePath = recipeToEdit.imagePath

            etTitle.setText(recipeToEdit.title)
            etTime.setText(recipeToEdit.timeMins.toString())

            val catIndex = categories.indexOf(recipeToEdit.category)
            if (catIndex >= 0) spinCategory.setSelection(catIndex)

            // Очищуємо порожні поля за замовчуванням перед заповненням
            containerIngredients.removeAllViews()
            containerSteps.removeAllViews()

            // Заповнюємо інгредієнти
            recipeToEdit.ingredients.split("\n").filter { it.isNotBlank() }.forEach {
                addDynamicField(containerIngredients, "Інгредієнт...", false, it)
            }

            // Заповнюємо кроки
            recipeToEdit.instructions.split("\n").filter { it.isNotBlank() }.forEach {
                addDynamicField(containerSteps, "Опишіть крок...", true, it)
            }

            tvFileName.text = "Фото вже є (можна змінити) 📷"
            btnPublish.text = "Зберегти зміни"
        } else {
            // Якщо це новий рецепт, додаємо по одному порожньому полю
            addDynamicField(containerIngredients, "Інгредієнт (напр. Цукор - 100г)")
            addDynamicField(containerSteps, "Опишіть крок приготування...", true)
        }

        btnClose.setOnClickListener { finish() }

        findViewById<Button>(R.id.btnSelectFile).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        findViewById<Button>(R.id.btnAddIngredient).setOnClickListener {
            addDynamicField(containerIngredients, "Інгредієнт (напр. Цукор - 100г)")
        }

        findViewById<Button>(R.id.btnAddStep).setOnClickListener {
            addDynamicField(containerSteps, "Опишіть крок приготування...", true)
        }

        btnPublish.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val timeStr = etTime.text.toString().trim()
            val category = spinCategory.selectedItem.toString()

            // ПЕРЕВІРКА ФОТО: Якщо це новий рецепт, фото обов'язкове. Якщо редагування — можна лишити старе.
            if (selectedImageUri == null && currentImagePath == null) {
                Toast.makeText(this, "Будь ласка, додайте фото страви! 📸", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (title.isEmpty() || timeStr.isEmpty()) {
                Toast.makeText(this, "Заповніть назву та час!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ingredients = collectData(containerIngredients)
            val steps = collectData(containerSteps)

            saveRecipe(title, timeStr.toInt(), category, ingredients, steps)
        }
    }

    private fun addDynamicField(container: LinearLayout, hint: String, isMultiLine: Boolean = false, initialText: String = "") {
        val view = LayoutInflater.from(this).inflate(R.layout.item_add_dynamic, container, false)
        val et = view.findViewById<EditText>(R.id.etDynamicInput)
        et.hint = hint
        et.setText(initialText)

        if (isMultiLine) {
            et.minLines = 2
            et.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        view.findViewById<ImageView>(R.id.btnRemoveItem).setOnClickListener { container.removeView(view) }
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

    private fun saveRecipe(title: String, time: Int, category: String, ingredients: String, steps: String) {
        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val currentUser = sharedPrefs.getString("current_user", "") ?: ""
        val authorName = sharedPrefs.getString("${currentUser}_name", currentUser) ?: currentUser

        // Якщо вибрали нове фото — копіюємо, інакше лишаємо старий шлях
        val finalImagePath = if (selectedImageUri != null) {
            copyImageToInternalStorage(selectedImageUri!!)
        } else {
            currentImagePath
        }

        val newRecipe = RecipeItem(
            id = editingRecipeId ?: System.currentTimeMillis(),
            title = title,
            category = category,
            timeMins = time,
            author = authorName,
            imagePath = finalImagePath,
            ingredients = ingredients,
            instructions = steps
        )

        val gson = Gson()
        val type = object : TypeToken<MutableList<RecipeItem>>() {}.type

        // 1. Оновлюємо "Мої рецепти"
        val myJson = sharedPrefs.getString("${currentUser}_my_recipes", null)
        val myList: MutableList<RecipeItem> = if (myJson != null) {
            gson.fromJson(myJson, type)
        } else mutableListOf()

        if (editingRecipeId != null) myList.removeAll { it.id == editingRecipeId }
        myList.add(newRecipe)
        sharedPrefs.edit().putString("${currentUser}_my_recipes", gson.toJson(myList)).apply()

        // 2. Оновлюємо "Загальний довідника"
        val globalJson = sharedPrefs.getString("global_recipes", null)
        val globalList: MutableList<RecipeItem> = if (globalJson != null) {
            gson.fromJson(globalJson, type)
        } else mutableListOf()

        if (editingRecipeId != null) globalList.removeAll { it.id == editingRecipeId }
        globalList.add(newRecipe)
        sharedPrefs.edit().putString("global_recipes", gson.toJson(globalList)).apply()

        Toast.makeText(this, if (editingRecipeId != null) "Зміни збережено! ✨" else "Рецепт опубліковано! 🚀", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun copyImageToInternalStorage(uri: Uri): String? {
        val fileName = "recipe_${System.currentTimeMillis()}.jpg"
        val outputFile = File(filesDir, fileName)
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }
            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}