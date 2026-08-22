# Product Requirements Document (PRD)
**Project Name:** SmritiAI — AI-Powered Memory Assistant for Dementia Patients
**Team (internal/dev name):** Team Chromium — *external/pitch-facing name: Team Garuda. Use Team Chromium consistently in this document and other internal engineering artifacts; use Team Garuda for pitch/competition materials.*
**Version:** 3.1
**Date:** August 14, 2026
**Status:** Draft

---

## Changelog

### v3 → v3.1
- **Moved Caregiver Dashboard from Mid-Term roadmap into MVP scope** (Section 5.6, Section 8, Section 11). This is the one deliberate departure from the pure on-device MVP: caregivers get a real dashboard at launch, not in a later phase.
- Added a thin **sync backend** (auth, storage, read API) to Technical Architecture to support the dashboard — kept intentionally minimal, distinct from the "traditional backend/large database" that was scoped out in v2/v3. Face embeddings, raw audio, and raw images still never leave the device; only pseudonymized, minimized metadata syncs (per NFR-S-04, NFR-S-07).
- Updated Tech Stack table to include the sync backend layer.
- Flagged an open ownership gap in Section 13 (Team): no current team member is explicitly assigned the sync backend/dashboard build.
- Added a risk entry (Section 10) for the new network-facing surface this introduces.

### v2 → v3
- Resolved team naming: **Team Chromium** for internal/dev docs, **Team Garuda** for external pitch materials (see header note).
- Added a **confidence threshold** to face-match logic (FR-FR-03) so the requirement is testable, not just descriptive.
- Added an explicit **"Unknown Person" flow** (FR-FR-05) covering what happens when no match is found.
- Added a **Competitor Analysis** section (Section 3) naming existing tool categories and why they fall short.
- Updated team responsibilities to reflect the on-device MVP scope: Rhythm Grover now owns SpeechRecognizer/TextToSpeech integration; Ritvik Jain's role reframed around the Intelligence Layer (API & NLP) rather than a traditional backend/large database.

### v1 → v2
- Corrected primary form factor: **MVP is an Android mobile app (SmritiAI)**, not smart glasses. Smart glasses/wearables repositioned as a mid/long-term roadmap item, not the current build.
- Rewrote Technical Architecture to match the actual stack: **on-device processing** (ML Kit, Room/SQLite, Android SpeechRecognizer, Android TTS) with selective cloud calls only for LLM summarization/emotion analysis (Grok (x.ai)).
- Added **Emotion-Aware Memory** as a first-class functional area (previously missing).
- Added **Memory Diary (Add Memory / Memory History)** as a functional area (previously missing).
- Expanded Privacy & Security NFRs with the specific controls from the deck: pseudonymization, data separation, hardware-backed key storage.
- Added product naming and Team section.
- Rebuilt the roadmap to merge the old phase-based plan with the deck's actual Short/Mid/Long-term scalability plan.
- Softened hardware requirements section — glasses-specific FRs moved to a "Future Hardware" appendix instead of core MVP requirements.

---

## 1. Executive Summary & Vision

**Vision:** To empower dementia patients to live with dignity, confidence, and improved social connection by providing a real-time, non-intrusive AI assistant that bridges the gap between their cognitive challenges and their daily interactions.

**Mission:** Deliver an AI-powered mobile memory assistant (SmritiAI) that uses on-device computer vision and NLP to recognize faces, recall conversations, and surface emotional context — reducing patient anxiety and caregiver burden — with a future path to a wearable (smart glasses) form factor.

**Problem Statement:**
55 million+ dementia patients worldwide cannot recognize faces, recall names, or remember conversations — yet no real-time assistive solution exists to help them in the moment. This leads to anxiety, confusion, social isolation, and caregiver dependency.

**Gap in existing solutions:** Current tools (medication reminders, basic notes) are passive. Nothing on the market combines real-time face recognition, conversation recall, and context memory.

**Value Proposition:** SmritiAI is a real-time, privacy-first memory assistant — the first to pair face recognition with *emotion-aware* memory logging, acting as a cognitive prosthesis for the user while keeping sensitive biometric data on-device.

---

## 2. Target Audience & User Personas

- **Primary User — Dementia Patient (Early to Mid-Stage):** Experiences memory loss, face blindness (prosopagnosia), and difficulty recalling recent events. Capable of using a smartphone app with voice commands.
  - *Pain points:* Social embarrassment, confusion, frustration, withdrawal from social situations.
- **Secondary User — Caregiver / Family Member:** Responsible for the patient's well-being; sets up the app and registers faces.
  - *Pain points:* Constant need for supervision, anxiety about safety and social interactions, lack of visibility into the patient's day.
- **Tertiary User — Healthcare Professionals:** Monitor patient progress in a clinical setting (future integration).

