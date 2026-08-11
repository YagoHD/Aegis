package com.yago.aegis.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Smoke test: valida que Robolectric arranca (compileSdk 36 -> se emula SDK 34) y que
 * DataStore/SettingsStore funcionan en JVM con runBlocking (sin el deadlock de runTest).
 * Si esto pasa, el resto de tests de repositorio con SettingsStore real son viables.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RobolectricSmokeTest {

    @Test
    fun settingsStore_persiste_nombre() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val store = SettingsStore(ctx)
        store.saveName("Leónidas")
        assertEquals("Leónidas", store.userName.first())
    }
}
