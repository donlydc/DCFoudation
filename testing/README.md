# DC testing kit

Reusable JVM-oriented test rules and fakes for foundation consumers. The module stays
business-neutral and intentionally does not provide mocking or feature-specific repository
builders.

## Root wiring

Add the module to `settings.gradle.kts`:

```kotlin
include(":testing")
```

Add it to the test configuration of each consuming module (normally the app or a feature):

```kotlin
testImplementation(project(":testing"))
```

The module exposes the existing `:runtime:concurrency` and `:observability` contracts because
`FakeDispatcherProvider` and the recording fakes implement those public interfaces. Its other
API dependencies are JUnit 4 and `kotlinx-coroutines-test`, which are required by
`MainDispatcherRule`.

## Typical use

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()

private val dispatcherProvider = FakeDispatcherProvider(Dispatchers.Main)
private val logger = RecordingLogger()
private val analytics = RecordingAnalyticsTracker()
private val crashReporter = RecordingCrashReporter()
```

Use `runTest` and `advanceUntilIdle()` in the test body. The kit provides primitives only; the
feature test still owns its subject construction and any feature-specific fakes/builders.
