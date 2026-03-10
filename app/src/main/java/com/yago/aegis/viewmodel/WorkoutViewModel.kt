package com.yago.aegis.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Factory para instanciar el ViewModel con sus dependencias.
 */
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

    // Sesión activa que contiene el progreso de los ejercicios
    var activeSession by mutableStateOf<WorkoutSession?>(null)
        private set

    /**
     * Inicia una nueva sesión de entrenamiento basada en una rutina.
     */
    fun startWorkout(routine: Routine) {
        val progress = routine.exercises.map { exercise ->
            ExerciseProgress(
                exercise = exercise,
                sets = listOf(ExerciseSet()) // Empezamos con una única serie vacía
            )
        }
        activeSession = WorkoutSession(
            routineName = routine.name,
            exercisesProgress = progress
        )
    }

    /**
     * Añade una nueva serie a un ejercicio específico dentro de la sesión activa.
     */
    fun addSet(exerciseId: Long) {
        activeSession = activeSession?.let { session ->
            session.copy(
                exercisesProgress = session.exercisesProgress.map { prog ->
                    if (prog.exercise.id == exerciseId) {
                        prog.copy(sets = prog.sets + ExerciseSet())
                    } else prog
                }
            )
        }
    }

    /**
     * Actualiza los datos de una serie específica (Peso, Reps, Estado).
     */
    fun updateSet(exerciseId: Long, setId: String, weight: Double, reps: Int, completed: Boolean) {
        activeSession = activeSession?.let { session ->
            session.copy(
                exercisesProgress = session.exercisesProgress.map { prog ->
                    if (prog.exercise.id == exerciseId) {
                        prog.copy(sets = prog.sets.map { set ->
                            if (set.id == setId) {
                                set.copy(weight = weight, reps = reps, isCompleted = completed)
                            } else set
                        })
                    } else prog
                }
            )
        }
    }

    /**
     * Elimina una serie. Si es la última, la resetea pero no deja el ejercicio vacío.
     */
    fun removeSet(exerciseId: Long, setId: String) {
        activeSession = activeSession?.let { session ->
            session.copy(
                exercisesProgress = session.exercisesProgress.map { prog ->
                    if (prog.exercise.id == exerciseId) {
                        val newSets = prog.sets.filter { it.id != setId }
                        prog.copy(sets = if (newSets.isEmpty()) listOf(ExerciseSet()) else newSets)
                    } else prog
                }
            )
        }
    }

    /**
     * Marca/Desmarca todas las series de un ejercicio de golpe.
     */
    fun toggleExerciseCompleted(exerciseId: Long) {
        activeSession = activeSession?.let { session ->
            session.copy(
                exercisesProgress = session.exercisesProgress.map { prog ->
                    if (prog.exercise.id == exerciseId) {
                        val allDone = prog.sets.all { it.isCompleted }
                        prog.copy(sets = prog.sets.map { it.copy(isCompleted = !allDone) })
                    } else prog
                }
            )
        }
    }

    /**
     * Finaliza el entrenamiento, procesa los datos para el historial y limpia la sesión.
     */
    fun finishWorkout(routinesViewModel: RoutinesViewModel, onComplete: () -> Unit) {
        val session = activeSession ?: return

        viewModelScope.launch {
            settingsStore.saveWorkoutSession(session)
        }

        session.exercisesProgress.forEach { progress ->
            val completedSets = progress.sets.filter { it.isCompleted }

            if (completedSets.isNotEmpty()) {
                val summary = completedSets.joinToString("   ") { set ->
                    val w = if (set.weight % 1 == 0.0) set.weight.toInt() else set.weight
                    "${w}kg x ${set.reps}"
                }

                val best1RMOfSession = completedSets.maxOf { set ->
                    set.weight * (1 + (set.reps / 30.0))
                }

                routinesViewModel.updateExercisePerformance(
                    exerciseId = progress.exercise.id,
                    summary = summary,
                    new1RM = best1RMOfSession
                )
            }
        }

        activeSession = null
        onComplete()
    }

    // --- FUNCIONES DE UTILIDAD PARA LA PANTALLA DE SELECCIÓN ---

    fun getSafeRoutine(routine: Routine): Routine {
        val currentIcon = routine.iconName ?: "dumbbell"
        return if (currentIcon.isBlank()) {
            routine.copy(iconName = "dumbbell")
        } else {
            routine.copy(iconName = currentIcon)
        }
    }

    fun calculateLastPerformed(dates: List<Long>?): String {
        val safeDates = dates ?: return "Never performed"
        if (safeDates.isEmpty()) return "Never performed"

        val now = System.currentTimeMillis()
        val lastDate = safeDates.last()
        val diffInMs = now - lastDate
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMs)

        return when {
            diffInDays < 1L -> "Last performed: Today"
            diffInDays == 1L -> "Last performed: Yesterday"
            else -> "Last performed: $diffInDays days ago"
        }
    }
}