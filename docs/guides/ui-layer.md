# UI 層ガイド

> 対象: `composeApp/feature/*`, `composeApp/ui` の実装規約（画面構成・状態管理・イベント・共通コンポーネント・リソース・テーマ）
> 関連: [navigation.md](../architecture/navigation.md), [dependency-injection.md](../architecture/dependency-injection.md), [error-handling.md](../architecture/error-handling.md), [module-guide.md](../architecture/module-guide.md), [testing.md](testing.md), [coding-style.md](coding-style.md), [0009](../decisions/0009-module-granularity.md), [0010](../decisions/0010-feature-depends-on-service-only.md), [0015](../decisions/0015-uistate-structure.md), [0016](../decisions/0016-one-shot-events.md), [0019](../decisions/0019-error-display-routes.md), [0020](../decisions/0020-material2-and-theme-placeholders.md), [0021](../decisions/0021-string-resources-base-language.md), [0022](../decisions/0022-shared-component-placement.md), [0023](../decisions/0023-shared-state-in-service.md), [0024](../decisions/0024-platform-capabilities-via-app-module.md)
> 最終確認コミット: ac56102

## 要点

- 1 feature モジュール = 1 機能ドメイン（ユーザーフロー）。1 画面なら 4 点セット（`XxxScreen.kt` / `XxxScreenState.kt` / `XxxScreenEvent.kt` / `XxxScreenViewModel.kt`）をパッケージ直下に、複数画面なら `<screen>/` サブパッケージごとに 4 点セットを置く。
- UDF: Content から ViewModel を直接呼ばない。すべて `dispatch(event)` → `XxxScreenEventHandler` 経由。画面遷移は EventHandler が `AppRouter` を直接呼び、ViewModel は Navigation を知らない。
- UiState は `@Immutable internal data class`、先頭フィールドは共通 `ScreenState`。ViewModel は `_screenState`/`_state` を `combine().stateIn(viewModelScope, SharingStarted.Eagerly, ...)` で読み取り専用公開し、更新は `update {}` のみ。
- ViewModel のコンストラクタ注入は `domain:service` のインターフェースのみ（Repository 直接注入は禁止、[ADR-0010](../decisions/0010-feature-depends-on-service-only.md)）。
- ローディング/エラーは `SystemScaffold` + `ScreenState`/`ScreenLoadingState` に統一する。独自の sealed UiState を画面ごとに作らない。
- 新規実装は `collectAsStateWithLifecycle()` を推奨する。既存サンプルの `collectAsState()` は既知の逸脱。
- 共通コンポーネントは「ドメインモデル依存の有無」×「再利用範囲」で `design/system/`（ドメイン非依存・`System` 接頭辞）/ `shared/component/<domain>/`（ドメイン依存・2 箇所以上）/ feature 内 `component/`（画面専用）に置き分ける。
- 表示文言は Compose Resources 必須、ベース言語は日本語に統一する。Material 2 を維持し、テーマ値はプレースホルダとして利用者が差し替える前提。

## ルール

