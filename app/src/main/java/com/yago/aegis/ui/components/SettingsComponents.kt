package com.yago.aegis.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yago.aegis.BuildConfig
import com.yago.aegis.R
import com.yago.aegis.viewmodel.AuthViewModel
import com.yago.aegis.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsMenu(
    viewModel: ProfileViewModel,
    authViewModel: AuthViewModel? = null,
    onLogout: (() -> Unit)? = null,
    onAccountDeleted: (() -> Unit)? = null,
    onNavigateToPrivacy: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    val user = state.user

    var newMeasureName by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var tempName by remember(user.name) { mutableStateOf(user.name) }
    var tempHeight by remember(user.height) { mutableStateOf(user.height.toString()) }

    // Estados para diálogos
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) { e.printStackTrace() }
            viewModel.updateAvatar(it.toString())
        }
    }

    // Diálogo de cambio de contraseña
    if (showChangePasswordDialog && authViewModel != null) {
        ChangePasswordDialog(
            authViewModel = authViewModel,
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    // Diálogo de borrado de cuenta
    if (showDeleteAccountDialog && authViewModel != null) {
        DeleteAccountDialog(
            authViewModel = authViewModel,
            onDismiss = { showDeleteAccountDialog = false },
            onDeleted = {
                showDeleteAccountDialog = false
                onAccountDeleted?.invoke()
            }
        )
    }

    // Diálogo de confirmación de logout
    if (showLogoutDialog) {
        AegisAlertDialog(
            title = stringResource(R.string.logout_label),
            confirmText = stringResource(R.string.logout_label),
            dismissText = stringResource(R.string.btn_cancel),
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                authViewModel?.logout()
                onLogout?.invoke()
            },
            confirmButtonColor = MaterialTheme.colorScheme.error
        ) {
            Text(
                stringResource(R.string.logout_confirmation),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(text = stringResource(R.string.user_data_section_title))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .clickable { avatarLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }

            AegisTextField(
                label = stringResource(R.string.name_label),
                value = tempName,
                onValueChange = { newValue ->
                    tempName = newValue
                    viewModel.updateName(newValue)
                },
                placeholder = stringResource(R.string.name_placeholder),
                modifier = Modifier.weight(1f)
            )
        }

        AegisTextField(
            label = stringResource(R.string.label_height),
            value = tempHeight,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    tempHeight = newValue
                    newValue.toIntOrNull()?.let { viewModel.updateHeight(it.toDouble()) }
                }
            },
            placeholder = "185",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.sex_label),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        SexSelector(selected = user.sex, onSelect = { viewModel.updateSex(it) })

        VerticalDividerSection()

        // --- SECCIÓN CUENTA (solo si hay authViewModel) ---
        if (authViewModel != null) {
            SectionHeader(text = stringResource(R.string.account_section_title))
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Email del usuario
                    authViewModel.currentUserEmail?.let { email ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(email, color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                    }

                    // Cambiar contraseña (solo si usa email, no Google)
                    if (authViewModel.isEmailProvider) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showChangePasswordDialog = true }
                                .padding(vertical = 14.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.change_password_label), color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("›", color = MaterialTheme.colorScheme.secondary, fontSize = 18.sp)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                    }

                    // Cerrar sesión
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLogoutDialog = true }
                            .padding(vertical = 14.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.logout_label), color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))

                    // Borrar cuenta (requisito de Google Play)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeleteAccountDialog = true }
                            .padding(vertical = 14.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.delete_account_label), color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("›", color = MaterialTheme.colorScheme.secondary, fontSize = 18.sp)
                    }
                }
            }

            VerticalDividerSection()
        }

        // --- POLÍTICA DE PRIVACIDAD ---
        if (onNavigateToPrivacy != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPrivacy() }
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PrivacyTip, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.privacy_policy_label), color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("›", color = MaterialTheme.colorScheme.secondary, fontSize = 18.sp)
                }
            }

            VerticalDividerSection()
        }

        SectionHeader(text = stringResource(R.string.settings_title_interface))
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                SettingsRow(stringResource(R.string.settings_label_body_fat), state.showBodyFat) { viewModel.toggleBodyFat(it) }
                SettingsRow(stringResource(R.string.settings_label_bmi), state.showBMI) { viewModel.toggleBMI(it) }
                SettingsRow(stringResource(R.string.settings_label_visual_log), state.showVisualLog) { viewModel.toggleVisualLog(it) }
                SettingsRow(stringResource(R.string.settings_label_girths), state.showGirths) { viewModel.toggleGirths(it) }
            }
        }

        VerticalDividerSection()

        SectionHeader(text = stringResource(R.string.settings_title_manage_measures))

        state.customMeasures.forEach { measure ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = measure.name.uppercase(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.removeMeasure(measure.id) }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AegisTextField(
                    label = stringResource(R.string.new_metric_label),
                    value = newMeasureName,
                    onValueChange = { newMeasureName = it },
                    placeholder = stringResource(R.string.metric_placeholder),
                    modifier = Modifier.onFocusChanged {
                        if (it.isFocused) {
                            coroutineScope.launch {
                                delay(300)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                    }
                )
            }
            IconButton(
                onClick = {
                    if (newMeasureName.isNotBlank()) {
                        viewModel.addMeasure(newMeasureName)
                        newMeasureName = ""
                    }
                },
                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "AEGIS v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun ChangePasswordDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val errorCurrentPasswordRequired = stringResource(R.string.error_current_password_required)
    val errorNewPasswordLength = stringResource(R.string.error_new_password_length)
    val errorPasswordsMismatch = stringResource(R.string.error_passwords_mismatch)

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            delay(1500)
            onDismiss()
            authViewModel.clearState()
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss(); authViewModel.clearState() },
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(stringResource(R.string.change_password_dialog_title), color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (uiState.successMessage != null) {
                    Text(uiState.successMessage!!, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                } else {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it; localError = null },
                        label = { Text(stringResource(R.string.current_password_label), fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { currentVisible = !currentVisible }) {
                                Icon(if (currentVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            }
                        },
                        visualTransformation = if (currentVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; localError = null },
                        label = { Text(stringResource(R.string.new_password_label), fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { newVisible = !newVisible }) {
                                Icon(if (newVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            }
                        },
                        visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; localError = null },
                        label = { Text(stringResource(R.string.confirm_password_label), fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                    val error = localError ?: uiState.errorMessage
                    if (error != null) {
                        Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            if (uiState.successMessage == null) {
                TextButton(
                    onClick = {
                        localError = when {
                            currentPassword.isBlank() -> errorCurrentPasswordRequired
                            newPassword.length < 6 -> errorNewPasswordLength
                            newPassword != confirmPassword -> errorPasswordsMismatch
                            else -> null
                        }
                        if (localError == null) authViewModel.changePassword(currentPassword, newPassword)
                    },
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.btn_save), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                    }
                }
            }
        },
        dismissButton = {
            if (uiState.successMessage == null) {
                TextButton(onClick = { onDismiss(); authViewModel.clearState() }) {
                    Text(stringResource(R.string.btn_cancel), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun DeleteAccountDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit,
    onDeleted: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    val isEmailProvider = authViewModel.isEmailProvider
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss(); authViewModel.clearState() },
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                stringResource(R.string.delete_account_dialog_title),
                color = MaterialTheme.colorScheme.error,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.delete_account_warning),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                if (isEmailProvider) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.password_label), fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = MaterialTheme.colorScheme.error,
                            unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            cursorColor = MaterialTheme.colorScheme.error,
                            focusedLabelColor = MaterialTheme.colorScheme.error,
                            unfocusedLabelColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                } else {
                    Text(
                        stringResource(R.string.delete_account_google_note),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp
                    )
                }
                if (uiState.errorMessage != null) {
                    Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    authViewModel.deleteAccount(
                        currentPassword = if (isEmailProvider) password else null,
                        onDeleted = onDeleted
                    )
                },
                enabled = !uiState.isLoading && (!isEmailProvider || password.isNotBlank())
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.error, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.delete_account_confirm), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Black)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss(); authViewModel.clearState() }) {
                Text(stringResource(R.string.btn_cancel), color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp, fontSize = 11.sp)
    )
}

@Composable
fun VerticalDividerSection() {
    Spacer(modifier = Modifier.height(24.dp))
    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun SettingsRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            color = if (checked) Color.White else MaterialTheme.colorScheme.secondary,
            style = TextStyle(fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.5.sp),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        )
    }
}
