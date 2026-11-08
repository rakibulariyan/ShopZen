---
name: MarketMate
colors:
  surface: '#f0fcf4'
  surface-dim: '#d0ddd5'
  surface-bright: '#f0fcf4'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eaf7ee'
  surface-container: '#e4f1e9'
  surface-container-high: '#deebe3'
  surface-container-highest: '#d9e6dd'
  on-surface: '#131e19'
  on-surface-variant: '#3e4942'
  inverse-surface: '#28332d'
  inverse-on-surface: '#e7f4eb'
  outline: '#6e7a72'
  outline-variant: '#bdcac0'
  surface-tint: '#006c48'
  primary: '#006946'
  on-primary: '#ffffff'
  primary-container: '#16845b'
  on-primary-container: '#f2fff4'
  inverse-primary: '#78d9aa'
  secondary: '#406915'
  on-secondary: '#ffffff'
  secondary-container: '#bdef8b'
  on-secondary-container: '#446d19'
  tertiary: '#954040'
  on-tertiary: '#ffffff'
  tertiary-container: '#b45857'
  on-tertiary-container: '#fffaf9'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#94f6c4'
  primary-fixed-dim: '#78d9aa'
  on-primary-fixed: '#002113'
  on-primary-fixed-variant: '#005236'
  secondary-fixed: '#c0f18d'
  secondary-fixed-dim: '#a5d574'
  on-secondary-fixed: '#0e2000'
  on-secondary-fixed-variant: '#2b5000'
  tertiary-fixed: '#ffdad8'
  tertiary-fixed-dim: '#ffb3b0'
  on-tertiary-fixed: '#400107'
  on-tertiary-fixed-variant: '#7b2c2d'
  background: '#f0fcf4'
  on-background: '#131e19'
  surface-variant: '#d9e6dd'
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

This design system defines an offline-first grocery and household procurement companion built on Material 3 and Jetpack Compose paradigms. The system rejects the disposable, utilitarian aesthetic of basic to-do checklists in favor of an ergonomic, reliable, and premium productivity tool tailored for high-frequency daily routines.

### Core Character & Aesthetic Posture
- **Grounded Vitality:** Rooted in deep botanical greens paired with crisp, high-contrast produce tones. It feels fresh, hygienic, and precise.
- **Physical Reliability:** Emphasizes tactile certainty through clear hit-states, distinct elevation changes on surface sheets, and zero visual stutter during rapid entry.
- **Modern Minimal Structure:** Edge-to-edge Android canvas handling with transparent system bars, clean typographic hierarchy, and structural surface cards that organize high-density data without visual noise.

## Colors

The palette leverages a deep forest tone alongside an energizing botanical chartreuse accent to deliver high legibility under varying ambient lighting conditions (such as bright grocery aisles or low-light kitchen pantries).

### Token Mappings & Roles
- **Primary (`#16845B`):** Brand anchor; drives primary Floating Action Buttons (FABs), key interactive toggles, active bottom navigation states, and confirm actions.
- **Primary Pressed/Dark (`#105C43`):** State layer for pressed buttons, selected container borders, and prominent interactive focus states.
- **Secondary / Container Accent (`#B8E986`):** Highlight surface, badge fills, budget surplus chips, and filled progress track accents. Provides vivid contrast against deep primary tones.
- **Neutral / On-Surface (`#17221D`):** Primary data readouts, monetary totals, item names, and high-emphasis headers.
- **On-Surface Variant (`#68766E`):** Secondary metadata, unit weights, item counts, completed state copy, and inactive navigation items.
- **Surface Canvas (`#F7F9F7`):** Base window canvas for the light theme, providing a cool, organic foundation distinct from pure white.
- **Surface / Card Background (`#FFFFFF`):** High-level structured cards, modal sheets, and dialogue containers.
- **Outline & Separators (`#E4EAE5`):** Structural dividing lines, unselected chip outlines, and card stroke borders.
- **Semantic Feedback:**
  - Success: `#16845B` (inventory acquired, budget target met).
  - Warning: `#E5A323` (expiring items, near budget threshold).
  - Error / Alert: `#D74646` (over budget, sync failure, destructive actions).

### Dark Surface Mappings
When switching to dark mode, the canvas maps to `#121A17` with elevated containers shifting to `#1C2922`. Outlines shift down to 12% white alpha overlay to prevent harsh contrast lines.

