package com.tucugo.app.data.models

import java.util.Date

data class Report(
    val id: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val reportedId: String = "",
    val reportedName: String = "",
    val reason: String = "",
    val comments: String = "",
    val type: String = "", // 'driver', 'business', 'customer'
    val status: String = "open", // 'open', 'under_review', 'resolved', 'dismissed'
    val relatedDocPath: String? = null,
    val createdAt: Date? = null,
    val resolvedAt: Date? = null,
    val resolution: String? = null,
    val reporterEvidence: List<String> = emptyList(),
    val reportedEvidence: List<String> = emptyList()
)
