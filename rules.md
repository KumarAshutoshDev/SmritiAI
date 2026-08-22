# SmritiAI — Rules for AI Coding Assistants

**Source of truth:** `SmritiAI_PRD_v3.1.md` + `architecture.md`. If this file and either of those disagree, the PRD/architecture win — flag the conflict instead of silently picking one.
**Applies to:** any AI assistant (Claude, Copilot, Cursor, etc.) generating or modifying code in this repo.

---

## 1. Non-negotiables (violating these is a stop-and-ask, not a judgment call)

1. **Biometric data never leaves the device.** Face embeddings, raw audio, raw images/photos are processed and stored on-device only. Never write code that transmits any of these over the network, to a log, to a crash reporter, or to an LLM API call — including as debug output. (NFR-S-01)
2. **Only transcript/summary *text* crosses the network**, and only to the LLM API (Grok (x.ai)) for summarization, emotion analysis, and Q&A — or to the sync backend as pseudonymized, minimized metadata for the Caregiver Dashboard. No other network egress paths for user data.
3. **Identity data and behavioral data stay in separate stores.** Never join, denormalize, or cache them into a single table/object that could be exfiltrated or logged as one unit. Link only via internal ID. (NFR-S-03)
4. **Anything sent to the sync backend must be pseudonymized and minimized first**, on-device, before it leaves the phone. Real names/relationships are never sent — only random IDs plus the specific minimized fields the dashboard needs (e.g. "Met with daughter at 2:00 PM, mood: neutral"). (NFR-S-04, NFR-S-07)
5. **All local storage is AES-256 encrypted; keys live in Android Keystore only.** Never store keys in code, resources, SharedPreferences, or environment variables. (NFR-S-02, NFR-S-05)
6. **No face/audio capture before explicit consent is recorded.** Any capture flow (Add Person, Add Memory, Recognize) must check a consent flag first. (NFR-S-06)
7. **Do not introduce a general-purpose cloud backend.** The sync backend is intentionally thin (auth, storage, read API) and exists *only* to serve the Caregiver Dashboard. If a task seems to need more backend surface than that, stop and flag it — don't quietly grow it into the cloud-native backend that was explicitly descoped in v1→v2.
8. **Core face recognition and memory recall must work fully offline.** Never make these features hard-depend on network availability. Only Q&A, summarization, and emotion tagging are allowed to require connectivity, and they must degrade gracefully (clear "offline" state, not a crash or silent failure) when it's absent. (NFR-R-02)

---

## 2. Fixed tech stack — don't substitute without asking

| Layer | Required tech | Do not substitute with |
|---|---|---|
| App | Kotlin, native Android (Android Studio) | Flutter, React Native, Java-only, cross-platform frameworks |
| Face detection / embedding / matching | Google ML Kit (detection) + on-device TFLite (embedding) + on-device similarity search | Cloud-based face recognition APIs (Azure Face, AWS Rekognition, etc.) |
| Speech-to-text | Android `SpeechRecognizer` | Cloud STT (Whisper API, Google Cloud Speech) for the on-device flows |
| Text-to-speech | Android `TextToSpeech` | Third-party TTS SDKs |
| NLP (summarization/emotion/Q&A) | Grok (x.ai) API, text-only calls | Any call that sends images, audio, or embeddings |
| Local DB | Room (SQLite) | Realm, raw SQLite, cloud-synced local DBs (Firebase Realtime DB as local store) |
| Key storage | Android Keystore | Custom key management, hardcoded keys |
| Sync backend (MVP, dashboard only) | Thin service: auth + storage + read API | A full backend framework doing business logic beyond serving the dashboard |

If a task seems to require a technology outside this table, that's a signal to check with the team before installing/adding it — not to just add the package.

---

## 3. Architectural boundaries

- Respect the six layers in `architecture.md` §2–3: Input → Processing (on-device) → Intelligence (orchestration) → Storage → Output, plus the new Sync Layer. Code for one layer shouldn't reach across into another's responsibility (e.g. UI code shouldn't call the LLM API directly — that goes through the Intelligence Layer's orchestration).
- The **Intelligence Layer is an orchestrator, not a database.** Don't add persistent state to it that belongs in Storage.
- The **face-match confidence threshold is 80%**, defined once (FR-FR-03). Don't hardcode this number in multiple places — reference a single constant. Below 80% or no match → Unknown Person flow (FR-FR-05), never a silent failure or a guessed name.
- **Latency target: under 2 seconds**, capture-to-response, for recognition and assistant replies (NFR-P-01). Flag any change that risks this budget (e.g. adding a network round-trip to a previously on-device path).
- **Sync backend and Caregiver Dashboard currently have no assigned owner on the team** (per PRD v3.1 §13 open gap). Don't assume conventions for this layer that haven't been agreed — ask before establishing patterns here that others will have to follow.

---

## 4. Data model rules

- **Identity store:** name, relationship, face embedding reference, contact metadata. Higher sensitivity — treat as the more restricted store.
- **Behavior store:** memory entries (photo ref, audio ref, transcript, AI summary, mood tag, timestamp), derived "last seen" values. Linked to Identity store by internal ID only — never duplicate identity fields into this store.
- Index the Behavior store on contact ID and timestamp (NFR-R-01) — Memory History and "last seen" queries depend on this as data grows.
- Any new field added to either store: decide which store it belongs in using the sensitivity test above before writing migration code, and update NFR-S-03 documentation if the boundary shifts.

---

## 5. Product/UX constraints that affect implementation

- Home screen exposes exactly four actions: Ask Smriti AI, Recognize Person, Add Memory, Memory History (FR-APP-01). Don't add a fifth top-level action without a product decision — this is a deliberate simplicity constraint for cognitively impaired users (NFR-U-01).
- UI must default to large text, high contrast, minimal steps (FR-APP-02). Assistant responses default to voice output, with text as a secondary channel (NFR-U-02) — don't build voice as an afterthought bolted onto a text-first flow.
- Unknown Person flow must always offer a way out ("Add to memory diary" or "Skip (no face)") — never leave the user on a dead-end screen (FR-FR-05).

---

## 6. Scope guardrails

Do not implement, scaffold, or add dependencies for anything in the PRD's explicit non-scope list (architecture.md §9) unless a task explicitly asks for it:
- Smart-glasses/wearable hardware integration
- A vector database or cloud-hosted embedding store
- Any backend beyond the thin sync service defined above
- Multi-language support, healthcare API integrations, AR overlays — these are roadmap items, not current build targets

If a request seems to need one of these, say so and confirm scope before proceeding rather than building toward it speculatively.

---

## 7. When PRD/architecture and a request conflict

If a coding request would violate a rule above (e.g. "just send the photo to the LLM too, it's easier"), don't silently comply and don't silently refuse — explain the conflict with the specific NFR/FR it touches and ask how to proceed. These privacy/architecture rules exist because they're load-bearing for the product's core value proposition (privacy-first, on-device biometric processing), not arbitrary style preferences.