---

## 3. Product Naming

- **App name:** SmritiAI
- **Project/theme name (pitch deck):** "AI Powered Lens" — used as the umbrella concept name; the shipped product is the SmritiAI app.
- **Team name:** Reconcile "Team Garuda" (title slide) vs. "Team Chromium" (team slide) before external distribution.

---

## 4. Competitor Analysis

Existing tools address adjacent problems but none combine real-time face recognition, conversation recall, and emotional context:

- **Medication reminder apps** (e.g., generic pill-reminder apps): Solve adherence, not recognition or recall — no camera, no NLP, no memory of interactions.
- **Basic caregiver note-taking / photo album apps:** Let caregivers log photos and notes manually, but provide no real-time in-the-moment assistance to the patient and no AI-driven context or emotion tagging.
- **General-purpose face recognition apps:** Can identify a pre-tagged face but stop there — no relationship context, no conversation recall, no mood tracking, and typically not designed for a cognitively impaired user.
- **Dementia care platforms (clinical/EHR-adjacent tools):** Focus on caregiver-side reporting and clinical tracking, not real-time, in-the-moment patient-facing assistance.

**SmritiAI's gap-fill:** it is the only category entrant that pairs on-device face recognition with conversational recall *and* emotion-aware memory logging, in a single patient-facing, privacy-first app.

---

## 5. User Stories

1. **Face Recognition:** *As a patient*, I want to be told who's in front of me and my relationship to them, so I can greet them confidently.
2. **Conversation Recall:** *As a patient*, I want to ask "Who is this?" or "What are we talking about?" and get a discreet, brief answer.
3. **Emotional Context:** *As a patient*, I want my memories tagged with how I felt, so I retain the emotional connection even when facts fade.
4. **Memory Logging:** *As a patient or caregiver*, I want to add and review memories (with photos, notes, and mood) in a simple diary.
5. **Discreet Assistance:** *As a patient*, I want information delivered simply through the app / voice, without drawing attention in social settings.
6. **Safety & Peace of Mind:** *As a caregiver*, I want summaries of interactions and mood so I can check in on my loved one's well-being.
7. **Ease of Setup:** *As a caregiver*, I want a simple "Add Person" flow — capture a face, enter name and relationship — with no technical complexity.
8. **Ease of Use:** *As a patient*, I want the app's home screen to offer a small number of clear actions (Ask Smriti AI, Recognize Person, Add Memory, Memory History).

---

## 6. Functional Requirements

### 5.1 Mobile App (Primary Product)
- **FR-APP-01:** Native Android app (Kotlin) with a home screen offering: Ask Smriti AI, Recognize Person, Add Memory, Memory History.
- **FR-APP-02:** Large text, high-contrast, minimal-step UI suitable for cognitively impaired users and caregivers.
- **FR-APP-03:** "Add Person" flow: capture face photo, enter name and relationship, optionally record an audio memory/voice note.

### 5.2 Face Recognition Module
- **FR-FR-01:** Detect and match faces in real-time from the camera feed using an on-device face detection model (ML Kit) and an on-device TFLite embedding model.
- **FR-FR-02:** On match, display the person's name, relationship, and a match confidence score (e.g., "Matched: Laura (93%)").
- **FR-FR-03:** Define a **confidence threshold of 80%**. Matches ≥80% are shown as a confirmable result ("Use match"); matches below 80%, or no match found, trigger the Unknown Person flow (FR-FR-05) instead of a silent failure.
- **FR-FR-04:** Performance target: recognition result returned in under 2 seconds from capture.
- **FR-FR-05: Unknown Person Flow.** When no face is detected or confidence is below the 80% threshold, the app must not fail silently. It should prompt: *"I don't recognize this person — would you like to add them to your memory diary?"*, offering a direct path into the Add Person flow (FR-APP-03), or a "Skip (no face)" option to dismiss without guessing.

### 5.3 Conversation & Voice AI Assistant ("Ask Smriti AI")
- **FR-VA-01:** Use Android's native SpeechRecognizer for speech-to-text.
- **FR-VA-02:** Send transcript + relevant context (recognized people, recent memory log) to an LLM (Grok (x.ai) API) to answer patient questions such as "Who is my sister?" or "What are we talking about?"
- **FR-VA-03:** Respond via Android TextToSpeech (voice) with text also shown in a chat-style interface.
- **FR-VA-04:** Assistant responses should reference stored relationship and memory data, not just generic answers.

### 5.4 Emotion-Aware Memory (Novelty Feature)
- **FR-EM-01:** Run sentiment/emotion analysis on captured conversation transcripts (via the same LLM call used for summarization).
- **FR-EM-02:** Tag each saved memory with a detected mood (e.g., Neutral, Happy, Anxious) alongside a text summary.
- **FR-EM-03:** Display mood alongside memory entries in Memory History and in assistant responses (e.g., "Laura (Sister) added to your memory diary. Mood: Neutral").
- **FR-EM-04:** Mid-term goal: expand from 3–4 emotional states to a broader emotion taxonomy.

