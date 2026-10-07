package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FocusSessionEntity
import com.example.data.model.SessionStatus
import com.example.data.model.UserRole
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
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionHistoryScreen(
    currentRole: UserRole,
    sessions: List<FocusSessionEntity>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "COMPLETED", "EARLY_END"

    val filteredSessions = remember(sessions, selectedFilter) {
        when (selectedFilter) {
            "COMPLETED" -> sessions.filter { it.status == SessionStatus.COMPLETED }
            "EARLY_END" -> sessions.filter { it.status == SessionStatus.ENDED_EARLY_APPROVED || it.status == SessionStatus.CANCELLED_BY_PARENT }
            else -> sessions
        }
    }

    val totalMinutes = remember(sessions) {
        sessions.filter { it.status == SessionStatus.COMPLETED }.sumOf { it.durationMinutes }
    }
    val completedCount = remember(sessions) {
        sessions.count { it.status == SessionStatus.COMPLETED }
    }
    val completionRate = remember(sessions) {
        if (sessions.isNotEmpty()) ((completedCount.toFloat() / sessions.size) * 100).toInt() else 0
    }

    val dateFormat = remember { SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
            .testTag("session_history_screen")
    ) {
        // Back Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("history_back_button")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Study History",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (currentRole == UserRole.PARENT)
                        "Track student focus performance & completion"
                    else
                        "Your completed study sessions",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Analytics Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Focus Time",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${totalMinutes / 60}h ${totalMinutes % 60}m",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = FocusEmerald
                        )
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$completedCount sessions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = FocusPrimary
                        )
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Success Rate",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$completionRate%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = FocusAmber
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All (${sessions.size})") }
            )
            FilterChip(
                selected = selectedFilter == "COMPLETED",
                onClick = { selectedFilter = "COMPLETED" },
                label = { Text("Completed ($completedCount)") }
            )
            FilterChip(
                selected = selectedFilter == "EARLY_END",
                onClick = { selectedFilter = "EARLY_END" },
                label = { Text("Ended Early (${sessions.size - completedCount})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredSessions.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No study sessions match this filter",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Slate500)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredSessions) { session ->
                    val isDone = session.status == SessionStatus.COMPLETED
                    val blockedApps = try {
                        val arr = JSONArray(session.blockedAppsJson)
                        val list = mutableListOf<String>()
                        for (i in 0 until arr.length()) list.add(arr.getString(i))
                        list
                    } catch (_: Exception) {
                        emptyList()
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_item_${session.sessionId}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDone) FocusEmeraldContainer else FocusAmberContainer
                                            )
                                    ) {
                                        Icon(
                                            imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.HourglassBottom,
                                            contentDescription = null,
                                            tint = if (isDone) FocusEmerald else FocusAmber,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = (if (isDone) "✓ " else "• ") + session.studyGoal,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = dateFormat.format(Date(session.startTimeEpoch)),
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Slate500)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isDone) FocusEmeraldContainer else FocusAmberContainer
                                ) {
                                    Text(
                                        text = if (isDone) "Completed" else "Ended Early",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDone) FocusEmerald else FocusAmber
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Duration: ${session.durationMinutes} minutes",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )

                                Text(
                                    text = "Student: ${session.studentDisplayName}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                                )
                            }

                            if (blockedApps.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = null,
                                        tint = FocusCrimson,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Blocked: " + blockedApps.joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Slate600),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (!session.earlyEndDecisionNote.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Parent note: \"${session.earlyEndDecisionNote}\"",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Slate500)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
