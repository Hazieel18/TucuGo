package com.tucugo.app.data.repository

import android.net.Uri
import com.tucugo.app.data.models.Chat
import com.tucugo.app.data.models.ChatMessage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class ChatRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val chatCollection = db.collection("chats")
    private val storageRef = storage.reference.child("chat_audio")

    fun getChats(userId: String): Flow<List<Chat>> = callbackFlow {
        val subscription = chatCollection
            .whereArrayContains("participantIds", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    trySend(it.toObjects(Chat::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun getOrCreateChat(participants: List<String>, participantNames: Map<String, String>, participantImageUrls: Map<String, String>): String {
        val existingChat = chatCollection
            .whereEqualTo("participantIds", participants.sorted())
            .get()
            .await()

        if (!existingChat.isEmpty) {
            return existingChat.documents[0].id
        }

        val newChatRef = chatCollection.document()
        val chat = Chat(
            id = newChatRef.id,
            participantIds = participants.sorted(),
            participantNames = participantNames,
            participantImageUrls = participantImageUrls
        )
        newChatRef.set(chat).await()
        return newChatRef.id
    }

    suspend fun sendMessage(message: ChatMessage): Result<Unit> {
        return try {
            val batch = db.batch()
            val chatRef = chatCollection.document(message.relatedId)
            val messageRef = chatRef.collection("messages").document()
            
            val finalMessage = message.copy(id = messageRef.id)
            
            batch.set(messageRef, finalMessage)
            
            val lastMsgText = if (message.audioUrl != null) "Nota de voz" else message.text
            batch.update(chatRef, mapOf(
                "lastMessage" to lastMsgText,
                "lastMessageTimestamp" to FieldValue.serverTimestamp()
            ))
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendVoiceNote(relatedId: String, senderId: String, audioUri: Uri, duration: Int): Result<Unit> {
        return try {
            val audioName = "${UUID.randomUUID()}.m4a"
            val uploadTask = storageRef.child(audioName).putFile(audioUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

            val message = ChatMessage(
                senderId = senderId,
                audioUrl = downloadUrl,
                duration = duration,
                relatedId = relatedId
            )
            sendMessage(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMessages(relatedId: String, currentUserId: String): Flow<List<ChatMessage>> = callbackFlow {
        val subscription = chatCollection.document(relatedId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val messages = it.toObjects(ChatMessage::class.java).map { msg ->
                        msg.apply { isSender = senderId == currentUserId }
                    }
                    trySend(messages)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun markMessagesAsRead(chatId: String, currentUserId: String) {
        try {
            val unreadMessages = chatCollection.document(chatId).collection("messages")
                .whereEqualTo("isRead", false)
                .get()
                .await()

            if (!unreadMessages.isEmpty) {
                val batch = db.batch()
                unreadMessages.documents.forEach { doc ->
                    if (doc.getString("senderId") != currentUserId) {
                        batch.update(doc.reference, "isRead", true)
                    }
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
