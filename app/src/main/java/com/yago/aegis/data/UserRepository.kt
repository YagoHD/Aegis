package com.yago.aegis.data

import kotlinx.coroutines.flow.Flow

class UserRepository(private val settingsStore: SettingsStore) {

    val userName = settingsStore.userName
    val showBMI = settingsStore.showBMI
    val showBodyFat = settingsStore.showBodyFat
    val showVisualLog = settingsStore.showVisualLog
    val showGirths = settingsStore.showGirths
    val avatarUri = settingsStore.avatarUri
    val currentMass = settingsStore.currentMass
    val height = settingsStore.height
    val bodyFat = settingsStore.bodyFat
    val customMeasures = settingsStore.customMeasures
    val routines: Flow<List<Routine>> = settingsStore.routines
    val basePhotoUri = settingsStore.basePhotoUri
    val basePhotoDate = settingsStore.basePhotoDate
    val actualPhotoUri = settingsStore.actualPhotoUri
    val actualPhotoDate = settingsStore.actualPhotoDate
    val exerciseLibrary: Flow<List<Exercise>> = settingsStore.exerciseLibrary
    val globalTags: Flow<List<String>> = settingsStore.globalTags

    suspend fun updateName(name: String) = settingsStore.saveName(name)
    suspend fun toggleBMI(enabled: Boolean) = settingsStore.saveShowBMI(enabled)
    suspend fun toggleBodyFat(enabled: Boolean) = settingsStore.saveShowBodyFat(enabled)
    suspend fun toggleVisualLog(enabled: Boolean) = settingsStore.saveShowVisualLog(enabled)
    suspend fun toggleGirths(enabled: Boolean) = settingsStore.saveShowGirths(enabled)
    suspend fun updateAvatar(uri: String) = settingsStore.saveAvatarUri(uri)
    suspend fun updateMass(mass: String) = settingsStore.saveMass(mass)
    suspend fun updateHeight(h: Double) = settingsStore.saveHeight(h)
    suspend fun updateBodyFat(fat: String) = settingsStore.saveBodyFat(fat)
    suspend fun updateMeasures(list: List<BodyMeasure>) = settingsStore.saveCustomMeasures(list)
    suspend fun updateRoutines(list: List<Routine>) = settingsStore.saveRoutines(list)
    suspend fun updateExerciseLibrary(list: List<Exercise>) = settingsStore.saveExerciseLibrary(list)
    suspend fun updateGlobalTags(tags: List<String>) = settingsStore.saveGlobalTags(tags)

    // Fotos y sus fechas (Escritura)
    suspend fun updateBasePhoto(uri: String) = settingsStore.saveBasePhotoUri(uri)
    suspend fun updateBasePhotoDate(date: String) = settingsStore.saveBasePhotoDate(date)

    suspend fun updateActualPhoto(uri: String) = settingsStore.saveActualPhotoUri(uri)
    suspend fun updateActualPhotoDate(date: String) = settingsStore.saveActualPhotoDate(date)
}