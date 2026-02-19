package com.tucugo.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.tucugo.app.R
import com.tucugo.app.data.models.Product
import com.tucugo.app.databinding.ItemProductBinding

class ProductAdapter(
    private var products: List<Product>,
    private val onAddClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newList: List<Product>) {
        products = newList
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(product: Product) {
            val context = itemView.context
            binding.tvProductName.text = product.name
            binding.tvProductDescription.text = product.description
            
            // Usamos un recurso de string para el formato de moneda (Evita hardcoding)
            binding.tvProductPrice.text = context.getString(R.string.price_format, product.price)
            
            if (product.photosUrl.isNotEmpty()) {
                binding.ivProductImage.load(product.photosUrl[0]) {
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_background)
                    error(R.drawable.ic_launcher_background)
                }
            }

            binding.btnAddToCart.setOnClickListener { onAddClick(product) }
        }
    }
}
