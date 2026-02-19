package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.tucugo.app.data.models.SystemSettings
import com.tucugo.app.databinding.FragmentAdminSettingsBinding
import com.tucugo.app.utils.CurrencyUtils
import com.tucugo.app.utils.FirebaseSeeder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminSettingsFragment : Fragment() {

    private var _binding: FragmentAdminSettingsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCurrentConfig()

        binding.btnFetchExternalRate.setOnClickListener {
            suggestExternalRate()
        }

        binding.btnSaveConfig.setOnClickListener {
            saveConfiguration()
        }

        binding.btnSeedData.setOnClickListener {
            seedDatabase()
        }
    }

    private fun loadCurrentConfig() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val doc = db.collection("system_settings").document("bcv").get().await()
                val settings = doc.toObject(SystemSettings::class.java)
                settings?.let {
                    binding.etBcvRate.setText(it.rate.toString())
                    binding.etPagoMovilPhone.setText(it.pagoMovilPhone)
                    binding.etPagoMovilId.setText(it.pagoMovilId)
                    binding.etBankName.setText(it.bankName)
                    binding.etAccountNumber.setText(it.accountNumber)
                    binding.etAccountHolder.setText(it.accountHolder)
                    binding.etBinanceId.setText(it.binanceId)
                    binding.etZinliEmail.setText(it.zinliEmail)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar configuración", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun suggestExternalRate() {
        viewLifecycleOwner.lifecycleScope.launch {
            val externalRate = CurrencyUtils.fetchExternalRate()
            if (externalRate != null) {
                binding.etBcvRate.setText(externalRate.toString())
                Toast.makeText(context, "Tasa sugerida desde API externa", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No se pudo obtener tasa externa", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveConfiguration() {
        val settings = SystemSettings(
            rate = binding.etBcvRate.text.toString().toDoubleOrNull() ?: 36.5,
            pagoMovilPhone = binding.etPagoMovilPhone.text.toString(),
            pagoMovilId = binding.etPagoMovilId.text.toString(),
            bankName = binding.etBankName.text.toString(),
            accountNumber = binding.etAccountNumber.text.toString(),
            accountHolder = binding.etAccountHolder.text.toString(),
            binanceId = binding.etBinanceId.text.toString(),
            zinliEmail = binding.etZinliEmail.text.toString()
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                db.collection("system_settings").document("bcv").set(settings).await()
                Toast.makeText(context, "Configuración guardada correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun seedDatabase() {
        viewLifecycleOwner.lifecycleScope.launch {
            FirebaseSeeder.seedAll()
            Toast.makeText(context, "Base de datos poblada con éxito", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
