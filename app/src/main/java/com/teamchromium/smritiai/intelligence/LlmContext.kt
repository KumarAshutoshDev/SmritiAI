package com.teamchromium.smritiai.intelligence

data class LlmContext(
    val recognizedPersonName: String?,
    val recognizedPersonRelationship: String?,
    val recentMemorySummaries: List<String>
)