| ID | 内容 | レベル |
|---|---|---|
| UI-1 | 1 feature モジュールは 1 機能ドメイン（ユーザーフロー）を表す。関連の薄い画面を 1 モジュールに詰め込まない | 必須 |
| UI-2 | 1 画面の feature モジュールはパッケージ直下に `XxxScreen.kt` / `XxxScreenState.kt` / `XxxScreenEvent.kt` / `XxxScreenViewModel.kt` の 4 点セットを置く。画面専用の小コンポーネントは `component/` サブパッケージに置く | 必須 |
| UI-3 | 1 モジュールが複数画面を持つ場合、画面ごとに `<screen>/` サブパッケージを切り、その中に UI-2 と同じ 4 点セット（＋必要なら `component/`）を置く（新規決定） | 必須 |
| UI-4 | `XxxScreen`（stateful・`public`・ViewModel/AppRouter/NavArgs を受け取る）と `XxxScreenContent`（stateless・`private`・`state`と`dispatch`のみ受け取る）は同一ファイルに定義する | 必須 |
| UI-5 | UiState は `@Immutable` を付けた `internal data class XxxScreenState` とし、先頭フィールドに共通 `ScreenState` を持つ `screenState: ScreenState` を含める | 必須 |
| UI-6 | UiState のトップレベルフィールド数が 8 を超えたら、関連フィールドをネストした `data class` にグルーピングする。`state` の公開プロパティは 1 つのまま保つ（新規決定） | 推奨 |
| UI-7 | ViewModel は `private val _screenState: MutableStateFlow<ScreenState>` と `private val _state: MutableStateFlow<XxxScreenState>` を持ち、公開プロパティ `internal val state` は `combine(_screenState, _state) { ... }.stateIn(viewModelScope, SharingStarted.Eagerly, _state.value)` とする。`_state`/`_screenState` を直接公開しない | 必須 |
| UI-8 | 状態更新は `_state.update { it.copy(...) }` / `_screenState.update { it.copy(...) }` のみで行い、直接代入しない | 必須 |
| UI-9 | ViewModel のコンストラクタ注入は `domain:service` のインターフェース（`XxxService`）のみ。`data:repository` や `domain:<name>`（実装）を直接注入しない | 必須（[ADR-0010](../decisions/0010-feature-depends-on-service-only.md)） |
| UI-10 | ユーザー操作は `sealed interface XxxScreenEvent : ScreenEvent` として型定義する。Content は `dispatch: (event: ScreenEvent) -> Unit` のみを受け取り、ViewModel のメソッドを直接呼ばない | 必須 |
| UI-11 | イベント処理は同ファイルの `internal object XxxScreenEventHandler { operator fun invoke(event: ScreenEvent, appRouter: AppRouter, viewModel: XxxScreenViewModel, ...) }` の `when` 式に集約する | 必須 |
| UI-12 | 画面遷移は `XxxScreenEventHandler` が `appRouter.navigate(...)` / `appRouter.popBackStack()` を直接呼ぶ。ViewModel のコンストラクタに `AppRouter` を注入しない | 必須（[ADR-0016](../decisions/0016-one-shot-events.md)） |
| UI-13 | 一回性の通知（Snackbar 等）は `ScreenState.snackBarState: SnackBarState?` に値をセットし、表示後は ViewModel 側の関数で `null` に戻す。Channel/SharedFlow による専用 Effect ストリームは新設しない | 必須（[ADR-0016](../decisions/0016-one-shot-events.md)。現状のサンプルはクリア処理が未実装＝既知の逸脱） |
| UI-14 | StateFlow の収集は `collectAsStateWithLifecycle()` を使う | 推奨（既存サンプルの `collectAsState()` は既知の逸脱） |
| UI-15 | `LaunchedEffect` には副作用が依存する値を明示的にキーとして渡す（`Unit` キーは「初回 1 回だけ」の場合のみ許容） | 必須 |
| UI-16 | `LifecycleEventEffect`（`androidx.lifecycle.compose`）は Android/iOS のライフクルイベントに応じた副作用が必要な画面でのみ使う | 任意 |
| UI-17 | `derivedStateOf` は、複数の State から導出するコストの高い計算をコンポジションのたびに再計算したくない場合にのみ使う。単純な計算やコストの低い導出には使わない（新規決定・実例なし） | 任意 |
| UI-18 | ローディング/エラー表示は `SystemScaffold` と共通 `ScreenLoadingState`（`Initial`/`Loading`/`Success`/`Failure`）で表現する。画面ごとに独自の Loading/Error sealed 型を作らない | 必須 |
| UI-19 | 一覧が 0 件のときのプレースホルダは、Content 内で `if (items.isEmpty()) { ... }` のように分岐して専用コンポーネントを表示する（新規決定・実例なし） | 任意 |
| UI-20 | ページングは `Pager` + 自作 `PagingSource` を UI 層（`ui` モジュールの `shared/component/<domain>/XxxPagingSource`）に置き、Screen 側で `collectAsLazyPagingItems()`、ViewModel 側で `.cachedIn(viewModelScope)` する | 必須 |
| UI-21 | `LazyColumn`/`LazyListScope` の各要素には安定した ID を `key` に指定する | 推奨（既存サンプルの `"..._$index"` という index 連結は既知の逸脱） |
| UI-22 | 共通コンポーネントの置き場所は「判断基準」の表に従う | 必須 |
| UI-23 | `design/system/` と `shared/component/` 配下の Composable は `modifier: Modifier = Modifier` を第一引数（デフォルト値付き）で持つ | 必須 |
| UI-24 | feature 内の `XxxScreenContent` や画面専用 `component/` の Composable は `modifier` 引数を省略してよい | 任意 |
| UI-25 | ViewModel/`LazyPagingItems` などの外部依存を持たない stateless な末端コンポーネントには `@Preview` を付ける | 必須 |
| UI-26 | `XxxScreen`/`XxxScreenContent` のような Screen 層 Composable には `@Preview` を付けなくてよい | 任意 |
| UI-27 | `@Preview` 関数は対象コンポーネントと同一ファイルに `private fun <Component>Preview()` として定義し、`SystemTheme { }` でラップする | 必須 |
| UI-28 | 画面・共通コンポーネントに表示する文言はすべて Compose Resources（`src/commonMain/composeResources/values/string.xml`）に切り出す | 必須 |
| UI-29 | ベース言語は日本語に統一する | 必須（既存の `feature:user` の英語文言は既知の逸脱） |
| UI-30 | ロケール別ファイル（`values-en/string.xml` 等）は多言語対応が必要になった時点で追加する（新規決定・実例なし） | 任意 |
| UI-31 | 他モジュールから参照される共通モジュール（例: `ui`）は `compose.resources { publicResClass = true }`、feature 単体モジュールは `publicResClass = false`（既定）にする | 必須 |
| UI-32 | Resource クラスのパッケージ（`<namespace>.resources`）は `kmp-compose-library` 規約プラグインが自動導出する。手動で `packageOfResClass` を指定しない | 必須 |
| UI-33 | テーマはアプリのルート（`Main()`）で一度だけ `SystemTheme { }` にラップし、以降は `MaterialTheme.xxx` ではなく `SystemTheme.colors` / `SystemTheme.typography` / `SystemTheme.shapes` を参照する | 必須 |
| UI-34 | Material 2（`androidx.compose.material.*`）を維持する。Material 3 への移行は人間が判断する（AI エージェントが自律的に行わない） | 必須（[ADR-0020](../decisions/0020-material2-and-theme-placeholders.md)） |
| UI-35 | `SystemTheme.kt` の 3 つの `staticCompositionLocalOf` 初期値（`lightColors()`/`Typography()`/`Shapes()`）はテンプレートのプレースホルダであり、利用者がプロジェクト固有の値に差し替える前提で残す | 必須 |
| UI-36 | ダークモード対応が必要な場合は `isSystemInDarkTheme()` の分岐を `LocalCustomColors` 等の初期値取得箇所に追加する | 任意（実装パターンを参照） |
| UI-37 | `ui`・`feature:*` モジュールに `expect`/`actual` を置かない。プラットフォーム分岐は `core`（ネットワーク）と `app`（DI/エントリポイント）に閉じる | 必須 |
| UI-38 | 命名は `XxxScreen`（stateful・public）/ `XxxScreenContent`（stateless・private）/ `<Component>Preview`（private）に統一する。`design/system/` 配下のみ `System` 接頭辞を付ける | 必須 |
| UI-39 | ViewModel のテストから直接呼びたい内部ロジックは `private` にせず `@VisibleForTesting internal fun` として公開する | 必須 |