## Typography

The typographic system utilizes **Inter** across all interface layers to ensure uniform cross-device consistency and strict geometric balance.

### Numeral Tabular Lining Requirement
Financial totals, item quantities, and list weights must be rendered with OpenType tabular figures (`fontFeatureSettings = "tnum"` in Compose). This maintains strict vertical decimal and currency alignment across lists and budgeting ledgers:
- ₹ INR, $, €, and £ currency glyphs match the height and weight of adjacent numerical values.
- `numeral-lg` and `numeral-md` tokens are dedicated to aggregate cost headers, list subtotals, and quantity stepper counts.

### Text Hierarchy & Tone
- **Display & Screen Headings (`display-lg`, `headline-md`):** Reserved for screen-level titles, active shopping mode overviews, and primary store dashboard heads. Tight tracking prevents wrap breaks.
- **Section Headings (`title-md`):** Category dividers (e.g., "Produce", "Dairy & Refrigerated") within list views.
- **Body (`body-lg`, `body-md`):** Unchecked checklist item names use `body-lg` at regular 400 weight. Checked or strikethrough items transition down to `body-md` with `On-Surface Variant` color application.
- **Supporting Meta (`label-md`):** Brand notes, package size details (e.g., "500g", "2 pk"), store aisle numbers, and timestamp sync metadata.

## Layout & Spacing

The layout is built for single-handed mobile efficiency following Material 3 guidelines and Jetpack Compose scaffold practices.

### Edge-to-Edge Architecture
- The system renders completely edge-to-edge behind the Android status bar and gesture navigation bar.
- `WindowInsets.systemBars` governs root content clearance. Top app bars absorb dynamic status bar heights; bottom navigation bars and floating sheets clear the system home indicator with `WindowInsets.navigationBars.asPaddingValues()`.

### Grid & Density Rules
- **Mobile Grid:** Single fluid column with fixed outer boundaries (`margin`: `1rem` / 16dp).
- **Component Gutters:** `1rem` (16dp) standard between vertical list cards and operational sections.
- **Internal Density:** 
  - Standard list item padding: `12dp` vertical, `16dp` horizontal.
  - Quick-entry fields and inline controls leverage `space-sm` (8dp) separation to preserve thumb dexterity.
  - Key operational anchors (such as floating actions) sit 16dp inside screen edges, resting 16dp clear above the navigation bar.

## Elevation & Depth

Visual hierarchy is maintained primarily through structured tonal layering and low-contrast surface outlines, rather than heavy, dirty drop shadows.

### Surface Elevation Hierarchy
- **Level 0 (Canvas):** Base background color (`#F7F9F7`). Houses uncontained group dividers, background status indicators, and screen headers.
- **Level 1 (Default Containers & List Cards):** Background `#FFFFFF` accompanied by a precise 1dp stroke using `Outline` (`#E4EAE5`). In dark mode, rendered as `#1C2922` without cast shadows.
- **Level 2 (Active/Floating Contexts & Extended FAB):** Tinted primary shadow using 8% opacity of `#105C43`, 8dp blur radius, 3dp vertical offset. Delivers tactile lift during drag-and-drop item reordering.
- **Level 3 (Modal Bottom Sheets):** Base surface `#FFFFFF` elevated with a 16dp ambient blur (`#17221D` at 12% alpha) combined with standard scrim (`#17221D` at 40% alpha) covering canvas layers beneath.

### Low-Contrast Border Philosophy
Every card and chip utilizes a structural 1dp border (`#E4EAE5`) under light theme conditions. This eliminates reliance on multi-layer shadows, maintaining razor-sharp rendering on lower-tier OLED and LCD panels.

## Shapes

The geometric vocabulary prioritizes organic ergonomics while maintaining high informational density. Corner radiuses clearly delineate temporary sheets, operational cards, and active micro-controls.

### Corner Radii Scale
- **Cards & Primary Groupings:** `16dp` (`rounded-lg`). Softens category enclosures and budget dashboard summaries.
- **Buttons & Text Entry Containers:** `12dp` (`roundedness` base). Balances interactive definition with thumb-friendly touch surfaces.
- **Filter & Category Chips:** Fully pill-shaped (`24dp` - `rounded-xl`). Separates micro-filtering toggles from square list items.
- **Modal Bottom Sheets:** `28dp` top-start and top-end corner radii. Establishes clear tactile drag semantics for quick-add item panels and detail sheets.
- **Floating Action Button:** `16dp` continuous squircle corners per Material 3 guidelines.

