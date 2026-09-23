# 001 - Sparse-query expansion

Type: feat
Branch: feat/query-expansion

## Goal

Give visitors older crime and 311 context when recent queries are sparse, without changing API requests or expanding historical queries.

## Rules

- Evaluate crime and 311 separately. If a query ending today or yesterday (America/Chicago) returns fewer than 10 reports, add older reports from the same bounds, up to 100 total per type.
- Look back at most six months from the requested end date. Include the full requested end day; keep older results strictly before the requested start. Queries ending earlier remain strict.
- Return the newest older reports first, breaking equal timestamps by ID. A custom range ending today or yesterday qualifies just like a preset; the API request does not identify which was used.
- Keep controller cache keys based on the existing request parameters; entries expire after 60 minutes.

## Implementation / commit groups

1. **API fallback** — Query the requested range first, then bounded older context only when eligible. Keep GeoJSON `type` and `features`; label each feature `dateMatch: within_range | older_context`. Do not add collection-level counts, dates, or fallback flags.
2. **Web display** — Fade older markers and all-older clusters; keep mixed clusters at full color. Exclude older reports from heatmaps, label them in the feature list, and provide a static “Hide older reports” checkbox.
3. **MapInfo and fetch behavior** — Keep crime and 311 record totals. Show “Reports unreleased for date range. Including most recent … reports from MM/DD/YYYY.” only when older reports are visible; leave the message blank otherwise. Show the checkbox from 768px, the message from 960px, and counts from 1536px without increasing the bar height. Deduplicate identical API queries while allowing bounds changes during loading to fetch afterward.

## Acceptance

- [x] Recent queries with 0–9 matches retain those matches and add at most `100 - matches` older reports per type, within the same bounds and six-month window.
- [x] Queries with 10+ matches or an earlier end date do not expand; requested and older date ranges do not overlap.
- [x] API requests are unchanged; the GeoJSON collection retains `type` and `features`, with per-feature `dateMatch` as the only added fallback field.
- [x] Older reports fade, can be hidden, do not enter heatmaps, and do not fade a mixed cluster; record totals remain visible at the full-width breakpoint.
- [x] MapInfo shows the date/type message only for visible older reports, with no empty-results message or increase in navbar height.
- [x] Focused API tests and web type-check pass; identical requests are deduplicated and map movement can trigger a new bounds query.
