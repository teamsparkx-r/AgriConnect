# AgriConnect — Software Requirements Specification
## Module: Buyer Role

**Document version:** 1.0
**Prepared for:** Development Team
**Status:** Draft for review
**Companion document:** `AgriConnect_Farmer_Role_SRS.md` (shares entities: Product, Booking, Report)

---

## 1. Purpose

This document specifies the functional and non-functional requirements for the **Buyer role** of the AgriConnect platform, covering merchants, wholesalers, retailers, businesses, and individual consumers. It is written to the same standard as the Farmer Role SRS so both can be handed to the development team together.

AgriConnect is a **0% mediator, completely FREE marketplace**. Buyers can browse, search, book products, and unlock farmer contact details instantly at no cost. Everything after contact unlock — negotiation, inspection, logistics, final payment — happens off-platform.

---

## 2. Scope

This document covers Buyer registration, browsing/search/filter, product details, booking and payment, contact unlock, booking management, notifications, profile, and reporting. Farmer-side and Admin-side behavior are covered in their respective documents and referenced here only at the points where they intersect (e.g., what the farmer sees when a booking is created).

---

## 3. Definitions and Glossary

Same glossary as the Farmer Role SRS §3. Key terms repeated here for convenience:

| Term | Meaning |
|---|---|
| Booking | A record created when a buyer requests a product; contact details unlock instantly at no cost |
| Contact Unlock | The moment a specific buyer gains free, instant visibility into a specific farmer's protected fields, and vice versa |
| Booking Stage | A phase in the buyer journey from registration through completion, with no fees at any stage |

---

## 4. Actors

| Actor | Description |
|---|---|
| Buyer | Primary actor for this document |
| Farmer | Secondary actor — receives booking notifications, has protected fields unlocked |
| Admin | Secondary actor — reviews buyer-submitted reports |
| System | Background actor — payment verification, OTP, notifications, listing status checks |

---

## 5. Buyer Role Overview

**High-level flow (FREE at every stage):**

`Register/Login → Browse/Search/Filter → View Product Details (farmer info hidden) → Book Product (FREE) → Farmer Details Unlocked Instantly → Contact Farmer → Visit & Verify → Negotiate Directly → Complete Off-Platform`

**Buyer Journey Stages:**
- **Stage 1: Account Setup** — Registration, OTP verification, profile creation
- **Stage 2: Discovery** — Browse, search, filter products by category/location/price
- **Stage 3: Product Evaluation** — View product details (farmer info hidden until booking)
- **Stage 4: Booking & Unlock** — Submit booking request (FREE) → Contact details unlock instantly
- **Stage 5: Direct Engagement** — Contact farmer, arrange visit, discuss terms
- **Stage 6: Off-Platform Transaction** — Visit farm, verify, negotiate, transact directly
- **Stage 7: Completion** — Mark booking completed, optional report submission

---

## 6. Data Model

The Buyer role reuses `Product`, `Booking`, and `Report` entities defined in the Farmer Role SRS (§6.2–6.4). Only the Buyer entity is new here.

### 6.1 Buyer

| Field | Type | Required | Notes |
|---|---|---|---|
| buyer_id | UUID/PK | Yes | System-generated |
| full_name | string | Yes | Max 100 chars |
| mobile_number | string(10-15) | Yes | Unique per role — see Open Question 24.2 in Farmer SRS |
| mobile_verified | boolean | Yes | Default false until OTP success |
| email | string | No | Validated format if provided |
| password_hash | string | Yes | Never store plaintext |
| buyer_type | enum | No | MERCHANT, WHOLESALER, RETAILER, INDIVIDUAL, OTHER |
| profile_photo_url | string | No | |
| state | string | Yes | |
| district | string | Yes | |
| preferred_language | enum | Yes | |
| account_status | enum | Yes | ACTIVE, SUSPENDED, DELETED |
| created_at / updated_at | datetime | Yes | |

**Note:** unlike the Farmer entity, Buyer has no `village`/exact-location field, since buyer location isn't shown to farmers pre-booking in the current spec. If farmers should see approximate buyer location (e.g., to judge delivery distance), add it explicitly — see Open Question 24.6.

### 6.2 Booking — Buyer-relevant fields (full schema in Farmer SRS §6.3)

