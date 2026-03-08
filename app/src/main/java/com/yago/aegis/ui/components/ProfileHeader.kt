package com.yago.aegis.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.yago.aegis.R

@Composable
fun ProfileHeader(
    name: String,
    disciplineDay: Int,
    profilePhotoUri: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- AVATAR CON BORDE TÉCNICO ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(130.dp) // Un poco más grande para dar presencia
        ) {
            AsyncImage(
                model = profilePhotoUri ?: R.drawable.ic_launcher_foreground,
                contentDescription = "Foto de perfil",
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                error = painterResource(R.drawable.ic_launcher_foreground),
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    // Borde fino de 1.dp: Elegancia y precisión
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )

            // Opcional: Podrías añadir un segundo anillo decorativo muy fino
            // con AegisSteel para dar un aspecto de "visor"
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- NOMBRE: AegisWhite con espaciado ---
        Text(
            text = name.uppercase(), // En mayúsculas para mantener la autoridad de la marca
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // --- INDICADOR DE DISCIPLINA: El toque de 10% Bronce ---
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), // Fondo sutil bronce
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.label_discipline_day, disciplineDay).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}