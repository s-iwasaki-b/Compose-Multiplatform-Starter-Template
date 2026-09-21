# 新しい画面を追加する

> 対象: 新しい画面（feature モジュール）を1つ追加する手順。`settings.gradle.kts` への登録から Route/NavHost/DI 登録、テスト、実機検証までを一気通貫で扱う。
> 関連: [module-guide.md](../architecture/module-guide.md), [dependency-injection.md](../architecture/dependency-injection.md), [navigation.md](../architecture/navigation.md), [error-handling.md](../architecture/error-handling.md), [ui-layer.md](../guides/ui-layer.md), [testing.md](../guides/testing.md), [0009-module-granularity.md](../decisions/0009-module-granularity.md), [0010-feature-depends-on-service-only.md](../decisions/0010-feature-depends-on-service-only.md), [add-data-source.md](add-data-source.md)
> 最終確認コミット: ac56102

このドキュメントは、以下のテンプレート実装を Zenn サンプル（`composeApp/feature/user`, `composeApp/feature/home`）から一般化したものです。クラス名・パッケージ名はプレースホルダ（`Xxx` = PascalCase の画面名、`<feature>` = 小文字の機能名）に置き換えてあります。サンプル自体はテンプレート利用開始時に削除される前提なので、本文はサンプルを見なくても再現できるよう実ファイルパスとコード全文を埋め込んでいます。

## 前提と所要ファイル

- **画面が呼び出す `Service` interface が `composeApp/domain/service` に既に存在すること。** 存在しない場合は先に [add-data-source.md](add-data-source.md) で `data`/`domain` 側を作る。API を持たずローカル設定のみを扱う画面（例: ダークモードのオン/オフ切替）でも Repository + Service は省略できない（[ADR-0010](../decisions/0010-feature-depends-on-service-only.md) に例外なし）。この場合は [add-data-source.md](add-data-source.md) の「Preferences のみの場合」分岐で `data`/`domain` 側を作ってから本手順に進む。
- 新しい `feature` モジュールを作るか、既存モジュールにパッケージを追加するかを判断する（**ADR-0009** の基準）。
  | 状況 | 判断 |
  |---|---|
  | 既存の feature モジュールと同じユーザーフロー内の画面（一覧→詳細など、画面遷移で強く連結する） | 既存モジュール内に `<screen>/` サブパッケージを作り4点セットを置く（新規決定。実例なし） |
  | 既存のどの feature とも独立した新しい機能ドメイン | `composeApp/feature/<feature>` を新設する |
- 触るファイル一覧（新規作成/編集の別は各手順に明記）:
  - `settings.gradle.kts`
  - `composeApp/feature/<feature>/build.gradle.kts`（新規）
  - `composeApp/feature/<feature>/src/commonMain/kotlin/org/starter/project/feature/<feature>/{XxxScreen.kt,XxxScreenState.kt,XxxScreenEvent.kt,XxxScreenViewModel.kt}`（新規）
  - `composeApp/feature/<feature>/src/commonMain/composeResources/values/string.xml`（文言があれば新規）
  - `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/route/AppRoute.kt`（編集）
  - `composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/AppNavHost.kt`（編集）
  - `composeApp/app/build.gradle.kts`（編集）
  - `composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt`（編集）
  - 遷移元画面の `XxxScreenEvent.kt`（編集、必要な場合のみ）
  - `composeApp/feature/<feature>/src/commonTest/kotlin/org/starter/project/feature/<feature>/XxxScreenViewModelTest.kt`（新規）

## 手順

### 1. `settings.gradle.kts` にモジュールを登録する

`composeApp` シェルモジュールは `projectDir.walk(maxDepth=3)` で配下の `build.gradle.kts` を自動検出するため `composeApp/build.gradle.kts` 自体の編集は不要。ただし Gradle プロジェクトとしての `include` は自動化されていないので必須。

```kotlin
// settings.gradle.kts
include(":composeApp:feature:<feature>")
```

既存の `include(":composeApp:feature:home")` などと同じ並びに追記する。

### 2. `composeApp/feature/<feature>/build.gradle.kts` を作成する

`composeApp/feature/home/build.gradle.kts`（サンプル）を一般化した基本形:

