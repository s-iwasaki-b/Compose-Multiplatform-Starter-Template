[![Kotlin](https://img.shields.io/badge/kotlin-2.4.20-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![GitHub](https://img.shields.io/github/license/s-iwasaki-b/Compose-Multiplatform-Starter-Template)](https://github.com/s-iwasaki-b/Compose-Multiplatform-Starter-Template/blob/main/LICENSE)

# Overview
This is starter template to develop multiplatform application by using [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/).

This already includes a basic module structure and dependencies, following the [Android Architecture Guide](https://developer.android.com/topic/architecture), so you can start development with Compose Multiplatform right away.

Currently, the targets are Android and iOS.


# Usage
[Create your repository from this template](https://github.com/new?template_name=Compose-Multiplatform-Starter-Template&template_owner=s-iwasaki-b)

### Build Requirements

The project uses Kotlin 2.4.20, Compose Multiplatform 1.12.0, Android Gradle Plugin 9.4.1, and Gradle 9.7.1.
Use JDK 17 or newer and install Android SDK Platform 37.0. The Gradle wrapper supplies the required Gradle version.
An Xcode installation is required to build the iOS app.

Run the shared unit tests on the Android host with `./gradlew testAndroidHostTest` or the `AllTest` run configuration.

### How to Rename
Gradle task is available for renaming projects and packages.  
Please follow the steps below to use it. 

Be sure to finish this setup in the template's initial state.  
Its behavior during development has not been confirmed.

#### 1. Rename Project Name
The default project name is `StarterProject`.  
Execute gradle task named `ChangeProjectName` from Run Configurations, and input new project name in configuration settings, finally click Run.

<img width="400" alt="スクリーンショット 2024-10-14 0 22 04" src="https://github.com/user-attachments/assets/b38f9b1f-5813-489d-8f44-b1cf3f1b9e89">


#### 2. Rename Package Name
The default package name is `org.starter.project`.  
Execute `ChangePackageName` in the same way as step 1.

<img width="400" alt="スクリーンショット 2024-10-14 0 08 02" src="https://github.com/user-attachments/assets/615a127b-5041-426a-a513-8ae69f9d1376">

#### 3. Gradle Sync
Finally run gradle sync and restart Android Studio before building.

# Architecture
![Compose Multiplatform Starter Repository Architecture](https://github.com/user-attachments/assets/90445e4b-ceda-47d3-a21b-b2461c7e3eab)


# iOS Integration

### Direct Integration
The iOS app consumes a static Kotlin/Native framework, `ComposeApp.framework`, generated from the `composeApp:app` module (Direct Integration). Xcode embeds it via the Run Script phase `./gradlew :composeApp:app:embedAndSignAppleFrameworkForXcode`, with Framework Search Paths set to `composeApp/app/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`.

Swift consumes three Kotlin APIs exposed by `composeApp:app`: `MainViewController()`, `DeepLinkHandler`, and `initNapier()` (renamed to `doInitNapier()` when called from Swift via ObjC interop).

To check the framework output locally, run:
```
./gradlew :composeApp:app:linkDebugFrameworkIosSimulatorArm64
```

### Swift Export (Evaluation, 2026-09-20)
Since Kotlin 2.4.0, [Swift Export](https://kotlinlang.org/docs/native-swift-export.html) is Alpha, and the official docs state that breaking changes are expected. The Direct Integration approach described above is this template's current, unaffected default.

Verification results:
- Adding `swiftExport { moduleName = "ComposeApp"; flattenPackage = "org.starter.project" }` to `composeApp:app` generated Swift APIs for `MainViewController()` (returning `UIViewController`), `DeepLinkHandler` (`shared` / `listener` / `onNewUri(uri:)`), and `initNapier()` (no rename). `@Composable fun Main()` was excluded without warning.
- However, `embedSwiftExportForXcode` fails at Compose Multiplatform 1.12.0's Compose Resources sync task (`syncSwiftExportBinaryComposeResourcesForIos`, whose `outputDir` is unset), so the build never reaches `.swiftmodule` / `.a`. Excluding resource sync breaks string resources at runtime, so switching to Swift Export in production is not currently possible.

Decision: **Postponed**. Reconsider when either (a) Swift Export reaches Beta or later, or (b) Compose Multiplatform fixes resource sync for Swift Export builds. Migration steps once adopted:
- Add `swiftExport {}` to the `composeApp:app` Gradle build.
- Change the Xcode Run Script phase to `embedSwiftExportForXcode`.
- Change the Swift-side call from `NapierProxyKt.doInitNapier()` to `initNapier()`.


# Navigation
This template uses [`org.jetbrains.androidx.navigation:navigation-compose`](https://developer.android.com/jetpack/androidx/releases/navigation) 2.9.2, the latest stable KMP release, with type-safe routes, deep links via `navDeepLink` + `NavUri`, and screen-scoped ViewModels via `koinViewModel`.

Evaluation summary (2026-09-20):

| Option | Notes |
|:--|:--|
| Navigation 2 (adopted) | Google's original library is in maintenance mode. The next KMP release, 2.10.0, is beta: `handleDeepLink` will ignore unrecognized deep links (breaking change), a predictive-back pop-transition parameter is added, and minSdk rises to 24. Compose Multiplatform 1.12 bundles 2.10.0-alpha02. |
| NavigationEvent 1.1.0 | `PredictiveBackHandler` is deprecated in favor of `NavigationBackHandler`. This template does not use `BackHandler`. |
| [Navigation 3](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html) (KMP) | 1.1.1 is stable and production-ready. The app owns its own back stack and renders it with `NavDisplay` + `entryProvider`. Built-in deep link support only lands in 1.2.0-alpha03 and later. Koin support exists via `koin-compose-navigation3`, but ViewModel clearing has an open issue ([InsertKoinIO/koin#2235](https://github.com/InsertKoinIO/koin/issues/2235)). The official guidance describes migrating from Navigation 2 as a rewrite. |

Decision: **Stay on Navigation 2 (2.9.2)**. Reconsider once Navigation 3's built-in deep link support (the 1.2.0 line) reaches stable.


# 3rd Party Dependencies

| Name | Version | Description |
|:--|:--|:--|
| [koin](https://github.com/InsertKoinIO/koin) | 4.2.2 | Dependency Injection |
| [multiplatform-settings](https://github.com/russhwolf/multiplatform-settings) | 1.3.0 | Key-Value data source such as [SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences) |
| [ktor](https://github.com/ktorio/ktor) | 3.6.0 | HTTP client |
| [Ktorfit](https://github.com/Foso/Ktorfit) | 2.7.5 | REST API interface such as [Retrofit](https://github.com/square/retrofit) |
| [coil](https://github.com/coil-kt/coil?tab=readme-ov-file#jetpack-compose) | 3.6.3 | Loading async image |
| [Mokkery](https://github.com/lupuuss/Mokkery) | 3.5.0 | Mocking such as [MockK](https://github.com/mockk/mockk?tab=readme-ov-file) |
| [Napier](https://github.com/AAkira/Napier) | 2.7.1 | Logging, it is implemented to output only in debug build in this repository |


# Demo
This repository includes a simple [Zenn](https://zenn.dev/) viewer implemented as sample code.

| Pixel 3a - Android 12 | iPhone XS - iOS 18.0 |
|--|--|
| <video src="https://github.com/user-attachments/assets/e9bdc1c5-458a-4e9d-8af7-780f556cbd23"> | <video src="https://github.com/user-attachments/assets/4719859c-a21c-4bfd-a246-9b3b20c4ddb3"> |

# More Detail

cf. https://zenn.dev/dely_jp/articles/ce01725bde5ed4
