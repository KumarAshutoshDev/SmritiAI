# SmritiAI — System Architecture

**Based on:** SmritiAI PRD v3.1 (August 14, 2026)
**Scope:** MVP — Native Android app, on-device-first, edge processing with selective cloud LLM calls, plus a lightweight sync backend for the Caregiver Dashboard.
**Team (internal):** Team Chromium

> **v3.2 update (architecture correction):** ML Kit Face Detection finds faces, but it does **not** produce face embeddings. For face matching (T20/T22), we add a separate on-device TFLite embedding model. This stays fully on-device and does not change privacy/security rules.

---

## 1. Architecture Principles

1. **Edge-first, cloud-optional.** Biometric data (faces, voice) never leaves the device. Only text (transcripts, summaries) crosses the network, and only for LLM calls.
2. **Graceful degradation.** Face recognition, matching, and voice I/O must work fully offline. Only summarization, emotion tagging, and Q&A depend on connectivity.
3. **Data separation by sensitivity.** Identity data (names, relationships, face embeddings) is stored separately from behavioral/interaction data (moods, transcripts, timestamps) to limit blast radius if one store is compromised.
4. **Pseudonymize at the boundary.** Any data leaving the device (future caregiver dashboard sync) is stripped of real names and replaced with random IDs before transmission.
5. **Low-latency by design.** Target: under 2 seconds from capture (face or voice) to user-facing response.

---

## 2. High-Level System Diagram

```
                         ┌─────────────────────────────────────────┐
                         │              ANDROID DEVICE               │
                         │                                             │
 Camera ──────┐          │  ┌──────────────┐   ┌───────────────────┐ │
               │          │  │  Input Layer  │──▶│  Processing Layer   │ │
 Microphone ───┘          │  │ (Camera/Mic)  │   │   (on-device)        │ │
                         │  └──────────────┘   │  - ML Kit face det.  │ │
                         │                      │  - TFLite embedding  │ │
                         │                      │  - Face matching     │ │
                         │                      │  - SpeechRecognizer  │ │
                         │                      └─────────┬───────────┘ │
                         │                                │             │
                         │                                ▼             │
                         │                     ┌────────────────────┐  │
                         │                     │ Intelligence Layer  │  │
                         │                     │ - Confidence check   │  │
                         │                     │ - "Last seen" calc   │  │
                         │                     │ - Orchestration      │  │
                         │                     └─────────┬──────────┘  │
                         │                                │             │
                         │              ┌─────────────────┼───────────┐ │
                         │              ▼                             ▼ │
                         │   ┌────────────────────┐     ┌──────────────────┐
                         │   │  Storage Layer       │     │  Network Client   │
                         │   │  Room / SQLite        │     │  (text only)       │
                         │   │  - Identity store      │     └────────┬─────────┘
                         │   │  - Behavior store       │              │
                         │   │  - AES-256 encrypted    │              │
                         │   │  - Keys in Keystore     │              │
                         │   └──────────┬─────────────┘              │
                         │              │                              │
                         │              ▼                              │
                         │   ┌────────────────────┐                    │
                         │   │   Output Layer       │                    │
                         │   │  - Android TTS         │                    │
                         │   │  - UI (Compose/Views)  │                    │
                         │   └────────────────────┘                    │
                         └───────────────────────────────────────────┼───┘
                                                                       │
                                                                       ▼
                                                        ┌──────────────────────┐
                                                        │  External LLM API      │
                                                        │  (OpenAI / Gemini)      │
                                                        │  - Summarization         │
                                                        │  - Emotion analysis       │
                                                        │  - Q&A over context        │
                                                        │  Receives: transcript text  │
                                                        │            only — no images  │
                                                        │            or embeddings      │
                                                        └──────────────────────┘

                         ┌───────────────────────────────────────────┐
                         │        SYNC BACKEND (thin, MVP — new)       │
                         │  - Auth                                       │
                         │  - Storage: pseudonymized/minimized metadata   │
                         │  - Read API for dashboard                       │
                         │  Receives: activity logs, mood trends, contact   │
                         │            IDs (random) — no real names, no       │
                         │            embeddings, no raw audio/images          │
                         └──────────────────────┬────────────────────────┘
                                                 │
                                                 ▼
                                    ┌──────────────────────────┐
                                    │  Caregiver Dashboard        │
                                    │  (web or companion view)      │
                                    │  - Activity logs                │
                                    │  - Recognized-face summary        │
                                    │  - Mood trends                      │
                                    │  - Anomaly alerts (repeated           │
                                    │    unrecognized face)                   │
                                    └──────────────────────────┘
```

---

## 3. Layer Breakdown

### 3.1 Input Layer
| Component | Responsibility |
|---|---|
| Camera feed | Captures frames for face detection (Recognize Person flow, Add Person flow). |
| Microphone | Captures raw audio for "Ask Smriti AI" and memory voice notes. |

