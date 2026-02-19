package com.tucugo.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.tucugo.app.data.models.PaymentProof
import com.tucugo.app.databinding.ItemPendingPaymentBinding

class AdminPaymentsAdapter(
    private var payments: List<PaymentProof>,
    private val onApprove: (PaymentProof) -> Unit,
    private val onReject: (PaymentProof) -> Unit
) : RecyclerView.Adapter<AdminPaymentsAdapter.PaymentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPendingPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(payments[position])
    }

    override fun getItemCount(): Int = payments.size

    fun updateData(newList: List<PaymentProof>) {
        payments = newList
        notifyDataSetChanged()
    }

    inner class PaymentViewHolder(private val binding: ItemPendingPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(payment: PaymentProof) {
            binding.tvPaymentMethod.text = payment.paymentMethod
            binding.tvAmount.text = "$${String.format("%.2f", payment.amount)}"
            binding.tvReference.text = "Ref: ${payment.referenceNumber}"
            
            binding.ivProofPreview.load(payment.proofPhotoUrl) {
                crossfade(true)
            }

            binding.btnApprove.setOnClickListener { onApprove(payment) }
            binding.btnReject.setOnClickListener { onReject(payment) }
        }
    }
}
