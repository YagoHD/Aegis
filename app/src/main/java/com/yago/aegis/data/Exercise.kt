package com.yago.aegis.data

data class Exercise(
    val name: String,
    val type: String,        // Ejemplo: "COMPOUND" o "MACHINE"
    val muscleGroup: String, // Ejemplo: "LEGS"
    val iconName: String = "dumbbell"
)