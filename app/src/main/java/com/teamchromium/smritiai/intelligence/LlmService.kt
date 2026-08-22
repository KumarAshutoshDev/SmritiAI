package com.teamchromium.smritiai.intelligence

import com.teamchromium.smritiai.BuildConfig
import com.teamchromium.smritiai.security.PayloadGuard
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

object LlmService {

    private const val MAX_ATTEMPTS = 3
    private const val RETRY_DELAY_MS = 500L

    suspend fun askQuestion(context: LlmContext, question: String): String {
        val systemPrompt = buildSystemPrompt(context)

        val request = GrokRequest(
            messages = listOf(
                GrokMessage(role = "system", content = systemPrompt),
                GrokMessage(role = "user", content = question)
            )
        )

        PayloadGuard.validate(request)

        val authHeader = "Bearer ${BuildConfig.GROK_API_KEY}"

        for (attempt in 1..MAX_ATTEMPTS) {
            try {
                val response = GrokClient.api.getChatCompletion(authHeader, request)

                return LlmResponseValidator.validate(response)
                    ?: "Sorry, I couldn't process that right now."
            } catch (e: IOException) {
                if (attempt < MAX_ATTEMPTS) delay(RETRY_DELAY_MS * attempt)
            } catch (e: HttpException) {
                if (e.code() in 500..599 && attempt < MAX_ATTEMPTS) {
                    delay(RETRY_DELAY_MS * attempt)
                } else {
                    return "I'm having trouble connecting right now. Please try again shortly."
                }
            }
        }

        return "I'm having trouble connecting right now. Please try again shortly."
    }

    private fun buildSystemPrompt(context: LlmContext): String {
        val personInfo = if (context.recognizedPersonName != null) {
            "The person currently in front of the user is ${context.recognizedPersonName}, their ${context.recognizedPersonRelationship}."
        } else {
            "No specific person has been recognized right now."
        }

        val recentMemories = if (context.recentMemorySummaries.isNotEmpty()) {
            "Recent memories: " + context.recentMemorySummaries.joinToString("; ")
        } else {
            "No recent memories recorded yet."
        }

        return "You are Smriti AI, a gentle memory assistant for someone living with dementia. " +
            "Answer clearly and warmly, in short simple sentences. $personInfo $recentMemories"
    }
}
