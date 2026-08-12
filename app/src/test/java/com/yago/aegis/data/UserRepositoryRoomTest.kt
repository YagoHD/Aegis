package com.yago.aegis.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.yago.aegis.data.db.AegisDatabase
import com.yago.aegis.data.db.RoomMigrator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * US-03 Fase 3: verifica que UserRepository lee/escribe las colecciones complejas en ROOM
 * (a través de la compuerta de migración), incluida la escritura GRANULAR de upsertExercise.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UserRepositoryRoomTest {

    private lateinit var store: SettingsStore
    private lateinit var db: AegisDatabase
    private lateinit var repo: UserRepository

    @Before
    fun setup() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        store = SettingsStore(ctx)
        store.clearAll()
        db = Room.inMemoryDatabaseBuilder(ctx, AegisDatabase::class.java).allowMainThreadQueries().build()
        repo = UserRepository(store, db, RoomMigrator(store, db), FakeCloudDataSource())
    }

    @After
    fun teardown() = db.close()

    @Test
    fun updateExerciseLibrary_escribe_y_lee_de_room() = runBlocking {
        repo.updateExerciseLibrary(listOf(
            Exercise(name = "Press", type = "t", muscleGroup = "PECHO"),
            Exercise(name = "Remo", type = "t", muscleGroup = "ESPALDA")
        ))

        val fromRepo = repo.exerciseLibrary.first()
        assertEquals(2, fromRepo.size)
        assertEquals(setOf("Press", "Remo"), fromRepo.map { it.name }.toSet())
    }

    @Test
    fun upsertExercise_edita_una_fila_sin_duplicar() = runBlocking {
        repo.updateExerciseLibrary(listOf(Exercise(name = "Press", type = "t", muscleGroup = "PECHO")))
        val ex = repo.exerciseLibrary.first().single()

        repo.upsertExercise(ex.copy(oneRepMax = 120.0))   // editar 1 (granular)

        val back = repo.exerciseLibrary.first()
        assertEquals("no duplica", 1, back.size)
        assertEquals(120.0, back.single().oneRepMax, 0.0)
    }

    @Test
    fun saveWorkoutSession_aparece_en_el_historial_desde_room() = runBlocking {
        repo.saveWorkoutSession(WorkoutSession(routineName = "PUSH", exercisesProgress = emptyList()))

        assertEquals(1, repo.workoutHistory.first().size)
        assertEquals("PUSH", repo.workoutHistory.first().single().routineName)
    }
}
