package com.yago.aegis.ui.components
import com.yago.aegis.ui.theme.Spacing

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.data.SyncState

/**
 * Indicador discreto de sincronización (O13): compartido por Perfil, Stats e Historial.
 * Solo se muestra al sincronizar, en error o con datos locales sin confirmar (offline).
 * En Idle/Success no pinta nada.
 */
@Composable
fun SyncIndicator(syncState: SyncState, onRetry: () -> Unit) {
    when (syncState) {
        is SyncState.Syncing -> {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = stringResource(R.string.sync_syncing),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp
                )
            }
        }
        is SyncState.Error -> {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = stringResource(R.string.sync_error),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onRetry,
                    contentPadding = PaddingValues(horizontal = Spacing.sm, vertical = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sync_retry),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
        is SyncState.Cached -> {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudQueue,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = stringResource(R.string.sync_cached),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onRetry,
                    contentPadding = PaddingValues(horizontal = Spacing.sm, vertical = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sync_retry),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
        else -> { /* Idle / Success: discreto, no se muestra nada */ }
    }
}
