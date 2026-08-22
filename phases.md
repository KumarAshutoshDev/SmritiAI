# SmritiAI — Build Phases

**Source of truth:** `SmritiAI_PRD_v3.1.md` + `architecture.md`. Each phase below maps to specific FR/NFR codes so scope stays traceable back to the PRD.
**Rule:** don't start a phase's dependencies before the phase that produces them is done — see the dependency notes under each phase.

---

## Phase 0 — Foundations
**Goal:** Nothing user-facing yet; the scaffolding everything else depends on.

- Android project setup (Kotlin, Android Studio), base app shell, navigation
- Room/SQLite schema: Identity store + Behavior store, kept separate per NFR-S-03
- AES-256 encryption at rest wired up; keys in Android Keystore (NFR-S-02, NFR-S-05)
- Consent gate flow (NFR-S-06) — must exist before any capture code is written, since every later phase's flows depend on it

**Exit criteria:** empty app runs, encrypted DB stores/reads a dummy record, consent screen blocks capture until accepted.

---

## Phase 1 — Face Recognition Core
**Goal:** The single most central flow — recognize a person and show who they are.

- ML Kit face detection + on-device TFLite embedding extraction (FR-FR-01)
- Face Matcher against Identity store, with the 80% confidence threshold as a single shared constant (FR-FR-03)
- Match result UI: name, relationship, confidence score (FR-FR-02)
- Unknown Person flow: prompt to add or skip, never a silent failure or guess (FR-FR-05)
- Add Person flow: capture photo, name, relationship, optional voice note (FR-APP-03)
- Performance check against the <2s target (NFR-P-01, NFR-P-04)

**Depends on:** Phase 0 (Identity store, consent gate).
**Exit criteria:** a registered face is recognized and displayed within 2 seconds; an unregistered face triggers the Unknown Person flow, not a guess.

---

## Phase 2 — Voice Assistant ("Ask Smriti AI")
**Goal:** Patient can ask a question and get a spoken answer grounded in their own data.

- Android `SpeechRecognizer` integration (FR-VA-01)
- Intelligence Layer orchestration: assemble context (recognized person, recent memory) before calling the LLM (FR-VA-02, FR-VA-04)
- LLM API call (OpenAI/Gemini) — transcript + context only, never audio/images (NFR-S-01)
- `TextToSpeech` output + chat-style UI (FR-VA-03)
- Offline degradation: clear "needs connectivity" state, core recognition still works (NFR-R-02)

**Depends on:** Phase 1 (recognized-person context to feed the assistant).
**Exit criteria:** a spoken question about a known person returns a correct, spoken answer; going offline degrades gracefully instead of crashing.

---

## Phase 3 — Memory Diary
**Goal:** Patients/caregivers can log and browse memories manually, not just via recognition.

- Add Memory flow: photo, contact link, audio note, timestamp (FR-MD-01)
- On-device transcription of the audio note (`SpeechRecognizer`, reused from Phase 2)
- AI summary generation via LLM (FR-MD-02) — reuses the Intelligence Layer call pattern from Phase 2
- Memory History screen: chronological, filterable by person (FR-MD-03)
- "Last Seen" calculation per contact (FR-MD-04) — needs the Behavior store indexed on contact ID + timestamp (NFR-R-01)

**Depends on:** Phase 1 (Identity store/contacts to link to), Phase 2 (LLM call pattern, transcription).
**Exit criteria:** a manually added memory appears correctly in Memory History with a working "Last Seen" value.

---

## Phase 4 — Emotion-Aware Memory
**Goal:** The product's novelty differentiator — mood tagging layered onto what's already working.

- Sentiment/emotion analysis piggybacking on the existing LLM summarization call (FR-EM-01) — don't add a second LLM round-trip
- Mood tag storage alongside memory summaries (FR-EM-02)
- Mood surfaced in Memory History and assistant responses (FR-EM-03)

**Depends on:** Phase 2 and 3 (the LLM call and memory storage this attaches to).
**Exit criteria:** every new memory (from recognition or manual add) carries a mood tag visible in both the diary and assistant responses.

---

## Phase 5 — Sync Backend + Caregiver Dashboard (moved into MVP in v3.1)
**Goal:** Caregivers get real visibility, not just the patient-facing app.

⚠️ **No team member is currently assigned to this phase** (PRD v3.1 §13 open gap) — resolve ownership before starting, not during.

- On-device sync client: pseudonymize + minimize outbound records before transmission (FR-CA-03, NFR-S-04, NFR-S-07)
- Thin sync backend: auth, storage, read API only — resist scope creep toward a general backend (FR-CA-04)
- Caregiver Dashboard (web/companion view): activity logs, recognized-face summary, mood trends (FR-CA-01)
- Configurable anomaly alerts, e.g. repeated unrecognized face (FR-CA-02)
- Offline behavior: dashboard shows last-synced state rather than failing (NFR-R-02)

**Depends on:** Phase 1–4 (this is the phase that reads and syncs data all of those produce).
**Exit criteria:** a caregiver can see activity/mood trends for their patient with no real names ever having left the device; sync gracefully handles being offline.

---

## Phase 6 — Hardening & Launch Readiness
**Goal:** Cross-cutting NFRs that touch every phase above — don't treat as an afterthought.

- Lighting/angle robustness testing for face recognition (NFR-P-02)
- Noisy-environment testing for speech recognition (NFR-P-03)
- Load-test Room/SQLite as face/memory counts grow (NFR-R-01)
- Security review pass: confirm no biometric data anywhere in logs, crash reports, or LLM payloads (NFR-S-01)
- Usability pass against the four-action home screen and voice-first defaults (NFR-U-01, NFR-U-02)
- Metrics instrumentation: face match accuracy, latency, caregiver onboarding completion, dashboard engagement (PRD §9)

**Depends on:** all prior phases functionally complete.
**Exit criteria:** metrics in PRD §9 are instrumented and reporting; no open NFR gaps from Section 7.

---

## Explicitly out of scope for these phases

Per PRD §12 and architecture.md §9 — do not schedule work for these until a future roadmap pass:
- Smart-glasses/wearable hardware
- Multi-language support (Hindi + English) — short-term roadmap, not MVP
- Full cloud-native backend beyond the thin sync service in Phase 5
- Healthcare API integrations, AR overlays — long-term roadmap

---

## Phase dependency summary

```
Phase 0 (Foundations)
   └─▶ Phase 1 (Face Recognition)
          └─▶ Phase 2 (Voice Assistant)
                 └─▶ Phase 3 (Memory Diary)
                        └─▶ Phase 4 (Emotion-Aware Memory)
                               └─▶ Phase 5 (Sync Backend + Dashboard)
                                      └─▶ Phase 6 (Hardening & Launch)
```

Phases are mostly sequential because each on-device feature builds on the Identity/Behavior store and LLM call pattern established earlier — but Phase 4 (emotion tagging) and Phase 3 (memory diary) could run in parallel with enough hands, since both depend on Phase 2's LLM pattern rather than on each other.
