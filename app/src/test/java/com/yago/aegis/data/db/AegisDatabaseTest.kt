package com.yago.aegis.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.ExerciseProgress
import com.yago.aegis.data.ExerciseRecord
import com.yago.aegis.data.ExerciseSet
import com.yago.aegis.data.ExerciseSlot
import com.yago.aegis.data.MuscleContribution
import com.yago.aegis.data.Routine
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
    fun routine_round_trip_reconstruye_lista_legacy_y_conserva_slots() = runBlocking {
        val slots = listOf(
            ExerciseSlot(variants = listOf(
                Exercise(name = "Sentadilla", type = "x", muscleGroup = "PIERNA"),
                Exercise(name = "Prensa", type = "x", muscleGroup = "PIERNA")   // variante alternativa
            )),
            ExerciseSlot(variants = listOf(Exercise(name = "Peso Muerto", type = "x", muscleGroup = "ESPALDA")))
        )
        val routine = Routine(id = 7, name = "LEG DAY", exerciseSlots = slots,
            exercises = slots.map { it.variants.first() })

        db.routineDao().upsert(routine.toEntity())
        val back = db.routineDao().getAll().single().toDomain()

        assertEquals("LEG DAY", back.name)
        assertEquals(2, back.exerciseSlots.size)
        assertEquals(2, back.exerciseSlots[0].variants.size)   // se conservan las variantes
        // La lista legacy se reconstruye = 1ª variante por slot (lo que leen las tarjetas)
        assertEquals(listOf("Sentadilla", "Peso Muerto"), back.exercises.map { it.name })
    }

    @Test
    fun routine_legacy_sin_slots_se_migra_a_slots_al_guardar() = runBlocking {
        val legacy = Routine(
            id = 3, name = "OLD",
            exercises = listOf(Exercise(name = "Curl", type = "x", muscleGroup = "BRAZO")),
            exerciseSlots = emptyList()
        )

        db.routineDao().upsert(legacy.toEntity())
        val back = db.routineDao().getAll().single().toDomain()

        assertEquals(1, back.exerciseSlots.size)   // exercises legacy -> 1 slot
        assertEquals("Curl", back.exerciseSlots.single().variants.single().name)
        assertEquals("Curl", back.exercises.single().name)   // legacy reconstruida
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

    @Test
    fun toEntity_tolera_nulls_de_gson_en_datos_antiguos() = runBlocking {
        // Gson inyecta null en `notes` (sesión antigua sin ese campo) pese a ser no-null en Kotlin.
        // Era el crash real en dispositivo durante la migración.
        val old = com.google.gson.Gson().fromJson(
            """{"id":"x","routineName":"A","date":1,"exercisesProgress":[]}""",
            WorkoutSession::class.java
        )

        db.workoutSessionDao().upsert(old.toEntity())   // no debe lanzar NPE
        val back = db.workoutSessionDao().getAll().single().toDomain()

        assertEquals("x", back.id)
        assertEquals("", back.notes)
    }
}
