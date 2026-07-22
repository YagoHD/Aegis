package com.yago.aegis.data

/** Sexo del usuario — necesario para estándares de fuerza justos en el Panteón. */
enum class Sex { MALE, FEMALE, UNSPECIFIED }

data class UserProfile(
    val name: String,
    val disciplineDay: Int,
    val currentMass: String,
    val height: Int,
    val bodyFat: String,
    val goal: String,
    val basePhotoUri: String? = null,
    val actualPhotoUri: String? = null,
    val profilePhotoUri: String? = null,
    val basePhotoDate: String? = null,
    val actualPhotoDate: String? = null,
    val currentStreak: Int = 0,
    val sex: String = Sex.UNSPECIFIED.name
)