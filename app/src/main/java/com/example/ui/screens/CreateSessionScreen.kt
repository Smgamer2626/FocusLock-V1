package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockedAppInfo
import com.example.data.model.BlockedAppsCatalog
import com.example.data.model.UserEntity
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateSessionScreen(
    targetStudent: UserEntity?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onStartSession: (studyGoal: String, durationMinutes: Int, blockedApps: List<String>, onComplete: (Boolean, String?) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var studyGoal by remember { mutableStateOf("Mathematics Homework") }
    var durationMinutes by remember { mutableIntStateOf(45) }
    var isCustomDuration by remember { mutableStateOf(false) }

    // Apps selected to block - default to standard distraction catalog
    val selectedApps = remember {
        mutableStateListOf(
            "YouTube",
            "Instagram",
            "TikTok",
            "Snapchat",
            "Netflix",
            "Discord",
            "Reddit",
            "Roblox"
        )
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    val quickGoals = listOf(
        "Mathematics Homework",
        "Science Exam Prep",
        "History Research Paper",
        "Reading & Literature",
        "Coding Practice",
        "Language Learning"
    )

    val durationPresets = listOf(15, 30, 45, 60, 120)

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("create_session_screen")
    ) {
        // Top Back & Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("create_session_back_button")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "New Focus Session",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Target Student: ${targetStudent?.displayName ?: "No student"}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 1: STUDY GOAL
        Text(
            text = "1. Study Goal / Subject",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Specify what the student will be focusing on.",
            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = studyGoal,
            onValueChange = { studyGoal = it },
            label = { Text("Study Goal (e.g. Math Homework)") },
            leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null, tint = FocusPrimary) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("study_goal_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick goal recommendation chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            quickGoals.forEach { goal ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (studyGoal == goal) FocusPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { studyGoal = goal }
                ) {
                    Text(
                        text = goal,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (studyGoal == goal) FocusPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (studyGoal == goal) FontWeight.Bold else FontWeight.Normal
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 2: DURATION PRESETS
        Text(
            text = "2. Session Duration",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Choose a preset duration or customize.",
            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            durationPresets.forEach { mins ->
                val label = if (mins >= 60) "${mins / 60} hr" else "$mins m"
                val isSelected = !isCustomDuration && durationMinutes == mins

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) FocusPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            isCustomDuration = false
                            durationMinutes = mins
                        }
                        .testTag("duration_preset_${mins}m")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) PureWhite else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom duration stepper
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Selected Duration: $durationMinutes minutes",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            isCustomDuration = true
                            if (durationMinutes > 5) durationMinutes -= 5
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                    }

                    Text(
                        text = "$durationMinutes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = {
                            isCustomDuration = true
                            if (durationMinutes < 360) durationMinutes += 5
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 3: APPS TO BLOCK
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "3. Distracting Apps to Block",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${selectedApps.size} apps selected for FocusLock Shield",
                    style = MaterialTheme.typography.bodySmall.copy(color = FocusCrimson)
                )
            }

            Row {
                TextButton(
                    onClick = {
                        if (selectedApps.size == BlockedAppsCatalog.defaultCatalog.size) {
                            selectedApps.clear()
                        } else {
                            selectedApps.clear()
                            selectedApps.addAll(BlockedAppsCatalog.defaultCatalog.map { it.name })
                        }
                    },
                    modifier = Modifier.testTag("toggle_all_apps_button")
                ) {
                    Text(if (selectedApps.size == BlockedAppsCatalog.defaultCatalog.size) "Deselect All" else "Select All")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid of popular apps
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BlockedAppsCatalog.defaultCatalog.forEach { app ->
                val isBlocked = selectedApps.contains(app.name)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isBlocked) FocusCrimsonContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isBlocked) FocusCrimson.copy(alpha = 0.5f) else Slate200
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (isBlocked) {
                                selectedApps.remove(app.name)
                            } else {
                                selectedApps.add(app.name)
                            }
                        }
                        .testTag("block_app_item_${app.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isBlocked) FocusCrimson else Slate400)
                            ) {
                                Icon(
                                    imageVector = if (isBlocked) Icons.Default.Block else Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = app.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isBlocked) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = app.category,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Slate500)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (isBlocked) FocusCrimson else Color.Transparent,
                            border = if (!isBlocked) androidx.compose.foundation.BorderStroke(1.5.dp, Slate400) else null,
                            modifier = Modifier.size(22.dp)
                        ) {
                            if (isBlocked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = PureWhite,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Transparency & Security Notice
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = FocusEmerald,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Timer is synchronized via absolute epoch timestamp so changing device clock cannot bypass FocusLock.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage ?: "",
                color = FocusCrimson,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // START BUTTON
        Button(
            onClick = {
                errorMessage = null
                if (studyGoal.isBlank()) {
                    errorMessage = "Please enter a study goal"
                    return@Button
                }
                if (selectedApps.isEmpty()) {
                    errorMessage = "Please select at least 1 app to block"
                    return@Button
                }
                onStartSession(studyGoal, durationMinutes, selectedApps.toList()) { success, err ->
                    if (!success) {
                        errorMessage = err
                    }
                }
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("submit_start_focus_session_button")
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = PureWhite, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            } else {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START FOCUS SESSION",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
