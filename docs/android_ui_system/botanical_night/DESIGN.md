---
name: Botanical Night
colors:
  surface: '#1C2922'
  surface-dim: '#0d1512'
  surface-bright: '#333b37'
  surface-container-lowest: '#08100d'
  surface-container-low: '#151d1a'
  surface-container: '#22332B'
  surface-container-high: '#283C33'
  surface-container-highest: '#2F463B'
  on-surface: '#dce5df'
  on-surface-variant: '#bdcac0'
  inverse-surface: '#dce5df'
  inverse-on-surface: '#2a322f'
  outline: '#88948b'
  outline-variant: '#3e4942'
  surface-tint: '#78d9aa'
  primary: '#78d9aa'
  on-primary: '#003824'
  primary-container: '#16845b'
  on-primary-container: '#f2fff4'
  inverse-primary: '#006c48'
  secondary: '#a5d574'
  on-secondary: '#1c3700'
  secondary-container: '#2e5500'
  on-secondary-container: '#9aca6b'
  tertiary: '#c1c8c3'
  on-tertiary: '#2b322f'
  tertiary-container: '#6f7672'
  on-tertiary-container: '#f7fef8'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#94f6c4'
  primary-fixed-dim: '#78d9aa'
  on-primary-fixed: '#002113'
  on-primary-fixed-variant: '#005236'
  secondary-fixed: '#c0f18d'
  secondary-fixed-dim: '#a5d574'
  on-secondary-fixed: '#0e2000'
  on-secondary-fixed-variant: '#2b5000'
  tertiary-fixed: '#dde4df'
  tertiary-fixed-dim: '#c1c8c3'
  on-tertiary-fixed: '#161d1a'
  on-tertiary-fixed-variant: '#414845'
  background: '#0d1512'
  on-background: '#dce5df'
  surface-variant: '#2e3733'
  text-primary: '#E1E8E3'
  text-secondary: '#96A79D'
  border-divider: '#2B3D34'
  status-error: '#FF6B6B'
  status-warning: '#F5C042'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md-mobile:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: 0em
  title-md:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: 0em
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.01em
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  numeral-lg:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
    letterSpacing: -0.01em
  numeral-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
    letterSpacing: 0em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system delivers an Android Material 3 dark-first interface tailored for high-frequency grocery logistics, home inventory, and budget management. It translates the organic clarity of fresh botanical environments into an ergonomic, low-strain night mode that excels under variable ambient lighting—from high-glare supermarket aisles to dim home pantries.

### Aesthetic Character
- **Botanical Precision:** Deep pine and mineral foundations paired with vivid chartreuse accents create sharp optical legibility while preventing OLED battery drain and visual fatigue.
- **Material 3 Utility:** Strict adherence to modern Android design patterns, featuring edge-to-edge rendering, tactile surface tonal stepping, and thumb-friendly touch targets.
- **Tactile Confidence:** High-contrast border delineations, responsive state layers, and optimistic micro-interactions reinforce tactile certainty during rapid checklist navigation.

## Colors

The dark-first palette is constructed from deep, desaturated chlorophyll neutrals paired with vibrant, high-contrast produce tones to ensure complete WCAG AAA text legibility and rapid glanceability.

### Key Roles & Assignments
- **Background (`#121A17`):** The foundational canvas layer across all screens, handling edge-to-edge transparent system bars without muddy gray shifts.
- **Surface (`#1C2922`):** Primary card bodies, modal sheets, and standard app bar containers.
- **Surface Container (`#22332B`):** Elevated elements, grouped section backgrounds, active text input containers, and list item wrappers.
- **Primary (`#16845B`):** Anchors primary actions, confirmed checklist checkboxes, and the core Floating Action Button (FAB).
- **Secondary / High-Contrast Accent (`#B8E986`):** Vibrant chartreuse tone applied to badge highlights, navigation pill indicators, progress bar indicators, and critical real-time totals.
- **Text Primary (`#E1E8E3`):** Unchecked checklist items, prominent currency headings, and high-emphasis labels.
- **Text Secondary (`#96A79D`):** Metadata, aisle location pills, checked/strikethrough items, and inactive bottom navigation destinations.
- **Border & Divider (`#2B3D34`):** Crisp 1dp ghost borders that delineate surfaces without casting heavy shadows.

## Typography

The design system employs **Inter** exclusively across display, body, and micro-label hierarchies, bringing uniform geometric clarity and optical alignment to high-density lists.

### Tabular Figures & Numerals
All quantitative figures—including financial balances, item tallies, weights, and category counters—must enable tabular figures via OpenType settings (`fontFeatureSettings = "tnum"` in Android Jetpack Compose or `font-variant-numeric: tabular-nums` in web previews). This guarantees strict vertical column alignment across ledgers and shopping lists.

### Text Hierarchy & Roles
- **Display & Headlines:** Used for screen titles and active shopping summaries. Tighter letter spacing on mobile prevents premature line wrapping.
- **Titles:** Delineate major category blocks (e.g., "Pantry", "Produce", "Chilled").
- **Body:** Standard checklist entry text uses `body-lg` in `#E1E8E3`. When an item is marked as complete, it transitions down to `body-md` in `#96A79D` with a strikethrough.
- **Numerals:** `numeral-lg` drives total spend indicators, while `numeral-md` powers inline quantity steppers and badge metrics.

## Layout & Spacing

Layouts are designed mobile-first, prioritizing single-thumb interaction zones and zero edge clipping.

