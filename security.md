# SmritiAI — Security Rules & Common Bugs

**Source of truth:** `SmritiAI_PRD_v3.1.md` §6.2 (NFR-S-01–07) + `architecture.md` §5. This file translates those NFRs into concrete coding rules and the failure modes to watch for, since "it compiles and the demo works" is not the same as "it's safe."

---

## 1. Why this file exists

AI coding assistants optimize for the feature working, not for the feature being safe. Left unguided, they will happily:
- Skip input validation because the happy path demos fine without it
- Log sensitive data "temporarily for debugging" and leave it in
- Swallow exceptions with an empty catch block so the app doesn't crash on stage
- Send more data to an API call than the feature actually needs, because it's easier than filtering it
- Store a secret in code or in a config file that gets committed

For SmritiAI specifically, the stakes are higher than usual: the data involved is biometric (faces, voice) and belongs to a cognitively vulnerable user population. A privacy bug here isn't just embarrassing — it undermines the entire value proposition ("privacy-first memory assistant"). Treat every rule below as load-bearing, not stylistic.

---

## 2. Hard rules

### 2.1 Data never leaves the device unless explicitly allowed
- Face embeddings, raw photos, raw audio: **never** appear in a network call, a log line, a crash report, an analytics event, or an LLM prompt. This includes "just for testing" debug builds — gate any such logging behind a build flag that is never true in a build anyone else runs.
- The only two network egress paths that exist for user data: (a) transcript text to the LLM API, (b) pseudonymized/minimized metadata to the sync backend. If a new feature seems to need a third path, that's a stop-and-ask, not a quiet addition (NFR-S-01).
- Before writing any LLM API call, check exactly what's in the payload. A common bug: passing a whole object (e.g. a Memory entity) into the prompt/context builder because it's convenient, instead of picking only the fields that are supposed to leave the device.

### 2.2 Encryption and key handling
- All local storage (Identity store, Behavior store) is AES-256 encrypted at rest (NFR-S-02). Never add a new Room table or DataStore file that bypasses this.
- Keys live in Android Keystore only. Common bugs to avoid: a key hardcoded as a string constant, a key derived from a predictable value (device ID, package name), a key stored in SharedPreferences "temporarily."
- Don't roll your own crypto. Use Android's Keystore-backed `Cipher`/`EncryptedSharedPreferences` / Jetpack Security APIs rather than a custom AES implementation.

### 2.3 Data separation
- Identity data and Behavior data stay in separate stores, linked only by internal ID (NFR-S-03). A common shortcut bug: joining them into a single denormalized object "to make the UI code simpler" — this defeats the blast-radius containment the separation exists for. If a screen needs both, join at read time in the view layer, don't merge at the storage layer.

### 2.4 Pseudonymization at the sync boundary
- Anything queued for the sync backend must be pseudonymized (random ID instead of real name) and minimized (only the specific fields the dashboard needs) **before** it leaves the device (NFR-S-04, NFR-S-07). This transformation happens in the on-device sync client — never assume the backend will "clean it up" server-side. If the backend ever receives a real name, that's a bug, not a data-cleanup task.
- Test this explicitly: log (locally, in a test) what a sync payload actually contains before shipping any change to the sync client.

