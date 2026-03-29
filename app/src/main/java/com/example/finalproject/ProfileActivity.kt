package com.example.finalproject

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

class ProfileActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etSurname: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etDob: TextInputEditText
    private lateinit var ivAvatar: ImageView
    private lateinit var tvFullName: TextView
    private lateinit var tvLogin: TextView

    private var currentUser: String = ""
    private var cameraBitmap: Bitmap? = null
    private var galleryUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            galleryUri = result.data?.data
            ivAvatar.setImageURI(galleryUri)
            ivAvatar.setPadding(0, 0, 0, 0) // Прибираємо відступи при виборі фото
            cameraBitmap = null
        }
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                ivAvatar.setImageBitmap(it)
                ivAvatar.setPadding(0, 0, 0, 0) // Прибираємо відступи
                cameraBitmap = it
                galleryUri = null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val btnBack = findViewById<ImageView>(R.id.btnBackProfile)
        val btnChangeAvatar = findViewById<androidx.cardview.widget.CardView>(R.id.btnChangeAvatar)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)

        tvFullName = findViewById(R.id.tvProfileFullName)
        tvLogin = findViewById(R.id.tvProfileLogin)
        ivAvatar = findViewById(R.id.ivProfileAvatar)

        etName = findViewById(R.id.etProfileName)
        etSurname = findViewById(R.id.etProfileSurname)
        etEmail = findViewById(R.id.etProfileEmail)
        etDob = findViewById(R.id.etProfileDob)

        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        currentUser = sharedPrefs.getString("current_user", "") ?: ""

        loadUserData()

        btnBack.setOnClickListener { finish() }
        etDob.setOnClickListener { showDatePicker() }
        btnChangeAvatar.setOnClickListener { showImageSourceRequest() }
        btnSave.setOnClickListener { saveChanges() }
    }

    private fun loadUserData() {
        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        if (currentUser.isNotEmpty()) {
            val name = sharedPrefs.getString("${currentUser}_name", "") ?: ""
            val surname = sharedPrefs.getString("${currentUser}_surname", "") ?: ""
            val email = sharedPrefs.getString("${currentUser}_email", "") ?: ""
            val dob = sharedPrefs.getString("${currentUser}_dob", "") ?: ""
            val avatarPath = sharedPrefs.getString("${currentUser}_avatar_path", null)

            etName.setText(name)
            etSurname.setText(surname)
            etEmail.setText(email)
            etDob.setText(dob)
            tvLogin.text = currentUser
            tvFullName.text = if (name.isEmpty() && surname.isEmpty()) "Ваш профіль" else "$name $surname"

            // --- ЗМІНИ ТУТ: ЛОГІКА АВАТАРА ЗА ЗАМОВЧУВАННЯМ ---
            if (avatarPath != null) {
                val file = File(avatarPath)
                if (file.exists()) {
                    ivAvatar.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
                    ivAvatar.setPadding(0, 0, 0, 0)
                } else {
                    setDefaultLogo()
                }
            } else {
                setDefaultLogo()
            }
        }
    }

    // Допоміжна функція для встановлення логотипа
    private fun setDefaultLogo() {
        ivAvatar.setImageResource(R.drawable.my_logo)
        ivAvatar.setPadding(0, 0, 0, 0) // Щоб логотип був чітким і без зайвих рамок
    }

    private fun saveChanges() {
        val newName = etName.text.toString().trim()
        val newSurname = etSurname.text.toString().trim()
        val newEmail = etEmail.text.toString().trim()
        val newDob = etDob.text.toString().trim()

        if (newName.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "Ім'я та Email обов'язкові!", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        sharedPrefs.edit().apply {
            putString("${currentUser}_name", newName)
            putString("${currentUser}_surname", newSurname)
            putString("${currentUser}_email", newEmail)
            putString("${currentUser}_dob", newDob)
            apply()
        }

        when {
            cameraBitmap != null -> saveBitmapToStorage(cameraBitmap!!)
            galleryUri != null -> saveUriToStorage(galleryUri!!)
        }

        Toast.makeText(this, "Дані успішно оновлено!", Toast.LENGTH_SHORT).show()
        tvFullName.text = "$newName $newSurname"
        finish()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this,
            { _, year, month, day ->
                val date = String.format("%02d.%02d.%d", day, month + 1, year)
                etDob.setText(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showImageSourceRequest() {
        val options = arrayOf("Галерея", "Камера")
        AlertDialog.Builder(this)
            .setTitle("Змінити фото")
            .setItems(options) { _, which ->
                if (which == 0) {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    pickImage.launch(intent)
                } else {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    takePhoto.launch(intent)
                }
            }.show()
    }

    private fun saveBitmapToStorage(bitmap: Bitmap) {
        val file = File(filesDir, "avatar_${currentUser}.jpg")
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit()
                .putString("${currentUser}_avatar_path", file.absolutePath).apply()
        } catch (e: IOException) { e.printStackTrace() }
    }

    private fun saveUriToStorage(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                val bitmap = BitmapFactory.decodeStream(input)
                saveBitmapToStorage(bitmap)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}