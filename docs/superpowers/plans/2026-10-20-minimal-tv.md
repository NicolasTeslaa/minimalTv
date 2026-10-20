# Minimal TV Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an installable Android TV launcher with dynamic app discovery, D-pad navigation, persistent favorites and measurable performance.

**Architecture:** One Android app module and one Activity with Home, favorites and settings states. Repository adapters isolate PackageManager and SharedPreferences; pure ordering rules are JVM testable. RecyclerView reuses focusable app cards.

**Tech Stack:** Kotlin, Android Views, RecyclerView, Gradle Kotlin DSL, JUnit, Android SDK.

**Spec:** `docs/superpowers/specs/2026-10-20-minimal-tv-design.md`; detailed rules in `SPEC.md`.

## Global Constraints

- Android TV 14 target hardware; minSdk 26, targetSdk 34, compileSdk 35.
- Background `#10151C`; violet palette from `DESIGN.md`.
- No networking, persistent service, QUERY_ALL_PACKAGES, or OS modification.
- Do not change the device's HOME role or disable the original launcher.
- Each behavior follows RED → GREEN → REFACTOR; record actual command output.

---

### Task 1: Build foundation and ordering

**Files:** `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle/wrapper/*`, `app/src/main/AndroidManifest.xml`, `app/src/main/java/com/minimaltv/launcher/model/TvApp.kt`, `app/src/main/java/com/minimaltv/launcher/data/AppOrder.kt`, `app/src/test/java/com/minimaltv/launcher/data/AppOrderTest.kt`.

**Interfaces:** `TvApp(packageName: String, name: String, componentName: String)`; `AppOrder.sort(apps, favorites, launchCounts): List<TvApp>`.

- [ ] Write JUnit test with apps C, A, B: A/B favorites with counts 2/4; expect B,A,C, and tie by name.
- [ ] Run `./gradlew :app:testDebugUnitTest --tests '*AppOrderTest'` and confirm missing implementation failure.
- [ ] Implement model and comparator with favorite/count/name/package tie breakers.
- [ ] Re-run test to green; refactor and re-run.
- [ ] Add manifest, resources, theme, launcher banner and Activity shell; run `./gradlew :app:assembleDebug`.

### Task 2: Persistence and discovery

**Files:** `data/PreferencesRepository.kt`, `data/AppRepository.kt`, `data/LaunchResolver.kt`, tests under `app/src/test/.../data/` and `app/src/androidTest/.../`.

**Interfaces:** `PreferencesRepository` exposes favorite package IDs and launch counts; `AppRepository.loadApps()` returns unique launchable `TvApp` entries with Leanback priority.

- [ ] Write persistence test: toggle favorite, record two launches, recreate repository, expect retained state. Run targeted test to RED.
- [ ] Write removal test: remove package, recreate repository, reinstall same package, expect no favorite and zero launches. Run to RED.
- [ ] Implement SharedPreferences state minimally; re-run to GREEN.
- [ ] Write discovery test with fake package query: Leanback wins over ordinary launcher; fallback appears only when Leanback absent; non-launchable/self package excluded. Run targeted test to RED.
- [ ] Implement discovery and intent resolution via PackageManager, off the UI thread; re-run to GREEN.
- [ ] Add package-change receiver with `package` data scheme; add `<queries>` for Leanback and ordinary launcher. Test an update and focus behavior.

### Task 3: Home and D-pad behavior

**Files:** `MainActivity.kt`, `ui/AppGridAdapter.kt`, `ui/FocusRestorer.kt`, `res/layout/*`, `res/drawable/*`, `res/values/*`, UI instrumentation tests.

**Interfaces:** Grid cards open resolved app on short OK and show favorite menu on long OK. `FocusRestorer` chooses retained package or adjacent visible item.

- [ ] Write UI test for initial focus, D-pad movement and OK action; run to RED.
- [ ] Implement RecyclerView four-column grid, focus ring, icon loading and clock minute updates; run to GREEN.
- [ ] Write UI test for long OK favorite toggle and Back; run to RED.
- [ ] Assert long OK suppresses short OK on key release, and failed launches do not increment counts.
- [ ] Implement contextual menu, favorites management and settings state; run to GREEN.
- [ ] Add error feedback for missing launch target; check rapid D-pad events and return from external app.

### Task 4: QA, device and delivery

**Files:** `README.md`, `TEST_PLAN.md`, `PERFORMANCE.md`, `CHANGELOG.md`, optional UI fixes and tests.

- [ ] Run all unit tests and `:app:assembleDebug`; fix only observed failures, with a reproducing test first.
- [ ] Install debug APK with ADB on the connected Aiwa; launch as an ordinary app, test D-pad, favorites, Netflix and Play Store. Do not change HOME role.
- [ ] Verify offline Home, process restart, HOME eligibility without assigning it, and return from apps by Back/reopening Activity (Home goes to the factory launcher until the user chooses a default).
- [ ] Capture screenshots at logical 1280×720 and, if safe, alternate size on emulator; collect PSS, RSS, CPU and startup timing with conditions noted.
- [ ] Write test evidence, performance results, recovery instructions and known limitations, including unavailable apps or tools.
- [ ] Review diff and git status. If no GitHub Actions workflow exists, commit and push to the configured branch; otherwise ask for authorization.
