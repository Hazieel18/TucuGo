package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tucugo.app.data.models.PaymentProof
import com.tucugo.app.databinding.FragmentAdminVerifyPaymentsBinding
import com.tucugo.app.ui.adapters.AdminPaymentsAdapter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminVerifyPaymentsFragment : Fragment() {

    private var _binding: FragmentAdminVerifyPaymentsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var paymentsAdapter: AdminPaymentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminVerifyPaymentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observePendingPayments()
    }

    private fun setupRecyclerView() {
        paymentsAdapter = AdminPaymentsAdapter(
            emptyList(),
            onApprove = { payment -> processPayment(payment, "approved") },
            onReject = { payment -> processPayment(payment, "rejected") }
        )
        binding.rvPendingPayments.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = paymentsAdapter
        }
    }

    private fun observePendingPayments() {
        db.collection("payments")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, _ ->
                val payments = snapshot?.toObjects(PaymentProof::class.java) ?: emptyList()
                paymentsAdapter.updateData(payments)
            }
    }

    private fun processPayment(payment: PaymentProof, newStatus: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                db.runTransaction { transaction ->
                    val paymentRef = db.collection("payments").document(payment.referenceNumber)
                    transaction.update(paymentRef, "status", newStatus)
                    
                    payment.orderId?.let { orderId ->
                        val orderRef = db.collection("orders").document(orderId)
                        val orderStatus = if (newStatus == "approved") "preparing" else "payment_rejected"
                        transaction.update(orderRef, "status", orderStatus)
                    }
                }.await()
                
                val msg = if (newStatus == "approved") "Pago aprobado" else "Pago rechazado"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
