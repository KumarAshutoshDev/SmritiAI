---
name: Intellectual Minimalist
colors:
  surface: '#fbf9f1'
  surface-dim: '#dcdad2'
  surface-bright: '#fbf9f1'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f6f4ec'
  surface-container: '#f0eee6'
  surface-container-high: '#eae8e0'
  surface-container-highest: '#e4e2db'
  on-surface: '#1b1c17'
  on-surface-variant: '#464742'
  inverse-surface: '#30312c'
  inverse-on-surface: '#f3f1e9'
  outline: '#767872'
  outline-variant: '#c7c7c0'
  surface-tint: '#5f5e5d'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#1c1c1a'
  on-primary-container: '#858382'
  inverse-primary: '#c9c6c4'
  secondary: '#99462a'
  on-secondary: '#ffffff'
  secondary-container: '#fe9572'
  on-secondary-container: '#762c12'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#1c1c17'
  on-tertiary-container: '#85847d'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e5e2e0'
  primary-fixed-dim: '#c9c6c4'
  on-primary-fixed: '#1c1c1a'
  on-primary-fixed-variant: '#474745'
  secondary-fixed: '#ffdbd0'
  secondary-fixed-dim: '#ffb59e'
  on-secondary-fixed: '#390b00'
  on-secondary-fixed-variant: '#7a2f15'
  tertiary-fixed: '#e5e2da'
  tertiary-fixed-dim: '#c8c6bf'
  on-tertiary-fixed: '#1c1c17'
  on-tertiary-fixed-variant: '#474741'
  background: '#fbf9f1'
  on-background: '#1b1c17'
  surface-variant: '#e4e2db'
  surface-alt: '#E6E1D1'
  accent-sage: '#7A8271'
  ink-deep: '#141413'
  paper-base: '#F0EEE6'
typography:
  display-lg:
    fontFamily: Source Serif 4
    fontSize: 72px
    fontWeight: '600'
    lineHeight: 80px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Source Serif 4
    fontSize: 48px
    fontWeight: '600'
    lineHeight: 56px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Source Serif 4
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-md:
    fontFamily: Source Serif 4
    fontSize: 32px
    fontWeight: '500'
    lineHeight: 40px
  body-lg:
    fontFamily: Hanken Grotesk
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Hanken Grotesk
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-caps:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.05em
  label-md:
    fontFamily: Hanken Grotesk
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  unit: 4px
  container-max: 1280px
  gutter: 32px
  margin-desktop: 64px
  margin-tablet: 32px
  margin-mobile: 20px
  stack-lg: 80px
  stack-md: 48px
  stack-sm: 24px
---

## 0. Scope — read this before using anything below

**Source of truth:** `SmritiAI_PRD_v3.1.md` + `architecture.md` (same hierarchy as `rules.md`). Where this file conflicts with those, they win — this section documents that resolution rather than silently overriding it.

**Conflict identified:** the "Intellectual Minimalist" system below (small 12–16px type, low-contrast 30–50%-opacity outlines as the primary depth cue, thin-underline-only button affordances, dense asymmetrical editorial layouts) is an academic/luxury *editorial* aesthetic. It directly conflicts with three requirements that govern the **patient-facing Android app**:

- **FR-APP-02** — large text, high contrast, minimal-step UI for cognitively impaired users
- **NFR-U-01** — home screen limited to a small set of large, clearly labeled actions
- **NFR-U-02** — assistant interactions default to voice output, text is secondary

Those NFRs apply to the patient-facing screens specifically (Home, Recognize Person, Ask Smriti AI, Add Memory, Memory History) — the PRD doesn't impose them on the Caregiver Dashboard or on pitch/marketing collateral, which are read by caregivers and judges, not by the cognitively-impaired primary user.

**Resolution — this file now has two scopes, don't mix them:**

| Applies to | System to use |
|---|---|
| Marketing site, pitch deck, README/investor collateral, **Caregiver Dashboard** (Phase 5, web/companion view) | Everything in §1–8 below, unchanged |
| **Patient-facing Android app** (Home, Recognize, Ask Smriti AI, Add Memory, Memory History) | §9 "Patient App Accessibility Override" — this replaces, not supplements, the type scale, contrast rules, and button styling in §1–8 for those screens |

If a task touches both (e.g. a shared component library), build the patient-app version to §9 and don't assume the editorial defaults are "close enough" — per `rules.md` §7, that's the kind of silent scope-blur this file exists to prevent.

---

## 1. Brand & Style
The design system is built on a "Research-meets-Luxury" philosophy. It balances the rigor of an academic institution with the tactile elegance of a high-end editorial publication. The aesthetic is rooted in **Modern Minimalism** with a focus on intellectual clarity, calm interactions, and premium execution.

