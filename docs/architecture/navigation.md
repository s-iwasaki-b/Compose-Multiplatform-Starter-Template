# ナビゲーション

> 対象: 画面遷移の型安全ルート定義（`AppRoute`）、`AppRouter`、NavHost 登録、ディープリンクの実装規約
> 関連: [ui-layer.md](../guides/ui-layer.md), [module-guide.md](module-guide.md), [dependency-injection.md](dependency-injection.md), [0009](../decisions/0009-module-granularity.md), [0016](../decisions/0016-one-shot-events.md)
> 最終確認コミット: ac56102

## 要点

- ナビゲーションは `org.jetbrains.androidx.navigation:navigation-compose`（バージョン 2.9.2）の型安全ルートを使う。文字列ルートは使わない。
- ルート定義 `AppRoute`（`sealed class` + ネストした `@Serializable data class`）と `AppRouter` interface は `composeApp:ui` に置く。実装 `AppRouterImpl` と `rememberAppRouter` は `composeApp:app` に `internal` で置く。feature/ui モジュールは `NavHostController` の実体を知らない。
- 新しい画面を NavHost に登録する手順は 4 ステップの定型パターン（`composable<AppRoute.Xxx>` 登録 → `toRoute` → `koinViewModel` → `XxxScreen` 呼び出し）。引数 0 個のルートは `NavArgs`/`toRoute` の手順を省略してよい（NAV-19、推奨）。
- ルート引数はプリミティブ型（`String`/`Int`/`Long`/`Boolean` 等）のみ。詳細データは遷移先の画面が Service から再取得する。
- 画面遷移のトリガーは常に `XxxScreenEventHandler`（Screen 層）が持つ。ViewModel のコンストラクタに `AppRouter` を注入しない。
- ディープリンクは `DeepLinkConfig.SCHEME` を各 `composable` の `navDeepLink` に設定する。スキームを変更する場合は 3 箇所の更新が必要。
- Navigation 3 への移行は見送る（[ADR-0007](../decisions/0007-stable-dependencies.md)。安定版優先の方針により、Alpha 版ライブラリへの移行は評価記録に留める）。

## ルール

