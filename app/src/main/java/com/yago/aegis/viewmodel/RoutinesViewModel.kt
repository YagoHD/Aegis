package com.yago.aegis.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.Routine
import com.yago.aegis.data.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoutinesViewModel(private val repository: UserRepository) : ViewModel() {
    var tempExercises = mutableStateListOf<Exercise>()
        private set
    // ✅ La lista empieza vacía y se llena desde el disco
    var routines = mutableStateListOf<Routine>()
        private set
    val exerciseLibrary: StateFlow<List<Exercise>> = repository.exerciseLibrary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalTags: StateFlow<List<String>> = repository.globalTags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("COMPOUND", "CHEST"))
    init {
        // Al arrancar, "escuchamos" las rutinas guardadas en el repositorio
        viewModelScope.launch {
            repository.routines.collect { savedRoutines ->
                routines.clear()
                routines.addAll(savedRoutines)
            }
        }
    }

    // Función interna para no repetir código: guarda el estado actual en el disco
    private fun persistChanges() {
        viewModelScope.launch {
            repository.updateRoutines(routines.toList())
        }
    }

    fun addRoutine(name: String) {
        val newId = (routines.maxOfOrNull { it.id } ?: 0) + 1
        routines.add(Routine(newId, name.uppercase(), emptyList(), 0))
        persistChanges()
    }

    fun removeRoutine(routine: Routine) {
        routines.remove(routine)
        persistChanges() // ✅ Guardar en disco
    }

    fun updateRoutine(id: Int, newName: String) {
        val index = routines.indexOfFirst { it.id == id }
        if (index != -1) {
            routines[index] = routines[index].copy(name = newName.uppercase())
            persistChanges() // ✅ Guardar en disco
        }
    }
    fun updateRoutineFull(id: Int, newName: String, newExercises: List<Exercise>) {
        val index = routines.indexOfFirst { it.id == id }

        if (index != -1) {
            val updatedRoutine = routines[index].copy(
                name = newName,
                exercises = newExercises
            )
            routines[index] = updatedRoutine

            persistChanges()
        }
    }
    fun addNewExerciseToLibrary(exercise: Exercise) {
        viewModelScope.launch {
            val currentList = exerciseLibrary.value.toMutableList()
            if (!currentList.any { it.name == exercise.name }) { // Evita duplicados
                currentList.add(exercise)
                repository.updateExerciseLibrary(currentList)
            }
        }
    }

    fun addGlobalTag(tag: String) {
        viewModelScope.launch {
            val currentTags = globalTags.value.toMutableList()
            if (!currentTags.contains(tag)) {
                currentTags.add(tag)
                repository.updateGlobalTags(currentTags)
            }
        }
    }

    fun removeExerciseFromLibrary(exercise: Exercise) {
        viewModelScope.launch {
            val currentList = exerciseLibrary.value.toMutableList()
            currentList.removeAll { it.name == exercise.name }
            repository.updateExerciseLibrary(currentList)
        }
    }

    fun removeGlobalTags(tagsToRemove: List<String>) {
        viewModelScope.launch {
            val currentTags = globalTags.value.toMutableList()
            currentTags.removeAll(tagsToRemove)
            repository.updateGlobalTags(currentTags)
        }
    }
    fun setTempExercises(exercises: List<Exercise>?) {
        tempExercises.clear()
        exercises?.let { tempExercises.addAll(it) }
    }

    fun addExerciseToTemp(exercise: Exercise) {
        tempExercises.add(exercise)
    }

    fun clearTempExercises() {
        tempExercises.clear()
    }
}