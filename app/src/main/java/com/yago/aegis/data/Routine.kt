package com.yago.aegis.data

data class Routine(
    val id: Int,
    val name: String,
    val exercises: List<Exercise> = emptyList(),
    val iconRes: Int
)