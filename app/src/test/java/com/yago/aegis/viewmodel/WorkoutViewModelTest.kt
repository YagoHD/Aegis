package com.yago.aegis.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.yago.aegis.MainDispatcherRule
import com.yago.aegis.data.FakeCloudDataSource
import com.yago.aegis.data.SettingsStore
import com.yago.aegis.data.UserRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * US-08: tests de [WorkoutViewModel] sobre estado síncrono de la sesión activa.
 * Robolectric aporta el Application (AndroidViewModel); [MainDispatcherRule] el dispatcher Main.
 * Se prueban métodos que actualizan StateFlow de forma síncrona (sin depender de temporización).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WorkoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var vm: WorkoutViewModel

    @Before
    fun setup() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val store = SettingsStore(app)
        store.clearAll()
        val repo = UserRepository(store, FakeCloudDataSource())
        vm = WorkoutViewModel(app, repo)
    }

    @Test
    fun startCustomWorkout_crea_sesion_vacia_con_nombre_en_mayusculas() {
        vm.startCustomWorkout("piernas")

        val s = vm.activeSession.value
        assertNotNull(s)
        assertEquals("PIERNAS", s!!.routineName)
        assertTrue("la sesión libre empieza sin ejercicios", s.exercisesProgress.isEmpty())
    }

    @Test
    fun pausar_y_reanudar_alterna_isPaused() {
        vm.startCustomWorkout("x")

        vm.pauseWorkout()
        assertTrue(vm.isPaused.value)

        vm.resumeWorkout()
        assertFalse(vm.isPaused.value)
    }

    @Test
    fun updateSessionNotes_actualiza_las_notas_de_la_sesion() {
        vm.startCustomWorkout("x")

        vm.updateSessionNotes("buena sesión")

        assertEquals("buena sesión", vm.activeSession.value?.notes)
    }
}