## 判断基準

### UI-G1: 新しい画面をどこに置くか（feature モジュールを新設するか既存に足すか）

| 状況 | 判断 |
|---|---|
| 既存 feature モジュールの画面と強く連続するユーザーフロー（一覧→詳細、など）の新画面である | 既存モジュール内に `<screen>/` サブパッケージ（UI-3）を追加する |
| 独立した機能ドメイン（既存画面と遷移的にしか繋がらない、別のユーザーフローの起点になる）である | `composeApp/feature/<new>` として新規モジュールを作る（[module-guide.md](../architecture/module-guide.md)の新規モジュール手順に従う） |

### UI-G2: UiState をいつ分割するか

- if UiState のトップレベルフィールド数が 8 以下 then 単一 `data class` のまま追加する。
- if 8 を超える、または明確に関連するフィールド群（例: フォーム入力とバリデーション結果）がある then その群をネストした `data class` にグルーピングする。`state` の公開プロパティ・`combine().stateIn()` の構造自体は変えない（UI-6）。

入力バリデーションエラー（フォームの入力欄直下に表示する軽微なエラー）は `ScreenState.snackBarState`/`ScreenLoadingState.Failure` のどちらにも属さない4本目の表示経路として扱い、UiState のフィールドに直接載せる（新規決定）。例: `internal data class XxxScreenState(val screenState: ScreenState, val email: String, val emailError: String? = null, ...)`。Service が `Result.failure(ValidationError)` を返した場合、ViewModel が `.handle()` の中でそのフィールドへマッピングする（詳細は [error-handling.md](../architecture/error-handling.md) のエラー表示経路表、[ADR-0014](../decisions/0014-validation-placement.md)、[ADR-0019](../decisions/0019-error-display-routes.md) を参照）。

### UI-G3: 一回性イベント（Navigation / Snackbar）の実装

| 種類 | 実装 |
|---|---|
| 画面遷移 | `XxxScreenEventHandler` が `appRouter.navigate(...)` / `appRouter.popBackStack()` を同期的に直接呼ぶ。ViewModel は `AppRouter` を知らない（UI-12） |
| Snackbar / 軽微なエラー通知 | `SnackBarThrowableHandler(_screenState)` 等で `ScreenState.snackBarState` にセットし、`SystemScaffold` が表示後に呼ぶコールバックで ViewModel 側の `clearSnackBar()`（`_screenState.update { it.copy(snackBarState = null) }`）を呼んでクリアする（UI-13、実装パターン参照） |

### UI-G4: ViewModel に何を注入するか

`domain:service` のインターフェースのみ（UI-9）。Repository を直接注入したくなった場合でも、必要な操作を `domain:service` に追加してから注入する。詳細は [ADR-0010](../decisions/0010-feature-depends-on-service-only.md) を参照。

### UI-G5: 共通コンポーネントの置き場所

