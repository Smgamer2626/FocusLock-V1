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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EarlyEndDecision
import com.example.data.model.FocusSessionEntity
import com.example.data.model.SessionStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.components.DeviceModeSwitcherBar
import com.example.ui.components.StatusPill
import com.example.ui.components.formatSecondsToTime
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.FocusAmberContainer
import com.example.ui.theme.FocusCrimson
import com.example.ui.theme.FocusCrimsonContainer
import com.example.ui.theme.FocusEmerald
import com.example.ui.theme.FocusEmeraldContainer
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.FocusPrimaryContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ParentDashboardScreen(
    currentUser: UserEntity?,
    linkedStudents: List<UserEntity>,
    selectedStudent: UserEntity?,
    activeSession: FocusSessionEntity?,
    remainingSeconds: Long,
    sessionProgress: Float,
    sessionHistory: List<FocusSessionEntity>,
    onSelectStudent: (UserEntity) -> Unit,
    onStartSessionClick: () -> Unit,
    onStopSessionClick: (String) -> Unit,
    onRespondEarlyEnd: (sessionId: String, approve: Boolean, note: String?) -> Unit,
    onPairStudentClick: () -> Unit,
    onViewHistoryClick: () -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showStopConfirmDialog by remember { mutableStateOf(false) }
    var showDenyNoteDialog by remember { mutableStateOf(false) }
    var denyNoteText by remember { mutableStateOf("") }

    val hasActiveSession = activeSession != null && activeSession.status == SessionStatus.ACTIVE
    val pendingEarlyEndRequest = hasActiveSession &&
            activeSession?.earlyEndRequested == true &&
            activeSession.earlyEndDecision == EarlyEndDecision.PENDING

    val blockedAppsList = remember(activeSession?.blockedAppsJson) {
        if (activeSession?.blockedAppsJson != null) {
            try {
                val arr = JSONArray(activeSession.blockedAppsJson)
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) list.add(arr.getString(i))
                list
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("parent_dashboard_screen")
    ) {
        // Device Mode Switcher Banner (allows testing Parent vs Student perspective in one click)
        DeviceModeSwitcherBar(
            currentRole = UserRole.PARENT,
            onSwitchRole = onSwitchRole
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Student Selector Header
        Text(
            text = "Linked Student Devices",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(linkedStudents) { student ->
                val isSelected = selectedStudent?.userId == student.userId
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) FocusPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, FocusPrimary) else null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectStudent(student) }
                        .testTag("student_chip_${student.username}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(FocusEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = student.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) FocusPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPairStudentClick() }
                        .testTag("pair_new_student_chip")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = FocusPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pair Student",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = FocusPrimary
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Student Device Status Bar
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = FocusPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Student: ${selectedStudent?.displayName ?: "No student paired"}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "@${selectedStudent?.username ?: "none"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }
                }

                StatusPill(
                    text = if (selectedStudent != null) "● Connected" else "● Disconnected",
                    isActive = selectedStudent != null,
                    color = if (selectedStudent != null) FocusEmerald else FocusCrimson,
                    containerColor = if (selectedStudent != null) FocusEmeraldContainer else FocusCrimsonContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PENDING EARLY END REQUEST ALERT CARD
        if (pendingEarlyEndRequest && activeSession != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FocusAmberContainer),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, FocusAmber),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pending_early_end_alert_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alert",
                            tint = FocusAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Early End Request Received",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = FocusAmber
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${activeSession.studentDisplayName} wants to end the current focus session early.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    if (!activeSession.earlyEndRequestReason.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Reason: \"${activeSession.earlyEndRequestReason}\"",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onRespondEarlyEnd(activeSession.sessionId, true, null)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FocusEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("parent_approve_early_end_button")
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Approve", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                showDenyNoteDialog = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FocusCrimson),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FocusCrimson),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("parent_deny_early_end_button")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Deny", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // CURRENT ACTIVE STUDY SESSION CARD
        if (hasActiveSession && activeSession != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("parent_active_session_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = FocusEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CURRENT SESSION",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = FocusEmerald
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FocusEmeraldContainer
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FocusEmerald
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "📚 ${activeSession.studyGoal}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "⏱ ${formatSecondsToTime(remainingSeconds)} remaining",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FocusEmerald
                                )
                            )
                            Text(
                                text = "Original Duration: ${activeSession.durationMinutes} min",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Blocked: ${blockedAppsList.size} apps",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FocusCrimson
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { sessionProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = FocusEmerald,
                        trackColor = Slate200
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Blocked apps tag preview
                    if (blockedAppsList.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = FocusCrimson,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = blockedAppsList.take(4).joinToString(", ") + if (blockedAppsList.size > 4) " +${blockedAppsList.size - 4} more" else "",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate600),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stop Session Button
                    Button(
                        onClick = { showStopConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusCrimson),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("parent_stop_session_button")
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stop Focus Session", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // NO ACTIVE SESSION: Start Session Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("no_active_session_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(FocusPrimaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = FocusPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Ready for Study Time",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "Configure a focused study session for ${selectedStudent?.displayName ?: "your student"} to block distracting apps.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onStartSessionClick,
                        colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = selectedStudent != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("parent_start_session_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Focus Session",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Session History Header & Preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Study History",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            TextButton(
                onClick = onViewHistoryClick,
                modifier = Modifier.testTag("view_all_history_text_button")
            ) {
                Text("View All")
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (sessionHistory.isEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No study sessions recorded yet. Start a session to track progress!",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sessionHistory.take(3).forEach { session ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (session.status == SessionStatus.COMPLETED)
                                                FocusEmeraldContainer
                                            else
                                                FocusAmberContainer
                                        )
                                ) {
                                    Icon(
                                        imageVector = if (session.status == SessionStatus.COMPLETED)
                                            Icons.Default.CheckCircle
                                        else
                                            Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = if (session.status == SessionStatus.COMPLETED)
                                            FocusEmerald
                                        else
                                            FocusAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = session.studyGoal,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${session.durationMinutes} min • ${dateFormat.format(Date(session.startTimeEpoch))}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Slate500)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (session.status == SessionStatus.COMPLETED)
                                    FocusEmeraldContainer
                                else
                                    FocusAmberContainer
                            ) {
                                Text(
                                    text = if (session.status == SessionStatus.COMPLETED) "Done" else "Ended Early",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (session.status == SessionStatus.COMPLETED)
                                            FocusEmerald
                                        else
                                            FocusAmber
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Stop Session Confirmation Dialog
    if (showStopConfirmDialog && activeSession != null) {
        AlertDialog(
            onDismissRequest = { showStopConfirmDialog = false },
            title = { Text("Stop Active Session?") },
            text = {
                Text("Are you sure you want to stop ${activeSession.studentDisplayName}'s focus session now? Blocked apps will immediately become available.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStopConfirmDialog = false
                        onStopSessionClick(activeSession.sessionId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusCrimson),
                    modifier = Modifier.testTag("confirm_stop_session_button")
                ) {
                    Text("Stop Session")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Deny Note Dialog
    if (showDenyNoteDialog && activeSession != null) {
        AlertDialog(
            onDismissRequest = { showDenyNoteDialog = false },
            title = { Text("Deny Early Termination") },
            text = {
                Column {
                    Text("You are keeping the session active for ${activeSession.studentDisplayName}. You can optionally include an encouraging note:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = denyNoteText,
                        onValueChange = { denyNoteText = it },
                        placeholder = { Text("e.g. 15 minutes left, finish the review problems!") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDenyNoteDialog = false
                        onRespondEarlyEnd(activeSession.sessionId, false, denyNoteText.ifBlank { null })
                        denyNoteText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusCrimson),
                    modifier = Modifier.testTag("confirm_deny_button")
                ) {
                    Text("Deny Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDenyNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
