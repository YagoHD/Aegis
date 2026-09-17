package com.yago.aegis.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.RankTier
import com.yago.aegis.data.UserRepository
import com.yago.aegis.data.league.LeagueDataSource
import com.yago.aegis.data.league.LeagueEntry
import com.yago.aegis.data.league.LeagueSystem
import com.yago.aegis.util.AvatarImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Liga mensual global (Fase 1). Calcula mis puntos de esfuerzo del mes desde el historial,
 * sube mi entrada a la colección global y baja la tabla mundial (top-100) para pintarla.
 *
 * Reutiliza la identidad social: se necesita @usuario (misma puerta que el ranking). Los uids de
 * mis amigos los aporta la pantalla (ya los tiene el SocialViewModel) para resaltarlos en la tabla.
 */
class LeagueViewModel(
    private val league: LeagueDataSource,
    private val repo: UserRepository,
    private val appContext: Context
) : ViewModel() {

    private val myUid: String = league.currentUid() ?: ""

    data class LeagueState(
        val loading: Boolean = false,
        val hasUsername: Boolean = true,
        val seasonId: String = "",
        val myUid: String = "",
        val myPoints: Long = 0,
        val myTier: RankTier = RankTier.BRONCE,
        val mySessions: Int = 0,
        val nextThreshold: Long? = null,
        val progress: Float = 0f,
        val board: List<LeagueEntry> = emptyList(),
        val friendUids: Set<String> = emptySet()
    )

    private val _state = MutableStateFlow(LeagueState(myUid = myUid))
    val state: StateFlow<LeagueState> = _state.asStateFlow()

    /**
     * Recalcula mis puntos del mes, sube mi entrada y refresca la tabla mundial.
     * [friendUids] = uids de mis amigos aceptados (para resaltarlos). [myUsername] = null → sin liga.
     */
    fun load(friendUids: Set<String>, myUsername: String?) {
        viewModelScope.launch {
            if (myUsername == null) {
                _state.value = LeagueState(loading = false, hasUsername = false, myUid = myUid)
                return@launch
            }
            _state.value = _state.value.copy(loading = true, hasUsername = true)

            val now = System.currentTimeMillis()
            val seasonId = LeagueSystem.seasonIdFor(now)
            val (score, avatarB64) = withContext(Dispatchers.Default) {
                val history = repo.workoutHistory.first()
                val bw = repo.currentMass.first().replace(",", ".").toDoubleOrNull() ?: 0.0
                val streak = repo.computeCurrentStreak()
                val sc = LeagueSystem.computeSeason(history, bw, streak, now)
                val av = repo.avatarUri.first()?.let { AvatarImage.encode(appContext, Uri.parse(it)) } ?: ""
                sc to av
            }
            val myTier = LeagueSystem.leagueFor(score.points)
            val myEntry = LeagueEntry(
                uid = myUid,
                seasonId = seasonId,
                username = myUsername,
                avatar = avatarB64,
                points = score.points,
                sessions = score.sessions,
                relativeWork = score.relativeWork,
                tier = myTier.name,
                updatedAt = now
            )
            runCatching { league.uploadEntry(myEntry) }

            // Baja el top mundial y lo filtra a la temporada actual (sin índice compuesto en Fase 1).
            val fetched = runCatching { league.topEntries(150) }.getOrDefault(emptyList())
                .filter { it.seasonId == seasonId }
            // Sustituye mi doc remoto (puede venir cacheado/viejo) por el recién calculado y
            // garantiza que yo salgo en la tabla aunque quede fuera del top.
            val board = (fetched.filter { it.uid != myUid } + myEntry)
                .sortedWith(compareByDescending<LeagueEntry> { it.points }.thenBy { it.username.lowercase() })
                .take(100)

            _state.value = LeagueState(
                loading = false,
                hasUsername = true,
                seasonId = seasonId,
                myUid = myUid,
                myPoints = score.points,
                myTier = myTier,
                mySessions = score.sessions,
                nextThreshold = LeagueSystem.nextThreshold(score.points),
                progress = LeagueSystem.progressToNext(score.points),
                board = board,
                friendUids = friendUids
            )
        }
    }

    class Factory(
        private val league: LeagueDataSource,
        private val repo: UserRepository,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LeagueViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LeagueViewModel(league, repo, appContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
