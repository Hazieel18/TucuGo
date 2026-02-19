package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tucugo.app.data.models.Order
import com.tucugo.app.data.repository.OrderRepository
import com.tucugo.app.databinding.FragmentBusinessDashboardBinding
import com.tucugo.app.ui.adapters.BusinessOrdersAdapter
import com.tucugo.app.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BusinessDashboardFragment : Fragment() {

    private var _binding: FragmentBusinessDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val orderRepository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var ordersAdapter: BusinessOrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusinessDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadBusinessOrders()
    }

    private fun setupRecyclerView() {
        ordersAdapter = BusinessOrdersAdapter(
            emptyList(),
            onAcceptClick = { order -> acceptOrder(order) },
            onDenyClick = { order -> denyOrder(order) },
            onReadyClick = { order -> readyOrder(order) }
        )
        
        binding.rvBusinessOrders.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ordersAdapter
        }
    }

    private fun loadBusinessOrders() {
        val businessId = auth.currentUser?.uid ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            orderRepository.getPendingOrdersForBusiness(businessId).collectLatest { orders ->
                ordersAdapter.updateData(orders)
            }
        }
    }

    private fun acceptOrder(order: Order) {
        viewLifecycleOwner.lifecycleScope.launch {
            orderRepository.updateOrderStatus(order.id, Constants.STATUS_PREPARING)
            Toast.makeText(context, "Pedido aceptado. Iniciando preparación.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun denyOrder(order: Order) {
        viewLifecycleOwner.lifecycleScope.launch {
            orderRepository.updateOrderStatus(order.id, Constants.STATUS_CANCELED)
            Toast.makeText(context, "Pedido denegado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readyOrder(order: Order) {
        viewLifecycleOwner.lifecycleScope.launch {
            orderRepository.updateOrderStatus(order.id, Constants.STATUS_READY)
            Toast.makeText(context, "Pedido listo para retirar.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
