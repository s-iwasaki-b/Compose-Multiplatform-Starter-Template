[![Kotlin](https://img.shields.io/badge/kotlin-2.3.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.1-blue)](https://www.jetbrains.com/compose-multiplatform/)
[![GitHub](https://img.shields.io/github/license/s-iwasaki-b/Compose-Multiplatform-Starter-Template)](https://github.com/s-iwasaki-b/Compose-Multiplatform-Starter-Template/blob/main/LICENSE)

# Compose Multiplatform Starter Template

A starter template for building **Android** and **iOS** applications with [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/).

This template provides a pre-configured multi-module project structure following the [Android Architecture Guide](https://developer.android.com/topic/architecture), so you can start development right away.

### Features

- **Clean Architecture** with layered module separation (UI / Domain / Data)
- **MVVM + Event-driven UI** pattern with `StateFlow` and sealed event interfaces
- **Multi-module structure** with 11 pre-configured Gradle modules
- **Type-safe navigation** with deep linking support
- **Dependency injection** with Koin, pre-wired across all layers
- **Networking** with Ktor + Ktorfit (Retrofit-like type-safe REST client)
- **Async image loading** with Coil
- **Pagination** with AndroidX Paging 3
- **Unit testing** setup with Mokkery mocking framework
- **Gradle convention plugins** for consistent module configuration

# Demo

This repository includes a simple [Zenn](https://zenn.dev/) article viewer as sample code.

| Pixel 3a - Android 12 | iPhone XS - iOS 18.0 |
|--|--|
| <video src="https://github.com/user-attachments/assets/e9bdc1c5-458a-4e9d-8af7-780f556cbd23"> | <video src="https://github.com/user-attachments/assets/4719859c-a21c-4bfd-a246-9b3b20c4ddb3"> |

# Prerequisites

| Tool | Version |
|:--|:--|
| Android Studio | Ladybug or later (with [Kotlin Multiplatform plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform)) |
| Xcode | 15+ (for iOS builds) |
| JDK | 11+ |

# Getting Started

[Create your repository from this template](https://github.com/new?template_name=Compose-Multiplatform-Starter-Template&template_owner=s-iwasaki-b)

### 1. Rename Project Name

The default project name is `StarterProject`.
Execute the Gradle task `ChangeProjectName` from Run Configurations, input the new project name in configuration settings, and click Run.

<img width="400" alt="スクリーンショット 2024-10-14 0 22 04" src="https://github.com/user-attachments/assets/b38f9b1f-5813-489d-8f44-b1cf3f1b9e89">

Or run from the command line:

```sh
./gradlew changeProjectName -PnewProjectName="YourProjectName"
```

### 2. Rename Package Name

The default package name is `org.starter.project`.
Execute `ChangePackageName` in the same way as step 1.

<img width="400" alt="スクリーンショット 2024-10-14 0 08 02" src="https://github.com/user-attachments/assets/615a127b-5041-426a-a513-8ae69f9d1376">

Or run from the command line:

```sh
./gradlew changePackageName -PnewPackageName="com.example.yourapp"
```

> **Note:** Be sure to finish renaming in the template's initial state before starting development.

### 3. Gradle Sync

Finally, run Gradle sync and restart Android Studio before building.

# Build & Run

### Android

Open the project in Android Studio and run the `androidApp` configuration on an emulator or device.

### iOS

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select your target device or simulator
3. Build and run

# Project Structure

```
├── androidApp/           # Android entry point (MainActivity, Application)
├── composeApp/           # Shared Kotlin Multiplatform code
│   ├── app/              # App entry point, navigation, DI setup
│   ├── base/             # Shared data models, error types, extensions
│   ├── core/             # API client (platform-specific), preferences config
│   ├── data/
│   │   ├── repository/   # Repository interfaces
│   │   └── zenn/         # Data source implementation (API, converters, preferences)
│   ├── domain/
│   │   ├── service/      # Service interfaces, ResultHandler
│   │   └── zenn/         # Service implementation (business logic)
│   ├── feature/
│   │   ├── home/         # Home screen (article list, search, pagination)
│   │   └── user/         # User profile screen
│   └── ui/               # Design system (theme, scaffold), shared components, routing
├── build-logic/          # Gradle convention plugins
├── iosApp/               # iOS Xcode project
└── gradle/               # Version catalog (libs.versions.toml)
```

# Architecture

![Compose Multiplatform Starter Repository Architecture](https://github.com/user-attachments/assets/90445e4b-ceda-47d3-a21b-b2461c7e3eab)

### Layer Overview

| Layer | Module | Responsibility |
|:--|:--|:--|
| **UI** | `feature/*`, `ui/` | Composable screens, design system, shared UI components |
| **Presentation** | `feature/*` | ViewModels managing screen state via `StateFlow` |
| **Domain** | `domain/*` | Business logic wrapped in `Result<T>` via `ResultHandler` |
| **Data** | `data/*` | Repository implementations, API calls, data converters |
| **Core** | `core/` | Platform-specific HTTP client, preferences configuration |
| **Base** | `base/` | Shared data models and error definitions |

### Data Flow

```
Screen (UI) ──Event──▶ ViewModel ──▶ Service ──▶ Repository ──▶ DataSource (API / Preferences)
                            │                         │
                      StateFlow<State>           Result<T>
                            │                         │
Screen (UI) ◀──collect──── ViewModel ◀──── Service ◀──┘
```

### Key Patterns

- **MVVM + Event-driven UI** — Each feature screen consists of 4 files: `Screen`, `ViewModel`, `State`, and `Event`
- **Repository pattern** — Data access abstracted via interfaces, with implementations injected by Koin
- **`Result<T>` error handling** — `ResultHandler` wraps service operations, routing errors through `ThrowableHandler`
- **`expect` / `actual`** — Platform-specific code (HTTP client, preferences) implemented per platform

### Feature Module Pattern

Each screen follows a consistent 4-file structure:

| File | Role |
|:--|:--|
| `*Screen.kt` | Composable UI — collects state, dispatches events |
| `*ScreenViewModel.kt` | ViewModel — connects service layer to UI state |
| `*ScreenState.kt` | Immutable state data class (`@Immutable`) |
| `*ScreenEvent.kt` | Sealed event interface + event handler object |

# How to Add a New Feature

Follow these steps to add a new screen or feature module:

### 1. Create a feature module

Add a new module under `composeApp/feature/` (e.g., `composeApp/feature/settings/`).

Register it in `settings.gradle.kts`:

```kotlin
include(":composeApp:feature:settings")
```

Apply the `kmp-compose-library` convention plugin in its `build.gradle.kts`:

```kotlin
plugins {
    id("kmp-compose-library")
}
```

### 2. Create Screen, ViewModel, State, Event

Follow the pattern in `composeApp/feature/home/` as a reference:

- `SettingsScreen.kt` — Composable UI
- `SettingsScreenViewModel.kt` — ViewModel with `StateFlow`
- `SettingsScreenState.kt` — Immutable state data class
- `SettingsScreenEvent.kt` — Sealed event interface + handler

### 3. Add data and domain layers (if needed)

- Define a repository interface in `composeApp/data/repository/`
- Create implementation under `composeApp/data/yourfeature/` with API, converters, and repository
- Create a service interface in `composeApp/domain/service/` and implementation in `composeApp/domain/yourfeature/`
- Register modules in `settings.gradle.kts`

### 4. Add a route

Add a new route to `AppRoute` in `composeApp/ui/`:

```kotlin
@Serializable
data class Settings(val id: String? = null) : AppRoute()
```

### 5. Register navigation

Add a `composable` entry in `AppNavHost` (`composeApp/app/`):

```kotlin
composable<AppRoute.Settings> { backStackEntry ->
    val route = backStackEntry.toRoute<AppRoute.Settings>()
    val viewModel = koinViewModel<SettingsScreenViewModel>()
    SettingsScreen(viewModel = viewModel, appRouter = appRouter)
}
```

### 6. Register DI

Add bindings to `Koin.kt` (`composeApp/app/`):

```kotlin
// In the appropriate module block:
single<SettingsRepository> { SettingsRepositoryImpl(get()) }
single<SettingsService> { SettingsServiceImpl(get(), get()) }
viewModelOf(::SettingsScreenViewModel)
```

# Testing

### Running Tests

Use the pre-configured `AllTest` run configuration in Android Studio, or run from the command line:

```sh
./gradlew testDebugUnitTest
```

### Writing Tests

Tests use `kotlin-test`, `kotlinx-coroutines-test`, and [Mokkery](https://github.com/lupuuss/Mokkery) for mocking.

See existing tests as references:

- `composeApp/domain/zenn/` — `ZennServiceTest.kt` (service layer testing)
- `composeApp/data/zenn/` — `ZennRepositoryTest.kt` (repository testing), `ArticlesConverterTest.kt` (converter testing)

Example test pattern:

```kotlin
class YourServiceTest {
    private val mockRepository = mock<YourRepository>(MockMode.autofill)
    private val testDispatcher = StandardTestDispatcher()
    private val testResultHandler = ResultHandler(testDispatcher)
    private val subject = YourServiceImpl(testResultHandler, mockRepository)

    @Test
    fun yourTest() = runTest(testDispatcher) {
        // arrange
        everySuspend { mockRepository.fetchData(any()) } returns expectedData
        // act
        val actual = subject.fetchData("param")
        // assert
        assertEquals(Result.success(expectedData), actual)
    }
}
```

# Build System

### Convention Plugins

Two Gradle convention plugins are available in `build-logic/`:

| Plugin | Usage |
|:--|:--|
| `kmp-library` | Base KMP library — Android + iOS targets, auto-derived namespace |
| `kmp-compose-library` | Extends `kmp-library` with Compose plugin and resource generation |

Use `kmp-compose-library` for feature/UI modules and `kmp-library` for data/domain modules.

### Version Catalog

All dependency versions are centralized in `gradle/libs.versions.toml` with pre-defined bundles (`base`, `core`, `data`, `ui`, `test`) for consistent dependency sets across modules.

# 3rd Party Dependencies

| Name | Version | Description |
|:--|:--|:--|
| [koin](https://github.com/InsertKoinIO/koin) | 4.1.1 | Dependency Injection |
| [multiplatform-settings](https://github.com/russhwolf/multiplatform-settings) | 1.3.0 | Key-Value data source such as [SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences) |
| [ktor](https://github.com/ktorio/ktor) | 3.4.0 | HTTP client |
| [Ktorfit](https://github.com/Foso/Ktorfit) | 2.7.2 | REST API interface such as [Retrofit](https://github.com/square/retrofit) |
| [coil](https://github.com/coil-kt/coil?tab=readme-ov-file#jetpack-compose) | 3.3.0 | Loading async image |
| [Mokkery](https://github.com/lupuuss/Mokkery) | 3.2.0 | Mocking such as [MockK](https://github.com/mockk/mockk?tab=readme-ov-file) |
| [Napier](https://github.com/AAkira/Napier) | 2.7.1 | Logging, it is implemented to output only in debug build in this repository |

# References

- [Detailed article (Japanese)](https://zenn.dev/dely_jp/articles/ce01725bde5ed4)
- [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)

# License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
