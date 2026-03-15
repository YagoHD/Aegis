package com.yago.aegis.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserRepository(
    private val settingsStore: SettingsStore,
    private val firestore: FirestoreDataSource = FirestoreDataSource()
) {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
    val basePhotoUri = settingsStore.basePhotoUri
    val basePhotoDate = settingsStore.basePhotoDate
    val actualPhotoUri = settingsStore.actualPhotoUri
    val actualPhotoDate = settingsStore.actualPhotoDate
    val disciplineDay: Flow<Int> = settingsStore.disciplineDay
    val onboardingCompleted: Flow<Boolean> = settingsStore.onboardingCompleted
    val routines: Flow<List<Routine>> = settingsStore.routines
    val exerciseLibrary: Flow<List<Exercise>> = settingsStore.exerciseLibrary
    val globalTags: Flow<List<String>> = settingsStore.globalTags
    val workoutHistory: Flow<List<WorkoutSession>> = settingsStore.workoutHistory
    val showVolumeCard = settingsStore.showVolumeCard
    val showDisciplineCard = settingsStore.showDisciplineCard
    val showEvolutionGraph = settingsStore.showEvolutionGraph
    val showAnalyticsList = settingsStore.showAnalyticsList
    val targetDaysPerWeek = settingsStore.targetDaysPerWeek

    fun getAllExercises(): Flow<List<Exercise>> = settingsStore.exerciseLibrary

    suspend fun updateName(name: String) {
        settingsStore.saveName(name)
        syncProfileToCloud()
    }

    suspend fun updateMass(mass: String) {
        settingsStore.saveMass(mass)
        syncProfileToCloud()
    }

    suspend fun updateHeight(h: Double) {
        settingsStore.saveHeight(h)
        syncProfileToCloud()
    }

    suspend fun updateBodyFat(fat: String) {
        settingsStore.saveBodyFat(fat)
        syncProfileToCloud()
    }

    suspend fun updateDisciplineDay(days: Int) {
        settingsStore.saveDisciplineDay(days)
        syncProfileToCloud()
    }

    suspend fun updateMeasures(list: List<BodyMeasure>) {
        settingsStore.saveCustomMeasures(list)
        syncProfileToCloud()
    }

    suspend fun updateAvatar(uri: String) = settingsStore.saveAvatarUri(uri)
    suspend fun updateBasePhoto(uri: String) = settingsStore.saveBasePhotoUri(uri)
    suspend fun updateActualPhoto(uri: String) = settingsStore.saveActualPhotoUri(uri)

    suspend fun updateBasePhotoDate(date: String) {
        settingsStore.saveBasePhotoDate(date)
        syncProfileToCloud()
    }

    suspend fun updateActualPhotoDate(date: String) {
        settingsStore.saveActualPhotoDate(date)
        syncProfileToCloud()
    }

    suspend fun toggleBMI(enabled: Boolean) {
        settingsStore.saveShowBMI(enabled)
        syncSettingsToCloud()
    }

    suspend fun toggleBodyFat(enabled: Boolean) {
        settingsStore.saveShowBodyFat(enabled)
        syncSettingsToCloud()
    }

    suspend fun toggleVisualLog(enabled: Boolean) {
        settingsStore.saveShowVisualLog(enabled)
        syncSettingsToCloud()
    }

    suspend fun toggleGirths(enabled: Boolean) {
        settingsStore.saveShowGirths(enabled)
        syncSettingsToCloud()
    }

    suspend fun updateOnboardingCompleted(completed: Boolean) =
        settingsStore.saveOnboardingCompleted(completed)

    suspend fun updateRoutines(list: List<Routine>) {
        settingsStore.saveRoutines(list)
        syncScope.launch { runCatching { firestore.saveRoutines(list) } }
    }

    suspend fun updateExerciseLibrary(list: List<Exercise>) {
        settingsStore.saveExerciseLibrary(list)
        syncScope.launch { runCatching { firestore.saveExercises(list) } }
    }

    suspend fun updateGlobalTags(tags: List<String>) {
        settingsStore.saveGlobalTags(tags)
        syncScope.launch { runCatching { firestore.saveTags(tags) } }
    }

    suspend fun upsertExercise(exercise: Exercise) {
        val currentList = settingsStore.exerciseLibrary.first().toMutableList()
        val index = currentList.indexOfFirst { it.id == exercise.id }
        if (index != -1) currentList[index] = exercise else currentList.add(exercise)
        settingsStore.saveExerciseLibrary(currentList)
        syncScope.launch { runCatching { firestore.saveExercises(currentList) } }
    }

    suspend fun deleteExercise(exercise: Exercise) {
        val currentList = settingsStore.exerciseLibrary.first().toMutableList()
        currentList.removeAll { it.name.equals(exercise.name, ignoreCase = true) }
        settingsStore.saveExerciseLibrary(currentList)
        syncScope.launch { runCatching { firestore.saveExercises(currentList) } }
    }

    suspend fun saveWorkoutSession(session: WorkoutSession) {
        settingsStore.saveWorkoutSession(session)
        syncScope.launch { runCatching { firestore.appendWorkoutSession(session) } }
    }

    suspend fun updateTargetDays(days: Int) {
        settingsStore.updateTargetDays(days)
        syncSettingsToCloud()
    }

    suspend fun toggleStatSection(keyName: String, isEnabled: Boolean) {
        settingsStore.toggleStatSection(keyName, isEnabled)
        syncSettingsToCloud()
    }

    private fun syncProfileToCloud() {
        syncScope.launch {
            runCatching {
                firestore.saveProfile(
                    name = settingsStore.userName.first(),
                    mass = settingsStore.currentMass.first(),
                    height = settingsStore.height.first(),
                    bodyFat = settingsStore.bodyFat.first(),
                    disciplineDay = settingsStore.disciplineDay.first(),
                    customMeasures = settingsStore.customMeasures.first(),
                    basePhotoDate = settingsStore.basePhotoDate.first(),
                    actualPhotoDate = settingsStore.actualPhotoDate.first()
                )
            }
        }
    }

    private fun syncSettingsToCloud() {
        syncScope.launch {
            runCatching {
                firestore.saveSettings(
                    showBMI = settingsStore.showBMI.first(),
                    showBodyFat = settingsStore.showBodyFat.first(),
                    showVisualLog = settingsStore.showVisualLog.first(),
                    showGirths = settingsStore.showGirths.first(),
                    showVolumeCard = settingsStore.showVolumeCard.first(),
                    showDisciplineCard = settingsStore.showDisciplineCard.first(),
                    showEvolutionGraph = settingsStore.showEvolutionGraph.first(),
                    showAnalyticsList = settingsStore.showAnalyticsList.first(),
                    targetDaysPerWeek = settingsStore.targetDaysPerWeek.first()
                )
            }
        }
    }

    suspend fun syncOnLogin() {
        runCatching {
            if (firestore.hasCloudData()) {
                downloadFromCloud()
            } else {
                uploadToCloud()
            }
        }
    }

    private suspend fun downloadFromCloud() {
        firestore.getProfile()?.let { data ->
            data["name"]?.toString()?.let { settingsStore.saveName(it) }
            data["mass"]?.toString()?.let { settingsStore.saveMass(it) }
            (data["height"] as? Double)?.let { settingsStore.saveHeight(it) }
            data["bodyFat"]?.toString()?.let { settingsStore.saveBodyFat(it) }
            (data["disciplineDay"] as? Long)?.toInt()?.let { settingsStore.saveDisciplineDay(it) }
            data["basePhotoDate"]?.toString()?.takeIf { it.isNotEmpty() }?.let { settingsStore.saveBasePhotoDate(it) }
            data["actualPhotoDate"]?.toString()?.takeIf { it.isNotEmpty() }?.let { settingsStore.saveActualPhotoDate(it) }
            data["customMeasures"]?.toString()?.takeIf { it.isNotEmpty() }?.let { json ->
                val type = object : com.google.gson.reflect.TypeToken<List<BodyMeasure>>() {}.type
                val measures: List<BodyMeasure> = com.google.gson.Gson().fromJson(json, type)
                settingsStore.saveCustomMeasures(measures)
            }
        }
        firestore.getRoutines()?.let { settingsStore.saveRoutines(it) }
        firestore.getExercises()?.let { settingsStore.saveExerciseLibrary(it) }
        firestore.getWorkoutHistory()?.let { cloudHistory ->
            val localHistory = settingsStore.workoutHistory.first()
            val localIds = localHistory.map { it.id }.toSet()
            val cloudIds = cloudHistory.map { it.id }.toSet()
            val localOnly = localHistory.filter { it.id !in cloudIds }
            val merged = (cloudHistory + localOnly).sortedBy { it.date }
            merged.filter { it.id !in localIds }.forEach { settingsStore.saveWorkoutSession(it) }
        }
        firestore.getTags()?.let { settingsStore.saveGlobalTags(it) }
        firestore.getSettings()?.let { data ->
            (data["showBMI"] as? Boolean)?.let { settingsStore.saveShowBMI(it) }
            (data["showBodyFat"] as? Boolean)?.let { settingsStore.saveShowBodyFat(it) }
            (data["showVisualLog"] as? Boolean)?.let { settingsStore.saveShowVisualLog(it) }
            (data["showGirths"] as? Boolean)?.let { settingsStore.saveShowGirths(it) }
            (data["targetDaysPerWeek"] as? Long)?.toInt()?.let { settingsStore.updateTargetDays(it) }
        }
    }

    private suspend fun uploadToCloud() {
        firestore.saveProfile(
            name = settingsStore.userName.first(),
            mass = settingsStore.currentMass.first(),
            height = settingsStore.height.first(),
            bodyFat = settingsStore.bodyFat.first(),
            disciplineDay = settingsStore.disciplineDay.first(),
            customMeasures = settingsStore.customMeasures.first(),
            basePhotoDate = settingsStore.basePhotoDate.first(),
            actualPhotoDate = settingsStore.actualPhotoDate.first()
        )
        firestore.saveRoutines(settingsStore.routines.first())
        firestore.saveExercises(settingsStore.exerciseLibrary.first())
        firestore.saveWorkoutHistory(settingsStore.workoutHistory.first())
        firestore.saveTags(settingsStore.globalTags.first())
        firestore.saveSettings(
            showBMI = settingsStore.showBMI.first(),
            showBodyFat = settingsStore.showBodyFat.first(),
            showVisualLog = settingsStore.showVisualLog.first(),
            showGirths = settingsStore.showGirths.first(),
            showVolumeCard = settingsStore.showVolumeCard.first(),
            showDisciplineCard = settingsStore.showDisciplineCard.first(),
            showEvolutionGraph = settingsStore.showEvolutionGraph.first(),
            showAnalyticsList = settingsStore.showAnalyticsList.first(),
            targetDaysPerWeek = settingsStore.targetDaysPerWeek.first()
        )
    }
}
