# 002 - Sparse-query fallback performance and display

Type: fix
Branch: master

## Goal

- Keep repeated fallback GETs fast without changing SQL or worker settings.
- Show up to 1,000 older reports, with map context controls only when older reports are returned.

## Context

- Repeated fallback queries can switch to a slow generic PostgreSQL plan; DTO and controller work were negligible.
- On merged NYC dev data, repeated crime GETs had a 1.62 s median with default pgJDBC versus 44 ms with `prepareThreshold=0`; 311 was about 38 ms in both cases.
- The API-only setting affects its other database queries too. Production has not been changed.

## Implementation / commit group

1. **One fix commit** — Set the API deployment URL to `$(POSTGRESQL_URL)?prepareThreshold=0`; raise `SparseQueryPolicy.FALLBACK_LIMIT` to 1,000; change `MapInfo` copy to “Showing most recent” and show its older-report control only when the API response contains older data. Update the focused tests and query-index note. Leave SQL, worker configuration, and benchmark scaffolding out of the change.

## Acceptance

- [x] Local repeated fallback GETs avoid the generic-plan slowdown; other tested GETs return identical responses with no clear regression.
- [x] API deployment alone has `prepareThreshold=0`; temporary timing code is removed.
- [x] Sparse-query tests and web build pass with the 1,000-report cap and conditional control.
- [ ] After deployment, confirm API startup and monitor populated fallback GET latency and database load. Roll back the API manifest change if either regresses.
