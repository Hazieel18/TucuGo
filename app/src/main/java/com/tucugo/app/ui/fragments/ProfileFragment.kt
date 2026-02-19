package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.tucugo.app.R
import com.tucugo.app.data.models.User
import com.tucugo.app.data.repository.WalletRepository
import com.tucugo.app.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val walletRepository = WalletRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()
        setupClickListeners()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null || !isAdded) return@addSnapshotListener
            
            val user = snapshot.toObject(User::class.java) ?: return@addSnapshotListener
            binding.tvUserName.text = user.email.split("@")[0].replaceFirstChar { it.uppercase() }
            binding.chipRole.text = user.role.uppercase()
            
            // Cargar foto de perfil si existe
            user.photoUrl?.let {
                binding.ivUserProfile.load(it) {
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_foreground)
                    transformations(CircleCropTransformation())
                }
            }

            // Lógica de visibilidad de la billetera
            if (user.role == "customer") {
                binding.cardWallet.visibility = View.GONE
                binding.btnVerifyDriver.visibility = View.VISIBLE
                binding.btnVerifyBusiness.visibility = View.VISIBLE
            } else {
                binding.cardWallet.visibility = View.VISIBLE
                binding.btnVerifyDriver.visibility = View.GONE
                binding.btnVerifyBusiness.visibility = View.GONE
                loadWalletBalance(userId)
            }
        }
    }

    private fun loadWalletBalance(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val wallet = walletRepository.getOrCreateWallet(userId)
                binding.tvWalletBalance.text = "$${String.format("%.2f", wallet.balance)}"
            } catch (e: Exception) {
                binding.tvWalletBalance.text = "$0.00"
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnWithdraw.setOnClickListener {
            findNavController().navigate(R.id.withdrawalRequestFragment)
        }

        binding.btnVerifyDriver.setOnClickListener {
            val bundle = Bundle().apply { putString("requestRole", "driver") }
            findNavController().navigate(R.id.verificationFragment, bundle)
        }

        binding.btnVerifyBusiness.setOnClickListener {
            val bundle = Bundle().apply { putString("requestRole", "business") }
            findNavController().navigate(R.id.verificationFragment, bundle)
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_nav_profile_to_welcomeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
