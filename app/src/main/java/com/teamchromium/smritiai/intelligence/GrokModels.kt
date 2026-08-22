package com.teamchromium.smritiai.intelligence

data class GrokMessage(
    val role: String,
    val content: String
)

data class GrokRequest(
    val model: String = "openai/gpt-oss-20b",
    val messages: List<GrokMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 500
)

data class GrokChoice(
    val message: GrokMessage
)

data class GrokResponse(
    val choices: List<GrokChoice>
)