package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tucugo.app.data.models.Vehicle
import com.tucugo.app.data.repository.AdminRepository
import com.tucugo.app.databinding.FragmentAdminVerifyVehiclesBinding
import com.tucugo.app.ui.adapters.AdminVehiclesAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminVerifyVehiclesFragment : Fragment() {

    private var _binding: FragmentAdminVerifyVehiclesBinding? = null
    private val binding get() = _binding!!
    
    private val adminRepository = AdminRepository()
    private lateinit var vehiclesAdapter: AdminVehiclesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminVerifyVehiclesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadPendingVehicles()
    }

    private fun setupRecyclerView() {
        vehiclesAdapter = AdminVehiclesAdapter(
            emptyList(),
            onApprove = { vehicle -> verify(vehicle, true) },
            onReject = { vehicle -> verify(vehicle, false) }
        )
        
        binding.rvPendingVehicles.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = vehiclesAdapter
        }
    }

    private fun loadPendingVehicles() {
        viewLifecycleOwner.lifecycleScope.launch {
            adminRepository.getPendingVehicleVerifications().collectLatest { vehicles ->
                vehiclesAdapter.updateData(vehicles)
            }
        }
    }

    private fun verify(vehicle: Vehicle, approve: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = adminRepository.verifyVehicle(vehicle.driverId, approve)
            if (result.isSuccess) {
                val msg = if (approve) "Vehículo Aprobado" else "Vehículo Rechazado"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
