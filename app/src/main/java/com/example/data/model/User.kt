package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    PARENT,
    STUDENT
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val role: UserRole,
    val username: String,
    val displayName: String,
    val passwordHash: String,
    val salt: String,
    val securityQuestion: String = "What was the name of your first school?",
    val securityAnswerHash: String = "",
    val linkedParentId: String? = null,
    val linkedStudentIdsJson: String = "[]", // JSON array of student userIds for parents
    val createdAtEpoch: Long = System.currentTimeMillis()
)

@Entity(tableName = "pairing_codes")
data class PairingCodeEntity(
    @PrimaryKey val code: String, // 6 characters e.g. "FL-4921"
    val studentId: String,
    val studentDisplayName: String,
    val studentUsername: String,
    val createdAtEpoch: Long,
    val expiresAtEpoch: Long,
    val isUsed: Boolean = false,
    val usedByParentId: String? = null
)

enum class SessionStatus {
    ACTIVE,
    COMPLETED,
    ENDED_EARLY_APPROVED,
    CANCELLED_BY_PARENT
}

enum class EarlyEndDecision {
    NONE,
    PENDING,
    APPROVED,
    DENIED
}

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey val sessionId: String,
    val parentId: String,
    val parentDisplayName: String,
    val studentId: String,
    val studentDisplayName: String,
    val studyGoal: String,
    val durationMinutes: Int,
    val startTimeEpoch: Long,
    val endTimeEpoch: Long, // Absolute expiration timestamp to prevent local clock tampering
    val blockedAppsJson: String, // JSON array of blocked package names / labels
    val status: SessionStatus = SessionStatus.ACTIVE,
    val earlyEndRequested: Boolean = false,
    val earlyEndRequestTimeEpoch: Long? = null,
    val earlyEndRequestReason: String? = null,
    val earlyEndDecision: EarlyEndDecision = EarlyEndDecision.NONE,
    val earlyEndDecisionNote: String? = null,
    val completedAtEpoch: Long? = null
)
