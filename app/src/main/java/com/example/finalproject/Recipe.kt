package com.example.finalproject

data class Recipe(
    val id: String,          // Унікальний ID
    val title: String,       // Назва
    val category: String,    // Категорія
    val ingredients: String, // Інгредієнти
    val instructions: String,// Опис приготування
    val imagePath: String? = null // Шлях до фото (якщо буде)
)