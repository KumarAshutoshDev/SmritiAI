package com.teamchromium.smritiai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "identity")
data class IdentityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val relationship: String,
    val faceEmbedding: FloatArray,
    val createdAt: Long = System.currentTimeMillis()
)