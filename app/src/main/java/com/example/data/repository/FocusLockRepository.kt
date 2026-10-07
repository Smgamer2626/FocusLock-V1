package com.example.data.repository

import com.example.data.local.FocusLockDatabase
import com.example.data.model.EarlyEndDecision
import com.example.data.model.FocusSessionEntity
import com.example.data.model.PairingCodeEntity
import com.example.data.model.SessionStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.security.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import java.util.UUID

class FocusLockRepository(private val database: FocusLockDatabase) {

    private val userDao = database.userDao()
    private val pairingCodeDao = database.pairingCodeDao()
    private val focusSessionDao = database.focusSessionDao()

    suspend fun seedInitialDataIfEmpty() {
        if (userDao.getUsersCount() > 0) return

        val parentSalt = SecurityUtils.generateSalt()
        val parentPassHash = SecurityUtils.hashPassword("FocusParent#1", parentSalt)
        val parentAnswerHash = SecurityUtils.hashPassword("lincoln high", parentSalt)
        val parentId = "parent_sarah_101"

        val studentSalt = SecurityUtils.generateSalt()
        val studentPassHash = SecurityUtils.hashPassword("StudyAlex#1", studentSalt)
        val studentAnswerHash = SecurityUtils.hashPassword("lincoln high", studentSalt)
        val studentId = "student_alex_202"

        val parentUser = UserEntity(
            userId = parentId,
            role = UserRole.PARENT,
            username = "sarah_parent",
            displayName = "Sarah (Guardian)",
            passwordHash = parentPassHash,
            salt = parentSalt,
            securityQuestion = "What was the name of your first school?",
            securityAnswerHash = parentAnswerHash,
            linkedParentId = null,
            linkedStudentIdsJson = JSONArray(listOf(studentId)).toString(),
            createdAtEpoch = System.currentTimeMillis() - 86400000L * 7
        )

        val studentUser = UserEntity(
            userId = studentId,
            role = UserRole.STUDENT,
            username = "alex_student",
            displayName = "Alex",
            passwordHash = studentPassHash,
            salt = studentSalt,
            securityQuestion = "What was the name of your first school?",
            securityAnswerHash = studentAnswerHash,
            linkedParentId = parentId,
            linkedStudentIdsJson = "[]",
            createdAtEpoch = System.currentTimeMillis() - 86400000L * 7
        )

        userDao.insertUser(parentUser)
        userDao.insertUser(studentUser)

        // Seed realistic study history
        val now = System.currentTimeMillis()
        val sampleHistory = listOf(
            FocusSessionEntity(
                sessionId = UUID.randomUUID().toString(),
                parentId = parentId,
                parentDisplayName = "Sarah (Guardian)",
                studentId = studentId,
                studentDisplayName = "Alex",
                studyGoal = "Mathematics - Algebra Review",
                durationMinutes = 60,
                startTimeEpoch = now - 86400000L * 2 - 3600000L,
                endTimeEpoch = now - 86400000L * 2,
                blockedAppsJson = JSONArray(listOf("YouTube", "Instagram", "TikTok", "Discord")).toString(),
                status = SessionStatus.COMPLETED,
                completedAtEpoch = now - 86400000L * 2
            ),
            FocusSessionEntity(
                sessionId = UUID.randomUUID().toString(),
                parentId = parentId,
                parentDisplayName = "Sarah (Guardian)",
                studentId = studentId,
                studentDisplayName = "Alex",
                studyGoal = "Science - Cell Biology Essay",
                durationMinutes = 45,
                startTimeEpoch = now - 86400000L - 2700000L,
                endTimeEpoch = now - 86400000L,
                blockedAppsJson = JSONArray(listOf("YouTube", "Roblox", "Snapchat", "Twitch")).toString(),
                status = SessionStatus.COMPLETED,
                completedAtEpoch = now - 86400000L
            ),
            FocusSessionEntity(
                sessionId = UUID.randomUUID().toString(),
                parentId = parentId,
                parentDisplayName = "Sarah (Guardian)",
                studentId = studentId,
                studentDisplayName = "Alex",
                studyGoal = "English Literature - Reading Chapter 4",
                durationMinutes = 30,
                startTimeEpoch = now - 3600000L * 6 - 1800000L,
                endTimeEpoch = now - 3600000L * 6,
                blockedAppsJson = JSONArray(listOf("Instagram", "TikTok", "Reddit")).toString(),
                status = SessionStatus.COMPLETED,
                completedAtEpoch = now - 3600000L * 6
            )
        )

        for (session in sampleHistory) {
            focusSessionDao.insertSession(session)
        }
    }

