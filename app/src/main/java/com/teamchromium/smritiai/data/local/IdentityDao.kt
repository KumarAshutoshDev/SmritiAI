package com.teamchromium.smritiai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface IdentityDao {

    @Insert
    suspend fun insert(identity: IdentityEntity): Long

    @Query("SELECT * FROM identity")
    suspend fun getAll(): List<IdentityEntity>

    @Query("SELECT * FROM identity WHERE id = :id")
    suspend fun getById(id: Long): IdentityEntity?
}