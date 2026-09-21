# サンプルコード（Zenn ビューワー）を削除する

> 対象: 本テンプレートに同梱されている Zenn 記事ビューワーのサンプル実装を安全に削除し、アーキテクチャの骨格（層構成・DI・Navigation の型）だけを残す手順。
> 関連: [architecture/overview.md](../architecture/overview.md), [architecture/module-guide.md](../architecture/module-guide.md), [architecture/dependency-injection.md](../architecture/dependency-injection.md), [architecture/navigation.md](../architecture/navigation.md), [guides/data-layer.md](../guides/data-layer.md), [guides/domain-layer.md](../guides/domain-layer.md), [guides/ui-layer.md](../guides/ui-layer.md), [guides/testing.md](../guides/testing.md), [add-feature-screen.md](./add-feature-screen.md), [add-data-source.md](./add-data-source.md)
> 最終確認コミット: ac56102

## 前提と所要ファイル

- 本テンプレートを使って新規プロジェクトを始める最初のタイミングで実施する（開発が進んだ後に実施した場合の動作は未検証）。
- リネームより先に本手順（サンプル削除）を実施する。[README.md](../../README.md) の `## How to Rename` に従ったプロジェクト名・パッケージ名変更（`build-logic/src/main/kotlin/BuildUtils.kt` の `PACKAGE_NAME`）は、本手順の完了後に行う（削除対象一覧のパスが本ドキュメント記載のパス `org.starter.project` 基準と一致した状態で作業できるため。新規決定。本サンプル削除の必須手順ではないが、同時に行うことが多いため関連手順として記載する）。
- **削除はすべて破壊的な操作である。** 本手順の各 `git rm` を実行する前に、必ずステップ1の削除対象一覧を人（レビュアー）に提示し、明示的な確認を得ること。確認なしに一括削除しない。
- 作業前に `git status` でツリーがクリーンであることを確認し、可能であれば専用ブランチ（`claude/remove-sample-code` 等）を切る。

## 手順

### ステップ1: 削除対象の確定と提示（確認必須）

以下は本ワークツリー（コミット `ac56102`）を `find`/`grep` で実走査して確認した、削除対象の正確なファイル一覧である。新しいコミットで内容が増減している場合は、適用前に同じコマンドで再確認すること。

**削除するモジュール（ディレクトリごと）**

```text
composeApp/data/zenn/
  build.gradle.kts
  proguard-rules.pro
  src/commonMain/kotlin/org/starter/project/data/zenn/converter/ArticlesConverter.kt
  src/commonMain/kotlin/org/starter/project/data/zenn/converter/UserConverter.kt
  src/commonMain/kotlin/org/starter/project/data/zenn/datasource/api/ZennApi.kt
  src/commonMain/kotlin/org/starter/project/data/zenn/datasource/api/response/ArticlesResponse.kt
  src/commonMain/kotlin/org/starter/project/data/zenn/datasource/api/response/UserResponse.kt
  src/commonMain/kotlin/org/starter/project/data/zenn/datasource/preferences/ZennPreferences.kt
  src/commonMain/kotlin/org/starter/project/data/zenn/repository/ZennRepositoryImpl.kt
  src/commonTest/kotlin/org/starter/project/data/zenn/converter/ArticlesConverterTest.kt
  src/commonTest/kotlin/org/starter/project/data/zenn/repository/ZennRepositoryTest.kt

composeApp/domain/zenn/
  build.gradle.kts
  proguard-rules.pro
  src/commonMain/kotlin/org/starter/project/domain/zenn/ZennServiceImpl.kt
  src/commonTest/kotlin/org/starter/project/domain/zenn/ZennServiceTest.kt

composeApp/feature/user/
  build.gradle.kts
  src/commonMain/composeResources/values/string.xml
  src/commonMain/kotlin/org/starter/project/feature/user/component/UserProfile.kt
  src/commonMain/kotlin/org/starter/project/feature/user/UserScreen.kt
  src/commonMain/kotlin/org/starter/project/feature/user/UserScreenEvent.kt
  src/commonMain/kotlin/org/starter/project/feature/user/UserScreenState.kt
  src/commonMain/kotlin/org/starter/project/feature/user/UserScreenViewModel.kt
```

**削除する個別ファイル・ディレクトリ（モジュール自体は残す）**

