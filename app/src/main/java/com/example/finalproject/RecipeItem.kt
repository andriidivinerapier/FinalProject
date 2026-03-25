package com.example.finalproject

import java.io.Serializable

data class RecipeItem(
    val id: Long,
    val title: String,
    val category: String,
    val timeMins: Int,
    val difficulty: String, // <--- НОВЕ ПОЛЕ ДЛЯ СКЛАДНОСТІ
    val author: String = "Система",
    val imagePath: String? = null,
    val ingredients: String = "",
    val instructions: String = ""
) : Serializable