The Buyer flow is the primary writer of this entity. No payment fields needed since platform is FREE.

| Field | Type | Required | Notes |
|---|---|---|---|
| booking_stage | enum | Yes | Stage 4 (Booking & Unlock) represents instant contact unlock, Stage 5-7 for engagement/completion |

---

## 7. Buyer Registration Workflow

### 7.1 Required Fields

| Field | Required | Validation |
|---|---|---|
| Full Name | Yes | 2–100 chars |
| Mobile Number | Yes | 10-digit Indian mobile format, unique per BUYER role |
| OTP Verification | Yes | 6-digit, expires in 5 minutes, max 3 resends |
| Email | No | Standard email regex if provided |
| Password | Yes | Min 8 chars, at least 1 letter + 1 number |
| Confirm Password | Yes | Must match |
| Buyer Type | No | Enum list; default OTHER if omitted |
| State / District | Yes | From predefined location dataset |
| Preferred Language | Yes | From supported language list |

### 7.2 Functional Requirements

- The system shall prevent duplicate Buyer accounts on the same verified mobile number.
- The system shall send and validate OTP with the same rules as the Farmer flow (5-minute expiry, lockout after 5 failed attempts for 15 minutes).
- The system shall hash passwords; never store or log plaintext.
- The system shall assign role `BUYER` only after successful OTP verification.

### 7.3 Flow

`Open App → Select "Register as Buyer" → Enter Details → Verify OTP → Account Created → Buyer Home`

---

## 8. Buyer Login Workflow

Identical mechanics to Farmer login (§8 of Farmer SRS): mobile + password or mobile + OTP, account lockout after 5 failed attempts, OTP-based password recovery, JWT/session token with expiry and refresh.

---

## 9. Buyer Home Screen Workflow

### 9.1 Components

Search bar, product categories, featured products, recently added, available products, location filter, notifications icon, profile access.

### 9.2 Navigation

Home, Search, My Bookings, Notifications, Profile.

### 9.3 Functional Requirement

- The system shall only display products with `status = ACTIVE` on the home screen and in all browse/search/filter views. Products in `DRAFT`, `SOLD`, `EXPIRED`, or `REMOVED` status must never appear to buyers, including via direct link or cached results.

---

## 10. Product Categories Workflow

Categories: Vegetables, Fruits, Grains, Pulses, Spices, Oil Seeds, Commercial Crops, Other.

`Buyer Home → Select Category → Filtered Active Listings`

---

## 11. Product Search Workflow

### 11.1 Functional Requirements

- The system shall support partial-match search on `product_name` (e.g., "tom" matches "Tomato").
- The system shall return only `ACTIVE` listings in search results.
- The system shall handle basic spelling variance — recommend a fuzzy-match/trigram search rather than exact string match, since farmers and buyers may spell regional produce names differently (e.g., "Chilli" vs "Chili" vs "Mirchi").
- The system shall show a clear empty-state message when no results are found, with a suggestion to broaden filters.

### 11.2 Non-Functional Note

Search should be backed by an indexed field (or a dedicated search service like Postgres full-text search or Elasticsearch) once listing volume grows — a naive `LIKE '%term%'` query will not scale well past a few thousand listings.

---

## 12. Product Filter and Sorting Workflow

### 12.1 Filters

Category, State, District, Available Quantity (min), Unit, Harvest Date range, Price Range (only applies to listings where `expected_price` is set — listings without a price should still appear, not be excluded, when a price filter is applied, unless the buyer explicitly opts to hide priceless listings).

### 12.2 Sorting

Recently Added, Oldest First, Price Low→High, Price High→Low, Quantity High→Low, Nearest Harvest Date.

**Functional requirement:** the system shall clearly indicate, in price-based sort modes, which listings have no price set (they should sort to the end, not be treated as zero).

---

## 13. Product Listing Card

### 13.1 Visible Pre-Booking

Image, name, category, quantity, unit, district, state, harvest date, expected price (if set), availability status.

### 13.2 Hidden Pre-Booking (enforced server-side per Farmer SRS §9.1)

Farmer name, phone number, alternate number, exact village/address, GPS coordinates.

**Cross-reference:** this must be enforced identically to FR-F-007 in the Farmer SRS. The Buyer-facing API and the Farmer-facing API should call the same serializer/permission layer, not two independently maintained field lists, or they will drift out of sync.

