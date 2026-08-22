# Memory Log — Rhythm Grover

## Role
App Developer: patient UI/UX, accessibility, SpeechRecognizer & TextToSpeech, dashboard UI.

## Key Responsibilities
- Patient-app design tokens and screens
- Camera preview and match/unknown-person UI
- Ask Smriti AI chat UI + TTS
- Caregiver Dashboard UI

## Completed
- [x] 2026-08-15: Completed T3 base app shell with MainActivity and an initial Navigation Compose graph.
- [x] 2026-08-15: Completed T4 patient-app design tokens for high-contrast colors, 20sp+ typography, spacing, and 56dp touch targets.
- [x] 2026-08-15: Completed T31 reusable Android SpeechRecognizer integration with mic permission, transcript state, partial results, error messages, and lifecycle cleanup.
- [x] 2026-08-22: Completed T37 by adding connectivity monitoring plus a clear offline state to Ask Smriti AI so Q&A is blocked cleanly when the device is offline while other patient-app features remain unaffected.
- [x] 2026-08-22: Completed T36 by wiring TextToSpeech as the default Ask Smriti AI response channel, adding spoken-answer status, microphone entry, and a stop-speaking control in the patient-accessible chat UI.
- [x] 2026-08-22: Completed T43 by adding the Add Memory contact-link picker from the Identity store, with the contact join kept in UI state only.
- [x] 2026-08-22: Completed T44 by adding Add Memory audio-note dictation via the reusable Android SpeechRecognizer and saving the transcript to the Behavior store.

## In Progress
- [ ] 2026-08-22: Local Gradle verification is pending because this workspace has no configured Android SDK location.

## Blockers / Questions
- [ ] `./gradlew --no-daemon :app:compileDebugKotlin` cannot run until `ANDROID_HOME` or `local.properties` points to a valid Android SDK.

## Debug Notes
<!-- Use for fixes, decisions, or gotchas -->
- `:app:assembleDebug` passes after T3/T4.
- The scaffold's Kotlin Compose plugin already registers the Kotlin extension; adding `org.jetbrains.kotlin.android` caused a duplicate extension error and was removed.
- T31 uses Android `SpeechRecognizer`; no cloud STT or audio upload path was added.
- T37 introduced a small connectivity observer for Ask Smriti AI and surfaces a high-contrast "Needs connectivity" state instead of letting the assistant fail silently.
- T36 uses Android `TextToSpeech` only, keeps spoken output as the primary response channel, and leaves on-screen text as the confirmation copy per the patient-app accessibility override.
- T43 reads contacts from `IdentityDao` and keeps selected contact details in view-layer state only; Behavior rows store the internal `contactId`, not duplicated names or relationships.
- T44 reuses `SmritiSpeechRecognizer`; no raw audio file is persisted or uploaded by this flow.
