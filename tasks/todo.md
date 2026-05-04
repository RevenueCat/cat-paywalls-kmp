# Sync features from `articles-paywall-compose` (Android-only) → `cat-paywalls-kmp`

## Background
Android repo recently shipped:
1. **Bookmark feature** (commit `10deea5`) — bookmark articles, dedicated screen, premium-gated
2. **Reading tracker** — quota of 3 free articles/day, gates navigation behind paywall
3. **PromoBottomSheet** — appears on the 3rd article, offers subscription packages
4. **Navigation3 migration** — Android-only nav library; **NOT applicable to KMP** (skip)
5. **Test Store** — RevenueCat Test Store demo flow; will defer (separate scope)
6. **CustomerCenter screen** — already in KMP

This plan ports items 1–3.

## KMP-specific adaptations
- `kotlinx-datetime` instead of `java.time.LocalDate` (commonMain compatible)
- `androidx.datastore:datastore-preferences-core` (KMP variant since 1.1.x) with `PreferenceDataStoreFactory.createWithPath { ... }` and platform `expect`/`actual` for the storage path (Context filesDir on Android, NSDocumentDirectory on iOS)
- `@Inject` (Metro) replaces `@HiltViewModel`/`@Inject (javax)` and qualifier annotations
- KMP nav uses `articleId: Long` (existing); bookmarks key by `article.title` (matches Android impl)
- KMP `awaitPurchase(packageId)` doesn't take `Activity`; PromoBottomSheet wires through that signature
- Skip `SharedTransitionLayout` (KMP doesn't use it elsewhere)

## Tasks

### Phase 1 — Dependencies & versions
- [ ] Add `androidx-datastore = "1.1.7"` and `androidx-datastore-preferences-core` library to `gradle/libs.versions.toml`
- [ ] Confirm `kotlinx-datetime = "0.7.1"` is already present (yes, in versions block — only add library entry if missing)

### Phase 2 — Persistence layer (`core/data`)
- [ ] Add `BookmarksRepository` interface (commonMain) — `bookmarkedArticleTitles: Flow<Set<String>>`, `suspend toggleBookmark(title)`
- [ ] Add `ReadingTrackerRepository` interface (commonMain) — `todayReadCount: Flow<Int>`, `suspend recordArticleRead(title)`
- [ ] Add expect `dataStorePath(name: String): String` (commonMain) + Android/iOS actuals
- [ ] Add `BookmarksRepositoryImpl` & `ReadingTrackerRepositoryImpl` (commonMain) consuming `DataStore<Preferences>` via constructor; reading tracker uses `Clock.System.todayIn(...)` for daily reset
- [ ] Add factory funcs `createBookmarksRepository(...)` / `createReadingTrackerRepository(...)` in `di/DataModule.kt`
- [ ] `core/data/build.gradle.kts` — add datastore-preferences-core (commonMain), kotlinx-datetime (commonMain)

### Phase 3 — Navigation route
- [ ] Add `data object Bookmarks : CatArticlesScreen` to `core/navigation/.../CatArticlesScreen.kt`

### Phase 4 — `feature/bookmarks` module
- [ ] Create `feature/bookmarks/build.gradle.kts` (uses `catpaywalls.kmp.feature` plugin)
- [ ] Add `BookmarksViewModel` (commonMain) — combines articles + bookmarked titles → `BookmarksUiState`
- [ ] Add `BookmarksScreen` (commonMain) — Scaffold + LazyVerticalGrid; navigates to article detail
- [ ] Register `:feature:bookmarks` in `settings.gradle.kts`

### Phase 5 — Wire DI in `composeApp`
- [ ] Inject `DataStore<Preferences>` providers in `AppGraph` (using `dataStorePath` expect to build per-name DataStore instances; provide as named/qualified bindings or use distinct factory funcs)
- [ ] Add `provideBookmarksRepository()` and `provideReadingTrackerRepository()` to `AppGraph`
- [ ] Expose `bookmarksViewModel: BookmarksViewModel` from `AppGraph`
- [ ] Wire-up depends on Metro pattern — pass concrete `DataStore<Preferences>` per repo via factory funcs that accept the path

### Phase 6 — Update `CatArticlesNavHost`
- [ ] Register `composable<CatArticlesScreen.Bookmarks> { BookmarksScreen(viewModel = appGraph.bookmarksViewModel) }`

### Phase 7 — Home screen wiring
- [ ] `CatArticlesViewModel` — add `bookmarkedTitles`, `customerInfo`, `todayReadCount` StateFlows + `toggleBookmark()` (premium gate)
- [ ] `CatArticlesHome` — add Bookmark icon button to AppBar (navigates to `Bookmarks`); show `QuotaBanner` for non-entitled; route to Paywalls when quota exceeded; bookmark icon overlay on each card (gated)
- [ ] `feature/home/build.gradle.kts` — add `purchases-kmp-core` to commonMain (CustomerInfo type)

### Phase 8 — Article detail wiring
- [ ] `CatArticlesDetailViewModel` — add bookmark/reading flows, `shouldShowPromo`, `dismissPromo`, `purchasePackage(packageId)`, `recordRead`, `toggleBookmark` (premium gate)
- [ ] `CatArticlesDetail` — add bookmark icon to AppBar; show PromoBottomSheet on 3rd read; record read via `LaunchedEffect`; show `DailyLimitContent` when quota exceeded
- [ ] Add `PromoBottomSheet` composable
- [ ] `feature/article/build.gradle.kts` — add `purchases-kmp-core` to commonMain

### Phase 9 — App entry points
- [ ] `composeApp/build.gradle.kts` — add `:feature:bookmarks` to commonMain
- [ ] If DataStore needs Android `Context` for filesDir, accept it via `App` / `MainActivity` and pass through to `AppGraph` (likely yes — graph creation needs path; expect/actual for path uses platform APIs at runtime, so just call `dataStorePath("name")` from within `@Provides`)

### Phase 10 — Verification
- [ ] Run `./gradlew :composeApp:compileDebugKotlinAndroid` (Android target)
- [ ] Run `./gradlew :composeApp:compileKotlinIosArm64` (iOS target)
- [ ] Spotless: `./gradlew spotlessApply`
- [ ] Manually verify: bookmark icon toggles, quota banner counts up, PromoBottomSheet shows on 3rd article, bookmarks screen lists items, premium users bypass quota

## Out of scope (defer)
- Test Store integration (Android-only `Purchases.configureAsTestStoreApp`; KMP API support unclear, separate research needed)
- Navigation3 migration (Android-only library)
- Shared element transitions (KMP doesn't have this in current scope)
- Unit tests (existing tests pattern can be applied later)

## Open questions to verify with user
1. Should this be **one big commit** or **split into smaller commits/PRs** (data layer, then UI per screen)? Default: one feature branch with logical commits.
2. Test Store — port now or defer? Default: **defer** (separate scope, has Android-only specifics).
3. README / docs updates? Default: **skip** unless asked.

## Review

### What changed
- **Persistence (`core/data`)**: `BookmarksRepository` + `ReadingTrackerRepository` (commonMain) backed by `androidx.datastore:datastore-preferences-core` 1.1.7 via `PreferenceDataStoreFactory.create(OkioStorage(...))`. Path injected via `expect dataStoreDirectory()` with `androidMain` (Context.filesDir) and `iosMain` (`NSDocumentDirectory`) actuals. `installApplicationContext(this)` wired from `CatArticlesApplication.onCreate()` before graph creation. Date logic uses `kotlin.time.Clock.System.now().toLocalDateTime(...)` (kotlinx-datetime 0.7.x dropped `kotlinx.datetime.Clock`).
- **Navigation**: `CatArticlesScreen.Bookmarks` route added.
- **`feature/bookmarks` (new module)**: `BookmarksScreen`, `BookmarksViewModel`, `BookmarksUiState` (Loading/Empty/Success), wired into NavHost + AppGraph. Skipped Nav3 / SharedTransitionLayout (Android-only / unused in this repo).
- **`feature/home`**: AppBar bookmark icon, daily quota banner (3 free articles), bookmark overlay on each card, paywall redirect when quota exceeded or non-premium toggles bookmark.
- **`feature/article`**: AppBar bookmark icon, `LaunchedEffect`-driven `recordRead`, `DailyLimitContent` when quota exceeded, `PromoBottomSheet` on the 3rd article (lists offering packages, dismissable, calls `viewModel.purchasePackage(packageId)`).
- **Test Store**: `composeApp/build.gradle.kts` reads `revenuecat.test.api.key` from `local.properties` → `BuildConfig.REVENUECAT_TEST_API_KEY`. `CatArticlesApplication` prefers it over the default key when non-blank.
- **Tests**: New `BookmarksViewModelTest` (3 cases, turbine-based), new fakes for `Bookmarks`/`ReadingTracker`/`Paywalls` in `feature/home` test source set. Existing `CatArticlesViewModelTest` updated to new constructor signature.

### Verification
- `./gradlew :composeApp:compileDebugKotlinAndroid` — green
- `./gradlew :composeApp:compileKotlinIosArm64 :composeApp:compileKotlinIosSimulatorArm64 :composeApp:compileKotlinIosX64` — green
- `./gradlew :composeApp:assembleDebug` — green (APK built)
- `./gradlew testDebugUnitTest` — green (all KMP commonTest run on Android JVM)
- `./gradlew spotlessApply` — green

### Skipped scope
- Navigation3 migration — Android-only library, KMP stays on `org.jetbrains.androidx.navigation:navigation-compose`.
- Espresso instrumentation tests for Test Store flow — Android-only test infra; KMP unit tests cover state logic instead.
- README updates — not requested.

### Next time
- iOS device testing requires running on a Mac with simulator/device — not validated end-to-end here, only compilation. Run `./gradlew :composeApp:iosSimulatorArm64Test` on a simulator-equipped host to verify common test on iOS.
