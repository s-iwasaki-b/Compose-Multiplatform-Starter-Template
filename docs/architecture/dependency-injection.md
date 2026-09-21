# 依存性注入（Koin）

> 対象: Koin の層別モジュール構成、登録パターン、`platformModule`（expect/actual）、新規バインド追加手順
> 関連: [overview.md](overview.md), [module-guide.md](module-guide.md), [../guides/data-layer.md](../guides/data-layer.md), [../guides/domain-layer.md](../guides/domain-layer.md), [../guides/ui-layer.md](../guides/ui-layer.md), [../guides/coding-style.md](../guides/coding-style.md), [../decisions/0008-di-centralized-in-app.md](../decisions/0008-di-centralized-in-app.md), [../decisions/0010-feature-depends-on-service-only.md](../decisions/0010-feature-depends-on-service-only.md), [../decisions/0024-platform-capabilities-via-app-module.md](../decisions/0024-platform-capabilities-via-app-module.md), [../decisions/0025-multi-host-api-clients.md](../decisions/0025-multi-host-api-clients.md)
> 最終確認コミット: ac56102

## 要点

- 全ての DI 定義は `composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt` の `startKoin` 関数1箇所に集約する。機能・データ・ドメインモジュール内に DI 定義ファイルは作らない。
- 層ごとに `val xxxModule = module { }` を分け（`coreModule` / `dataSourceModule` / `repositoryModule` / `serviceModule` / `appModule`）、最後に `modules(...)` へまとめて渡す。
- バインドは `single<Interface> { Impl(get(), get()) }` が基本形。`factory` は現状未使用。
- ViewModel は `viewModelOf(::XxxScreenViewModel)` で `appModule` に登録し、取得は NavHost 側で `koinViewModel<XxxScreenViewModel>()` を使う。
- プラットフォーム差分（`Settings.Factory` の実体）は `expect val platformModule` + `actual` で分離する。
- ViewModel のコンストラクタには `domain:service` の interface のみを注入する。Repository の直接注入は必須で禁止。
- DI で束ねられる実装クラス（`XxxRepositoryImpl` 等）は `public` のまま維持する。`internal` にすると `app` の `Koin.kt` から参照できずコンパイルが壊れる。
- Koin の起動は Android が `App.onCreate()`、iOS が `MainViewController()` の `configure` ラムダ。

## ルール

- **DI-1（必須）**: DI 定義は `composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt` に集約する。`data:<source>`、`domain:<name>`、`feature:<name>` 等の各モジュール内に `XxxModule.kt` のような DI 定義ファイルを作らない。
- **DI-2（必須）**: 層ごとに `val xxxModule = module { }` を分ける。既存の5層（`coreModule` / `dataSourceModule` / `repositoryModule` / `serviceModule` / `appModule`）+ `platformModule` で表現できる場合は新しい層変数を追加せず、該当する既存の `module { }` に追記する。
- **DI-3（必須）**: interface を持つ実装をバインドする際は `single<Interface> { Impl(get(), get()) }` のように型引数で interface を明示する。`single { Impl(...) }` のように型引数を省略しない（呼び出し側の `get<Interface>()` 解決に失敗する）。
- **DI-4（推奨）**: コンストラクタの全引数をそのまま DI コンテナから解決できる場合は `singleOf(::Impl)` を使う。引数の一部を `get<X>()` のように明示指定する必要がある場合、または依存関係を式で組み立てる必要がある場合（例: `get<ApiClient>().ktorfit.createZennApi()`。サンプルでの根拠。削除後は存在しない）は `single<Interface> { ... }` のラムダ形式を使う。
- **DI-5（必須）**: ViewModel は `appModule` に `viewModelOf(::XxxScreenViewModel)` で登録する。取得は NavHost 側の `koinViewModel<XxxScreenViewModel>()` で行い、Screen 関数自体は DI を意識せずコンストラクタ引数として ViewModel を受け取る。
- **DI-6（必須）**: `factory` は使わない。ただし画面ごとに独立した状態を持つ非 ViewModel オブジェクト（例: 画面スコープの一時的なコントローラ）を登録する場合に限り `factory` を使ってよい（新規決定。現状この用途の実例は無い）。
- **DI-7（必須）**: プラットフォーム差分は `expect val platformModule: Module` + `actual`（`Koin.android.kt` / `Koin.ios.kt`）に閉じ込める。Android の `Context` が必要な依存（`SharedPreferencesSettings.Factory` 等）は `androidContext()` をここで使う。
- **DI-8（必須）**: DI で解決される実装クラス（`XxxRepositoryImpl`、`XxxServiceImpl` 等）は `public` のままにする。`internal` にすると DI 集約先の `composeApp:app` から型解決できずコンパイルエラーになる。
- **DI-9（必須）**: ViewModel のコンストラクタには `domain:service` の interface のみを注入する。`data:repository`/`domain:<name>`（実装）や `PreferencesConfig` のような下位コンポーネントを直接注入しない。