| 条件 | 置き場所 | 命名 |
|---|---|---|
| ドメインモデル（`base` モジュールの型）に依存しない汎用 UI プリミティブ | `composeApp/ui` の `design/system/<category>/` | `System` 接頭辞（例: `SystemScaffold`） |
| ドメインモデルに依存し、かつ 2 箇所以上の feature から再利用される | `composeApp/ui` の `shared/component/<domain>/` | 接頭辞なし（例: `ArticleListItem`） |
| 画面専用で他画面から再利用されない | 各 feature モジュールの `component/` | 接頭辞なし（例: `UserProfile`） |

判定は「2 箇所以上で使われるか」を先に確認し、Yes ならさらに「`base` の型に依存するか」で `design/system/` か `shared/component/` かを決める。1 箇所でしか使わない間は feature 内 `component/` に留める。

### UI-G6: `@Preview` を付けるべきか

- if コンポーネントが ViewModel・`LazyPagingItems`・`AppRouter` などの外部依存を持たないstateless な末端コンポーネントである then `@Preview` を必須とする（UI-25）。
- if `XxxScreen`/`XxxScreenContent` のように ViewModel・Paging・AppRouter に依存する Screen 層である then `@Preview` は任意とする（UI-26。モックデータ生成コストが高いため）。

### UI-G7: 文字列リソース化と言語

- 表示文言はすべて `string.xml` に切り出す（UI-28、必須）。
- ベース言語は日本語に統一する（UI-29）。既存の `feature:user` の英語文言（`user_stat_articles` 等）は是正対象であり、新規モジュールで真似しない。
- 多言語対応が必要になった時点で `values-<lang>/string.xml` を追加する（UI-30、任意、実例なし）。

### UI-G8: `publicResClass` を true にするか

- if モジュールが他モジュールから `Res` を参照される共通基盤である（例: `ui`） then `true`。
- if feature 単体モジュールである then `false`（既定。省略した場合のデフォルト値でもある）。

### UI-G11: `Modifier` 引数の要否と位置

- if コンポーネントが `design/system/` または `shared/component/` 配下（再利用目的） then `modifier: Modifier = Modifier` を第一引数として必須にする（UI-23）。
- if feature 内の `XxxScreenContent` や画面専用 `component/` である then 省略してよい（UI-24）。

### UI-G12: 命名規則

| 対象 | 命名 | 可視性 |
|---|---|---|
| Stateful Screen Composable | `XxxScreen` | `public` |
| Stateless Content Composable | `XxxScreenContent` | `private` |
| Preview 関数 | `<Component>Preview` | `private` |
| `design/system/` 配下の共通コンポーネント | `System` 接頭辞 | `public` |
| `shared/component/<domain>/` 配下の共通コンポーネント | 接頭辞なし（ドメイン名をそのまま） | `public` |
| Event 型 | `XxxScreenEvent` | `internal` |
| EventHandler | `XxxScreenEventHandler` | `internal` |

### UI-G13: `collectAsState()` と `collectAsStateWithLifecycle()`

新規実装は `collectAsStateWithLifecycle()` に統一する（UI-14、推奨）。既存の `HomeScreen`/`UserScreen` の `collectAsState()` は技術的負債として扱い、改修時に置き換える。理由: Android 実機でバックグラウンド時の無駄な Flow 収集を避けられるため。

### UI-G14: Material 2 を維持するか、ダークモードをどう足すか

- Material 2 を維持し、`SystemTheme` 経由の抽象化のみで対応する（UI-34）。Material 3 への移行はライブラリ選定レベルの意思決定であり、AI エージェントが自律的に行わない。
- ダークモードが必要になったら `isSystemInDarkTheme()` の分岐を追加する（UI-36、実装パターン参照）。詳細は [ADR-0020](../decisions/0020-material2-and-theme-placeholders.md)。

### UI-G15: ViewModel テストの書き方

本ガイドの責務は構造（テスト容易性のための `@VisibleForTesting internal` 公開、UI-39）の説明までに留める。実際のテストコードの書き方（Mokkery の使い方、命名、AAA コメント等）は [testing.md](testing.md) を参照する。

### UI-G16: UI から呼ぶプラットフォーム固有機能（共有シート・外部ブラウザ起動・クリップボード等）をどう配線するか（[ADR-0024](../decisions/0024-platform-capabilities-via-app-module.md)、新規決定・実例なし）

UI-37 により `ui`/`feature:*` に `expect`/`actual` は置けない。共有シートや外部ブラウザ起動のように UI 起点でプラットフォーム固有 API を呼ぶ必要がある場合は、`ui` モジュールに interface（例: `ShareHandler { fun share(text: String) }`）を置き、`composeApp/app` の `androidMain`/`iosMain` に実装クラスを置いて `platformModule`（[dependency-injection.md](../architecture/dependency-injection.md) の `expect val platformModule`）でバインドする。feature はこの interface を EventHandler または ViewModel の引数として注入する。既存の `AppRouter`（interface は `ui`、実装は `app`）と同じ分離パターンである。詳細は [ADR-0024](../decisions/0024-platform-capabilities-via-app-module.md) を参照。