```kotlin
plugins {
    id("kmp-compose-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.composeApp.ui)
            implementation(projects.composeApp.domain.service)
        }
    }
}
```

- `feature` は Compose UI を持つので convention plugin は `kmp-compose-library`。
- 依存は `ui`（`api`）と `domain:service`（`implementation`）のみ。**`data:*` や `domain:<name>`（実装モジュール）への依存は必須で禁止**（ADR-0010、Gradle 依存で物理的に強制する）。Repository を直接使いたくなっても `feature` からは依存を張れない。

文言（Compose Resources）を持つ画面の場合は `composeApp/feature/user/build.gradle.kts`（サンプル）の追加設定も必要:

```kotlin
plugins {
    id("kmp-compose-library")
}

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.composeApp.ui)
            implementation(projects.composeApp.domain.service)
            implementation(libs.compose.components.resources)
        }
    }
}

compose.resources {
    publicResClass = false
}
```

- `android { androidResources { enable = true } }` と `implementation(libs.compose.components.resources)` は、そのモジュール自身が `composeResources/` を持つ場合に必要（`ui` モジュール経由の推移依存だけでは自モジュールのリソース生成が有効にならない）。文言を持たない画面（`feature:home` 相当）ではこの2行と `compose.resources {}` ブロックは不要。
- `publicResClass`: feature モジュールは他モジュールから `Res` を参照されない前提のため **`false`**（`ui` のような共通モジュールのみ `true`）。

### 3. Screen/State/Event/ViewModel の4点セットを作成する

1画面につき、モジュール直下の同一パッケージに4ファイルを置く（`component/` サブパッケージは画面専用の小コンポーネントが必要になったときだけ作る）。

**`XxxScreenState.kt`**（新規）— `composeApp/feature/<feature>/src/commonMain/kotlin/org/starter/project/feature/<feature>/XxxScreenState.kt`

```kotlin
package org.starter.project.feature.<feature>

import androidx.compose.runtime.Immutable
import org.starter.project.ui.shared.state.ScreenState

@Immutable
internal data class XxxScreenState(
    val screenState: ScreenState,
    // TODO: 画面固有のフィールドをここに追加する
)
```

- 先頭フィールドは常に共通型 `ScreenState`（Loading/Success/Failure と Snackbar 状態を持つ、`composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/state/ScreenState.kt` 定義）。画面固有の Loading/Error を独自 sealed class で作らない。
- フィールドが8を超えたらネストした `data class` でグルーピングする（新規決定）。

**`XxxScreenEvent.kt`**（新規）— 同ディレクトリ

```kotlin
package org.starter.project.feature.<feature>

import org.starter.project.ui.route.AppRouter
import org.starter.project.ui.shared.event.ScreenEvent

internal sealed interface XxxScreenEvent : ScreenEvent {
    data object OnClickErrorScreenAction : XxxScreenEvent
    // TODO: 画面固有のイベントをここに追加する
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
            // TODO: 画面固有のイベント分岐をここに追加する
            else -> {
                /* no-op */
            }
        }
    }
}
```

- Composable は `dispatch: (event: ScreenEvent) -> Unit` だけを持ち、ViewModel のメソッドを直接呼ばない。すべて `XxxScreenEvent` → `XxxScreenEventHandler` を経由する。
- **画面遷移は `EventHandler` が `appRouter.navigate(...)` を直接呼ぶ。ViewModel のコンストラクタに `AppRouter` を注入しない**（ViewModel のテスト容易性を保つため）。遷移がある場合の書き方は手順9を参照。

**`XxxScreenViewModel.kt`**（新規）— 同ディレクトリ

```kotlin
package org.starter.project.feature.<feature>

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
}
```

- `_screenState`/`_state` の2つの `private val MutableStateFlow` を持ち、外部公開の `state` は `combine(...).stateIn(viewModelScope, SharingStarted.Eagerly, ...)` で読み取り専用にする。更新は必ず `update { it.copy(...) }`。`_state` を直接公開しない。
- コンストラクタ注入は **`domain:service` の interface のみ**（`XxxService`）。Repository 実装を直接注入しない（build.gradle.kts の依存でも物理的に不可能）。
- 内部ロジックは `@VisibleForTesting internal` にしてテストから呼べるようにする。
- 上記は Paging を使わない単純化テンプレート（`feature:user` の `fetchUser` 相当を一般化）。一覧画面で Paging が必要な場合は [ui-layer.md](../guides/ui-layer.md) の Paging 節と、サンプルの `HomeScreenViewModel`/`ArticlesPagingSource`（`composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/component/article/ArticlesPagingSource.kt`。サンプル、削除後は存在しない）を参照する。

