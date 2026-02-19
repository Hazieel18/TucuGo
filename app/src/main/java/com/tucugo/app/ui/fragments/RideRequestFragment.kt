package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.tucugo.app.R
import com.tucugo.app.data.models.Trip
import com.tucugo.app.data.repository.OrderRepository
import com.tucugo.app.databinding.FragmentRideRequestBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RideRequestFragment : Fragment() {

    private var _binding: FragmentRideRequestBinding? = null
    private val binding get() = _binding!!
    
    private val orderRepository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRideRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRequestRide.setOnClickListener {
            performRideRequest()
        }
    }

    private fun performRideRequest() {
        val pickup = binding.etPickupLocation.text.toString().trim()
        val destination = binding.etDestination.text.toString().trim()
        val passengers = binding.spinnerPassengers.selectedItem.toString().toIntOrNull() ?: 1
        val userId = auth.currentUser?.uid ?: return

        if (pickup.isEmpty() || destination.isEmpty()) {
            Toast.makeText(context, "Por favor indica origen y destino", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRequestRide.isEnabled = false
        
        val tripRequest = Trip(
            userId = userId,
            pickupLocation = pickup,
            destinationLocation = destination,
            numberOfPassengers = passengers,
            status = "requested"
        )

        viewLifecycleOwner.lifecycleScope.launch {
            val result = orderRepository.requestTrip(tripRequest)
            if (result.isSuccess) {
                Toast.makeText(context, "Buscando conductores cercanos...", Toast.LENGTH_LONG).show()
                // Navegamos al fragmento de viaje activo pasando el ID generado
                val bundle = Bundle().apply { putString("tripId", result.getOrNull()) }
                findNavController().navigate(R.id.activeTripFragment, bundle)
            } else {
                Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                binding.btnRequestRide.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
