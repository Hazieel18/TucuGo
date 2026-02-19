package com.tucugo.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tucugo.app.data.models.Order
import com.tucugo.app.databinding.ItemBusinessOrderBinding
import com.tucugo.app.utils.Constants

class BusinessOrdersAdapter(
    private var orders: List<Order>,
    private val onAcceptClick: (Order) -> Unit,
    private val onDenyClick: (Order) -> Unit,
    private val onReadyClick: (Order) -> Unit
) : RecyclerView.Adapter<BusinessOrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemBusinessOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class OrderViewHolder(private val binding: ItemBusinessOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(order: Order) {
            binding.tvOrderId.text = "Pedido #${order.id.takeLast(5)}"
            binding.tvOrderAmount.text = "$${String.format("%.2f", order.totalAmount)}"
            binding.tvOrderStatus.text = "Estado: ${order.status}"
            binding.tvOrderDetails.text = if (order.isDelivery) "Para envío a: ${order.deliveryAddress.address}" else "Retiro en tienda"

            when (order.status) {
                Constants.STATUS_PENDING -> {
                    binding.btnAcceptOrder.visibility = View.VISIBLE
                    binding.btnDenyOrder.visibility = View.VISIBLE
                    binding.btnReadyOrder.visibility = View.GONE
                }
                Constants.STATUS_PREPARING -> {
                    binding.btnAcceptOrder.visibility = View.GONE
                    binding.btnDenyOrder.visibility = View.GONE
                    binding.btnReadyOrder.visibility = View.VISIBLE
                }
                else -> {
                    binding.btnAcceptOrder.visibility = View.GONE
                    binding.btnDenyOrder.visibility = View.GONE
                    binding.btnReadyOrder.visibility = View.GONE
                }
            }

            binding.btnAcceptOrder.setOnClickListener { onAcceptClick(order) }
            binding.btnDenyOrder.setOnClickListener { onDenyClick(order) }
            binding.btnReadyOrder.setOnClickListener { onReadyClick(order) }
        }
    }
}
