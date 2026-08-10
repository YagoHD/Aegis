package com.yago.aegis

import com.yago.aegis.data.Exercise
import com.yago.aegis.data.ExerciseProgress
import com.yago.aegis.data.ExerciseSet
import com.yago.aegis.data.WorkoutSession
import com.yago.aegis.data.withAddedSet
import com.yago.aegis.data.withRemovedSet
import com.yago.aegis.data.withUpdatedSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionOpsTest {

    private val exId = 42L
    private fun session(vararg sets: ExerciseSet) = WorkoutSession(
        routineName = "R",
        exercisesProgress = listOf(
            ExerciseProgress(Exercise(name = "X", type = "T", muscleGroup = "G", id = exId), sets.toList())
        )
    )

    @Test
    fun withAddedSet_appendsEmptySet() {
        val s = session(ExerciseSet(id = "s1", weight = 100.0, reps = 5))
        val out = s.withAddedSet(exId)
        assertEquals(2, out.exercisesProgress[0].sets.size)
    }

    @Test
    fun withUpdatedSet_updatesFields() {
        val s = session(ExerciseSet(id = "s1", weight = 0.0, reps = 0, isCompleted = false))
        val out = s.withUpdatedSet(exId, "s1", weight = 80.0, reps = 8, completed = true, loadModifier = 10.0)
        val set = out.exercisesProgress[0].sets.first { it.id == "s1" }
        assertEquals(80.0, set.weight, 0.0)
        assertEquals(8, set.reps)
        assertTrue(set.isCompleted)
        assertEquals(10.0, set.loadModifier, 0.0)
    }

    @Test
    fun withRemovedSet_keepsAtLeastOne() {
        val s = session(ExerciseSet(id = "s1"), ExerciseSet(id = "s2"))
        val afterOne = s.withRemovedSet(exId, "s1")
        assertEquals(listOf("s2"), afterOne.exercisesProgress[0].sets.map { it.id })
        // borrar la última no deja el ejercicio sin series
        val afterAll = afterOne.withRemovedSet(exId, "s2")
        assertEquals(1, afterAll.exercisesProgress[0].sets.size)
    }
}
