# ShopZen — Google Play Store Listing Specification

## 1. Store Listing Metadata

| Field | Limit | ShopZen Value | Char Count | Compliance |
|---|---|---|---|---|
| **App Name** | 30 chars | `ShopZen: Grocery List & Budget` | 30 chars | PASS |
| **Short Description** | 80 chars | `Calm, offline-first grocery list and budget planner. Shop fast and track spend.` | 78 chars | PASS |
| **Category** | — | Shopping / Productivity | — | PASS |
| **Tags / Keywords** | — | grocery list, budget planner, shopping checklist, offline shopping list, market mode | — | PASS |
| **Content Rating** | — | Everyone (PEGI 3 / ESRB Everyone) | — | PASS |

---

## 2. Full Description (Play Store Text)

```text
ShopZen is a calm, fast, and completely offline-first grocery list and budget planner designed for effortless one-handed shopping in busy markets.

Replace paper notes and clunky apps with a thoughtful, private shopping companion that helps you organize items, calculate exact costs, and stay within your budget without distractions.

WHY SHOPPERS LOVE SHOPZEN:

🛒 EFFORTLESS LIST ORGANIZING
• Create clean, organized shopping lists in seconds.
• Group items by intuitive categories: Produce, Pantry, Dairy, Bakery, Meat, and Household Essentials.
• Flexible quantities and custom units (kg, lb, box, bunch, pcs, etc.).
• Duplicate previous lists with a single tap — reset checkmarks and actual prices automatically for your next grocery run.

⚡ MARKET MODE FOR ONE-HANDED SHOPPING
• Designed specifically for in-store use: large, tactile checkboxes suitable for one-handed operation while pushing a cart.
• Keep your focus on your groceries with clear in-cart vs. pending counters.
• Subtle haptic feedback confirms every item checked off.
• Quick undo for accidental taps — never lose track of what's in your basket.

💰 REAL-TIME BUDGET & SPEND TRACKING
• Set a target budget for any shopping trip and monitor estimated vs. actual spending in real time.
• Adjust actual prices in the aisle as you discover discounts or price fluctuations.
• Transparent disclosures: items purchased without confirmed prices are clearly flagged so your budget is never misleading.
• Calm, non-blocking over-budget alerts keep you informed without interrupting your trip.

🌍 MULTI-CURRENCY WITH EXACT ARITHMETIC
• Select any ISO 4217 currency per list (USD, EUR, GBP, JPY, CAD, AUD, INR, BHD, and more).
• Zero binary floating-point errors: monetary totals are calculated with exact integer minor-unit arithmetic and half-up rounding.
• Proper zero-decimal support for Yen and Won.

🔒 100% PRIVATE & OFFLINE-FIRST
• No account required: start using ShopZen immediately without passwords, emails, or phone numbers.
• Zero cloud tracking: your shopping lists, item notes, and prices stay 100% on your device in secure local storage.
• Fully functional without an internet connection — shop smoothly in basement supermarkets or rural markets with zero signal.

📜 PERMANENT SHOPPING HISTORY
• Archive completed trips into immutable historical records.
• Review past grocery costs, dates, and item breakdowns anytime, even if original lists are edited or deleted.

🎨 THOUGHTFUL BOTANICAL DESIGN
• Beautiful, eye-pleasing Botanical Light and Night palettes designed to reduce eye strain.
• Fully compliant with WCAG 2.1 AA accessibility standards and high-contrast readable typography.
• Supports system-wide dynamic text scaling and dark mode.

🛡️ ZERO-AD POLICY ON SHOPPING SCREENS
• We respect your shopping experience: advertisements NEVER appear in Market Mode, Add/Edit Item, Budget screens, or Checkout.
• Enjoy an optional yearly Remove Ads subscription for an entirely ad-free experience across all surfaces.

Download ShopZen today and bring calm, organized confidence to every grocery run!
```

*Character count: ~2,840 characters (well within the 4,000-character Google Play limit).*

---

## 3. Graphic Asset Requirements

### 3.1 App Icon
- **Dimensions:** 512 × 512 px
- **Format:** 32-bit PNG with alpha
- **Max file size:** 1024 KB
- **Design:** Botanical green background (`#16845B`) with ShopZen minimalist woven leaf/shopping basket emblem in warm sage (`#B8E986`) and white.

### 3.2 Feature Graphic
- **Dimensions:** 1024 × 500 px
- **Format:** 24-bit PNG or JPEG (no alpha)
- **Max file size:** 15 MB
- **Composition:** High-resolution botanical background displaying the ShopZen wordmark, subtle grocery silhouettes, and tagline: *"Calm, Offline-First Grocery List & Budget Planner"*.

### 3.3 Phone Screenshots (Minimum 4 required, 1080 × 2400 px, 9:20 aspect ratio)
1. **Screenshot 1 — Home Overview:** Clean dashboard showing multiple shopping lists, active budget health bars, and progress rings.
2. **Screenshot 2 — Market Mode:** Highlighting large 48dp checkoff targets, real-time in-cart count, and quick actual price adjustment.
3. **Screenshot 3 — Budget & Financial Metrics:** Clear estimated vs. actual spend, over-budget non-blocking banner, and category spend distribution.
4. **Screenshot 4 — Shopping History:** Historical list details preserving immutable snapshot totals and archived dates.
5. **Screenshot 5 — Botanical Night Palette:** Rich dark mode surface (`#121A17` / `#1C2922`) demonstrating eye-friendly contrast.

---

## 4. Release Notes (Version 1.0.0, versionCode 1)

**Play Console Text (en-US, max 500 characters):**
```text
Welcome to ShopZen v1.0.0 — your calm, offline-first grocery list and budget planner!

What's New:
• Organized shopping lists with customizable categories
• Market Mode: large touch targets for easy one-handed shopping
• Live budget tracking with over-budget alerts & price adjustments
• Multi-currency support (USD, EUR, GBP, JPY) with exact math
• 100% offline-first: your data stays on your device
• Calming Botanical Light & Dark themes
```

---

## 5. Google Play Store Declarations Reference
- **Privacy Policy URL:** `https://barakwebsoft.com/shopzen/privacy-policy`
- **Data Safety Form:** Fully documented in [`docs/SECURITY.md`](file:///c:/Users/rakib/AndroidStudioProjects/ShopZen/docs/SECURITY.md) Section 11.
- **Ads Declaration:** "Yes, my app contains ads" (Google Mobile Ads banner, strictly excluded from critical shopping screens per ADR-009).
- **Target Audience:** All ages (13+ / General audience; no children targeted).
