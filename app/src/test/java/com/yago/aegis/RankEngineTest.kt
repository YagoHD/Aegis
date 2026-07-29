package com.yago.aegis

import com.yago.aegis.data.DefaultExercises
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.ExerciseProgress
import com.yago.aegis.data.ExerciseSet
import com.yago.aegis.data.Fatigue
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
        // Hace 90 días -> más allá de la ventana de decaimiento (84 d) -> no cuenta
        val history = listOf(sessionWith(bench(), weight = 120.0, reps = 5, daysAgo = 90))
        val r = RankEngine.compute(history, library, bodyweight = 80.0, sex = "MALE")
        val pecho = r.groups.first { it.group == MuscleGroup.PECHO }
        assertEquals(RankTier.SIN_RANGO, pecho.tier)
    }

    @Test
    fun decay_countsBeyond28DaysButFades() {
        // A 60 días la marca decae (×0.65) pero SIGUE contando -> ya no hay corte duro a los 28 d
        val history = listOf(sessionWith(bench(), weight = 120.0, reps = 5, daysAgo = 60))
        val r = RankEngine.compute(history, library, bodyweight = 80.0, sex = "MALE")
        val pecho = r.groups.first { it.group == MuscleGroup.PECHO }
        assertTrue(pecho.tier != RankTier.SIN_RANGO)
    }

    @Test
    fun recencyCurve_matchesSpec() {
        assertEquals(1.0, RankEngine.recencyFactor(0), 0.0001)
        assertEquals(1.0, RankEngine.recencyFactor(28), 0.0001)
        assertEquals(0.95, RankEngine.recencyFactor(35), 0.0001)
        assertEquals(0.90, RankEngine.recencyFactor(42), 0.0001)
        assertEquals(0.80, RankEngine.recencyFactor(56), 0.0001)
        assertEquals(0.65, RankEngine.recencyFactor(70), 0.0001)
        assertEquals(0.50, RankEngine.recencyFactor(84), 0.0001)
        assertEquals(0.0, RankEngine.recencyFactor(85), 0.0001)
    }

    @Test
    fun fatigue_recentTrainingIsHigh() {
        val history = listOf(sessionWith(bench(), weight = 80.0, reps = 5, daysAgo = 0))
        val r = RankEngine.compute(history, library, bodyweight = 80.0, sex = "MALE")
        val pecho = r.groups.first { it.group == MuscleGroup.PECHO }
        assertEquals(0, pecho.daysSinceTrained)
        assertEquals(Fatigue.ALTA, pecho.fatigue)
    }

    @Test
    fun fatigue_oldTrainingIsRested() {
        // Entrenado hace 10 días (dentro de la ventana) -> descansado
        val history = listOf(sessionWith(bench(), weight = 80.0, reps = 5, daysAgo = 10))
        val r = RankEngine.compute(history, library, bodyweight = 80.0, sex = "MALE")
        val pecho = r.groups.first { it.group == MuscleGroup.PECHO }
        assertEquals(10, pecho.daysSinceTrained)
        assertEquals(Fatigue.DESCANSADO, pecho.fatigue)
    }

    @Test
    fun fatigue_untrainedGroupHasNoData() {
        val history = listOf(sessionWith(bench(), weight = 80.0, reps = 5, daysAgo = 0))
        val r = RankEngine.compute(history, library, bodyweight = 80.0, sex = "MALE")
        // La pierna no se entrenó -> sin datos de fatiga
        val pierna = r.groups.first { it.group == MuscleGroup.PIERNA }
        assertEquals(-1, pierna.daysSinceTrained)
        assertEquals(Fatigue.SIN_DATOS, pierna.fatigue)
    }
}
