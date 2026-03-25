package com.example.finalproject

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class RecipeAdapter(
    private var recipes: List<RecipeItem>,
    private val isMyRecipesScreen: Boolean = false,
    private val onDeleteClick: (RecipeItem) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvRecipeTitle)
        val category: TextView = view.findViewById(R.id.tvRecipeCategory)
        val time: TextView = view.findViewById(R.id.tvRecipeTime)
        val author: TextView = view.findViewById(R.id.tvRecipeAuthor)
        val image: ImageView = view.findViewById(R.id.ivRecipeImage)

        val controlButtons: View = view.findViewById(R.id.llControlButtons)
        val btnDelete: ImageView = view.findViewById(R.id.btnDeleteRecipe)
        val btnEdit: ImageView = view.findViewById(R.id.btnEditRecipe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe_card, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]

        holder.title.text = recipe.title
        holder.category.text = recipe.category
        holder.time.text = "${recipe.timeMins} хв"

        // --- ЛОГІКА КЕРУВАННЯ ---
        holder.controlButtons.visibility = if (isMyRecipesScreen) View.VISIBLE else View.GONE
        holder.btnDelete.setOnClickListener { onDeleteClick(recipe) }

        // --- ЛОГІКА ДЛЯ АВТОРА ---
        val authorName = recipe.author ?: ""
        val isSystem = authorName.equals("Система", ignoreCase = true) ||
                authorName.equals("System", ignoreCase = true) ||
                authorName.isEmpty()

        if (isSystem) {
            holder.author.visibility = View.GONE
        } else {
            holder.author.visibility = View.VISIBLE
            holder.author.text = authorName
            holder.author.setTextColor(Color.WHITE)
        }

        // --- ЛОГІКА ДЛЯ ФОТО (ВИПРАВЛЕНО) ---
        if (!recipe.imagePath.isNullOrEmpty()) {
            try {
                val imgFile = File(recipe.imagePath!!)
                if (imgFile.exists()) {
                    // Якщо це шлях до файлу (внутрішня пам'ять)
                    holder.image.setImageURI(Uri.fromFile(imgFile))
                } else {
                    // Якщо файл не знайдено, спробуємо розпарсити як звичайний Uri (для DummyData)
                    holder.image.setImageURI(Uri.parse(recipe.imagePath))
                }
            } catch (e: Exception) {
                holder.image.setImageResource(R.drawable.ic_launcher_foreground)
            }
        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }

    override fun getItemCount(): Int = recipes.size

    fun updateData(newRecipes: List<RecipeItem>) {
        this.recipes = newRecipes
        notifyDataSetChanged()
    }
}