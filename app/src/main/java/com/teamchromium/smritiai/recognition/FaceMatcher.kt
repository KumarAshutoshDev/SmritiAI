package com.teamchromium.smritiai.recognition

import com.teamchromium.smritiai.config.FACE_MATCH_CONFIDENCE_THRESHOLD
import com.teamchromium.smritiai.data.local.IdentityDao
import com.teamchromium.smritiai.data.local.IdentityEntity
import kotlin.math.sqrt

sealed class MatchResult {
    data class Found(val identity: IdentityEntity, val confidence: Float) : MatchResult()
    object NotFound : MatchResult()
}

class FaceMatcher(private val identityDao: IdentityDao) {

    suspend fun findBestMatch(newEmbedding: FloatArray): MatchResult {
        val allIdentities = identityDao.getAll()
        if (allIdentities.isEmpty()) return MatchResult.NotFound

        var bestMatch: IdentityEntity? = null
        var bestScore = -1f

        for (identity in allIdentities) {
            val score = cosineSimilarity(newEmbedding, identity.faceEmbedding)
            if (score > bestScore) {
                bestScore = score
                bestMatch = identity
            }
        }

        return if (bestMatch != null && bestScore >= FACE_MATCH_CONFIDENCE_THRESHOLD) {
            MatchResult.Found(bestMatch, bestScore)
        } else {
            MatchResult.NotFound
        }
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) return 0f
        var dot = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denominator = sqrt(normA) * sqrt(normB)
        return if (denominator == 0f) 0f else dot / denominator
    }
}