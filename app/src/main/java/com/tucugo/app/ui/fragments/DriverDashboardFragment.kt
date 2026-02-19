package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tucugo.app.data.models.Trip
import com.tucugo.app.data.repository.TripRepository
import com.tucugo.app.databinding.FragmentDriverDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DriverDashboardFragment : Fragment() {

    private var _binding: FragmentDriverDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val tripRepository = TripRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriverDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupOnlineSwitch()
    }

    private fun setupRecyclerView() {
        binding.rvAvailableTrips.layoutManager = LinearLayoutManager(context)
        // Aquí se inicializaría el adaptador de viajes
    }

    private fun setupOnlineSwitch() {
        binding.switchOnlineStatus.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.switchOnlineStatus.text = "Estado: En línea"
                startListeningForTrips()
            } else {
                binding.switchOnlineStatus.text = "Estado: Desconectado"
            }
        }
    }

    private fun startListeningForTrips() {
        val tripType = "taxi" // Esto se obtendría del perfil del conductor
        
        viewLifecycleOwner.lifecycleScope.launch {
            tripRepository.getAvailableTrips(tripType).collectLatest { trips ->
                // Actualizar UI con la lista de viajes disponibles
            }
        }
    }

    private fun acceptTrip(trip: Trip) {
        val driverId = auth.currentUser?.uid ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val result = tripRepository.acceptTrip(trip.id, driverId)
            if (result.isSuccess) {
                Toast.makeText(context, "Viaje aceptado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
