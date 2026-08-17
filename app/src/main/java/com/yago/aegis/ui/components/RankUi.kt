package com.yago.aegis.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.yago.aegis.R
import com.yago.aegis.data.RankTier

/**
 * Avatar del usuario. De momento es la INICIAL del @usuario sobre un círculo (la foto
 * sincronizada llega más adelante). Compartido por la pantalla de Amigos y el ranking.
 */
@Composable
fun AegisAvatar(
    username: String,
    size: Dp,
    borderColor: Color,
    borderWidth: Dp = 1.dp,
    photo: Any? = null
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when (photo) {
            // Amigo: base64 ya decodificado a ImageBitmap.
            is ImageBitmap -> Image(
                bitmap = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
            // Sin foto: inicial del @usuario.
            null -> Text(
                text = username.firstOrNull()?.uppercase() ?: "?",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = (size.value * 0.4f).sp,
                fontWeight = FontWeight.Black
            )
            // Mío: content:// Uri (String) cargado por Coil a calidad plena.
            else -> AsyncImage(
                model = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        }
    }
}

/** Medalla (imagen) del tier de rango. */
@Composable
fun RankMedal(tier: RankTier, size: Dp) {
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
