# ShopZen — Product Requirements Document

## 1. Product overview
ShopZen is a native Android shopping list and grocery budget planner. It replaces handwritten lists with reusable digital lists, organized items, in-market purchase tracking and spending summaries.

## 2. Problem
People often prepare shopping lists informally and struggle to:
- Keep items and quantities organized.
- Track what has already been purchased while shopping.
- Estimate and monitor the shopping budget.
- Recall previous shopping trips.

## 3. Target users
- Individuals and households who regularly shop for groceries or household essentials.
- Users who want a lightweight shopping organizer without creating an account.
- Users who need basic budget visibility while shopping.

## 4. Product goals
1. Create and manage shopping lists quickly.
2. Organize items by category, quantity and unit.
3. Make purchase tracking easy in a crowded market.
4. Show estimated and actual spend transparently.
5. Preserve completed shopping history.
6. Work reliably without an internet connection.

## 5. MVP scope

### Shopping lists
- Create, view, edit, delete and archive lists.
- Duplicate a list using new list/item IDs.
- Each list has a name, optional description, currency and optional budget.
- List status: active, completed or archived as appropriate to the implemented lifecycle.
- Show item count, purchase progress, budget/spend and last-updated information.

### Shopping items
- Add, edit and delete items.
- Item fields: name, quantity, unit, category, estimated unit price, actual unit price, purchase state, optional notes and sort order.
- Support predefined categories: vegetables, fruits, dairy, bakery, groceries, cleaning, personal care, meat/fish and others.
- Validate names, quantities and prices.
- Support custom units where appropriate.

### Market Mode
- Clear, large purchase controls suitable for one-handed use.
- Display purchased and pending counts.
- Allow actual price entry.
- Provide undo for accidental purchase toggles.
- Work offline.
- Haptic feedback where supported and appropriate.
- No advertisements in Market Mode.

### Budget
- Track actual spend for purchased items with known actual prices.
- Clearly indicate purchased items without actual prices; do not silently treat unknown spend as zero.
- Show estimated amounts separately from actual amounts.
- Provide a non-blocking over-budget warning.
- No advertisements on Budget screens or purchase screens.

### History
- Archive/completion creates an immutable snapshot of the shopping trip.
- Historical details remain available even if the original active list is later edited or deleted.
- Snapshot includes list name, currency, budget, completion time and item-level details needed to reconstruct the trip.

### Preferences
- Light, dark and system theme.
- Currency selection per list.
- Default currency may be derived from device locale, but users can change it.
- Multiple ISO 4217 currencies are supported; no conversion between currencies in MVP.

### Monetization
- AdMob advertising and a yearly Remove Ads subscription are planned.
- Do not hardcode subscription price; use the localized price returned by Google Play.
- Ads must never appear in Market Mode, Add/Edit Item, Budget screen or purchase screen.
- Consent and ad initialization must follow current UMP/Google Mobile Ads guidance.
- Billing entitlement must be based on verified Play purchase state, not a locally stored boolean alone.
- Monetization implementation is gated by current official SDK guidance and privacy review.

## 6. Out of scope for MVP
- Mandatory authentication or account creation.
- Backend, cloud backup or real-time family synchronization.
- Currency conversion.
- Grocery delivery or marketplace.
- Retailer integrations and live price comparison.
- AI assistant.
- Barcode scanning and pantry management.
- Voice entry, widgets and advanced sharing unless separately approved.

## 7. V1 candidates
- Built-in and custom shopping templates.
- Widgets.
- Voice item entry.
- Sharing and export improvements.

## 8. V2 candidates
- Family sharing and real-time sync.
- Cloud backup.
- Barcode scanning.
- Pantry and low-stock management.

## 9. Core user journeys
1. Open app → create list → select currency/budget → add items → save.
2. Open list → enter Market Mode → mark items purchased → enter actual prices → review spend.
3. Complete/archive list → save history snapshot → open historical details later.
4. Duplicate list → edit copied items → shop again.

## 10. Success criteria
- User can create and reopen a list with no network connection.
- User can add, edit, delete and categorize items.
- Purchase state survives app restart.
- Market Mode supports purchase toggling and undo.
- Budget calculations are exact under defined currency precision and rounding rules.
- Unpriced purchased items are visibly disclosed.
- Archiving preserves an immutable historical snapshot.
- No ads appear in protected screens.
- Critical flows have automated tests.

## 11. Open product details
The implementation agent must not invent unspecified behavior. Raise a focused clarification when a requirement materially affects data integrity, UX or release behavior. Record approved answers in this document and `docs/DECISIONS.md`.
