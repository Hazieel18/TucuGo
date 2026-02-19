package com.tucugo.app.data.repository

import android.net.Uri
import com.tucugo.app.data.models.PaymentProof
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PaymentRepository(
    db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val paymentsCollection = db.collection("payments")
    private val storageRef = storage.reference.child("payment_proofs")

    /**
     * Sube la foto del comprobante y guarda el reporte de pago.
     */
    suspend fun submitPayment(proof: PaymentProof, imageUri: Uri): Result<Unit> {
        return try {
            // 1. Subir imagen
            val fileName = "${proof.userId}_${UUID.randomUUID()}.jpg"
            val uploadTask = storageRef.child(fileName).putFile(imageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

            // 2. Guardar datos en Firestore
            val finalProof = proof.copy(proofPhotoUrl = downloadUrl)
            paymentsCollection.add(finalProof).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
