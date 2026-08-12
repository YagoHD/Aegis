package com.yago.aegis

import android.app.Application
import com.yago.aegis.di.AppContainer
import com.yago.aegis.di.DefaultAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application de Aegis. Crea el [AppContainer] (US-05) una sola vez a nivel de app.
 * MainActivity lee las dependencias desde aquí en lugar de instanciarlas él mismo.
 */
class AegisApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        // US-03 fase 2: vuelca DataStore -> Room una sola vez, en 2º plano. Room aún NO es la
        // fuente de verdad (fase 3); esto solo rellena la BD. Si falla, se reintenta al próximo
        // arranque y los datos locales quedan intactos.
        appScope.launch {
            runCatching { container.roomMigrator.migrateIfNeeded() }
        }
    }
}
