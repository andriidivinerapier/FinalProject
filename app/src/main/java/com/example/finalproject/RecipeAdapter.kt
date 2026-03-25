package com.example.finalproject

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
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
        val btnOpenRecipe: Button = view.findViewById(R.id.btnOpenFullRecipe)

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

        // --- НОВА ЛОГІКА: ВІДКРИТТЯ МОДАЛЬНОГО ВІКНА ---
        holder.btnOpenRecipe.setOnClickListener {
            showRecipeDetails(holder.itemView.context, recipe)
        }

        // --- ЛОГІКА КЕРУВАННЯ (ДЛЯ МОЇХ РЕЦЕПТІВ) ---
        if (isMyRecipesScreen) {
            holder.controlButtons.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener { onDeleteClick(recipe) }
            holder.btnEdit.setOnClickListener {
                val intent = Intent(holder.itemView.context, AddRecipeActivity::class.java)
                intent.putExtra("edit_recipe", recipe)
                holder.itemView.context.startActivity(intent)
            }
        } else {
            holder.controlButtons.visibility = View.GONE
        }

        // --- ЛОГІКА ДЛЯ АВТОРА ---
        val authorName = recipe.author ?: ""
        if (authorName.equals("Система", ignoreCase = true) || authorName.isEmpty()) {
            holder.author.visibility = View.GONE
        } else {
            holder.author.visibility = View.VISIBLE
            holder.author.text = authorName
            holder.author.setTextColor(Color.WHITE)
        }

        // --- ЛОГІКА ДЛЯ ФОТО ---
        renderImage(recipe.imagePath, holder.image)
    }

    // МЕТОД ДЛЯ СТВОРЕННЯ МОДАЛЬНОГО ВІКНА (BOTTOM SHEET)
    private fun showRecipeDetails(context: Context, recipe: RecipeItem) {
        val dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_recipe_details, null)

        val ivDetailImage = view.findViewById<ImageView>(R.id.ivDetailImage)
        val tvDetailTitle = view.findViewById<TextView>(R.id.tvDetailTitle)
        val tvDetailDifficulty = view.findViewById<TextView>(R.id.tvDetailDifficulty)
        val tvDetailTime = view.findViewById<TextView>(R.id.tvDetailTime)
        val tvDetailIngredients = view.findViewById<TextView>(R.id.tvDetailIngredients)
        val tvDetailSteps = view.findViewById<TextView>(R.id.tvDetailSteps)
        val btnClose = view.findViewById<ImageButton>(R.id.btnClose)

        // ЗАПОВНЕННЯ ДАНИХ
        tvDetailTitle.text = recipe.title
        tvDetailTime.text = "${recipe.timeMins} хв"

        // Встановлюємо текст і колір (щоб точно було видно)
        tvDetailDifficulty.text = recipe.difficulty
        tvDetailDifficulty.setTextColor(Color.WHITE) // Явно робимо білим

        tvDetailIngredients.text = recipe.ingredients.split("\n")
            .filter { it.isNotBlank() }
            .joinToString("\n") { "• $it" }

        tvDetailSteps.text = recipe.instructions.split("\n")
            .filter { it.isNotBlank() }
            .mapIndexed { index, s -> "${index + 1}. $s" }
            .joinToString("\n\n")

        renderImage(recipe.imagePath, ivDetailImage)

        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    // Допоміжна функція для обробки зображень
    private fun renderImage(path: String?, imageView: ImageView) {
        if (!path.isNullOrEmpty()) {
            try {
                val imgFile = File(path)
                if (imgFile.exists()) {
                    imageView.setImageBitmap(BitmapFactory.decodeFile(imgFile.absolutePath))
                } else {
                    imageView.setImageURI(Uri.parse(path))
                }
            } catch (e: Exception) {
                imageView.setImageResource(R.drawable.ic_launcher_foreground)
            }
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }

    override fun getItemCount(): Int = recipes.size

    fun updateData(newRecipes: List<RecipeItem>) {
        this.recipes = newRecipes
        notifyDataSetChanged()
    }
}