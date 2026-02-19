package com.tucugo.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tucugo.app.databinding.ItemCategoryBinding
import com.tucugo.app.ui.viewmodel.DashboardViewModel

class CategoryAdapter(
    private val categories: List<DashboardViewModel.CategoryItem>,
    private val onCategoryClick: (DashboardViewModel.CategoryItem) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: DashboardViewModel.CategoryItem) {
            binding.tvCategoryName.text = category.name
            binding.root.setOnClickListener { onCategoryClick(category) }
        }
    }
}
