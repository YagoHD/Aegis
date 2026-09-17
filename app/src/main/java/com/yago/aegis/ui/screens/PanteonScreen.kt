package com.yago.aegis.ui.screens
import com.yago.aegis.ui.theme.Spacing
import com.yago.aegis.ui.theme.Radius

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import com.yago.aegis.R
import com.yago.aegis.data.Fatigue
import com.yago.aegis.data.GroupRank
import com.yago.aegis.data.MuscleGroup
import com.yago.aegis.data.PanteonResult
import com.yago.aegis.data.Rank
import com.yago.aegis.data.RankTier
import com.yago.aegis.data.SubgroupRank
import com.yago.aegis.data.divisionFromProgress
import com.yago.aegis.data.social.PublicProfile
import com.yago.aegis.util.AvatarImage
import com.yago.aegis.ui.components.AegisAvatar
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.components.RankMedal
import com.yago.aegis.ui.theme.AegisGoldAccent
import com.yago.aegis.data.league.LeagueEntry
import com.yago.aegis.viewmodel.LeagueViewModel
import com.yago.aegis.viewmodel.PanteonViewModel
import com.yago.aegis.viewmodel.SocialViewModel

private enum class PanteonTab { MINE, FRIENDS, LEAGUE }

@Composable
fun PanteonScreen(
    viewModel: PanteonViewModel,
    socialViewModel: SocialViewModel,
    leagueViewModel: LeagueViewModel,
    onOpenFriends: () -> Unit = {}
) {
    val result by viewModel.result.collectAsState()
    val hasRanks = result.groups.any { it.tier != RankTier.SIN_RANGO }
    var tab by remember { mutableStateOf(PanteonTab.MINE) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AegisTopBar(
                title = stringResource(R.string.nav_panteon).uppercase(),
                subtitle = stringResource(R.string.panteon_subtitle),
                actions = {
                    IconButton(onClick = onOpenFriends) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = stringResource(R.string.content_desc_friends),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item { Spacer(modifier = Modifier.height(Spacing.xs)) }
            item { PanteonTabs(tab) { tab = it } }

            when (tab) {
                PanteonTab.MINE -> {
                    item { BodyMapPlaceholder() }
                    if (!hasRanks) {
                        item { EmptyRanks() }
                    } else {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                SummaryCard(
                                    label = stringResource(R.string.highest_rank_label),
                                    group = result.strongest,
                                    modifier = Modifier.weight(1f)
                                )
                                SummaryCard(
                                    label = stringResource(R.string.to_improve_label),
                                    group = result.weakest,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    items(result.groups, key = { it.group.name }) { g -> GroupRow(g) }
                }
                PanteonTab.FRIENDS -> {
                    item {
                        FriendsRankingSection(
                            socialViewModel = socialViewModel,
                            myResult = result,
                            onManageFriends = onOpenFriends
                        )
                    }
                }
                PanteonTab.LEAGUE -> {
                    item {
                        LeagueSection(
                            leagueViewModel = leagueViewModel,
                            socialViewModel = socialViewModel,
                            onManageFriends = onOpenFriends
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
private fun PanteonTabs(selected: PanteonTab, onSelect: (PanteonTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TabItem(stringResource(R.string.panteon_my_ranks), active = selected == PanteonTab.MINE, locked = false) { onSelect(PanteonTab.MINE) }
        TabItem(stringResource(R.string.panteon_friends), active = selected == PanteonTab.FRIENDS, locked = false) { onSelect(PanteonTab.FRIENDS) }
        TabItem(stringResource(R.string.panteon_league), active = selected == PanteonTab.LEAGUE, locked = false) { onSelect(PanteonTab.LEAGUE) }
    }
}

@Composable
private fun TabItem(text: String, active: Boolean, locked: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (locked) Modifier else Modifier.clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                color = if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            if (locked) {
                Spacer(modifier = Modifier.width(Spacing.xs))
                Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), modifier = Modifier.size(11.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(28.dp)
                .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
        )
    }
}

@Composable
private fun BodyMapPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(Radius.xl))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(Radius.xl)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.MilitaryTech, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = stringResource(R.string.body_map_soon),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
private fun EmptyRanks() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.panteon_empty),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun SummaryCard(label: String, group: GroupRank?, modifier: Modifier = Modifier) {
    // Sin altura fija: el contenido fluye para que el tag de rango se vea entero (antes se cortaba).
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Radius.lg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        val tier = group?.tier ?: RankTier.SIN_RANGO
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RankMedal(tier, 52.dp)
            Spacer(modifier = Modifier.width(Spacing.md))
            Column {
                Text(label, color = MaterialTheme.colorScheme.secondary, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = group?.group?.display?.uppercase() ?: "—",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
                Text(
                    text = Rank(tier, divisionFromProgress(group?.progressToNext ?: 0f)).label,
                    color = if (tier == RankTier.SIN_RANGO) MaterialTheme.colorScheme.secondary else Color(tier.colorHex),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun GroupRow(g: GroupRank) {
    var expanded by remember { mutableStateOf(false) }
    val tierColor = Color(g.tier.colorHex)

    Surface(
        shape = RoundedCornerShape(Radius.lg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = g.group.display.uppercase(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = Rank(g.tier, divisionFromProgress(g.progressToNext)).label,
                        color = if (g.tier == RankTier.SIN_RANGO) MaterialTheme.colorScheme.secondary else tierColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                FatigueChip(g.fatigue, g.daysSinceTrained)
                Spacer(modifier = Modifier.width(Spacing.sm))
                RankMedal(g.tier, 44.dp)
                Spacer(modifier = Modifier.width(Spacing.sm))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            TierBar(g.progressToNext, if (g.tier == RankTier.SIN_RANGO) MaterialTheme.colorScheme.secondary else tierColor)

            if (expanded) {
                // La medalla en GRANDE para disfrutar el icono del rango del grupo.
                if (g.tier != RankTier.SIN_RANGO) {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RankMedal(g.tier, 104.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Rank(g.tier, divisionFromProgress(g.progressToNext)).label,
                            color = tierColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.md))
                g.subgroups.forEach { s -> SubgroupRow(s) }
            }
        }
    }
}

@Composable
private fun SubgroupRow(s: SubgroupRank) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = s.subgroup.display,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (s.approx) {
            Text(
                text = "APROX.",
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(end = Spacing.sm)
            )
        }
        RankBadge(s.tier, small = true)
    }
}

@Composable
private fun RankBadge(tier: RankTier, small: Boolean = false, winner: Boolean = false) {
    val isRanked = tier != RankTier.SIN_RANGO
    // Ancho fijo + texto centrado -> todos los tags miden igual (Oro no queda más pequeño que Platino)
    Surface(
        modifier = Modifier
            .width(if (small) 78.dp else 96.dp)
            .then(if (winner) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(Radius.sm)) else Modifier),
        shape = RoundedCornerShape(Radius.sm),
        color = if (isRanked) Color(tier.colorHex) else MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = tier.display.uppercase(),
            color = if (isRanked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
            fontSize = if (small) 8.sp else 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
        )
    }
}

/** Indicador compacto de fatiga: punto de color (nivel) + recencia del último entreno. */
@Composable
private fun FatigueChip(fatigue: Fatigue, daysSince: Int) {
    if (fatigue == Fatigue.SIN_DATOS) return
    val color = fatigueColor(fatigue)
    val levelLabel = when (fatigue) {
        Fatigue.ALTA -> stringResource(R.string.fatigue_alta)
        Fatigue.MEDIA -> stringResource(R.string.fatigue_media)
        Fatigue.BAJA -> stringResource(R.string.fatigue_baja)
        else -> stringResource(R.string.fatigue_descansado)
    }
    val recency = when {
        daysSince <= 0 -> stringResource(R.string.fatigue_trained_today)
        daysSince == 1 -> stringResource(R.string.fatigue_trained_yesterday)
        else -> stringResource(R.string.fatigue_trained_days, daysSince)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics { contentDescription = levelLabel }
    ) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = recency,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun fatigueColor(fatigue: Fatigue): Color = when (fatigue) {
    Fatigue.ALTA -> Color(0xFFE5533D)
    Fatigue.MEDIA -> Color(0xFFD4AF37)
    Fatigue.BAJA -> Color(0xFF7FB069)
    else -> MaterialTheme.colorScheme.secondary
}

@Composable
private fun TierBar(progress: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pestaña AMIGOS: ranking (podio + filtro por músculo) + comparación (Fases 5 y B/D)
// ─────────────────────────────────────────────────────────────────────────────

/** Fila del ranking. Mi lado sale del PanteonResult; el de un amigo, de su PublicProfile. */
private data class RankRow(
    val username: String,
    val level: Int,
    val overall: RankTier,
    val groups: Map<MuscleGroup, RankTier>,
    val divisions: Map<MuscleGroup, Int>,
    val isMe: Boolean = false,
    val photo: Any? = null   // content:// Uri (mío) o ImageBitmap decodificado (amigo)
)

private fun RankRow.rankFor(group: MuscleGroup): Rank =
    Rank(groups[group] ?: RankTier.SIN_RANGO, divisions[group] ?: 3)

/** Tier + división que se muestran según el filtro (null = global). */
private fun rankOf(row: RankRow, filter: MuscleGroup?): Pair<RankTier, Int> =
    if (filter == null) row.overall to 3
    else (row.groups[filter] ?: RankTier.SIN_RANGO) to (row.divisions[filter] ?: 3)

/** Puntuación comparable: pesa el tier y, a igualdad, la división (I mejor que III). */
private fun rankScore(tier: RankTier, division: Int): Int = tierIndex(tier) * 3 + (3 - division)

@Composable
private fun FriendsRankingSection(
    socialViewModel: SocialViewModel,
    myResult: PanteonResult,
    onManageFriends: () -> Unit
) {
    val username = socialViewModel.myUsername.collectAsState().value
    val buckets = socialViewModel.buckets.collectAsState().value   // mantiene vivo el listener de amistades
    val ranking = socialViewModel.friendRanking.collectAsState().value
    val myAvatar = socialViewModel.myAvatarUri.collectAsState().value
    var filter by remember { mutableStateOf<MuscleGroup?>(null) }
    var comparing by remember { mutableStateOf<String?>(null) }   // @usuario del amigo en comparación

    // Carga/recarga al abrir la pestaña y cuando cambie mi lista de amigos.
    LaunchedEffect(buckets.friends) { socialViewModel.loadRanking() }

    if (username == null) {
        RankingCta(
            text = stringResource(R.string.ranking_need_username),
            button = stringResource(R.string.ranking_manage_friends),
            onClick = onManageFriends
        )
        return
    }

    // Decodifica una sola vez los avatares (base64) de los amigos.
    val friendAvatars = remember(ranking.profiles) {
        ranking.profiles.associate { it.uid to AvatarImage.decode(it.avatar) }
    }

    val me = RankRow(
        username = username,
        level = ranking.myLevel,
        overall = myResult.strongest?.tier ?: RankTier.SIN_RANGO,
        groups = myResult.groups.associate { it.group to it.tier },
        divisions = myResult.groups.associate { it.group to divisionFromProgress(it.progressToNext) },
        isMe = true,
        photo = myAvatar
    )
    val friends = ranking.profiles.map { it.toRankRow().copy(photo = friendAvatars[it.uid]) }
    val board = (listOf(me) + friends).sortedWith(
        compareByDescending<RankRow> { val (t, d) = rankOf(it, filter); rankScore(t, d) }
            .thenBy { it.username.lowercase() }
    )

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        RankingFilterChips(filter) { filter = it }

        // Tocar a un amigo (en el podio o en la lista) lo pone en comparación; volver a tocarlo la cierra.
        val onSelect: (RankRow) -> Unit = { r ->
            if (!r.isMe) comparing = if (comparing == r.username) null else r.username
        }

        if (board.size >= 2) {
            Podium(board.take(3), filter, comparing, onSelect)
            board.drop(3).forEachIndexed { i, row ->
                RankingListRow(position = i + 4, row = row, filter = filter,
                    selected = row.username == comparing, onClick = { onSelect(row) })
            }
        } else {
            board.forEachIndexed { i, row ->
                RankingListRow(position = i + 1, row = row, filter = filter,
                    selected = row.username == comparing, onClick = { onSelect(row) })
            }
        }

        // Cara a cara del amigo seleccionado, debajo de todo (podio o lista).
        friends.firstOrNull { it.username == comparing }?.let { friend ->
            ComparisonPanel(me = me, friend = friend)
        }

        if (friends.isEmpty()) {
            if (ranking.loading) {
                Box(Modifier.fillMaxWidth().padding(Spacing.lg), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(26.dp)
                    )
                }
            } else {
                RankingCta(
                    text = stringResource(R.string.ranking_no_friends),
                    button = stringResource(R.string.ranking_manage_friends),
                    onClick = onManageFriends
                )
            }
        }
    }
}

@Composable
private fun RankingFilterChips(selected: MuscleGroup?, onSelect: (MuscleGroup?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        FilterChip(stringResource(R.string.ranking_filter_global), selected == null) { onSelect(null) }
        MuscleGroup.entries.forEach { g ->
            FilterChip(g.display.uppercase(), selected == g) { onSelect(g) }
        }
    }
}

@Composable
private fun FilterChip(text: String, active: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (active) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)
        )
    }
}

@Composable
private fun Podium(top3: List<RankRow>, filter: MuscleGroup?, comparing: String?, onSelect: (RankRow) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
            top3.getOrNull(1)?.let { PodiumPlace(it, 2, filter, it.username == comparing) { onSelect(it) } }
        }
        Box(Modifier.weight(1.25f), contentAlignment = Alignment.BottomCenter) {
            top3.getOrNull(0)?.let { PodiumPlace(it, 1, filter, it.username == comparing) { onSelect(it) } }
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
            top3.getOrNull(2)?.let { PodiumPlace(it, 3, filter, it.username == comparing) { onSelect(it) } }
        }
    }
}

@Composable
private fun PodiumPlace(row: RankRow, place: Int, filter: MuscleGroup?, selected: Boolean, onClick: () -> Unit) {
    val avatarSize = if (place == 1) 84.dp else 60.dp
    val ringColor = when (place) {
        1 -> Color(RankTier.ORO.colorHex)
        2 -> Color(RankTier.PLATA.colorHex)
        else -> Color(RankTier.BRONCE.colorHex)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.lg))
            .then(if (!row.isMe) Modifier.clickable { onClick() } else Modifier)
            .then(if (selected) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) else Modifier)
            .padding(horizontal = 6.dp, vertical = Spacing.xs)
    ) {
        if (place == 1) {
            Icon(
                Icons.Default.EmojiEvents, null,
                tint = AegisGoldAccent,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.height(Spacing.xs))
        }
        AegisAvatar(row.username, avatarSize, ringColor, borderWidth = if (place == 1) 3.dp else 2.dp, photo = row.photo)
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (row.isMe) stringResource(R.string.ranking_you) else "@${row.username}",
            color = if (row.isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            fontSize = if (place == 1) 14.sp else 12.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
        Text(
            text = stringResource(R.string.ranking_level_short, row.level),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun RankingListRow(position: Int, row: RankRow, filter: MuscleGroup?, selected: Boolean, onClick: () -> Unit) {
    val canCompare = !row.isMe
    val (tier, _) = rankOf(row, filter)

    Surface(
        shape = RoundedCornerShape(Radius.lg),
        color = if (row.isMe || selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (canCompare) Modifier.clickable { onClick() } else Modifier)
                .padding(vertical = 10.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$position",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(24.dp)
            )
            Spacer(Modifier.width(Spacing.sm))
            AegisAvatar(
                row.username, 44.dp,
                borderColor = if (row.isMe) MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                borderWidth = if (row.isMe) 2.dp else 1.dp,
                photo = row.photo
            )
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "@${row.username}",
                        color = if (row.isMe) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    if (row.isMe) {
                        Spacer(Modifier.width(6.dp))
                        TuPill()
                    }
                }
                Text(
                    text = stringResource(R.string.ranking_level, row.level),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            RankMedal(tier, 34.dp)
        }
    }
}

@Composable
private fun TuPill() {
    Surface(shape = RoundedCornerShape(Radius.sm), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)) {
        Text(
            text = stringResource(R.string.ranking_you),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/** Cara a cara: cabecera (TÚ vs @amigo) + una fila por grupo con divisiones y ganador. */
@Composable
private fun ComparisonPanel(me: RankRow, friend: RankRow) {
    Column(modifier = Modifier.padding(horizontal = 6.dp).padding(top = Spacing.xs, bottom = Spacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AegisAvatar(me.username, 44.dp, MaterialTheme.colorScheme.primary, 2.dp, photo = me.photo)
                Spacer(Modifier.height(Spacing.xs))
                Text(stringResource(R.string.ranking_you), color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            Text(
                text = "VS",
                color = AegisGoldAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AegisAvatar(friend.username, 44.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), 2.dp, photo = friend.photo)
                Spacer(Modifier.height(Spacing.xs))
                Text("@${friend.username}", color = MaterialTheme.colorScheme.onBackground, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1)
            }
        }
        Spacer(Modifier.height(Spacing.sm))
        MuscleGroup.entries.forEach { grp ->
            CompareGroupRow(group = grp, mine = me.rankFor(grp), theirs = friend.rankFor(grp))
        }
    }
}

@Composable
private fun CompareGroupRow(group: MuscleGroup, mine: Rank, theirs: Rank) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RankDivisionBadge(mine)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = group.display.uppercase(),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
        RankDivisionBadge(theirs)
    }
}

/** Insignia de rango con división (ej. "ORO I"). */
@Composable
private fun RankDivisionBadge(rank: Rank) {
    val isRanked = rank.tier != RankTier.SIN_RANGO
    Surface(
        modifier = Modifier.width(78.dp),
        shape = RoundedCornerShape(Radius.sm),
        color = if (isRanked) Color(rank.tier.colorHex) else MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = rank.label,
            color = if (isRanked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
        )
    }
}

@Composable
private fun RankingCta(text: String, button: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(Radius.lg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(Radius.md),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onClick() }
            ) {
                Text(
                    text = button,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}

private fun PublicProfile.toRankRow(): RankRow = RankRow(
    username = username,
    level = level,
    overall = tierOf(overallTier),
    groups = groupTiers.mapNotNull { (k, v) -> groupOf(k)?.let { it to tierOf(v) } }.toMap(),
    divisions = groupDivisions.mapNotNull { (k, v) -> groupOf(k)?.let { it to v } }.toMap()
)

private fun tierOf(name: String): RankTier =
    runCatching { RankTier.valueOf(name) }.getOrDefault(RankTier.SIN_RANGO)

private fun groupOf(name: String): MuscleGroup? =
    runCatching { MuscleGroup.valueOf(name) }.getOrNull()

private fun tierIndex(t: RankTier): Int =
    if (t == RankTier.SIN_RANGO) -1 else RankTier.ladder.indexOf(t)

// ─────────────────────────────────────────────────────────────────────────────
// Pestaña LIGA: liga mensual global (esfuerzo del mes relativo al peso). Fase 1.
// Tarjeta hero con mi liga + puntos + progreso, y tabla mundial (top-100) con mi
// fila y mis amigos resaltados. Ver docs/us-liga.md.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LeagueSection(
    leagueViewModel: LeagueViewModel,
    socialViewModel: SocialViewModel,
    onManageFriends: () -> Unit
) {
    val username = socialViewModel.myUsername.collectAsState().value
    val buckets = socialViewModel.buckets.collectAsState().value   // mantiene vivo el listener de amistades
    val myAvatar = socialViewModel.myAvatarUri.collectAsState().value
    val state = leagueViewModel.state.collectAsState().value

    // Recalcula mis puntos y refresca la tabla al abrir la pestaña y al cambiar amigos/@usuario.
    LaunchedEffect(username, buckets.friends) {
        leagueViewModel.load(buckets.friends.map { it.uid }.toSet(), username)
    }

    if (username == null) {
        RankingCta(
            text = stringResource(R.string.league_need_username),
            button = stringResource(R.string.ranking_manage_friends),
            onClick = onManageFriends
        )
        return
    }

    // Decodifica una vez los avatares (base64) de los demás competidores.
    val avatars = remember(state.board) {
        state.board.filter { it.uid != state.myUid }.associate { it.uid to AvatarImage.decode(it.avatar) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        LeagueHeroCard(state)

        Text(
            text = stringResource(R.string.league_world_table),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(top = Spacing.sm, start = 6.dp)
        )

        if (state.loading && state.board.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(Spacing.lg), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(26.dp)
                )
            }
        } else {
            state.board.forEachIndexed { i, entry ->
                val isMe = entry.uid == state.myUid
                LeagueRow(
                    position = i + 1,
                    entry = entry,
                    isMe = isMe,
                    isFriend = entry.uid in state.friendUids,
                    photo = if (isMe) myAvatar else avatars[entry.uid]
                )
            }
        }
    }
}

@Composable
private fun LeagueHeroCard(state: LeagueViewModel.LeagueState) {
    val tierColor = Color(state.myTier.colorHex)
    Surface(
        shape = RoundedCornerShape(Radius.xl),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(Spacing.lg)) {
            Text(
                text = stringResource(R.string.league_season, seasonLabel(state.seasonId)),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(Spacing.md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RankMedal(state.myTier, 72.dp)
                Spacer(Modifier.width(Spacing.lg))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.league_tier_name, state.myTier.display.uppercase()),
                        color = tierColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = stringResource(R.string.league_points, formatPoints(state.myPoints)),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = stringResource(R.string.league_sessions_month, state.mySessions),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(Spacing.md))
            TierBar(state.progress, tierColor)
            Spacer(Modifier.height(6.dp))
            Text(
                text = state.nextThreshold?.let {
                    stringResource(R.string.league_to_next, formatPoints((it - state.myPoints).coerceAtLeast(0)))
                } ?: stringResource(R.string.league_top_tier),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LeagueRow(position: Int, entry: LeagueEntry, isMe: Boolean, isFriend: Boolean, photo: Any?) {
    val tier = runCatching { RankTier.valueOf(entry.tier) }.getOrDefault(RankTier.BRONCE)
    Surface(
        shape = RoundedCornerShape(Radius.lg),
        color = if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$position",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(24.dp)
            )
            Spacer(Modifier.width(Spacing.sm))
            AegisAvatar(
                entry.username.ifBlank { "?" }, 44.dp,
                borderColor = when {
                    isMe -> MaterialTheme.colorScheme.primary
                    isFriend -> AegisGoldAccent.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                },
                borderWidth = if (isMe || isFriend) 2.dp else 1.dp,
                photo = photo
            )
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "@${entry.username}",
                        color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    if (isMe) {
                        Spacer(Modifier.width(6.dp)); TuPill()
                    } else if (isFriend) {
                        Spacer(Modifier.width(6.dp)); FriendPill()
                    }
                }
                Text(
                    text = stringResource(R.string.league_points, formatPoints(entry.points)),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            RankMedal(tier, 34.dp)
        }
    }
}

@Composable
private fun FriendPill() {
    Surface(shape = RoundedCornerShape(Radius.sm), color = AegisGoldAccent.copy(alpha = 0.20f)) {
        Text(
            text = stringResource(R.string.league_friend),
            color = AegisGoldAccent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/** "2026-09" → "SEP 2026" (nombre de mes localizado). */
private fun seasonLabel(seasonId: String): String {
    val parts = seasonId.split("-")
    val y = parts.getOrNull(0)?.toIntOrNull()
    val m = parts.getOrNull(1)?.toIntOrNull()
    if (y == null || m == null) return seasonId
    val cal = java.util.Calendar.getInstance().apply { set(y, m - 1, 1) }
    return java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault()).format(cal.time).uppercase()
}

private fun formatPoints(p: Long): String = String.format(java.util.Locale.getDefault(), "%,d", p)
