package com.yago.aegis.ui.screens
import com.yago.aegis.ui.theme.Spacing
import com.yago.aegis.ui.theme.Radius

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.yago.aegis.R
import com.yago.aegis.ui.components.AegisPrimaryButton
import com.yago.aegis.ui.components.AegisTextField
import com.yago.aegis.ui.components.AegisTopBar
import com.yago.aegis.viewmodel.ProfileViewModel
import android.net.Uri
import android.content.Intent
import com.yago.aegis.ui.components.AegisStepProgress

@Composable
fun IdentityScreen(
    viewModel: ProfileViewModel,
    onContinue: (String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    var avatarCropUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { avatarCropUri = it }
    }
    avatarCropUri?.let { cropUri ->
        com.yago.aegis.ui.components.AvatarCropDialog(
            uri = cropUri,
            onDismiss = { avatarCropUri = null },
            onConfirm = { bmp ->
                val saved = com.yago.aegis.util.AvatarImage.saveAvatar(context, bmp)
                viewModel.updateAvatar(saved.toString())
                selectedPhotoUri = saved.toString()
                avatarCropUri = null
            }
        )
    }

    // ELIMINADO: .verticalScroll(rememberScrollState())
    // Esto hace que la pantalla sea estática y "Premium"
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(Spacing.xl)
    ) {
        AegisTopBar(
            title = stringResource(R.string.identity_title),
            subtitle = stringResource(R.string.step_01_subtitle),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.content_desc_back), tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        )

        AegisStepProgress(currentStep = 1)

        // Y8: contenido scrollable (evita recortes con teclado en pantallas pequeñas);
        // el botón queda fijo abajo.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {

        Spacer(modifier = Modifier.height(32.dp))

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

        Spacer(modifier = Modifier.height(Spacing.xl))

        Text(
            text = stringResource(R.string.configure_avatar_title),
            style = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp, // Un poco más pequeño para elegancia
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(Spacing.xxl))

        // Form Section
        AegisTextField(
            label = stringResource(R.string.username_label),
            value = name,
            onValueChange = { name = it },
            placeholder = stringResource(R.string.username_placeholder)
        )

        } // fin del contenido scrollable

        Spacer(modifier = Modifier.height(Spacing.lg))

        AegisPrimaryButton(
            text = stringResource(R.string.btn_continue),
            onClick = { if (name.isNotBlank()) onContinue(name) },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = Icons.Default.ArrowForward
        )
    }
}