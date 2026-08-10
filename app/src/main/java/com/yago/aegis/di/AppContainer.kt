package com.yago.aegis.di

import android.content.Context
import com.yago.aegis.data.FirebaseAuthRepository
import com.yago.aegis.data.SettingsStore
import com.yago.aegis.data.UserRepository

/**
 * Contenedor de dependencias EXPLÍCITO (US-05). Centraliza la creación de las dependencias de
 * datos en un único punto (antes estaban sueltas en MainActivity). Es una interfaz para poder
 * sustituirlo por un fake en pruebas. Los ViewModels siguen creándose Activity-scoped en
 * MainActivity (para no tocar la navegación ni el estado compartido de la sesión activa).
 */
interface AppContainer {
    val settingsStore: SettingsStore
    val userRepository: UserRepository
    val authRepository: FirebaseAuthRepository
}

/** Implementación real, respaldada por DataStore/Firebase. */
class DefaultAppContainer(context: Context) : AppContainer {
    private val appContext = context.applicationContext
    override val settingsStore: SettingsStore by lazy { SettingsStore(appContext) }
    override val userRepository: UserRepository by lazy { UserRepository(settingsStore) }
    override val authRepository: FirebaseAuthRepository by lazy { FirebaseAuthRepository() }
}
