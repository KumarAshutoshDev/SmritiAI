package com.teamchromium.smritiai.intelligence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LlmResponseValidatorTest {

    @Test
    fun validResponse_returnsContent() {
        val response = GrokResponse(
            choices = listOf(
                GrokChoice(
                    message = GrokMessage(
                        role = "assistant",
                        content = "Her name is Laura."
                    )
                )
            )
        )
        assertEquals("Her name is Laura.", LlmResponseValidator.validate(response))
    }

    @Test
    fun emptyContent_returnsNull() {
        val response = GrokResponse(
            choices = listOf(
                GrokChoice(
                    message = GrokMessage(
                        role = "assistant",
                        content = ""
                    )
                )
            )
        )
        assertNull(LlmResponseValidator.validate(response))
    }

    @Test
    fun nullCharacter_returnsNull() {
        val response = GrokResponse(
            choices = listOf(
                GrokChoice(
                    message = GrokMessage(
                        role = "assistant",
                        content = "bad\u0000"
                    )
                )
            )
        )
        assertNull(LlmResponseValidator.validate(response))
    }

    @Test
    fun overlyLongContent_returnsNull() {
        val longContent = "a".repeat(2001)
        val response = GrokResponse(
            choices = listOf(
                GrokChoice(
                    message = GrokMessage(
                        role = "assistant",
                        content = longContent
                    )
                )
            )
        )
        assertNull(LlmResponseValidator.validate(response))
    }
}
