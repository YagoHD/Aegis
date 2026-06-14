package com.yago.aegis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R

/**
 * Política de privacidad mostrada dentro de la app. El texto canónico (mismo contenido)
 * está en PRIVACY_POLICY.md en la raíz del repo para poder publicarlo como URL pública
 * (requisito de la ficha de Google Play).
 */
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scroll)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.content_desc_back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.privacy_policy_title),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Última actualización: junio de 2026",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        PolicySection(
            "1. Datos que recogemos",
            "Para usar Aegis necesitas una cuenta (email y contraseña, o inicio de sesión con " +
                "Google). Además guardamos los datos que tú introduces: nombre y biografía, " +
                "métricas corporales (peso, altura, grasa, medidas personalizadas), fotos de " +
                "progreso, tus rutinas, ejercicios e historial de entrenamientos, y tus " +
                "preferencias de la app."
        )
        PolicySection(
            "2. Cómo usamos tus datos",
            "Usamos tus datos exclusivamente para prestarte el servicio: registrar tus " +
                "entrenamientos, mostrar tu progreso y sincronizar tu información entre " +
                "dispositivos. No vendemos tus datos a terceros."
        )
        PolicySection(
            "3. Dónde se almacenan",
            "Tus datos se guardan localmente en tu dispositivo y, si has iniciado sesión, se " +
                "sincronizan con Firebase (Google Cloud): autenticación con Firebase Auth y " +
                "datos con Cloud Firestore. Las fotos se guardan como referencias locales de tu " +
                "dispositivo."
        )
        PolicySection(
            "4. Servicios de terceros",
            "Aegis utiliza servicios de Google: Firebase Authentication, Cloud Firestore, " +
                "Firebase Analytics, Firebase Crashlytics y Google Sign-In. Estos servicios " +
                "pueden procesar datos según la política de privacidad de Google."
        )
        PolicySection(
            "5. Tus derechos",
            "Puedes acceder y modificar tus datos en cualquier momento desde la app. Puedes " +
                "borrar tu cuenta y todos tus datos asociados (rutinas, ejercicios, historial y " +
                "métricas) desde Ajustes → Cuenta → Borrar cuenta. Esta acción es permanente e " +
                "irreversible."
        )
        PolicySection(
            "6. Contacto",
            "Para cualquier duda sobre esta política o el tratamiento de tus datos, escríbenos a " +
                "tyagorbt@gmail.com."
        )

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.5.sp
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = body,
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 13.sp,
        lineHeight = 20.sp
    )
    Spacer(modifier = Modifier.height(20.dp))
}
