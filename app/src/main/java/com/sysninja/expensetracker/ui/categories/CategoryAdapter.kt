package com.sysninja.expensetracker.ui.categories

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.data.local.entity.CategoryEntity
import com.sysninja.expensetracker.databinding.ItemCategoryCardBinding

class CategoryAdapter(
    private val onCategoryClick: (CategoryEntity) -> Unit,
    private val onEditClick: (CategoryEntity) -> Unit,
    private val onDeleteClick: (CategoryEntity) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val categories = mutableListOf<CategoryEntity>()

    fun submitList(newCategories: List<CategoryEntity>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(
        private val binding: ItemCategoryCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryEntity) {
            val context = binding.root.context
            binding.tvCategoryName.text = category.name

            val isExpense = category.type.equals("EXPENSE", ignoreCase = true)
            binding.tvCategoryType.text = if (isExpense) context.getString(R.string.type_expense) else context.getString(R.string.type_income)

            if (category.isDefault) {
                binding.tvDefaultBadge.visibility = View.VISIBLE
                binding.btnDeleteCategory.visibility = View.GONE
            } else {
                binding.tvDefaultBadge.visibility = View.GONE
                binding.btnDeleteCategory.visibility = View.VISIBLE
            }

            // Tint icon container based on colorHex
            try {
                val color = Color.parseColor(category.colorHex)
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.argb(40, Color.red(color), Color.green(color), Color.blue(color)))
                    setStroke(2, color)
                }
                binding.flCategoryIcon.background = bg
                binding.ivCategoryIcon.setColorFilter(color)
            } catch (e: Exception) {
                binding.flCategoryIcon.setBackgroundResource(R.drawable.bg_circle_savings)
                binding.ivCategoryIcon.setColorFilter(ContextCompat.getColor(context, R.color.teal_primary))
            }

            binding.cardCategory.setOnClickListener { onCategoryClick(category) }
            binding.btnEditCategory.setOnClickListener { onEditClick(category) }
            binding.btnDeleteCategory.setOnClickListener { onDeleteClick(category) }
        }
    }
}
