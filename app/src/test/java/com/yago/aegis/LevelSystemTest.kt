package com.yago.aegis

import com.yago.aegis.data.Exercise
import com.yago.aegis.data.ExerciseProgress
import com.yago.aegis.data.ExerciseSet
import com.yago.aegis.data.LevelSystem
import com.yago.aegis.data.WorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Test

class LevelSystemTest {

    private fun ex() = Exercise(name = "X", type = "COMPOUND", muscleGroup = "CHEST")

    /** Sesión con un ejercicio y una serie de [weight]×[reps] completada (volumen = weight*reps). */
    private fun session(weight: Double, reps: Int): WorkoutSession =
        WorkoutSession(
            routineName = "T",
            exercisesProgress = listOf(
                ExerciseProgress(
                    exercise = ex(),
                    sets = listOf(ExerciseSet(reps = reps, weight = weight, isCompleted = true))
                )
            )
        )

    @Test
    fun empty_isLevel1() {
        val s = LevelSystem.compute(emptyList(), 0)
        assertEquals(1, s.level)
        assertEquals(0L, s.totalXp)
        assertEquals(0f, s.progress, 0.001f)
    }

    @Test
    fun oneSession_addsBaseAndVolumeXp() {
        // volumen 100x10 = 1000 -> 50 base + 20 (1000/1000*20) = 70 XP
        val s = LevelSystem.compute(listOf(session(100.0, 10)), 0)
        assertEquals(70L, s.totalXp)
        assertEquals(1, s.level)
        assertEquals(70L, s.xpIntoLevel)
        assertEquals(100L, s.xpForLevel)
        assertEquals(0.7f, s.progress, 0.001f)
    }

    @Test
    fun streakGivesBonus() {
        // sin entrenos, racha 3 semanas -> 3 * 30 = 90 XP
        val s = LevelSystem.compute(emptyList(), 3)
        assertEquals(90L, s.totalXp)
    }

    @Test
    fun enoughXp_levelsUp() {
        // 2 sesiones de volumen 2500 -> (50 + 50) * 2 = 200 XP -> nivel 2 (umbral 100)
        val history = listOf(session(100.0, 25), session(100.0, 25))
        val s = LevelSystem.compute(history, 0)
        assertEquals(200L, s.totalXp)
        assertEquals(2, s.level)
        assertEquals(100L, s.xpIntoLevel)
        assertEquals(200L, s.xpForLevel)
        assertEquals(0.5f, s.progress, 0.001f)
    }
}
