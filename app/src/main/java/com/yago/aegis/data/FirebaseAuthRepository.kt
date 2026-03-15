package com.yago.aegis.data

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class SimpleResult {
    object Success : SimpleResult()
    data class Error(val message: String) : SimpleResult()
}

class FirebaseAuthRepository {

    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = auth.currentUser != null
    val isEmailProvider: Boolean
        get() = auth.currentUser?.providerData?.any { it.providerId == "password" } == true

    suspend fun registerWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            AuthResult.Success(result.user!!)
        } catch (e: Exception) {
            AuthResult.Error(friendlyError(e.message))
        }
    }

    suspend fun loginWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success(result.user!!)
        } catch (e: Exception) {
            AuthResult.Error(friendlyError(e.message))
        }
    }

    suspend fun loginWithGoogle(account: GoogleSignInAccount): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            AuthResult.Success(result.user!!)
        } catch (e: Exception) {
            AuthResult.Error(friendlyError(e.message))
        }
    }

    // Cambia la contraseña (requiere reautenticación con la contraseña actual)
    suspend fun changePassword(currentPassword: String, newPassword: String): SimpleResult {
        return try {
            val user = auth.currentUser ?: return SimpleResult.Error("No hay sesión activa")
            val email = user.email ?: return SimpleResult.Error("No se encontró el email")
            // Reautenticamos antes de cambiar la contraseña
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            SimpleResult.Success
        } catch (e: Exception) {
            SimpleResult.Error(friendlyError(e.message))
        }
    }

    // Envía email de recuperación de contraseña
    suspend fun sendPasswordResetEmail(email: String): SimpleResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            SimpleResult.Success
        } catch (e: Exception) {
            SimpleResult.Error(friendlyError(e.message))
        }
    }

    fun logout() {
        auth.signOut()
    }

    private fun friendlyError(message: String?): String {
        return when {
            message == null -> "Error desconocido"
            message.contains("email address is already in use") -> "Este correo ya está registrado"
            message.contains("no user record") -> "No existe una cuenta con este correo"
            message.contains("password is invalid") || message.contains("wrong-password") -> "Contraseña incorrecta"
            message.contains("badly formatted") -> "El correo no tiene un formato válido"
            message.contains("weak-password") -> "La contraseña debe tener al menos 6 caracteres"
            message.contains("network") -> "Error de conexión. Comprueba tu internet"
            message.contains("requires-recent-login") -> "Por seguridad, vuelve a iniciar sesión antes de cambiar la contraseña"
            else -> "Error: $message"
        }
    }
}