## Components

### Buttons
- **Touch Target:** Mandatory minimum of `48dp x 48dp` interactive hit area regardless of icon or visual label dimensions.
- **Primary Action Button:** Height `48dp`, corner radius `12dp`. Container fill is Primary (`#16845B`), text/icon is `#FFFFFF` (`label-md` bold). Pressed state shifts container to Primary Dark (`#105C43`).
- **Tonal / Secondary Button:** Height `48dp`, corner radius `12dp`. Container fill is Accent Container (`#B8E986`), content color is `#105C43`.
- **Outlined / Tertiary Button:** Height `48dp`, corner radius `12dp`. Transparent fill with a 1dp stroke (`#E4EAE5`), content color is Primary (`#16845B`).

### Floating Action Button (FAB)
- **Primary Quick-Add FAB:** Material 3 Extended FAB or large square-round button (`56dp x 56dp`), `16dp` corner radius. Fill: Primary (`#16845B`), icon: `#FFFFFF`. Sits anchored at bottom-end, 16dp above the `NavigationBar`.

### Material 3 Cards
- **List Item Card:** Outer radius `16dp`, background `#FFFFFF`, outline `1dp` solid `#E4EAE5`. 
- **Active State / In-Cart:** When an item is marked as checked, card background transitions to Canvas (`#F7F9F7`), text applies strikethrough styling with `On-Surface Variant` color, and border softens to 0.5dp thickness.

### Chips
- **Category Filter Chips:** Height `32dp`, pill-shaped (`24dp` radius). Unselected state: surface `#FFFFFF`, outline `1dp` (`#E4EAE5`), text `On-Surface Variant` (`label-md`). Selected state: container Primary (`#16845B`), text `#FFFFFF`, zero border.
- **Budget / Status Chips:** Compact read-only badges, height `24dp`, container Accent (`#B8E986`) at 20% alpha, text Primary Dark (`#105C43`) rendered in tabular figures.

### Checkboxes & Selection Controls
- **Touch Bounds:** 48dp square hit target centering a `20dp x 20dp` rounded checkbox (`4dp` radius).
- **Color Mapping:** Unchecked border: 2dp `#68766E`. Checked container: Primary (`#16845B`) with `#FFFFFF` checkmark vector. Check actions must execute immediate optimistic UI updates locally with zero frame latency.

### Input Fields (Compose OutlinedTextField)
- **Container Specs:** Height `56dp`, corner radius `12dp`. Unfocused border: 1dp `#E4EAE5`. Focused border: 2dp Primary (`#16845B`). 
- **Typography:** Placeholder text rendered in `On-Surface Variant` (`body-lg`); typed input rendered in `On-Surface` (`body-lg`). Supports trailing clear (`X`) and voice/barcode search icons with minimum 48dp touch bounds.

### Progress Bars
- **Budget & List Completion Track:** Height `8dp`, fully pill-radiused (`4dp`). Track background: `#E4EAE5`. Indicator fill: Accent (`#B8E986`) transitioning to Primary (`#16845B`) upon full completion. Error state (`#D74646`) activates if expenditure exceeds entered budget ceiling. Accompanied by `numeral-md` tabular progress indicators (e.g., "12/18 items").

### Modal Bottom Sheet
- **Container Structure:** Top corners curved at `28dp`, flat bottom. Surface fill: `#FFFFFF`. Houses a centered grab handle (`32dp x 4dp`, corner radius `2dp`, fill `#68766E` at 40% alpha) positioned 12dp from the top edge. Automatically binds input focus to quick-entry fields upon expansion.

### NavigationBar (Material 3)
- **Container Layout:** Base height `80dp` plus window navigation bar insets. Fill: `#FFFFFF` with top border 1dp (`#E4EAE5`).
- **Pill Indicator:** Active destination is highlighted by an elongated pill container (`64dp x 32dp`) filled with Accent (`#B8E986`), containing a Primary (`#16845B`) icon. Inactive destinations render `On-Surface Variant` icons with `label-md` descriptors.