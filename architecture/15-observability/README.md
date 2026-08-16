# Observability

[Back to architecture index](../00-index.md)

## Contract

Logging, analytics, crash reporting, and performance tracing stay behind separate DC-owned contracts (never a vendor SDK imported straight into feature code — plan §H), so a vendor can be added/swapped later without touching every call site.

## Current implementation

Only **logging** is implemented (`logging.md`). Analytics, crash reporting, and performance tracing are **not implemented** — no vendor has been chosen, and inventing a contract with zero real backend would be pure speculation (plan §7).

## Related documents

- [`logging.md`](logging.md), [`analytics.md`](analytics.md), [`crash-and-performance.md`](crash-and-performance.md)
