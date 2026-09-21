# 設計ガイド

## 1. 全体像（層と依存方向）

```text
androidApp ─┐
            ├─→ app（集約シェル。全モジュールに依存）
iosApp ─────┘      │
 (Framework link)   ├─→ feature:<name> ─→ ui, domain:service
                     ├─→ data:<source> ─→ core, data:repository
                     ├─→ domain:<name> ─→ data:repository, domain:service
                     └─→ core, ui, data:repository, domain:service ─→ base
```

| 層 | 責務 | 依存してよい先 |
|---|---|---|
| base | 共有カーネル。domain モデル、共通エラー型（`ApiError`/`ConversionError`）、拡張関数 | なし |
| core | インフラ層。`ApiClient`（Ktor）、`PreferencesConfig` | base |
| data:repository | Repository interface のみを持つ契約モジュール | base |
| data:<source> | Repository 実装（外部データソース1つ分） | core, data:repository |
| domain:service | Service interface + `ResultHandler` を持つ契約モジュール | base |
| domain:<name> | Service 実装（機能ドメイン1つ分） | data:repository, domain:service |
| ui | 共有 UI 部品、`AppRoute`/`AppRouter` interface、エラーハンドラ | base |
| feature:<name> | 1機能ドメイン（ユーザーフロー）の画面一式 | ui, domain:service |
| app | DI 起動、Navigation 実装、エントリポイント | 全モジュール |

- **必須** 実装モジュール同士（`data:<source>` ↔ `domain:<name>`、`feature:<a>` ↔ `feature:<b>` 等）を直接依存させない。層を跨ぐ参照は契約モジュール（`data:repository`/`domain:service`）または `ui` を経由する。
- **必須** `app`（集約シェルを含む）だけが例外的に全実装モジュールへ依存してよい。
- ユーザー操作から画面描画までは、`EventHandler → ViewModel → Service（Result 化）→ Repository（例外透過）→ Converter → UiState → 描画` という一方向のデータフローを取る。

## 2. モジュール構成

- **必須** モジュールは `composeApp/<layer>/<name>`（`data`/`domain`/`feature`、2階層）または `composeApp/<name>`（`base`/`core`/`ui`/`app`、1階層）のいずれかに置く。
- **必須** convention plugin は「Compose UI（`@Composable`）または `compose.resources` を使うか」で機械的に選ぶ。使うなら `kmp-compose-library`、使わないなら `kmp-library`。`base` は `@Immutable` 利用のため例外的に `kmp-compose-library` を適用する。
- **必須** `gradle/libs.versions.toml` の `[bundles]` はレイヤー単位（`base`/`core`/`data`/`domain`/`ui`/`test`）で定義する。新規モジュールは既存の `libs.bundles.<layer>` を再利用し、モジュールごとの新規 bundle は作らない。
- **必須** モジュールが `composeResources/` を自ら持つ場合、他モジュール経由で transitively 利用可能でも `compose-components-resources`（または含む bundle）への直接依存を明示的に追加する。省略するとビルドが壊れる。
- **必須** ソースパッケージは `deriveNamespace(project)` が導出する `org.starter.project.<layer>.<name>` と一致させる。`app` のみ `app`/`navigation`/`di`/`log` の関心事別パッケージ分割を例外的に許可する。

`composeApp/build.gradle.kts`（集約シェル）は配下を動的スキャンして束ねる:

```kotlin
fun allSubProjects(action: (String) -> Unit) {
    projectDir.walk().maxDepth(3).filter {
        it != projectDir && it.isDirectory && it.resolve("build.gradle.kts").exists()
    }.forEach {
        val path = it.relativeTo(rootDir).path.replace(File.separator, ":")
        action(path)
    }
}
// commonMain.dependencies { allSubProjects { api(project(":$it")) } }
```

- 新規モジュールを追加しても `composeApp/build.gradle.kts` 自体の編集は不要。動的スキャンが自動検出する。
- `maxDepth(3)` は起点（`composeApp`、深さ0）から3世代下まで探索するため、`composeApp/<layer>/<name>`（深さ2）はもちろん3階層目（深さ3）も技術的には検出される。ただし本テンプレートは設計方針として2階層までに統一しており、4階層目（深さ4）は自動検出から実際に漏れる。

| モジュール | convention plugin |
|---|---|
| base | `kmp-compose-library`（例外的適用） |
| core, data:repository, data:<source>, domain:service, domain:<name> | `kmp-library` |
| ui, feature:<name>, app（composeApp 集約シェルを含む） | `kmp-compose-library` |
| androidApp | `project` + AGP + jetbrainsCompose |

新規モジュールの追加は「`settings.gradle.kts` への `include` → `build.gradle.kts` 作成 → ソース配置 → 利用元モジュールへの依存追加 → 必要なら DI 登録」の順で行う。手順は skills（`.claude/skills/add-data-source` 等）、実装テンプレは [coding-guide.md](coding-guide.md) を参照。

## 3. 層の境界と実装粒度

