# SmritiAI — Project Todo List (Team of 4)

> Derived from `SmritiAI_PRD_v3.1.md`, `architecture.md`, `rules.md`, `phases.md`, `security.md`, `design.md`.
> Split by **owner** across 4 people. Each task has an **Owner**, and a **Depends on**
> column pointing to task numbers that must land first — check that before you start
> pulling, so you're never blocked mid-sprint by someone else's unmerged branch.

---

## 0. Team & Branch Legend

| Owner tag | Name | Owns |
|---|---|---|
| **KA** | Kumar Ashutosh | Coordination, phase exit-criteria sign-off, process/ownership decisions, pitch & demo framing |
| **MC** | Maahi Choudhary | High-clarity, single-deliverable tasks with an unambiguous pass/fail: setup, scaffolding, constants, straightforward UI stubs and wiring against an already-defined pattern, seeding/dummy data, measurement and instrumentation passes |
| **RG** | Rhythm Grover | Mobile app UI/UX, screen flows, accessibility, SpeechRecognizer & TextToSpeech integration, design system implementation |
| **RJ** | Ritvik Jain | Room/SQLite + encryption, face-matching logic, LLM/Intelligence Layer, sync backend, security-sensitive wiring |

**Branch naming:** `feature/<owner>-<phase#>-<short-desc>`
Examples: `feature/mc-00-room-setup`, `feature/rj-01-face-matcher`, `feature/rg-02-chat-ui`