**`XxxScreen.kt`**（新規）— 同ディレクトリ

```kotlin
package org.starter.project.feature.<feature>

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    val state by viewModel.state.collectAsState()

    XxxScreenContent(state = state) { event ->
        XxxScreenEventHandler(
            event = event,
            appRouter = appRouter,
            viewModel = viewModel
        )
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
        onClickErrorActionButton = {
            dispatch(XxxScreenEvent.OnClickErrorScreenAction)
        }
    ) { paddingValues ->
        // TODO: 画面のコンテンツをここに描画する
    }
}
```

- `XxxScreen`（`public`、stateful）と `XxxScreenContent`（`private`、stateless）は同一ファイルに同居させる。別ファイルに分割しない。
- **推奨**: `collectAsState()` ではなく `collectAsStateWithLifecycle()` を使う。上のテンプレートはサンプルに合わせて `collectAsState()` にしてあるが、これは既知の逸脱（`androidx.lifecycle.compose.collectAsStateWithLifecycle` への置き換えを推奨）。
- 画面全体（`XxxScreen`/`XxxScreenContent`）への `@Preview` は任意。ViewModel/Paging に依存しない末端コンポーネントには `@Preview` を必須で付ける（[ui-layer.md](../guides/ui-layer.md) 参照）。

### 4. 文言があれば Compose Resources を追加する

`composeApp/feature/<feature>/src/commonMain/composeResources/values/string.xml`（新規。ファイル名は複数形 `strings.xml` ではなく単数形 `string.xml` — サンプル `composeApp/feature/user/src/commonMain/composeResources/values/string.xml` で確認済み）。

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="xxx_some_label">ラベル</string>
</resources>
```

- ベース言語は日本語に統一する（`composeApp/feature/user` の英語文言は既知の逸脱）。
- Res クラスのパッケージ（`org.starter.project.feature.<feature>.resources`）は `kmp-compose-library` convention plugin が自動導出するため手動指定不要。`import org.starter.project.feature.<feature>.resources.Res` として参照する。
- このファイルを追加した場合、手順2の「文言を持つ画面」向け `build.gradle.kts` 設定（`androidResources { enable = true }` と `libs.compose.components.resources`）が必須になる。

### 5. `AppRoute.kt` にルートを追加する

`composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/route/AppRoute.kt`（編集）。ルート定義は `feature` モジュールではなく `ui` モジュールに集約する。

```kotlin
// composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/route/AppRoute.kt
sealed class AppRoute {
    // ...既存の Home / User はそのまま...