## 判断基準

### ViewModel に何を注入するか（C-9-1 / D-G4）

- ViewModel は常に `domain:service` の Service interface のみをコンストラクタ注入する。Repository の直接注入は行わない。
- 根拠: `feature:<name>` の `build.gradle.kts` は `domain:service` にのみ `implementation` 依存し、`data:repository`/`domain:<name>`（実装）への依存を持たない（[overview.md](overview.md) の ARCH-3）。Repository を直接注入しようとしても、モジュール境界上 import できない。
- 単純な CRUD であっても例外を設けない。Service 層を経由させることでテスト容易性とレイヤー境界を一貫させる。

### `singleOf` と `single { }` ラムダの使い分け

| 状況 | 判断 |
|---|---|
| コンストラクタの全引数を DI コンテナがそのまま解決できる（例: `PreferencesConfig(settingsFactory: Settings.Factory)`） | `singleOf(::PreferencesConfig)` |
| コンストラクタ引数の一部に `get<X>()` の型指定や式（メソッドチェーン）が必要（例: `get<ApiClient>().ktorfit.createZennApi()`。サンプルでの根拠。削除後は存在しない） | `single<Interface> { ... }` のラムダ形式 |
| interface へのバインドが必要 | どちらの形式でも良いが、`single<Interface> { Impl(get(), get()) }` のように型引数を明示する（DI-3） |

### 新しい DI レイヤーを追加すべきか

既存の5層（`coreModule`/`dataSourceModule`/`repositoryModule`/`serviceModule`/`appModule`）のいずれかに当てはまる登録は、その既存 `module { }` に追記する。全く新しい横断的関心事（例: analytics、feature flag）を導入する場合のみ、新しい `val xxxModule = module { }` を追加し `modules(...)` の引数リストに加えてよい。

### 可視性（B-G12、DI 観点のみ）

DI で束ねられる実装クラスは `public` を維持する（DI-8）。これは `composeApp:app` の `Koin.kt` が各実装モジュールをクラスパス上で参照し、`single<Interface> { Impl(...) }` のようにコンストラクタを直接呼び出す必要があるため。`internal` 修飾子の一般的な使い分け（モジュール内部のヘルパー関数、`@VisibleForTesting internal` 等）は本書の対象外とし、[../guides/coding-style.md](../guides/coding-style.md) に委ねる。

## 実装パターン

### 層別モジュール構成（現状の5層 + `platformModule`）

