package com.tucugo.app.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import coil.load
import com.tucugo.app.data.models.Trip
import com.tucugo.app.data.repository.TripRepository
import com.tucugo.app.databinding.FragmentActiveTripBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions

class ActiveTripFragment : Fragment() {

    private var _binding: FragmentActiveTripBinding? = null
    private val binding get() = _binding!!
    private val tripRepository = TripRepository()
    private var mapLibreMap: MapLibreMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActiveTripBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val tripId = arguments?.getString("tripId") ?: return

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { map ->
            mapLibreMap = map
            map.setStyle(Style.getPredefinedStyle("Streets"))
            observeTripUpdates(tripId)
        }
    }

    private fun observeTripUpdates(tripId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            tripRepository.getTripUpdates(tripId).collectLatest { trip ->
                trip?.let { updateUI(it) }
            }
        }
    }

    private fun updateUI(trip: Trip) {
        binding.tvDriverName.text = "Conductor asignado"
        binding.tvVehicleDetails.text = "Tipo: ${trip.tripType.uppercase()}"
        
        // Actualizar posición en el mapa
        trip.driverLocation?.let { loc ->
            val pos = LatLng(loc.lat, loc.lng)
            mapLibreMap?.clear()
            mapLibreMap?.addMarker(MarkerOptions().position(pos).title("Conductor"))
            mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15.0))
        }

        binding.btnCallDriver.setOnClickListener {
            // Lógica para llamar (requiere número en el modelo, se asocia al perfil)
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:04120000000"))
            startActivity(intent)
        }
    }

    override fun onStart() { super.onStart(); binding.mapView.onStart() }
    override fun onResume() { super.onResume(); binding.mapView.onResume() }
    override fun onPause() { super.onResume(); binding.mapView.onPause() }
    override fun onStop() { super.onStop(); binding.mapView.onStop() }
    override fun onLowMemory() { super.onLowMemory(); binding.mapView.onLowMemory() }
    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }
}
