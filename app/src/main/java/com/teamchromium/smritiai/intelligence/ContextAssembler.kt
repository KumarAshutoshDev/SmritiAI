package com.teamchromium.smritiai.intelligence

import com.teamchromium.smritiai.data.local.BehaviorDao
import com.teamchromium.smritiai.data.local.IdentityDao

class ContextAssembler(
    private val identityDao: IdentityDao,
    private val behaviorDao: BehaviorDao,
) {

    suspend fun assembleContext(recognizedContactId: Long?): LlmContext {
        val identity = recognizedContactId?.let { identityDao.getById(it) }
        val recentSummaries = behaviorDao.getRecentSummaries(limit = 5)

        return LlmContext(
            recognizedPersonName = identity?.name,
            recognizedPersonRelationship = identity?.relationship,
            recentMemorySummaries = recentSummaries
        )
    }
}