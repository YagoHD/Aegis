package com.yago.aegis.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * US-08: tests de la orquestación de sincronización de [UserRepository.syncOnLogin] usando
 * el [SettingsStore] REAL (DataStore, vía Robolectric) y un [FakeCloudDataSource] (sin Firebase).
 *
 * Regla clave verificada: la escritura local es la fuente de verdad; un fallo de nube nunca
 * la corrompe (queda intacta y el estado pasa a Error para poder reintentar).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UserRepositorySyncTest {

    private lateinit var store: SettingsStore
    private lateinit var fake: FakeCloudDataSource
    private lateinit var repo: UserRepository

    @Before
    fun setup() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        store = SettingsStore(ctx)
        store.clearAll()                 // estado determinista entre tests (DataStore persiste en la sandbox)
        fake = FakeCloudDataSource()
        repo = UserRepository(store, fake)
    }

    @Test
    fun nube_vacia_sube_local_y_marca_success() = runBlocking {
        store.saveName("Local")
        // fake.profile == null -> hasCloudData() == false -> camino de SUBIDA

        repo.syncOnLogin()

        assertTrue("debió subir el perfil local", fake.saveProfileCalls >= 1)
        assertEquals("Local", fake.profile?.get("name"))
        assertEquals(SyncState.Success, repo.syncState.value)
    }

    @Test
    fun nube_con_datos_descarga_a_local_y_marca_success() = runBlocking {
        fake.profile = mapOf("name" to "Nube")   // hasCloudData() == true -> camino de DESCARGA

        repo.syncOnLogin()

        assertEquals("el nombre local debe venir de la nube", "Nube", store.userName.first())
        assertEquals(SyncState.Success, repo.syncState.value)
    }

    @Test
    fun fallo_de_lectura_marca_error_y_deja_local_intacto() = runBlocking {
        store.saveName("Local")
        fake.failReads = true            // hasCloudData()/getters lanzan

        repo.syncOnLogin()

        assertTrue("estado debe ser Error", repo.syncState.value is SyncState.Error)
        assertEquals("los datos locales NO deben tocarse", "Local", store.userName.first())
    }

    @Test
    fun datos_servidos_desde_cache_marcan_cached_no_success() = runBlocking {
        fake.profile = mapOf("name" to "Nube")
        fake.fromCache = true            // leído de caché, sin confirmación del servidor

        repo.syncOnLogin()

        assertEquals(SyncState.Cached, repo.syncState.value)
    }

    @Test
    fun retry_tras_error_recupera_success() = runBlocking {
        fake.failReads = true
        repo.syncOnLogin()
        assertTrue(repo.syncState.value is SyncState.Error)

        // vuelve la red; la nube está vacía -> sube y confirma
        fake.failReads = false
        repo.retrySync()

        assertEquals(SyncState.Success, repo.syncState.value)
    }

    @Test
    fun deleteCloudData_propaga_el_fallo_para_no_borrar_auth_con_datos_vivos() = runBlocking {
        fake.failDelete = true

        var propago = false
        try {
            repo.deleteCloudData()
        } catch (_: Exception) {
            propago = true
        }

        assertTrue("deleteCloudData debe propagar el fallo (si no, se borraría Auth con datos vivos)", propago)
    }
}
