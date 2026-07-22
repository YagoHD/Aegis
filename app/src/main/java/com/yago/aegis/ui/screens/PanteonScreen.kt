package com.yago.aegis.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.data.GroupRank
import com.yago.aegis.data.RankTier
import com.yago.aegis.data.SubgroupRank
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.viewmodel.PanteonViewModel

@Composable
fun PanteonScreen(viewModel: PanteonViewModel) {
    val result by viewModel.result.collectAsState()
    val hasRanks = result.groups.any { it.tier != RankTier.SIN_RANGO }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AegisTopBar(
                title = stringResource(R.string.nav_panteon).uppercase(),
                subtitle = stringResource(R.string.panteon_subtitle)
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
            item { PanteonTabs() }
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

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
private fun PanteonTabs() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TabItem(stringResource(R.string.panteon_my_ranks), active = true, locked = false)
        TabItem(stringResource(R.string.panteon_friends), active = false, locked = true)
        TabItem(stringResource(R.string.panteon_league), active = false, locked = true)
    }
}

@Composable
private fun TabItem(text: String, active: Boolean, locked: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
    Surface(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = MaterialTheme.colorScheme.secondary, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Column {
                Text(
                    text = group?.group?.display?.uppercase() ?: "—",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                RankBadge(group?.tier ?: RankTier.SIN_RANGO)
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
                Text(
                    text = g.group.display.uppercase(),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )
                RankBadge(g.tier)
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

@Composable
private fun RankBadge(tier: RankTier, small: Boolean = false) {
    val isRanked = tier != RankTier.SIN_RANGO
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (isRanked) Color(tier.colorHex) else MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = tier.display.uppercase(),
            color = if (isRanked) Color.Black else MaterialTheme.colorScheme.secondary,
            fontSize = if (small) 8.sp else 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
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
