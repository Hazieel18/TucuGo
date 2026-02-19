package com.tucugo.app.data.repository

import com.tucugo.app.data.models.Wallet
import com.tucugo.app.data.models.WithdrawalRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class WalletRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val walletsCollection = db.collection("wallets")
    private val withdrawalsCollection = db.collection("withdrawals")

    /**
     * Obtiene la billetera de un usuario. Si no existe, la crea.
     */
    suspend fun getOrCreateWallet(userId: String): Wallet {
        val document = walletsCollection.document(userId).get().await()
        return if (document.exists()) {
            document.toObject(Wallet::class.java)!!
        } else {
            val newWallet = Wallet(id = userId, userId = userId, balance = 0.0)
            walletsCollection.document(userId).set(newWallet).await()
            newWallet
        }
    }

    /**
     * Crea una solicitud para retirar dinero.
     */
    suspend fun requestWithdrawal(userId: String, amount: Double, details: String): Result<Unit> {
        return try {
            val wallet = getOrCreateWallet(userId)
            if (wallet.balance < amount) throw Exception("Saldo insuficiente")

            val requestId = UUID.randomUUID().toString()
            val request = WithdrawalRequest(
                id = requestId,
                walletId = userId,
                amount = amount,
                status = "pending",
                paymentDetails = details
            )

            // Restamos el saldo preventivamente (bloqueo de fondos)
            db.runTransaction { transaction ->
                val walletRef = walletsCollection.document(userId)
                val currentBalance = transaction.get(walletRef).getDouble("balance") ?: 0.0
                transaction.update(walletRef, "balance", currentBalance - amount)
                transaction.set(withdrawalsCollection.document(requestId), request)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
