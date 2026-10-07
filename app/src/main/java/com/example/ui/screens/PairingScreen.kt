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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PairingCodeEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.FocusCrimson
import com.example.ui.theme.FocusEmerald
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.FocusPrimaryContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600

@Composable
fun PairingScreen(
    linkedStudents: List<UserEntity>,
    activePairingCode: PairingCodeEntity?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onPairCode: (code: String, onComplete: (Boolean, String?) -> Unit) -> Unit,
    onUnlinkStudent: (studentId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("pairing_screen")
    ) {
        // Back Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("pairing_back_button")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Pair Student Device",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Connect a Student account using a temporary pairing code",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Enter Pairing Code Form
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(FocusPrimaryContainer)
                    ) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = FocusPrimary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Enter Pairing Code",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "On the student device, open FocusLock > Student > 'Generate New Pairing Code'. Enter the 6-character code here:",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it.uppercase() },
                    label = { Text("Pairing Code (e.g. FL-8392)") },
                    placeholder = { Text("FL-XXXX") },
                    leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pairing_code_input")
                )

                // Quick Autofill chip if an active pairing code was generated by student
                if (activePairingCode != null && !activePairingCode.isUsed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detected active code: ${activePairingCode.code} (${activePairingCode.studentDisplayName})",
                            style = MaterialTheme.typography.labelSmall.copy(color = FocusEmerald)
                        )
                        OutlinedButton(
                            onClick = { inputCode = activePairingCode.code },
                            modifier = Modifier.testTag("autofill_detected_code_button")
                        ) {
                            Text("Use Code", fontSize = 11.sp)
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = errorMessage ?: "", color = FocusCrimson, style = MaterialTheme.typography.bodySmall)
                }

                if (successMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = successMessage ?: "", color = FocusEmerald, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        errorMessage = null
                        successMessage = null
                        if (inputCode.isBlank()) {
                            errorMessage = "Please enter a pairing code"
                            return@Button
                        }
                        onPairCode(inputCode) { success, msg ->
                            if (success) {
                                successMessage = msg
                                inputCode = ""
                            } else {
                                errorMessage = msg
                            }
                        }
                    },
                    enabled = !isLoading && inputCode.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_pair_code_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = PureWhite, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Link Student Device", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Currently Linked Students
        Text(
            text = "Currently Linked Students (${linkedStudents.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (linkedStudents.isEmpty()) {
            Text(
                text = "No students linked yet.",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                linkedStudents.forEach { student ->
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
                                        .background(FocusEmerald.copy(alpha = 0.15f))
                                ) {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = FocusEmerald)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = student.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "@${student.username}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Slate500)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onUnlinkStudent(student.userId) },
                                modifier = Modifier.testTag("unlink_student_${student.username}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Unlink",
                                    tint = FocusCrimson
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pairing Security Info Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = FocusEmerald)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Pairing codes expire automatically in 15 minutes, are single-use, and never expose parent passwords to students.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
            }
        }
    }
}
