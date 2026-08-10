package com.yago.aegis.data

/**
 * Operaciones PURAS sobre una WorkoutSession (añadir/actualizar/borrar series). Extraídas del
 * WorkoutViewModel para poder testearlas sin Android y mantener el VM fino (US-08).
 */

fun WorkoutSession.withAddedSet(exerciseId: Long): WorkoutSession =
    copy(exercisesProgress = exercisesProgress.map { p ->
        if (p.exercise.id == exerciseId) p.copy(sets = p.sets + ExerciseSet()) else p
    })

fun WorkoutSession.withUpdatedSet(
    exerciseId: Long,
    setId: String,
    weight: Double,
    reps: Int,
    completed: Boolean,
    loadModifier: Double
): WorkoutSession =
    copy(exercisesProgress = exercisesProgress.map { p ->
        if (p.exercise.id == exerciseId)
            p.copy(sets = p.sets.map { s ->
                if (s.id == setId) s.copy(weight = weight, reps = reps, isCompleted = completed, loadModifier = loadModifier)
                else s
            })
        else p
    })

/** Borra una serie; si el ejercicio quedara sin series, deja una vacía (nunca 0 series). */
fun WorkoutSession.withRemovedSet(exerciseId: Long, setId: String): WorkoutSession =
    copy(exercisesProgress = exercisesProgress.map { p ->
        if (p.exercise.id == exerciseId) {
            val newSets = p.sets.filter { it.id != setId }
            p.copy(sets = if (newSets.isEmpty()) listOf(ExerciseSet()) else newSets)
        } else p
    })
