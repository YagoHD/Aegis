package com.yago.aegis.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.ui.theme.AegisCard
import com.yago.aegis.ui.theme.AegisWhite
import coil3.compose.AsyncImage
//TEST
@Composable
fun VisualLogSection(
    baseUri: Uri?,
    actualUri: Uri?,
    onAddClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.label_visual_log), color = AegisWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = stringResource(R.string.label_add), color = AegisBronze, fontSize = 12.sp, modifier = Modifier.clickable { onAddClick() }.padding(4.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto BASE
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                ProgressPhotoCard(
                    label = stringResource(R.string.label_base),
                    date = "OCT 12",
                    photoUri = baseUri
                )
            }

            Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp), tint = AegisBronze)
            }

            // Foto ACTUAL
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                ProgressPhotoCard(
                    label = stringResource(R.string.label_actual),
                    date = stringResource(R.string.label_today),
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
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AegisCard),
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
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A)))
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.6f)).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = date, color = Color.Gray, fontSize = 10.sp)
            Text(text = label, color = AegisWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}