---

## 14. Product Details Workflow

### 14.1 Displayed

Multiple images, name, category, quantity, unit, harvest date, description, expected price (if set), district, state, status.

### 14.2 Hidden Farmer Info

Same as §13.2. Show: *"Farmer contact details are protected. Book this product to unlock the farmer's contact information."*

### 14.3 Functional Requirement

- The system shall disable or hide the **Book Product** action if the listing's status is not `ACTIVE`, and shall show why (e.g., "This product is currently unavailable").

---

## 15. Booking Initiation Workflow

### 15.1 Booking Confirmation Screen

Product name, image, quantity, district/state-level location, terms notice (0% mediator disclosure — reuse the exact wording from the source spec, §10 of the original document). **No payment required.**

### 15.2 Flow

`Product Details → Book Product → Review Booking Info → Accept Terms → Confirm Booking (FREE, Instant)`

### 15.3 Functional Requirement

- The system shall require an explicit terms acknowledgment (checkbox or equivalent) before confirming the booking — this is the buyer's clear notice that AgriConnect is not a party to the trade, which matters for dispute handling later.
- The system shall instantly unlock and display farmer contact details upon booking confirmation (no payment processing, no delays).

---

## 16. Booking Confirmation Workflow (FREE, Instant)

### 16.1 Confirmation Screen

Product name, buyer acceptance of terms, instant confirmation button. **No payment required, no gateway integration needed.**

### 16.2 Booking Statuses (Simplified)

| Status | Meaning |
|---|---|
| INITIATED | Buyer submitted booking request |
| CONFIRMED | Booking confirmed, farmer contacted, contact details unlocked |
| COMPLETED | Buyer marked as completed |
| CANCELLED | Cancelled (if supported) |

### 16.3 Critical Business Rule

**Contact details unlock INSTANTLY upon booking confirmation.** There is no payment processing, no gateway delays, no server verification steps beyond basic data validation. The booking confirmation is immediate and synchronous.

### 16.4 Functional Requirements

- The system shall create a `CONFIRMED` booking record immediately upon terms acknowledgment and confirmation button tap.
- The system shall unlock farmer contact details instantly upon booking confirmation (no delay).
- The system shall notify the farmer immediately of the new booking.
- The system shall display the farmer's permitted fields to the buyer immediately upon confirmation.

---

## 17. Instant Contact Unlock Workflow

On booking confirmation (INSTANT, no payment needed):

1. Unique `booking_id` generated.
2. `booking_status = CONFIRMED`, `contact_unlocked_at` timestamp set immediately.
3. Farmer notified in real-time (per Farmer SRS §14).
4. Buyer receives instant confirmation with farmer's permitted fields: name, verified mobile, village, farm address (if farmer opted to share it), map location (if available).
5. Buyer can immediately Call, Copy Number, Open Maps, View Booking Details.

**Functional requirement:** the fields unlocked to the buyer must match exactly what the farmer consented to share at the field level — if `farm_address` sharing is optional for the farmer (recommended in Farmer SRS §6.1 as a privacy-conscious addition), the buyer-facing unlock must respect that per-field consent, not unlock everything by default.

---

## 18. Direct Farmer Contact Workflow

Buyer actions: Call, Message (if integrated), arrange visit, discuss quantity/condition/price/logistics.

Platform boundary (matches Farmer SRS §16): AgriConnect does not negotiate, guarantee availability/quality, verify final price, arrange logistics, process final payment, or act as broker.

---

## 19. Farm Visit and Product Verification Workflow

`Contact Farmer → Schedule Visit → Visit → Inspect → Verify Quantity/Quality → Negotiate → Decide`

Entirely off-platform; AgriConnect has no visibility into or responsibility for this stage beyond what the buyer voluntarily reports via **Mark Booking Completed** or a **Report** (§23).

---

## 20. My Bookings Workflow

### 20.1 Categories

Confirmed, Completed, Cancelled (if supported), Expired (if applicable).

### 20.2 Card Contents

Booking ID, product image, product name, booking date, booking amount, status, farmer-contact availability indicator.

### 20.3 Actions

View Details, View Farmer Details, Call Farmer, Open Location, Report Farmer/Listing.

---

## 21. Booking Status Workflow (Simple, No Payment Stages)