### Grid & Canvas Structure
- **Canvas Margins:** Fixed `1rem` (16dp) on handset viewports; scales to `1.5rem` (24dp) on tablets and foldables.
- **Section Gutters:** `1rem` (16dp) between stacked container cards and operational toolbars.
- **Edge-to-Edge System Handling:** Views render continuously beneath the Android status bar and gesture navigation bar. Top app bars consume `WindowInsets.statusBars` as top padding, while bottom navigation bars, snackbars, and floating actions pad against `WindowInsets.navigationBars`.

### Tap Target & Spacing Rules
- Every touch target enforces a strict minimum boundary of `48dp x 48dp` regardless of the nested visual glyph size.
- List elements utilize `12dp` vertical padding and `16dp` horizontal padding to maintain finger clearance while maximizing items per viewport.

## Elevation & Depth

Rather than relying on dark muddy shadows that disappear on OLED displays, the design system utilizes Material 3 surface container tiers combined with crisp structural outlines.

### Tonal Hierarchy
- **Level 0 (Canvas):** Base window color `#121A17`. Holds root backgrounds and uncontained list headers.
- **Level 1 (Cards & Static Containers):** Surface `#1C2922` paired with a 1dp stroke of `#2B3D34`.
- **Level 2 (Active Sheets & Inline Controls):** Surface Container `#22332B`. Used for active category blocks, search inputs, and dropdown panels.
- **Level 3 (Modal Sheets & Context Overlays):** Surface Container High `#283C33` with a subtle top boundary stroke of `#2B3D34` and an ambient `#000000` scrim at 60% opacity covering the canvas below.
- **Level 4 (Floating Action Button):** Raised container using Primary `#16845B` with an extra-diffused directional shadow (0dp 4dp 16dp) tinted with 30% alpha of `#121A17` to separate it from underlying cards during scroll.

## Shapes

The shape system blends modern squircle-like Android radii with distinct visual categories to distinguish operational containers from actionable micro-controls.

### Component Radii Mapping
- **Cards & Primary Enclosures:** Explicitly `16dp` (`rounded-lg`), softening list containers and dashboard overview cards.
- **Buttons & Interactive Text Inputs:** Explicitly `12dp`, creating an ergonomic touch geometry for thumb clicks.
- **Chips & Quick-Filter Badges:** Fully pill-shaped (`9999px` / `rounded-full`), clearly separating filter toggles from structural cards.
- **Modal Bottom Sheets:** `28dp` top-start and top-end corner radii, visually signaling pull-down dismissal capability.
- **Floating Action Button:** `16dp` continuous curved squircle conforming to Material 3 FAB specifications.

## Components

### Buttons
- **Primary Button:** Height `48dp`, corner radius `12dp`. Background `#16845B`, label `#E1E8E3` (`label-md` bold). Pressed state shifts background to `#105C43`.
- **Accent / Tonal Button:** Height `48dp`, corner radius `12dp`. Background `#B8E986`, label `#121A17`. Pressed state applies a 12% black overlay.
- **Outlined Button:** Height `48dp`, corner radius `12dp`. Transparent background, 1dp border `#2B3D34`, label `#E1E8E3`.

### Floating Action Button (FAB)
- Extended or regular FAB (`56dp x 56dp`), `16dp` corner radius. Background is `#16845B` with an `#E1E8E3` plus icon. Sits fixed at the bottom-end, floating `16dp` above the navigation bar insets.

### Cards & List Items
- **Standard Card:** Radius `16dp`, background `#1C2922`, border 1dp solid `#2B3D34`.
- **Interactive Checklist Item:** Height `64dp`, outer radius `16dp`, background `#22332B`, border 1dp solid `#2B3D34`. 
- **Completed / Strikethrough State:** Background transitions down to `#1C2922`, border softens to 0.5dp `#2B3D34`, title switches to `#96A79D` with a central strikethrough line.

### Chips & Badges
- **Filter Chips:** Height `32dp`, pill-shaped radius. Unselected: background `#1C2922`, border 1dp `#2B3D34`, text `#96A79D`. Selected: background `#22332B`, border 1.5dp `#B8E986`, text `#B8E986`.
- **Status Badges:** Compact height `24dp`, pill-shaped, background `#22332B`, text `#B8E986` (`label-md` tabular figures).

### Checkboxes
- 48dp touch bounding box surrounding a `20dp x 20dp` box with `4dp` corner radius.
- Unchecked: 2dp outline `#96A79D`, transparent fill.
- Checked: `#16845B` fill with `#E1E8E3` checkmark icon. Transitions use instant local state updates with zero frame delay.

### Input Fields
- Height `56dp`, corner radius `12dp`. Background `#1C2922`. Border 1dp `#2B3D34` when idle, transitioning to 2dp `#16845B` on active focus. Placeholder color `#96A79D`; active input text `#E1E8E3`.

### Progress Indicators
- Linear progress tracks: Height `8dp`, fully pill-radiused. Background track `#2B3D34`. Indicator fill `#B8E986`, transitioning smoothly to `#16845B` upon 100% list or budget fulfillment.

### Bottom Navigation Bar
- Height `80dp` plus device system navigation insets. Background `#1C2922` with a 1dp top divider `#2B3D34`.
- Selected destination features a `64dp x 32dp` pill indicator in `#22332B` with `#B8E986` iconography, while inactive icons render in `#96A79D`.