package com.teamchromium.smritiai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BehaviorDao {

    @Insert
    suspend fun insert(behavior: BehaviorEntity): Long

    @Query("SELECT * FROM behavior WHERE contactId = :contactId ORDER BY timestamp DESC")
    suspend fun getByContact(contactId: Long): List<BehaviorEntity>

    @Query("SELECT * FROM behavior ORDER BY timestamp DESC")
    suspend fun getAll(): List<BehaviorEntity>

    @Query("SELECT MAX(timestamp) FROM behavior WHERE contactId = :contactId")
    suspend fun getLastSeen(contactId: Long): Long?

    @Query("SELECT aiSummary FROM behavior WHERE aiSummary IS NOT NULL ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSummaries(limit: Int): List<String>
}