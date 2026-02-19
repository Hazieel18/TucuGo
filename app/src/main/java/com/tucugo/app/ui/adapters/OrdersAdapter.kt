package com.tucugo.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tucugo.app.data.models.Order
import com.tucugo.app.databinding.ItemOrderBinding
import java.text.SimpleDateFormat
import java.util.Locale

class OrdersAdapter(
    private var orders: List<Order>,
    private val onTrackClick: (Order) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateData(newList: List<Order>) {
        orders = newList
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(order: Order) {
            binding.tvOrderId.text = "Pedido #${order.id.takeLast(5)}"
            binding.tvOrderDate.text = order.orderDate?.let { dateFormatter.format(it) } ?: ""
            binding.tvOrderAmount.text = "$${String.format("%.2f", order.totalAmount)}"
            binding.tvOrderStatus.text = order.status.uppercase()

            // Solo mostrar botón de seguimiento si está en camino
            binding.btnTrackOrder.visibility = if (order.status == "shipping" || order.status == "preparing") {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.btnTrackOrder.setOnClickListener { onTrackClick(order) }
        }
    }
}
