# composeApp/ui

共有 UI 部品（デザインシステム）、`ScreenState`/`ScreenEvent` 契約、`AppRoute`/`AppRouter` interface、共通エラーハンドラを置く基盤モジュール。

本ファイルはこのモジュールで作業する実装エージェントの入口である。ルートの [AGENTS.md](../../AGENTS.md)（絶対ルール・実行体制）を前提とし、ツールが自動で読み込まない場合は先に読む。実装エージェントは本モジュール外のファイルを編集せず、必要な他モジュールの変更は「他モジュールとの接点」に沿ってオーケストレーターへ報告する。読み順は本ファイル → 「参照」節の docs。

## 責務

- 置く: `design/system/` の汎用 UI プリミティブ（`System` 接頭辞）、`shared/component/<domain>/` のドメイン依存共通コンポーネント（2 箇所以上で再利用するもの）、`ScreenState`/`ScreenLoadingState`/`SnackBarState`、`ScreenEvent` マーカー interface、`ErrorScreenThrowableHandler`/`SnackBarThrowableHandler`/`IgnoreThrowableHandler`、`AppRoute`（sealed class + ネストした `@Serializable data class`）、`AppRouter` interface（`navigate`/`popBackStack` の2メソッドのみ）、`SystemTheme`、Compose Resources の文言（`string.xml`）。
- 置かない: 画面固有の Screen/State/Event/ViewModel の4点セット（→ 各 `feature/<name>`）、`AppRouter` の実装・`AppNavHost`・DeepLink（→ `app`）、Koin 登録（→ `app`）、1 箇所でしか使わないコンポーネント（→ feature 内 `component/`）。

## 依存

`build.gradle.kts` と一致（`kmp-compose-library` + `kotlin.serialization`）:

- `api(projects.composeApp.base)` — domain モデル・`ApiError`/`ConversionError`・拡張関数
- `api(libs.bundles.ui)` — `compose-runtime`/`compose-material`（Material 2）/`compose-material-icons-extended`/`compose-components-resources`/`compose-ui-tooling-preview`/`androidx-navigation-compose`/`androidx-paging-compose`/`coil`/`coil-network`
- `compose.resources { publicResClass = true }`（他モジュールから `Res` を参照させる）

**依存禁止**: `domain:service`、`data:*`、`feature:*` のいずれにも依存しない。`expect`/`actual` を置かない（`core`/`app`限定）。

## 構成

```text
src/commonMain/kotlin/org/starter/project/ui/
  design/system/
    loading/SystemLoadingIndicator.kt
    scaffold/SystemScaffold.kt
    search/SystemSearchBar.kt
    theme/SystemTheme.kt
  route/AppRoute.kt
  shared/
    component/<domain>/       # ドメイン依存の共通コンポーネント（2 箇所以上で再利用するもの）
      XxxList.kt
      XxxListItem.kt
      XxxPagingSource.kt
    event/ScreenEvent.kt
    handler/{ErrorScreenThrowableHandler,SnackBarThrowableHandler,IgnoreThrowableHandler}.kt
    state/ScreenState.kt
src/commonMain/composeResources/values/string.xml
```

## 実装パターン

新しいルートを追加する（引数あり/なし）:

```kotlin
sealed class AppRoute {
    @Serializable
    data class Xxx(val id: String) : AppRoute() {
        @Serializable data class NavArgs(val id: String)
        val navArgs: NavArgs get() = NavArgs(id)
    }
    @Serializable
    data object Yyy : AppRoute() // 引数なしルート
}
```

共通コンポーネント（`design/system/`・`shared/component/`）の骨格:

```kotlin
@Composable
fun SystemXxx(
    modifier: Modifier = Modifier,
    // ...
) { /* SystemTheme.colors/typography/shapes のみ参照。MaterialTheme 直参照は避ける */ }

@Preview
@Composable
private fun SystemXxxPreview() {
    SystemTheme { SystemXxx() }
}
```
命名は `design/system/` のみ `System` 接頭辞、`shared/component/<domain>/` は接頭辞なし。Screen/State/Event/ViewModel の4点セットなど feature 側の一般則は [coding-guide.md](../../docs/coding-guide.md) §4 を参照。

## テスト

本モジュールに `commonTest` は無い。ビジネスロジックを持たず、`ScreenState`/`AppRoute` は宣言のみのため必須テスト対象（Converter/Repository 実装/Service 実装/ViewModel）に該当しない。Compose UI テストは書かなくてよい（必須ではない）。`commonTest.dependencies` を追加する場合は `mokkery` プラグインを適用しテストファイルを最低1つ置く。実行: `./gradlew :composeApp:ui:testAndroidHostTest`。

## 他モジュールとの接点

実装エージェントは自モジュール外を編集せず、以下をオーケストレーターへの完了報告に含める。

- `AppRoute` に新しいルートを追加した → `app` の `AppNavHost` に `composable<AppRoute.Xxx>` 登録が必要
- 新しい共通コンポーネント/ハンドラを追加した → 利用する `feature/<name>` 側の実装が必要
- 新しいプラットフォーム機能 interface（decisions.md D-24）を追加した → `app` の `platformModule`（`Koin.android.kt`/`Koin.ios.kt`）に実装バインドが必要

## 完了条件

- `design/system/`・`shared/component/` の Composable が `modifier: Modifier = Modifier` を第一引数に持つか確認した
- 表示文言を `string.xml` に切り出し、日本語で書いたか確認した
- `expect`/`actual` を追加していないか確認した
- `commonTest` を追加した場合、`./gradlew :composeApp:ui:testAndroidHostTest` を実行した
- [../../docs/coding-guide.md](../../docs/coding-guide.md) §8 の既知の逸脱を新規コードに複製していない。

## 参照

- [../../docs/design-guide.md](../../docs/design-guide.md) §3 層の境界と実装粒度、§5 ナビゲーション、§6 エラーハンドリング
- [../../docs/coding-guide.md](../../docs/coding-guide.md) §1 共通スタイル、§4 ui / feature 層
- docs/decisions.md D-15、D-16、D-19、D-20、D-21、D-22、D-24
