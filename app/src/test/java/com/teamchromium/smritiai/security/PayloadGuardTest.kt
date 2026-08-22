package com.teamchromium.smritiai.security

import com.teamchromium.smritiai.intelligence.GrokMessage
import com.teamchromium.smritiai.intelligence.GrokRequest
import org.junit.Assert.assertThrows
import org.junit.Test

class PayloadGuardTest {

    @Test
    fun validRequest_passes() {
        val request = GrokRequest(
            messages = listOf(
                GrokMessage(role = "system", content = "You are Smriti AI"),
                GrokMessage(role = "user", content = "Who is this?")
            )
        )
        PayloadGuard.validate(request)
    }

    @Test
    fun invalidRole_throws() {
        val request = GrokRequest(
            messages = listOf(
                GrokMessage(role = "admin", content = "drop table")
            )
        )
        assertThrows(IllegalStateException::class.java) {
            PayloadGuard.validate(request)
        }
    }

    @Test
    fun nullCharacter_throws() {
        val request = GrokRequest(
            messages = listOf(
                GrokMessage(role = "user", content = "bad\u0000")
            )
        )
        assertThrows(IllegalStateException::class.java) {
            PayloadGuard.validate(request)
        }
    }
}