## 実装パターン

### モジュール構成テンプレート

1 画面の場合:

```text
composeApp/feature/<xxx>/src/commonMain/kotlin/org/starter/project/feature/<xxx>/
  XxxScreen.kt
  XxxScreenState.kt
  XxxScreenEvent.kt
  XxxScreenViewModel.kt
  component/            # 画面専用の小コンポーネント（必要な場合のみ）
```

複数画面の場合（新規決定）:

```text
composeApp/feature/<xxx>/src/commonMain/kotlin/org/starter/project/feature/<xxx>/
  list/
    XxxListScreen.kt
    XxxListScreenState.kt
    XxxListScreenEvent.kt
    XxxListScreenViewModel.kt
    component/
  detail/
    XxxDetailScreen.kt
    XxxDetailScreenState.kt
    XxxDetailScreenEvent.kt
    XxxDetailScreenViewModel.kt
    component/
```

### 4 点セット: State

```kotlin
// composeApp/feature/<xxx>/src/commonMain/kotlin/org/starter/project/feature/<xxx>/XxxScreenState.kt
package org.starter.project.feature.xxx

import androidx.compose.runtime.Immutable
import org.starter.project.ui.shared.state.ScreenState

@Immutable
internal data class XxxScreenState(
    val screenState: ScreenState,
    // 画面固有のフィールドをここに追加する（8 を超えたらネストした data class にグルーピングする）
)
```

### 4 点セット: Event

```kotlin
// composeApp/feature/<xxx>/src/commonMain/kotlin/org/starter/project/feature/<xxx>/XxxScreenEvent.kt
package org.starter.project.feature.xxx

import org.starter.project.ui.route.AppRoute
import org.starter.project.ui.route.AppRouter
import org.starter.project.ui.shared.event.ScreenEvent

internal sealed interface XxxScreenEvent : ScreenEvent {
    data object OnClickErrorScreenAction : XxxScreenEvent
    data object OnSnackBarShown : XxxScreenEvent
    data class OnClickItem(val id: String) : XxxScreenEvent
}

internal object XxxScreenEventHandler {
    operator fun invoke(
        event: ScreenEvent,
        appRouter: AppRouter,
        viewModel: XxxScreenViewModel,
    ) {
        when (event) {
            XxxScreenEvent.OnClickErrorScreenAction -> {
                viewModel.reload()
            }

            XxxScreenEvent.OnSnackBarShown -> {
                viewModel.clearSnackBar()
            }

            is XxxScreenEvent.OnClickItem -> {
                appRouter.navigate(AppRoute.XxxDetail(event.id))
            }

            else -> {
                /* no-op */
            }
        }
    }
}
```

### 4 点セット: ViewModel

```kotlin
// composeApp/feature/<xxx>/src/commonMain/kotlin/org/starter/project/feature/<xxx>/XxxScreenViewModel.kt
package org.starter.project.feature.xxx

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.starter.project.base.extension.handle
import org.starter.project.domain.service.XxxService
import org.starter.project.ui.shared.handler.ErrorScreenThrowableHandler
import org.starter.project.ui.shared.handler.SnackBarThrowableHandler
import org.starter.project.ui.shared.state.ScreenLoadingState
import org.starter.project.ui.shared.state.ScreenState

class XxxScreenViewModel(
    private val xxxService: XxxService
) : ViewModel() {
    private val _screenState = MutableStateFlow(
        ScreenState(ScreenLoadingState.Initial(true), null)
    )
    private val _state = MutableStateFlow(
        XxxScreenState(screenState = _screenState.value)
    )
    internal val state = combine(
        _screenState,
        _state
    ) { screenState, state ->
        state.copy(screenState = screenState)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        _state.value
    )

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            fetch()
        }
    }

    @VisibleForTesting
    internal suspend fun fetch() {
        _screenState.update { it.copy(screenLoadingState = ScreenLoadingState.Loading()) }
        xxxService.fetchXxx().handle(ErrorScreenThrowableHandler(_screenState))?.let {
            _screenState.update { it.copy(screenLoadingState = ScreenLoadingState.Success()) }
        }
    }

    // 軽微なエラーを Snackbar で通知したい場合は ErrorScreenThrowableHandler の代わりに
    // SnackBarThrowableHandler(_screenState) を .handle() に渡す。

    fun clearSnackBar() {
        _screenState.update { it.copy(snackBarState = null) }
    }
}
```

### 4 点セット: Screen（Stateful + Stateless の同居）