    @Serializable
    data class Xxx(val param: String? = null) : AppRoute() {
        @Serializable
        data class NavArgs(val param: String?)
        val navArgs: NavArgs
            get() = NavArgs(param)
    }
}
```

- ルート引数は **プリミティブ型（String/Int/Long/Boolean 等）のみ**。詳細データはオブジェクトごと渡さず、遷移先で ID 等から再取得する。
- `AppRoute` 本体とは別に、同名ネストの `NavArgs` data class と `navArgs` 変換プロパティを必ず用意し、`XxxScreen` へは `AppRoute.Xxx` 本体ではなく `NavArgs` を渡す。

### 6. `composeApp/app/build.gradle.kts` に依存を追加する

```kotlin
// composeApp/app/build.gradle.kts の commonMain.dependencies { } に追加
implementation(projects.composeApp.feature.<feature>)
```

`composeApp:app` は実装モジュールを横断的に握る唯一のモジュールなので、新設した `feature` モジュールへの依存をここに追記する。`composeApp`（シェル）自体は動的スキャンのため編集不要。この依存を先に追加しておくことで、次の手順7で `AppNavHost.kt` に追記する `import org.starter.project.feature.<feature>.XxxScreen` 等が解決できる状態になる。

### 7. `AppNavHost.kt` に画面を登録する

`composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/AppNavHost.kt`（編集）。

```kotlin
// import 追加
import org.starter.project.feature.<feature>.XxxScreen
import org.starter.project.feature.<feature>.XxxScreenViewModel
```

```kotlin
// NavHost { } ブロック内に追加
composable<AppRoute.Xxx>(
    deepLinks = listOf(
        navDeepLink<AppRoute.Xxx>(
            basePath = "${DeepLinkConfig.SCHEME}://xxx"
        )
    )
) { backStackEntry ->
    val route = backStackEntry.toRoute<AppRoute.Xxx>()
    val viewModel = koinViewModel<XxxScreenViewModel>()
    XxxScreen(viewModel = viewModel, appRouter = appRouter, navArgs = route.navArgs)
}
```

- 4ステップの定型パターン: (1) `deepLinks` 付きで `composable<AppRoute.Xxx>` を登録 → (2) `backStackEntry.toRoute<AppRoute.Xxx>()` でルート取得 → (3) `koinViewModel<XxxScreenViewModel>()` で ViewModel 取得 → (4) `XxxScreen(...)` を呼ぶ。
- `AppNavHost` は `appRouter: AppRouterImpl` を受け取る（`AppRouter` interface ではなく実装型。`composeApp/app` モジュール内部で完結するため）。この関数自体を編集する必要はなく、上記ブロックを追記するだけでよい。
- DeepLink の `basePath` は `${DeepLinkConfig.SCHEME}://<画面を識別する適当なパス>`。scheme 自体（`cmp-starter`）は変更しない。

### 8. Koin に ViewModel を登録する

`composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt`（編集）。

```kotlin
// import 追加
import org.starter.project.feature.<feature>.XxxScreenViewModel
```

```kotlin
val appModule = module {
    viewModelOf(::HomeScreenViewModel)
    viewModelOf(::UserScreenViewModel)
    viewModelOf(::XxxScreenViewModel) // 追加
}
```

- `appModule` に1行追加するだけでよい。`single`/`factory` ではなく `viewModelOf` を使う。
- ここで未登録のまま NavHost 側だけ実装すると、実行時に `NoDefinitionFoundException` が発生する（よくある失敗を参照）。

### 9. 遷移元の EventHandler から画面遷移を追加する（別画面から遷移する場合）

既存画面から新しい画面へ遷移するリンクがある場合、遷移元の `XxxScreenEvent.kt`（`XxxScreenEventHandler`）に分岐を追加する。`HomeScreenEvent.kt`（サンプル）の例:

```kotlin
// 遷移元の XxxScreenEventHandler.invoke 内の when (event) に追加
is HomeScreenEvent.OnClickUser -> {
    appRouter.navigate(AppRoute.User(event.username))
}
```

- 遷移は必ず `EventHandler`（Screen 側）が `appRouter.navigate(AppRoute.Xxx(...))` を同期的に呼ぶ形にする。ViewModel が `AppRouter` を持つ設計にしない。

### 10. ViewModel テストを追加する

**この手順のテストパターンはサンプルに実例が無く、本ガイドで新たに定めたもの（新規決定）**。既存の `feature:home`/`feature:user` には ViewModel テストが存在しない。`domain:zenn` の `ZennServiceTest`（Mokkery ベース、[testing.md](../guides/testing.md) 参照）の書き方を ViewModel に適用する。

まず `composeApp/feature/<feature>/build.gradle.kts` にテスト関連の設定を追加する:

```kotlin
plugins {
    id("kmp-compose-library")
    alias(libs.plugins.mokkery) // 追加
}

kotlin {
    // ...
    sourceSets {
        commonMain.dependencies {
            // ...既存のまま
        }
        commonTest.dependencies { // 追加
            implementation(libs.bundles.test)
        }
    }
}
```

テスト本体 `composeApp/feature/<feature>/src/commonTest/kotlin/org/starter/project/feature/<feature>/XxxScreenViewModelTest.kt`（新規）:

```kotlin
package org.starter.project.feature.<feature>

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.starter.project.domain.service.XxxService
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class XxxScreenViewModelTest {
    private val mockXxxService = mock<XxxService>(MockMode.autofill)
    private lateinit var subject: XxxScreenViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        subject = XxxScreenViewModel(mockXxxService)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fetch_success_updatesState() = runTest {
        // arrange
        everySuspend { mockXxxService.fetchXxx() } returns Result.success(/* ダミーの戻り値 */)

        // act
        subject.fetch()

        // assert
        assertEquals(/* 期待する state */ Unit, Unit)
    }
}
```

