package com.yago.aegis.data

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
    val actualPhotoDate: String? = null
)