| ID | 内容 | レベル |
|---|---|---|
| NAV-1 | ルートは `composeApp:ui` の `route/AppRoute.kt` に集約する。`sealed class AppRoute` にネストした `@Serializable data class` として画面ごとのルートを定義する | 必須 |
| NAV-2 | 各ルート data class には同名ネストの `NavArgs` data class（`@Serializable`）を併設し、`val navArgs: NavArgs get() = NavArgs(...)` という変換プロパティを用意する。Screen 関数へは `AppRoute` 本体ではなく `NavArgs` を渡す | 必須 |
| NAV-3 | ルート引数はプリミティブ型のみ持たせる。オブジェクト全体を渡さない | 必須（判断基準 NAV-G9 参照） |
| NAV-4 | `AppRouter` interface（`navigate(route: AppRoute)` / `popBackStack()` の 2 メソッドのみ）は `composeApp:ui` に置く | 必須 |
| NAV-5 | `AppRouter` の実装 `AppRouterImpl`（`NavHostController` を保持）と生成用 Composable 関数 `rememberAppRouter` は `composeApp:app` の `navigation/AppRouter.kt` に `internal` 修飾で置く | 必須 |
| NAV-6 | NavHost の定義（`AppNavHost`）は `composeApp:app` の `navigation/AppNavHost.kt` に `internal` 修飾で置く。`startDestination` は最初に表示する画面の `AppRoute` インスタンス | 必須 |
| NAV-7 | 新しい画面を NavHost に登録するときは「実装パターン」の 4 ステップに従う | 必須 |
| NAV-8 | ViewModel の取得は NavHost の `composable` ブロック内で `koinViewModel<XxxScreenViewModel>()` を呼ぶ。Screen 関数自体は DI を意識せず、ViewModel をコンストラクタ引数として受け取るだけにする | 必須 |
| NAV-9 | 画面遷移のトリガーは `XxxScreenEventHandler` が `appRouter.navigate(AppRoute.Xxx(...))` / `appRouter.popBackStack()` を直接・同期的に呼ぶ形で実装する。ViewModel のコンストラクタに `AppRouter` を注入しない | 必須（[ADR-0016](../decisions/0016-one-shot-events.md)、判断基準 NAV-G10 参照） |
| NAV-10 | 戻る処理は `appRouter.popBackStack()` を `EventHandler` から呼ぶ | 必須 |
| NAV-11 | ディープリンクのスキームは `composeApp:app` の `navigation/DeepLinkConfig.kt` の `DeepLinkConfig.SCHEME` に定数で持つ | 必須 |
| NAV-12 | 各画面の `composable<AppRoute.Xxx>` には `deepLinks = listOf(navDeepLink<AppRoute.Xxx>(basePath = "${DeepLinkConfig.SCHEME}://xxx"))` を設定する | 必須 |
| NAV-13 | アプリ未起動時に受けたディープリンクは `DeepLinkHandler`（`composeApp:app` の `navigation/DeepLinkHandler.kt`）がキャッシュし、`listener` が設定された時点でリプレイする | 必須 |
| NAV-14 | `MainApp.kt` の `Main()` は `DisposableEffect(Unit)` で `DeepLinkHandler.listener` を設定し、`onDispose` で `null` に戻す | 必須 |
| NAV-15 | ディープリンク経由の遷移（`DeepLinkHandler.listener` 内の `navController.navigate(NavUri(uri), ...)`）には `navOptions { launchSingleTop = true }` を付ける。通常の `appRouter.navigate(AppRoute.Xxx(...))` には付けない（既存サンプルの状態） | 必須 |
| NAV-16 | ディープリンクのスキームを変更する場合、判断基準 NAV-G14 の 3 箇所を同時に更新する | 必須 |
| NAV-17 | ネストグラフ（`navigation { }` によるサブグラフ）は必要になるまで導入しない。フラットな `NavHost` に画面を並べる | 推奨（実例なし・新規決定） |
| NAV-18 | Navigation 3 への移行は行わない。現行の `navigation-compose`（Navigation 2 系）の型安全ルートを使い続ける | 必須（[ADR-0007](../decisions/0007-stable-dependencies.md)） |
| NAV-19 | 引数を持たないルートは同名ネストの `NavArgs` を併設した `@Serializable data class` ではなく `@Serializable data object` として定義してよい。この場合 NavHost 側の `backStackEntry.toRoute<AppRoute.Xxx>()` 呼び出しも省略できる（NAV-2/NAV-7 の例外。判断基準 NAV-G15 参照） | 推奨（実例なし・新規決定） |

## 判断基準

### NAV-G9: ルート引数に渡せる型の制限

- if 渡したい値が `String`/`Int`/`Long`/`Boolean` 等のプリミティブ型（または `String?` のような nullable プリミティブ）で表現できる then `AppRoute` のプロパティとして直接持たせる（NAV-3）。
- if 渡したい値がオブジェクト全体（例: 一覧画面で取得済みの `Article`）である then オブジェクトそのものは渡さず、ID やキーワードなどプリミティブな識別子だけを `AppRoute` に持たせ、遷移先の画面が `domain:service` から再取得する。

理由: Navigation Compose の型安全ルートは内部的に SavedState へシリアライズされるため、複雑なオブジェクトを持たせると状態復元やプロセス再生成時の扱いが不安定になる。既存の `AppRoute.Home(keyword: String?)` / `AppRoute.User(username: String)` はいずれもこのパターンに従っている（サンプルでの根拠。削除後は存在しない）。

### NAV-G10: 画面遷移のトリガーをどこに置くか

- 常に `XxxScreenEventHandler`（Screen 層）が遷移を発火する。ViewModel のコンストラクタに `AppRouter` を注入しない（NAV-9）。
- if 遷移するかどうかの判断にビジネスロジック（Service の呼び出し結果など）が必要な場合 then ViewModel 側にその判断結果を返す関数（例: `suspend fun canProceed(): Boolean`）や、判断結果を含む `ScreenEvent` を用意し、`XxxScreenEventHandler` がその結果を見て `appRouter.navigate(...)` を呼ぶ。ViewModel 自身が `AppRouter` を呼ぶことはない。