```text
composeApp/data/repository/src/commonMain/kotlin/org/starter/project/data/repository/ZennRepository.kt
composeApp/domain/service/src/commonMain/kotlin/org/starter/project/domain/service/ZennService.kt
composeApp/base/src/commonMain/kotlin/org/starter/project/base/data/model/zenn/          (ディレクトリごと: Articles.kt, User.kt)
composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/component/article/      (ディレクトリごと: ArticleList.kt, ArticleListItem.kt, ArticlesPagingSource.kt)
composeApp/core/src/commonMain/kotlin/org/starter/project/core/preferences/PreferencesKey.kt
```

**書き換える（プレースホルダに置き換える。削除ではない）**

```text
composeApp/feature/home/src/commonMain/kotlin/org/starter/project/feature/home/HomeScreen.kt
composeApp/feature/home/src/commonMain/kotlin/org/starter/project/feature/home/HomeScreenEvent.kt
composeApp/feature/home/src/commonMain/kotlin/org/starter/project/feature/home/HomeScreenState.kt
composeApp/feature/home/src/commonMain/kotlin/org/starter/project/feature/home/HomeScreenViewModel.kt
```

**残すもの（参考。削除対象に含めない）**: `base`（`ApiError.kt`, `ConversionError.kt`, `extension/` 配下）、`core`（`ApiClient` 一式, `ApiConfig.kt`, `PreferencesConfig.kt` は書き換えて残す）、`data:repository`（`Repository.kt` マーカーのみ残る）、`domain:service`（`Service.kt`, `ResultHandler.kt` のみ残る）、`ui`（デザインシステム、`ScreenState.kt`, `ScreenEvent.kt`, `handler/` 配下の3ハンドラ、`route/AppRoute.kt`, `route/AppRouter` 相当）、`app`（DI・Navigation・DeepLink・エントリポイント。中身は次ステップで書き換える）。

上記一覧を PR またはコミット前にレビュアーに提示し、承認を得てから次のステップへ進むこと。

### ステップ2: モジュール・ファイルの削除

承認後、以下を実行する（パスはステップ1のリストと一致させる）。

```bash
git rm -r composeApp/data/zenn
git rm -r composeApp/domain/zenn
git rm -r composeApp/feature/user
git rm composeApp/data/repository/src/commonMain/kotlin/org/starter/project/data/repository/ZennRepository.kt
git rm composeApp/domain/service/src/commonMain/kotlin/org/starter/project/domain/service/ZennService.kt
git rm -r composeApp/base/src/commonMain/kotlin/org/starter/project/base/data/model/zenn
git rm -r composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/component/article
git rm composeApp/core/src/commonMain/kotlin/org/starter/project/core/preferences/PreferencesKey.kt
```

### ステップ3: 参照の更新

削除だけではビルドが壊れる。以下のファイルを1つずつ編集する。

**`settings.gradle.kts`** — 削除したモジュールの `include` を3行削除する。

```diff
 include(":composeApp:app")
 include(":composeApp:base")
 include(":composeApp:core")
-include(":composeApp:data:zenn")
 include(":composeApp:data:repository")
-include(":composeApp:domain:zenn")
 include(":composeApp:domain:service")
 include(":composeApp:feature:home")
-include(":composeApp:feature:user")
 include(":composeApp:ui")
```

`composeApp/build.gradle.kts`（アグリゲーションシェル）は `projectDir.walk(maxDepth=3)` で配下の `build.gradle.kts` を動的に検出する実装のため、**この動的スキャン自体は編集不要**。ただし `settings.gradle.kts` の `include` を消し忘れると Gradle プロジェクトとして存在しないモジュールを参照し続けてエラーになるので、この手順は省略できない。

**`composeApp/app/build.gradle.kts`** — 削除したモジュールへの `implementation` を3行削除する。

```diff
         commonMain.dependencies {
             implementation(projects.composeApp.core)
             implementation(projects.composeApp.data.repository)
-            implementation(projects.composeApp.data.zenn)
             implementation(projects.composeApp.domain.service)
-            implementation(projects.composeApp.domain.zenn)
             implementation(projects.composeApp.feature.home)
-            implementation(projects.composeApp.feature.user)
             implementation(projects.composeApp.ui)
         }
```

