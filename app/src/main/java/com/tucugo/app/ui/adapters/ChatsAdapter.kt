package com.tucugo.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.tucugo.app.R
import com.tucugo.app.data.models.Chat
import com.tucugo.app.databinding.ItemChatBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ChatsAdapter(
    private var chats: List<Chat>,
    private val currentUserId: String,
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position])
    }

    override fun getItemCount(): Int = chats.size

    fun updateChats(newChats: List<Chat>) {
        chats = newChats
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: Chat) {
            val otherParticipantId = chat.participantIds.find { it != currentUserId }
            val name = chat.participantNames[otherParticipantId] ?: "Usuario"
            val imageUrl = chat.participantImageUrls[otherParticipantId]

            binding.tvParticipantName.text = name
            binding.tvLastMessage.text = chat.lastMessage
            binding.tvLastMessageTime.text = timeFormatter.format(chat.lastMessageTimestamp)

            binding.ivParticipantImage.load(imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_foreground) // Asegúrate de tener un placeholder
                error(R.drawable.ic_launcher_foreground)
                transformations(CircleCropTransformation())
            }

            binding.root.setOnClickListener { onChatClick(chat) }
        }
    }
}
