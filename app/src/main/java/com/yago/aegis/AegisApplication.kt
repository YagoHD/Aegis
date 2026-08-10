package com.yago.aegis

import android.app.Application
import com.yago.aegis.di.AppContainer
import com.yago.aegis.di.DefaultAppContainer

/**
 * Application de Aegis. Crea el [AppContainer] (US-05) una sola vez a nivel de app.
 * MainActivity lee las dependencias desde aquí en lugar de instanciarlas él mismo.
 */
class AegisApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
