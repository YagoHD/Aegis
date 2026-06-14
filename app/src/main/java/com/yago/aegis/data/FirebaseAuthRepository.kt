package com.yago.aegis.data

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.yago.aegis.R
import kotlinx.coroutines.tasks.await

// Los errores cargan un ID de recurso (Int), no un String. El ViewModel lo
// resuelve al idioma del dispositivo. Así nada de auth queda hardcodeado.
sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val messageRes: Int) : AuthResult()
}

sealed class SimpleResult {
    object Success : SimpleResult()
    data class Error(val messageRes: Int) : SimpleResult()
}

class FirebaseAuthRepository {

    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = auth.currentUser != null
    val isEmailProvider: Boolean
        get() = auth.currentUser?.providerData?.any { it.providerId == "password" } == true

    val isEmailVerified: Boolean
        get() = auth.currentUser?.isEmailVerified == true

    suspend fun reloadUser(): SimpleResult {
        return try {
            auth.currentUser?.reload()?.await()
            SimpleResult.Success
        } catch (e: Exception) {
            SimpleResult.Error(friendlyError(e.message))
        }
    }

    suspend fun sendVerificationEmail(): SimpleResult {
        return try {
            val user = auth.currentUser ?: return SimpleResult.Error(R.string.auth_error_no_session)
            user.sendEmailVerification().await()
            SimpleResult.Success
        } catch (e: Exception) {
            SimpleResult.Error(friendlyError(e.message))
        }
    }

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
            val user = auth.currentUser ?: return SimpleResult.Error(R.string.auth_error_no_session)
            val email = user.email ?: return SimpleResult.Error(R.string.auth_error_no_email)
            // Reautenticamos antes de cambiar la contraseña
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            SimpleResult.Success
        } catch (e: Exception) {
            SimpleResult.Error(friendlyError(e.message))
        }
    }

    /**
     * Borra la cuenta del usuario de forma permanente.
     * Para usuarios de email reautentica con la contraseña antes de borrar.
     * [onReauthenticated] se ejecuta tras reautenticar y ANTES de borrar la cuenta,
     * para poder limpiar Firestore mientras la sesión sigue siendo válida.
     */
    suspend fun deleteAccount(
        currentPassword: String?,
        onReauthenticated: suspend () -> Unit
    ): SimpleResult {
        return try {
            val user = auth.currentUser ?: return SimpleResult.Error(R.string.auth_error_no_session)
            if (isEmailProvider && currentPassword != null) {
                val email = user.email ?: return SimpleResult.Error(R.string.auth_error_no_email)
                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                user.reauthenticate(credential).await()
            }
            // Borra los datos en la nube mientras seguimos autenticados
            onReauthenticated()
            user.delete().await()
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

    // Devuelve un ID de recurso de string; el ViewModel lo resuelve al idioma activo.
    private fun friendlyError(message: String?): Int {
        return when {
            message == null -> R.string.auth_error_unknown
            message.contains("email address is already in use") -> R.string.auth_error_email_in_use
            message.contains("no user record") -> R.string.auth_error_no_account
            message.contains("password is invalid") || message.contains("wrong-password") -> R.string.auth_error_wrong_password
            message.contains("badly formatted") -> R.string.auth_error_invalid_email_format
            message.contains("weak-password") -> R.string.auth_error_weak_password
            message.contains("network") -> R.string.auth_error_network
            message.contains("requires-recent-login") -> R.string.auth_error_recent_login
            else -> R.string.auth_error_unknown
        }
    }
}
