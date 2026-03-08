package com.yago.aegis.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.yago.aegis.viewmodel.ProfileViewModel
import android.net.Uri
import android.content.Intent
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

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    selectedUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.updateAvatar(selectedUri.toString())
                selectedPhotoUri = selectedUri.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ELIMINADO: .verticalScroll(rememberScrollState())
    // Esto hace que la pantalla sea estática y "Premium"
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        AegisTopBar(
            title = "IDENTIDAD",
            subtitle = "PASO 02",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        )

        AegisStepProgress(currentStep = 2)

        Spacer(modifier = Modifier.height(32.dp)) // Reducido un poco para ganar aire

        // Avatar Section
        Box(
            modifier = Modifier
                .size(140.dp) // Ajustado ligeramente
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape)
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
                    modifier = Modifier.size(56.dp).align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "CONFIGURA TU AVATAR",
            style = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp, // Un poco más pequeño para elegancia
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Form Section
        AegisTextField(
            label = "NOMBRE DE USUARIO",
            value = name,
            onValueChange = { name = it },
            placeholder = "Alexander Vance"
        )

        Spacer(modifier = Modifier.height(24.dp))

        AegisTextField(
            label = "BIOGRAFÍA (OPCIONAL)",
            value = bio,
            onValueChange = { bio = it },
            placeholder = "Define tu filosofía...",
            isSingleLine = false,
            modifier = Modifier.height(120.dp) // Ajustado para que quepa en pantallas pequeñas
        )

        // Este Spacer "empuja" todo lo de arriba hacia arriba y lo de abajo hacia abajo
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { if (name.isNotBlank()) onContinue(name, bio, selectedPhotoUri) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = name.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "CONTINUAR",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
            }
        }
    }
}