The visual narrative prioritizes "quiet confidence"—using generous whitespace to allow ideas to breathe and high-contrast typography to establish authority. The style avoids unnecessary decoration, relying instead on precise alignment, sophisticated color theory, and subtle motion to convey quality.

**Core Principles:**
- **Intentional Whitespace:** Negative space is a functional element, used to group information and reduce cognitive load.
- **Editorial Authority:** Large, expressive serif headlines command attention and imply a history of thoughtful curation.
- **Utilitarian Precision:** UI controls and metadata use clean, functional sans-serif and mono fonts to represent technical accuracy.
- **Tactile Warmth:** Moving away from cold "tech blue," the system uses organic tones to feel human-centric and approachable.

## 2. Colors
The palette is centered on "Paper and Ink." The background uses a warm, parchment-like cream (`#F0EEE6`) rather than a sterile white to evoke a sense of heritage and comfort. 

- **Primary (Ink):** Deep Charcoal is used for maximum legibility and to anchor the design.
- **Secondary (Terracotta):** A muted organic orange used sparingly for primary actions, subtle highlights, or to signal human interaction.
- **Tertiary (Dust):** A low-chroma taupe used for secondary text, borders, and UI ornaments.
- **Named Accents:** `surface-alt` provides a subtle shift for card backgrounds or sectioning, while `accent-sage` can be used for success states or alternative research-themed highlights.

## 3. Typography
The typographic system uses a tripartite hierarchy to distinguish between narrative, utility, and data.

1.  **Narrative (Source Serif 4):** Used for headlines and storytelling. It provides a scholarly, editorial feel. Use "Optical Sizes" where available to maintain elegance at large scales.
2.  **Interface (Hanken Grotesk):** A modern, high-legibility sans-serif for body copy and general UI elements. Its geometric roots feel precise yet accessible.
3.  **Data (JetBrains Mono):** Used for technical metadata, labels, and small captions. The monospaced nature suggests the "raw research" or technical backend of the product.

**Usage Note:** Maintain high contrast between Serif and Sans levels. Never use the Serif for interactive UI elements like buttons or input labels.

## 4. Layout & Spacing
The layout follows a **Fixed Grid** philosophy for desktop to maintain the editorial "column" feel, transitioning to a fluid model for mobile devices.

- **Grid:** A 12-column grid for desktop with wide 32px gutters to emphasize the "Luxury" of space. 
- **Rhythm:** Vertical spacing is aggressive. Use `stack-lg` (80px) between major sections to ensure the user isn't overwhelmed. 
- **Alignment:** Lean heavily into asymmetrical layouts. For example, a headline might span 6 columns on the left while the right 6 columns remain empty or contain a small "Data" label in Mono.
- **Mobile:** On mobile, margins tighten to 20px, and the grid collapses to a single column, but vertical "breathing room" should be preserved.

## 5. Elevation & Depth
In this design system, depth is achieved through **Tonal Layers** and **Low-Contrast Outlines** rather than heavy shadows.

