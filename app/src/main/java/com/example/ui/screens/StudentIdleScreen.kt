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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockedAppInfo
import com.example.data.model.BlockedAppsCatalog
import com.example.data.model.FocusSessionEntity
import com.example.data.model.PairingCodeEntity
import com.example.data.model.SessionStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.components.DeviceModeSwitcherBar
import com.example.ui.components.StatusPill
import com.example.ui.components.formatSecondsToTime
import com.example.ui.theme.FocusAmber
import com.example.ui.theme.FocusAmberContainer
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StudentIdleScreen(
    currentStudent: UserEntity?,
    pairingCode: PairingCodeEntity?,
    pairingCodeRemainingSeconds: Long,
    sessionHistory: List<FocusSessionEntity>,
    isLoading: Boolean,
    onGeneratePairingCode: () -> Unit,
    onViewHistory: () -> Unit,
    onTestShield: (BlockedAppInfo) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isLinked = currentStudent?.linkedParentId != null

    val totalCompletedMinutes = remember(sessionHistory) {
        sessionHistory.filter { it.status == SessionStatus.COMPLETED }.sumOf { it.durationMinutes }
    }
    val completedCount = remember(sessionHistory) {
        sessionHistory.count { it.status == SessionStatus.COMPLETED }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("student_idle_screen")
    ) {
        // Device Mode Switcher Banner
        DeviceModeSwitcherBar(
            currentRole = UserRole.STUDENT,
            onSwitchRole = onSwitchRole
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Student Profile & Connection Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(FocusEmeraldContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = FocusEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = currentStudent?.displayName ?: "Student",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isLinked) "Linked to Parent Device" else "Not Linked to Parent",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }
                }

                StatusPill(
                    text = if (isLinked) "● Paired" else "● Unpaired",
                    isActive = isLinked,
                    color = if (isLinked) FocusEmerald else FocusAmber,
                    containerColor = if (isLinked) FocusEmeraldContainer else FocusAmberContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PAIRING CODE CARD (Temporary 6-digit code for Parent linking)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("student_pairing_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = FocusPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Device Pairing Code",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Generate a secure temporary code and enter it on the Parent device to connect.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (pairingCode != null && !pairingCode.isUsed && pairingCodeRemainingSeconds > 0) {
                    // Display Active Code
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FocusPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = pairingCode.code,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 6.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = FocusPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⏱ Code expires in ${formatSecondsToTime(pairingCodeRemainingSeconds)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = FocusAmber
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Ask your Parent to enter this code on their dashboard under 'Pair Student'.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Slate500),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Button(
                        onClick = onGeneratePairingCode,
                        colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_pairing_code_button")
                    ) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate New Pairing Code", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // STUDY METRICS CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Your Focus Achievements",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${totalCompletedMinutes / 60}h ${totalCompletedMinutes % 60}m",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = FocusEmerald
                            )
                        )
                        Text(
                            text = "Total Focus Time",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "$completedCount",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = FocusPrimary
                            )
                        )
                        Text(
                            text = "Sessions Completed",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shield Tester Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = FocusEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FocusLock Shield Status",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "No focus session is active right now. All applications are available. You can test the shield preview below:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        val yt = BlockedAppsCatalog.getAppByName("YouTube") ?: BlockedAppsCatalog.defaultCatalog.first()
                        onTestShield(yt)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Preview Shield Lock Screen")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Session History Header & Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Past Study Sessions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            TextButton(
                onClick = onViewHistory,
                modifier = Modifier.testTag("student_view_history_button")
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
            Text(
                text = "No sessions recorded yet.",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
            sessionHistory.take(3).forEach { session ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = session.studyGoal,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${session.durationMinutes} min • ${dateFormat.format(Date(session.startTimeEpoch))}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Slate500)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (session.status == SessionStatus.COMPLETED)
                                FocusEmeraldContainer
                            else
                                FocusAmberContainer
                        ) {
                            Text(
                                text = if (session.status == SessionStatus.COMPLETED) "Completed" else "Ended Early",
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

        Spacer(modifier = Modifier.height(24.dp))
    }
}
