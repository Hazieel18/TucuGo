package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.tucugo.app.R
import com.tucugo.app.databinding.FragmentOrderTrackingBinding

class OrderTrackingFragment : Fragment() {

    private var _binding: FragmentOrderTrackingBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val orderId = arguments?.getString("orderId") ?: "ID-TEMP"
        binding.tvOrderId.text = "#$orderId"

        listenToOrderUpdates(orderId)
        
        binding.btnBackToHome.setOnClickListener {
            findNavController().navigate(R.id.nav_home)
        }
    }

    private fun listenToOrderUpdates(orderId: String) {
        db.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, _ ->
                val status = snapshot?.getString("status") ?: "pending"
                updateStatusUI(status)
            }
    }

    private fun updateStatusUI(status: String) {
        // Reset steps
        binding.ivStepPending.alpha = 0.3f
        binding.ivStepPreparing.alpha = 0.3f
        binding.ivStepShipping.alpha = 0.3f

        when (status) {
            "pending" -> {
                binding.tvCurrentStatus.text = "Buscando repartidor..."
                binding.ivStepPending.alpha = 1.0f
            }
            "preparing" -> {
                binding.tvCurrentStatus.text = "El comercio está preparando tu pedido"
                binding.ivStepPending.alpha = 1.0f
                binding.ivStepPreparing.alpha = 1.0f
            }
            "shipping" -> {
                binding.tvCurrentStatus.text = "¡Tu pedido está en camino!"
                binding.ivStepPending.alpha = 1.0f
                binding.ivStepPreparing.alpha = 1.0f
                binding.ivStepShipping.alpha = 1.0f
            }
            "delivered" -> {
                binding.tvCurrentStatus.text = "¡Pedido entregado con éxito!"
                binding.ivStepPending.alpha = 1.0f
                binding.ivStepPreparing.alpha = 1.0f
                binding.ivStepShipping.alpha = 1.0f
                binding.btnBackToHome.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
