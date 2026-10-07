package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FocusSessionEntity
import com.example.data.model.SessionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity)

    @Update
    suspend fun updateSession(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions WHERE sessionId = :sessionId LIMIT 1")
    fun getSessionById(sessionId: String): Flow<FocusSessionEntity?>

    @Query("SELECT * FROM focus_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionByIdSync(sessionId: String): FocusSessionEntity?

    @Query("SELECT * FROM focus_sessions WHERE studentId = :studentId AND status = 'ACTIVE' ORDER BY startTimeEpoch DESC LIMIT 1")
    fun getActiveSessionForStudent(studentId: String): Flow<FocusSessionEntity?>

    @Query("SELECT * FROM focus_sessions WHERE studentId = :studentId AND status = 'ACTIVE' ORDER BY startTimeEpoch DESC LIMIT 1")
    suspend fun getActiveSessionForStudentSync(studentId: String): FocusSessionEntity?

    @Query("SELECT * FROM focus_sessions WHERE parentId = :parentId AND status = 'ACTIVE' ORDER BY startTimeEpoch DESC LIMIT 1")
    fun getActiveSessionForParent(parentId: String): Flow<FocusSessionEntity?>

    @Query("SELECT * FROM focus_sessions WHERE studentId = :studentId ORDER BY startTimeEpoch DESC")
    fun getSessionsForStudent(studentId: String): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE parentId = :parentId ORDER BY startTimeEpoch DESC")
    fun getSessionsForParent(parentId: String): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY startTimeEpoch DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("DELETE FROM focus_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)
}