- **必須** `feature:<name>` は `ui` と `domain:service` にのみ依存する。`data` 層・実装モジュール（`data:<source>`/`domain:<name>`）には一切触れない。ViewModel への Repository の直接注入も禁止する。
- **必須** Repository interface は `data:repository` に置く。domain 側が interface を所有する古典的な依存関係逆転（DIP）は採用しない（→ decisions.md D-02）。
- **必須** Repository は外部データソース（API・ローカルストレージ）1つにつき1つ作る。公開メソッドはそのデータソースの1操作に対応させ、戻り値は素の domain モデルとする。例外は握りつぶさず、そのまま呼び出し元へ伝播させる（例外透過）。
- **必須** 例外→`Result` への変換は `domain:service` の `ResultHandler.async`/`immediate` に一元化する。Repository・Converter（一覧変換で1件ずつ許容する場合を除く）・ViewModel で `Result` を自前構築しない（→ decisions.md D-03）。
- **必須** Dispatcher の切り替え（`Dispatchers.IO` への `withContext`）は `ResultHandler` の1箇所でのみ行う。Repository・DataSource・Converter・ViewModel では `Dispatchers` を直接扱わない。
- **必須** Service interface は1つの外部リソース/機能ドメインに対応する複数メソッドの集約とする。1クラス1メソッドの UseCase パターンは採らない（→ decisions.md D-11）。
- **必須** 複数リソースを組み合わせる業務ロジック（フォールバック・合成・副作用）は Service に書く。複数 Repository が必要な場合は Service のコンストラクタ注入で束ねる。
- **必須** domain モデルは `domain/*` ではなく `base` の `org.starter.project.base.data.model.<feature>` に置き、DTO とは常に別型にする（→ decisions.md D-04）。

契約と実装の最小骨格:

```kotlin
// 契約（data:repository） / 実装（data:<source>）
interface XxxRepository : Repository { suspend fun fetchXxx(id: String): XxxModel }
class XxxRepositoryImpl(private val api: XxxApi) : XxxRepository {
    override suspend fun fetchXxx(id: String) = XxxConverter(api.fetchXxx(id))
}

// 契約（domain:service） / 実装（domain:<name>）
interface XxxService : Service { suspend fun fetchXxx(id: String): Result<XxxModel> }
class XxxServiceImpl(
    private val resultHandler: ResultHandler,
    private val repo: XxxRepository
) : XxxService {
    override suspend fun fetchXxx(id: String) = resultHandler.async { repo.fetchXxx(id) }
}
```

モジュール分割の粒度は層で非対称（→ decisions.md D-09）:

| 状況 | 判断 |
|---|---|
| 新しい外部データソースを追加する | `data:<source>` と `domain:<name>` を新設し、Repository/Service interface は既存の契約モジュールに追記する |
| 既存データソースを別画面で使うだけ | 新規実装モジュールは作らず、`feature:<name>` のみ追加・拡張する |
| 同一ユーザーフロー内の関連画面を追加する | 既存 `feature:<name>` にサブパッケージを追加する |
| 独立した機能ドメインの画面を追加する | 新しい `feature:<name>` を追加する |

## 4. DI（Koin）

- **必須** DI 定義は `composeApp/app` の `Koin.kt` の `startKoin` 1箇所に集約する。各実装モジュール内に独自の DI 定義ファイルを作らない（→ decisions.md D-08）。
- **必須** 層ごとに `val xxxModule = module { }` を分ける（`coreModule`/`dataSourceModule`/`repositoryModule`/`serviceModule`/`appModule`）。プラットフォーム差分は `expect val platformModule` + `actual` に閉じ込める。
- **必須** interface へバインドする際は `single<Interface> { Impl(...) }` のように型引数を明示する。
- **推奨** コンストラクタの全引数がそのまま解決できる場合は `singleOf(::Impl)` を使う。`get<X>()` の明示指定や式が必要な場合はラムダ形式 `single<Interface> { ... }` を使う。
- **必須** ViewModel のコンストラクタには `domain:service` の interface のみを注入する。Repository や下位コンポーネントを直接注入しない。
- **必須** ViewModel は `appModule` に `viewModelOf(::XxxScreenViewModel)` で登録する。取得は NavHost 側の `koinViewModel<XxxScreenViewModel>()` で行う。
- **必須** DI で解決される実装クラス（`XxxRepositoryImpl` 等）は `public` のまま維持する。`internal` にすると `app` から型解決できずコンパイルが壊れる。

```kotlin
val coreModule = module { single<ApiClient> { ApiClientImpl() } }
val dataSourceModule = module { single<XxxApi> { get<ApiClient>().ktorfit.createXxxApi() } }
val repositoryModule = module { single<XxxRepository> { XxxRepositoryImpl(get()) } }
val serviceModule = module {
    single { ResultHandler() }
    single<XxxService> { XxxServiceImpl(get(), get()) }
}
val appModule = module { viewModelOf(::XxxScreenViewModel) }

modules(platformModule, coreModule, dataSourceModule, repositoryModule, serviceModule, appModule)
```

プラットフォーム差分（例: `Settings.Factory` の実体）は expect/actual に閉じ込める:

