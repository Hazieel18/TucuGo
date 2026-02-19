package com.tucugo.app.data.models

import com.google.firebase.firestore.Exclude
import java.util.Date

data class Chat(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    var lastMessage: String = "",
    var lastMessageTimestamp: Date = Date(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantImageUrls: Map<String, String> = emptyMap()
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val audioUrl: String? = null,
    val duration: Int? = null, // en segundos
    val timestamp: Date = Date(),
    val relatedId: String = "",
    var isRead: Boolean = false
) {
    @get:Exclude
    var isSender: Boolean = false
}

data class SystemSettings(
    val rate: Double = 36.5,
    val pagoMovilPhone: String = "",
    val pagoMovilId: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val accountHolder: String = "",
    val binanceId: String = "",
    val zinliEmail: String = ""
)
