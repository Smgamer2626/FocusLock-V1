package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockedAppInfo
import com.example.data.model.BlockedAppsCatalog
import com.example.data.model.EarlyEndDecision
import com.example.data.model.FocusSessionEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.components.DeviceModeSwitcherBar
import com.example.ui.components.formatSecondsToTime
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.FocusAmberContainer
import com.example.ui.theme.FocusCrimson
import com.example.ui.theme.FocusCrimsonContainer
import com.example.ui.theme.FocusEmerald
import com.example.ui.theme.FocusEmeraldContainer
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import org.json.JSONArray

@Composable
fun StudentFocusScreen(
    currentStudent: UserEntity?,
    activeSession: FocusSessionEntity,
    remainingSeconds: Long,
    sessionProgress: Float,
    onRequestEarlyEnd: (reason: String?) -> Unit,
    onTestShieldIntercept: (BlockedAppInfo) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRequestDialog by remember { mutableStateOf(false) }
    var requestReason by remember { mutableStateOf("") }
    var showBlockedAppsDialog by remember { mutableStateOf(false) }

    val blockedAppsList = remember(activeSession.blockedAppsJson) {
        try {
            val arr = JSONArray(activeSession.blockedAppsJson)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    val isEarlyEndPending = activeSession.earlyEndRequested && activeSession.earlyEndDecision == EarlyEndDecision.PENDING
    val isEarlyEndDenied = activeSession.earlyEndDecision == EarlyEndDecision.DENIED

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp)
            .testTag("student_focus_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Device Mode Switcher Banner (to easily switch back to Parent view)
        DeviceModeSwitcherBar(
            currentRole = UserRole.STUDENT,
            onSwitchRole = onSwitchRole
        )

        Spacer(modifier = Modifier.height(20.dp))

        // FOCUS SESSION ACTIVE Pulsing Banner
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FocusEmerald.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, FocusEmerald.copy(alpha = 0.5f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(FocusEmerald)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FOCUS SESSION ACTIVE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = FocusEmerald
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Study Goal / Subject Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = FocusEmerald,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = activeSession.studyGoal,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // MASSIVE COUNTDOWN CIRCLE & TIMER
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(6.dp, FocusEmerald.copy(alpha = 0.2f), CircleShape)
                .testTag("student_timer_display_container")
        ) {
            // Circular Progress Indicator behind timer
            CircularProgressIndicator(
                progress = { (1f - sessionProgress).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 10.dp,
                color = FocusEmerald,
                trackColor = Color.Transparent
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatSecondsToTime(remainingSeconds),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "remaining",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Slate500
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "of ${activeSession.durationMinutes} min",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Motivational Card: “Stay focused. You’ve got this!”
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "“Stay focused. You’ve got this!”",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = FocusEmerald
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Session controlled by ${activeSession.parentDisplayName}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Blocked Apps Card with Click to Inspect
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { showBlockedAppsDialog = true }
                .testTag("student_blocked_apps_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FocusCrimsonContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = FocusCrimson,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Blocked Apps: ${blockedAppsList.size}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Tap to view full restricted list",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FocusCrimson
                ) {
                    Text(
                        text = "ENFORCED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pending Request or Denied Notification Card
        if (isEarlyEndPending) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = FocusAmberContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, FocusAmber),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = FocusAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Early End Request Pending",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = FocusAmber)
                        )
                        Text(
                            text = "Waiting for ${activeSession.parentDisplayName} to approve on their device...",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isEarlyEndDenied) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = FocusCrimsonContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, FocusCrimson),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = FocusCrimson,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Early End Request Denied",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = FocusCrimson)
                        )
                        val note = activeSession.earlyEndDecisionNote ?: "Keep going! Finish your study goal."
                        Text(
                            text = "${activeSession.parentDisplayName}: \"$note\"",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Test App Shield Intercept Simulator Button
        OutlinedButton(
            onClick = {
                val sampleApp = BlockedAppsCatalog.defaultCatalog.firstOrNull { blockedAppsList.contains(it.name) }
                    ?: BlockedAppsCatalog.defaultCatalog.first()
                onTestShieldIntercept(sampleApp)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("test_app_shield_button")
        ) {
            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = FocusCrimson)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simulate Opening Blocked App (Test Shield)")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // [ Request to End Session ] Button (Student CANNOT end directly!)
        Button(
            onClick = { showRequestDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = FocusAmber),
            shape = RoundedCornerShape(14.dp),
            enabled = !isEarlyEndPending,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("student_request_end_session_button")
        ) {
            Icon(imageVector = Icons.Default.QuestionAnswer, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isEarlyEndPending) "Request Pending Approval..." else "Request to End Session",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // EARLY TERMINATION REQUEST DIALOG (as required in spec)
    if (showRequestDialog) {
        AlertDialog(
            onDismissRequest = { showRequestDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = FocusAmber,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Focus session is still active.",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "“Ask your Parent/Guardian to approve ending this session.”",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "FocusLock requires parent authorization so that students can maintain their study commitments.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = requestReason,
                        onValueChange = { requestReason = it },
                        label = { Text("Reason (optional)") },
                        placeholder = { Text("e.g. Finished all exercises early") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRequestDialog = false
                        onRequestEarlyEnd(requestReason.ifBlank { null })
                        requestReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusAmber),
                    modifier = Modifier.testTag("confirm_request_permission_button")
                ) {
                    Text("Request Permission", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Blocked Apps Inspector Dialog
    if (showBlockedAppsDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedAppsDialog = false },
            title = { Text("Blocked Applications") },
            text = {
                Column {
                    Text(
                        text = "These apps are locked during the focus session:",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    blockedAppsList.forEach { appName ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = FocusCrimson,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = appName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBlockedAppsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
