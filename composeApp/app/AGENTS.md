# composeApp/app

共有アプリ層。Koin の DI 起動、Compose のエントリ Composable、Navigation（`AppNavHost`/`AppRouter` 実装）、DeepLink、プラットフォーム別バインドを集約する。

本ファイルはこのモジュールで作業する実装エージェントの入口である。ルートの [AGENTS.md](../../AGENTS.md)（絶対ルール・実行体制）を前提とし、ツールが自動で読み込まない場合は先に読む。実装エージェントは本モジュール外のファイルを編集せず、必要な他モジュールの変更は「他モジュールとの接点」に沿ってオーケストレーターへ報告する。読み順は本ファイル → 「参照」節の docs。

## 責務

- 置く: `startKoin`（層別 `module { }` 全体、`Koin.kt`）、`expect val platformModule` とその `actual`（`Koin.android.kt`/`Koin.ios.kt`）、`Main()`（`MainApp.kt`）、`AppNavHost`/`AppRouterImpl`/`rememberAppRouter`、`DeepLinkConfig`/`DeepLinkHandler`、iOS エントリ `MainViewController`、iOS 用 Napier 初期化プロキシ（`log/NapierProxy.kt`）。
- 置かない: Android の `Application`/`MainActivity`（別モジュール `androidApp` に置く）、画面固有ロジック（→ `feature/<name>`）、ビジネスロジック（→ `domain/<name>`）、共通 UI 部品（→ `ui`）。

## 依存

`build.gradle.kts` と一致（`kmp-compose-library` + `kotlin.serialization`。iOS フレームワーク `ComposeApp` を静的リンクで生成）:

- `commonMain`: `core`、`data.repository`、`domain.service`、`ui`（常設）に加え、存在する `data:<source>` / `domain:<name>` / `feature:<name>` をすべて `implementation` で列挙する。現状の一覧は `build.gradle.kts` を見る
- `androidMain`: `implementation(libs.koin.android)`

`composeApp` 配下で唯一 `expect`/`actual`（`platformModule`）を持てるモジュール。namespace と異なる関心事別パッケージ分割（`app`/`navigation`/`di`/`log`）が許される唯一の例外。

## 構成

```text
src/commonMain/kotlin/org/starter/project/
  app/MainApp.kt               # Main()：SystemTheme + AppNavHost + DeepLink リスナー配線
  di/Koin.kt                   # startKoin、coreModule/dataSourceModule/repositoryModule/serviceModule/appModule、expect platformModule
  navigation/
    AppNavHost.kt               # composable<AppRoute.Xxx> 登録
    AppRouter.kt                 # internal AppRouterImpl、rememberAppRouter
    DeepLinkConfig.kt            # SCHEME 定数
    DeepLinkHandler.kt           # 未起動時ディープリンクのキャッシュ&リプレイ
src/androidMain/kotlin/org/starter/project/di/Koin.android.kt    # actual platformModule
src/iosMain/kotlin/org/starter/project/
  app/MainViewController.kt     # iOS エントリ
  di/Koin.ios.kt                # actual platformModule
  log/NapierProxy.kt            # iOS 用 Napier 初期化
```

## 実装パターン

DI 登録テンプレ（既存の5層のいずれかに1行追記する）:

```kotlin
val repositoryModule = module { single<XxxRepository> { XxxRepositoryImpl(get(), get()) } }
val serviceModule = module { single<XxxService> { XxxServiceImpl(get(), get()) } }
val appModule = module { viewModelOf(::XxxScreenViewModel) }
```

`AppNavHost` への画面登録（4 ステップ）:

```kotlin
composable<AppRoute.Xxx>(
    deepLinks = listOf(navDeepLink<AppRoute.Xxx>(basePath = "${DeepLinkConfig.SCHEME}://xxx"))
) { backStackEntry ->
    val route = backStackEntry.toRoute<AppRoute.Xxx>()
    val viewModel = koinViewModel<XxxScreenViewModel>()
    XxxScreen(viewModel = viewModel, appRouter = appRouter, navArgs = route.navArgs)
}
```

複数ホストの API を追加する場合は `named("<host>")` で `coreModule`/`dataSourceModule` に登録する（現状未実装。詳細は [design-guide.md](../../docs/design-guide.md) §4）。

## テスト

本モジュールに `commonTest` は無い。DI 配線・Navigation・DeepLink は Converter/Repository 実装/Service 実装/ViewModel のいずれにも該当せず必須テスト対象外。純粋なロジックを追加した場合のみ `commonTest` にテストを置く。実行: `./gradlew :composeApp:app:testAndroidHostTest`。

## 他モジュールとの接点

他モジュールの追加・変更が本モジュールの変更を要求する一覧。実装エージェントは自モジュール外を編集せず、対応する変更を本節の項目としてオーケストレーターへ報告する。

- 新しい `Repository` 追加 → `repositoryModule` にバインド追加
- 新しい `Service` 追加 → `serviceModule` にバインド追加
- 新しい `ViewModel`（画面）追加 → `appModule` に `viewModelOf` 追加 + `AppNavHost` に `composable` 登録
- 新しい `data:<source>`/`domain:<name>`/`feature:<name>` モジュール追加 → `build.gradle.kts` の `commonMain.dependencies` に `implementation(projects.composeApp.<layer>.<name>)` 追加
- `ui` に新しい `AppRoute` 追加 → `AppNavHost` に画面登録
- `ui` に新しいプラットフォーム機能 interface（decisions.md D-24）追加 → `platformModule`（`Koin.android.kt`/`Koin.ios.kt`）に実装追加
- DeepLink スキーム変更 → `BuildUtils.kt`/`DeepLinkConfig.kt`/`iosApp/Info.plist` の3箇所を同時更新

## 完了条件

- 追加した Repository/Service/ViewModel が対応する層別 `module { }` に登録されているか確認した
- 新しい画面が `AppNavHost` に登録されているか確認した
- `platformModule` の Android/iOS 両方の `actual` が揃っているか確認した
- `commonTest` を追加した場合、`./gradlew :composeApp:app:testAndroidHostTest` を実行した
- [../../docs/coding-guide.md](../../docs/coding-guide.md) §8 の既知の逸脱を新規コードに複製していない。

## 参照

- [../../docs/design-guide.md](../../docs/design-guide.md) §2 モジュール構成、§4 DI（Koin）、§5 ナビゲーション
- [../../docs/coding-guide.md](../../docs/coding-guide.md) §1 共通スタイル、§6 Git
- docs/decisions.md D-08、D-09、D-10、D-16、D-24、D-25
