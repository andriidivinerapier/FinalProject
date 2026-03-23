package com.example.finalproject

// Проста структура для зберігання даних рецепту
data class RecipeItem(
    val id: Int,
    val title: String,
    val category: String,
    val timeMins: Int
    // У реальному проекті тут було б посилання на фото (URL або Drawable ID)
)