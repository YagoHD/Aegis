package com.yago.aegis

import com.yago.aegis.data.DefaultExercises
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.ExerciseProgress
import com.yago.aegis.data.ExerciseSet
import com.yago.aegis.data.MuscleGroup
import com.yago.aegis.data.RankEngine
import com.yago.aegis.data.RankTier
import com.yago.aegis.data.WorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RankEngineTest {

    private val library = DefaultExercises.getAll()

    private fun bench(): Exercise =
        library.first { it.name.replace("​", "").trim() == "PRESS BANCA" }

    private fun sessionWith(ex: Exercise, weight: Double, reps: Int, daysAgo: Int): WorkoutSession {
        val date = System.currentTimeMillis() - daysAgo.toLong() * 24 * 60 * 60 * 1000
        return WorkoutSession(
            routineName = "TEST",
            date = date,
            exercisesProgress = listOf(
                ExerciseProgress(
                    exercise = ex,
                    sets = listOf(ExerciseSet(reps = reps, weight = weight, isCompleted = true))
                )
            )
        )
    }

    @Test
    fun emptyHistory_allSinRango() {
        val r = RankEngine.compute(emptyList(), library, bodyweight = 80.0, sex = "MALE")
        assertTrue(r.groups.all { it.tier == RankTier.SIN_RANGO })
        assertNull(r.strongest)
    }

    @Test
    fun heavyBench_manGivesDiamante() {
        // 120 kg x 5 -> Epley 1RM = 140 ; 140/80 = 1.75 -> Diamante
        val history = listOf(sessionWith(bench(), weight = 120.0, reps = 5, daysAgo = 2))
        val r = RankEngine.compute(history, library, bodyweight = 80.0, sex = "MALE")
        val pecho = r.groups.first { it.group == MuscleGroup.PECHO }
        assertEquals(RankTier.DIAMANTE, pecho.tier)
    }

    @Test
    fun lightHighVolume_doesNotInflate() {
        // 20 kg x 30 (mucho volumen) -> 1RM 40 ; 40/80 = 0.5 -> Bronce, no sube por volumen
        val history = listOf(sessionWith(bench(), weight = 20.0, reps = 30, daysAgo = 1))
        val r = RankEngine.compute(history, library, bodyweight = 80.0, sex = "MALE")
        val pecho = r.groups.first { it.group == MuscleGroup.PECHO }
        assertEquals(RankTier.BRONCE, pecho.tier)
    }

    @Test
    fun staleData_isIgnored() {
        // Mismo levantamiento fuerte pero hace 40 días -> fuera de la ventana de 28 días
        val history = listOf(sessionWith(bench(), weight = 120.0, reps = 5, daysAgo = 40))
        val r = RankEngine.compute(history, library, bodyweight = 80.0, sex = "MALE")
        val pecho = r.groups.first { it.group == MuscleGroup.PECHO }
        assertEquals(RankTier.SIN_RANGO, pecho.tier)
    }
}
