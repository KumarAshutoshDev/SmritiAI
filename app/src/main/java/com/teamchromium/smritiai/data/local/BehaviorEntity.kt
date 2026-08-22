package com.teamchromium.smritiai.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "behavior",
    indices = [
        Index(value = ["contactId", "timestamp"])
    ]
)
data class BehaviorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: Long?,        // links to IdentityEntity.id — nullable if unlinked
    val photoRef: String?,
    val audioRef: String?,
    val transcript: String?,
    val aiSummary: String?,
    val moodTag: String? = null, // filled in later, Phase 4
    val timestamp: Long = System.currentTimeMillis()
)
