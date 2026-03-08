package com.yago.aegis.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.yago.aegis.ui.components.AegisTextField
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.ui.theme.AegisCream
import com.yago.aegis.ui.theme.AegisSteel
import com.yago.aegis.ui.theme.AegisWhite
import com.yago.aegis.ui.theme.BackgroundBlackGrey
import com.yago.aegis.ui.theme.MatteBlack
import com.yago.aegis.viewmodel.ProfileViewModel
import android.net.Uri // 👈 Esta es la clave
import android.content.Intent
import com.yago.aegis.data.PhotoType
import com.yago.aegis.ui.components.AegisStepProgress

@Composable
fun IdentityScreen(
    viewModel: ProfileViewModel,
    onContinue: (String, String, String?) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    // Launcher para la foto de perfil
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                // IMPORTANTE: Primero el permiso, luego el guardado
                context.contentResolver.takePersistableUriPermission(
                    selectedUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                // Guardamos en el ViewModel (esto dispara el proceso en segundo plano)
                viewModel.updateAvatar(selectedUri.toString())

                // Actualizamos la vista previa local
                selectedPhotoUri = selectedUri.toString()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlackGrey)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AegisTopBar(
            title = "IDENTIDAD",
            subtitle = "PASO 02",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = AegisWhite)
                }
            }
        )
        // La barra de progreso ahora está en la misma posición relativa que en las otras
        AegisStepProgress(currentStep = 2)

        Spacer(modifier = Modifier.height(32.dp))

        // Selector de Foto de Perfil (Círculo con borde AegisBronze)
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .background(AegisCream) // El color crema de tu paleta
                .clickable { launcher.launch("image/*") }
        ) {
            if (selectedPhotoUri != null) {
                AsyncImage(
                    model = selectedPhotoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).align(Alignment.Center),
                    tint = AegisSteel
                )
            }
            // Botón "+" pequeño
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .background(MatteBlack, CircleShape)
                    .border(1.dp, AegisBronze, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = AegisBronze, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Prepara tu perfil",
            style = TextStyle(color = AegisWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Campo de Nombre Full Identity (OBLIGATORIO)
        AegisTextField(
            label = "NOMBRE DE USUARIO",
            value = name,
            onValueChange = { name = it },
            placeholder = "Alexander Vance"
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo de Bio (OPCIONAL)
        AegisTextField(
            label = "BIOGRAFIA",
            value = bio,
            onValueChange = { bio = it },
            placeholder = "Describe tu forma de entrenar...",
            isSingleLine = false,
            modifier = Modifier.height(120.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Botón Continuar con degradado Aegis
        Button(
            onClick = { if (name.isNotBlank()) onContinue(name, bio, selectedPhotoUri) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = name.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AegisBronze,
                disabledContainerColor = AegisSteel
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("CONTINUAR", color = Color.Black, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black)
            }
        }
    }
}