# `:platform:permission`

**Role:** DC-owned permission state and reusable permission policy.

**Owns:** `PermissionState` and the notification-permission adapter.

## AI guidance

- Keep raw launcher/framework calls at Route or other legitimate framework-glue boundaries.
- Features consume permission state/actions, not `ActivityCompat` or launcher objects.
- Add permission capability only when the manifest declares a real permission use case.
- Read `architecture/13-permissions.md` before changing the contract.
- Verify module compilation and the consuming feature tests.

Implementation guide: [`IMPLEMENTATION.md`](IMPLEMENTATION.md).