No raw camera or audio data is queued for network transmission at this layer.

### 3.2 Processing Layer (on-device)
| Component | Responsibility | Tech |
|---|---|---|
| Face Detector | Locates face(s) in frame | Google ML Kit Face Detection |
| Embedding Extractor | Converts detected face → vector embedding | On-device TFLite face embedding model (e.g., MobileFaceNet / FaceNet) |
| Face Matcher | Compares embedding against stored embeddings; returns best match + confidence score | On-device similarity search over local embedding store |
| Speech-to-Text | Converts mic audio → transcript | Android `SpeechRecognizer` |

**Decision point (FR-FR-03):** Match confidence ≥ 80% → confirmable result. Match confidence < 80% or no match → Unknown Person flow (FR-FR-05), never a silent failure.

### 3.3 Intelligence Layer
This is the orchestration layer — it does **not** host a traditional backend/database. Responsibilities:
- Assembles context (recognized person, relationship, recent memory log entries) before calling the LLM.
- Sends transcript + context to the LLM API (OpenAI or Gemini) for:
  - Conversational Q&A ("Who is my sister?", "What are we talking about?")
  - Post-conversation summarization
  - Emotion/sentiment tagging (3–4 states at MVP: e.g., Neutral, Happy, Anxious)
- Computes derived, non-sensitive values locally (e.g., "Last Seen" timestamp per contact) rather than delegating to the LLM.
- Applies confidence-threshold and validation logic before results reach the UI.

**Only text crosses this boundary to the network** — transcripts and lightweight context, never raw audio, images, or face embeddings.

### 3.4 Storage Layer — Room / SQLite (local, on-device by default)

Two logically separated tables/domains (NFR-S-03):

| Store | Contents | Notes |
|---|---|---|
| **Identity store** | Name, relationship, face embedding reference, contact metadata | Higher sensitivity; isolated from behavioral data |
| **Behavior/interaction store** | Memory entries (photo ref, audio ref, transcript, AI summary, mood tag, timestamp), "last seen" derived values | Linked to identity store via internal ID, not by duplicating identity fields |

Both stores are AES-256 encrypted at rest (NFR-S-02); encryption keys live in Android Keystore, hardware-backed, never in app code or SharedPreferences (NFR-S-05).

### 3.5 Output Layer
| Component | Responsibility |
|---|---|
| Android TextToSpeech | Voice responses for recognition results and assistant answers (default output mode, NFR-U-02) |
| UI (Home, Recognize, Ask Smriti AI, Add Memory, Memory History) | Text/visual display: name, relationship, mood, last seen, chat-style transcript |

### 3.6 Sync Backend + Caregiver Dashboard (MVP, added v3.1)
| Component | Responsibility | Notes |
|---|---|---|
| Sync client (on-device) | Pseudonymizes + minimizes outbound records before transmission | Real names → random IDs; only fields like "Met with daughter at 2:00 PM, mood: neutral" are sent (NFR-S-04, NFR-S-07) |
| Sync backend | Thin service: auth, storage, read API | Not a full cloud-native backend — just enough to persist and serve minimized metadata |
| Caregiver Dashboard | Activity logs, recognized-face summary, mood trends, configurable anomaly alerts (e.g., repeated unrecognized face) | FR-CA-01 to FR-CA-03 |

---

## 4. Core Flows

### 4.1 Recognize Person Flow
1. Camera captures frame → Face Detector → Embedding Extractor.
2. Face Matcher compares against local Identity store.
3. **≥80% confidence:** Intelligence Layer pulls relationship + last-seen data → Output Layer shows "Matched: Laura (93%)" + speaks it via TTS.
4. **<80% confidence / no match:** Unknown Person flow triggers — prompt to add person or skip; no guess is presented to the user.

### 4.2 Ask Smriti AI Flow
1. Mic captures speech → `SpeechRecognizer` → transcript.
2. Intelligence Layer assembles context (recently recognized person, relevant memory entries).
3. Transcript + context sent to LLM API → response text returned.
4. Response rendered in chat UI and spoken via TTS.
5. If offline: flow degrades — assistant indicates it needs connectivity for this feature; core recognition remains available (NFR-R-02).

### 4.3 Add Memory Flow
1. User captures photo/audio note, optionally links to an existing contact.
2. Audio note transcribed on-device (`SpeechRecognizer`).
3. Transcript sent to LLM for summarization + mood tagging (FR-MD-02).
4. Summary + mood tag + metadata written to Behavior store (encrypted); raw photo/audio stored locally.
5. Memory History screen reads chronologically from Behavior store, joined with Identity store for display, filterable by person.