理由: ViewModel を Navigation ライブラリから独立させることで、`AppRouter` をモックせずに ViewModel のユニットテストが書ける。既存の 2 画面（`HomeScreenViewModel`/`UserScreenViewModel`）はいずれも `AppRouter` を注入されていない（サンプルでの根拠。削除後は存在しない）。

### NAV-G14: ディープリンクのスキームを変更するときに更新が必要な 3 箇所

| ファイル | 内容 |
|---|---|
| `build-logic/src/main/kotlin/BuildUtils.kt` | `const val DEEP_LINK_SCHEME`（Android の `AndroidManifest.xml` の `${deepLinkScheme}` プレースホルダに `androidApp/build.gradle.kts` の `manifestPlaceholders["deepLinkScheme"]` 経由で反映される） |
| `composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/DeepLinkConfig.kt` | `DeepLinkConfig.SCHEME`（各 `composable` の `navDeepLink` の `basePath` に使う） |
| `iosApp/iosApp/Info.plist` | `CFBundleURLTypes` > `CFBundleURLSchemes` |

3 箇所すべてを同じ文字列に揃えないと、プラットフォームによってディープリンクが起動しなくなる。

### NAV-G15: 引数 0 個のルートの扱い（NAV-19）

- if ルートが引数を1つも持たない then `@Serializable data class Xxx(...)` ではなく `@Serializable data object Xxx : AppRoute()` として定義してよい。同名ネストの `NavArgs` の併設と、NavHost 側の `backStackEntry.toRoute<AppRoute.Xxx>()` 呼び出しは省略できる（NAV-2/NAV-7 の例外）。
- if 将来的に引数が増える見込みがある then 最初から `NavArgs` 付きの `data class` にしておくと、後で `data object` → `data class` へ書き換える手戻りを避けられる（判断は PR ごとに行う）。

理由: `NavArgs` は「ルートのシリアライズ用プロパティ」と「Screen 関数に渡す最終引数」を分離するための仕組みであり、渡す引数が無い場合はこの分離自体が不要になる。[playbooks/remove-sample-code.md](../playbooks/remove-sample-code.md) のプレースホルダ `AppRoute.Home` はこのパターンに従う。

```kotlin
// composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/route/AppRoute.kt（引数 0 個の場合。NAV-19、実在しない例）
sealed class AppRoute {
    @Serializable
    data object Xxx : AppRoute()
}
```

```kotlin
// composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/AppNavHost.kt（NAV-19 適用時。toRoute/NavArgs の手順を省略する）
composable<AppRoute.Xxx> {
    val viewModel = koinViewModel<XxxScreenViewModel>()
    XxxScreen(viewModel = viewModel, appRouter = appRouter)
}
```

### NAV-G16: 引数付きルートの `navDeepLink` パス構文