**`composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt`** — Zenn 固有の import・バインドをすべて削除し、層別モジュールの骨格（空の `module { }`）だけを残す。中身のない層は「次に機能を追加するときにここへ登録する」という骨格として残すこと（`val` ごと消さない）。

```kotlin
package org.starter.project.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.starter.project.core.api.ApiClient
import org.starter.project.core.api.ApiClientImpl
import org.starter.project.core.preferences.PreferencesConfig
import org.starter.project.domain.service.ResultHandler
import org.starter.project.feature.home.HomeScreenViewModel

expect val platformModule: org.koin.core.module.Module

fun startKoin(platformDeclaration: KoinAppDeclaration? = null) {
    org.koin.core.context.startKoin {
        platformDeclaration?.invoke(this)

        val coreModule = module {
            single<ApiClient> { ApiClientImpl() }
            singleOf(::PreferencesConfig)
        }

        val dataSourceModule = module {
            // TODO: register your XxxApi / XxxPreferences bindings here.
            // See docs/playbooks/add-data-source.md.
        }

        val repositoryModule = module {
            // TODO: register your single<XxxRepository> { XxxRepositoryImpl(...) } bindings here.
        }

        val serviceModule = module {
            single { ResultHandler() }
            // TODO: register your single<XxxService> { XxxServiceImpl(...) } bindings here.
        }

        val appModule = module {
            viewModelOf(::HomeScreenViewModel)
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

**`composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/route/AppRoute.kt`** — `User` ルートを削除する。`Home` はプレースホルダ画面がナビゲーション引数を必要としないため、`data class` + `NavArgs` から引数なしの `data object` に簡略化する。これは [architecture/navigation.md](../architecture/navigation.md) の NAV-19（引数を持たないルートは `data object` とし `NavArgs` の併設と `toRoute()` 呼び出しを省略してよい。NAV-2/NAV-7 の例外）に従う。引数を持つ画面を追加する場合は既存の `Home(val keyword: String?)` のような `data class` + ネストした `NavArgs` パターンに戻して構わない。

```kotlin
package org.starter.project.ui.route

import kotlinx.serialization.Serializable

sealed class AppRoute {
    @Serializable
    data object Home : AppRoute()
}

interface AppRouter {
    fun navigate(route: AppRoute)
    fun popBackStack()
}
```

**`composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/AppNavHost.kt`** — `User` の `composable` ブロックと未使用 import を削除し、`Home` の呼び出しを `NavArgs` なしに合わせる。

```kotlin
package org.starter.project.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import org.koin.compose.viewmodel.koinViewModel
import org.starter.project.feature.home.HomeScreen
import org.starter.project.feature.home.HomeScreenViewModel
import org.starter.project.ui.route.AppRoute

@Composable
internal fun AppNavHost(
    appRouter: AppRouterImpl
) {
    NavHost(appRouter.navController, startDestination = AppRoute.Home) {
        composable<AppRoute.Home>(
            deepLinks = listOf(
                navDeepLink<AppRoute.Home>(basePath = "${DeepLinkConfig.SCHEME}://home")
            )
        ) {
            val viewModel = koinViewModel<HomeScreenViewModel>()
            HomeScreen(viewModel = viewModel, appRouter = appRouter)
        }
    }
}
```

**`composeApp/core/src/commonMain/kotlin/org/starter/project/core/api/ApiConfig.kt`** — Zenn の baseUrl をプレースホルダに差し替える。

```kotlin
package org.starter.project.core.api

internal object ApiConfig {
    // TODO: replace with your own API base URL.
    const val API_BASE_URL = "https://example.com/"
}
```

**`composeApp/core/src/commonMain/kotlin/org/starter/project/core/preferences/PreferencesConfig.kt`** — `zennPreferences` プロパティは `ZennPreferencesImpl`（削除済み）専用だったため削除する。`factory` はそのまま残し、次の機能追加時に `val xxxPreferences: Settings = factory.create("xxx_preferences")` を足せるようにしておく。

```kotlin
package org.starter.project.core.preferences

import com.russhwolf.settings.Settings

