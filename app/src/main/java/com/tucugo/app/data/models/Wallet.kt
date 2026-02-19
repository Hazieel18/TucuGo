package com.tucugo.app.data.models

data class Wallet(
    val id: String = "",
    val userId: String = "",
    val balance: Double = 0.0
)

data class WithdrawalRequest(
    val id: String = "",
    val walletId: String = "",
    val amount: Double = 0.0,
    val status: String = "pending", // 'pending', 'paid', 'rejected'
    val paymentDetails: String = ""
)