```kotlin
// composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt
expect val platformModule: org.koin.core.module.Module

fun startKoin(platformDeclaration: KoinAppDeclaration? = null) {
    org.koin.core.context.startKoin {
        platformDeclaration?.invoke(this)

        val coreModule = module {
            single<ApiClient> { ApiClientImpl() }
            singleOf(::PreferencesConfig)
        }

        val dataSourceModule = module {
            single<XxxApi> { get<ApiClient>().ktorfit.createXxxApi() }
            single<XxxPreferences> { XxxPreferencesImpl(get<PreferencesConfig>().xxxPreferences) }
        }

        val repositoryModule = module {
            single<XxxRepository> { XxxRepositoryImpl(get(), get()) }
        }

        val serviceModule = module {
            single { ResultHandler() }
            single<XxxService> { XxxServiceImpl(get(), get()) }
        }

        val appModule = module {
            viewModelOf(::XxxScreenViewModel)
        }

        modules(
            platformModule,
            coreModule,
            dataSourceModule,
            repositoryModule,
            serviceModule,
            appModule
        )
    }
}
```

### 新規バインド追加手順

**Repository を追加する場合**（`repositoryModule` に1行追加）:

```kotlin
val repositoryModule = module {
    single<XxxRepository> { XxxRepositoryImpl(get(), get()) }
}
```

**Service を追加する場合**（`serviceModule` に1行追加。`ResultHandler` は共有の単一インスタンス）:

```kotlin
val serviceModule = module {
    single { ResultHandler() } // 既存。追加不要
    single<XxxService> { XxxServiceImpl(get(), get()) }
}
```

**ViewModel を追加する場合**（`appModule` に1行追加）:

```kotlin
val appModule = module {
    viewModelOf(::XxxScreenViewModel)
}
```

取得側（`AppNavHost.kt`）:

```kotlin
composable<AppRoute.Xxx>(
    deepLinks = listOf(navDeepLink<AppRoute.Xxx>(basePath = "${DeepLinkConfig.SCHEME}://xxx"))
) { backStackEntry ->
    val route = backStackEntry.toRoute<AppRoute.Xxx>()
    val viewModel = koinViewModel<XxxScreenViewModel>()
    XxxScreen(viewModel = viewModel, appRouter = appRouter, navArgs = route.navArgs)
}
```

### `platformModule` の expect/actual

```kotlin
// composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt
expect val platformModule: org.koin.core.module.Module
```

```kotlin
// composeApp/app/src/androidMain/kotlin/org/starter/project/di/Koin.android.kt
actual val platformModule = module {
    single<Settings.Factory> { SharedPreferencesSettings.Factory(androidContext()) }
}
```

```kotlin
// composeApp/app/src/iosMain/kotlin/org/starter/project/di/Koin.ios.kt
actual val platformModule = module {
    single<Settings.Factory> { NSUserDefaultsSettings.Factory() }
}
```

### プラットフォーム機能 interface のバインド（[ADR-0024](../decisions/0024-platform-capabilities-via-app-module.md)。現状未実装の差分。実例なし・新規決定）

共有シート・外部ブラウザ起動・クリップボードなど UI から呼ぶプラットフォーム固有機能も `Settings.Factory` と同じ形で `platformModule` の expect/actual に閉じ込める。interface は `ui` に置き、実装は `composeApp/app` の `androidMain`/`iosMain` に置いて `platformModule` でバインドする。feature は interface をコンストラクタ引数または `EventHandler` の引数として注入する。

```kotlin
// composeApp/app/src/androidMain/kotlin/org/starter/project/di/Koin.android.kt（差分）
actual val platformModule = module {
    single<Settings.Factory> { SharedPreferencesSettings.Factory(androidContext()) }
    single<ShareHandler> { AndroidShareHandler(androidContext()) } // ADR-0024。ShareHandler は composeApp/ui に置く interface（例）
}
```

```kotlin
// composeApp/app/src/iosMain/kotlin/org/starter/project/di/Koin.ios.kt（差分）
actual val platformModule = module {
    single<Settings.Factory> { NSUserDefaultsSettings.Factory() }
    single<ShareHandler> { IosShareHandler() } // ADR-0024（例）
}
```

### 別ホストの `ApiClient` を `named()` で登録する（[ADR-0025](../decisions/0025-multi-host-api-clients.md)。現状未実装の差分。実例なし・新規決定）

複数の外部ホストを呼ぶ必要が生じた場合、`ApiClientImpl` を `baseUrl` 引数化し、ホストごとに Koin `named()` で `coreModule` に複数登録する。