**Workflow (feature branches + PRs):**
1. Pull latest `main` before starting a task.
2. Branch off `main` using the naming convention above.
3. Commit small, working increments — don't let a branch cover more than 1–2 task rows.
4. Open a PR into `main` as soon as the task is done and runs locally. Tag the others for a quick look — don't let PRs sit.
5. **Whoever finishes a shared contract first** (the `checkConsent()` gate signature, the Identity/Behavior entity shapes, the LLM payload/response shape) posts it in the team chat immediately — others may already be coding against it.
6. Rebase on `main` (don't merge-commit) before opening a PR.
7. If a task depends on something not yet merged, don't start early on the assumption it'll land in time — pull first.

**Cross-owner contracts to nail down before Phase 1** (agree on shape early so no one blocks):
- Identity store / Behavior store entity fields (RJ defines in T07/T08, everyone else builds against them).
- `checkConsent()` function signature (RJ defines in T14 — every capture entry point in later phases calls it).
- LLM payload/response shape for the Intelligence Layer (RJ defines in T33–T35, MC/RG consume it in Phases 3–4).

---

## Phase 0 — Foundations
*(architecture.md §3.1–3.4, rules.md §1/§5–6, phases.md Phase 0, design.md §9)*

| # | Task | Owner | Depends on | Details |
|---|------|-------|-----------|---------|
| 1 | Create Android project (Kotlin, Android Studio), base package structure | **MC** | — | Straightforward scaffolding — follow standard Android Studio project wizard defaults. |
| 2 | Git repo + `.gitignore` excluding keys/config/secrets | **MC** | 1 | Use a standard Android `.gitignore` template; confirm no `local.properties`/keys tracked. |
| 3 | Base app shell: single entry point + empty navigation graph | **RG** | 1 | `MainActivity` + Navigation Component graph, no destinations yet. |
| 4 | Patient-app design tokens per `design.md` §9 — 20px+ body, 7:1 contrast, 56dp touch targets | **RG** | 1 | Colors/type/spacing as Android theme resources. Everything patient-facing builds on this. |
| 5 | Stub screens + routes for the four home-screen actions (FR-APP-01), using the T4 theme | **MC** | 4 | Empty Composable/Fragment per screen, wired into the nav graph. No fifth route. |
| 6 | Add Room dependency; create an empty `AppDatabase` class | **MC** | 1 | `build.gradle` entry + empty `@Database` class, no entities yet. |
| 7 | Identity store entity: name, relationship, face embedding reference, contact metadata (NFR-S-03) | **RJ** | 6 | Sensitivity-classify each field per rules.md §4 before adding. |
| 8 | Behavior store entity: photo ref, audio ref, transcript, AI summary, mood tag, timestamp (NFR-S-03) | **RJ** | 6 | No fields duplicated from Identity store — link by internal ID only. |
| 9 | Index the Behavior store on contact ID + timestamp (NFR-R-01) | **MC** | 8 | Mechanical `@Entity(indices = [...])` addition once T8 is merged. |
| 10 | Generate + store AES-256 key in Android Keystore (Jetpack Security), no hardcoded keys/SharedPreferences (NFR-S-05) | **RJ** | 1 | Security-critical — Keystore-backed `Cipher`, not custom crypto. |
| 11 | Wire Room DB encryption using the Keystore key (NFR-S-02) | **RJ** | 7, 8, 10 | Confirms both stores are encrypted at rest. |
| 12 | Write + read one dummy encrypted record end-to-end | **MC** | 11 | Simple verification test once T11 is merged — clear pass/fail. |
| 13 | Consent screen UI (explicit face/audio capture consent, NFR-S-06), using the T4 theme | **RG** | 4 | Large text, single primary action, per design.md §9.3. |
| 14 | Shared `checkConsent()` gate function — every future capture entry point calls this | **RJ** | 13 | This is a cross-owner contract — post the function signature to the team as soon as it's defined. |
| 15 | Verify Phase 0 exit criteria: app runs, encrypted DB read/write works, consent gate blocks capture | **KA** | 1–14 | Sign-off checkpoint before Phase 1 starts. |

---

## Phase 1 — Face Recognition Core
*(PRD §6 FR-FR-01–05, architecture.md §3.2, phases.md Phase 1)*

| # | Task | Owner | Depends on | Details |
|---|------|-------|-----------|---------|
| 16 | Add the ML Kit face detection dependency | **MC** | 1 | `build.gradle` entry, confirm it resolves. |
| 17 | Camera preview + single-frame capture on Recognize Person screen | **RG** | 5, 16 | CameraX preview bound to the T5 stub screen. |
| 18 | Route Recognize Person's camera capture through `checkConsent()` before any frame is captured (NFR-S-06) | **RJ** | 14, 17 | Third named capture path alongside Add Person/Add Memory — security-sensitive wiring. |
| 19 | Run ML Kit face detection on the captured frame | **RJ** | 16, 18 | Core detection call. |
| 20 | Extract a face embedding from the detected face | **RJ** | 19 | Algorithmic — use on-device TFLite model, feeds the matcher. |
| 21 | Define the 80% confidence threshold as a single shared constant (FR-FR-03) | **MC** | — | One `const val` referenced everywhere, hardcoded nowhere else. |
| 22 | Face Matcher: compare embedding against stored Identity store embeddings, return best match + score | **RJ** | 7, 20, 21 | Core matching logic — security/accuracy sensitive. |
| 23 | Match Result UI: name, relationship, confidence, at body-lg minimum (design.md §9.1) | **RG** | 4, 22 | FR-FR-02. |
| 24 | Unknown Person flow UI: "Add to memory diary" / "Skip" — never a dead end | **RG** | 4, 22 | FR-FR-05. |
| 25 | Add Person flow, step 1: capture face photo | **MC** | 14, 17 | Reuses the T17 camera-capture pattern. |
| 26 | Add Person flow, step 2: name + relationship text inputs | **MC** | 25 | Simple form, T4 theme. |
| 27 | Add Person flow, step 3: optional voice note recording | **MC** | 26 | Straightforward mic-capture UI. |
| 28 | Route the entire Add Person flow through `checkConsent()` | **RJ** | 14, 25–27 | Security-critical — verify all three steps are gated, not just the first. |
| 29 | Save a completed Add Person entry into the encrypted Identity store | **RJ** | 7, 11, 28 | Touches the encrypted store directly. |
| 30 | Measure capture-to-match-display latency; confirm under 2s (NFR-P-01) | **MC** | 22, 23 | Straightforward timing measurement once T22/T23 are merged. |

---

## Phase 2 — Voice Assistant ("Ask Smriti AI")
*(PRD §6 FR-VA-01–04, architecture.md §3.3, phases.md Phase 2)*

| # | Task | Owner | Depends on | Details |
|---|------|-------|-----------|---------|
| 31 | Integrate Android `SpeechRecognizer` for mic capture (FR-VA-01) | **RG** | 1 | Owns Speech/TTS integration per team responsibilities. |
| 32 | Ask Smriti AI chat-style UI shell, using the T4 theme | **MC** | 4 | Empty chat list + input, no LLM wiring yet. |
| 33 | Intelligence Layer context assembler: pull recognized person + recent memory before any LLM call (FR-VA-02/04) | **RJ** | 7, 8, 22 | Cross-owner contract — post the assembled-context shape once defined. |
| 34 | LLM API client: transcript + assembled context only — no images/audio/embeddings (NFR-S-01) | **RJ** | 33 | Security-critical — check exact payload contents before sending. |
| 35 | Payload check/test that fails the call if anything beyond transcript+context text is present | **RJ** | 34 | Guards T34 against future regressions. |
| 36 | Wire `TextToSpeech` as the default response channel, text secondary (NFR-U-02) | **RG** | 31, 32 | Voice-first per design.md §9.5. |
| 37 | Offline detection with a clear "needs connectivity" UI state (NFR-R-02) | **RG** | 32 | Core recognition (Phase 1) must remain unaffected. |
| 38 | Timeout/retry handling on the LLM call — no infinite spinner, no uncaught crash | **RJ** | 34 | security.md §2.6. |
| 39 | Validate the shape of the LLM's response before rendering/storing; fail safely on malformed input | **RJ** | 34 | Build as the reusable check Phase 3/4 LLM calls (T46, T52) also rely on. |
| 40 | Measure end-to-end assistant response latency; confirm under 2s (NFR-P-01) | **MC** | 36, 39 | Straightforward timing measurement once the flow is wired end-to-end. |
| 41 | Verify Phase 2 exit criteria: spoken Q&A works, offline degrades gracefully | **KA** | 31–40 | Sign-off checkpoint before Phase 3 starts. |

---

## Phase 3 — Memory Diary
*(PRD §6 FR-MD-01–04, architecture.md §4.3, phases.md Phase 3)*

| # | Task | Owner | Depends on | Details |
|---|------|-------|-----------|---------|
| 42 | Add Memory flow, step 1: photo capture | **MC** | 14, 17 | Reuses the T25 capture pattern. |
| 43 | Add Memory flow, step 2: contact-link picker (reads Identity store; join happens in the view layer only — security.md §2.3) | **RG** | 7, 42 | UI + read-time join, never a storage-layer merge. |
| 44 | Add Memory flow, step 3: audio note capture + on-device transcription (reuse `SpeechRecognizer` from T31) | **RG** | 31, 42 | Direct reuse of the Phase 2 integration. |
| 45 | Route the Add Memory flow's photo/audio capture through `checkConsent()` | **RJ** | 14, 42–44 | Security-critical — fourth capture path to verify. |
| 46 | AI summary generation, reusing the Intelligence Layer call pattern + validation from T33/T34/T39 (FR-MD-02) | **RJ** | 33, 34, 39 | Extends the existing pattern rather than a new call path. |
| 47 | Write the completed memory entry into the encrypted Behavior store | **RJ** | 8, 11, 46 | Touches the encrypted store directly. |
| 48 | Memory History screen: chronological list, joining Identity + Behavior data at the view layer only | **MC** | 7, 8, 47 | Follows the same view-layer-join pattern established in T43. |
| 49 | Filter-by-person control on Memory History (FR-MD-03) | **MC** | 48 | Straightforward filter UI once T48 is merged. |
| 50 | "Last Seen" calculation per contact, using the T9 index (FR-MD-04) | **RJ** | 9, 47 | Query logic against the indexed Behavior store. |
| 51 | Verify Phase 3 exit criteria: a manual memory entry appears correctly with a working Last Seen value | **KA** | 42–50 | Sign-off checkpoint before Phase 4 starts. |

---

## Phase 4 — Emotion-Aware Memory
*(PRD §6 FR-EM-01–03, phases.md Phase 4)*

| # | Task | Owner | Depends on | Details |
|---|------|-------|-----------|---------|
| 52 | Extend the existing LLM summarization call (T46) to also return a sentiment/emotion tag — no second round-trip (FR-EM-01) | **RJ** | 46 | Prompt/response contract change — post the updated response shape. |
| 53 | Add the mood tag field to the Behavior store write path (FR-EM-02) | **MC** | 8, 52 | Straightforward field addition once T52 defines the shape. |
| 54 | Surface the mood tag in Memory History list items | **MC** | 48, 53 | Small UI addition to an existing screen. |
| 55 | Surface the mood tag in Ask Smriti AI responses where relevant (FR-EM-03) | **MC** | 36, 53 | Small UI addition to an existing screen. |
| 56 | Verify Phase 4 exit criteria: every new memory carries a visible mood tag in both diary and assistant responses | **KA** | 52–55 | Sign-off checkpoint before Phase 5 starts. |

---

## Phase 5 — Sync Backend + Caregiver Dashboard
*(PRD §6 FR-CA-01–04, architecture.md §3.6, phases.md Phase 5 — ⚠️ unresolved owner per PRD §13, resolve first)*

| # | Task | Owner | Depends on | Details |
|---|------|-------|-----------|---------|
| 57 | Confirm/assign an owner for this phase before writing any code for it | **KA** | — | Process task — PRD §13 open gap. Do not let Phase 5 start without this resolved. |
| 58 | Design the pseudonymization mapping: real internal ID → random sync ID | **RJ** | 7 | Security-critical design decision. |
| 59 | On-device sync client's field-selection step: pick only the minimized fields a record needs to expose | **RJ** | 8, 58 | Feeds directly into T60. |
| 60 | Implement the pseudonymization step in the sync client — real names never included (NFR-S-04) | **RJ** | 58, 59 | Security-critical — this is the boundary the whole dashboard's privacy story depends on. |
| 61 | Local test: log an actual sync payload and assert no real names/embeddings/audio/images are present | **MC** | 60 | Clear pass/fail once T60 is merged — run it, don't design it. |
| 62 | Thin sync backend's auth endpoint | **RJ** | 57 | |
| 63 | Storage endpoint (accepts pseudonymized/minimized metadata only) | **RJ** | 60, 62 | |
| 64 | Read API endpoint for the dashboard | **RJ** | 63 | Cross-owner contract — post the response JSON shape once defined. |
| 65 | Auth check scoping a caregiver's reads to their own patient's data only (security.md §2.7) | **RJ** | 62, 64 | Security-critical — the easiest corner to cut on a "thin" backend; don't skip a real review here. |
| 66 | Timeout/retry/error handling on the sync client's network calls | **MC** | 60, 63 | Follows the same pattern already established in T38 — mechanical extension, not a new design. |
| 67 | Caregiver Dashboard design tokens per `design.md` §1–8 (distinct scope from patient-app tokens in T4) | **RG** | 57 | Editorial system, not the accessibility override. |
| 68 | Caregiver Dashboard: activity log view | **RG** | 64, 67 | |
| 69 | Caregiver Dashboard: recognized-face summary view | **RG** | 64, 67 | |
| 70 | Caregiver Dashboard: mood trend view | **RG** | 64, 67 | |
| 71 | Configurable anomaly alert, e.g. repeated unrecognized face (FR-CA-02) | **RJ** | 64, 65 | Backend-side logic/config. |
| 72 | Dashboard offline behavior: show last-synced state instead of failing (NFR-R-02) | **RG** | 68–70 | |
| 73 | Verify Phase 5 exit criteria: caregiver sees trends with no real names ever having left the device; sync handles offline gracefully | **KA** | 57–72 | Sign-off checkpoint before Phase 6 starts. |

---

## Phase 6 — Hardening & Launch Readiness
*(phases.md Phase 6, PRD §9, security.md §3)*

| # | Task | Owner | Depends on | Details |
|---|------|-------|-----------|---------|
| 74 | Lighting/angle robustness test pass on face recognition (NFR-P-02) | **MC** | 22, 30 | Run the test matrix, record results — clear pass/fail. |
| 75 | Noisy-environment test pass on speech recognition (NFR-P-03) | **MC** | 31 | Run the test matrix, record results. |
| 76 | Load-test Room/SQLite as face/memory counts grow (NFR-R-01) | **RJ** | 9, 11 | Needs judgment on realistic data volumes and what "performant" means here. |
| 77 | Security review pass: confirm no biometric data in logs, crash reports, or LLM payloads (NFR-S-01) | **RJ** | 1–76 | Full-conversation review — highest-context owner should run this. |
| 78 | Usability pass: confirm four-action home screen + voice-first output still hold | **RG** | 4, 5, 36 | |
| 79 | Instrument the face-match-accuracy metric | **MC** | 22 | Wire existing match results into a metrics event — mechanical. |
| 80 | Instrument the latency metric | **MC** | 30, 40 | Reuses T30/T40 measurements as an ongoing metric. |
| 81 | Instrument the caregiver-onboarding-completion metric | **MC** | 68 | |
| 82 | Instrument the dashboard-engagement metric | **MC** | 68 | |
| 83 | Verify Phase 6 exit criteria: PRD §9 metrics instrumented and reporting, no open NFR gaps | **KA** | 74–82 | Final sign-off before launch readiness is declared. |

---

> [!IMPORTANT]
> **Out of scope for this list** (per PRD §12 and architecture.md §9 — do not schedule work here until a future roadmap pass): smart-glasses/wearable hardware, multi-language support (Hindi + English), a full cloud-native backend beyond the Phase 5 sync service, healthcare API integrations, AR overlays.
>
> **Two open decisions sit outside the numbered tasks** — resolve before they block anything: the sync backend/dashboard owner (T57) and the "Team Garuda" vs. "Team Chromium" naming split (PRD header note) before any external-facing material ships.

---

## Per-Person Quick Reference

### KA — Kumar Ashutosh owns: 15, 41, 51, 56, 57, 73, 83
*(process decisions and phase exit-criteria sign-off — the checkpoints that need full-team context rather than a single deliverable)*

### MC — Maahi Choudhary owns: 1, 2, 5, 6, 9, 12, 16, 21, 25–27, 30, 32, 40, 42, 48, 49, 53–55, 61, 66, 74, 75, 79–82
*(setup, scaffolding, constants, UI stubs against an already-defined pattern, measurement and instrumentation passes — every task here has a single deliverable and a clear pass/fail)*

### RG — Rhythm Grover owns: 3, 4, 13, 17, 23, 24, 31, 36, 37, 43, 44, 67–70, 72, 78
*(design system, screen UI, accessibility, SpeechRecognizer/TextToSpeech, dashboard UI)*

### RJ — Ritvik Jain owns: 7, 8, 10, 11, 14, 18–20, 22, 28, 29, 33–35, 38, 39, 45–47, 50, 52, 58–60, 62–65, 71, 76, 77
*(encrypted storage, face-matching logic, Intelligence Layer/LLM integration, sync backend, every security-critical wiring point)*

---

## Summary

| Phase | Tasks | Primary Owner |
|-------|-------|---------------|
| 0 — Foundations | 1–15 | Mixed |
| 1 — Face Recognition Core | 16–30 | Mixed (RJ-heavy on the matching logic) |
| 2 — Voice Assistant | 31–41 | Mixed |
| 3 — Memory Diary | 42–51 | Mixed |
| 4 — Emotion-Aware Memory | 52–56 | MC + RJ |
| 5 — Sync Backend + Dashboard | 57–73 | RJ (backend) + RG (dashboard UI) |
| 6 — Hardening & Launch | 74–83 | Mixed |

**Total: 83 atomic tasks across 7 phases, split across 4 owners with a feature-branch + PR workflow.**
