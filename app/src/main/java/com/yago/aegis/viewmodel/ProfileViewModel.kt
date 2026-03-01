package com.yago.aegis.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.BodyMeasure
import com.yago.aegis.data.PhotoType
import com.yago.aegis.data.UserProfile
import com.yago.aegis.data.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    // 1. Estado del Usuario (Carga desde el repositorio)
    var user by mutableStateOf(
        UserProfile(
            name = "Cargando...",
            disciplineDay = 0,
            currentMass = "0.0",
            height = 1.0,
            bodyFat = "0.0",
            goal = "BULK"
        )
    )
        private set

    // 2. Estados de la Interfaz (Persistentes)
    var showBMI by mutableStateOf(true)
        private set
    var showBodyFat by mutableStateOf(true)
        private set
    var showVisualLog by mutableStateOf(true)
        private set
    var showGirths by mutableStateOf(true)
        private set

    // 3. Medidas personalizadas (Memoria temporal)
    var customMeasures by mutableStateOf<List<BodyMeasure>>(listOf(
        BodyMeasure("CHEST", "Pecho", "112.5"),
        BodyMeasure("WAIST", "Cintura", "78.2"),
        BodyMeasure("ARMS", "Brazo", "42.5")
    ))

    init {
        // --- ESCUCHAR CAMBIOS DEL REPOSITORIO (LECTURA) ---

        viewModelScope.launch {
            repository.userName.collect { nuevoNombre ->
                user = user.copy(name = nuevoNombre)
            }
        }

        viewModelScope.launch {
            repository.avatarUri.collect { uri ->
                user = user.copy(profilePhotoUri = uri)
            }
        }

        viewModelScope.launch {
            repository.showBMI.collect { showBMI = it }
        }

        viewModelScope.launch {
            repository.showBodyFat.collect { showBodyFat = it }
        }

        viewModelScope.launch {
            repository.showVisualLog.collect { showVisualLog = it }
        }

        viewModelScope.launch {
            repository.showGirths.collect { showGirths = it }
        }
    }

    // --- FUNCIONES DE ACTUALIZACIÓN (ESCRITURA EN DISCO) ---

    fun updateName(newName: String) {
        viewModelScope.launch {
            repository.updateName(newName)
        }
    }

    fun updateAvatar(uri: String) {
        viewModelScope.launch {
            repository.updateAvatar(uri)
        }
    }

    fun toggleBMI(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleBMI(enabled)
        }
    }

    fun toggleBodyFat(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleBodyFat(enabled)
        }
    }

    fun toggleVisualLog(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleVisualLog(enabled)
        }
    }

    fun toggleGirths(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleGirths(enabled)
        }
    }

    // --- FUNCIONES DE LÓGICA Y CÁLCULOS ---

    fun calcularBMI(): Double {
        val mass = user.currentMass.toDoubleOrNull() ?: 0.0
        return if (mass > 0) mass / (user.height * user.height) else 0.0
    }

    fun updateMass(newMass: String) {
        user = user.copy(currentMass = newMass)
    }

    fun updateBodyFat(newFat: String) {
        user = user.copy(bodyFat = newFat)
    }

    fun updatePhoto(uri: String, type: PhotoType) {
        user = when (type) {
            PhotoType.BASE -> user.copy(basePhotoUri = uri)
            PhotoType.ACTUAL -> user.copy(actualPhotoUri = uri)
        }
    }

    // --- GESTIÓN DE MEDIDAS (TEMPORAL) ---

    fun updateMeasureValue(id: String, newValue: String) {
        customMeasures = customMeasures.map {
            if (it.id == id) it.copy(value = newValue) else it
        }
    }

    fun addMeasure(name: String) {
        val newId = name.uppercase().replace(" ", "_")
        customMeasures = customMeasures + BodyMeasure(newId, name, "0.0")
    }

    fun removeMeasure(id: String) {
        customMeasures = customMeasures.filter { it.id != id }
    }
}