package com.yago.aegis.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.text.KeyboardOptions
import com.yago.aegis.R
import com.yago.aegis.data.social.FriendRef
import com.yago.aegis.viewmodel.SocialFeedback
import com.yago.aegis.viewmodel.SocialViewModel

@Composable
fun FriendsScreen(viewModel: SocialViewModel, onBack: () -> Unit) {
    val myUsername by viewModel.myUsername.collectAsState()
    val buckets by viewModel.buckets.collectAsState()
    val feedback by viewModel.feedback.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val ctx = LocalContext.current

    LaunchedEffect(feedback) {
        feedback?.let {
            snackbar.showSnackbar(ctx.getString(feedbackRes(it)))
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.social_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp
                )
            }
            Spacer(Modifier.height(16.dp))

            val u = myUsername
            if (u == null) {
                ClaimSection(busy) { viewModel.claimUsername(it) }
            } else {
                Text(
                    "${stringResource(R.string.social_your_username)}:  @$u",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(20.dp))
                AddSection(busy) { viewModel.addFriend(it) }

                if (buckets.incoming.isNotEmpty()) {
                    SectionTitle(stringResource(R.string.social_requests_title))
                    buckets.incoming.forEach { ref ->
                        FriendRow(ref) {
                            TextButton(onClick = { viewModel.accept(ref.uid) }) {
                                Text(stringResource(R.string.social_accept), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { viewModel.reject(ref.uid) }) {
                                Text(stringResource(R.string.social_reject), color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }

                SectionTitle(stringResource(R.string.social_friends_title))
                if (buckets.friends.isEmpty()) {
                    Text(
                        stringResource(R.string.social_no_friends),
                        color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp, lineHeight = 18.sp
                    )
                } else {
                    buckets.friends.forEach { ref ->
                        FriendRow(ref) {
                            TextButton(onClick = { viewModel.remove(ref.uid) }) {
                                Text(stringResource(R.string.social_remove), color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ClaimSection(busy: Boolean, onClaim: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Text(
        stringResource(R.string.social_claim_title),
        color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp
    )
    Spacer(Modifier.height(6.dp))
    Text(
        stringResource(R.string.social_claim_subtitle),
        color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp, lineHeight = 18.sp
    )
    Spacer(Modifier.height(14.dp))
    UsernameField(text, R.string.social_username_hint) { text = it }
    Spacer(Modifier.height(14.dp))
    PrimaryButton(stringResource(R.string.social_claim_button), busy, enabled = text.isNotBlank()) { onClaim(text) }
}

@Composable
private fun AddSection(busy: Boolean, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    SectionTitle(stringResource(R.string.social_add_title))
    UsernameField(text, R.string.social_add_hint) { text = it }
    Spacer(Modifier.height(10.dp))
    PrimaryButton(stringResource(R.string.social_add_button), busy, enabled = text.isNotBlank()) {
        onAdd(text); text = ""
    }
}

@Composable
private fun SectionTitle(title: String) {
    Spacer(Modifier.height(24.dp))
    Text(title, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun FriendRow(ref: FriendRef, actions: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "@${ref.username.ifBlank { "…" }}",
            color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Bold
        )
        Row { actions() }
    }
}

@Composable
private fun UsernameField(value: String, hintRes: Int, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        prefix = { Text("@", color = MaterialTheme.colorScheme.secondary) },
        placeholder = { Text(stringResource(hintRes), color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)) },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun PrimaryButton(label: String, busy: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled && !busy,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = androidx.compose.ui.graphics.Color.Black,
            disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        )
    ) {
        if (busy) CircularProgressIndicator(modifier = Modifier.height(20.dp).width(20.dp), strokeWidth = 2.dp, color = androidx.compose.ui.graphics.Color.Black)
        else Text(label, fontWeight = FontWeight.Black, letterSpacing = 1.sp, fontSize = 13.sp)
    }
}

private fun feedbackRes(f: SocialFeedback): Int = when (f) {
    SocialFeedback.USERNAME_CLAIMED -> R.string.social_fb_claimed
    SocialFeedback.USERNAME_TAKEN -> R.string.social_fb_taken
    SocialFeedback.USERNAME_INVALID -> R.string.social_fb_invalid
    SocialFeedback.REQUEST_SENT -> R.string.social_fb_sent
    SocialFeedback.USER_NOT_FOUND -> R.string.social_fb_not_found
    SocialFeedback.CANNOT_ADD_SELF -> R.string.social_fb_self
    SocialFeedback.ALREADY_LINKED -> R.string.social_fb_already
    SocialFeedback.ERROR -> R.string.social_fb_error
}