    suspend fun registerUser(
        role: UserRole,
        username: String,
        password: String,
        displayName: String,
        securityAnswer: String
    ): Result<UserEntity> {
        val trimmedUsername = username.trim()
        if (trimmedUsername.length < 3) {
            return Result.failure(IllegalArgumentException("Username must be at least 3 characters"))
        }
        val existing = userDao.getUserByUsername(trimmedUsername)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Username '$trimmedUsername' is already taken"))
        }

        val strength = SecurityUtils.evaluatePasswordStrength(password, role == UserRole.PARENT)
        if (!strength.isAcceptable) {
            return Result.failure(IllegalArgumentException(strength.feedback))
        }

        val salt = SecurityUtils.generateSalt()
        val passHash = SecurityUtils.hashPassword(password, salt)
        val answerHash = SecurityUtils.hashPassword(securityAnswer.trim().lowercase(), salt)

        val newUser = UserEntity(
            userId = UUID.randomUUID().toString(),
            role = role,
            username = trimmedUsername,
            displayName = displayName.trim().ifEmpty { trimmedUsername },
            passwordHash = passHash,
            salt = salt,
            securityQuestion = "What was the name of your first school?",
            securityAnswerHash = answerHash,
            linkedParentId = null,
            linkedStudentIdsJson = "[]",
            createdAtEpoch = System.currentTimeMillis()
        )

