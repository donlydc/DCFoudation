# C1-design — D-02 navigation depth + PERM-02 shape (PROPOSED)
## Navigation (D-02)
1. Status quo (Destination only). 2. **Minimal route/effect contract (recommended)**: `NavigationEffect {Navigate, Back}` + `DestinationRoutes` (stable route, round-trip). No Navigator/NavController wrapper. 3. Full `Navigator` interface + fake + deep-link table: rejected until a 2nd consumer navigates from a ViewModel (only `:app` MainActivity navigates today; destinations have no arguments, so NAV-01 argument encoding has no consumer -> deferred).
## Permission requester/observer (PERM-02)
1. Keep PermissionState only. 2. **Additive `PermissionStatus` (5 cases) + resolver + lifecycle-free `PermissionRequester` (suspend check/request) + `PermissionRequestTracker` (double-request guard) + `FakePermissionRequester` (recommended)**; legacy 3-state `PermissionState` untouched (app `when` exhaustive). 3. Flow-based observer + Activity-bound implementation: deferred (needs Activity glue in :app, lane C2).
