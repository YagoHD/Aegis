package com.yago.aegis.data

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class FirebaseAuthRepository {

    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = auth.currentUser != null

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
            else -> "Error: $message"
        }
    }
}
