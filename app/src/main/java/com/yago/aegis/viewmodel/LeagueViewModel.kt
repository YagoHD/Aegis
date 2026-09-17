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
import com.yago.aegis.data.league.LeagueMedal
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

    /** GROUP = liga real de 30 con ascenso/descenso (Functions desplegadas). GLOBAL = tabla mundial (Fase 1). */
    enum class LeagueMode { GROUP, GLOBAL }

    data class LeagueState(
        val loading: Boolean = false,
        val hasUsername: Boolean = true,
        val mode: LeagueMode = LeagueMode.GLOBAL,
        val seasonId: String = "",
        val myUid: String = "",
        val myPoints: Long = 0,
        val myTier: RankTier = RankTier.BRONCE,
        val mySessions: Int = 0,
        val nextThreshold: Long? = null,
        val progress: Float = 0f,
        // Solo modo GROUP:
        val groupId: String = "",
        val myPosition: Int = 0,       // 1-based; 0 = desconocido
        val promoteCount: Int = 0,     // los N primeros del grupo ascienden
        val relegateCount: Int = 0,    // los N últimos descienden
        val medals: List<LeagueMedal> = emptyList(),
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
            val provisionalTier = LeagueSystem.leagueFor(score.points)
            val myEntry = LeagueEntry(
                uid = myUid,
                seasonId = seasonId,
                username = myUsername,
                avatar = avatarB64,
                points = score.points,
                sessions = score.sessions,
                relativeWork = score.relativeWork,
                tier = provisionalTier.name,
                updatedAt = now
            )
            runCatching { league.uploadEntry(myEntry) }

            val medals = runCatching { league.getMedals(myUid, 6) }.getOrDefault(emptyList())
            // ¿El servidor (Fase 2) me asignó grupo? → modo GRUPO. Si no, tabla mundial (Fase 1).
            val myMember = runCatching { league.getEntry(myUid) }.getOrNull()

            if (myMember != null && myMember.groupId.isNotBlank() && myMember.league.isNotBlank()) {
                val serverLeague = tierOf(myMember.league)
                val gid = myMember.groupId
                val gseason = myMember.seasonId.ifBlank { seasonId }
                val members = runCatching { league.getGroupMembers(gseason, gid) }.getOrDefault(emptyList())
                // Mi fila fresca (el espejo del grupo lo escribe el trigger y puede ir un pelín retrasado).
                val myGroupEntry = myEntry.copy(league = myMember.league, groupId = gid, tier = myMember.league, seasonId = gseason)
                val board = (members.filter { it.uid != myUid } + myGroupEntry)
                    .sortedWith(compareByDescending<LeagueEntry> { it.points }.thenBy { it.username.lowercase() })
                    .take(LeagueSystem.GROUP_SIZE)
                _state.value = LeagueState(
                    loading = false, hasUsername = true, mode = LeagueMode.GROUP,
                    seasonId = gseason, myUid = myUid,
                    myPoints = score.points, myTier = serverLeague, mySessions = score.sessions,
                    groupId = gid, myPosition = board.indexOfFirst { it.uid == myUid } + 1,
                    promoteCount = LeagueSystem.promoteCount(board.size, serverLeague),
                    relegateCount = LeagueSystem.relegateCount(board.size, serverLeague),
                    medals = medals, board = board, friendUids = friendUids
                )
            } else {
                // Tabla mundial (Fase 1): top-100 por puntos, filtrado a la temporada en cliente.
                val fetched = runCatching { league.topEntries(150) }.getOrDefault(emptyList())
                    .filter { it.seasonId == seasonId }
                val board = (fetched.filter { it.uid != myUid } + myEntry)
                    .sortedWith(compareByDescending<LeagueEntry> { it.points }.thenBy { it.username.lowercase() })
                    .take(100)
                _state.value = LeagueState(
                    loading = false, hasUsername = true, mode = LeagueMode.GLOBAL,
                    seasonId = seasonId, myUid = myUid,
                    myPoints = score.points, myTier = provisionalTier, mySessions = score.sessions,
                    nextThreshold = LeagueSystem.nextThreshold(score.points),
                    progress = LeagueSystem.progressToNext(score.points),
                    medals = medals, board = board, friendUids = friendUids
                )
            }
        }
    }

    private fun tierOf(name: String): RankTier =
        runCatching { RankTier.valueOf(name) }.getOrDefault(RankTier.BRONCE)

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
