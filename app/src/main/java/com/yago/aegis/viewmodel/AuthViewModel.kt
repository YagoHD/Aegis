package com.yago.aegis.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.yago.aegis.R
import com.yago.aegis.data.AuthResult
import com.yago.aegis.data.FirebaseAuthRepository
import com.yago.aegis.data.SimpleResult
import com.yago.aegis.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val needsEmailVerification: Boolean = false,
)

class AuthViewModel(
    application: Application,
    private val authRepository: FirebaseAuthRepository,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Resuelve un ID de recurso al idioma activo del dispositivo.
    private fun str(resId: Int): String = getApplication<Application>().getString(resId)

    // StateFlow reactivo — Navigation lo observa para recalcular startDest
    private val _isLoggedIn = MutableStateFlow(authRepository.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    val currentUserEmail: String? get() = authRepository.currentUser?.email
    val isEmailProvider: Boolean get() = authRepository.isEmailProvider

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.registerWithEmail(email, password)) {
                is AuthResult.Success -> {
                    authRepository.sendVerificationEmail()
                    userRepository.syncOnLogin()
                    _isLoggedIn.value = true
                    _uiState.value = AuthUiState(needsEmailVerification = true)
                }
                is AuthResult.Error -> _uiState.value = AuthUiState(errorMessage = str(result.messageRes))
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.loginWithEmail(email, password)) {
                is AuthResult.Success -> {
                    if (!authRepository.isEmailVerified) {
                        authRepository.sendVerificationEmail()
                        _isLoggedIn.value = true
                        _uiState.value = AuthUiState(needsEmailVerification = true)
                    } else {
                        userRepository.syncOnLogin()
                        _isLoggedIn.value = true
                        _uiState.value = AuthUiState(isSuccess = true)
                    }
                }
                is AuthResult.Error -> _uiState.value = AuthUiState(errorMessage = str(result.messageRes))
            }
        }
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.loginWithGoogle(account)) {
                is AuthResult.Success -> {
                    userRepository.syncOnLogin()
                    _isLoggedIn.value = true
                    _uiState.value = AuthUiState(isSuccess = true)
                }
                is AuthResult.Error -> _uiState.value = AuthUiState(errorMessage = str(result.messageRes))
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.changePassword(currentPassword, newPassword)) {
                is SimpleResult.Success -> _uiState.value = AuthUiState(successMessage = str(R.string.auth_msg_password_updated))
                is SimpleResult.Error -> _uiState.value = AuthUiState(errorMessage = str(result.messageRes))
            }
        }
    }

    /**
     * Borra la cuenta y todos los datos (nube + local) de forma permanente.
     * El borrado en Firestore ocurre tras reautenticar y antes de eliminar la cuenta.
     */
    fun deleteAccount(currentPassword: String?, onDeleted: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.deleteAccount(currentPassword) {
                userRepository.deleteCloudData()
            }
            when (result) {
                is SimpleResult.Success -> {
                    userRepository.clearLocalData()
                    _isLoggedIn.value = false
                    _uiState.value = AuthUiState()
                    onDeleted()
                }
                is SimpleResult.Error -> _uiState.value = AuthUiState(errorMessage = str(result.messageRes))
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is SimpleResult.Success -> _uiState.value = AuthUiState(successMessage = str(R.string.auth_msg_reset_sent))
                is SimpleResult.Error -> _uiState.value = AuthUiState(errorMessage = str(result.messageRes))
            }
        }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            authRepository.sendVerificationEmail()
        }
    }

    fun checkEmailVerified(onVerified: () -> Unit) {
        viewModelScope.launch {
            authRepository.reloadUser()
            if (authRepository.isEmailVerified) {
                userRepository.syncOnLogin()
                _uiState.value = AuthUiState(isSuccess = true)
                onVerified()
            } else {
                _uiState.value = AuthUiState(
                    needsEmailVerification = true,
                    errorMessage = str(R.string.auth_msg_verification_not_detected)
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.clearLocalData()
            authRepository.logout()
            _isLoggedIn.value = false
            _uiState.value = AuthUiState()
        }
    }

    fun clearState() {
        _uiState.value = AuthUiState()
    }

    class Factory(
        private val application: Application,
        private val authRepository: FirebaseAuthRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(application, authRepository, userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