### 5.5 Memory Diary (Add Memory / Memory History)
- **FR-MD-01:** Allow manual creation of a memory entry with photo, name/relationship link, audio note, and timestamp.
- **FR-MD-02:** Auto-generate an AI summary and mood tag for each memory from captured audio (via LLM).
- **FR-MD-03:** Memory History screen lists past entries chronologically, filterable by person.
- **FR-MD-04:** Show "Last Seen" per contact based on most recent recognition/memory event.

### 5.6 Caregiver Dashboard (MVP — moved from Mid-Term in v3.1)
- **FR-CA-01:** Provide caregivers a summary view of patient activity logs, recognized faces, and mood trends.
- **FR-CA-02:** Support configurable anomaly alerts (e.g., unrecognized/"stranger" face detected repeatedly).
- **FR-CA-03:** Sync only de-identified/minimized metadata to any shared or cloud view, per NFR-S-04.
- **FR-CA-04:** Dashboard is served by a thin, MVP-scoped sync backend (auth, storage, read API) — not the full cloud-native backend the v1 PRD originally assumed. See Section 8 for architecture.

---

## 7. Non-Functional Requirements (NFRs)

### 6.1 Performance
- **NFR-P-01:** Face recognition + assistant response latency target: under 2 seconds end-to-end.
- **NFR-P-02:** Face recognition must tolerate varying lighting and angles.
- **NFR-P-03:** Speech recognition must remain usable in moderately noisy environments.

### 6.2 Privacy & Security (CRITICAL)
- **NFR-S-01: On-device processing.** Face detection/matching, speech-to-text, and text-to-speech run **entirely on-device**. Only summarization/emotion analysis text is sent to an external LLM API (Grok (x.ai)) — raw audio, images, and biometric embeddings are not sent to the cloud.
- **NFR-S-02: On-device encryption.** All locally stored data is encrypted with AES-256 before it leaves the phone's secure storage.
- **NFR-S-03: Data separation.** Identifying information (names, relationships) is stored separately from behavioral/interaction data to limit blast radius of any single compromise.
- **NFR-S-04: Pseudonymization.** Where data must be shared or synced (e.g., caregiver dashboard, future cloud features), real names are replaced with random IDs.
- **NFR-S-05: Secure key storage.** Encryption keys are stored in hardware-backed secure storage (Android Keystore), not in application code or shared preferences.
- **NFR-S-06: Consent.** Patients and caregivers must give explicit, transparent consent before any face/audio capture during onboarding.
- **NFR-S-07: Data minimization.** Only essential metadata (e.g., "Met with daughter at 2:00 PM, mood: neutral") is retained for any shared caregiver view.

### 6.3 Usability
- **NFR-U-01:** Home screen limited to a small set of large, clearly labeled actions.
- **NFR-U-02:** Assistant interactions should default to voice output to reduce reliance on reading small text.

### 6.4 Reliability & Scalability
- **NFR-R-01:** Local database (Room/SQLite) must remain performant with growing numbers of registered faces and memory entries on-device.
- **NFR-R-02:** Any cloud-dependent features (LLM calls, future dashboard sync) must degrade gracefully offline — core face recognition and recall must work without connectivity.

---

## 8. Technical Architecture

### 7.1 Layers
1. **Input Layer:** Phone camera + microphone.
2. **Processing Layer (on-device):**
   - *Face processing:* ML Kit detection → on-device TFLite embedding extraction → on-device face matching.
   - *Audio processing:* Android SpeechRecognizer → transcript.
3. **Intelligence Layer:** LLM (Grok (x.ai) API) for summarization and emotion analysis of transcripts; validation logic; "last seen" calculation.
4. **Storage Layer:** Room / SQLite local database — all data stays on-device by default.
5. **Output Layer:** Android TextToSpeech (voice) + UI display (name, relationship, summary, mood, last seen).
6. **Sync Layer (MVP, added v3.1):** Thin backend (auth, storage, read API) that receives only pseudonymized, minimized behavioral metadata from the device and serves it to the Caregiver Dashboard. No embeddings, raw audio, or raw images ever reach this layer.

### 7.2 Tech Stack
| Layer | Technology |
|---|---|
| Frontend | Kotlin, native Android app (Android Studio) |
| Face Recognition | Google ML Kit (detection) + on-device TFLite (embedding) + on-device similarity search |
| Speech-to-Text | Android SpeechRecognizer |
| Voice Output | Android TextToSpeech |
| NLP (summarization, emotion, Q&A) | Grok (x.ai) API |
| Local Database | Room (SQLite) |
| Sync Backend (MVP, added v3.1) | Thin service — auth, storage, read API — for pseudonymized/minimized metadata only |
| Caregiver Dashboard (MVP, added v3.1) | Web or companion view reading from the sync backend |