```kotlin
// composeApp/feature/<xxx>/src/commonMain/kotlin/org/starter/project/feature/<xxx>/XxxScreen.kt
package org.starter.project.feature.xxx

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.starter.project.ui.design.system.scaffold.SystemScaffold
import org.starter.project.ui.route.AppRoute
import org.starter.project.ui.route.AppRouter
import org.starter.project.ui.shared.event.ScreenEvent

@Composable
fun XxxScreen(
    viewModel: XxxScreenViewModel,
    appRouter: AppRouter,
    navArgs: AppRoute.Xxx.NavArgs
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    XxxScreenContent(state = state) { event ->
        XxxScreenEventHandler(event = event, appRouter = appRouter, viewModel = viewModel)
    }
}

@Composable
private fun XxxScreenContent(
    state: XxxScreenState,
    dispatch: (event: ScreenEvent) -> Unit
) {
    SystemScaffold(
        modifier = Modifier.fillMaxSize(),
        screenState = state.screenState,
        onClickErrorActionButton = { dispatch(XxxScreenEvent.OnClickErrorScreenAction) }
    ) { paddingValues ->
        // 画面コンテンツをここに書く
    }
}
```

上記は現状の `SystemScaffold`（`composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/design/system/scaffold/SystemScaffold.kt`）の実シグネチャ（`modifier, screenState, backgroundColor, onClickErrorActionButton, topBar, bottomBar, content`）のみでコンパイルできる形にしてある。Snackbar 表示後のクリア配線（UI-13）を完成させるには、下記の追加差分が必要（既知の逸脱、下記アンチパターン参照）。

### Snackbar 配線を完成させるための追加差分（新規決定・現状未実装）

上記の4点セットテンプレートには `XxxScreenEvent.OnSnackBarShown` の定義・`XxxScreenEventHandler` での分岐・`XxxScreenViewModel.clearSnackBar()` まではすでに含まれている（UI-G3 参照）。残るのは `SystemScaffold` に表示後コールバックを追加し、`XxxScreenContent` から配線することだけである。

`SystemScaffold.kt` への追加差分（`+` が追加行）:

```kotlin
@Composable
fun SystemScaffold(
    modifier: Modifier = Modifier,
    screenState: ScreenState,
    backgroundColor: Color = SystemTheme.colors.background,
    onClickErrorActionButton: (() -> Unit)? = null,
+   onSnackBarShown: () -> Unit = {},
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(screenState.snackBarState) {
        val message = screenState.snackBarState?.message
        if (message.isNullOrEmpty()) return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = message, duration = SnackbarDuration.Short
        )
+       onSnackBarShown()
    }
    // ...以下は変更なし
}
```

`XxxScreenContent` 側の追加差分:

```kotlin
    SystemScaffold(
        modifier = Modifier.fillMaxSize(),
        screenState = state.screenState,
        onClickErrorActionButton = { dispatch(XxxScreenEvent.OnClickErrorScreenAction) },
+       onSnackBarShown = { dispatch(XxxScreenEvent.OnSnackBarShown) }
    ) { paddingValues ->
        // 画面コンテンツをここに書く
    }
```

### 共通コンポーネント: Modifier 必須の例

```kotlin
// composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/design/system/<category>/SystemXxx.kt
@Composable
fun SystemXxx(
    modifier: Modifier = Modifier,
    // ...
) { /* ... */ }
```

### Preview テンプレート

```kotlin
@Preview
@Composable
private fun XxxPreview() {
    SystemTheme {
        Xxx(/* モックデータをインラインで生成して渡す */)
    }
}
```

### ページング（UI 層）

```kotlin
// composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/component/<domain>/XxxPagingSource.kt
class XxxPagingSource(
    private val onRefresh: () -> Unit = {},
    private val onLoadedFirstPage: () -> Unit = {},
    private val fetcher: suspend (key: String?) -> XxxPage?,
) : PagingSource<String, Xxx>() {
    companion object {
        const val PAGE_SIZE = 20
    }
    override fun getRefreshKey(state: PagingState<String, Xxx>): String? = null
    override suspend fun load(params: LoadParams<String>): LoadResult<String, Xxx> {
        return try {
            params.key ?: onRefresh()
            val currentKey = params.key
            val data = fetcher(currentKey)
            currentKey ?: onLoadedFirstPage()
            LoadResult.Page(data = data?.items.orEmpty(), prevKey = currentKey, nextKey = data?.nextPage)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
```

ViewModel 側:

```kotlin
val xxxPagingFlow = _state
    .map { it.keyword }
    .distinctUntilChanged()
    .flatMapLatest { keyword ->
        Pager(PagingConfig(XxxPagingSource.PAGE_SIZE)) {
            XxxPagingSource(
                onRefresh = ::updateScreenLoading,
                onLoadedFirstPage = ::updateScreenSuccess,
                fetcher = { key -> fetchXxx(keyword, key) }
            )
        }.flow
    }.cachedIn(viewModelScope)
```

Screen 側は `collectAsLazyPagingItems()` で受け取り、`LazyColumn` の `items(count = ..., key = { index -> "xxx_list_items_${xxxPagingItems[index]?.id}" })` のように安定 ID を `key` に使う。

