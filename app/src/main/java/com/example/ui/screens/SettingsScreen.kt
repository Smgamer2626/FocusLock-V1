package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.service.PlatformPermissions
import com.example.ui.theme.FocusCrimson
import com.example.ui.theme.FocusEmerald
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700

@Composable
fun SettingsScreen(
    currentUser: UserEntity?,
    currentRole: UserRole,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onOpenPairing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showChangePassDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPermissionsExplainerDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }

    val hasUsageStats = remember { PlatformPermissions.hasUsageStatsPermission(context) }
    val hasOverlay = remember { PlatformPermissions.hasOverlayPermission(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        // Back Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Account & Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Security, permissions, and privacy controls",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Details Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentRole == UserRole.PARENT) FocusPrimary.copy(alpha = 0.15f) else FocusEmerald.copy(alpha = 0.15f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (currentRole == UserRole.PARENT) FocusPrimary else FocusEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = currentUser?.displayName ?: "User",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "@${currentUser?.username ?: ""} • Role: ${if (currentRole == UserRole.PARENT) "Parent / Guardian" else "Student"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = Slate200)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Account Password",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Hashed securely using SHA-256 + salt",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Slate500)
                        )
                    }
                    OutlinedButton(
                        onClick = { showChangePassDialog = true },
                        modifier = Modifier.testTag("change_password_button")
                    ) {
                        Text("Change", fontSize = 12.sp)
                    }
                }

                if (currentRole == UserRole.PARENT) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Device Pairing & Links",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Manage linked student devices",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Slate500)
                            )
                        }
                        OutlinedButton(
                            onClick = onOpenPairing,
                            modifier = Modifier.testTag("manage_pairing_button")
                        ) {
                            Text("Manage", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: OFFICIAL OS CAPABILITIES & PERMISSIONS
        Text(
            text = "Platform App-Blocking & Permissions",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Usage stats status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Usage Access (Digital Wellbeing)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Detects when blocked apps are in the foreground",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Slate500)
                        )
                    }
                    OutlinedButton(
                        onClick = { PlatformPermissions.openUsageAccessSettings(context) }
                    ) {
                        Text(if (hasUsageStats) "Granted" else "Configure", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(12.dp))

                // Display Over Other Apps
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Display Over Other Apps (Overlay)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Presents FocusLock Shield when blocked apps open",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Slate500)
                        )
                    }
                    OutlinedButton(
                        onClick = { PlatformPermissions.openOverlaySettings(context) }
                    ) {
                        Text(if (hasOverlay) "Granted" else "Configure", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = { showPermissionsExplainerDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Read Android App Blocking Disclosures & Policy")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: PRIVACY & DATA TRANSPARENCY
        Text(
            text = "Privacy & Student Rights",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PrivacyTip, contentDescription = null, tint = FocusEmerald)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "FocusLock Privacy Commitment",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• FocusLock does NOT read private messages, photos, files, or keystrokes.\n" +
                            "• FocusLock does NOT sell or monetize personal data.\n" +
                            "• The student always sees why an app is locked and how much time remains.\n" +
                            "• Real-time synchronization is strictly for study timer and early-end permissions.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600, lineHeight = 18.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showPrivacyPolicyDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Full Privacy & Transparency Policy")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // LOGOUT & DELETE ACCOUNT
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("logout_button")
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Logout")
            }

            Button(
                onClick = { showDeleteConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = FocusCrimson),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("delete_account_button")
            ) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Account")
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Android App Blocking Disclosures Dialog
    if (showPermissionsExplainerDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionsExplainerDialog = false },
            icon = { Icon(Icons.Default.Shield, contentDescription = null, tint = FocusPrimary) },
            title = { Text("Android Platform Disclosures") },
            text = {
                Column {
                    Text(
                        text = "Official Platform Mechanisms Only",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Under standard non-enterprise Android, third-party apps cannot unilaterally uninstall or kill other applications without Device Management (MDM).\n\n" +
                                "FocusLock uses official Usage Access (UsageStatsManager) and Display Over Other Apps (SYSTEM_ALERT_WINDOW) to detect when a blocked app is brought to the foreground and immediately present the FocusLock Shield.\n\n" +
                                "FocusLock strictly avoids rooting, accessibility abuses, malware, or exploiting operating system vulnerabilities.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPermissionsExplainerDialog = false }) {
                    Text("Understood")
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            icon = { Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = FocusEmerald) },
            title = { Text("FocusLock Privacy Guarantee") },
            text = {
                Column {
                    Text(
                        text = "1. Zero Surveillance: FocusLock is designed for student productivity, not surveillance.\n\n" +
                                "2. Protected Content: We never access message contents, photos, videos, contacts, or personal documents.\n\n" +
                                "3. Password Security: Passwords are salted and hashed cryptographically with SHA-256 and never stored in plain text.\n\n" +
                                "4. Data Sovereignty: You can unlink devices or delete your entire account at any time.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Change Password Dialog
    if (showChangePassDialog) {
        var oldPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var dialogErr by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePassDialog = false },
            title = { Text("Change Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password (min 6 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (dialogErr != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = dialogErr ?: "", color = FocusCrimson, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPass.length < 6) {
                            dialogErr = "Password must be at least 6 characters"
                        } else {
                            showChangePassDialog = false
                        }
                    }
                ) {
                    Text("Update Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePassDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete FocusLock Account?") },
            text = {
                Text("This will permanently delete your account, credentials, and remove all device pairings. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusCrimson)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
