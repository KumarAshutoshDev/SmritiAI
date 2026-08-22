package com.teamchromium.smritiai.security

import com.google.gson.Gson
import com.teamchromium.smritiai.intelligence.GrokMessage
import com.teamchromium.smritiai.intelligence.GrokRequest

object PayloadGuard {

    private val gson = Gson()

    private val allowedRequestFields = setOf(
        "model",
        "messages",
        "temperature",
        "max_tokens",
    )

    private val allowedMessageFields = setOf(
        "role",
        "content",
    )

    fun validate(request: GrokRequest) {
        val requestJson = gson.toJsonTree(request).asJsonObject
        val actualRequestFields = requestJson.keySet()

        check(actualRequestFields == allowedRequestFields) {
            "Unexpected GrokRequest fields: ${actualRequestFields - allowedRequestFields}"
        }

        request.messages.forEach { message ->
            val messageJson = gson.toJsonTree(message).asJsonObject
            val actualMessageFields = messageJson.keySet()

            check(actualMessageFields == allowedMessageFields) {
                "Unexpected GrokMessage fields: ${actualMessageFields - allowedMessageFields}"
            }

            val role = messageJson.get("role").asString
            check(role in setOf("system", "user", "assistant")) {
                "Unexpected message role: $role"
            }

            val content = messageJson.get("content").asString
            check(content.none { it == '\u0000' }) {
                "Message content contains illegal character"
            }
        }
    }
}
