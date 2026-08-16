package com.yago.aegis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.RankEngine
import com.yago.aegis.data.UserRepository
import com.yago.aegis.data.social.FriendBuckets
import com.yago.aegis.data.social.PublicProfile
import com.yago.aegis.data.social.SocialDataSource
import com.yago.aegis.data.social.UsernameRules
import com.yago.aegis.data.social.bucketsFor
import com.yago.aegis.data.social.toRankSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Resultado de una acción social. La pantalla lo mapea a strings (i18n en los 6 idiomas). */
enum class SocialFeedback {
    USERNAME_CLAIMED, USERNAME_TAKEN, USERNAME_INVALID,
    REQUEST_SENT, USER_NOT_FOUND, CANNOT_ADD_SELF, ALREADY_LINKED, ERROR
}

/** Estado y acciones de Amigos: reclamar @usuario, añadir por @usuario, aceptar/rechazar. */
class SocialViewModel(
    private val social: SocialDataSource,
    private val repo: UserRepository
) : ViewModel() {

    private val myUid: String = social.currentUid() ?: ""

    val myUsername: StateFlow<String?> =
        repo.username.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val buckets: StateFlow<FriendBuckets> =
        social.observeFriendships()
            .map { it.bucketsFor(myUid) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FriendBuckets())

    private val _feedback = MutableStateFlow<SocialFeedback?>(null)
    val feedback: StateFlow<SocialFeedback?> = _feedback.asStateFlow()
    fun clearFeedback() { _feedback.value = null }

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    fun claimUsername(input: String) {
        val u = UsernameRules.normalize(input)
        if (!UsernameRules.isValid(u)) { _feedback.value = SocialFeedback.USERNAME_INVALID; return }
        viewModelScope.launch {
            _busy.value = true
            _feedback.value = if (!social.isUsernameAvailable(u)) {
                SocialFeedback.USERNAME_TAKEN
            } else {
                social.claimUsername(u).fold(
                    onSuccess = {
                        repo.saveUsername(u)
                        uploadMyProfileInternal(u)
                        SocialFeedback.USERNAME_CLAIMED
                    },
                    onFailure = { SocialFeedback.USERNAME_TAKEN }   // set() falló = ya cogido / error
                )
            }
            _busy.value = false
        }
    }

    fun addFriend(input: String) {
        val u = UsernameRules.normalize(input)
        if (!UsernameRules.isValid(u)) { _feedback.value = SocialFeedback.USER_NOT_FOUND; return }
        val mine = myUsername.value ?: ""
        viewModelScope.launch {
            _busy.value = true
            val targetUid = social.findUidByUsername(u)
            _feedback.value = when {
                targetUid == null -> SocialFeedback.USER_NOT_FOUND
                targetUid == myUid -> SocialFeedback.CANNOT_ADD_SELF
                isAlreadyLinked(targetUid) -> SocialFeedback.ALREADY_LINKED
                else -> social.sendFriendRequest(targetUid, u, mine).fold(
                    onSuccess = { SocialFeedback.REQUEST_SENT },
                    onFailure = { SocialFeedback.ERROR }
                )
            }
            _busy.value = false
        }
    }

    private fun isAlreadyLinked(uid: String): Boolean {
        val b = buckets.value
        return (b.friends + b.incoming + b.outgoing).any { it.uid == uid }
    }

    fun accept(uid: String) = mutate { social.acceptFriendship(uid) }
    fun reject(uid: String) = mutate { social.removeFriendship(uid) }
    fun remove(uid: String) = mutate { social.removeFriendship(uid) }

    private fun mutate(block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _busy.value = true
            if (block().isFailure) _feedback.value = SocialFeedback.ERROR
            _busy.value = false
        }
    }

    /** Sube mi perfil público (rangos calculados). Llamar al cambiar mis marcas o mi @usuario. */
    fun uploadMyProfile() {
        viewModelScope.launch { myUsername.value?.let { uploadMyProfileInternal(it) } }
    }

    private suspend fun uploadMyProfileInternal(username: String) {
        val summary = withContext(Dispatchers.Default) {
            val history = repo.workoutHistory.first()
            val library = repo.exerciseLibrary.first()
            val bodyweight = repo.currentMass.first().toDoubleOrNull() ?: 0.0
            val sex = repo.sex.first()
            RankEngine.compute(history, library, bodyweight, sex).toRankSummary()
        }
        social.uploadPublicProfile(
            PublicProfile(
                uid = myUid,
                username = username,
                displayName = repo.userName.first(),
                overallTier = summary.overall,
                groupTiers = summary.byGroup
            )
        )
    }

    class Factory(
        private val social: SocialDataSource,
        private val repo: UserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SocialViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SocialViewModel(social, repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
