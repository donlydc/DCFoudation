# Permissions

[Back to architecture index](00-index.md)

## Scope

Runtime permission ownership and rationale/denial handling.

## Current implementation

**Not implemented.** `AndroidManifest.xml` declares zero `<uses-permission>` entries — no feature touches camera/location/contacts/notifications/etc. Nothing to request, so no permission-state contract exists yet. Building one now (a generic `PermissionState` sealed type, a rationale-dialog pattern) would be pure speculation with no call site to validate it against.

## Rules

**PERM-01 — Reserved.** No rule yet.

## Related documents

- [`05-presentation-udf.md`](05-presentation-udf.md) (the pattern a permission flow would slot into, once one exists)
