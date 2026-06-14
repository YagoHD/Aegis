package com.yago.aegis.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun EmailVerificationScreen(
    authViewModel: AuthViewModel,
    email: String?,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    var resendCooldown by remember { mutableStateOf(0) }

    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000)
            resendCooldown--
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onVerified()
    }

    val cooldownText = if (resendCooldown > 0) {
        "%d:%02d".format(resendCooldown / 60, resendCooldown % 60)
    } else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (resendCooldown > 0) Icons.Default.MarkEmailRead else Icons.Default.Email,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(R.string.verify_account_title),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.verification_email_sent),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = email ?: "tu correo electrónico",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.verification_instructions),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        if (uiState.errorMessage != null && uiState.needsEmailVerification) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = { authViewModel.checkEmailVerified(onVerified) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.btn_verified), color = Color.Black, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                authViewModel.sendVerificationEmail()
                resendCooldown = 300
            },
            enabled = resendCooldown == 0 && !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (resendCooldown == 0) MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            )
        ) {
            Text(
                text = if (cooldownText != null) stringResource(R.string.resend_cooldown_label, cooldownText) else stringResource(R.string.btn_resend_email),
                color = if (resendCooldown == 0) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onBack) {
            Text(stringResource(R.string.use_another_account), color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
        }
    }
}
