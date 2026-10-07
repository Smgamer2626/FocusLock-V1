package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.FocusLockApp
import com.example.data.model.BlockedAppInfo
import com.example.data.model.BlockedAppsCatalog
import com.example.data.model.EarlyEndDecision
import com.example.data.model.FocusSessionEntity
import com.example.data.model.PairingCodeEntity
import com.example.data.model.SessionStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray

enum class AppScreen {
    ROLE_SELECTION,
    AUTH,
    PARENT_DASHBOARD,
    STUDENT_DASHBOARD,
    CREATE_SESSION,
    PAIRING_FLOW,
    SESSION_HISTORY,
    SETTINGS
}

class FocusLockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as FocusLockApp).repository

    // Current Navigation Screen
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.PARENT_DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Screen navigation stack for BackHandler
    private val screenBackStack = mutableListOf<AppScreen>()

    // Device Context / Role Mode (Parent vs Student device perspective)
    private val _deviceRole = MutableStateFlow<UserRole>(UserRole.PARENT)
    val deviceRole: StateFlow<UserRole> = _deviceRole.asStateFlow()

    // Current Logged-in User
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Parent Selected Student (for viewing & managing)
    private val _selectedStudent = MutableStateFlow<UserEntity?>(null)
    val selectedStudent: StateFlow<UserEntity?> = _selectedStudent.asStateFlow()

    // Linked Students list for Parent
    private val _linkedStudents = MutableStateFlow<List<UserEntity>>(emptyList())
    val linkedStudents: StateFlow<List<UserEntity>> = _linkedStudents.asStateFlow()

    // Active Focus Session
    private val _activeSession = MutableStateFlow<FocusSessionEntity?>(null)
    val activeSession: StateFlow<FocusSessionEntity?> = _activeSession.asStateFlow()

    // Countdown state (in seconds)
    private val _remainingSeconds = MutableStateFlow<Long>(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private val _sessionProgress = MutableStateFlow<Float>(0f)
    val sessionProgress: StateFlow<Float> = _sessionProgress.asStateFlow()

    // Active Pairing Code on Student Device
    private val _pairingCodeEntity = MutableStateFlow<PairingCodeEntity?>(null)
    val pairingCodeEntity: StateFlow<PairingCodeEntity?> = _pairingCodeEntity.asStateFlow()

    private val _pairingCodeSecondsRemaining = MutableStateFlow<Long>(0L)
    val pairingCodeSecondsRemaining: StateFlow<Long> = _pairingCodeSecondsRemaining.asStateFlow()

    // Session History
    private val _sessionHistory = MutableStateFlow<List<FocusSessionEntity>>(emptyList())
    val sessionHistory: StateFlow<List<FocusSessionEntity>> = _sessionHistory.asStateFlow()

    // Simulated / Triggered Shield Intercept
    private val _shieldInterceptApp = MutableStateFlow<BlockedAppInfo?>(null)
    val shieldInterceptApp: StateFlow<BlockedAppInfo?> = _shieldInterceptApp.asStateFlow()

    // User Feedback Snackbars / Dialogs
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var timerJob: Job? = null
    private var sessionObserverJob: Job? = null
    private var historyObserverJob: Job? = null

    init {
        // Automatically sign in with default seeded user on startup for smooth experience
        viewModelScope.launch {
            delay(150) // Allow repository seeding
            // Default to Parent Sarah
            val defaultParent = repository.login("sarah_parent", "FocusParent#1", UserRole.PARENT).getOrNull()
            if (defaultParent != null) {
                _currentUser.value = defaultParent
                _deviceRole.value = UserRole.PARENT
                _currentScreen.value = AppScreen.PARENT_DASHBOARD
                refreshLinkedStudents(defaultParent.userId)
                observeParentData(defaultParent.userId)
            } else {
                _currentScreen.value = AppScreen.ROLE_SELECTION
            }
        }

        startTimerEngine()
    }

    private fun startTimerEngine() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val session = _activeSession.value
                val now = System.currentTimeMillis()

                if (session != null && session.status == SessionStatus.ACTIVE) {
                    val remainingMs = session.endTimeEpoch - now
                    if (remainingMs <= 0) {
                        _remainingSeconds.value = 0L
                        _sessionProgress.value = 1f
                        // Automatically complete expired session
                        repository.completeExpiredSession(session.sessionId)
                    } else {
                        val totalMs = (session.durationMinutes * 60 * 1000L).coerceAtLeast(1L)
                        val elapsedMs = (totalMs - remainingMs).coerceAtLeast(0L)
                        _remainingSeconds.value = remainingMs / 1000L
                        _sessionProgress.value = (elapsedMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
                    }
                } else {
                    _remainingSeconds.value = 0L
                    _sessionProgress.value = 0f
                }

                // Update pairing code expiration
                val code = _pairingCodeEntity.value
                if (code != null && !code.isUsed) {
                    val codeRemaining = (code.expiresAtEpoch - now) / 1000L
                    _pairingCodeSecondsRemaining.value = codeRemaining.coerceAtLeast(0L)
                }

                delay(1000)
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            _currentScreen.value = screenBackStack.removeAt(screenBackStack.size - 1)
            return true
        }
        return false
    }

    // Switch between Parent Device perspective and Student Device perspective
    // (Crucial for testing 2-device behavior on single Android screen!)
    fun switchDeviceRole(targetRole: UserRole) {
        viewModelScope.launch {
            _isLoading.value = true
            _deviceRole.value = targetRole
            if (targetRole == UserRole.PARENT) {
                val parent = repository.login("sarah_parent", "FocusParent#1", UserRole.PARENT).getOrNull()
                if (parent != null) {
                    _currentUser.value = parent
                    refreshLinkedStudents(parent.userId)
                    observeParentData(parent.userId)
                    _currentScreen.value = AppScreen.PARENT_DASHBOARD
                }
            } else {
                val student = repository.login("alex_student", "StudyAlex#1", UserRole.STUDENT).getOrNull()
                if (student != null) {
                    _currentUser.value = student
                    observeStudentData(student.userId)
                    _currentScreen.value = AppScreen.STUDENT_DASHBOARD
                }
            }
            _isLoading.value = false
        }
    }

    fun selectRoleForAuth(role: UserRole) {
        _deviceRole.value = role
        navigateTo(AppScreen.AUTH)
    }

    fun refreshLinkedStudents(parentId: String) {
        viewModelScope.launch {
            val list = repository.getLinkedStudentsForParent(parentId)
            _linkedStudents.value = list
            if (list.isNotEmpty() && (_selectedStudent.value == null || !list.any { it.userId == _selectedStudent.value?.userId })) {
                _selectedStudent.value = list.first()
            }
        }
    }

    fun setSelectedStudent(student: UserEntity) {
        _selectedStudent.value = student
        observeStudentSessionForParent(student.userId)
    }

    private fun observeParentData(parentId: String) {
        sessionObserverJob?.cancel()
        historyObserverJob?.cancel()

        historyObserverJob = viewModelScope.launch {
            repository.observeParentHistory(parentId).collectLatest { list ->
                _sessionHistory.value = list
            }
        }

        // Also observe active session for selected student
        val student = _selectedStudent.value
        if (student != null) {
            observeStudentSessionForParent(student.userId)
        }
    }

    private fun observeStudentSessionForParent(studentId: String) {
        sessionObserverJob?.cancel()
        sessionObserverJob = viewModelScope.launch {
            repository.observeActiveSessionForStudent(studentId).collectLatest { session ->
                _activeSession.value = session
            }
        }
    }

    private fun observeStudentData(studentId: String) {
        sessionObserverJob?.cancel()
        historyObserverJob?.cancel()

        sessionObserverJob = viewModelScope.launch {
            repository.observeActiveSessionForStudent(studentId).collectLatest { session ->
                _activeSession.value = session
            }
        }

        historyObserverJob = viewModelScope.launch {
            repository.observeStudentHistory(studentId).collectLatest { list ->
                _sessionHistory.value = list
            }
        }

        // Check if student has active pairing code
        viewModelScope.launch {
            _pairingCodeEntity.value = repository.getActivePairingCodeForStudent(studentId)
        }
    }

    fun login(username: String, pass: String, role: UserRole, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.login(username, pass, role)
            _isLoading.value = false
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _deviceRole.value = user.role
                    if (user.role == UserRole.PARENT) {
                        refreshLinkedStudents(user.userId)
                        observeParentData(user.userId)
                        _currentScreen.value = AppScreen.PARENT_DASHBOARD
                    } else {
                        observeStudentData(user.userId)
                        _currentScreen.value = AppScreen.STUDENT_DASHBOARD
                    }
                    onResult(true, null)
                },
                onFailure = { error ->
                    onResult(false, error.message)
                }
            )
        }
    }

    fun register(
        username: String,
        pass: String,
        displayName: String,
        role: UserRole,
        securityAnswer: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.registerUser(role, username, pass, displayName, securityAnswer)
            _isLoading.value = false
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _deviceRole.value = user.role
                    if (user.role == UserRole.PARENT) {
                        refreshLinkedStudents(user.userId)
                        observeParentData(user.userId)
                        _currentScreen.value = AppScreen.PARENT_DASHBOARD
                    } else {
                        observeStudentData(user.userId)
                        _currentScreen.value = AppScreen.STUDENT_DASHBOARD
                    }
                    onResult(true, null)
                },
                onFailure = { error ->
                    onResult(false, error.message)
                }
            )
        }
    }

    fun resetPassword(username: String, answer: String, newPass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.resetPassword(username, answer, newPass)
            _isLoading.value = false
            res.fold(
                onSuccess = { onResult(true, "Password reset successfully. Please log in.") },
                onFailure = { onResult(false, it.message) }
            )
        }
    }

    fun generateStudentPairingCode() {
        val student = _currentUser.value ?: return
        if (student.role != UserRole.STUDENT) return

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.generatePairingCode(student.userId)
            _isLoading.value = false
            result.fold(
                onSuccess = { code ->
                    _pairingCodeEntity.value = code
                    _feedbackMessage.value = "New pairing code generated! Expires in 15 minutes."
                },
                onFailure = {
                    _feedbackMessage.value = it.message
                }
            )
        }
    }

    fun pairWithStudent(inputCode: String, onResult: (Boolean, String?) -> Unit) {
        val parent = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.pairStudentWithParent(parent.userId, inputCode)
            _isLoading.value = false
            result.fold(
                onSuccess = { studentName ->
                    refreshLinkedStudents(parent.userId)
                    onResult(true, "Successfully linked with student '$studentName'!")
                },
                onFailure = { error ->
                    onResult(false, error.message)
                }
            )
        }
    }

    fun unlinkStudent(studentId: String) {
        val parent = _currentUser.value ?: return
        viewModelScope.launch {
            repository.unlinkStudent(parent.userId, studentId)
            refreshLinkedStudents(parent.userId)
            _feedbackMessage.value = "Student unlinked from this Parent account."
        }
    }

    fun startFocusSession(
        studyGoal: String,
        durationMinutes: Int,
        blockedAppNames: List<String>,
        onResult: (Boolean, String?) -> Unit
    ) {
        val parent = _currentUser.value ?: return
        val student = _selectedStudent.value
        if (student == null) {
            onResult(false, "No student device selected. Please pair or select a student first.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.startFocusSession(
                parentId = parent.userId,
                parentDisplayName = parent.displayName,
                studentId = student.userId,
                studentDisplayName = student.displayName,
                studyGoal = studyGoal,
                durationMinutes = durationMinutes,
                blockedApps = blockedAppNames
            )
            _isLoading.value = false
            res.fold(
                onSuccess = { session ->
                    _activeSession.value = session
                    onResult(true, null)
                },
                onFailure = {
                    onResult(false, it.message)
                }
            )
        }
    }

    fun stopSessionByParent(sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.stopFocusSessionByParent(sessionId)
            _isLoading.value = false
            _feedbackMessage.value = "Focus session stopped by Parent."
        }
    }

    fun requestEarlyTermination(reason: String?) {
        val session = _activeSession.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.requestEarlyTermination(session.sessionId, reason)
            _isLoading.value = false
            res.fold(
                onSuccess = {
                    _feedbackMessage.value = "Early termination request sent to your Parent/Guardian."
                },
                onFailure = {
                    _feedbackMessage.value = it.message
                }
            )
        }
    }

    fun respondToEarlyEndRequest(sessionId: String, approve: Boolean, note: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.respondToEarlyEndRequest(sessionId, approve, note)
            _isLoading.value = false
            res.fold(
                onSuccess = {
                    _feedbackMessage.value = if (approve) {
                        "Approved! The focus session has ended and apps are unblocked."
                    } else {
                        "Denied. The focus timer continues on the student device."
                    }
                },
                onFailure = {
                    _feedbackMessage.value = it.message
                }
            )
        }
    }

    fun triggerSimulatedAppIntercept(appInfo: BlockedAppInfo) {
        _shieldInterceptApp.value = appInfo
    }

    fun dismissShieldIntercept() {
        _shieldInterceptApp.value = null
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun logout() {
        _currentUser.value = null
        _activeSession.value = null
        _currentScreen.value = AppScreen.ROLE_SELECTION
    }

    fun deleteAccount() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteAccount(user.userId)
            logout()
            _feedbackMessage.value = "Account deleted successfully."
        }
    }
}
