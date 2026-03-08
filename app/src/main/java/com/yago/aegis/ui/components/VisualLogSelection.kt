package com.yago.aegis.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import coil3.compose.AsyncImage

@Composable
fun VisualLogSection(
    baseUri: Uri?,
    actualUri: Uri?,
    onAddClick: () -> Unit,
    actualDate: String?,
    baseDate: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ETIQUETA SECCIÓN: AegisSteel (Gris técnico)
            Text(
                text = stringResource(R.string.label_visual_log).uppercase(),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            // ACCIÓN AÑADIR: 10% Bronce
            Text(
                text = "+ ${stringResource(R.string.label_add).uppercase()}",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .clickable { onAddClick() }
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp), // Espacio uniforme entre tarjetas
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto BASE
            Box(modifier = Modifier.weight(1f)) {
                ProgressPhotoCard(
                    label = stringResource(R.string.label_base),
                    date = baseDate ?: "-- --",
                    photoUri = baseUri
                )
            }

            // El separador ahora es más sutil para no robar atención
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )

            // Foto ACTUAL
            Box(modifier = Modifier.weight(1f)) {
                ProgressPhotoCard(
                    label = stringResource(R.string.label_actual),
                    date = actualDate ?: "-- --",
                    photoUri = actualUri
                )
            }
        }
    }
}

@Composable
fun ProgressPhotoCard(label: String, date: String, photoUri: Uri?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp) // Un poco más alto para estilización vertical
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface) // 30% SurfaceDark
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Estado vacío: Icono sutil en lugar de bloque negro
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image, // Añade este import si es necesario
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Overlay de Información: Degradado para que se vea "Premium"
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
                .padding(bottom = 12.dp, top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label.uppercase(),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }
    }
}