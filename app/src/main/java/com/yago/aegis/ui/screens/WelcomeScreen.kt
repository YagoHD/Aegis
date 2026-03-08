package com.yago.aegis.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.ui.components.AegisStepProgress
import com.yago.aegis.ui.theme.AegisBronze
import com.yago.aegis.ui.theme.AegisCream
import com.yago.aegis.ui.theme.AegisSteel
import com.yago.aegis.ui.theme.AegisWhite
import com.yago.aegis.ui.theme.BackgroundBlackGrey
import com.yago.aegis.ui.theme.MatteBlack

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlackGrey) // Usamos el negro profundo
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AegisStepProgress(currentStep = 1)

            // Icono de Escudo Aegis
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = AegisBronze,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "AEGIS",
                style = TextStyle(
                    color = AegisWhite,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 8.sp
                )
            )

            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "\"Forja tu disciplina.\"",
                style = TextStyle(
                    color = AegisCream,
                    fontSize = 20.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Light
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botón Principal estilo "Get Started"
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MatteBlack),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AegisSteel)
            ) {
                Text(
                    text = "EMPIEZA YA",
                    color = AegisWhite,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}