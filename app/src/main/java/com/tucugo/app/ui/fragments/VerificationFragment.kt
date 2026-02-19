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
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tucugo.app.R
import com.tucugo.app.data.models.Vehicle
import com.tucugo.app.databinding.FragmentVerificationBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class VerificationFragment : Fragment() {

    private var _binding: FragmentVerificationBinding? = null
    private val binding get() = _binding!!
    private val args: VerificationFragmentArgs by navArgs()
    
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var cedulaUri: Uri? = null
    private var vehicleUri: Uri? = null

    private val pickCedula = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { cedulaUri = it; Toast.makeText(context, "Cédula seleccionada", Toast.LENGTH_SHORT).show() }
    }

    private val pickVehicle = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            vehicleUri = it
            binding.ivVehiclePreview.setImageURI(it)
            binding.ivVehiclePreview.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val requestedRole = args.requestRole
        setupUI(requestedRole)

        binding.btnUploadCedula.setOnClickListener { pickCedula.launch("image/*") }
        binding.btnUploadVehiclePhoto.setOnClickListener { pickVehicle.launch("image/*") }

        binding.btnSubmitVerification.setOnClickListener {
            submitVerification(requestedRole)
        }
    }

    private fun setupUI(role: String) {
        if (role == "business") {
            binding.tvVerificationTitle.text = "Registro de Comercio"
            binding.layoutBusinessFields.visibility = View.VISIBLE
            binding.layoutDriverFields.visibility = View.GONE
            binding.btnUploadVehiclePhoto.visibility = View.GONE
        } else {
            binding.tvVerificationTitle.text = "Registro de Conductor"
            binding.layoutBusinessFields.visibility = View.GONE
            binding.layoutDriverFields.visibility = View.VISIBLE
            binding.btnUploadVehiclePhoto.visibility = View.VISIBLE
        }
    }

    private fun submitVerification(role: String) {
        val userId = auth.currentUser?.uid ?: return
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.btnSubmitVerification.isEnabled = false
                
                // 1. Subir Documentos
                val cedulaUrl = cedulaUri?.let { uploadFile(it, "verifications/$userId/cedula.jpg") }
                val vehicleUrl = vehicleUri?.let { uploadFile(it, "verifications/$userId/vehicle.jpg") }

                if (cedulaUrl == null) {
                    Toast.makeText(context, "La foto de la cédula es obligatoria", Toast.LENGTH_SHORT).show()
                    binding.btnSubmitVerification.isEnabled = true
                    return@launch
                }

                // 2. Crear solicitud en Firestore
                val requestData = mutableMapOf<String, Any>(
                    "userId" to userId,
                    "role" to role,
                    "status" to "pending",
                    "timestamp" to System.currentTimeMillis(),
                    "cedulaUrl" to cedulaUrl
                )

                if (role == "business") {
                    requestData["businessName"] = binding.etBusinessName.text.toString()
                    requestData["rif"] = binding.etBusinessRif.text.toString()
                } else {
                    val vehicle = Vehicle(
                        driverId = userId,
                        brand = binding.etVehicleMake.text.toString(),
                        model = binding.etVehicleModel.text.toString(),
                        plateNumber = binding.etVehiclePlate.text.toString(),
                        capacity = binding.etVehicleCapacity.text.toString().toIntOrNull() ?: 1,
                        photoUrls = listOfNotNull(vehicleUrl),
                        status = "pending"
                    )
                    db.collection("vehicles").document(userId).set(vehicle).await()
                }

                db.collection("verification_requests").document(userId).set(requestData).await()
                
                Toast.makeText(context, "Solicitud enviada. Un administrador la revisará pronto.", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()

            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSubmitVerification.isEnabled = true
            }
        }
    }

    private suspend fun uploadFile(uri: Uri, path: String): String {
        val ref = storage.reference.child(path)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
