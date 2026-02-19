package com.tucugo.app.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tucugo.app.R
import com.tucugo.app.data.models.ChatMessage
import com.tucugo.app.data.repository.ChatRepository
import com.tucugo.app.databinding.FragmentChatBinding
import com.tucugo.app.ui.adapters.ChatAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    private val chatRepository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var chatAdapter: ChatAdapter

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var recordingStartTime: Long = 0
    private var initialTouchX = 0f
    private var isRecordingCancelled = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, getString(R.string.msg_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserId = auth.currentUser?.uid ?: ""
        val relatedId = arguments?.getString("relatedId") ?: ""

        if (relatedId.isEmpty()) {
            Toast.makeText(context, getString(R.string.msg_error_chat_id), Toast.LENGTH_SHORT).show()
            return
        }

        setupRecyclerView(currentUserId)
        observeMessages(relatedId, currentUserId)
        setupVoiceRecording(currentUserId, relatedId)
        markAsRead(relatedId, currentUserId)

        binding.btnSendMessage.setOnClickListener {
            sendMessage(currentUserId, relatedId)
        }
    }

    private fun setupVoiceRecording(senderId: String, relatedId: String) {
        binding.btnRecordVoice.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchX = event.rawX
                    isRecordingCancelled = false
                    v.isPressed = true
                    v.performClick()
                    if (checkPermission()) startRecording()
                    else requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
                MotionEvent.ACTION_MOVE -> {
                    val distance = initialTouchX - event.rawX
                    if (distance > 200) { // Umbral de 200px para cancelar
                        if (!isRecordingCancelled) {
                            isRecordingCancelled = true
                            binding.tvRecordingStatus.text = getString(R.string.label_recording_cancelled)
                            binding.tvRecordingStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                        }
                    } else if (!isRecordingCancelled) {
                        binding.tvRecordingStatus.text = getString(R.string.label_slide_to_cancel)
                        binding.tvRecordingStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false
                    stopRecording(senderId, relatedId, isRecordingCancelled)
                }
            }
            true
        }
    }

    private fun startRecording() {
        context?.let { ctx ->
            try {
                audioFile = File(ctx.cacheDir, "temp_audio.m4a")
                mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(ctx)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(audioFile?.absolutePath)
                    prepare()
                    start()
                }
                recordingStartTime = System.currentTimeMillis()
                binding.tvRecordingStatus.text = getString(R.string.label_recording)
                binding.tvRecordingStatus.setTextColor(ContextCompat.getColor(ctx, R.color.primary))
                binding.tvRecordingStatus.visibility = View.VISIBLE
            } catch (e: Exception) {
                Log.e("ChatFragment", "startRecording failed", e)
                Toast.makeText(ctx, getString(R.string.msg_error_capture_audio), Toast.LENGTH_SHORT).show()
                mediaRecorder?.release()
                mediaRecorder = null
                audioFile?.delete()
                binding.tvRecordingStatus.visibility = View.GONE
            }
        }
    }

    private fun stopRecording(senderId: String, relatedId: String, isCancelled: Boolean) {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            binding.tvRecordingStatus.visibility = View.GONE

            if (isCancelled) {
                audioFile?.delete()
                return
            }

            val duration = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
            if (duration < 1) {
                audioFile?.delete()
                return
            }

            audioFile?.let { file ->
                viewLifecycleOwner.lifecycleScope.launch {
                    chatRepository.sendVoiceNote(relatedId, senderId, Uri.fromFile(file), duration)
                }
            }
        } catch (e: Exception) {
            Log.e("ChatFragment", "stopRecording failed", e)
            mediaRecorder = null
            binding.tvRecordingStatus.visibility = View.GONE
        }
    }

    private fun checkPermission(): Boolean {
        return context?.let {
            ContextCompat.checkSelfPermission(it, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        } ?: false
    }

    private fun setupRecyclerView(currentUserId: String) {
        chatAdapter = ChatAdapter(emptyList(), currentUserId)
        binding.rvChatMessages.apply {
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            adapter = chatAdapter
        }
    }

    private fun observeMessages(relatedId: String, currentUserId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            chatRepository.getMessages(relatedId, currentUserId).collectLatest { messages ->
                chatAdapter.updateMessages(messages)
                if (messages.isNotEmpty()) {
                    binding.rvChatMessages.smoothScrollToPosition(messages.size - 1)
                    markAsRead(relatedId, currentUserId)
                }
            }
        }
    }

    private fun markAsRead(chatId: String, userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            chatRepository.markMessagesAsRead(chatId, userId)
        }
    }

    private fun sendMessage(senderId: String, relatedId: String) {
        val text = binding.etChatMessage.text.toString().trim()
        if (text.isEmpty()) return
        viewLifecycleOwner.lifecycleScope.launch {
            chatRepository.sendMessage(ChatMessage(senderId = senderId, text = text, relatedId = relatedId))
            binding.etChatMessage.text.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chatAdapter.releasePlayer()
        _binding = null
    }
}
