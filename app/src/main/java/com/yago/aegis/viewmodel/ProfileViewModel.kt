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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ProfileViewModel(val repository: UserRepository) : ViewModel() {

    // 1. Estado del Usuario (Sincronizado con DataStore)
    var user by mutableStateOf(
        UserProfile(
            name = "Cargando...",
            disciplineDay = 0,
            currentMass = "0.0",
            height = 170,
            bodyFat = "0.0",
            goal = "BULK"
        )
    )
        private set

    // 2. Estados de la Interfaz (Sincronizados con DataStore)
    var showBMI by mutableStateOf(true)
        private set
    var showBodyFat by mutableStateOf(true)
        private set
    var showVisualLog by mutableStateOf(true)
        private set
    var showGirths by mutableStateOf(true)
        private set

    // 3. Medidas personalizadas (Sincronizadas con DataStore vía JSON)
    var customMeasures by mutableStateOf<List<BodyMeasure>>(emptyList())
        private set

    init {
        // --- ESCUCHAR CAMBIOS DEL REPOSITORIO (LECTURA EN TIEMPO REAL) ---

        viewModelScope.launch {
            repository.userName.collect { user = user.copy(name = it) }
        }
        viewModelScope.launch {
            repository.currentMass.collect { user = user.copy(currentMass = it) }
        }
        viewModelScope.launch {
            repository.height.collect { user = user.copy(height = it.toInt()) }
        }
        viewModelScope.launch {
            repository.bodyFat.collect { user = user.copy(bodyFat = it) }
        }
        viewModelScope.launch {
            repository.avatarUri.collect { uri -> user = user.copy(profilePhotoUri = uri) }
        }
        viewModelScope.launch {
            repository.basePhotoUri.collect { uri ->
                user = user.copy(basePhotoUri = uri)
            }
        }
        viewModelScope.launch {
            repository.actualPhotoUri.collect { uri ->
                user = user.copy(actualPhotoUri = uri)
            }
        }

        viewModelScope.launch { repository.showBMI.collect { showBMI = it } }
        viewModelScope.launch { repository.showBodyFat.collect { showBodyFat = it } }
        viewModelScope.launch { repository.showVisualLog.collect { showVisualLog = it } }
        viewModelScope.launch { repository.showGirths.collect { showGirths = it } }

        viewModelScope.launch {
            repository.customMeasures.collect { customMeasures = it }
        }

        viewModelScope.launch {
            repository.basePhotoDate.collect { date ->
                user = user.copy(basePhotoDate = date)
            }
        }

        viewModelScope.launch {
            repository.actualPhotoDate.collect { date ->
                user = user.copy(actualPhotoDate = date)
            }
        }
        viewModelScope.launch {
            repository.avatarUri.collect { uri ->
                user = user.copy(profilePhotoUri = uri)
            }
        }
    }

    // --- FUNCIONES DE ACTUALIZACIÓN (ESCRITURA EN DISCO) ---

    fun updateName(newName: String) {
        val validated = if (newName.isBlank()) "Guerrero Aegis" else newName
        viewModelScope.launch { repository.updateName(validated) }
    }

    fun updateMass(newMass: String) {
        viewModelScope.launch { repository.updateMass(newMass) }
    }

    fun updateBodyFat(newFat: String) {
        viewModelScope.launch { repository.updateBodyFat(newFat) }
    }

    fun updateAvatar(uri: String) {
        user = user.copy(profilePhotoUri = uri)

        viewModelScope.launch {
            repository.updateAvatar(uri)
        }
    }

    // Toggles persistentes
    fun toggleBMI(enabled: Boolean) = viewModelScope.launch { repository.toggleBMI(enabled) }
    fun toggleBodyFat(enabled: Boolean) = viewModelScope.launch { repository.toggleBodyFat(enabled) }
    fun toggleVisualLog(enabled: Boolean) = viewModelScope.launch { repository.toggleVisualLog(enabled) }
    fun toggleGirths(enabled: Boolean) = viewModelScope.launch { repository.toggleGirths(enabled) }

    // --- GESTIÓN DE MEDIDAS (PERSISTENCIA DE LISTAS) ---

    fun updateMeasureValue(id: String, newValue: String) {
        val updatedList = customMeasures.map {
            if (it.id == id) it.copy(value = newValue) else it
        }
        viewModelScope.launch { repository.updateMeasures(updatedList) }
    }

    fun addMeasure(name: String) {
        val newId = name.uppercase().replace(" ", "_")
        val newList = customMeasures + BodyMeasure(newId, name, "0.0")
        viewModelScope.launch { repository.updateMeasures(newList) }
    }

    fun removeMeasure(id: String) {
        val newList = customMeasures.filter { it.id != id }
        viewModelScope.launch { repository.updateMeasures(newList) }
    }

    fun calcularBMI(): Double {
        val mass = user.currentMass.toDoubleOrNull() ?: 0.0
        val heightInMeters = user.height / 100.0

        return if (mass > 0 && heightInMeters > 0) {
            mass / (heightInMeters * heightInMeters)
        } else {
            0.0
        }
    }

    fun updatePhoto(uri: String, type: PhotoType) {
        // 1. Generamos la fecha del momento exacto del cambio
        val formatter = DateTimeFormatter.ofPattern("dd MMMM", Locale("es", "ES"))
        val todayDate = LocalDate.now().format(formatter).uppercase()

        // 2. Actualizamos la UI inmediatamente
        user = when (type) {
            PhotoType.BASE -> user.copy(basePhotoUri = uri, basePhotoDate = todayDate)
            PhotoType.ACTUAL -> user.copy(actualPhotoUri = uri, actualPhotoDate = todayDate)
        }

        // 3. Guardamos en el DataStore (Permanencia)
        viewModelScope.launch {
            when (type) {
                PhotoType.BASE -> {
                    repository.updateBasePhoto(uri)
                    repository.updateBasePhotoDate(todayDate)
                }
                PhotoType.ACTUAL -> {
                    repository.updateActualPhoto(uri)
                    repository.updateActualPhotoDate(todayDate)
                }
            }
        }
    }

    fun updateHeight(newHeight: Int) {
        viewModelScope.launch {
            repository.updateHeight(newHeight.toDouble())
        }
    }

}