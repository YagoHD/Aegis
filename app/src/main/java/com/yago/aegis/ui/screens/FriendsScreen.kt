package com.yago.aegis.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.R
import com.yago.aegis.data.RankTier
import com.yago.aegis.data.social.FriendRef
import com.yago.aegis.ui.components.AegisAvatar
import com.yago.aegis.ui.components.RankMedal
import com.yago.aegis.viewmodel.MyRank
import com.yago.aegis.viewmodel.SocialFeedback
import com.yago.aegis.viewmodel.SocialViewModel

@Composable
fun FriendsScreen(viewModel: SocialViewModel, onBack: () -> Unit) {
    val myUsername by viewModel.myUsername.collectAsState()
    val buckets by viewModel.buckets.collectAsState()
    val ranking by viewModel.friendRanking.collectAsState()
    val myRank by viewModel.myRank.collectAsState()
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
    // Sube mi perfil (nivel/rango frescos) y trae los perfiles de mis amigos (para sus medallas).
    LaunchedEffect(buckets.friends, myUsername) { viewModel.loadRanking() }

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
                MyProfileCard(u, myRank)
                Spacer(Modifier.height(20.dp))
                AddAllySection(busy) { viewModel.addFriend(it) }

                if (buckets.incoming.isNotEmpty()) {
                    SectionTitle(stringResource(R.string.social_requests_title), buckets.incoming.size)
                    buckets.incoming.forEach { ref ->
                        RequestCard(
                            ref = ref,
                            onAccept = { viewModel.accept(ref.uid) },
                            onReject = { viewModel.reject(ref.uid) }
                        )
                    }
                }

                SectionTitle(stringResource(R.string.social_friends_title), buckets.friends.size)
                if (buckets.friends.isEmpty()) {
                    Text(
                        stringResource(R.string.social_no_friends),
                        color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp, lineHeight = 18.sp
                    )
                } else {
                    val tiers = ranking.profiles.associate { it.uid to tierOf(it.overallTier) }
                    buckets.friends.forEach { ref ->
                        FriendListRow(ref, tiers[ref.uid] ?: RankTier.SIN_RANGO) { viewModel.remove(ref.uid) }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

/** Tarjeta de mi identidad social: avatar + @usuario + nivel + medalla de rango global. */
@Composable
private fun MyProfileCard(username: String, rank: MyRank) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AegisAvatar(username, 60.dp, MaterialTheme.colorScheme.primary, 2.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "@${username.uppercase()}",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 17.sp, fontWeight = FontWeight.Black, maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(R.string.ranking_level, rank.level),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp
                )
            }
            RankMedal(rank.overall, 48.dp)
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
private fun AddAllySection(busy: Boolean, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    SectionTitle(stringResource(R.string.social_add_title))
    UsernameField(text, R.string.social_add_hint) { text = it }
    Spacer(Modifier.height(10.dp))
    PrimaryButton(stringResource(R.string.social_add_button), busy, enabled = text.isNotBlank()) {
        onAdd(text); text = ""
    }
}

@Composable
private fun SectionTitle(title: String, count: Int = 0) {
    Spacer(Modifier.height(24.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        if (count > 0) {
            Spacer(Modifier.width(8.dp))
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                Text(
                    "$count",
                    color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp)
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
}

/** Solicitud recibida: avatar + @usuario + Aceptar (bronce) / Rechazar (contorno). */
@Composable
private fun RequestCard(ref: FriendRef, onAccept: () -> Unit, onReject: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AegisAvatar(ref.username.ifBlank { "?" }, 40.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                Spacer(Modifier.width(12.dp))
                Text(
                    "@${ref.username.ifBlank { "…" }}",
                    color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                ) {
                    Text(stringResource(R.string.social_accept), fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 0.5.sp)
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                ) {
                    Text(stringResource(R.string.social_reject), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.5.sp)
                }
            }
        }
    }
}

/** Amigo aceptado: avatar + @usuario + medalla de su rango global + eliminar. */
@Composable
private fun FriendListRow(ref: FriendRef, tier: RankTier, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AegisAvatar(ref.username.ifBlank { "?" }, 44.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
        Spacer(Modifier.width(12.dp))
        Text(
            "@${ref.username.ifBlank { "…" }}",
            color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Black,
            maxLines = 1, modifier = Modifier.weight(1f)
        )
        RankMedal(tier, 34.dp)
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.social_remove),
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
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
            contentColor = Color.Black,
            disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
        )
    ) {
        if (busy) CircularProgressIndicator(modifier = Modifier.height(20.dp).width(20.dp), strokeWidth = 2.dp, color = Color.Black)
        else Text(label, fontWeight = FontWeight.Black, letterSpacing = 1.sp, fontSize = 13.sp)
    }
}

private fun tierOf(name: String): RankTier =
    runCatching { RankTier.valueOf(name) }.getOrDefault(RankTier.SIN_RANGO)

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
