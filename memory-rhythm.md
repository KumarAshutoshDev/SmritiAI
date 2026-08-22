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

## In Progress
- [ ] 2026-08-15: Waiting on MC's T32 before wiring T36 TextToSpeech into Ask Smriti AI.

## Blockers / Questions
- [ ]

## Debug Notes
<!-- Use for fixes, decisions, or gotchas -->
- `:app:assembleDebug` passes after T3/T4.
- The scaffold's Kotlin Compose plugin already registers the Kotlin extension; adding `org.jetbrains.kotlin.android` caused a duplicate extension error and was removed.
- T31 uses Android `SpeechRecognizer`; no cloud STT or audio upload path was added.
