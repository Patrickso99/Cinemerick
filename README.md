# Cinemerick

![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-blue?logo=kotlin)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.12.1-brightgreen?logo=jetpackcompose)
![Koin](https://img.shields.io/badge/Koin-4.2.2-orange)
![Ktor](https://img.shields.io/badge/Ktor-3.6.0-purple)

A **Kotlin Multiplatform** cinema showtimes aggregator that fetches real-time schedules from multiple cinema chains (The Space, UCI, Notorious, Cinergia, Cristallo, Don Bosco, Cinemazero, Arcadia) and generates shareable poll text for group chats.

## Features

- 🎬 **Multi-chain showtimes**: Query 8 cinema chains with multiple venues per chain, discovered at runtime from each chain's API
- 📅 **Flexible date/time filtering**: Select days and time ranges per day, with a time range slider
- ⏭️ **Quick selection**: Jump directly to today, tomorrow or the weekend's showtimes without manual date selection
- 🗓️ **Calendar**: Pick several dates at once; on larger screens the calendar sits inline in the filters panel (a dialog on mobile)
- 🏢 **Venue selection**: Choose which venues to query in a searchable multi-select sheet; venues are fetched at runtime (no hardcoded limits)
- 🎞️ **Film filtering**: Narrow results by comma-separated film titles
- 🖼️ **Film posters**: Each film shows its poster (UCI's when both cinemas are selected, otherwise The Space's), loaded with Coil
- 📖 **Film details**: Tap a film for synopsis, tagline, genres, saga, director and cast (with photos and characters), trailer, runtime, year, original title and language, production countries and companies, budget and box office (when known), Italian release date, a backdrop with a gallery and the TMDB score. Details come from [TMDB](https://www.themoviedb.org) in Italian whatever the language of the app (a synopsis or trailer TMDB only has in another language is not shown); films TMDB does not recognise (concerts, live events, rassegne) fall back to what the cinemas publish, and a TMDB result is never mixed with cinema data
- 🎟️ **Showtime status**: Sold-out showings are marked
- 📋 **Copy-to-clipboard**: Generate poll text and copy it with one tap; the poll text stays in Italian
- 🌐 **Language choice**: English (default) or Italiano, selected in Settings and applied per platform
- 🌓 **Theme choice**: Auto (follows the system), Light or Dark, selected in Settings
- ✨ **Animations toggle**: Smooth navigation, tap, scroll and item effects can be turned off in Settings
- 🧱 **Results grid**: Film cards flow into a multi-column grid on larger screens (a single list on mobile)
- 📱 **Responsive design**: Adaptive layouts for phone, tablet and desktop, in portrait and landscape
- 🎥 **Smart movie unification**: Handles accent variations in film titles across cinemas (e.g., "Café" vs "Cafe")
- 🏷️ **Version in Settings**: Shown as `v1.1.9 (20261016)` (version name and date-based code)
- 🪵 **Logging**: Kermit logs network failures and fetch results on every platform
- 🏗️ **MVI architecture**: Clean separation of state, actions, and events
- 🔧 **Koin DI**: Lightweight dependency injection across all modules
- 🔐 **Error resilience**: Debounce and retry logic for transient failures (HTTP 429 rate limits, 500 errors); one cinema failure doesn't hide results from others
- ⚡ **Short-lived cache**: Showtimes are cached for 5 minutes per venue and day

## Platform Support

| Platform | Status |
|----------|--------|
| Android | ✅ API 26+ |
| Desktop | ✅ JVM (Windows, macOS, Linux) |
| iOS | ✅ arm64 + simulator (deployment target iOS 16.0) |

## Architecture

Cinemerick follows a **modular Kotlin Multiplatform** structure:

| Module | Responsibility |
|--------|----------------|
| `:core:domain` | Result types, errors, preference interfaces, `BuildKonfig` |
| `:core:data` | HTTP client, retry queue, settings preferences |
| `:core:presentation` | `UiText`, controllers (theme, language, animations, time window) |
| `:core:design-system` | Theme, typography, shapes, adaptive layouts, components |
| `:core:testing` | Test helpers |
| `:feature:showtimes:domain` | Models, repository interfaces, filtering and formatting |
| `:feature:showtimes:data` | Cinema data sources, HTML parsers, TMDB lookup, cache, merging |
| `:feature:showtimes:presentation` | MVI ViewModel, showtimes screen, pickers, film details |
| `:feature:settings:presentation` | Settings screen and its ViewModel |

### Build Plugins

Shared Gradle setup lives in `:build-logic:convention`, under the `com.preichert.convention.*` IDs.

| Plugin | Adds |
|--------|------|
| `android.application` | Android app config, version from `AppInfo` |
| `cmp.application` | Compose Multiplatform app: Android, desktop and iOS targets |
| `kmp.library` | Kotlin Multiplatform library with all targets |
| `cmp.library` | `kmp.library` plus Compose Multiplatform |
| `cmp.feature` | `cmp.library` plus Koin Compose and ViewModel |
| `koin` | Koin core |
| `ktor` | Ktor client with a platform engine |
| `serialization` | kotlinx.serialization JSON |
| `buildkonfig` | `BuildKonfig` in `:core:domain` (version, git hash, TMDB token) |

## Tech Stack

| Library | Version |
|---------|---------|
| **Kotlin** | 2.4.20 |
| **Compose Multiplatform** | 1.12.1 |
| **Material 3** | 1.9.0 |
| **Material 3 Adaptive** | 1.2.0 |
| **Ktor Client** | 3.6.0 |
| **Koin** | 4.2.2 |
| **Kermit** (logging) | 2.2.0 |
| **Coil** (images) | 3.6.3 |
| **BuildKonfig** | 0.23.0 |
| **Multiplatform Settings** | 1.3.0 |
| **Kotlinx Serialization** | 1.11.0 |
| **Kotlinx Datetime** | 0.8.0 |
| **Kotlinx Coroutines** | 1.11.0 |
| **Navigation Compose** | 2.9.2 |
| **Lifecycle** | 2.11.0 |
| **Activity** (Android) | 1.13.0 |
| **AppCompat** (Android) | 1.7.1 |
| **AGP** | 9.2.1 |
| **Gradle** (wrapper) | 9.6.0 |

Typography uses the Plus Jakarta Sans font, bundled in `:core:design-system`.

## Getting Started

### Prerequisites

- JDK 17+ (JDK 21 for the iOS build phase)
- Gradle wrapper (`./gradlew`, Gradle 9.6.0); no separate Gradle install needed
- Android SDK 37 (compile), API 26 (min)
- Xcode with the iOS 16.0+ SDK (for iOS)

### TMDB access token (optional)

Film details (synopsis, cast, director, trailer, runtime, score) come from [TMDB](https://www.themoviedb.org); films TMDB does not know fall back to what the cinemas publish. To enable the lookup you need a free TMDB account and its **API Read Access Token** (account settings → API; the short "API Key" is not used):

- locally, add `tmdb.token=<token>` to `local.properties` (git-ignored);
- in CI, provide it as the `TMDB_TOKEN` secret / environment variable.

Without a token the app builds and runs exactly as before, using only the cinemas' data.

### Build & Run

#### Desktop

```bash
./gradlew :app:run
```

Resize the window to see adaptive layouts (narrow → mobile, wide → tablet/desktop).

To create a distributable DMG package for macOS:

```bash
./gradlew :app:packageDmg
```

Output: `app/build/compose/binaries/main/dmg/`

#### Android

Debug APK:

```bash
./gradlew :androidApp:installDebug
```

Release APK (unsigned):

```bash
./gradlew :androidApp:assembleRelease
```

Output: `androidApp/build/outputs/apk/release/cinemerick-v1.1.9-{VERSION_CODE}.apk`

> **APK Naming**: All generated APKs follow the convention `cinemerick-vX.X.X-{versionCode}[-debug].apk`, where the version code is auto-generated as `yyyyMMdd` (e.g., `20261016`). Example: `cinemerick-v1.1.9-20261016.apk`.

#### iOS

Open `iosApp/iosApp.xcodeproj` in Xcode. The Xcode build phase runs Gradle (`:app:embedAndSignAppleFrameworkForXcode`) to build the Kotlin framework, so the Kotlin side builds as part of the app build. The app name and version are read from the Gradle project (`:app:generateIosVersionConfig`).

```bash
open iosApp/iosApp.xcodeproj
```

#### Fastlane (build artifacts)

Fastlane only builds the artifacts and copies them into `dist/`; nothing is uploaded.

Install Fastlane with `brew install fastlane` (or `bundle install` with Ruby 3.1+), then:

```bash
fastlane lanes
fastlane android apk_release
```

| Lane | Output | Host OS |
|------|--------|---------|
| `android apk_debug` / `android apk_release` | `.apk` (release is signed with `ANDROID_KEYSTORE_*` when set, otherwise the debug key) | any |
| `desktop_dmg` | `.dmg` | macOS |
| `desktop_exe` / `desktop_msi` | `.exe` / `.msi` (requires WiX 3) | Windows |
| `desktop_deb` | `.deb` | Linux |
| `ios ad-hoc` | ad-hoc `.ipa` | macOS |
| `all_local` | everything buildable on the current OS | any |

Desktop packages cannot be cross-compiled, so a lane run on the wrong OS fails with an explanatory message. The GitHub Actions workflows in `.github/workflows/` (manual run, or a `v*` tag) build one artifact each: Android APK, macOS DMG, Windows EXE, Windows MSI, Linux DEB and iOS IPA. The IPA is local only: the ad-hoc export needs your signing identity and a provisioning profile with registered devices.

#### Tests

```bash
./gradlew :core:domain:allTests :core:data:allTests :core:presentation:allTests
./gradlew :feature:showtimes:domain:allTests :feature:showtimes:data:allTests :feature:showtimes:presentation:allTests
./gradlew :feature:settings:presentation:allTests
```

The HTML parsers have their own tests in `:feature:showtimes:data`.

## Design System

Cinemerick uses a **purple, role-based palette** from the redesign (specification in `docs/design`). Colors are defined as tokens in `Color.kt` (`LightCinemerickColors` / `DarkCinemerickColors`), and Material's color scheme is built from the same tokens.

### Color Palette (Light)

| Role | Hex |
|------|-----|
| Background | `#FAF8FF` |
| Surface | `#FFFFFF` |
| Surface container | `#F3F0FC` |
| Primary | `#5B3FD6` |
| On surface | `#1B1730` |
| Primary container | `#E7E0FF` |

### Color Palette (Dark)

| Role | Hex |
|------|-----|
| Background | `#0D0B14` |
| Surface | `#15121F` |
| Surface container | `#24203D` |
| Primary | `#A78BFA` |
| On surface | `#ECE9F7` |
| Primary container | `#3A2C7A` |

### Cinema Brand Colors

`CinemaBrandColors` (in `Color.kt`) defines one fixed accent color per cinema, used only for its badge and chips, so they stay recognizable in light and dark mode (e.g. UCI `#1F3F7A`, The Space `#E8650A`, Cinergia `#414C4C`, Cristallo `#D22D27`, Don Bosco `#8B3A3B`, Cinemazero `#032129`, Arcadia `#6E398A`). When adding a cinema, also add its logo to `design/logos/` and update `design/generate_icons.py`, which regenerates the app icons for all platforms.

### Typography and Shapes

- **Typography**: Plus Jakarta Sans, with SemiBold / Bold weights for titles and regular weight for body text
- **Shapes**: Rounded corners from 8 dp (format tags) up to 28 dp (sheets and the bottom bar); buttons and switches use a pill shape

## Adaptive Layouts

The app picks its layout from the window size (`DeviceConfiguration`):

- **Mobile Portrait**: Single column, stacked filters and results
- **Mobile Landscape**: Two-column layout (filters left, results right)
- **Tablet Portrait**: Centered two-panel layout
- **Tablet Landscape & Desktop**: Fixed header with two panels (filters / results) that scroll independently; the results panel is wider on desktop

## Project Structure

```
Cinemerick/
├── androidApp/                       # Android application (manifest, MainActivity)
├── app/                              # Shared entry points (Desktop, iOS, Android library)
├── core/
│   ├── domain/                       # Result types, errors, preferences, BuildKonfig
│   ├── data/                         # HTTP client, SafeCall, retry queue, settings preferences
│   ├── presentation/                 # UI utilities, controllers, DeviceConfiguration
│   ├── design-system/                # Theme, colors, typography, shapes, layouts, components
│   └── testing/                      # Shared test helpers
├── feature/
│   ├── showtimes/
│   │   ├── domain/                   # Business logic, models, filtering
│   │   ├── data/                     # APIs, DTOs, parsers, TMDB lookup, merging, cache
│   │   └── presentation/             # ViewModel, Screen, venue and date pickers
│   └── settings/
│       └── presentation/             # Settings screen, ViewModel, navigation
├── iosApp/                           # Xcode project and project.yml
├── build-logic/convention/           # Gradle convention plugins
├── design/                           # Cinema logos and app icon generator
├── docs/design/                      # Redesign specification
├── fastlane/                         # Build lanes for every platform
├── .github/workflows/                # CI builds per platform
└── gradle/libs.versions.toml         # Centralized dependency versions
```

## How It Works

1. **Choose cinemas and venues** in the venue picker
2. **Select days** and time ranges in the filter panel (use "Today", "Tomorrow" or "Weekend" for a quick selection, or pick dates in the calendar)
3. **Optional: filter by film title** (comma-separated)
4. **Tap "Generate"** to fetch showtimes from all the selected chains
5. **Results appear** grouped by film, with poster, cinema and time (unified across cinemas even with accent variations)
6. **Tap "Copy"** to copy the poll text to clipboard
7. **Paste** into your group chat and start the poll 🎬

Example poll text:
```
Film Name (Thursday 21:00 - Cinema 1)
Film Name (Thursday 21:30 - Cinema 2)
Other Film (Friday 20:00 - Cinema 1)
```

## Error Handling

The app implements robust error handling for network resilience:

- **Graceful degradation**: If one cinema API fails, results from the other cinemas are still displayed with an error message showing which cinema had issues. Users always get partial results when possible.
- **Debounce & Retry**: Automatic retry logic with exponential backoff for transient failures:
  - HTTP 429 (Rate Limited): Requests are debounced and retried after the server's retry-after delay
  - HTTP 500 (Server Error): Automatic retry with exponential backoff for temporary server issues
- **User experience**: Failed requests don't block the entire operation; the app continues fetching from working sources and surfaces errors in the UI

## Contributing

This is a personal project, but contributions and suggestions are welcome. Please ensure:
- All comments and commit messages are in **English**
- Code follows the existing modular structure
- Tests pass (see [Tests](#tests))

## Credits

Film details are provided by TMDB. This product uses the TMDB API but is not endorsed or certified by TMDB. The credit (logo, notice and a localized description) is shown in the app's settings screen.

## License

MIT License

## Author

**Patrick Reichert**  
Maintained with ❤️ and Kotlin ☕
