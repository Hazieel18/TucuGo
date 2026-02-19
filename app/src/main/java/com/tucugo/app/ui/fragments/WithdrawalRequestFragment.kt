package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.tucugo.app.data.repository.WalletRepository
import com.tucugo.app.databinding.FragmentWithdrawalRequestBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class WithdrawalRequestFragment : Fragment() {

    private var _binding: FragmentWithdrawalRequestBinding? = null
    private val binding get() = _binding!!
    
    private val walletRepository = WalletRepository()
    private val auth = FirebaseAuth.getInstance()
    private var currentBalance: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWithdrawalRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadBalance()

        binding.btnSubmitWithdrawal.setOnClickListener {
            submitWithdrawal()
        }
    }

    private fun loadBalance() {
        val userId = auth.currentUser?.uid ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val wallet = walletRepository.getOrCreateWallet(userId)
            currentBalance = wallet.balance
            binding.tvAvailableBalance.text = "Saldo disponible: $${String.format("%.2f", currentBalance)}"
        }
    }

    private fun submitWithdrawal() {
        val amountStr = binding.etWithdrawAmount.text.toString()
        val details = binding.etPaymentDetails.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        if (amountStr.isEmpty() || details.isEmpty()) {
            Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDouble()
        if (amount <= 0) {
            Toast.makeText(context, "Monto inválido", Toast.LENGTH_SHORT).show()
            return
        }

        if (amount > currentBalance) {
            Toast.makeText(context, "Saldo insuficiente", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            binding.btnSubmitWithdrawal.isEnabled = false
            val result = walletRepository.requestWithdrawal(userId, amount, details)
            if (result.isSuccess) {
                Toast.makeText(context, "Solicitud enviada con éxito", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                binding.btnSubmitWithdrawal.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
