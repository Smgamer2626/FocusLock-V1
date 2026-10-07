package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.model.UserRole
import com.example.ui.components.FocusLockTopBar
import com.example.ui.components.ShieldInterceptOverlay
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CreateSessionScreen
import com.example.ui.screens.PairingScreen
import com.example.ui.screens.ParentDashboardScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.SessionHistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudentFocusScreen
import com.example.ui.screens.StudentIdleScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.FocusLockViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FocusLockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                FocusLockAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FocusLockAppContent(viewModel: FocusLockViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val deviceRole by viewModel.deviceRole.collectAsState()
    val linkedStudents by viewModel.linkedStudents.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val sessionProgress by viewModel.sessionProgress.collectAsState()
    val pairingCodeEntity by viewModel.pairingCodeEntity.collectAsState()
    val pairingCodeRemainingSeconds by viewModel.pairingCodeSecondsRemaining.collectAsState()
    val sessionHistory by viewModel.sessionHistory.collectAsState()
    val shieldInterceptApp by viewModel.shieldInterceptApp.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle feedback toasts/snackbars
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    // Handle System Back Button
    BackHandler(enabled = currentScreen != AppScreen.PARENT_DASHBOARD && currentScreen != AppScreen.STUDENT_DASHBOARD && currentScreen != AppScreen.ROLE_SELECTION) {
        viewModel.navigateBack()
    }

    val showTopBar = currentUser != null &&
            currentScreen != AppScreen.ROLE_SELECTION &&
            currentScreen != AppScreen.AUTH

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (showTopBar) {
                    FocusLockTopBar(
                        currentUser = currentUser,
                        currentRole = deviceRole,
                        onSwitchRole = { viewModel.switchDeviceRole(it) },
                        onOpenHistory = { viewModel.navigateTo(AppScreen.SESSION_HISTORY) },
                        onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        onLogout = { viewModel.logout() }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        AppScreen.ROLE_SELECTION -> {
                            RoleSelectionScreen(
                                onSelectRole = { role ->
                                    viewModel.selectRoleForAuth(role)
                                }
                            )
                        }

                        AppScreen.AUTH -> {
                            AuthScreen(
                                currentRole = deviceRole,
                                isLoading = isLoading,
                                onLogin = { username, pass, role, onComplete ->
                                    viewModel.login(username, pass, role, onComplete)
                                },
                                onRegister = { username, pass, displayName, role, answer, onComplete ->
                                    viewModel.register(username, pass, displayName, role, answer, onComplete)
                                },
                                onResetPassword = { username, answer, newPass, onComplete ->
                                    viewModel.resetPassword(username, answer, newPass, onComplete)
                                },
                                onBackToRoleSelect = {
                                    viewModel.navigateTo(AppScreen.ROLE_SELECTION)
                                }
                            )
                        }

                        AppScreen.PARENT_DASHBOARD -> {
                            ParentDashboardScreen(
                                currentUser = currentUser,
                                linkedStudents = linkedStudents,
                                selectedStudent = selectedStudent,
                                activeSession = activeSession,
                                remainingSeconds = remainingSeconds,
                                sessionProgress = sessionProgress,
                                sessionHistory = sessionHistory,
                                onSelectStudent = { viewModel.setSelectedStudent(it) },
                                onStartSessionClick = { viewModel.navigateTo(AppScreen.CREATE_SESSION) },
                                onStopSessionClick = { viewModel.stopSessionByParent(it) },
                                onRespondEarlyEnd = { sessionId, approve, note ->
                                    viewModel.respondToEarlyEndRequest(sessionId, approve, note)
                                },
                                onPairStudentClick = { viewModel.navigateTo(AppScreen.PAIRING_FLOW) },
                                onViewHistoryClick = { viewModel.navigateTo(AppScreen.SESSION_HISTORY) },
                                onSwitchRole = { viewModel.switchDeviceRole(it) }
                            )
                        }

                        AppScreen.STUDENT_DASHBOARD -> {
                            val isSessionActive = activeSession != null &&
                                    activeSession?.status == com.example.data.model.SessionStatus.ACTIVE

                            if (isSessionActive && activeSession != null) {
                                StudentFocusScreen(
                                    currentStudent = currentUser,
                                    activeSession = activeSession!!,
                                    remainingSeconds = remainingSeconds,
                                    sessionProgress = sessionProgress,
                                    onRequestEarlyEnd = { viewModel.requestEarlyTermination(it) },
                                    onTestShieldIntercept = { viewModel.triggerSimulatedAppIntercept(it) },
                                    onSwitchRole = { viewModel.switchDeviceRole(it) }
                                )
                            } else {
                                StudentIdleScreen(
                                    currentStudent = currentUser,
                                    pairingCode = pairingCodeEntity,
                                    pairingCodeRemainingSeconds = pairingCodeRemainingSeconds,
                                    sessionHistory = sessionHistory,
                                    isLoading = isLoading,
                                    onGeneratePairingCode = { viewModel.generateStudentPairingCode() },
                                    onViewHistory = { viewModel.navigateTo(AppScreen.SESSION_HISTORY) },
                                    onTestShield = { viewModel.triggerSimulatedAppIntercept(it) },
                                    onSwitchRole = { viewModel.switchDeviceRole(it) }
                                )
                            }
                        }

                        AppScreen.CREATE_SESSION -> {
                            CreateSessionScreen(
                                targetStudent = selectedStudent,
                                isLoading = isLoading,
                                onBack = { viewModel.navigateBack() },
                                onStartSession = { goal, mins, apps, onComplete ->
                                    viewModel.startFocusSession(goal, mins, apps) { success, err ->
                                        if (success) {
                                            viewModel.navigateTo(AppScreen.PARENT_DASHBOARD)
                                        }
                                        onComplete(success, err)
                                    }
                                }
                            )
                        }

                        AppScreen.PAIRING_FLOW -> {
                            PairingScreen(
                                linkedStudents = linkedStudents,
                                activePairingCode = pairingCodeEntity,
                                isLoading = isLoading,
                                onBack = { viewModel.navigateBack() },
                                onPairCode = { code, onComplete ->
                                    viewModel.pairWithStudent(code, onComplete)
                                },
                                onUnlinkStudent = { viewModel.unlinkStudent(it) }
                            )
                        }

                        AppScreen.SESSION_HISTORY -> {
                            SessionHistoryScreen(
                                currentRole = deviceRole,
                                sessions = sessionHistory,
                                onBack = { viewModel.navigateBack() }
                            )
                        }

                        AppScreen.SETTINGS -> {
                            SettingsScreen(
                                currentUser = currentUser,
                                currentRole = deviceRole,
                                onBack = { viewModel.navigateBack() },
                                onLogout = { viewModel.logout() },
                                onDeleteAccount = { viewModel.deleteAccount() },
                                onOpenPairing = { viewModel.navigateTo(AppScreen.PAIRING_FLOW) }
                            )
                        }
                    }
                }
            }
        }

        // FULLSCREEN SHIELD INTERCEPT OVERLAY (When student opens/tests a blocked app during active focus)
        if (shieldInterceptApp != null) {
            ShieldInterceptOverlay(
                blockedApp = shieldInterceptApp!!,
                activeSession = activeSession,
                remainingSeconds = remainingSeconds,
                onDismiss = { viewModel.dismissShieldIntercept() },
                onRequestEarlyEnd = {
                    viewModel.dismissShieldIntercept()
                    viewModel.requestEarlyTermination(null)
                }
            )
        }
    }
}
