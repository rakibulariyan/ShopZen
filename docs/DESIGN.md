# ShopZen — UI/UX Design System

## 1. Product experience
ShopZen should feel calm, fast and practical. The primary use case is checking items while shopping, often with one hand in a crowded market. Avoid visual clutter and unnecessary steps.

## 2. Visual direction
- Style: modern, minimal, friendly and utility-focused.
- Framework: Material 3.
- Layout baseline: Android phone screens around 393 dp wide; adapt to smaller and larger devices.
- Spacing: 8 dp rhythm, with approximately 16 dp screen margins.
- Minimum interactive touch target: 48 dp.
- Cards: approximately 16 dp corner radius.
- Buttons: approximately 12 dp corner radius.
- Maintain clear contrast and readable text.

## 3. Color tokens
| Token | Value | Use |
|---|---|---|
| Primary | `#16845B` | Main actions and selected states |
| Primary dark | `#105C43` | Darker emphasis |
| Accent | `#B8E986` | Secondary highlights |
| Light background | `#F7F9F7` | Main light surface |
| Dark background | `#121A17` | Main dark surface |
| Dark surface | `#1C2922` | Elevated dark surface |
| Main text | `#17221D` | Primary text |
| Secondary text | `#68766E` | Supporting text |
| Success | `#16845B` | Successful states |
| Warning | `#E5A323` | Budget warnings |
| Error | `#D74646` | Destructive/error states |

Use semantic Material 3 color schemes and verify contrast in both themes. Do not hardcode these values throughout individual screens.

## 4. Typography
- Prefer Inter or a visually equivalent readable sans-serif.
- Use Material 3 typography roles consistently.
- Use tabular numerals for monetary values where supported.
- Keep item names and prices easy to scan.
- Avoid excessive font weights and decorative type.

## 5. Navigation
Planned bottom navigation:
- Home
- History
- Templates (templates are a V1 candidate unless product scope is explicitly moved into MVP)
- Settings

Do not display a Templates destination that leads to a dead screen. If templates are deferred, omit the destination until implemented or provide an explicitly approved useful state.

## 6. Planned screens
- Onboarding / feature introduction
- Home and list overview
- Search lists
- Create/edit shopping list
- List details
- Add/edit shopping item
- Category selector
- Market Mode
- Actual price entry
- Shopping completion
- Budget summary and over-budget state
- Shopping history and historical details
- Settings
- Currency selection
- Appearance
- Remove Ads / Premium
- Optional account screen only if a meaningful approved account/backup feature exists

## 7. Key interaction requirements
- Home cards show list name, item count/progress, budget/spend and last-updated information where available.
- Market Mode uses large checkboxes/actions and clear purchased/pending counts.
- Provide undo after purchase toggles.
- Budget warnings are non-blocking.
- Clearly identify purchased items with no actual price.
- Confirm destructive delete/archive actions.
- Use clear empty, loading, error, success and validation states.
- Avoid fake controls and unexplained icons.

## 8. Advertising restrictions
Never show ads on:
- Market Mode
- Add/Edit Item
- Budget screen
- Purchase screen

Ad placements must not obscure controls, disrupt shopping or cause accidental taps. Consent and SDK behavior must follow current policies.

## 9. Accessibility and responsive behavior
- **Touch target minimum:** 48 dp interactive touch targets enforced across all buttons, checkboxes, icons, and list items (`AccessibilityAndThemeReviewTest`).
- **Semantic content descriptions:** All icon-only actions (navigation back, item creation, search, clear, category selection, more options) provide explicit content descriptions for screen readers (TalkBack).
- **Color contrast (WCAG 2.1 AA):**
  - Light theme: Main text `#17221D` on Light Background `#F7F9F7` exceeds 12.0:1 (exceeds 4.5:1 requirement).
  - Light theme: Main text on Light Surface `#FFFFFF` exceeds 12.5:1. Secondary text `#68766E` exceeds 4.0:1.
  - Dark theme: Main text `#F1F5F2` on Dark Background `#121A17` exceeds 14.0:1.
  - Dark theme: Main text on Dark Surface `#1C2922` exceeds 12.0:1. Secondary text `#A1B0A7` exceeds 7.0:1.
