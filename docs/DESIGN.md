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
- 48 dp minimum touch targets.
- Accessible labels and semantics for icon-only actions.
- Adequate contrast and scalable text.
- Do not rely on color alone to communicate purchased/error/warning state.
- Support small screens without clipped controls.
- Respect system dark mode and user-selected theme.
- Provide clear focus and keyboard behavior where applicable.

## 10. Design implementation rule
This document defines the intended design system. Update it when a product-approved design change is made. Do not claim a screen is implemented because it appears in this planned screen list.
