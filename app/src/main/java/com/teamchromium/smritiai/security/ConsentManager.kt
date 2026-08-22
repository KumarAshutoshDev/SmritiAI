package com.teamchromium.smritiai.security

import android.content.Context

object ConsentManager {

    private const val PREFS_NAME = "smriti_consent"
    private const val KEY_CONSENT_ACCEPTED = "consent_accepted"

    fun checkConsent(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_CONSENT_ACCEPTED, false)
    }

    fun setConsentAccepted(context: Context, accepted: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_CONSENT_ACCEPTED, accepted).apply()
    }
}
