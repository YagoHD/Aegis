package com.yago.aegis.viewmodel

import androidx.compose.runtime.mutableStateOf
import com.yago.aegis.data.UserProfile
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.yago.aegis.data.BodyMeasure
import com.yago.aegis.data.PhotoType

class ProfileViewModel : ViewModel() {
    var user by mutableStateOf(
        UserProfile(
            name = "Yago Ramos",
            disciplineDay = 0,
            currentMass = "98.0", // Mejor empezar con un valor para que el BMI no sea 0
            height = 1.84,
            bodyFat = "11.4",
            goal = "BULK"
            // Borramos chest, waist y arms de aquí
        )
    )

    // 2. Esta es tu fuente de verdad para todas las medidas corporales
    var customMeasures by mutableStateOf<List<BodyMeasure>>(listOf(
        BodyMeasure("CHEST", "Pecho", "112.5"),
        BodyMeasure("WAIST", "Cintura", "78.2"),
        BodyMeasure("ARMS", "Brazo", "42.5")
    ))

    // Función para actualizar el valor de una medida específica
    fun updateMeasureValue(id: String, newValue: String) {
        customMeasures = customMeasures.map {
            if (it.id == id) it.copy(value = newValue) else it
        }
    }

    // Función "mágica" para añadir nuevas medidas (cuádriceps, cuello, etc.)
    fun addMeasure(name: String) {
        val newId = name.uppercase().replace(" ", "_")
        customMeasures = customMeasures + BodyMeasure(newId, name, "0.0")
    }

    // Función para eliminar una medida si el usuario ya no la quiere
    fun removeMeasure(id: String) {
        customMeasures = customMeasures.filter { it.id != id }
    }

    fun calcularBMI(): Double {
        // toDoubleOrNull() evita que la app explote si el campo está vacío
        val mass = user.currentMass.toDoubleOrNull() ?: 0.0
        return if (mass > 0) mass / (user.height * user.height) else 0.0
    }

    fun daysToMilestone(targetDay: Int): Int {
        return targetDay - user.disciplineDay
    }

    fun getWeightStatus() : String{
        return if (user.goal == "BULK") {
            "Bulk Mode"
        } else "Cutting Mode"
    }

    fun incrementDisciplineDay() {
        user = user.copy(disciplineDay = user.disciplineDay + 1)
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

    var showBodyFat by mutableStateOf(true)
    var showBMI by mutableStateOf(true)
    var showVisualLog by mutableStateOf(true)
    var showGirths by mutableStateOf(true)

    // Funciones para cambiar los estados
    fun toggleBodyFat(enabled: Boolean) { showBodyFat = enabled }
    fun toggleBMI(enabled: Boolean) { showBMI = enabled }
    fun toggleVisualLog(enabled: Boolean) { showVisualLog = enabled }
    fun toggleGirths(enabled: Boolean) { showGirths = enabled }
}
