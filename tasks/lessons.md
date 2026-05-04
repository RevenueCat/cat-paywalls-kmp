# Lessons — cat-paywalls-kmp

## KMP gotchas encountered while porting Android features

### kotlin.time.Clock supersedes kotlinx.datetime.Clock in Kotlin 2.1+
- Kotlin 2.3 + kotlinx-datetime 0.7.x: `kotlinx.datetime.Clock.System` no longer resolves; the canonical type lives at `kotlin.time.Clock` and requires `@OptIn(ExperimentalTime::class)`.
- For "today's date" use `Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()` (kotlinx-datetime still provides the `LocalDate` and `TimeZone` types and the `Instant.toLocalDateTime` extension).

### androidx.datastore preferences in KMP (1.1+)
- Use `PreferenceDataStoreFactory.create(storage = OkioStorage(FileSystem.SYSTEM, PreferencesSerializer, producePath = { ... }))`.
- The path must be platform-specific via `expect`/`actual`:
  - Android: `applicationContext.filesDir.toOkioPath().resolve("datastore")` — needs a Context. Cleanest is a one-shot `installApplicationContext(this)` from `Application.onCreate()` before the DI graph is created.
  - iOS: `NSFileManager.defaultManager.URLForDirectory(NSDocumentDirectory, NSUserDomainMask, …)` (cinterop, `@OptIn(ExperimentalForeignApi::class)`).

### Metro DI without qualifiers
- Prefer hiding multiple instances of the same type behind distinct factory functions over `@Named`/qualifier annotations. E.g. `createBookmarksRepository()` and `createReadingTrackerRepository()` each create their own `DataStore<Preferences>` internally — no qualifier annotation needed in `@DependencyGraph`.

### Spotless quirk
- `./gradlew spotlessApply` reorders imports to `kotlin.*` last; will silently rewrite source files. Don't be surprised by the diff.

### Skip Android-only library migrations
- Navigation3 (`androidx.navigation3`) is Android-only — KMP repo stays on `org.jetbrains.androidx.navigation:navigation-compose`.
- Test Store has no platform-specific KMP API — sandbox is just a different `apiKey` passed to `Purchases.configure(...)`. Wire via BuildConfig from `local.properties` so commits don't leak the key.
