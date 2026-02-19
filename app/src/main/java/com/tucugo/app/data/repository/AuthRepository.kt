package com.tucugo.app.data.repository

import com.tucugo.app.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository = UserRepository()
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun registerUser(email: String, password: String, userDetails: User): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Error al crear usuario")
            
            val finalUser = userDetails.copy(id = firebaseUser.uid, email = email)
            userRepository.saveUser(finalUser)
            
            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Usuario no encontrado")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun updateProfilePhotos(userId: String, photoUrl: String?, photoCedulaUrl: String?): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>()
            photoUrl?.let { updates["photoUrl"] = it }
            photoCedulaUrl?.let { updates["photoCedulaUrl"] = it }
            
            if (updates.isNotEmpty()) {
                userRepository.updateUser(userId, updates)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
