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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.tucugo.app.R
import com.tucugo.app.data.models.User
import com.tucugo.app.data.repository.UserRepository
import com.tucugo.app.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val userRepository = UserRepository()

    private var profileUri: Uri? = null
    private var cedulaUri: Uri? = null
    private var selfieUri: Uri? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { handleImageSelection(it) }
    }
    
    private var currentImageType = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivProfilePhoto.setOnClickListener { 
            currentImageType = "profile"
            selectImageLauncher.launch("image/*") 
        }
        binding.ivCedulaPhoto.setOnClickListener { 
            currentImageType = "cedula"
            selectImageLauncher.launch("image/*") 
        }
        binding.ivSelfiePhoto.setOnClickListener { 
            currentImageType = "selfie"
            selectImageLauncher.launch("image/*") 
        }

        binding.btnRegister.setOnClickListener {
            performRegistration()
        }
    }

    private fun handleImageSelection(uri: Uri) {
        when (currentImageType) {
            "profile" -> { profileUri = uri; binding.ivProfilePhoto.setImageURI(uri) }
            "cedula" -> { cedulaUri = uri; binding.ivCedulaPhoto.setImageURI(uri) }
            "selfie" -> { selfieUri = uri; binding.ivSelfiePhoto.setImageURI(uri) }
        }
    }

    private fun performRegistration() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val cedula = binding.etCedula.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || cedula.isEmpty() ||
            profileUri == null || cedulaUri == null || selfieUri == null) {
            Toast.makeText(context, "Por favor, completa todos los campos y fotos", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid ?: throw Exception("Error al crear usuario")

                val profileUrl = uploadImage(profileUri!!, "profiles/$userId")
                val cedulaUrl = uploadImage(cedulaUri!!, "verifications/$userId/cedula")
                val selfieUrl = uploadImage(selfieUri!!, "verifications/$userId/selfie")

                val user = User(
                    id = userId,
                    cedula = cedula,
                    photoUrl = profileUrl,
                    photoCedulaUrl = cedulaUrl,
                    phoneNumber = phone,
                    email = email,
                    role = "customer",
                    isVerified = false // Pendiente de revisión admin
                )

                userRepository.saveUser(user)
                
                Toast.makeText(context, "Registro exitoso. Tu cuenta está en revisión.", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.nav_home)
                
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true
            }
        }
    }

    private suspend fun uploadImage(uri: Uri, path: String): String {
        val ref = storage.reference.child(path)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
