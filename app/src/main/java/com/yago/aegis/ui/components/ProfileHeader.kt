package com.yago.aegis.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.yago.aegis.R // Importante para acceder a tus recursos
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.ui.theme.AegisWhite

@Composable
fun ProfileHeader(name: String, disciplineDay: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally // Centra todo el contenido
    ) {
        // La Imagen de Perfil Circular
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Usa el nombre de tu archivo
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape) // Corta la imagen en círculo
                .border(2.dp, AegisBronze, CircleShape), // Añade el borde bronce de Aegis
            contentScale = ContentScale.Crop // Evita que la foto se deforme
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre del Usuario
        Text(
            text = name,
            color = AegisWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        // Día de Disciplina
        Text(
            text = stringResource(R.string.label_discipline_day, disciplineDay).uppercase(),
            color = AegisBronze,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
    }
}