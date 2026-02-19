package com.tucugo.app.ui.adapters

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.tucugo.app.R
import com.tucugo.app.data.models.ChatMessage
import com.tucugo.app.databinding.ItemChatMessageBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter(
    private var messages: List<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private var mediaPlayer: MediaPlayer? = null
    private var currentlyPlayingId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    inner class ChatViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.tvMessageTime.text = timeFormatter.format(message.timestamp)

            // Setup alignment
            val params = binding.cardMessage.layoutParams as LinearLayout.LayoutParams
            if (message.senderId == currentUserId) {
                binding.layoutMessageContainer.gravity = Gravity.END
                binding.cardMessage.setCardBackgroundColor(binding.root.context.getColor(R.color.secondary))
                params.setMargins(100, 0, 0, 0)
            } else {
                binding.layoutMessageContainer.gravity = Gravity.START
                binding.cardMessage.setCardBackgroundColor(binding.root.context.getColor(R.color.white))
                params.setMargins(0, 0, 100, 0)
            }
            binding.cardMessage.layoutParams = params

            // Toggle between Text and Voice Note
            if (message.audioUrl != null) {
                binding.tvMessageText.visibility = View.GONE
                binding.layoutVoiceNote.visibility = View.VISIBLE
                setupVoiceNote(message)
            } else {
                binding.tvMessageText.visibility = View.VISIBLE
                binding.layoutVoiceNote.visibility = View.GONE
                binding.tvMessageText.text = message.text
            }
        }

        private fun setupVoiceNote(message: ChatMessage) {
            binding.tvAudioDuration.text = formatDuration(message.duration ?: 0)
            
            binding.btnPlayPause.setOnClickListener {
                if (currentlyPlayingId == message.id && mediaPlayer?.isPlaying == true) {
                    pauseAudio()
                } else {
                    playAudio(message)
                }
            }
        }

        private fun playAudio(message: ChatMessage) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(message.audioUrl)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                    currentlyPlayingId = message.id
                }
                setOnCompletionListener {
                    binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
                    currentlyPlayingId = null
                }
            }
        }

        private fun pauseAudio() {
            mediaPlayer?.pause()
            binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
        }

        private fun formatDuration(seconds: Int): String {
            val mins = seconds / 60
            val secs = seconds % 60
            return String.format("%d:%02d", mins, secs)
        }
    }
}