`navDeepLink<AppRoute.Xxx>(basePath = "...")` のように `basePath` のみを指定すると、`AppRoute.Xxx` のシリアライズ可能プロパティが自動的にクエリパラメータとして URI パターンに追加される（[Navigation の型安全性ガイド](https://developer.android.com/guide/navigation/design/type-safety)）。実コードの `composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/AppNavHost.kt` では `AppRoute.Home(val keyword: String? = null)` に対して `navDeepLink<AppRoute.Home>(basePath = "${DeepLinkConfig.SCHEME}://home")` とだけ書いており、`{keyword}` のようなプレースホルダを手書きしていない（サンプルでの根拠。削除後は存在しない）。このため `keyword` は `?keyword=...` 形式のクエリパラメータとして自動的に受け付けられ、`cmp-starter://home?keyword=xxx` の形式でディープリンクが解決される。

- if 引数をクエリパラメータとして渡したい（既定） then `basePath` のみを指定する。プロパティ名がそのままクエリキーになる（上記 `Home(keyword)` の実例参照。サンプルでの根拠。削除後は存在しない）。
- if 引数を URL パスの一部（例: `.../detail/{id}`）として渡したい then `basePath` ではなく `uriPattern` 全体を明示し、プロパティ名と同じ `{id}` のようなプレースホルダをパスに書く（本リポジトリに実例なし。新規決定）。

## 実装パターン

### ルート定義（`composeApp:ui`）

```kotlin
// composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/route/AppRoute.kt
package org.starter.project.ui.route

import kotlinx.serialization.Serializable

sealed class AppRoute {
    @Serializable
    data class Xxx(val id: String) : AppRoute() {
        @Serializable
        data class NavArgs(val id: String)
        val navArgs: NavArgs
            get() = NavArgs(id)
    }
}

interface AppRouter {
    fun navigate(route: AppRoute)
    fun popBackStack()
}
```

新しい画面を追加する場合は、既存の `AppRoute` に新しいネストクラス（`Xxx`）を 1 つ追加するだけでよい。`AppRouter` interface 自体は変更しない。

### NavHost への新規画面登録（4 ステップ）

`composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/AppNavHost.kt` の `NavHost { }` ブロックに以下を追加する。

```kotlin
composable<AppRoute.Xxx>(
    // 1. ルートを composable<AppRoute.Xxx> として登録し、必要なら deepLinks を設定する
    deepLinks = listOf(
        navDeepLink<AppRoute.Xxx>(basePath = "${DeepLinkConfig.SCHEME}://xxx")
    )
) { backStackEntry ->
    // 2. backStackEntry からルートを取得する
    val route = backStackEntry.toRoute<AppRoute.Xxx>()
    // 3. Koin から画面スコープの ViewModel を取得する
    val viewModel = koinViewModel<XxxScreenViewModel>()
    // 4. Screen 関数を呼び出し、viewModel / appRouter / navArgs を渡す
    XxxScreen(viewModel = viewModel, appRouter = appRouter, navArgs = route.navArgs)
}
```

あわせて `composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt` の `appModule` に `viewModelOf(::XxxScreenViewModel)` を 1 行追加する（詳細は [dependency-injection.md](dependency-injection.md)）。

### 画面遷移トリガー（Screen 層の `EventHandler`）

```kotlin
internal object XxxScreenEventHandler {
    operator fun invoke(
        event: ScreenEvent,
        appRouter: AppRouter,
        viewModel: XxxScreenViewModel,
    ) {
        when (event) {
            is XxxScreenEvent.OnClickItem -> {
                appRouter.navigate(AppRoute.XxxDetail(event.id))
            }
            XxxScreenEvent.OnClickBack -> {
                appRouter.popBackStack()
            }
            else -> {
                /* no-op */
            }
        }
    }
}
```

### `AppRouter` の実装（`composeApp:app`）

```kotlin
// composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/AppRouter.kt
package org.starter.project.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.starter.project.ui.route.AppRoute
import org.starter.project.ui.route.AppRouter

internal class AppRouterImpl(
    internal val navController: NavHostController,
) : AppRouter {
    override fun navigate(route: AppRoute) {
        navController.navigate(route)
    }

    override fun popBackStack() {
        navController.popBackStack()
    }
}

@Composable
internal fun rememberAppRouter(
    navController: NavHostController = rememberNavController()
) = remember(navController) {
    AppRouterImpl(navController)
}
```

### ディープリンクの配線

1. スキーム定義（`composeApp:app`）:

```kotlin
// composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/DeepLinkConfig.kt
object DeepLinkConfig {
    const val SCHEME = "cmp-starter"
}
```

2. `MainApp.kt` でリスナーの設定・解除（アプリ起動中のディープリンク受信）:

```kotlin
@Composable
fun Main() {
    val appRouter = rememberAppRouter()
    SystemTheme {
        // ...
        AppNavHost(appRouter)
        DisposableEffect(Unit) {
            DeepLinkHandler.listener = { uri ->
                appRouter.navController.navigate(
                    NavUri(uri),
                    navOptions { launchSingleTop = true }
                )
            }
            onDispose { DeepLinkHandler.listener = null }
        }
    }
}
```

3. Android: `MainActivity` の `onCreate`/`onNewIntent` で `Intent.ACTION_VIEW` を `DeepLinkHandler.onNewUri(uri)` に渡す（アプリ未起動時は `DeepLinkHandler` 内部でキャッシュされ、`listener` 設定時にリプレイされる）。

4. iOS: `Info.plist` の `CFBundleURLTypes` > `CFBundleURLSchemes` にスキームを登録する（Swift 側のディープリンク受信処理から `DeepLinkHandler.onNewUri(uri)` を呼ぶ）。

## アンチパターン

| アンチパターン | なぜダメか | 正しい形 |
|---|---|---|
| ViewModel のコンストラクタに `AppRouter` を注入し、ViewModel 内で `appRouter.navigate(...)` を呼ぶ | ViewModel が Navigation ライブラリに依存し、ユニットテストで `AppRouter` のモックが必須になる | `XxxScreenEventHandler` が `appRouter.navigate(...)` を呼ぶ（NAV-9） |
| `AppRoute` のプロパティにドメインオブジェクト（例: `Article` 全体）をそのまま持たせる | SavedState へのシリアライズが不安定になり、プロセス再生成後の復元で問題が起きうる | ID 等のプリミティブのみ渡し、遷移先で Service から再取得する（NAV-3、NAV-G9） |
| feature モジュールが `AppRouterImpl` や `NavHostController` を直接参照する | `AppRouter` interface による抽象化が崩れ、feature モジュールが Navigation Compose の実装詳細に依存する | feature/ui モジュールは `AppRouter` interface のみに依存する（NAV-4, NAV-5） |
| ディープリンクのスキームを 1 箇所だけ変更する | Android/iOS のいずれかでディープリンクが起動しなくなる | NAV-G14 の 3 箇所を同時に更新する |
| 新しい画面用に `NavHost` を複数用意する、または `navigation { }` のネストグラフを安易に導入する | フラットな 2 画面規模のアプリに対して過剰な複雑さを持ち込む | 画面が増えても `AppNavHost` 内にフラットに `composable` を並べる。ネストグラフは実際に必要になってから検討する（NAV-17） |

## 参考実装（サンプル。削除後は存在しない）

- `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/route/AppRoute.kt` — `AppRoute` sealed class と `AppRouter` interface の実例（`Home`/`User` の 2 ルート）
- `composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/AppNavHost.kt` — NavHost 登録の実例（`composable<AppRoute.Home>`/`composable<AppRoute.User>`）
- `composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/AppRouter.kt` — `AppRouterImpl`/`rememberAppRouter` の実例
- `composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/DeepLinkConfig.kt` — スキーム定数の実例
- `composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/DeepLinkHandler.kt` — キャッシュ&リプレイの実例
- `composeApp/app/src/commonMain/kotlin/org/starter/project/app/MainApp.kt` — `DisposableEffect` によるリスナー配線の実例
- `composeApp/feature/home/src/commonMain/kotlin/org/starter/project/feature/home/HomeScreenEvent.kt` — `appRouter.navigate(...)` を EventHandler から呼ぶ実例
- `composeApp/feature/user/src/commonMain/kotlin/org/starter/project/feature/user/UserScreenEvent.kt` — `appRouter.popBackStack()` を EventHandler から呼ぶ実例
- `androidApp/src/main/kotlin/org/starter/project/android/MainActivity.kt` — Android 側のディープリンク Intent 処理の実例
- `androidApp/src/main/AndroidManifest.xml`, `androidApp/build.gradle.kts` — `manifestPlaceholders["deepLinkScheme"]` によるスキーム反映の実例
- `iosApp/iosApp/Info.plist` — iOS 側の URL スキーム登録の実例
- `build-logic/src/main/kotlin/BuildUtils.kt` — `DEEP_LINK_SCHEME` 定数の実例

## チェックリスト

- [ ] 新しいルートは `composeApp:ui` の `AppRoute.kt` にネストクラスとして追加したか（NAV-1）
- [ ] `NavArgs` と `navArgs` プロパティを用意したか。引数 0 個のルートなら `data object` で省略したか（NAV-2、NAV-19）
- [ ] ルート引数がプリミティブ型のみになっているか（NAV-3, NAV-G9）
- [ ] NavHost 登録の 4 ステップ（`composable` 登録 → `toRoute` → `koinViewModel` → Screen 呼び出し）を満たしているか（NAV-7）
- [ ] `appModule` に `viewModelOf(::XxxScreenViewModel)` を追加したか
- [ ] 画面遷移のトリガーが `XxxScreenEventHandler` にあり、ViewModel に `AppRouter` を注入していないか（NAV-9, NAV-G10）
- [ ] ディープリンクが必要な画面に `navDeepLink` を設定したか（NAV-12）
- [ ] スキームを変更した場合、3 箇所すべてを更新したか（NAV-16, NAV-G14）