| Status | Meaning |
|---|---|
| INITIATED | Booking request submitted |
| CONFIRMED | Booking confirmed, contact unlocked (immediate) |
| COMPLETED | Buyer/farmer indicates transaction is done |
| CANCELLED | Cancelled per policy |

**Happy path:** `INITIATED → CONFIRMED → COMPLETED`

**Design note:** All bookings that reach `CONFIRMED` represent successful, persisted transactions. Since there's no payment processing, no failed bookings exist — a buyer either completes the booking or doesn't submit it.

---

## 22. Product Availability Changes

- If a product becomes unavailable (SOLD/REMOVED/EXPIRED) before booking is confirmed, the system shall block the booking and show: *"This product is currently unavailable and can no longer be booked."*
- If the booking already succeeded (moved to `CONFIRMED`) before the status changed, the existing booking record and unlocked contact details remain accessible to the buyer regardless of subsequent listing changes.

**Edge case:** Since booking is instant with no payment processing, all availability checks happen at booking initiation. If a farmer marks a product SOLD after a booking is confirmed, the existing booking record remains valid — the buyer already has access to the contact details.

---

## 23. Report and Safety Workflow

### 23.1 Reasons

False product info, misleading images, farmer unreachable, suspicious behavior, fraud attempt, abusive behavior, listed-as-available-but-isn't, other.

### 23.2 Flow

`Product/Booking Details → Report → Select Reason → Optional Description → Submit → Admin Review`

### 23.3 Functional Requirement

- The system shall rate-limit buyer reports (e.g., max 10/day) matching the equivalent Farmer-side control (Farmer SRS §19), to prevent the reporting system from being used to harass farmers.

---

## 24. Buyer Notifications Workflow

Trigger events: booking successful, payment successful/failed, farmer details unlocked, booked product status changed, product marked sold/unavailable, account security events.

Delivery: in-app + SMS/push for payment and unlock events (time-sensitive); in-app only for informational status changes.

---

## 25. Buyer Profile Management

`Profile → My Profile` — view/edit name, photo, email, location, language, password, booking history, logout. Primary mobile number change requires fresh OTP (same rule as Farmer SRS §9.2).

---

## 26. Complete Buyer Journey (End-to-End, All Stages)

**Stage 1 - Account Setup:**
`Register as Buyer → Enter Details → Verify OTP → Complete Profile → Buyer Home`

**Stage 2 - Discovery:**
`Browse/Search/Filter Products → View Categories & Listings`

**Stage 3 - Product Evaluation:**
`Click Product → View Details (farmer info hidden) → Review Description/Quantity/Price`

**Stage 4 - Booking & Unlock (FREE):**
`Book Product → Review Terms → Accept Terms & Confirm (FREE, Instant) → Booking Confirmed`

**Stage 5 - Direct Engagement:**
`Farmer Notified → Contact Details Instantly Displayed → Call/Message Farmer → Arrange Visit`

**Stage 6 - Off-Platform Transaction:**
`Visit Farm → Inspect Product → Negotiate Price & Terms → Agree on Final Details → Transact Directly`

**Stage 7 - Completion:**
`Mark Booking Completed → Optional: Submit Report if Issues → Transaction Closed`

---

## 27. Functional Requirements Summary

