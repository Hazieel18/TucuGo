package com.tucugo.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tucugo.app.data.models.User
import com.tucugo.app.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel para la gestión de autenticación y registro de usuarios.
 * Mantiene val en authRepository ya que es requerido por métodos asíncronos.
 */
class AuthViewModel(private val authRepository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _authState = MutableLiveData<AuthResult>(AuthResult.Idle)
    val authState: LiveData<AuthResult> = _authState

    private var tempUser = User()
    private var tempPassword = ""

    fun setBasicInfo(email: String, pass: String, phone: String, dob: Date) {
        tempUser = tempUser.copy(email = email, phoneNumber = phone, dateOfBirth = dob)
        tempPassword = pass
    }

    fun setIdentification(cedula: String) {
        tempUser = tempUser.copy(cedula = cedula)
    }

    fun completeRegistration(photoUrl: String?, photoCedulaUrl: String?) {
        _authState.value = AuthResult.Loading
        viewModelScope.launch {
            val result = authRepository.registerUser(tempUser.email, tempPassword, tempUser)
            result.onSuccess { firebaseUser ->
                if (photoUrl != null || photoCedulaUrl != null) {
                    authRepository.updateProfilePhotos(firebaseUser.uid, photoUrl, photoCedulaUrl)
                }
                _authState.value = AuthResult.Success(firebaseUser)
            }.onFailure {
                _authState.value = AuthResult.Error(it.message ?: "Error en el registro")
            }
        }
    }

    fun login(email: String, pass: String) {
        _authState.value = AuthResult.Loading
        viewModelScope.launch {
            authRepository.login(email, pass)
                .onSuccess { _authState.value = AuthResult.Success(it) }
                .onFailure { _authState.value = AuthResult.Error(it.message ?: "Credenciales inválidas") }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = AuthResult.Idle
    }

    sealed class AuthResult {
        object Idle : AuthResult()
        object Loading : AuthResult()
        data class Success(val user: FirebaseUser) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }
}