class PreferencesConfig(private val factory: Settings.Factory) {
    // TODO: add `val xxxPreferences: Settings = factory.create("xxx_preferences")` per feature.
}
```

**`README.md`** — Zenn サンプルに言及している箇所を更新する。`## Demo` セクション（Zenn ビューワーのデモ動画）と `## More Detail`（`cf. https://zenn.dev/dely_jp/articles/ce01725bde5ed4` へのリンク）は、サンプル固有の内容なので削除するか自分のプロジェクトの内容に置き換える。3rd Party Dependencies の表（Koin, Ktor, Mokkery 等）は Zenn 固有ではないため変更不要。

**`gradle/libs.versions.toml`**（任意）— `ui` bundle が持つ `androidx-paging-compose` / `coil` / `coil-network` は `ArticleList*`/`ArticlesPagingSource`/`UserProfile` 専用だった。Paging・画像ロードを使う予定がなければ、`[versions]` の `androidx-paging`/`coil`、`[libraries]` の該当行、`[bundles].ui` の該当3行を削除してよい。使う予定があるなら残す。

**`build-logic/src/main/kotlin/BuildUtils.kt`**（関連手順。本削除作業とは独立だが同時に行うことが多い）— `PACKAGE_NAME`/`DEEP_LINK_SCHEME` の変更は README の `## How to Rename` にある `ChangeProjectName`/`ChangePackageName` Gradle タスクで行う。手動編集は非推奨（`deriveNamespace` が壊れる）。

**`.claude/skills/debug-run/skill.md`, `.claude/skills/debug-run-android/skill.md`, `.claude/skills/debug-run-ios/skill.md`** — 確認した限り Zenn 固有の記述は無い（パッケージ名 `org.starter.project` 等のみ）。変更不要。

### ステップ4: プレースホルダ画面の作成

`feature:home` は Navigation の `startDestination` として必ず1画面必要なため、モジュールごと削除せず、依存を持たない最小の4点セットに置き換える。これは**サンプルに実例が無い新規決定**であり、状態管理・イベント処理の骨格だけを示す最小構成である。

**`HomeScreenState.kt`**

```kotlin
package org.starter.project.feature.home

import androidx.compose.runtime.Immutable
import org.starter.project.ui.shared.state.ScreenState

@Immutable
internal data class HomeScreenState(
    val screenState: ScreenState
)
```

**`HomeScreenEvent.kt`**

```kotlin
package org.starter.project.feature.home

import org.starter.project.ui.shared.event.ScreenEvent

internal sealed interface HomeScreenEvent : ScreenEvent {
    data object OnClickErrorScreenAction : HomeScreenEvent
}

internal object HomeScreenEventHandler {
    operator fun invoke(
        event: ScreenEvent,
        viewModel: HomeScreenViewModel
    ) {
        when (event) {
            HomeScreenEvent.OnClickErrorScreenAction -> {
                // TODO: implement retry/reload logic once this screen fetches data.
            }

            else -> {
                /* no-op */
            }
        }
    }
}
```

**`HomeScreenViewModel.kt`**

```kotlin
package org.starter.project.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.starter.project.ui.shared.state.ScreenLoadingState
import org.starter.project.ui.shared.state.ScreenState

class HomeScreenViewModel : ViewModel() {
    private val _screenState = MutableStateFlow(ScreenState(ScreenLoadingState.Success(), null))
    private val _state = MutableStateFlow(HomeScreenState(screenState = _screenState.value))

    internal val state = combine(_screenState, _state) { screenState, state ->
        state.copy(screenState = screenState)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, _state.value)
}
```

**`HomeScreen.kt`**

```kotlin
package org.starter.project.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.starter.project.ui.design.system.scaffold.SystemScaffold
import org.starter.project.ui.route.AppRouter
import org.starter.project.ui.shared.event.ScreenEvent

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    appRouter: AppRouter
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreenContent(state = state) { event: ScreenEvent ->
        HomeScreenEventHandler(event = event, viewModel = viewModel)
    }
}

@Composable
private fun HomeScreenContent(
    state: HomeScreenState,
    dispatch: (event: ScreenEvent) -> Unit
) {
    SystemScaffold(
        modifier = Modifier.fillMaxSize(),
        screenState = state.screenState,
        onClickErrorActionButton = { dispatch(HomeScreenEvent.OnClickErrorScreenAction) }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Hello, Compose Multiplatform Starter Template!")
        }
    }
}
```