### 2.5 Consent gating
- Every capture path (Add Person's photo, Add Memory's audio, Recognize Person's camera frame) must check a consent flag first (NFR-S-06). Common bug: a new capture entry point added later (e.g. a quick-add shortcut) that skips the consent check because it reuses low-level camera/mic code directly instead of going through the gated capture flow.

### 2.6 Input validation and error handling
- Validate all user- and LLM-provided input before it's stored or displayed — this includes LLM responses. A malformed or unexpectedly-shaped LLM response should fail safely (show a fallback message), not be trusted and rendered/stored as-is.
- No empty or overly broad catch blocks. A `catch (e: Exception) {}` that silently swallows a failure is a common AI-generated pattern that hides broken consent checks, failed encryption, or failed pseudonymization — these must fail loud (log locally, surface an error state), not fail silent.
- Network calls (LLM API, sync backend) must handle failure and timeout explicitly, feeding into the offline-degradation behavior required by NFR-R-02 — not an uncaught exception or an infinite spinner.

### 2.7 Auth (sync backend / dashboard, Phase 5)
- The sync backend's auth must actually authenticate the caregiver as the correct patient's caregiver — not just "any authenticated user can read any patient's data." This is an easy corner to cut when the backend is described as "thin."
- No default/sample credentials left in place from scaffolding. No auth tokens logged or embedded in URLs.

### 2.8 Dependency and secret hygiene
- Don't add a new third-party SDK/package (especially anything with network access — analytics, crash reporting, ad SDKs) without checking whether it could capture biometric data incidentally (e.g. a generic crash reporter that auto-attaches screenshots).
- No API keys (LLM provider, sync backend) committed to source. Use local config not checked into version control, consistent with how the project already keeps keys out of `rules.md`'s "no hardcoded secrets" rule.

---

## 3. Common vibe-coded bugs to check for in review

| Pattern | Why it's a problem here |
|---|---|
| Logging a full request/response object for debugging | Likely contains transcript text, embeddings, or PII; easy to forget to remove before shipping |
| Passing a whole entity into an LLM prompt builder | Sends more data off-device than the feature needs |
| Empty/broad catch blocks | Hides failed encryption, failed consent checks, failed pseudonymization |
| Hardcoded API keys/secrets "just to get it running" | Ends up committed; especially bad for the LLM API key and sync backend auth |
| Skipping the consent check on a new/shortcut entry point to a capture flow | Violates NFR-S-06 without an obvious symptom in normal testing |
| Denormalizing Identity + Behavior data into one object for UI convenience | Undermines NFR-S-03's blast-radius containment |
| Trusting LLM output as pre-validated (e.g. rendering it as HTML, or storing it without a length/shape check) | LLM responses are external input and should be treated as such |
| Sync client sending a real name "just this once" for a debug/test payload | Directly violates NFR-S-04; test payloads need the same pseudonymization path as production |
| No timeout/retry handling on LLM or sync calls | Produces a hang or crash instead of the required graceful offline degradation |

---

## 4. Pitch-facing summary — Security Measures Taken

*For pitch decks, demo narration, and judge Q&A. Plain-language version of the technical controls above.*

**SmritiAI is built privacy-first, not privacy-added-later.** The core design decision is that the most sensitive data — a user's face and voice — never leaves their phone.

- **On-device biometric processing.** Face recognition and voice recognition run entirely on the device using Google ML Kit and Android's native speech APIs. Face embeddings, photos, and raw audio are never transmitted anywhere — not to our servers, not to any third party.
- **Minimal, deliberate cloud use.** The only data that ever leaves the device is (1) text transcripts sent to an LLM for summarization and Q&A, and (2) a small, anonymized activity summary sent to power the caregiver dashboard. Nothing else crosses the network.
- **Encrypted at rest, everywhere.** All data stored on the phone is encrypted with AES-256, with encryption keys held in the phone's hardware-backed secure enclave (Android Keystore) — not in our code, not recoverable by us.
- **Separated storage by sensitivity.** Who someone is (name, relationship) is stored separately from what happened (conversations, moods, timestamps), so a single point of compromise can't expose a complete picture of the patient's life.
- **Anonymized before it's shared.** When data is used to power the caregiver dashboard, real names are replaced with random IDs before the data ever leaves the phone — the dashboard backend never sees who anyone actually is.
- **Consent by design.** No face or voice is ever captured without an explicit, transparent consent step during setup.
- **Built to fail safely.** If connectivity drops, the app doesn't break — core recognition and memory recall keep working offline, and only the LLM-dependent features (like Q&A) pause until connectivity returns.

**One line for the deck:** *"The only things that ever leave the phone are anonymized text summaries — never a face, never a voice recording, never a raw photo."*
