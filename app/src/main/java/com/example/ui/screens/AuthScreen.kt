package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.security.SecurityUtils
import com.example.ui.components.PasswordStrengthMeter
import com.example.ui.theme.FocusCrimson
import com.example.ui.theme.FocusEmerald
import com.example.ui.theme.FocusPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate500

@Composable
fun AuthScreen(
    currentRole: UserRole,
    isLoading: Boolean,
    onLogin: (username: String, pass: String, role: UserRole, onComplete: (Boolean, String?) -> Unit) -> Unit,
    onRegister: (username: String, pass: String, displayName: String, role: UserRole, securityAnswer: String, onComplete: (Boolean, String?) -> Unit) -> Unit,
    onResetPassword: (username: String, answer: String, newPass: String, onComplete: (Boolean, String?) -> Unit) -> Unit,
    onBackToRoleSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Create Account, 2: Forgot Password

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var securityAnswer by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var rememberDevice by remember { mutableStateOf(true) }

    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val isParent = currentRole == UserRole.PARENT
    val themeColor = if (isParent) FocusPrimary else FocusEmerald

    val passwordStrength = remember(password, isParent) {
        SecurityUtils.evaluatePasswordStrength(password, isParent)
    }

    val newPasswordStrength = remember(newPassword, isParent) {
        SecurityUtils.evaluatePasswordStrength(newPassword, isParent)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(24.dp)
            .testTag("auth_screen")
    ) {
        // Back Button & Role Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackToRoleSelect,
                modifier = Modifier.testTag("auth_back_to_role_button")
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (isParent) "Parent / Guardian Portal" else "Student Portal",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "FocusLock Authentication",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.testTag("auth_tab_row")
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                    errorMessage = null
                    successMessage = null
                },
                text = { Text("Login", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("auth_tab_login")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    errorMessage = null
                    successMessage = null
                },
                text = { Text("Create Account", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("auth_tab_create_account")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = {
                    selectedTab = 2
                    errorMessage = null
                    successMessage = null
                },
                text = { Text("Reset", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("auth_tab_forgot_password")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Demo Autofill Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isParent) "Quick Demo: Sarah (Guardian)" else "Quick Demo: Alex (Student)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isParent) "Pre-seeded Parent account" else "Pre-seeded Student account",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Slate500)
                    )
                }
                OutlinedButton(
                    onClick = {
                        if (isParent) {
                            username = "sarah_parent"
                            password = "FocusParent#1"
                            displayName = "Sarah (Guardian)"
                            securityAnswer = "lincoln high"
                        } else {
                            username = "alex_student"
                            password = "StudyAlex#1"
                            displayName = "Alex"
                            securityAnswer = "lincoln high"
                        }
                    },
                    modifier = Modifier.testTag("autofill_demo_credentials_button")
                ) {
                    Text("Autofill Demo", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error / Success Message
        AnimatedVisibility(visible = errorMessage != null) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        AnimatedVisibility(visible = successMessage != null) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = FocusEmerald.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = successMessage ?: "",
                    color = FocusEmerald,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // TAB 0: LOGIN
        if (selectedTab == 0) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_username_field")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_field")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = rememberDevice,
                    onCheckedChange = { rememberDevice = it },
                    modifier = Modifier.testTag("remember_device_checkbox")
                )
                Text(
                    text = "Remember this device",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    errorMessage = null
                    onLogin(username, password, currentRole) { success, err ->
                        if (!success) {
                            errorMessage = err ?: "Login failed"
                        }
                    }
                },
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = PureWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Login to FocusLock", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.testTag("forgot_password_button")
                ) {
                    Text("Forgot Password?")
                }
            }
        }

        // TAB 1: CREATE ACCOUNT
        if (selectedTab == 1) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name (e.g. Alex, Mom)") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_display_name_field")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username (min 3 characters)") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_username_field")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(if (isParent) "Strong Password (min 8 chars)" else "Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_password_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            PasswordStrengthMeter(strength = passwordStrength)

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = securityAnswer,
                onValueChange = { securityAnswer = it },
                label = { Text("Security: What was your first school?") },
                placeholder = { Text("Answer used for password recovery") },
                leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_security_answer_field")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    errorMessage = null
                    onRegister(username, password, displayName, currentRole, securityAnswer) { success, err ->
                        if (!success) {
                            errorMessage = err ?: "Account creation failed"
                        }
                    }
                },
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank() && securityAnswer.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("register_submit_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = PureWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create Account", fontWeight = FontWeight.Bold)
                }
            }
        }

        // TAB 2: FORGOT PASSWORD
        if (selectedTab == 2) {
            Text(
                text = "Account Recovery",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Verify your security question to reset your FocusLock password.",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Your Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_username_field")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = securityAnswer,
                onValueChange = { securityAnswer = it },
                label = { Text("Security: What was your first school?") },
                leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_security_answer_field")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_new_password_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            PasswordStrengthMeter(strength = newPasswordStrength)

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    errorMessage = null
                    onResetPassword(username, securityAnswer, newPassword) { success, msg ->
                        if (success) {
                            successMessage = msg
                            selectedTab = 0
                        } else {
                            errorMessage = msg
                        }
                    }
                },
                enabled = !isLoading && username.isNotBlank() && securityAnswer.isNotBlank() && newPassword.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("reset_password_submit_button")
            ) {
                Text("Reset Password", fontWeight = FontWeight.Bold)
            }
        }
    }
}
