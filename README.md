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

This repository does not use a mocking library. Instead, it follows the [Android testing guide's recommendation](https://developer.android.com/training/testing/fundamentals/test-doubles) to use fakes: lightweight, in-memory implementations of an interface. Fakes for boundaries internal to a single module (e.g. `FakeZennApi`, `FakeZennPreferences`) live in that module's `commonTest`, while fakes shared across module tests (e.g. `FakeZennRepository`) live in the `composeApp:testing` module. Tests assert against return values and fake state rather than verifying calls.

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
The iOS app links the static Kotlin/Native framework `ComposeApp.framework` built from the `composeApp:app` module. Xcode builds and embeds it through the Run Script phase `./gradlew :composeApp:app:embedAndSignAppleFrameworkForXcode`, and Framework Search Paths point to `composeApp/app/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`.

Swift uses three Kotlin APIs from `composeApp:app`: `MainViewController()`, `DeepLinkHandler`, and `initNapier()` (exposed as `doInitNapier()` through ObjC interop).

To build the framework locally, run:
```
./gradlew :composeApp:app:linkDebugFrameworkIosSimulatorArm64
```


# Navigation
Navigation uses [`org.jetbrains.androidx.navigation:navigation-compose`](https://developer.android.com/jetpack/androidx/releases/navigation) 2.9.2 with type-safe routes, deep links via `navDeepLink` and `NavUri`, and screen-scoped ViewModels via `koinViewModel`.


# 3rd Party Dependencies

| Name | Version | Description |
|:--|:--|:--|
| [koin](https://github.com/InsertKoinIO/koin) | 4.2.2 | Dependency Injection |
| [multiplatform-settings](https://github.com/russhwolf/multiplatform-settings) | 1.3.0 | Key-Value data source such as [SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences) |
| [ktor](https://github.com/ktorio/ktor) | 3.6.0 | HTTP client |
| [Ktorfit](https://github.com/Foso/Ktorfit) | 2.7.5 | REST API interface such as [Retrofit](https://github.com/square/retrofit) |
| [coil](https://github.com/coil-kt/coil?tab=readme-ov-file#jetpack-compose) | 3.6.3 | Loading async image |
| [Napier](https://github.com/AAkira/Napier) | 2.7.1 | Logging, it is implemented to output only in debug build in this repository |


# Demo
This repository includes a simple [Zenn](https://zenn.dev/) viewer implemented as sample code.

| Pixel 3a - Android 12 | iPhone XS - iOS 18.0 |
|--|--|
| <video src="https://github.com/user-attachments/assets/e9bdc1c5-458a-4e9d-8af7-780f556cbd23"> | <video src="https://github.com/user-attachments/assets/4719859c-a21c-4bfd-a246-9b3b20c4ddb3"> |

# More Detail

cf. https://zenn.dev/dely_jp/articles/ce01725bde5ed4
