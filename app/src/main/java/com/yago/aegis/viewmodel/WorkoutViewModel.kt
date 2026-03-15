package com.yago.aegis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.ExerciseProgress
import com.yago.aegis.data.ExerciseSet
import com.yago.aegis.data.Routine
import com.yago.aegis.data.UserRepository
import com.yago.aegis.data.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// WorkoutViewModel ahora recibe UserRepository (capa de datos correcta),
// NO SettingsStore directamente como estaba antes.
class WorkoutViewModel(private val repository: UserRepository) : ViewModel() {

    private val _activeSession = MutableStateFlow<WorkoutSession?>(null)
    val activeSession: StateFlow<WorkoutSession?> = _activeSession.asStateFlow()

    // Estado para ejercicios con datos pero sin marcar (detectado aquí, no en la Screen)
    private val _uncompletedWithData = MutableStateFlow<List<ExerciseProgress>>(emptyList())
    val uncompletedWithData: StateFlow<List<ExerciseProgress>> = _uncompletedWithData.asStateFlow()

    fun startWorkout(routine: Routine) {
        val progress = routine.exercises.map { exercise ->
            ExerciseProgress(exercise = exercise, sets = listOf(ExerciseSet()))
        }
        _activeSession.value = WorkoutSession(
            routineName = routine.name,
            exercisesProgress = progress
        )
    }

    fun addSet(exerciseId: Long) {
        _activeSession.update { session ->
            session?.copy(
                exercisesProgress = session.exercisesProgress.map { prog ->
                    if (prog.exercise.id == exerciseId) prog.copy(sets = prog.sets + ExerciseSet())
                    else prog
                }
            )
        }
    }

    fun updateSet(exerciseId: Long, setId: String, weight: Double, reps: Int, completed: Boolean) {
        _activeSession.update { session ->
            session?.copy(
                exercisesProgress = session.exercisesProgress.map { prog ->
                    if (prog.exercise.id == exerciseId) {
                        prog.copy(sets = prog.sets.map { set ->
                            if (set.id == setId) set.copy(weight = weight, reps = reps, isCompleted = completed)
                            else set
                        })
                    } else prog
                }
            )
        }
    }

    fun removeSet(exerciseId: Long, setId: String) {
        _activeSession.update { session ->
            session?.copy(
                exercisesProgress = session.exercisesProgress.map { prog ->
                    if (prog.exercise.id == exerciseId) {
                        val newSets = prog.sets.filter { it.id != setId }
                        prog.copy(sets = if (newSets.isEmpty()) listOf(ExerciseSet()) else newSets)
                    } else prog
                }
            )
        }
    }

    fun toggleExerciseCompleted(exerciseId: Long) {
        _activeSession.update { session ->
            session?.copy(
                exercisesProgress = session.exercisesProgress.map { prog ->
                    if (prog.exercise.id == exerciseId) {
                        val allDone = prog.sets.all { it.isCompleted }
                        prog.copy(sets = prog.sets.map { it.copy(isCompleted = !allDone) })
                    } else prog
                }
            )
        }
    }

    // Lógica de validación extraída de ActiveSessionScreen (antes estaba en el onClick del botón)
    fun requestFinishWorkout() {
        val session = _activeSession.value ?: return
        val uncompleted = session.exercisesProgress.filter { progress ->
            val hasData = progress.sets.any { it.weight > 0 || it.reps > 0 }
            val isNotChecked = !progress.sets.all { it.isCompleted }
            hasData && isNotChecked
        }
        if (uncompleted.isNotEmpty()) {
            _uncompletedWithData.value = uncompleted
        } else {
            finishWorkout()
        }
    }

    // Marca los ejercicios sin marcar y finaliza (acción del diálogo de confirmación)
    fun forceFinishWorkout(routinesViewModel: RoutinesViewModel, onComplete: () -> Unit) {
        _uncompletedWithData.value.forEach { toggleExerciseCompleted(it.exercise.id) }
        _uncompletedWithData.value = emptyList()
        finishWorkout(routinesViewModel, onComplete)
    }

    fun dismissUncompletedDialog() {
        _uncompletedWithData.value = emptyList()
    }

    fun finishWorkout(routinesViewModel: RoutinesViewModel? = null, onComplete: () -> Unit = {}) {
        val session = _activeSession.value ?: return
        val hasAnyData = session.exercisesProgress.any { it.sets.any { s -> s.weight > 0 || s.reps > 0 } }

        viewModelScope.launch {
            if (hasAnyData) {
                repository.saveWorkoutSession(session)
                session.exercisesProgress.forEach { progress ->
                    val completedSets = progress.sets.filter { it.isCompleted }
                    if (completedSets.isNotEmpty()) {
                        val summary = completedSets.joinToString("   ") { set ->
                            val w = if (set.weight % 1 == 0.0) set.weight.toInt() else set.weight
                            "${w}kg x ${set.reps}"
                        }
                        val best1RM = completedSets.maxOf { it.weight * (1 + (it.reps / 30.0)) }
                        routinesViewModel?.updateExercisePerformance(
                            exerciseId = progress.exercise.id,
                            summary = summary,
                            new1RM = best1RM
                        )
                    }
                }
            }
            _activeSession.value = null
            onComplete()
        }
    }

    fun cancelWorkout(onComplete: () -> Unit) {
        _activeSession.value = null
        onComplete()
    }

    // --- UTILIDADES PARA SelectRoutineScreen ---

    fun getSafeRoutine(routine: Routine): Routine {
        val currentIcon = routine.iconName ?: "dumbbell"
        return if (currentIcon.isBlank()) routine.copy(iconName = "dumbbell") else routine.copy(iconName = currentIcon)
    }

    fun calculateLastPerformed(dates: List<Long>?): String {
        val safeDates = dates ?: return "Never performed"
        if (safeDates.isEmpty()) return "Never performed"
        val diffInDays = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - safeDates.last())
        return when {
            diffInDays < 1L -> "Last performed: Today"
            diffInDays == 1L -> "Last performed: Yesterday"
            else -> "Last performed: $diffInDays days ago"
        }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WorkoutViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