```kotlin
val coreModule = module {
    single<ApiClient>(named("zenn")) { ApiClientImpl(ApiConfig.API_BASE_URL) }
    single<ApiClient>(named("github")) { ApiClientImpl(GitHubApiConfig.API_BASE_URL) }
}

val dataSourceModule = module {
    single<XxxApi> { get<ApiClient>(named("zenn")).ktorfit.createXxxApi() }
    single<GitHubApi> { get<ApiClient>(named("github")).ktorfit.createGitHubApi() }
}
```

`named("zenn")`/`named("github")` はホストごとに一意な識別子であればよい。`baseUrl` の値は `core` に置く `internal object <Host>ApiConfig`（例: `GitHubApiConfig`）の定数として持つ。

### Koin 起動場所

```kotlin
// androidApp/src/main/kotlin/org/starter/project/android/App.kt
class App : Application() {
    init {
        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog()) // Napier のログ初期化（DI とは別関心事。デバッグビルドのみ）
        }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
        }
    }
}
```

```kotlin
// composeApp/app/src/iosMain/kotlin/org/starter/project/app/MainViewController.kt
fun MainViewController() = ComposeUIViewController(
    configure = { startKoin() }
) { Main() }
```

## アンチパターン

- **`data:<source>`/`domain:<name>` モジュール内に `XxxModule.kt` を作り、独自に `module { }` を定義する** → NG。DI 定義は `app` の `Koin.kt` に一元集約する（DI-1）。分散すると `dataSourceModule`/`repositoryModule`/`serviceModule` が空になり、集約方式との一貫性が崩れる。
- **`single { XxxRepositoryImpl(get(), get()) }` のように型引数を書かない** → NG。`get<XxxRepository>()` 側の型解決に失敗する（DI-3）。
- **ViewModel のコンストラクタに `XxxRepository` や `PreferencesConfig` を直接注入する** → NG。`domain:service` の Service のみを注入する（DI-9）。モジュール境界上そもそも import できないはず。
- **`XxxRepositoryImpl`/`XxxServiceImpl` を `internal class` にする** → NG。`app` の `Koin.kt` から参照できずコンパイルエラーになる（DI-8）。
- **`factory` を汎用的に使う** → NG。現状のバインドは全て `single`。画面ごとに独立した状態を持つ非 ViewModel オブジェクト以外には使わない（DI-6）。

## 参考実装（サンプル。削除後は存在しない）

- `composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt` — 層別モジュール構成の全文。
- `composeApp/app/src/androidMain/kotlin/org/starter/project/di/Koin.android.kt` — `platformModule` の Android 実装。
- `composeApp/app/src/iosMain/kotlin/org/starter/project/di/Koin.ios.kt` — `platformModule` の iOS 実装。
- `composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/AppNavHost.kt` — `koinViewModel<XxxViewModel>()` による ViewModel 取得の実例。
- `androidApp/src/main/kotlin/org/starter/project/android/App.kt` — Android での Koin 起動場所。
- `composeApp/app/src/iosMain/kotlin/org/starter/project/app/MainViewController.kt` — iOS での Koin 起動場所。

## チェックリスト

- [ ] 新しい DI 定義を `composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt` 以外の場所に置いていない。
- [ ] 既存の5層（`coreModule`/`dataSourceModule`/`repositoryModule`/`serviceModule`/`appModule`）のいずれかに登録した。新しい層を追加した場合は横断的関心事であることを確認した。
- [ ] `single<Interface> { Impl(...) }` のように型引数を明示した。
- [ ] ViewModel には `domain:service` の interface のみを注入した。
- [ ] 新規 ViewModel を `viewModelOf(::XxxScreenViewModel)` で `appModule` に登録した。
- [ ] DI 対象の実装クラスが `public` のままである。
- [ ] プラットフォーム固有の依存は `platformModule` の expect/actual に閉じ込めた。