```kotlin
expect val platformModule: Module
// actual（Android）: single<Settings.Factory> { SharedPreferencesSettings.Factory(androidContext()) }
// actual（iOS）    : single<Settings.Factory> { NSUserDefaultsSettings.Factory() }
```

複数の外部ホストを呼ぶ必要が生じた場合は、`ApiClientImpl` を `baseUrl` 引数化し、ホストごとに Koin の `named("<host>")` で `coreModule`/`dataSourceModule` に複数登録する。現状は未実装（→ decisions.md D-25）。

## 5. ナビゲーション

- **必須** ナビゲーションは型安全ルート（`navigation-compose`）を使う。文字列ルートは使わない。
- **必須** ルート定義 `AppRoute`（`sealed class` + ネストした `@Serializable data class`）と `AppRouter` interface（`navigate`/`popBackStack` の2メソッドのみ）は `ui` に置く。実装 `AppRouterImpl`/`rememberAppRouter` は `app` に internal で置く。
- **必須** 各ルートには同名ネストの `NavArgs`（`@Serializable data class`）を併設し、`val navArgs: NavArgs get() = NavArgs(...)` を用意する。Screen 関数へは `AppRoute` 本体ではなく `NavArgs` を渡す。
- **推奨** 引数を持たないルートは `NavArgs` を省略し `@Serializable data object Xxx : AppRoute()` として定義してよい。この場合 NavHost 側の `toRoute()` 呼び出しも省略できる。
- **必須** ルート引数はプリミティブ型のみ持たせる。オブジェクト全体は渡さず、遷移先の画面が Service から再取得する。
- **必須** 画面遷移のトリガーは常に `XxxScreenEventHandler`（Screen 層）が `appRouter.navigate(...)`/`popBackStack()` を直接・同期的に呼ぶ形で実装する。ViewModel のコンストラクタに `AppRouter` を注入しない（→ decisions.md D-16）。
- **必須** ディープリンクのスキームは `app` の `DeepLinkConfig.SCHEME` に定数で持つ。各 `composable<AppRoute.Xxx>` に対応する `navDeepLink` を設定する。
- **必須** アプリ未起動時に受けたディープリンクは `DeepLinkHandler` がキャッシュし、`listener` が設定された時点でリプレイする。
- **必須** ディープリンクのスキームを変更する場合、`BuildUtils.kt`（Android の manifest placeholder）・`DeepLinkConfig.SCHEME`・`iosApp/Info.plist` の3箇所を同時に更新する。

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
interface AppRouter {
    fun navigate(route: AppRoute)
    fun popBackStack()
}
```

## 6. エラーハンドリング

- **必須** 独自エラー型は `ApiError`（HTTP 由来）と `ConversionError`（変換由来）の2つの sealed class（`Throwable` を継承）とし、いずれも `base` に置く。
- **必須** HTTP ステータス→独自エラー型への変換は `core` の `ApiClientImpl` の `HttpResponseValidator` 1箇所に集約する。
- **必須** Repository・Converter（一覧変換で1件ずつ許容する場合を除く）は例外を透過する。`Result` 化は `domain:service` の `ResultHandler` だけの責務とする（→ decisions.md D-03）。
- **必須** `ResultHandler` の `catch (e: Throwable)` は `Result.failure` を返す前に `coroutineContext.ensureActive()` を呼び、`CancellationException` を再送出する。
- **必須** ViewModel は Service から返る `Result<T>` を `.handle(handler)` で UI 状態に変換する。`getOrThrow()`/`getOrNull()` を直接使って独自にハンドリングしない。
- **必須** エラー表示は用途に応じて4経路を使い分け、表示が不要な失敗は無視する経路（`IgnoreThrowableHandler`）を使う。1つの失敗に対して複数経路を同時に発火させない（→ decisions.md D-19）。

| 経路 | 用途 | 実装 |
|---|---|---|
| 全画面 `ScreenLoadingState.Failure` | 初回ロード失敗 | `.handle(ErrorScreenThrowableHandler(_screenState))` |
| Paging の `LoadState.Error`（**推奨**・新規決定） | 追加ページ失敗 | `Result.getOrThrow()` で例外を伝播させ PagingSource 側の `catch` が変換する。現状は全経路が `ErrorScreenThrowableHandler` 経由で、`ArticlesPagingSource` の `catch` は到達不能。 |
| Snackbar | 軽微・ユーザー操作起因の失敗 | `.handle(SnackBarThrowableHandler(_screenState))` |
| `UiState` のフィールド | フォーム入力のバリデーションエラー | `.handle { ... }` でフィールドへ直接マッピングし、入力欄直下に表示する（→ decisions.md D-14） |
| 結果を無視してよい失敗 | 前回入力値の復元など、失敗しても UX に影響しない読み取り | `.handle(IgnoreThrowableHandler())` |

設計意図としては初回ロードと追加ページの切り分けを Paging のページキー（`key == null` か否か）で判定する。現状はページ番号によらず `ErrorScreenThrowableHandler` を使うため、この切り分けは未実装。