- **Surfaces:** Use `paper-base` (#F0EEE6) for the main background. Use `surface-alt` (#E6E1D1) to define "Container" areas like cards or sidebars.
- **Outlines:** Instead of shadows, use 1px solid borders in `tertiary_color` (#87867F) at 30-50% opacity. This creates a "technical drawing" or blueprint feel.
- **Interaction Depth:** For hover states, a very subtle, highly diffused ambient shadow (8% opacity, 20px blur, 0px offset) can be used to suggest the element is lifting off the "paper."
- **Glassmorphism:** Use sparingly for navigation bars only. A light blur (12px) with a semi-transparent `paper-base` background keeps the content below visible but secondary.

## 6. Shapes
Shapes are "Soft" (`roundedness: 1`). This provides a gentle, human touch to an otherwise rigorous and structured layout.

- **Standard Elements:** Buttons and input fields use a 0.25rem (4px) radius. This is enough to feel "designed" without appearing "bubbly" or overly casual.
- **Featured Cards:** Use `rounded-lg` (8px) for larger containers to create a distinct soft-rectangle silhouette.
- **Buttons:** Keep corners soft but distinct. Avoid fully rounded "pill" buttons as they conflict with the editorial, structured nature of the typography.

## 7. Components
Consistent component styling reinforces the "Research-meets-Luxury" vibe:

- **Buttons:**
    - *Primary:* Solid `ink-deep` with `paper-base` text. No shadow. Minimalist and heavy.
    - *Secondary:* Transparent background with a 1px `ink-deep` border.
    - *Text:* All-caps `label-caps` typography with a thin underline that grows on hover.
- **Inputs:**
    - Minimalist bottom-border only or a very light `surface-alt` fill. Use `label-caps` for the field label above the input.
- **Cards:**
    - No shadows. Use a subtle `surface-alt` background or a fine 1px border. Focus on internal padding (32px+) to maintain the whitespace theme.
- **Chips/Tags:**
    - Use `JetBrains Mono` at a small size. Use `secondary_color` (Terracotta) at 10% opacity for the background with solid terracotta text for "Active" states.
- **Lists:**
    - Use wide vertical spacing between items. Use a thin horizontal rule (0.5px) between list items to mimic a table of contents.
- **Navigation:**
    - A clean, persistent top bar. Links in `label-caps`. The active state is indicated by a small terracotta dot below the text rather than a bold font weight.

---

## 9. Patient App Accessibility Override

**Applies only to:** Home, Recognize Person, Ask Smriti AI, Add Memory, Memory History (the four FR-APP-01 actions) and any dialog/flow reachable from them (Add Person, Unknown Person flow, consent screens). Does **not** apply to the Caregiver Dashboard.

This overrides §2–7 wherever they conflict; it does not layer on top of them.

### 9.1 Type scale (replaces §3)
- Body text minimum **20px / line-height 30px** (Hanken Grotesk) — not the 16–18px `body-md`/`body-lg` tokens above.
- Primary action labels minimum **24px**, bold weight.
- **`label-caps` (12px JetBrains Mono) must never be used as the only label for an interactive element on these screens** — it's below a legible size for the target users. Use it only as supplementary metadata (e.g. a timestamp next to a memory entry that already has a legible headline).
- Match confidence display ("Matched: Laura (93%)", FR-FR-02) renders at body-lg minimum, not as a small caption.

### 9.2 Contrast (replaces §5's "Low-Contrast Outlines" approach)
- Text-on-background contrast: minimum **7:1** (WCAG AAA), not the system default. `on-surface` (#1b1c17) on `paper-base` already clears this — keep it, but don't substitute `on-surface-variant` or lighter tones for primary content on these screens.
- Borders/dividers that carry meaning (e.g. separating list items in Memory History) use full-opacity `outline` (#767872) or darker — **not** the 30–50%-opacity blueprint-style outlines from §5. Those are fine on the dashboard; they're not fine here.
- No glassmorphism/blur on these screens — it reduces legibility for low-vision users and adds no functional value on a phone screen already tight for space.

### 9.3 Buttons & touch targets (replaces §7's button rules)
- **Primary and secondary actions are both solid, filled, high-contrast buttons.** The thin-underline-only "Text" button style from §7 is not used anywhere in the patient app — it's too low-affordance for users with cognitive impairment to reliably recognize as tappable.
- Minimum touch target **56dp height**, minimum **16dp** spacing between adjacent tappable elements, per Android accessibility guidance — well above the 4px-radius, dense-spacing defaults in §4/§6.
- The four home-screen actions (Ask Smriti AI, Recognize Person, Add Memory, Memory History — FR-APP-01) render as large, equally-sized tiles with icon + label, not as a text-link list. No fifth action, ever, without a product decision (`rules.md` §5).

### 9.4 Layout (replaces §4's asymmetrical/dense grid)
- No asymmetrical "headline spans 6 columns, rest empty" layouts on these screens — every screen is single-column, one primary action or piece of information in view at a time, consistent with FR-APP-02's "minimal steps."
- `stack-lg`/`stack-md` spacing values are fine to reuse for breathing room, but content density (items per screen) stays low regardless of spacing — don't fill freed-up whitespace with more simultaneous choices.

### 9.5 Voice-first (new — no §-equivalent above, since the editorial system has no voice component)
- Every assistant response (Recognize Person match, Ask Smriti AI answer) is spoken via TTS **by default**, with the on-screen text as the secondary/confirming channel (NFR-U-02) — never build a screen where reading is required to get the answer.
- Visual indication that speech is happening (e.g. a simple waveform or speaking-state icon) should use the same high-contrast palette as the rest of this section, not a subtle/diffused motion treatment.

### 9.6 What carries over unchanged from §1–7
- Color palette (ink/terracotta/paper tokens) — contrast rules in 9.2 constrain *which* tokens pair together, not the palette itself.
- Serif-for-narrative / sans-for-interface split, as long as sans (Hanken Grotesk) sizes follow 9.1 — the serif headline treatment on, e.g., a "Welcome" or empty-state screen is fine at the larger sizes already defined in §3.
- Soft corner radii (§6) — roundedness isn't an accessibility concern here.
