package com.yago.aegis.data

data class WorkoutSummary(
    val routineName: String,
    val durationMs: Long,
    val totalVolume: Double,
    val exerciseCount: Int,
    val exercises: List<ExerciseSummary>,
    // Volumen de la sesión anterior (se calcula al mostrar la pantalla)
    val previousVolume: Double = 0.0
)

data class ExerciseSummary(
    val name: String,
    val sets: Int,
    val avgWeight: Double,
    val maxWeight: Double,
    val isBodyweight: Boolean,
    val isNewPR: Boolean
)
