package com.yago.aegis.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.SettingsStore
import com.yago.aegis.data.WorkoutSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * US-03 Fase 2: valida el volcado one-shot DataStore -> Room, la deduplicación por PK
 * (el requisito del QA) y la idempotencia. SettingsStore real (DataStore) + Room en memoria.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomMigratorTest {

    private lateinit var store: SettingsStore
    private lateinit var db: AegisDatabase
    private lateinit var migrator: RoomMigrator

    @Before
    fun setup() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        store = SettingsStore(ctx)
        store.clearAll()
        db = Room.inMemoryDatabaseBuilder(ctx, AegisDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        migrator = RoomMigrator(store, db)
    }

    @After
    fun teardown() = db.close()

    @Test
    fun vuelca_datastore_a_room_y_deduplica_ids_de_sesion() = runBlocking {
        // Historial con id DUPLICADO (bug histórico) -> debe colapsar a 1 sin perder por sorpresa.
        val dupId = "same-id"
        store.replaceWorkoutHistory(listOf(
            WorkoutSession(id = dupId, routineName = "A", date = 1L, exercisesProgress = emptyList()),
            WorkoutSession(id = dupId, routineName = "B", date = 2L, exercisesProgress = emptyList())
        ))
        store.saveExerciseLibrary(listOf(
            Exercise(name = "Press", type = "t", muscleGroup = "PECHO"),
            Exercise(name = "Remo", type = "t", muscleGroup = "ESPALDA")
        ))

        migrator.migrateIfNeeded()

        assertEquals("historial deduplicado por id", 1, db.workoutSessionDao().getAll().size)
        assertEquals(2, db.exerciseDao().getAll().size)
        assertTrue("flag de migrado puesto", store.roomMigrated.first())
    }

    @Test
    fun es_idempotente_no_re_vuelca_si_ya_migro() = runBlocking {
        store.saveExerciseLibrary(listOf(Exercise(name = "X", type = "t", muscleGroup = "m")))
        migrator.migrateIfNeeded()

        // Cambian los datos locales, pero como ya migró, la 2ª llamada NO debe re-volcar.
        store.saveExerciseLibrary(listOf(
            Exercise(name = "X", type = "t", muscleGroup = "m"),
            Exercise(name = "Y", type = "t", muscleGroup = "m")
        ))
        migrator.migrateIfNeeded()

        assertEquals("no re-migró (sigue con 1)", 1, db.exerciseDao().getAll().size)
    }
}
