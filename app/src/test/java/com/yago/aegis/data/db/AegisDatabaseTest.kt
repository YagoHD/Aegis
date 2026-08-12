package com.yago.aegis.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.ExerciseProgress
import com.yago.aegis.data.ExerciseRecord
import com.yago.aegis.data.ExerciseSet
import com.yago.aegis.data.MuscleContribution
import com.yago.aegis.data.WorkoutSession
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
 * US-03 Fase 1: valida que la capa Room (entidades + TypeConverters + mappers) hace round-trip
 * de los datos AGREGADOS con sus listas anidadas, sin pérdida. BD en memoria (Robolectric).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AegisDatabaseTest {

    private lateinit var db: AegisDatabase

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AegisDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun exercise_round_trip_conserva_campos_y_listas_anidadas() = runBlocking {
        val ex = Exercise(
            name = "Press Banca", type = "COMPOUND", muscleGroup = "PECHO",
            tags = listOf("PUSH", "PECHO"),
            history = listOf(ExerciseRecord(date = 1L, weight = 100.0, reps = 5, oneRepMax = 115.0)),
            muscleContributions = listOf(MuscleContribution("PECHO_MEDIO", 65))
        )

        db.exerciseDao().upsert(ex.toEntity())
        val back = db.exerciseDao().getAll().single().toDomain()

        assertEquals("Press Banca", back.name)
        assertEquals(ex.id, back.id)
        assertEquals(listOf("PUSH", "PECHO"), back.tags)
        assertEquals(1, back.history.size)
        assertEquals(115.0, back.history[0].oneRepMax, 0.0)
        assertEquals("PECHO_MEDIO", back.muscleContributions?.single()?.muscle)
    }

    @Test
    fun workoutSession_round_trip_conserva_progreso_anidado() = runBlocking {
        val session = WorkoutSession(
            routineName = "PUSH DAY",
            exercisesProgress = listOf(
                ExerciseProgress(
                    exercise = Exercise(name = "Fondos", type = "BW", muscleGroup = "PECHO"),
                    sets = listOf(ExerciseSet(reps = 10, weight = 80.0, isCompleted = true))
                )
            )
        )

        db.workoutSessionDao().upsert(session.toEntity())
        val back = db.workoutSessionDao().getAll().single().toDomain()

        assertEquals("PUSH DAY", back.routineName)
        assertEquals("Fondos", back.exercisesProgress.single().exercise.name)
        assertEquals(10, back.exercisesProgress.single().sets.single().reps)
        assertTrue(back.exercisesProgress.single().sets.single().isCompleted)
    }

    @Test
    fun replaceAll_sustituye_el_conjunto() = runBlocking {
        db.exerciseDao().upsertAll(listOf(
            Exercise(name = "A", type = "x", muscleGroup = "y").toEntity(),
            Exercise(name = "B", type = "x", muscleGroup = "y").toEntity()
        ))
        db.exerciseDao().replaceAll(listOf(Exercise(name = "C", type = "x", muscleGroup = "y").toEntity()))

        val all = db.exerciseDao().getAll()
        assertEquals(1, all.size)
        assertEquals("C", all.single().name)
    }
}
