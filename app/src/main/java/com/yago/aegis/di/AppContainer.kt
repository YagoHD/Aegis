package com.yago.aegis.di

import android.content.Context
import com.yago.aegis.data.FirebaseAuthRepository
import com.yago.aegis.data.SettingsStore
import com.yago.aegis.data.UserRepository
import com.yago.aegis.data.db.AegisDatabase
import com.yago.aegis.data.db.RoomMigrator
import com.yago.aegis.data.social.SocialDataSource

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
    val database: AegisDatabase
    val roomMigrator: RoomMigrator
    val socialDataSource: SocialDataSource
}

/** Implementación real, respaldada por DataStore/Room/Firebase. */
class DefaultAppContainer(context: Context) : AppContainer {
    private val appContext = context.applicationContext
    override val settingsStore: SettingsStore by lazy { SettingsStore(appContext) }
    override val database: AegisDatabase by lazy { AegisDatabase.get(appContext) }
    override val roomMigrator: RoomMigrator by lazy { RoomMigrator(settingsStore, database) }
    // US-03 fase 3: UserRepository lee/escribe las 5 colecciones complejas en Room (misma
    // instancia de migrator que dispara el arranque -> el Mutex se comparte, migra 1 sola vez).
    override val userRepository: UserRepository by lazy { UserRepository(settingsStore, database, roomMigrator) }
    override val authRepository: FirebaseAuthRepository by lazy { FirebaseAuthRepository() }
    override val socialDataSource: SocialDataSource by lazy { SocialDataSource() }
}