        userDao.insertUser(newUser)
        return Result.success(newUser)
    }

    suspend fun login(
        username: String,
        password: String,
        expectedRole: UserRole
    ): Result<UserEntity> {
        val user = userDao.getUserByUsername(username.trim())
            ?: return Result.failure(IllegalArgumentException("Invalid username or password"))

        if (user.role != expectedRole) {
            val roleName = if (expectedRole == UserRole.PARENT) "Parent/Guardian" else "Student"
            return Result.failure(IllegalArgumentException("This account is not registered as a $roleName. Please switch role or log in as ${user.role.name}."))
        }

        val isValid = SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)
        if (!isValid) {
            return Result.failure(IllegalArgumentException("Invalid username or password"))
        }

        return Result.success(user)
    }

    suspend fun generatePairingCode(studentId: String): Result<PairingCodeEntity> {
        val student = userDao.getUserByIdSync(studentId)
            ?: return Result.failure(IllegalStateException("Student account not found"))

        val now = System.currentTimeMillis()
        val expiresAt = now + (15 * 60 * 1000L) // 15 minute temporary expiration
        val codeStr = SecurityUtils.generatePairingCode()

        // Invalidate older unused codes
        pairingCodeDao.invalidateCodesForStudent(studentId)

        val codeEntity = PairingCodeEntity(
            code = codeStr,
            studentId = studentId,
            studentDisplayName = student.displayName,
            studentUsername = student.username,
            createdAtEpoch = now,
            expiresAtEpoch = expiresAt,
            isUsed = false,
            usedByParentId = null
        )
        pairingCodeDao.insertCode(codeEntity)
        return Result.success(codeEntity)
    }

    suspend fun getActivePairingCodeForStudent(studentId: String): PairingCodeEntity? {
        val now = System.currentTimeMillis()
        return pairingCodeDao.getActiveCodeForStudent(studentId, now)
    }

    suspend fun pairStudentWithParent(parentId: String, inputCode: String): Result<String> {
        val parent = userDao.getUserByIdSync(parentId)
            ?: return Result.failure(IllegalStateException("Parent account not found"))

        val cleanedCode = inputCode.trim().uppercase()
        val pairing = pairingCodeDao.getCode(cleanedCode)
            ?: return Result.failure(IllegalArgumentException("Invalid pairing code. Please double-check the code shown on the Student device."))

        if (pairing.isUsed) {
            return Result.failure(IllegalArgumentException("This pairing code has already been used. Please generate a new code on the Student device."))
        }

        val now = System.currentTimeMillis()
        if (now > pairing.expiresAtEpoch) {
            return Result.failure(IllegalArgumentException("This pairing code has expired. Please tap 'Generate Code' again on the Student device."))
        }

        val student = userDao.getUserByIdSync(pairing.studentId)
            ?: return Result.failure(IllegalStateException("Student profile associated with this code no longer exists."))

        // Mark code used
        pairingCodeDao.updateCode(
            pairing.copy(
                isUsed = true,
                usedByParentId = parentId
            )
        )

        // Update Student's linked parent
        userDao.updateUser(student.copy(linkedParentId = parentId))

        // Update Parent's linked students
        val studentIds = try {
            val arr = JSONArray(parent.linkedStudentIdsJson)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            list
        } catch (_: Exception) {
            mutableListOf()
        }
        if (!studentIds.contains(student.userId)) {
            studentIds.add(student.userId)
        }
        userDao.updateUser(parent.copy(linkedStudentIdsJson = JSONArray(studentIds).toString()))

        return Result.success(student.displayName)
    }

    suspend fun unlinkStudent(parentId: String, studentId: String): Result<Unit> {
        val parent = userDao.getUserByIdSync(parentId) ?: return Result.failure(IllegalStateException("Parent not found"))
        val student = userDao.getUserByIdSync(studentId)

        if (student != null && student.linkedParentId == parentId) {
            userDao.updateUser(student.copy(linkedParentId = null))
        }

        val currentList = try {
            val arr = JSONArray(parent.linkedStudentIdsJson)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val id = arr.getString(i)
                if (id != studentId) list.add(id)
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
        userDao.updateUser(parent.copy(linkedStudentIdsJson = JSONArray(currentList).toString()))
        return Result.success(Unit)
    }

    suspend fun startFocusSession(
        parentId: String,
        parentDisplayName: String,
        studentId: String,
        studentDisplayName: String,
        studyGoal: String,
        durationMinutes: Int,
        blockedApps: List<String>
    ): Result<FocusSessionEntity> {
        // Check if there is already an active session for this student
        val existingActive = focusSessionDao.getActiveSessionForStudentSync(studentId)
        if (existingActive != null && existingActive.status == SessionStatus.ACTIVE) {
            // Check if it naturally expired
            if (System.currentTimeMillis() >= existingActive.endTimeEpoch) {
                focusSessionDao.updateSession(
                    existingActive.copy(
                        status = SessionStatus.COMPLETED,
                        completedAtEpoch = existingActive.endTimeEpoch
                    )
                )
            } else {
                return Result.failure(IllegalStateException("A focus session is already active for $studentDisplayName"))
            }
        }

        val now = System.currentTimeMillis()
        val durationMillis = durationMinutes * 60 * 1000L
        val endTimeEpoch = now + durationMillis

        val session = FocusSessionEntity(
            sessionId = UUID.randomUUID().toString(),
            parentId = parentId,
            parentDisplayName = parentDisplayName,
            studentId = studentId,
            studentDisplayName = studentDisplayName,
            studyGoal = studyGoal.trim().ifEmpty { "General Study Session" },
            durationMinutes = durationMinutes,
            startTimeEpoch = now,
            endTimeEpoch = endTimeEpoch,
            blockedAppsJson = JSONArray(blockedApps).toString(),
            status = SessionStatus.ACTIVE
        )

        focusSessionDao.insertSession(session)
        return Result.success(session)
    }

    suspend fun stopFocusSessionByParent(sessionId: String): Result<Unit> {
        val session = focusSessionDao.getSessionByIdSync(sessionId)
            ?: return Result.failure(IllegalStateException("Session not found"))

        focusSessionDao.updateSession(
            session.copy(
                status = SessionStatus.CANCELLED_BY_PARENT,
                completedAtEpoch = System.currentTimeMillis()
            )
        )
        return Result.success(Unit)
    }

    suspend fun requestEarlyTermination(sessionId: String, reason: String?): Result<Unit> {
        val session = focusSessionDao.getSessionByIdSync(sessionId)
            ?: return Result.failure(IllegalStateException("Session not found"))

        if (session.status != SessionStatus.ACTIVE) {
            return Result.failure(IllegalStateException("This session is no longer active"))
        }

        focusSessionDao.updateSession(
            session.copy(
                earlyEndRequested = true,
                earlyEndRequestTimeEpoch = System.currentTimeMillis(),
                earlyEndRequestReason = reason?.trim()?.ifEmpty { null },
                earlyEndDecision = EarlyEndDecision.PENDING
            )
        )
        return Result.success(Unit)
    }

    suspend fun respondToEarlyEndRequest(
        sessionId: String,
        approve: Boolean,
        note: String?
    ): Result<Unit> {
        val session = focusSessionDao.getSessionByIdSync(sessionId)
            ?: return Result.failure(IllegalStateException("Session not found"))

        val updated = if (approve) {
            session.copy(
                status = SessionStatus.ENDED_EARLY_APPROVED,
                earlyEndDecision = EarlyEndDecision.APPROVED,
                earlyEndDecisionNote = note,
                completedAtEpoch = System.currentTimeMillis()
            )
        } else {
            session.copy(
                earlyEndDecision = EarlyEndDecision.DENIED,
                earlyEndDecisionNote = note
            )
        }

        focusSessionDao.updateSession(updated)
        return Result.success(Unit)
    }

    suspend fun completeExpiredSession(sessionId: String): Result<Unit> {
        val session = focusSessionDao.getSessionByIdSync(sessionId) ?: return Result.failure(IllegalStateException())
        if (session.status == SessionStatus.ACTIVE) {
            focusSessionDao.updateSession(
                session.copy(
                    status = SessionStatus.COMPLETED,
                    completedAtEpoch = session.endTimeEpoch
                )
            )
        }
        return Result.success(Unit)
    }

    suspend fun changePassword(userId: String, currentPass: String, newPass: String): Result<Unit> {
        val user = userDao.getUserByIdSync(userId) ?: return Result.failure(IllegalStateException("User not found"))
        if (!SecurityUtils.verifyPassword(currentPass, user.salt, user.passwordHash)) {
            return Result.failure(IllegalArgumentException("Current password is incorrect"))
        }

        val strength = SecurityUtils.evaluatePasswordStrength(newPass, user.role == UserRole.PARENT)
        if (!strength.isAcceptable) {
            return Result.failure(IllegalArgumentException(strength.feedback))
        }

        val newSalt = SecurityUtils.generateSalt()
        val newHash = SecurityUtils.hashPassword(newPass, newSalt)
        userDao.updateUser(user.copy(passwordHash = newHash, salt = newSalt))
        return Result.success(Unit)
    }

    suspend fun resetPassword(username: String, securityAnswer: String, newPass: String): Result<Unit> {
        val user = userDao.getUserByUsername(username.trim())
            ?: return Result.failure(IllegalArgumentException("Username not found"))

        val answerHash = SecurityUtils.hashPassword(securityAnswer.trim().lowercase(), user.salt)
        if (!SecurityUtils.verifyPassword(securityAnswer.trim().lowercase(), user.salt, user.securityAnswerHash)) {
            return Result.failure(IllegalArgumentException("Incorrect security answer"))
        }

        val strength = SecurityUtils.evaluatePasswordStrength(newPass, user.role == UserRole.PARENT)
        if (!strength.isAcceptable) {
            return Result.failure(IllegalArgumentException(strength.feedback))
        }

        val newSalt = SecurityUtils.generateSalt()
        val newHash = SecurityUtils.hashPassword(newPass, newSalt)
        val newAnswerHash = SecurityUtils.hashPassword(securityAnswer.trim().lowercase(), newSalt)
        userDao.updateUser(
            user.copy(
                passwordHash = newHash,
                salt = newSalt,
                securityAnswerHash = newAnswerHash
            )
        )
        return Result.success(Unit)
    }

    suspend fun deleteAccount(userId: String): Result<Unit> {
        userDao.deleteUser(userId)
        return Result.success(Unit)
    }

    fun observeUser(userId: String): Flow<UserEntity?> = userDao.getUserById(userId)

    fun observeActiveSessionForStudent(studentId: String): Flow<FocusSessionEntity?> =
        focusSessionDao.getActiveSessionForStudent(studentId)

    fun observeActiveSessionForParent(parentId: String): Flow<FocusSessionEntity?> =
        focusSessionDao.getActiveSessionForParent(parentId)

    fun observeStudentHistory(studentId: String): Flow<List<FocusSessionEntity>> =
        focusSessionDao.getSessionsForStudent(studentId)

    fun observeParentHistory(parentId: String): Flow<List<FocusSessionEntity>> =
        focusSessionDao.getSessionsForParent(parentId)

    fun observeAllStudents(): Flow<List<UserEntity>> = userDao.getAllStudents()

    suspend fun getLinkedStudentsForParent(parentId: String): List<UserEntity> {
        val parent = userDao.getUserByIdSync(parentId) ?: return emptyList()
        val studentIds = try {
            val arr = JSONArray(parent.linkedStudentIdsJson)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            list
        } catch (_: Exception) {
            emptyList()
        }
        val all = userDao.getAllStudentsSync()
        return all.filter { studentIds.contains(it.userId) || it.linkedParentId == parentId }
    }
}
