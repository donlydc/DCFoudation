# Observability

[Back to architecture index](../00-index.md)

## Contract

Logging, analytics, crash reporting, and performance tracing stay behind separate DC-owned contracts (never a vendor SDK imported straight into feature code — plan §H), so a vendor can be added/swapped later without touching every call site.

## Current implementation

Logging, analytics and crash reporting have DC-owned contracts with real call sites. The current analytics/crash adapters intentionally forward to logging rather than a vendor SDK (KD-05), so vendor selection remains a downstream composition decision. Dedicated performance tracing is not implemented yet.

## Related documents

- [`logging.md`](logging.md), [`analytics.md`](analytics.md), [`crash-and-performance.md`](crash-and-performance.md)
