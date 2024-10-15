[![Kotlin](https://img.shields.io/badge/kotlin-2.0.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub](https://img.shields.io/github/license/s-iwasaki-b/Compose-Multiplatform-Starter-Template)](https://github.com/s-iwasaki-b/Compose-Multiplatform-Starter-Template/blob/main/LICENSE)

# Overview
This is starter template to develop multiplatform application by using [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/).

This already includes a basic module structure and dependencies, following the [Android Architecture Guide](https://developer.android.com/topic/architecture), so you can start development with Compose Multiplatform right away.

Currently, the targets are Android and iOS.


# Usage
[Create your repository from this template](https://github.com/new?template_name=Compose-Multiplatform-Starter-Template&template_owner=s-iwasaki-b)

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


# 3rd Party Dependencies

| Name | Version | Description |
|:--|:--|:--|
| [koin](https://github.com/InsertKoinIO/koin) | 4.0.0 | Dependency Injection |
| [multiplatform-settings](https://github.com/russhwolf/multiplatform-settings) | 1.2.0 | Key-Value data source such as [SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences) |
| [ktor](https://github.com/ktorio/ktor) | 2.3.12 | HTTP client |
| [Ktorfit](https://github.com/Foso/Ktorfit) | 2.1.0 | REST API interface such as [Retrofit](https://github.com/square/retrofit) |
| [multiplatform-paging](https://github.com/cashapp/multiplatform-paging) | 3.3.0-alpha02-0.5.1 | Paging |
| [coil](https://github.com/coil-kt/coil?tab=readme-ov-file#jetpack-compose) | 3.0.0-rc01 | Loading async image |
| [Mokkery](https://github.com/lupuuss/Mokkery) | 2.4.0 | Mocking such as [MockK](https://github.com/mockk/mockk?tab=readme-ov-file) |
| [Napier](https://github.com/AAkira/Napier) | 2.7.1 | Logging, it is implemented to output only in debug build in this repository |


# Demo
This repository includes a simple [Zenn](https://zenn.dev/) viewer implemented as sample code.

| Pixel 3a - Android 12 | iPhone XS - iOS 18.0 |
|--|--|
| <video src="https://github.com/user-attachments/assets/e9bdc1c5-458a-4e9d-8af7-780f556cbd23"> | <video src="https://github.com/user-attachments/assets/4719859c-a21c-4bfd-a246-9b3b20c4ddb3"> |
