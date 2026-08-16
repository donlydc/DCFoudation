# App bootstrap & lifecycle

[Back to architecture index](00-index.md)

## Scope

App startup ordering and process-death/restorable-state policy. Does not own screen-level state shape (see [`05-presentation-udf.md`](05-presentation-udf.md)).

## Current implementation

- `DcApplication` (`@HiltAndroidApp`) — no custom init logic yet; exists purely as the Hilt component root (Phase D).
- `MainActivity` (`@AndroidEntryPoint`) — `onCreate` calls `enableEdgeToEdge()` then `setContent { DcTheme { GreetingRoute() } }`. No other lifecycle hooks are overridden.
- **Process death / restorable state:** `GreetingViewModel` takes an injected `SavedStateHandle` and stores `nameIndex` through it (`savedStateHandle["nameIndex"]`) instead of a plain `var`, so the greeting survives process death + restoration, not just configuration change. Verified in `GreetingViewModelTest`: `nameIndex survives via SavedStateHandle` constructs two ViewModel instances over the same handle to simulate restoration.

## Rules

### LC-01 — State that must survive process death goes through `SavedStateHandle`, not a plain field

**Rule:** A ViewModel field only `viewModelScope`-safe against configuration change (not process death) must be justified; anything the user would notice reverting after the OS kills the process goes through `SavedStateHandle`.

**Why:** `ViewModel` alone survives configuration change but not process death; `SavedStateHandle` is what actually survives that (Android platform behavior, not a DC-specific invention).

**Verification:** Inspect ViewModel constructors for `SavedStateHandle` where the feature has user-visible transient state worth restoring.

## Testing / validation

`GreetingViewModelTest.nameIndex survives via SavedStateHandle` — real (not simulated-in-prose) proof.

## Related documents

- [`05-presentation-udf.md`](05-presentation-udf.md)
- [`08-dependency-injection-hilt.md`](08-dependency-injection-hilt.md)
