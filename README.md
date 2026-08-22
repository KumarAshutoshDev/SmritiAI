# SmritiAI

AI-Powered Memory Assistant for Dementia Patients

SmritiAI is a privacy-first Android app that helps dementia patients recognize faces, recall conversations, and retain emotional context — reducing anxiety and caregiver burden.

## Team

- **Internal Dev Name:** Team Chromium
- **Public Pitch Name:** Team Garuda

## Core Features

- Real-time face recognition (on-device)
- Voice assistant ("Ask Smriti AI") for conversation recall
- Emotion-aware memory logging
- Memory diary with history
- Caregiver dashboard (thin sync, anonymized metadata)

## Privacy & Security

- Face embeddings, raw audio, and images never leave the device.
- Only transcript text goes to LLM API for summarization/emotion analysis.
- Local database encrypted with AES-256; keys in Android Keystore.
- Pseudonymization before any cloud sync.

## Tech Stack

- Kotlin, Android (Compose)
- Google ML Kit (face detection)
- On-device TFLite (face embeddings)
- On-device similarity search (face matching)
- Android SpeechRecognizer & TextToSpeech
- Room/SQLite (encrypted with SQLCipher)
- OpenAI/Gemini API for summarization/emotion (text-only)
- Jira for task tracking

## Build

```bash
./gradlew assembleDebug

