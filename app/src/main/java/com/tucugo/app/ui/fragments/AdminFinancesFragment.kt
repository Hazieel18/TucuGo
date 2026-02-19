package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.tucugo.app.R
import com.tucugo.app.data.repository.AdminRepository
import com.tucugo.app.databinding.FragmentAdminFinancesBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminFinancesFragment : Fragment() {

    private var _binding: FragmentAdminFinancesBinding? = null
    private val binding get() = _binding!!
    private val adminRepository = AdminRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminFinancesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeFinances()

        binding.btnExportReport.setOnClickListener {
            // Lógica para exportar reporte (ej. generar CSV o PDF)
            Toast.makeText(context, "Generando reporte mensual...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeFinances() {
        viewLifecycleOwner.lifecycleScope.launch {
            adminRepository.getIncomeSummary().collectLatest { summary ->
                binding.tvAdminProfit.text = getString(R.string.price_format, summary.totalAdminCommissions)
                binding.tvGrossRevenue.text = getString(R.string.price_format, summary.totalGrossRevenue)
                binding.tvDriverPayouts.text = getString(R.string.price_format, summary.totalDriverPayouts)
                binding.tvBusinessPayouts.text = getString(R.string.price_format, summary.totalBusinessPayouts)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