| ID | Requirement |
|---|---|
| FR-B-001 | The system shall allow a user to register as a Buyer (Stage 1). |
| FR-B-002 | The system shall verify the buyer's mobile number via OTP (Stage 1). |
| FR-B-003 | The system shall support login via mobile+password or mobile+OTP (Stage 1). |
| FR-B-004 | The system shall show buyers only `ACTIVE` product listings in all browse/search/filter contexts (Stage 2). |
| FR-B-005 | The system shall support partial/fuzzy product-name search (Stage 2). |
| FR-B-006 | The system shall support filtering by category, location, quantity, harvest date, and price range where available (Stage 2). |
| FR-B-007 | The system shall display product details while excluding protected farmer fields, server-side, before booking (Stage 3). |
| FR-B-008 | The system shall require explicit terms acknowledgment before booking confirmation (Stage 4). |
| FR-B-009 | The system shall instantly unlock farmer contact details upon booking confirmation, with no delays or payment processing (Stage 4). |
| FR-B-010 | The system shall unlock only the farmer fields the farmer has consented to share, matched at the field level (Stage 4). |
| FR-B-011 | The system shall generate a unique Booking ID upon booking confirmation (Stage 4). |
| FR-B-012 | The system shall notify the farmer immediately on booking confirmation (Stage 4). |
| FR-B-013 | The system shall let the buyer view full booking history and status (All Stages). |
| FR-B-014 | The system shall block booking attempts on non-`ACTIVE` listings at booking-initiation time (Stage 4). |
| FR-B-015 | The system shall not require any payment processing, timeouts, or payment verification (All Stages - Platform is FREE). |
| FR-B-016 | The system shall not require payment webhook processing (All Stages - Platform is FREE). |
| FR-B-017 | The system shall allow the buyer to report a farmer/listing tied to a specific booking, with rate limiting (Stage 7). |
| FR-B-018 | The system shall allow full buyer profile management, with OTP re-verification for mobile number changes (All Stages). |
| FR-B-019 | The system shall not participate in negotiation, logistics, inspection, or final payment for goods (Stages 5-6). |
| FR-B-020 | The system shall maintain a full, timestamped booking-status history per buyer, with no payment records (All Stages). |

---

## 28. Non-Functional Requirements

| Category | Requirement |
|---|---|
| Security | HTTPS everywhere; passwords hashed; no payment data stored; no external gateway integration required |
| Privacy | Farmer field-level consent respected on unlock (§17); buyer's own data likewise excluded from farmer view beyond permitted booking fields |
| Performance | Search/browse results within 1–2 seconds under normal load; instant booking confirmation with immediate contact unlock |
| Reliability | Booking records must persist reliably; no lost bookings; no race conditions on product availability checks |
| Scalability | Listing search should use indexed/full-text search, not naive substring queries, once catalog size grows (§11.2) |
| Auditability | All booking status transitions logged with timestamp, actor, product ID, and farmer ID for full traceability |
| Accessibility | Same as Farmer SRS §22 — large touch targets, dropdowns over free text, screen-reader-friendly forms |

---

## 29. Error Handling & Edge Cases

1. **Buyer double-taps "Book" button:** must not create two bookings for the same intent — use an idempotency key per booking request or disable the button immediately after first tap.
2. **Product goes unavailable mid-booking (race condition):** check product availability again immediately before confirming the booking; if unavailable, reject and show: *"This product is no longer available."*
3. **Buyer cancels after contact is unlocked:** contact info, once unlocked, cannot be un-shown to the farmer or buyer — cancellation (if supported) should only affect booking status/records, not retroactively hide already-exchanged information.
4. **Buyer creates many bookings rapidly to harvest farmer numbers:** rate-limit bookings per buyer per day — this is a real abuse vector even though the platform is free. Limit: e.g., max 20 bookings/day per buyer.
5. **Buyer never completes transaction, accumulates contact info:** consider auto-expiring booking visibility after N days of inactivity (e.g., 30 days) with notification that contact access will expire, to prevent indefinite contact harvesting.
6. **Farmer marks product SOLD after booking confirmed:** existing booking record remains valid — buyer keeps contact info. This is intentional; contact, once exchanged, is permanent.

---

## 30. Open Questions for Product Owner

These extend the Farmer SRS's open-question list (§24) with Buyer-specific items:

| # | Question |
|---|---|
| 30.1 | Should buyers be able to see an approximate farmer rating/completion history (e.g., "12 completed bookings") before booking, to build trust without breaking the 0% mediator model? |
| 30.2 | What is the daily booking limit per buyer to prevent contact-harvesting (e.g., 20/day, 50/day)? |
| 30.3 | Should "Mark Booking Completed" be buyer-only, farmer-only, or require both sides to confirm — and does it affect anything functionally, or is it purely informational/analytics? |
| 30.4 | Does the buyer ever see the farmer's exact GPS location, or only district/village-level detail even post-booking? |
| 30.5 | Should booking visibility expire after N days of inactivity (e.g., farmer contact auto-hidden after 30 days if no completion) to prevent indefinite contact access? |

---

## 31. Traceability Note

Each FR-B-### ID in §27 should map directly to test cases and issue-tracker items, and should be cross-checked against the Farmer SRS's FR-F-### list wherever both roles touch the same entity (Booking, Product) to avoid contradictory implementations.
