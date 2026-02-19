package com.tucugo.app.data.repository

import com.tucugo.app.data.models.User
import com.tucugo.app.data.models.VerificationRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = db.collection("users")
    private val verificationRequestsCollection = db.collection("verification_requests")

    suspend fun saveUser(user: User) {
        usersCollection.document(user.id).set(user).await()
    }

    suspend fun getUser(userId: String): User? {
        val document = usersCollection.document(userId).get().await()
        return document.toObject(User::class.java)
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>) {
        usersCollection.document(userId).update(updates).await()
    }

    suspend fun submitVerificationRequest(request: VerificationRequest) {
        verificationRequestsCollection.add(request).await()
    }
}