`HomeScreen.kt` は `collectAsState()`（既知の逸脱）ではなく `collectAsStateWithLifecycle()` を使う。`androidx.lifecycle.compose.collectAsStateWithLifecycle` は `ui` モジュールが `libs.bundles.ui`（`androidx-lifecycle-runtime-compose` を含む）を `api` 公開しているため、追加の依存設定なしで使える（[guides/ui-layer.md](../guides/ui-layer.md) UI-14）。

`composeApp/feature/home/build.gradle.kts` はそのまま変更不要（`implementation(projects.composeApp.domain.service)` は次に Service を追加するまで一時的に未使用になるが、依存を消すとプレースホルダを卒業するたびに再度足す手間が増えるため残す）。

新しい画面（検索・一覧など）を実装する場合は、この4点セットを拡張する形で書き進める。手順は [add-feature-screen.md](./add-feature-screen.md) を参照。新しい API 連携を追加する場合は [add-data-source.md](./add-data-source.md) を参照。

## 検証

1. `grep -rn -i zenn --include=*.kt --include=*.kts --include=*.xml --include=*.md .` を実行し、`docs/` 配下全体（ガイド本文・ADR・索引の解説文を含む）を除いてヒットが0件であることを確認する。`docs/` 配下は設計ガイド・ADR・索引が根拠としてサンプルの実名に意図的に言及しているため、検証対象から除外する。
2. `./gradlew testAndroidHostTest` — 共有ユニットテストを実行する。data/zenn, domain/zenn のテストが削除されているため、以後は0件のテストが成功する状態になる（失敗しないことのみ確認する）。
3. `./gradlew :composeApp:app:linkDebugFrameworkIosSimulatorArm64` — iOS フレームワークのビルドが通ることを確認する（README 記載のコマンド）。
4. Android の起動確認は `.claude/skills/debug-run-android/skill.md` の手順に従う（起動中のエミュレーターに `HomeScreen` のプレースホルダ文言が表示されることを確認する）。
5. `git status` で削除・変更したファイルがステップ1〜4の一覧と過不足なく一致していることを確認する。

## よくある失敗と対処

| 症状 | 原因 | 対処 |
|---|---|---|
| `Project ':composeApp:data:zenn' not found` 等の Gradle エラー | `settings.gradle.kts` の `include` を消し忘れた | ステップ3の `settings.gradle.kts` diff を再確認する。`composeApp/build.gradle.kts` 自体は編集不要だが `settings.gradle.kts` は手動編集が必須。 |
| `ui` モジュールがコンパイルエラーになる（`Unresolved reference: Article`） | `base` の `data/model/zenn/` を消したのに `ui/shared/component/article/` を消し忘れた、またはその逆 | 両方をセットで削除する（ステップ1の「削除する個別ファイル・ディレクトリ」を参照）。 |
| `Koin.kt` のコンパイルが通らない、または起動時に `NoBeanDefFoundException` | `Koin.kt` に Zenn の import・バインドが残っている、または `HomeScreenViewModel` のコンストラクタとバインド定義がずれている | ステップ3の `Koin.kt` テンプレートと実ファイルを1行ずつ突き合わせる。プレースホルダの `HomeScreenViewModel` はコンストラクタ引数なしなので `viewModelOf(::HomeScreenViewModel)` のみで解決できる。 |
| iOS 側で `Info.plist` の変更が必要だと思い込む | Deep Link scheme（`cmp-starter`）や Bundle ID はパッケージ名変更（`ChangePackageName`）の管轄であり、サンプル削除では変更不要 | 本手順では `iosApp/iosApp/Info.plist` に触れない。scheme や Bundle ID を変える場合は README の `## How to Rename` を参照する。 |
| `feature:home` の `build.gradle.kts` から `domain.service` 依存を消してしまう | 「未使用だから消してよい」と誤判断 | 消さない。骨格として残し、次の機能追加時にそのまま使う。 |
| 削除後も `ArticleList`/`ArticlesPagingSource` の import だけが残ってビルドが壊れる | `ui/shared/component/article/` の削除と、それを参照していた `feature/home`・`feature/user` のファイル削除の順序がずれた | ステップ2はモジュール・ディレクトリ単位でまとめて実行し、部分的に消さない。 |