`PagingSource<Key, Value>` の `Key` 型は API のページング方式に合わせる（新規決定）: カーソル方式（次ページを識別する文字列トークンを返す API）なら上記テンプレートどおり `String`、オフセット・ページ番号方式（`page=2` のような整数パラメータで次ページを要求する API）なら `Key` を `Int` にし、`fetcher` の引数とレスポンスの「次のページ番号」もそれに合わせて `Int` にする。Repository のメソッドシグネチャもこの `Key` 型に揃える（[data-layer.md](data-layer.md) のページング節も参照）。

### ダークモード対応テンプレート（`SystemTheme.kt` への追加例）

```kotlin
val LocalCustomColors = staticCompositionLocalOf { lightColors() }

@Composable
fun SystemTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) darkColors() else lightColors()
    CompositionLocalProvider(
        LocalCustomColors provides colors,
        LocalCustomTypography provides SystemTheme.typography,
        LocalCustomShapes provides SystemTheme.shapes,
        content = content
    )
}
```

### ダークモードの手動切替（[ADR-0023](../decisions/0023-shared-state-in-service.md)、現状未実装の差分）

OS 追従（上記）ではなく、Settings 画面等でユーザーが手動切替したテーマを永続化してアプリ全体に反映したい場合は、`SystemTheme` を `darkTheme` 引数付きに変え、`Main()`（`composeApp/app` の `MainApp.kt`。`domain:service` に依存できる唯一の Compose ルート）が共有状態保持 Service（[ADR-0023](../decisions/0023-shared-state-in-service.md) の `AppSettingsService`）を購読して渡す。本テンプレートには実装が無いため、導入する場合は以下の差分を適用する。

`SystemTheme.kt` への追加差分:

```kotlin
@Composable
fun SystemTheme(
+   darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
-   val colors = if (isSystemInDarkTheme()) darkColors() else lightColors()
+   val colors = if (darkTheme) darkColors() else lightColors()
    CompositionLocalProvider(
        LocalCustomColors provides colors,
        LocalCustomTypography provides SystemTheme.typography,
        LocalCustomShapes provides SystemTheme.shapes,
        content = content
    )
}
```

`Main()`（`composeApp/app/src/commonMain/kotlin/org/starter/project/app/MainApp.kt`）側の追加差分:

```kotlin
@Composable
fun Main() {
    val appRouter = rememberAppRouter()
+   val appSettingsService = koinInject<AppSettingsService>()
+   val themeMode by appSettingsService.themeMode.collectAsStateWithLifecycle()
-   SystemTheme {
+   SystemTheme(darkTheme = themeMode == ThemeMode.Dark) {
        Box(/* ... */) {
            AppNavHost(appRouter)
            // ...
        }
    }
}
```

`AppSettingsService`/`ThemeMode` の定義は [ADR-0023](../decisions/0023-shared-state-in-service.md) を参照する。

## アンチパターン

| アンチパターン | なぜダメか | 正しい形 |
|---|---|---|
| `XxxScreenContent` から `viewModel.someMethod()` を直接呼ぶ | UDF が崩れ、`dispatch` 経由の一貫性が失われ、ViewModel のテストからイベント処理を再現できなくなる | `dispatch(XxxScreenEvent.OnClickSomething)` を呼び、`XxxScreenEventHandler` 側で `viewModel` のメソッドを呼ぶ（UI-10, UI-11） |
| ViewModel のコンストラクタに `AppRouter` を注入し、ViewModel 内で `appRouter.navigate(...)` を呼ぶ | ViewModel が Navigation ライブラリに依存し、ユニットテストが困難になる | `XxxScreenEventHandler` が `appRouter.navigate(...)` を呼ぶ（UI-12） |
| 画面ごとに `sealed interface XxxUiState { Loading, Success, Error }` を独自定義する | 共通 `SystemScaffold` が前提とする `ScreenState`/`ScreenLoadingState` と噛み合わず、ローディング/エラー表示が個別実装になる | `@Immutable data class XxxScreenState(val screenState: ScreenState, ...)` を使う（UI-5, UI-18） |
| ViewModel で `_state` を `MutableStateFlow` のまま `internal val` として公開する | 外部から `_state.value = ...` のように書き換え可能になり、単方向データフローが崩れる | `combine(...).stateIn(viewModelScope, SharingStarted.Eagerly, ...)` で読み取り専用公開する（UI-7） |
| `LazyColumn` の `key` を省略する、または `"item_$index"` のように index だけに依存する | 並び替えやページング途中の挿入・削除で recomposition の対応がずれ、状態（フォーカス等）が誤ったアイテムに残る | 記事 ID 等の安定した値を `key` に使う（UI-21） |
| ViewModel に `data:repository` や `domain:<name>`（実装）を直接注入する | レイヤー境界が崩れ、`feature` が実装詳細に依存する。テスト時のモック境界も曖昧になる | `domain:service` のインターフェースのみ注入する（UI-9、[ADR-0010](../decisions/0010-feature-depends-on-service-only.md)） |
| `SnackBarThrowableHandler` でセットした `snackBarState` をクリアせず放置する | 画面回転や再コンポジションのたびに同じ Snackbar が再表示される | 表示後にコールバックで ViewModel の `clearSnackBar()` を呼び `null` に戻す（UI-13） |
| `design/system/` の共通コンポーネントに `modifier` 引数を持たせない | 呼び出し側でレイアウト調整ができず、コンポーネントの再利用性が下がる | `modifier: Modifier = Modifier` を第一引数に持たせる（UI-23） |

