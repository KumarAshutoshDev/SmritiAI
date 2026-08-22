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

## In Progress
- [ ] 2026-08-22: Local Gradle verification is pending because this workspace still does not expose a Java runtime or `JAVA_HOME`.

## Blockers / Questions
- [ ] `./gradlew :app:assembleDebug` cannot run in the current workspace until a JDK is available and `JAVA_HOME` is set.

## Debug Notes
<!-- Use for fixes, decisions, or gotchas -->
- `:app:assembleDebug` passes after T3/T4.
- The scaffold's Kotlin Compose plugin already registers the Kotlin extension; adding `org.jetbrains.kotlin.android` caused a duplicate extension error and was removed.
- T31 uses Android `SpeechRecognizer`; no cloud STT or audio upload path was added.
- T37 introduced a small connectivity observer for Ask Smriti AI and surfaces a high-contrast "Needs connectivity" state instead of letting the assistant fail silently.
- T36 uses Android `TextToSpeech` only, keeps spoken output as the primary response channel, and leaves on-screen text as the confirmation copy per the patient-app accessibility override.