> **Note:** This replaces the v1 architecture's assumption of a cloud-native backend (API Gateway, cloud STT via Whisper, vector DB). The actual design is edge-first; only summarization/emotion text calls and dashboard-bound metadata leave the device — see `architecture.md` for the full sync backend and Caregiver Dashboard design.

---

## 9. Key Metrics & Success Criteria

- **Face match accuracy:** >95% in standard lighting (deck example shows a 93% match displayed to the user — confirm this is a realistic representative number, not just a demo value).
- **Latency:** Under 2 seconds from question/capture to response.
- **Adoption:** Caregiver onboarding/registration completion rate >80%.
- **Dashboard engagement (MVP, added v3.1):** Caregiver dashboard weekly active usage rate — target TBD, needs a baseline once the dashboard ships.
- **Patient outcomes:** Reduction in reported confusion/anxiety (via caregiver surveys).
- **Caregiver outcomes:** Reduction in perceived caregiving burden (via validated scales).

---

## 10. Potential Risks & Mitigation

| Risk | Mitigation |
|---|---|
| On-device model accuracy in low light / poor angles | Use lightweight but robust on-device models (ML Kit + TFLite); prompt user to recapture on low confidence |
| LLM cost/latency for summarization at scale | Batch or throttle summarization calls; cache repeated context |
| Privacy/data misuse concerns | Privacy-by-design: on-device-first processing, pseudonymization, hardware-backed keys, explicit consent |
| Emotion detection misclassification affecting patient trust | Keep emotion tags as supportive context, not diagnostic; allow caregiver correction |
| Social stigma (future wearable) | Deck notes glasses should look like standard eyewear — revisit once wearable phase begins |
| Multi-language accuracy (Hindi + English) | Dedicated short-term roadmap item to train/tune STT and NLP for both languages |
| New network-facing surface from pulling the Caregiver Dashboard into MVP (auth, sync backend, minimized-data leakage risk) | Keep the sync backend thin and pseudonymized by design (NFR-S-04, NFR-S-07); scope auth tightly; do not let this backend grow into the general-purpose cloud backend that was explicitly descoped in v2/v3 |

---

## 11. Roadmap

### Short Term
- Multi-language support (Hindi + English)
- Improve face recognition accuracy (train on higher-quality dataset)
- Basic cloud deployment for backend services (AWS/GCP) supporting LLM calls

### Mid Term
- Emotion detection expansion (3–4 → broader emotional states)
- Wearable integration: connect with smartwatch for real-time data (first step toward the smart-glasses vision)

### Long Term
- International expansion (2–3 countries, localization, ≥10,000 active users)
- Healthcare API connections (1–2 hospital/health platform integrations)
- AR-based health insights (reminders/instructions via AR overlay — this is where the original "smart glasses" hardware vision re-enters)

---

## 12. Appendix: Future Hardware Vision (Not Current MVP Scope)

The pitch deck's "AI Powered Lens" concept envisions an eventual smart-glasses form factor. These requirements are **aspirational, long-term**, and out of scope for the current SmritiAI mobile app MVP:

- Lightweight, standard-eyewear-like glasses with front-facing camera, microphone array, HUD or visual indicator, and bone-conduction audio.
- 8–12 hour battery life; Bluetooth/Wi-Fi pairing to phone.
- Should only be scoped in detail once the mobile app has validated the core recognition/recall/emotion-tagging experience.

---

## 13. Team

| Name | Role | Responsibilities |
|---|---|---|
| Kumar Ashutosh | Team Lead | Project coordination, presentation & pitch, problem framing |
| Maahi Choudhary | AI/ML Engineer | Face recognition model, real-time AI pipeline, model optimization |
| Rhythm Grover | App Developer | Mobile app UI/UX, screen overlays & flow, accessibility design, **SpeechRecognizer & TextToSpeech integration** |
| Ritvik Jain | Backend Developer *(scope now centers on the Intelligence Layer rather than a traditional backend)* | Local memory database (Room/SQLite) setup, **LLM API & NLP integration** for summarization/emotion analysis, data security & storage |

> **Open gap (v3.1):** Pulling the Caregiver Dashboard into MVP scope adds a sync backend and web/companion dashboard that isn't clearly owned by any role above — Ritvik Jain's scope is described as the on-device Intelligence Layer, not a hosted backend. This needs an explicit owner (or scope decision) before MVP build starts.

*Theme: Healthcare + AI · PS Category: Software + Hardware*
