package com.yago.aegis.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.yago.aegis.data.*

class WorkoutViewModel : ViewModel() {

    // Esta es la lista "viva" de ejercicios que el usuario está entrenando ahora mismo
    private val _exercisesProgress = mutableStateListOf<ExerciseProgress>()
    val exercisesProgress: List<ExerciseProgress> get() = _exercisesProgress

    /**
     * Inicializa la sesión con los ejercicios de la rutina seleccionada.
     */
    fun startWorkout(routine: Routine) {
        _exercisesProgress.clear()
        routine.exercises.forEach { exercise ->
            _exercisesProgress.add(
                ExerciseProgress(
                    exercise = exercise,
                    sets = mutableStateListOf(ExerciseSet(reps = 0, weight = 0.0))
                )
            )
        }
    }

    /**
     * Añade una nueva serie vacía a un ejercicio específico.
     */
    fun addSet(exerciseId: Long) {
        val index = _exercisesProgress.indexOfFirst { it.exercise.id == exerciseId }
        if (index != -1) {
            val currentProgress = _exercisesProgress[index]
            // En Compose, para que la lista se actualice, a veces es mejor reemplazar el objeto
            val updatedSets = currentProgress.sets.toMutableList().apply {
                add(ExerciseSet(reps = 0, weight = 0.0))
            }
            _exercisesProgress[index] = currentProgress.copy(sets = updatedSets)
        }
    }

    /**
     * Actualiza los datos de una serie (reps o peso).
     */
    fun updateSet(exerciseId: Long, setId: String, newReps: Int? = null, newWeight: Double? = null) {
        val exerciseIndex = _exercisesProgress.indexOfFirst { it.exercise.id == exerciseId }
        if (exerciseIndex != -1) {
            val progress = _exercisesProgress[exerciseIndex]
            val updatedSets = progress.sets.map { set ->
                if (set.id == setId) {
                    set.copy(
                        reps = newReps ?: set.reps,
                        weight = newWeight ?: set.weight
                    )
                } else set
            }
            _exercisesProgress[exerciseIndex] = progress.copy(sets = updatedSets)
        }
    }

    /**
     * Marca una serie como completada (el check verde).
     */
    fun toggleSetCompleted(exerciseId: Long, setId: String) {
        val exerciseIndex = _exercisesProgress.indexOfFirst { it.exercise.id == exerciseId }
        if (exerciseIndex != -1) {
            val progress = _exercisesProgress[exerciseIndex]
            val updatedSets = progress.sets.map { set ->
                if (set.id == setId) set.copy(isCompleted = !set.isCompleted) else set
            }
            _exercisesProgress[exerciseIndex] = progress.copy(sets = updatedSets)
        }
    }
}