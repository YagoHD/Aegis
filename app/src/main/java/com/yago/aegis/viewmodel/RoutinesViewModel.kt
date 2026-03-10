package com.yago.aegis.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.Routine
import com.yago.aegis.data.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoutinesViewModel(private val repository: UserRepository) : ViewModel() {

    // ✅ Fuente única para la librería de ejercicios (Base de Datos)
    val allExercises: StateFlow<List<Exercise>> = repository.getAllExercises()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ✅ Etiquetas globales sincronizadas
    val globalTags: StateFlow<List<String>> = repository.globalTags
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("COMPOUND", "CHEST", "LEGS", "BACK", "SHOULDERS")
        )

    // ✅ Lista de rutinas que se muestran en la UI
    var routines = mutableStateListOf<Routine>()
        private set

    // Lista temporal para la pantalla de creación/edición
    var tempExercises = mutableStateListOf<Exercise>()
        private set

    init {
        // Observamos los cambios en el repositorio para mantener la lista 'routines' al día
        viewModelScope.launch {
            repository.routines.collect { savedRoutines ->
                routines.clear()
                routines.addAll(savedRoutines)
            }
        }
    }

    /**
     * Guarda el estado actual de las rutinas en el almacenamiento persistente (DataStore).
     */
    private fun persistChanges() {
        viewModelScope.launch {
            repository.updateRoutines(routines.toList())
        }
    }

    // --- 🛠️ GESTIÓN DE RENDIMIENTO (EL "CORAZÓN" DEL GUARDADO) ---

    /**
     * Actualiza el texto de 'Último Entrenamiento' en un ejercicio específico.
     * Esta función es llamada por el WorkoutViewModel al finalizar una sesión.
     */
    fun updateExercisePerformance(exerciseId: Long, summary: String, new1RM: Double) {
        viewModelScope.launch {
            // 1. Obtenemos la versión más reciente de la librería directamente del repositorio
            // Usamos .first() para leer el valor actual sin quedarnos escuchando
            val currentLibrary = repository.getAllExercises().first()
            val exerciseInLibrary = currentLibrary.find { it.id == exerciseId }

            exerciseInLibrary?.let { currentExercise ->
                val updatedPR = if (new1RM > currentExercise.oneRepMax) {
                    // Redondeo manual para evitar problemas de formato
                    (kotlin.math.round(new1RM * 10) / 10.0)
                } else {
                    currentExercise.oneRepMax
                }

                // LOG DE CONTROL: Mira el Logcat en Android Studio con el filtro "AEGIS_DEBUG"
                android.util.Log.d("AEGIS_DEBUG", "Actualizando Ejercicio: ${currentExercise.name} | Nuevo PR: $updatedPR")

                val updatedExercise = currentExercise.copy(
                    lastPerformance = summary,
                    oneRepMax = updatedPR
                )

                // GUARDADO CRÍTICO EN EL REPOSITORIO
                repository.upsertExercise(updatedExercise)
            }

            // 2. Actualizar las rutinas locales (esto es lo que ya hacías)
            val updatedRoutines = routines.map { routine ->
                routine.copy(
                    exercises = routine.exercises.map { exercise ->
                        if (exercise.id == exerciseId) {
                            val updatedPR = if (new1RM > exercise.oneRepMax) (kotlin.math.round(new1RM * 10) / 10.0) else exercise.oneRepMax
                            exercise.copy(lastPerformance = summary, oneRepMax = updatedPR)
                        } else exercise
                    }
                )
            }
            routines.clear()
            routines.addAll(updatedRoutines)
            persistChanges()
        }
    }

    /**
     * Función auxiliar para extraer el peso y calcular el 1RM aproximado
     * Espera un formato tipo "100.0kg x 8"
     */
    private fun extract1RMFromSummary(summary: String): Double {
        return try {
            // Buscamos el peso antes de "kg" y las reps después de "x"
            val weight = summary.substringBefore("kg").trim().toDouble()
            val reps = summary.substringAfter("x").trim().toInt()

            // Fórmula de Brzycki: Peso * (36 / (37 - Reps)) o la simplificada:
            // Peso * (1 + 0.0333 * reps)
            (weight * (1 + (reps / 30.0))).let {
                kotlin.math.round(it * 10) / 10.0 // Redondear a 1 decimal
            }
        } catch (e: Exception) {
            0.0
        }
    }

    // --- 📁 GESTIÓN DE RUTINAS ---

    fun addRoutine(name: String, iconName: String = "dumbbell") {
        val newId = (routines.maxOfOrNull { it.id } ?: 0) + 1
        routines.add(
            Routine(
                id = newId,
                name = name.uppercase(),
                exercises = emptyList(),
                iconName = iconName
            )
        )
        persistChanges()
    }

    fun removeRoutine(routine: Routine) {
        routines.remove(routine)
        persistChanges()
    }

    fun updateRoutineFull(id: Int, newName: String, newExercises: List<Exercise>, newIconName: String) {
        val index = routines.indexOfFirst { it.id == id }
        if (index != -1) {
            routines[index] = routines[index].copy(
                name = newName.uppercase(),
                exercises = newExercises,
                iconName = newIconName
            )
            persistChanges()
        }
    }

    // --- 📚 GESTIÓN DE LIBRERÍA DE EJERCICIOS ---

    fun saveOrUpdateExercise(exercise: Exercise) {
        viewModelScope.launch {
            val formattedExercise = exercise.copy(name = exercise.name.uppercase())
            repository.upsertExercise(formattedExercise)
        }
    }

    fun deleteExerciseFromLibrary(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
            removeExerciseFromAllRoutines(exercise.name)
        }
    }

    private fun removeExerciseFromAllRoutines(exerciseName: String) {
        val updatedRoutines = routines.map { routine ->
            routine.copy(exercises = routine.exercises.filterNot { it.name == exerciseName })
        }
        routines.clear()
        routines.addAll(updatedRoutines)
        persistChanges()
    }

    // --- 🏷️ GESTIÓN DE TAGS ---

    fun addGlobalTag(tag: String) {
        viewModelScope.launch {
            val currentTags = globalTags.value.toMutableList()
            if (!currentTags.contains(tag.uppercase())) {
                currentTags.add(tag.uppercase())
                repository.updateGlobalTags(currentTags)
            }
        }
    }

    fun removeGlobalTags(tagsToRemove: List<String>) {
        viewModelScope.launch {
            val currentTags = globalTags.value.toMutableList()
            currentTags.removeAll(tagsToRemove)
            repository.updateGlobalTags(currentTags)
        }
    }

    // --- ⏳ GESTIÓN TEMPORAL (EDICIÓN) ---

    fun setTempExercises(exercises: List<Exercise>?) {
        tempExercises.clear()
        exercises?.let { tempExercises.addAll(it) }
    }

    fun addExerciseToTemp(exercise: Exercise) {
        if (!tempExercises.any { it.id == exercise.id }) {
            tempExercises.add(exercise)
        }
    }

    fun clearTempExercises() {
        tempExercises.clear()
    }

    fun moveExercise(from: Int, to: Int) {
        if (from in tempExercises.indices && to in tempExercises.indices) {
            tempExercises.apply { add(to, removeAt(from)) }
        }
    }

    // Factory para crear el ViewModel
    class RoutinesViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoutinesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RoutinesViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}