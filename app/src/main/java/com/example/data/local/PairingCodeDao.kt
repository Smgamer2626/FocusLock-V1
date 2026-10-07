package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PairingCodeEntity

@Dao
interface PairingCodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCode(code: PairingCodeEntity)

    @Query("SELECT * FROM pairing_codes WHERE code = :code LIMIT 1")
    suspend fun getCode(code: String): PairingCodeEntity?

    @Query("SELECT * FROM pairing_codes WHERE studentId = :studentId AND isUsed = 0 AND expiresAtEpoch > :currentEpoch ORDER BY createdAtEpoch DESC LIMIT 1")
    suspend fun getActiveCodeForStudent(studentId: String, currentEpoch: Long): PairingCodeEntity?

    @Update
    suspend fun updateCode(code: PairingCodeEntity)

    @Query("UPDATE pairing_codes SET isUsed = 1 WHERE studentId = :studentId")
    suspend fun invalidateCodesForStudent(studentId: String)
}
