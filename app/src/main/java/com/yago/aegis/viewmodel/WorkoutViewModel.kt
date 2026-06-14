package com.yago.aegis.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.R
import com.yago.aegis.data.ExerciseProgress
import com.yago.aegis.data.ExerciseSet
import com.yago.aegis.data.Routine
import com.yago.aegis.data.effectiveSlots
import com.yago.aegis.data.withSafeDefaults
import com.yago.aegis.data.ExerciseSummary
import com.yago.aegis.data.UserRepository
import com.yago.aegis.data.WorkoutSession
import com.yago.aegis.data.WorkoutSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class WorkoutViewModel(
    application: Application,
    private val repository: UserRepository
) : AndroidViewModel(application) {

    private val _activeSession = MutableStateFlow<WorkoutSession?>(null)
    val activeSession: StateFlow<WorkoutSession?> = _activeSession.asStateFlow()

    // Sesión pausada — el usuario puede navegar fuera y volver
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    // Tiempo de inicio para calcular duración
    private var sessionStartTime: Long = 0L

    // Resumen post-entrenamiento
    private val _workoutSummary = MutableStateFlow<WorkoutSummary?>(null)
    val workoutSummary: StateFlow<WorkoutSummary?> = _workoutSummary.asStateFlow()

    fun clearSummary() { _workoutSummary.value = null }

    private val _uncompletedWithData = MutableStateFlow<List<ExerciseProgress>>(emptyList())
    val uncompletedWithData: StateFlow<List<ExerciseProgress>> = _uncompletedWithData.asStateFlow()

    // ─────────────────────────────────────────────
    // TIMER DE DESCANSO
    // ─────────────────────────────────────────────

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    // Emite true una vez cuando el timer llega a 0 (para disparar vibración/sonido en la UI)
    private val _timerFinished = MutableStateFlow(false)
    val timerFinished: StateFlow<Boolean> = _timerFinished.asStateFlow()

    private var timerJob: Job? = null

    val restTimerSeconds: StateFlow<Int> = repository.restTimerSeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 90)

    val timerVibrate: StateFlow<Boolean> = repository.timerVibrate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val timerSound: StateFlow<Boolean> = repository.timerSound
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val showRestTimer: StateFlow<Boolean> = repository.showRestTimer
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val timerPosX: StateFlow<Float> = repository.timerPosX
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1f)

    val timerPosY: StateFlow<Float> = repository.timerPosY
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1f)

    fun startTimer() {
        timerJob?.cancel()
        _timerSeconds.value = restTimerSeconds.value
        _timerRunning.value = true
        _timerFinished.value = false

        timerJob = viewModelScope.launch {
            while (_timerSeconds.value > 0) {
                delay(1000)
                _timerSeconds.update { it - 1 }
            }
            _timerRunning.value = false
            _timerFinished.value = true  // La pantalla observa esto para vibrar/sonar
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _timerRunning.value = false
        _timerSeconds.value = 0
    }

    fun addOneMinuteToTimer() {
        if (_timerRunning.value) _timerSeconds.update { it + 60 }
    }

    fun onTimerFinishedHandled() {
        _timerFinished.value = false
    }

    fun saveTimerPosition(x: Float, y: Float) {
        viewModelScope.launch { repository.updateTimerPosition(x, y) }
    }

    // ─────────────────────────────────────────────
    // SESIÓN
    // ─────────────────────────────────────────────

    fun startWorkout(routine: Routine) {
        // Si ya hay sesión activa para esta rutina (ej: volvemos de Settings),
        // no la reiniciamos — simplemente reanudamos
        val current = _activeSession.value
        if (current != null && current.routineName == routine.name) {
            resumeWorkout()
            return
        }
        _isPaused.value = false
        sessionStartTime = System.currentTimeMillis()
        viewModelScope.launch {
            val history = repository.workoutHistory.first()
            val libraryExercises = repository.getAllExercises().first()
            val routineHistory = history
                .filter { session -> session.routineName == routine.name }
                .sortedByDescending { session -> session.date }

            // Función auxiliar: busca el último rendimiento de un ejercicio dado su nombre
            fun findLastPerformance(exerciseName: String): String? {
                for (session in routineHistory) {
                    val ep = session.exercisesProgress.find { it.exercise.name == exerciseName }
                    if (ep != null) {
                        val completedSets = ep.sets.filter { it.isCompleted }
                        if (completedSets.isNotEmpty()) {
                            return completedSets.joinToString("   ") { s ->
                                val w = when {
                                    s.weight == 0.0 -> "BW"
                                    s.weight % 1 == 0.0 -> "${s.weight.toInt()}kg"
                                    else -> "${"%.1f".format(s.weight)}kg"
                                }
                                "${w} x ${s.reps}"
                            }
                        }
                    }
                }
                return null
            }

            // Construir ExerciseProgress por slot — pre-carga el historial de TODAS las variantes
            val progress = routine.effectiveSlots().map { slot ->
                val variantsWithHistory = slot.variants.map { exercise ->
                    // Traer notes e isBodyweight frescos desde la librería (el ejercicio en la
                    // rutina es una copia antigua que puede no tener los últimos cambios)
                    val fresh = libraryExercises.find { it.id == exercise.id }
                    exercise.copy(
                        lastPerformance = findLastPerformance(exercise.name) ?: exercise.lastPerformance,
                        notes = fresh?.notes ?: exercise.notes,
                        isBodyweight = fresh?.isBodyweight ?: exercise.isBodyweight
                    )
                }
                ExerciseProgress(
                    exercise = variantsWithHistory.first(),
                    sets = listOf(ExerciseSet()),
                    slotVariants = variantsWithHistory   // cache de variantes con historial
                )
            }

            _activeSession.value = WorkoutSession(
                routineName = routine.name,
                exercisesProgress = progress
            )
        }
    }

    fun updateSessionNotes(notes: String) {
        _activeSession.update { it?.copy(notes = notes) }
    }

    fun saveSessionNotes(notes: String) {
        // Actualiza las notas en la última sesión guardada en el historial
        if (notes.isBlank()) return
        viewModelScope.launch {
            val history = repository.workoutHistory.first()
            val lastSession = history.lastOrNull() ?: return@launch
            val updated = lastSession.copy(notes = notes)
            repository.updateWorkoutSession(updated)
        }
    }

    fun pauseWorkout() {
        stopTimer()
        _isPaused.value = true
    }

    fun resumeWorkout() {
        _isPaused.value = false
    }

    fun addSet(exerciseId: Long) {
        _activeSession.update { session ->
            session?.copy(exercisesProgress = session.exercisesProgress.map { prog ->
                if (prog.exercise.id == exerciseId) prog.copy(sets = prog.sets + ExerciseSet()) else prog
            })
        }
    }

    fun updateSet(exerciseId: Long, setId: String, weight: Double, reps: Int, completed: Boolean) {
        _activeSession.update { session ->
            session?.copy(exercisesProgress = session.exercisesProgress.map { prog ->
                if (prog.exercise.id == exerciseId) {
                    prog.copy(sets = prog.sets.map { set ->
                        if (set.id == setId) set.copy(weight = weight, reps = reps, isCompleted = completed) else set
                    })
                } else prog
            })
        }
    }

    fun removeSet(exerciseId: Long, setId: String) {
        _activeSession.update { session ->
            session?.copy(exercisesProgress = session.exercisesProgress.map { prog ->
                if (prog.exercise.id == exerciseId) {
                    val newSets = prog.sets.filter { it.id != setId }
                    prog.copy(sets = if (newSets.isEmpty()) listOf(ExerciseSet()) else newSets)
                } else prog
            })
        }
    }

    fun toggleExerciseCompleted(exerciseId: Long) {
        _activeSession.update { session ->
            session?.copy(exercisesProgress = session.exercisesProgress.map { prog ->
                if (prog.exercise.id == exerciseId) {
                    val allDone = prog.sets.all { it.isCompleted }
                    prog.copy(sets = prog.sets.map { it.copy(isCompleted = !allDone) })
                } else prog
            })
        }
    }

    /**
     * Cambia la variante activa de un slot durante la sesión.
     * Usa la posición del slot (slotPosition) para ser inmune a IDs duplicados.
     * Resetea las series al cambiar (el usuario va a hacer un ejercicio diferente).
     */
    fun switchVariant(slotPosition: Int, newVariantIndex: Int) {
        _activeSession.update { session ->
            val list = session?.exercisesProgress?.toMutableList() ?: return@update session
            val prog = list.getOrNull(slotPosition) ?: return@update session
            if (prog.slotVariants.size <= 1) return@update session
            val newVariant = prog.slotVariants.getOrNull(newVariantIndex) ?: return@update session
            list[slotPosition] = prog.copy(
                exercise = newVariant,
                sets = listOf(ExerciseSet())
            )
            session.copy(exercisesProgress = list)
        }
    }

    fun requestFinishWorkout() {
        val session = _activeSession.value ?: return
        val uncompleted = session.exercisesProgress.filter { progress ->
            val hasData = progress.sets.any { it.weight > 0 || it.reps > 0 }
            val isNotChecked = !progress.sets.all { it.isCompleted }
            hasData && isNotChecked
        }
        if (uncompleted.isNotEmpty()) _uncompletedWithData.value = uncompleted
        else finishWorkout()
    }

    fun forceFinishWorkout(routinesViewModel: RoutinesViewModel, onComplete: () -> Unit) {
        _uncompletedWithData.value.forEach { toggleExerciseCompleted(it.exercise.id) }
        _uncompletedWithData.value = emptyList()
        finishWorkout(routinesViewModel, onComplete)
    }

    fun dismissUncompletedDialog() { _uncompletedWithData.value = emptyList() }

    fun finishWorkout(routinesViewModel: RoutinesViewModel? = null, onComplete: () -> Unit = {}) {
        val session = _activeSession.value ?: return
        val hasAnyData = session.exercisesProgress.any { it.sets.any { s -> s.weight > 0 || s.reps > 0 } }
        stopTimer()
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
            // Calcular resumen post-entrenamiento
            val durationMs = System.currentTimeMillis() - sessionStartTime
            val totalVolume = session.exercisesProgress.sumOf { prog ->
                prog.sets.filter { it.isCompleted }.sumOf { it.weight * it.reps }
            }
            val completedExercises = session.exercisesProgress.filter { prog ->
                prog.sets.any { it.isCompleted }
            }
            val exerciseSummaries = completedExercises.map { prog ->
                val completedSets = prog.sets.filter { it.isCompleted }
                val avgWeight = if (completedSets.isNotEmpty()) completedSets.map { it.weight }.average() else 0.0
                val maxWeight = completedSets.maxOfOrNull { it.weight } ?: 0.0
                val isBodyweight = completedSets.all { it.weight == 0.0 }
                val isNewPR = maxWeight > prog.exercise.oneRepMax && maxWeight > 0.0
                ExerciseSummary(
                    name = prog.exercise.name,
                    sets = completedSets.size,
                    avgWeight = avgWeight,
                    maxWeight = maxWeight,
                    isBodyweight = isBodyweight,
                    isNewPR = isNewPR
                )
            }
            _workoutSummary.value = WorkoutSummary(
                routineName = session.routineName,
                durationMs = durationMs,
                totalVolume = totalVolume,
                exerciseCount = completedExercises.size,
                exercises = exerciseSummaries
            )
            _activeSession.value = null
            onComplete()
        }
    }

    fun cancelWorkout(onComplete: () -> Unit) {
        stopTimer()
        _activeSession.value = null
        onComplete()
    }

    fun getSafeRoutine(routine: Routine): Routine {
        // Gson puede inyectar null en campos non-null al deserializar datos antiguos
        val safe = routine.withSafeDefaults()
        val currentIcon = safe.iconName ?: "dumbbell"
        return if (currentIcon.isBlank()) safe.copy(iconName = "dumbbell") else safe.copy(iconName = currentIcon)
    }

    fun calculateLastPerformed(dates: List<Long>?): String {
        val ctx = getApplication<Application>()
        val safeDates = dates ?: return ctx.getString(R.string.last_performed_never)
        if (safeDates.isEmpty()) return ctx.getString(R.string.last_performed_never)
        val diffInDays = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - safeDates.last())
        return when {
            diffInDays < 1L -> ctx.getString(R.string.last_performed_today)
            diffInDays == 1L -> ctx.getString(R.string.last_performed_yesterday)
            else -> ctx.getString(R.string.last_performed_days_ago, diffInDays.toInt())
        }
    }

    class Factory(
        private val application: Application,
        private val repository: UserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WorkoutViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
