package com.yago.aegis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.yago.aegis.data.AuthResult
import com.yago.aegis.data.FirebaseAuthRepository
import com.yago.aegis.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(
    private val authRepository: FirebaseAuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: Boolean get() = authRepository.isLoggedIn
    val currentUserEmail: String? get() = authRepository.currentUser?.email

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.registerWithEmail(email, password)) {
                is AuthResult.Success -> {
                    // Primera vez: sube los datos locales a Firestore
                    userRepository.syncOnLogin()
                    _uiState.value = AuthUiState(isSuccess = true)
                }
                is AuthResult.Error -> _uiState.value = AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.loginWithEmail(email, password)) {
                is AuthResult.Success -> {
                    // Sincroniza: descarga nube si hay datos, o sube locales si es nuevo dispositivo
                    userRepository.syncOnLogin()
                    _uiState.value = AuthUiState(isSuccess = true)
                }
                is AuthResult.Error -> _uiState.value = AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.loginWithGoogle(account)) {
                is AuthResult.Success -> {
                    userRepository.syncOnLogin()
                    _uiState.value = AuthUiState(isSuccess = true)
                }
                is AuthResult.Error -> _uiState.value = AuthUiState(errorMessage = result.message)
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    class Factory(
        private val authRepository: FirebaseAuthRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(authRepository, userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