- **Dynamic type scaling:** All Material 3 typography tokens use scalable `sp` units for `fontSize` and `lineHeight` to support system-wide font scaling without overlapping.
- **Color independence:** Purchase state is communicated via both strikethrough typography and explicit checkmark iconography; budget warnings include explicit textual overspend amounts, not just color indicators.
- **Small-screen responsiveness:** Compact displays down to 320 dp width are supported; list surfaces use `LazyColumn` with stable item IDs, and forms/dialogs use `verticalScroll(rememberScrollState())` to prevent content clipping or keyboard obstruction.
- **Theme support:** System default, forced Light, and forced Dark modes are supported via DataStore preferences.

## 10. Brand Identity, App Icon & Splash Screen

### 10.1 Brand Identity
- **App Name:** ShopZen
- **Tagline:** Plan Smart. Shop Easy.
- **Product:** Shopping List & Grocery Budget Planner
- **Personality:** Modern, Minimal, Friendly, Trustworthy, Premium, Fresh & Organized.
- **Palette Alignment:**
  - Primary Green: `#16845B`
  - Dark Green: `#105C43`
  - Accent Green: `#B8E986`
  - Light Splash Background: `#F7F9F7`
  - Dark Splash Background: `#121A17`
  - Dark Surface: `#1C2922`

### 10.2 App Icon Architecture
The ShopZen launcher icon combines a clean geometric shopping basket silhouette with an integrated checkmark badge, communicating effortless shopping organization and task completion.
- **Adaptive Icon Specification:**
  - Standard Android 108 × 108 dp viewport with an inner 72 dp safe zone (masked by launcher circles, squircles, or rounded rects without clipping).
  - Background (`ic_launcher_background.xml`): Linear diagonal gradient flowing from Primary Green `#16845B` to Dark Green `#105C43`.
  - Foreground (`ic_launcher_foreground.xml`): Crisp white geometric basket wireframe with handle and rim, paired with a vibrant Accent Green `#B8E986` checkmark at center right.
  - Monochrome (`ic_launcher_monochrome.xml`): Pure flat silhouette with alpha transparency for Android 13+ (API 33+) Material You dynamic themed icons.
- **Legacy Raster Mipmaps:**
  - Densities generated: `mdpi` (48px), `hdpi` (72px), `xhdpi` (96px), `xxhdpi` (144px), `xxxhdpi` (192px).
  - Square (`ic_launcher.png`) and circular (`ic_launcher_round.png`) legacy fallbacks for Android 7.1 and older launchers.
- **Store Listing Icon:**
  - 512 × 512 px 32-bit PNG with rounded corner squircle mask, preserved at `store/icon/shopzen_play_store_icon_512.png` and `docs/assets/icon/shopzen_play_store_icon_512.png`.

### 10.3 Android 12+ Official Splash Screen Experience
Built using `androidx.core:core-splashscreen` to deliver a zero-flicker native launch experience without synthetic loading delays or redundant Activities:
- **Light Theme:** Background `#F7F9F7` with centered botanical badge emblem (`ic_splash_icon.xml`, 288 × 288 dp viewport, 160 dp primary emblem circle containing basket and checkmark).
- **Dark Theme:** Background `#121A17` (`values-night/themes.xml`) ensuring seamless contrast and parity with dark mode preferences.
- **Theme Transition:** Theme inherits from `Theme.SplashScreen` with `postSplashScreenTheme = @style/Theme.ShopZen`, automatically resetting window decor and status bar styling upon app content readiness.

## 11. Design implementation rule
This document defines the intended design system. Update it when a product-approved design change is made. Do not claim a screen is implemented because it appears in this planned screen list.
