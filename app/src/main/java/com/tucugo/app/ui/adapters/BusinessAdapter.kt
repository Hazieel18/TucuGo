package com.tucugo.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tucugo.app.data.models.Business
import com.tucugo.app.databinding.ItemBusinessBinding

class BusinessAdapter(
    private var businesses: List<Business>,
    private val onBusinessClick: (Business) -> Unit
) : RecyclerView.Adapter<BusinessAdapter.BusinessViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessViewHolder {
        val binding = ItemBusinessBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BusinessViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {
        holder.bind(businesses[position])
    }

    override fun getItemCount(): Int = businesses.size

    fun updateData(newList: List<Business>) {
        businesses = newList
        notifyDataSetChanged()
    }

    inner class BusinessViewHolder(private val binding: ItemBusinessBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(business: Business) {
            binding.tvBusinessName.text = business.name
            binding.tvBusinessCategory.text = business.category.replaceFirstChar { it.uppercase() }
            
            // Ripple effect y navegación
            binding.root.setOnClickListener { onBusinessClick(business) }
        }
    }
}