## 参考実装（サンプル。削除後は存在しない）

- `composeApp/feature/home/src/commonMain/kotlin/org/starter/project/feature/home/{HomeScreen,HomeScreenState,HomeScreenEvent,HomeScreenViewModel}.kt` — 1 画面 4 点セットの実例。検索・pull-to-refresh・ページングを含む
- `composeApp/feature/user/src/commonMain/kotlin/org/starter/project/feature/user/{UserScreen,UserScreenState,UserScreenEvent,UserScreenViewModel}.kt` — 4 点セット＋`component/` の実例
- `composeApp/feature/user/src/commonMain/kotlin/org/starter/project/feature/user/component/UserProfile.kt` — 画面専用コンポーネントと `@Preview` の実例。ただし Preview 関数名が実際には `UserProfileHeaderPreview` であり、UI-27/UI-38 が規定する `<Component>Preview`（本来は `UserProfilePreview`）から外れている（既知の逸脱。新規実装では規約どおり `<Component>Preview` にする）
- `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/design/system/{scaffold/SystemScaffold,search/SystemSearchBar,loading/SystemLoadingIndicator,theme/SystemTheme}.kt` — `design/system/` 配下の共通コンポーネントとテーマの実例
- `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/component/article/{ArticleList,ArticleListItem,ArticlesPagingSource}.kt` — `shared/component/<domain>/` の実例とページング実装
- `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/state/ScreenState.kt` — 共通 `ScreenState`/`ScreenLoadingState`/`SnackBarState` の定義
- `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/event/ScreenEvent.kt` — `ScreenEvent` マーカー interface
- `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/handler/{ErrorScreenThrowableHandler,SnackBarThrowableHandler,IgnoreThrowableHandler}.kt` — エラー/Snackbar ハンドラの実例（`SnackBarThrowableHandler` は現状未配線）
- `composeApp/ui/src/commonMain/composeResources/values/string.xml`, `composeApp/feature/user/src/commonMain/composeResources/values/string.xml` — Compose Resources のファイル配置（ファイル名は `string.xml`、`strings.xml` ではない）。`feature:user` 側は英語文言のため是正対象
- `composeApp/ui/build.gradle.kts`（`publicResClass = true`）, `composeApp/feature/user/build.gradle.kts`（`publicResClass = false`）

## チェックリスト

- [ ] feature モジュールは 1 機能ドメインに閉じており、無関係な画面を追加していないか（UI-1）
- [ ] 1 画面 / 複数画面のどちらの構成でも 4 点セットが揃っているか（UI-2, UI-3）
- [ ] Content から ViewModel のメソッドを直接呼んでいないか、`dispatch` 経由になっているか（UI-10）
- [ ] ViewModel のコンストラクタ引数が `domain:service` のインターフェースのみか（UI-9）
- [ ] UiState の先頭フィールドが `screenState: ScreenState` か（UI-5）
- [ ] `_state`/`_screenState` を直接公開せず `combine().stateIn()` を使っているか（UI-7）
- [ ] 画面遷移が `XxxScreenEventHandler` から発火されており、ViewModel に `AppRouter` を注入していないか（UI-12）
- [ ] Snackbar を使う場合、表示後にクリアする経路があるか（UI-13）
- [ ] 新規画面で `collectAsStateWithLifecycle()` を使っているか（UI-14）
- [ ] `LazyColumn` の `key` が安定した ID になっているか（UI-21）
- [ ] 新規共通コンポーネントの置き場所が `design/system/` か `shared/component/<domain>/` か feature 内 `component/` か、判断基準表に従っているか（UI-22）
- [ ] `design/system/`・`shared/component/` の Composable が `modifier: Modifier = Modifier` を第一引数に持つか（UI-23）
- [ ] 末端 stateless コンポーネントに `@Preview` があるか（UI-25）
- [ ] 表示文言が `string.xml` に切り出されており、日本語で書かれているか（UI-28, UI-29）
- [ ] 新規 feature モジュールの `publicResClass` が `false`（既定）になっているか（UI-31）
- [ ] `ui`/`feature:*` に `expect`/`actual` を置いていないか（UI-37）
- [ ] ViewModel のテスト対象ロジックが `@VisibleForTesting internal` として公開されているか（UI-39）