### 4.4 Add Person Flow (Onboarding a Face)
1. Caregiver/patient captures face photo.
2. Embedding computed on-device using the TFLite model, stored in Identity store alongside name + relationship.
3. Optional voice note recorded and linked as an initial memory entry.
4. Explicit consent captured before any face/audio capture is persisted (NFR-S-06).

### 4.5 Caregiver Dashboard Sync Flow (MVP, added v3.1)
1. On a schedule or after new activity, the on-device sync client reads relevant records from the Behavior store.
2. Records are pseudonymized (real names/IDs → random IDs) and minimized to only essential fields.
3. Minimized payload is sent to the sync backend over an authenticated connection.
4. Caregiver Dashboard reads from the sync backend to show activity logs, mood trends, and anomaly alerts (e.g., a "stranger" face detected repeatedly — FR-CA-02).
5. If offline, sync is deferred; the dashboard shows the last successfully synced state rather than failing (consistent with NFR-R-02's graceful-degradation principle).

---

## 5. Security & Privacy Architecture

| Control | Implementation |
|---|---|
| On-device-only biometrics | Face embeddings, raw audio, raw images never transmitted (NFR-S-01) |
| Encryption at rest | AES-256 on all local stores (NFR-S-02) |
| Data separation | Identity vs. behavioral data in separate stores/tables (NFR-S-03) |
| Pseudonymization at sync boundary | Any future caregiver dashboard / cloud sync replaces real names with random IDs before leaving device (NFR-S-04) |
| Key management | Android Keystore, hardware-backed (NFR-S-05) |
| Consent | Explicit onboarding consent gate before first capture (NFR-S-06) |
| Data minimization | Only essential fields (e.g., "Met with daughter at 2:00 PM, mood: neutral") included in any shared/caregiver-facing view (NFR-S-07) |

**Network boundary:** the only traffic ever leaving the device is transcript text + lightweight context sent to the LLM API for summarization, emotion tagging, and Q&A. No images, audio files, or embeddings are ever included in these calls.

---

## 6. Tech Stack Summary

| Layer | Technology |
|---|---|
| Frontend | Kotlin, native Android (Android Studio) |
| Face Detection | Google ML Kit (on-device) |
| Face Embedding Extraction | On-device TFLite model (e.g., MobileFaceNet / FaceNet) |
| Face Matching | On-device similarity search over local embeddings |
| Speech-to-Text | Android `SpeechRecognizer` |
| Voice Output | Android `TextToSpeech` |
| NLP (summarization, emotion, Q&A) | OpenAI API or Google Gemini (cloud, text-only) |
| Local Database | Room (SQLite) |
| Key Storage | Android Keystore |

> **Note on v1 → current divergence:** the original v1 assumption of a cloud-native backend (API Gateway, cloud STT via Whisper, vector DB) has been replaced with an edge-first design. There is no traditional backend service for the MVP; the "Intelligence Layer" is an orchestration layer inside the app that makes selective, text-only calls to a third-party LLM API.

---

## 7. Reliability & Offline Behavior

- Core face recognition and memory recall (FR-FR-01–05, Memory Diary read/write) must function fully offline (NFR-R-02).
- Only LLM-dependent features (Q&A, auto-summarization, emotion tagging) require connectivity; these should degrade gracefully — e.g., queue transcripts for summarization when connectivity returns, or show a clear "offline" state rather than failing silently.
- Room/SQLite performance must hold up as the number of registered faces and memory entries grows (NFR-R-01) — indexing on contact ID and timestamp is recommended for the Behavior store.

---

## 8. Roadmap Impact on Architecture

| Roadmap item | Architectural implication |
|---|---|
| Multi-language support (Hindi + English) | `SpeechRecognizer` locale config + LLM prompt/response localization; no structural change |
| Basic cloud backend for LLM calls (Short term) | Introduces a thin proxy/backend service between app and LLM API (cost control, key management) — first departure from pure client-to-LLM calls; may share infrastructure with the sync backend below |
| Wearable integration (Mid term) | New Input Layer source (smartwatch/glasses sensors) feeding the existing Processing Layer over Bluetooth/Wi-Fi |
| Healthcare API integrations (Long term) | New integration layer at the cloud backend boundary, subject to additional compliance/security review (not detailed here — out of MVP scope) |
| AR-based health insights (Long term) | New Output Layer target (HUD/AR overlay) in addition to/instead of phone UI + TTS |

---

## 9. Explicit Non-Scope (MVP)

Per PRD Appendix (Section 12), the following are **not** part of this architecture and are not represented in the diagrams above:
- Smart-glasses hardware (camera, mic array, HUD, bone-conduction audio, battery/pairing design).
- Any cloud-native backend replacing the on-device processing layer.
- Vector database / cloud-hosted face embedding store.

These remain long-term roadmap items and should get their own architecture pass once the mobile MVP validates the core recognition/recall/emotion-tagging experience.
