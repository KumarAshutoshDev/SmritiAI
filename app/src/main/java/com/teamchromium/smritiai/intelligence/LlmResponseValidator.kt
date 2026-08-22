package com.teamchromium.smritiai.intelligence

object LlmResponseValidator {

    private const val MAX_RESPONSE_LENGTH = 2000

    fun validate(response: GrokResponse): String? = runCatching {
        val content = response.choices.firstOrNull()?.message?.content
            ?: return null

        require(content.isNotBlank()) { "Response content is blank" }
        require(content.length <= MAX_RESPONSE_LENGTH) {
            "Response content exceeds $MAX_RESPONSE_LENGTH characters"
        }
        require(content.none { it == '\u0000' }) {
            "Response content contains illegal null character"
        }

        content
    }.getOrNull()
}
