package com.yago.aegis.data


class UserRepository(private val settingsStore: SettingsStore) {

    val userName = settingsStore.userName
    val showBMI = settingsStore.showBMI
    val showBodyFat = settingsStore.showBodyFat
    val showVisualLog = settingsStore.showVisualLog
    val showGirths = settingsStore.showGirths
    val avatarUri = settingsStore.avatarUri

    suspend fun updateName(name: String) = settingsStore.saveName(name)
    suspend fun toggleBMI(enabled: Boolean) = settingsStore.saveShowBMI(enabled)
    suspend fun toggleBodyFat(enabled: Boolean) = settingsStore.saveShowBodyFat(enabled)
    suspend fun toggleVisualLog(enabled: Boolean) = settingsStore.saveShowVisualLog(enabled)
    suspend fun toggleGirths(enabled: Boolean) = settingsStore.saveShowGirths(enabled)
    suspend fun updateAvatar(uri: String) = settingsStore.saveAvatarUri(uri)
}