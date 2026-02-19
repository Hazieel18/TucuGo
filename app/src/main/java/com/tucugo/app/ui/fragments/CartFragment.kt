package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.tucugo.app.R
import com.tucugo.app.data.repository.CartRepository
import com.tucugo.app.databinding.FragmentCartBinding
import com.tucugo.app.utils.CurrencyUtils
import kotlinx.coroutines.launch

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar tasa del BCV al entrar al carrito
        viewLifecycleOwner.lifecycleScope.launch {
            CurrencyUtils.fetchCurrentRate()
            updateUI()
        }

        CartRepository.cartItems.observe(viewLifecycleOwner) {
            updateUI()
        }

        binding.btnCheckout.setOnClickListener {
            val total = CartRepository.calculateTotal() + 2.0
            val bundle = Bundle().apply {
                putDouble("amountToPay", total)
                putString("orderId", "ORDER_${System.currentTimeMillis()}")
            }
            findNavController().navigate(R.id.paymentUploadFragment, bundle)
        }
    }

    private fun updateUI() {
        val subtotal = CartRepository.calculateTotal()
        val deliveryFee = 2.0 // Fijo
        val totalUsd = subtotal + deliveryFee
        val totalVes = totalUsd * CurrencyUtils.currentRate

        binding.tvSubtotal.text = "$${String.format("%.2f", subtotal)}"
        binding.tvDeliveryFee.text = "$${String.format("%.2f", deliveryFee)}"
        binding.tvTotalUsd.text = "$${String.format("%.2f", totalUsd)}"
        
        // Mostrar conversión a Bolívares
        binding.tvTotalVes.text = "Total: ${String.format("%.2f", totalVes)} BS"
        binding.tvExchangeRate.text = "Tasa BCV: ${String.format("%.2f", CurrencyUtils.currentRate)} BS/$"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
