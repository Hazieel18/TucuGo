package com.tucugo.app.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.tucugo.app.data.models.PaymentProof
import com.tucugo.app.data.repository.AdminRepository
import com.tucugo.app.data.repository.PaymentRepository
import com.tucugo.app.databinding.FragmentPaymentUploadBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class PaymentUploadFragment : Fragment() {

    private var _binding: FragmentPaymentUploadBinding? = null
    private val binding get() = _binding!!
    
    private val adminRepository = AdminRepository()
    private val paymentRepository = PaymentRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivPaymentProofPreview.setImageURI(it)
            binding.ivPaymentProofPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadAdminPaymentInfo()

        binding.btnSelectProofPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSubmitPayment.setOnClickListener {
            submitPayment()
        }
    }

    private fun loadAdminPaymentInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            val config = adminRepository.getPaymentConfig()
            if (config != null) {
                val info = """
                    Pago Móvil: ${config.phoneForPagoMovil} (${config.idForPagoMovil})
                    Transferencia: ${config.bankName} - ${config.accountNumber}
                    Titular: ${config.accountHolder}
                    Binance ID: ${config.binanceId}
                    Zinli Email: ${config.zinliEmail}
                """.trimIndent()
                binding.tvAdminPaymentDetails.text = info
            } else {
                binding.tvAdminPaymentDetails.text = "Error al cargar datos de pago. Intenta más tarde."
            }
        }
    }

    private fun submitPayment() {
        val reference = binding.etReferenceNumber.text.toString()
        val amountStr = binding.etPaymentAmount.text.toString()
        val uri = selectedImageUri

        if (reference.isEmpty() || amountStr.isEmpty() || uri == null) {
            Toast.makeText(context, "Por favor completa todos los campos y adjunta la foto", Toast.LENGTH_SHORT).show()
            return
        }

        val proof = PaymentProof(
            userId = auth.currentUser?.uid ?: "",
            amount = amountStr.toDouble(),
            referenceNumber = reference,
            paymentMethod = binding.spinnerPaymentMethod.selectedItem.toString(),
            status = "pending"
        )

        viewLifecycleOwner.lifecycleScope.launch {
            binding.btnSubmitPayment.isEnabled = false
            val result = paymentRepository.submitPayment(proof, uri)
            if (result.isSuccess) {
                Toast.makeText(context, "Pago reportado. Esperando validación del administrador.", Toast.LENGTH_LONG).show()
                findNavController().popBackStack(com.tucugo.app.R.id.nav_home, false)
            } else {
                Toast.makeText(context, "Error al enviar el reporte", Toast.LENGTH_SHORT).show()
                binding.btnSubmitPayment.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
