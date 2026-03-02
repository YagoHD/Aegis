package com.yago.aegis.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.Routine
import com.yago.aegis.data.UserRepository
import kotlinx.coroutines.launch

class RoutinesViewModel(private val repository: UserRepository) : ViewModel() {

    // ✅ La lista empieza vacía y se llena desde el disco
    var routines = mutableStateListOf<Routine>()
        private set

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
        routines.add(Routine(newId, name.uppercase(), 0, 0))
        persistChanges() // ✅ Guardar en disco
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
}