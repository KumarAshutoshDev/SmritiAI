package com.teamchromium.smritiai.intelligence

import com.teamchromium.smritiai.BuildConfig

object LlmService {

    suspend fun askQuestion(context: LlmContext, question: String): String {
        val systemPrompt = buildSystemPrompt(context)

        val request = GrokRequest(
            messages = listOf(
                GrokMessage(role = "system", content = systemPrompt),
                GrokMessage(role = "user", content = question)
            )
        )

        val authHeader = "Bearer ${BuildConfig.GROK_API_KEY}"
        val response = GrokClient.api.getChatCompletion(authHeader, request)

        return response.choices.firstOrNull()?.message?.content
            ?: "Sorry, I couldn't process that right now."
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