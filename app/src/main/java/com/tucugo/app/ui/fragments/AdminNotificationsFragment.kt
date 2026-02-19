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
import com.tucugo.app.databinding.FragmentAdminNotificationsBinding
import kotlinx.coroutines.launch

class AdminNotificationsFragment : Fragment() {

    private var _binding: FragmentAdminNotificationsBinding? = null
    private val binding get() = _binding!!
    private val adminRepository = AdminRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSendNotification.setOnClickListener {
            sendNotification()
        }
    }

    private fun sendNotification() {
        val title = binding.etNotificationTitle.text.toString().trim()
        val message = binding.etNotificationBody.text.toString().trim()
        
        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val target = when (binding.rgTarget.checkedRadioButtonId) {
            R.id.rbDrivers -> "drivers"
            R.id.rbBusinesses -> "businesses"
            else -> "all"
        }

        viewLifecycleOwner.lifecycleScope.launch {
            binding.btnSendNotification.isEnabled = false
            val result = adminRepository.sendBroadcastNotification(title, message, target)
            if (result.isSuccess) {
                Toast.makeText(context, "Notificación programada con éxito", Toast.LENGTH_SHORT).show()
                binding.etNotificationTitle.text?.clear()
                binding.etNotificationBody.text?.clear()
            } else {
                Toast.makeText(context, "Error al enviar", Toast.LENGTH_SHORT).show()
            }
            binding.btnSendNotification.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
