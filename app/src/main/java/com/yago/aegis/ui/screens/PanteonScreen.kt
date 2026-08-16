package com.yago.aegis.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.painterResource
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
import com.yago.aegis.data.RankTier
import com.yago.aegis.data.SubgroupRank
import com.yago.aegis.data.social.PublicProfile
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.viewmodel.PanteonViewModel
import com.yago.aegis.viewmodel.SocialViewModel

private enum class PanteonTab { MINE, FRIENDS }

@Composable
fun PanteonScreen(
    viewModel: PanteonViewModel,
    socialViewModel: SocialViewModel,
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
                            tint = MaterialTheme.colorScheme.primary
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item { PanteonTabs(tab) { tab = it } }

            when (tab) {
                PanteonTab.MINE -> {
                    item { BodyMapPlaceholder() }
                    if (!hasRanks) {
                        item { EmptyRanks() }
                    } else {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
        TabItem(stringResource(R.string.panteon_league), active = false, locked = true) {}
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
                Spacer(modifier = Modifier.width(4.dp))
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
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.MilitaryTech, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
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
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        val tier = group?.tier ?: RankTier.SIN_RANGO
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RankMedal(tier, 52.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, color = MaterialTheme.colorScheme.secondary, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = group?.group?.display?.uppercase() ?: "—",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
                Text(
                    text = tier.display.uppercase(),
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
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        text = g.tier.display.uppercase(),
                        color = if (g.tier == RankTier.SIN_RANGO) MaterialTheme.colorScheme.secondary else tierColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                FatigueChip(g.fatigue, g.daysSinceTrained)
                Spacer(modifier = Modifier.width(8.dp))
                RankMedal(g.tier, 44.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            TierBar(g.progressToNext, if (g.tier == RankTier.SIN_RANGO) MaterialTheme.colorScheme.secondary else tierColor)

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
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
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        RankBadge(s.tier, small = true)
    }
}

/** Medalla (imagen) del tier. Se usa en las tarjetas resumen y en las filas de grupo. */
@Composable
private fun RankMedal(tier: RankTier, size: Dp) {
    Image(
        painter = painterResource(medalRes(tier)),
        contentDescription = tier.display,
        modifier = Modifier.size(size)
    )
}

@DrawableRes
private fun medalRes(tier: RankTier): Int = when (tier) {
    RankTier.BRONCE -> R.drawable.rank_bronce
    RankTier.PLATA -> R.drawable.rank_plata
    RankTier.ORO -> R.drawable.rank_oro
    RankTier.PLATINO -> R.drawable.rank_platino
    RankTier.DIAMANTE -> R.drawable.rank_diamante
    RankTier.TITAN -> R.drawable.rank_titan
    RankTier.SIN_RANGO -> R.drawable.rank_sin_rango
}

@Composable
private fun RankBadge(tier: RankTier, small: Boolean = false, winner: Boolean = false) {
    val isRanked = tier != RankTier.SIN_RANGO
    // Ancho fijo + texto centrado -> todos los tags miden igual (Oro no queda más pequeño que Platino)
    Surface(
        modifier = Modifier
            .width(if (small) 78.dp else 96.dp)
            .then(if (winner) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)) else Modifier),
        shape = RoundedCornerShape(4.dp),
        color = if (isRanked) Color(tier.colorHex) else MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = tier.display.uppercase(),
            color = if (isRanked) Color.Black else MaterialTheme.colorScheme.secondary,
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
// Pestaña AMIGOS: ranking + comparación de rangos (Fase 5)
// ─────────────────────────────────────────────────────────────────────────────

/** Fila del ranking. Mi lado sale del PanteonResult; el de un amigo, de su PublicProfile. */
private data class RankRow(
    val username: String,
    val overall: RankTier,
    val groups: Map<MuscleGroup, RankTier>,
    val isMe: Boolean = false
)

@Composable
private fun FriendsRankingSection(
    socialViewModel: SocialViewModel,
    myResult: PanteonResult,
    onManageFriends: () -> Unit
) {
    val username = socialViewModel.myUsername.collectAsState().value
    val buckets = socialViewModel.buckets.collectAsState().value   // mantiene vivo el listener de amistades
    val ranking = socialViewModel.friendRanking.collectAsState().value

    // Carga/recarga al abrir la pestaña y cuando cambie mi lista de amigos.
    LaunchedEffect(buckets.friends) { socialViewModel.loadRanking() }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (username == null) {
            RankingCta(
                text = stringResource(R.string.ranking_need_username),
                button = stringResource(R.string.ranking_manage_friends),
                onClick = onManageFriends
            )
        } else {
            val me = RankRow(
                username = username,
                overall = myResult.strongest?.tier ?: RankTier.SIN_RANGO,
                groups = myResult.groups.associate { it.group to it.tier },
                isMe = true
            )
            val friends = ranking.profiles.map { it.toRankRow() }
            val board = (listOf(me) + friends).sortedWith(
                compareByDescending<RankRow> { tierIndex(it.overall) }.thenBy { it.username.lowercase() }
            )

            Text(
                text = stringResource(R.string.ranking_compare_hint),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 15.sp
            )

            board.forEachIndexed { i, row -> RankingRow(position = i + 1, row = row, me = me) }

            if (friends.isEmpty()) {
                if (ranking.loading) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
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
}

@Composable
private fun RankingRow(position: Int, row: RankRow, me: RankRow) {
    var expanded by remember { mutableStateOf(false) }
    val canCompare = !row.isMe
    val tierColor = if (row.overall == RankTier.SIN_RANGO) MaterialTheme.colorScheme.secondary
                    else Color(row.overall.colorHex)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (row.isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (canCompare) Modifier.clickable { expanded = !expanded } else Modifier),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#$position",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.width(30.dp)
                )
                RankMedal(row.overall, 40.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (row.isMe) stringResource(R.string.ranking_you) else "@${row.username}",
                        color = if (row.isMe) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1
                    )
                    Text(
                        text = row.overall.display.uppercase(),
                        color = tierColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                if (canCompare) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (expanded && canCompare) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    CompareHeaderLabel(stringResource(R.string.ranking_you))
                    Spacer(modifier = Modifier.weight(1f))
                    CompareHeaderLabel("@${row.username}")
                }
                Spacer(modifier = Modifier.height(6.dp))
                MuscleGroup.entries.forEach { grp ->
                    CompareGroupRow(
                        group = grp,
                        mine = me.groups[grp] ?: RankTier.SIN_RANGO,
                        theirs = row.groups[grp] ?: RankTier.SIN_RANGO
                    )
                }
            }
        }
    }
}

@Composable
private fun CompareHeaderLabel(text: String) {
    Box(modifier = Modifier.width(78.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun CompareGroupRow(group: MuscleGroup, mine: RankTier, theirs: RankTier) {
    val mineIdx = tierIndex(mine)
    val theirsIdx = tierIndex(theirs)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RankBadge(mine, small = true, winner = mineIdx > theirsIdx)
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
        RankBadge(theirs, small = true, winner = theirsIdx > mineIdx)
    }
}

@Composable
private fun RankingCta(text: String, button: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
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
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onClick() }
            ) {
                Text(
                    text = button,
                    color = Color.Black,
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
    overall = tierOf(overallTier),
    groups = groupTiers.mapNotNull { (k, v) -> groupOf(k)?.let { it to tierOf(v) } }.toMap()
)

private fun tierOf(name: String): RankTier =
    runCatching { RankTier.valueOf(name) }.getOrDefault(RankTier.SIN_RANGO)

private fun groupOf(name: String): MuscleGroup? =
    runCatching { MuscleGroup.valueOf(name) }.getOrNull()

private fun tierIndex(t: RankTier): Int =
    if (t == RankTier.SIN_RANGO) -1 else RankTier.ladder.indexOf(t)
