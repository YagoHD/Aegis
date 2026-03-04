package com.yago.aegis.viewmodel

import android.R
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.yago.aegis.data.*
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import androidx.lifecycle.ViewModelProvider
import com.yago.aegis.data.SettingsStore

class WorkoutViewModelFactory(private val settingsStore: SettingsStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(settingsStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class WorkoutViewModel(private val settingsStore: SettingsStore) : ViewModel() {
    // Esta es la lista "viva" de ejercicios que el usuario está entrenando ahora mismo
    private val _exercisesProgress = mutableStateListOf<ExerciseProgress>()
    val exercisesProgress: List<ExerciseProgress> get() = _exercisesProgress
    val routines = settingsStore.routines
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
    fun calculateLastPerformed(dates: List<Long>?): String {
        val safeDates = dates ?: return "Never performed"
        if (safeDates.isEmpty()) return "Never performed"

        val now = System.currentTimeMillis()
        val lastDate = safeDates.last()

        val oneWeekAgo = now - TimeUnit.DAYS.toMillis(7)
        val sessionsThisWeek = safeDates.count { it >= oneWeekAgo }

        return if (sessionsThisWeek > 1) {
            "$sessionsThisWeek times this week"
        } else {
            val diffInMs = now - lastDate
            val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMs)

            when {
                diffInDays < 1L -> "Last performed: Today"
                diffInDays == 1L -> "Last performed: Yesterday"
                else -> "Last performed: $diffInDays days ago"
            }
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
    fun getSafeRoutine(routine: Routine): Routine {
        return if (routine.iconRes <= 0) {
            routine.copy(iconRes = R.drawable.ic_btn_speak_now)
        } else {
            routine
        }
    }
}