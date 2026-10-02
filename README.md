# Cinemerick

![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-blue?logo=kotlin)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.12.1-brightgreen?logo=jetpackcompose)
![Koin](https://img.shields.io/badge/Koin-4.2.2-orange)
![Ktor](https://img.shields.io/badge/Ktor-3.6.0-purple)

A **Kotlin Multiplatform** cinema showtimes aggregator that fetches real-time schedules from multiple cinema chains (The Space Cinema, UCI, Notorious, Cinergia, Cristallo) and generates shareable poll text for group chats.

## Features

- 🎬 **Multi-source aggregation**: Query The Space Cinema (Silea), UCI (Marcon), Notorious (Ferrara), Cinergia (Conegliano, via 18tickets) and Cristallo (Oderzo) simultaneously
- 📅 **Flexible date/time filtering**: Select days and time ranges per day
- 🏢 **Cinema selection**: Choose which chains to query (The Space / Silea, UCI / Marcon, Notorious / Ferrara, Cinergia / Conegliano, Cristallo / Oderzo); all are on by default and excluded chains are never fetched
- 🖼️ **Film posters**: Each film shows its poster (UCI's when both cinemas are selected, otherwise The Space's), loaded with Coil
- 🏷️ **Version in the header**: Shown as `v1.0.8 (20261002) [abc1234]` (version name, date-based code, git short hash)
- 🪵 **Logging**: Kermit logs network failures and fetch results on every platform
- 🎞️ **Film filtering**: Narrow results by comma-separated film titles
- 📋 **Copy-to-clipboard**: Generate poll text and copy it with one tap
- 🌓 **Dark mode**: Adaptive purple theme (light & dark) with quick theme toggle UI
- 📱 **Responsive design**: Optimized layouts for mobile, tablet, and desktop screens
- 🗓️ **Inline calendar**: On larger screens the day picker is shown inside the filters panel (a dialog on mobile)
- ⏭️ **Tomorrow quick selection**: Jump directly to tomorrow's showtimes without manual date selection
- 🎥 **Smart movie unification**: Handles accent variations in film titles across cinemas (e.g., "Café" vs "Cafe")
- 🧱 **Results grid**: Film cards flow into a multi-column grid on larger screens (a single list on mobile)
- 🏗️ **MVI architecture**: Clean separation of state, actions, and events
- 🔧 **Koin DI**: Lightweight dependency injection across all modules
- 🔐 **Error resilience**: Debounce and retry logic for transient failures (HTTP 429 rate limits, 500 errors); one cinema failure doesn't hide results from others

## Platform Support

| Platform | Status |
|----------|--------|
| Android | ✅ API 26+ |
| Desktop | ✅ JVM (Windows, macOS, Linux) |
| iOS | ✅ arm64 + simulator |

## Architecture

Cinemerick follows a **modular Kotlin Multiplatform** structure:

### Core Modules

- **`:core:domain`**: Common result types and data errors
- **`:core:data`**: HTTP client setup, safe API call wrapping, platform-specific network configuration
- **`:core:presentation`**: UI utilities (error text mapping, text resources, event observation)
- **`:core:design-system`**: Compose theme, colors (purple palette), typography, shapes, adaptive layouts, and reusable components

### Feature Modules

- **`:feature:showtimes:domain`**: Business logic
  - `Cinema` / `CinemaShowings`: Core data models
  - `ShowtimesRepository` / `ShowtimesDataSource`: Repository pattern
  - `ShowingFilter`, `ShowingFormatter`, `ShowingGrouping`: Filtering and formatting logic
  - `DayRange`, `FilmTitles`, `Showing`: Domain value objects

- **`:feature:showtimes:data`**: Data layer
  - `KtorTheSpaceShowtimesDataSource` / `KtorUciShowtimesDataSource` / `KtorNotoriousShowtimesDataSource` (HTML parsing) / `KtorCinergiaShowtimesDataSource` / `KtorCristalloShowtimesDataSource` (HTML parsing): Platform HTTP clients
  - `NotoriousParser` / `CinergiaParser` / `CristalloParser`: HTML parsers for the cinemas without a JSON API (each with a `*ParserTest`)
  - `HtmlEntities` (`unescapeHtml`): Decodes named and numeric HTML entities in scraped text
  - `TheSpaceTokenProvider`: Extracts JWT from The Space Cinema pages
  - `ResultMerging`: Merges and deduplicates results from multiple cinemas
  - DTOs and mappers to/from domain models

- **`:feature:showtimes:presentation`**: UI layer (Jetpack Compose)
  - `ShowtimesViewModel`: MVI state management
  - `ShowtimesScreen`: Adaptive responsive UI with filters and results
  - `ShowtimesState`, `ShowtimesAction`, `ShowtimesEvent`: State machine

### Build Plugins

Convention plugins in `:build-logic:convention` handle Gradle configuration:
- `cinemerick.android.application`: Android app config
- `cinemerick.kmp.library`: KMP library defaults
- `cinemerick.kmp.compose`: Compose Multiplatform setup
- `cinemerick.kmp.feature`: Feature module structure
- `cinemerick.koin`: Koin DI setup
- `cinemerick.buildkonfig`: Generates `BuildKonfig` (`VERSION_NAME`, `VERSION_CODE`, `GIT_HASH`) in `:core:domain`; the version is defined once in `AppVersion.kt` and shared with the Android and desktop packaging
- `cinemerick.ktor`: Ktor client configuration
- `cinemerick.kotlinx-serialization`: Serialization plugin

## Tech Stack

| Library | Version |
|---------|---------|
| **Kotlin** | 2.4.20 |
| **Compose Multiplatform** | 1.10.3 |
| **Material 3** | 1.9.0 |
| **Ktor Client** | 3.5.2 |
| **Koin** | 4.2.2 |
| **Kermit** (logging) | 2.2.0 |
| **Coil** (images) | 3.4.0 |
| **BuildKonfig** | 0.23.0 |
| **Kotlinx Serialization** | 1.11.0 |
| **Kotlinx Datetime** | 0.8.0 |
| **Kotlinx Coroutines** | 1.11.0 |
| **Lifecycle** | 2.8.1 |
| **Activity** (Android) | 1.9.0 |
| **AGP** | 9.2.1 |

## Getting Started

### Prerequisites

- JDK 17+
- Gradle 8.1+
- Android SDK 37 (compile), API 26 (min)
- Xcode 14+ (for iOS)

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

Output: `app/build/compose/binaries/main/dmg/Cinemerick-1.0.8.dmg`

#### Android

Debug APK:

```bash
./gradlew :androidApp:installDebug
```

Release APK (unsigned):

```bash
./gradlew :androidApp:assembleRelease
```

Output: `androidApp/build/outputs/apk/release/cinemerick-v1.0.8-{VERSION_CODE}.apk`

> **APK Naming**: All generated APKs follow the convention `cinemerick-vX.X.X-{versionCode}[-debug].apk`, where the version code is auto-generated as `yyyyMMdd` (e.g., `20261002`). Example: `cinemerick-v1.0.8-20261002.apk`.

#### iOS

Open `iosApp/` in Xcode for iOS builds:

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
| `android apk_debug` / `android apk_release` | `.apk` | any |
| `desktop_dmg` | `.dmg` | macOS |
| `desktop_exe` / `desktop_msi` | `.exe` / `.msi` (requires WiX 3) | Windows |
| `desktop_deb` | `.deb` | Linux |
| `ios ad-hoc` | ad-hoc `.ipa` | macOS |
| `all_local` | everything buildable on the current OS | any |

Desktop packages cannot be cross-compiled, so a lane run on the wrong OS fails with an explanatory message. The `Build artifacts` GitHub Actions workflow (manual run or `v*` tag) builds APK and DEB on Ubuntu, EXE and MSI on Windows, and DMG on macOS. The IPA is local only: the ad-hoc export needs your signing identity and a provisioning profile with registered devices.

#### Tests

```bash
./gradlew :feature:showtimes:domain:allTests
./gradlew :feature:showtimes:data:allTests   # HTML parsers
```

## Design System

Cinemerick uses a **custom purple tonal palette** inspired by Material Design 3:

### Color Palette (Light)

| Role | Color | Hex |
|------|-------|-----|
| Primary | Purple | `#6750A4` |
| Primary Container | Light Purple | `#EADDFF` |
| Secondary | Mauve | `#7D5260` |
| Tertiary | Mauve (secondary) | `#7D5260` |
| Background | Off-white | `#FFFBFE` |
| Surface | Off-white | `#FFFBFE` |
| Error | Red | `#B3261E` |

### Color Palette (Dark)

| Role | Color | Hex |
|------|-------|-----|
| Primary | Light Purple | `#D0BCFF` |
| Primary Container | Deep Purple | `#4F378B` |
| Secondary | Mauve | `#FFEFCB` |
| Background | Very Dark | `#1C1B1F` |
| Surface | Very Dark | `#1C1B1F` |
| Error | Light Red | `#F2B8B5` |

### Cinema Brand Colors

`CinemaBrandColors` (in `Color.kt`) defines one accent color per cinema, used for its badge in the results (e.g. Cinergia `#414C4C`, Cristallo `#D22D27`). When adding a cinema, also add its logo to `design/logos/` and update `design/generate_icons.py`, which regenerates the app icons for all platforms.

### Typography

- **Titles**: SemiBold / Bold weights for emphasis
- **Body**: Regular weight, increased letter-spacing for legibility
- **Shapes**: Rounded corners (4–20 dp) for modern, friendly appearance

## Adaptive Layouts

The app automatically adapts to screen size and orientation:

- **Mobile Portrait** (< 600 dp width): Single column, stacked filters and results
- **Mobile Landscape** (≥ 600 dp width, < 840 dp height): Two-column layout (filters left, results right)
- **Tablet Portrait** (600–840 dp width): Centered two-panel layout
- **Tablet Landscape & Desktop** (≥ 840 dp width): Fixed header with two panels (filters / results) that scroll independently; max content width 1200 dp on tablet and 1400 dp on desktop, where the results panel is wider (0.8 : 1.2)

## Project Structure

```
Cinemerick/
├── androidApp/                       # Android application (manifest, MainActivity)
├── app/                              # Shared entry points (Desktop, iOS, Android library)
├── core/
│   ├── domain/                       # Result types, errors
│   ├── data/                         # HTTP client, SafeCall, platform config
│   ├── presentation/                 # UI utilities, DeviceConfiguration
│   └── design-system/                # Theme, colors, typography, layouts
├── feature/showtimes/
│   ├── domain/                       # Business logic, models, filtering
│   ├── data/                         # APIs, DTOs, mappers, merging
│   └── presentation/                 # ViewModel, Screen, State machine
├── build-logic/convention/           # Gradle convention plugins
└── gradle/libs.versions.toml         # Centralized dependency versions
```

## How It Works

1. **Choose cinemas** (both selected by default)
2. **User selects days** and time ranges in the filter panel (use "Tomorrow" quick selection or manually pick dates)
3. **Optional: filter by film title** (comma-separated)
4. **Tap "Generate"** to fetch showtimes from The Space Cinema, UCI, Notorious, Cinergia and Cristallo
5. **Results appear** grouped by film title with poster, cinema and time (unified across cinemas even with accent variations)
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
- Tests pass: `./gradlew :feature:showtimes:domain:allTests :feature:showtimes:data:allTests`

## License

MIT License (see LICENSE file if present)

## Author

**Patrick Reichert**  
Maintained with ❤️ and Kotlin ☕