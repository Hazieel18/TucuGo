package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tucugo.app.R
import com.tucugo.app.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    
    private val sharedPrefs by lazy {
        val masterKey = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        EncryptedSharedPreferences.create(
            requireContext(),
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = Firebase.auth
        
        setupListeners()
        checkIfUserIsLoggedIn()
    }
    
    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }
        
        binding.tvRegisterLink.setOnClickListener {
            // Asegúrate de que esta acción exista en tu nav_graph.xml
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.etEmail.error = "Email requerido"
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Contraseña requerida"
                false
            }
            password.length < 6 -> {
                binding.etPassword.error = "Mínimo 6 caracteres"
                false
            }
            else -> true
        }
    }
    
    private fun loginUser(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        // binding.progressBar?.visibility = View.VISIBLE // Descomenta si tienes un ProgressBar
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.btnLogin.isEnabled = true
                // binding.progressBar?.visibility = View.GONE
                
                if (task.isSuccessful) {
                    saveUserSession(email)
                    findNavController().navigate(R.id.action_loginFragment_to_nav_home)
                } else {
                    Toast.makeText(
                        context,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
    
    private fun saveUserSession(email: String) {
        sharedPrefs.edit()
            .putString("user_email", email)
            .putBoolean("is_logged_in", true)
            .apply()
    }
    
    private fun checkIfUserIsLoggedIn() {
        if (sharedPrefs.getBoolean("is_logged_in", false)) {
            findNavController().navigate(R.id.action_loginFragment_to_nav_home)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