- `mock<XxxService>(MockMode.autofill)` を使うか既定モードにするかは [testing.md](../guides/testing.md) の判断基準に従う（テストが呼び出しを明示的に検証する依存は既定モード、結果に影響しない協調オブジェクトは `autofill`）。
- `viewModelScope` は内部で `Dispatchers.Main.immediate` を使うため、`@BeforeTest` で `Dispatchers.setMain(StandardTestDispatcher())`、`@AfterTest` で `Dispatchers.resetMain()` が必須（新規決定。`domain` 層の `ResultHandler(StandardTestDispatcher())` 注入パターンとは異なる）。
- `subject` は `private lateinit var` で宣言し、`@BeforeTest` 内で `Dispatchers.setMain(...)` の**後**に生成する。Kotlin のテストライフサイクルではプロパティ初期化子（`private val subject = XxxScreenViewModel(...)`）は `@BeforeTest` より前に実行されるため、先に `subject` をプロパティ初期化子で作ると、`init { reload() }` が起動する `viewModelScope.launch` が Main dispatcher 未設定のまま実行され失敗する。
- テストクラス名は `対象クラス名+Test`、メソッド名は `<メソッド名>_<条件>_<期待結果>`、本体は `// arrange` `// act` `// assert` コメント必須（[testing.md](../guides/testing.md)）。

## 検証

1. ユニットテスト: `./gradlew testAndroidHostTest`（リポジトリ全体）。モジュール単位なら `./gradlew :composeApp:feature:<feature>:testAndroidHostTest`。
2. 実機/シミュレータでの起動確認: [`.claude/skills/debug-run/skill.md`](../../.claude/skills/debug-run/skill.md)（Android/iOS 両対応。片方だけなら `debug-run-android`/`debug-run-ios`）を使う。ビルドエラー・実行時エラーの検出も併せて行われる。
3. DeepLink の動作確認（手順7で `basePath` を設定した場合）。起動中のシミュレータ/エミュレータに対して:
   ```bash
   # Android
   adb shell am start -W -a android.intent.action.VIEW -d "cmp-starter://xxx" org.starter.project

   # iOS
   xcrun simctl openurl booted "cmp-starter://xxx"
   ```
   アプリが起動し `XxxScreen` が表示されれば成功。
4. Koin 起動確認: アプリ起動直後に画面へ遷移し、`NoDefinitionFoundException` が出ないこと（手順8の登録漏れがあると発生する）。

## よくある失敗と対処

| 失敗 | 症状 | 対処 |
|---|---|---|
| `settings.gradle.kts` の `include` を忘れる | Gradle が新モジュールを認識せず、`projects.composeApp.feature.<feature>` の型セーフアクセサが解決できない | 手順1を実施する |
| `composeApp/app/build.gradle.kts` への依存追加を忘れる | `AppNavHost.kt`/`Koin.kt` から新 feature の import が解決できずコンパイルエラー | 手順6を実施する |
| Koin 未登録 | 実行時に `NoDefinitionFoundException`（画面遷移時にクラッシュ） | 手順8で `appModule` に `viewModelOf(::XxxScreenViewModel)` を追加する |
| `publicResClass` の誤設定 | `true` にすると不要に `Res` を公開してしまう。`false` のまま他モジュールから参照しようとするとコンパイルエラー | feature モジュールは `false` を既定にする（手順2） |
| Route を feature 側に定義してしまう | `ui` モジュールが feature に依存できない（依存許可表に反する）ため、他 feature から同じルートを参照できない | `AppRoute` は必ず `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/route/AppRoute.kt` に置く（手順5） |
| ViewModel に Repository を直接注入しようとする | `feature/<feature>/build.gradle.kts` に `data:repository` への依存が無いため Gradle 解決エラー | `domain:service` の Service interface のみをコンストラクタ注入する（手順3） |
| Compose Resources 用の `build.gradle.kts` 追記漏れ | `string.xml` を追加したのに `androidResources { enable = true }` や `libs.compose.components.resources` が無く、リソースがコンパイルされない/参照できない | 手順2の「文言を持つ画面」向け設定を追加する |
