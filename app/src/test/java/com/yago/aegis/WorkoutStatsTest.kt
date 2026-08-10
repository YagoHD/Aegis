package com.yago.aegis

import com.yago.aegis.data.Exercise
import com.yago.aegis.data.ExerciseProgress
import com.yago.aegis.data.ExerciseSet
import com.yago.aegis.data.WorkoutSession
import com.yago.aegis.data.WorkoutStats
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutStatsTest {

    private fun ex() = Exercise(name = "X", type = "T", muscleGroup = "G")
    private fun set(w: Double, r: Int, done: Boolean = true) = ExerciseSet(weight = w, reps = r, isCompleted = done)

    @Test
    fun epley1RM_formula() {
        assertEquals(116.667, WorkoutStats.epley1RM(100.0, 5), 0.01)
        assertEquals(100.0, WorkoutStats.epley1RM(100.0, 0), 0.001)  // 1 rep-eq
    }

    @Test
    fun best1RM_ignoresBodyweightAndIncomplete() {
        val prog = ExerciseProgress(
            exercise = ex(),
            sets = listOf(
                set(100.0, 5),            // Epley 116.67
                set(120.0, 1),            // Epley 124  <- mejor
                set(0.0, 20),             // peso corporal -> ignorado
                set(200.0, 3, done = false) // no completada -> ignorada
            )
        )
        assertEquals(124.0, WorkoutStats.best1RM(prog), 0.01)
    }

    @Test
    fun best1RM_noWeighted_isZero() {
        val prog = ExerciseProgress(ex(), listOf(set(0.0, 10), set(50.0, 5, done = false)))
        assertEquals(0.0, WorkoutStats.best1RM(prog), 0.001)
    }

    @Test
    fun sessionVolume_onlyCompleted() {
        val session = WorkoutSession(
            routineName = "R",
            exercisesProgress = listOf(
                ExerciseProgress(ex(), listOf(set(100.0, 5), set(50.0, 10, done = false))), // 500 (+0)
                ExerciseProgress(ex(), listOf(set(20.0, 10)))                                // 200
            )
        )
        assertEquals(700.0, WorkoutStats.sessionVolume(session), 0.001)
    }
}
