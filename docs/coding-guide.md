# コーディングガイド

## 1. 共通スタイル

- **必須** ベースは Kotlin 公式スタイル（`kotlin.code.style=official`）。インデントは4スペース、ワイルドカード import は禁止する。
- **推奨** 複数行の関数引数・パラメータリスト・コンストラクタ引数の末尾に trailing comma を付けない。
- **必須** 関数名は `fetchXxx`＝リモート取得（`suspend`）、`getXxx`＝ローカル同期取得、`updateXxx`＝更新、で意味を使い分ける。
- **必須** パッケージは `org.starter.project.<layer>.<name>.<role>` の形式で、role（`repository`/`converter`/`datasource.api.response` 等）まで一貫して細分化する。
- **必須** クラス命名・配置は下表に従う。`UseCase` という命名は使わない。

| 役割 | 命名 | 配置 |
|---|---|---|
| Repository interface | `XxxRepository` | `data/repository` |
| Repository 実装 | `XxxRepositoryImpl` | `data/<source>/repository` |
| Service interface | `XxxService` | `domain/service` |
| Service 実装 | `XxxServiceImpl` | `domain/<name>` |
| API interface（Ktorfit） | `XxxApi` | `data/<source>/datasource/api` |
| レスポンス DTO | `XxxResponse` | `data/<source>/datasource/api/response` |
| DTO→domain モデル変換 | `XxxConverter` | `data/<source>/converter` |
| ローカル設定 | `XxxPreferences`/`XxxPreferencesImpl` | `data/<source>/datasource/preferences` |
| 画面（stateful・public） | `XxxScreen` | `feature/<name>` |
| 画面本体（stateless・private） | `XxxScreenContent` | `feature/<name>` |
| Preview 関数（private） | `<対象>Preview` | Preview 対象と同一ファイル |
| 汎用 UI プリミティブ | `SystemXxx` | `ui/design/system` |
| 設定値オブジェクト | `XxxConfig` | 使用モジュール直下 |

- **必須** 可視性は既定 public。feature 内部専用の型（State/Event/EventHandler 等）と DI を経由しない実装クラスは `internal`。テストのため公開が必要な内部ロジックは `@VisibleForTesting internal`。
- **必須** DTO・domain モデル・UiState は `data class`。ステートレスな変換は `object` + `operator fun invoke`。
- **必須** `@Immutable` は domain モデル（`base` の `data.model.<feature>`）と Compose に渡す UiState に付ける。DTO には付けない。
- **必須** `@OptIn` は使用箇所を含む最小の関数・プロパティ単位で付与する。`@file:OptIn` は使わない。
- **必須** `expect`/`actual` は `core`（HTTP エンジン）と `app`（`platformModule` 等）に限定する。UI から呼ぶプラットフォーム固有機能は interface + `app` 実装で配線する（→ decisions.md D-24）。
- **必須** ログは Napier のみを使う。`println`・`android.util.Log` は使わない。例外捕捉は `Napier.e`、ライフサイクル等の追跡は `Napier.d`、通信詳細は `Napier.v` を使う。
- **必須** 設定値は `internal object XxxConfig { const val ... }` に集約する。シークレット（API キー等）をクライアントに平文で埋め込まない。
- **必須** Compose Resources のリソースファイル名は `string.xml`（`strings.xml` ではない）。
- **必須** コードコメントは why（意図・制約・経緯）だけを書く。コードを読めば分かる what / how のコメントは書かず、コードを読んでも分からない暗黙知（外部 API の仕様上の制約、回避策の理由、あえて採らなかった実装など）を明文化する用途に限る。例外は次の TODO と、§5 のテストの `// arrange` `// act` `// assert` 区切り。
- **必須** TODO コメントは `// TODO: <何をすべきか>` の形式で、英語で書く。
- 本文・コメントは日本語で構わないが、識別子・ファイル名・コード・コミットメッセージは英語で書く。

## 2. data 層

モジュール固有の配置・テンプレは `composeApp/<layer>/<name>/AGENTS.md` を参照。

- **必須** Repository interface は `composeApp/data/repository` に置き、マーカー interface `Repository` を継承する。実装は `composeApp/data/<source>` に `XxxRepositoryImpl` として置く。
- **必須** Repository の公開メソッドは API 1 エンドポイント（またはローカルストレージの1操作）に対応させ、戻り値は素の domain モデルにする。`Result` や独自 sealed class でラップしない。
- **必須** Repository・DataSource・Converter 内で例外を `try/catch` しない。`withContext`/`Dispatchers` の切り替えも行わない。例外は `domain:service` の `ResultHandler` にそのまま伝播させる。
- **必須** リモート API は `datasource/api` パッケージに `interface XxxApi` として Ktorfit アノテーション（`@GET`/`@POST`/`@Query`/`@Path` 等）のみで定義する。実装は書かない（KSP がビルド時に `Ktorfit.createXxxApi(): XxxApi` を生成する）。Ktorfit で表現できない呼び出し（multipart 等）に限り、該当メソッドのみ手書き実装クラスに切り出してよい。
- **必須** レスポンス DTO は `datasource/api/response` パッケージに `@Serializable data class XxxResponse` として定義し、全フィールドを `Xxx? = null` にする。`@SerialName("snake_case")` を付与し、クラス名サフィックスは `Response` にする。
- **必須** DTO→domain モデル変換は `converter` パッケージの `object XxxConverter { operator fun invoke(...) }` に書く。拡張関数形式（`fun XxxResponse.toDomain()`）は使わない。
- **必須** 必須項目の取り出しは `response.field.validateNotNull("field_name")` を使う。一覧変換は `mapNotNull { try { createXxx(it) } catch (e: ConversionError) { null } }.orEmpty()` で1件ずつ変換失敗を許容し、リスト全体を失敗させない。
- **必須** ローカル設定は `datasource/preferences` パッケージに `interface XxxPreferences` + `class XxxPreferencesImpl(settings: Settings)` として定義し、`multiplatform-settings` の委譲プロパティで実装する。
- **必須** HttpClient の生成・設定は `core` の `ApiClientImpl` 1箇所に集約する。data モジュールで独自の `HttpClient` を生成しない。
- **必須** タイムアウト・リトライ・認証・キャッシュは要件が出るまで実装しない（→ decisions.md D-13）。
- **必須** ページングは Repository では扱わない。カーソル付き単発取得のみ提供し、`Pager`/`PagingSource` の組み立ては UI 層の責務にする（→ decisions.md D-05）。
- **必須** Repository/Service が `Flow` を返すのは継続監視が明示的要件のときのみ（→ decisions.md D-12）。
- **必須** 新規 data モジュールの `build.gradle.kts` は `id("kmp-library")` を適用し、必要な機能別プラグインのみ追加する。テストを持つモジュールには `alias(libs.plugins.mokkery)` も適用する。
- **推奨** Repository 実装クラス・Converter・Preferences 実装クラスは `public`（DI のコンストラクタ参照のため）。Converter 内のヘルパー関数は `@VisibleForTesting internal fun` にする。

判断基準:

| 状況 | 既定 |
|---|---|
| Repository を分割する単位 | 外部サービス（API）単位。肥大化してきたらドメイン単位の分割を検討する |
| リモート API とローカル設定の混在 | 小規模な付帯情報は同居してよい。キャッシュ/オフライン要件が出たら Remote/Local DataSource に分割する |
| リモート/ローカルキャッシュの併存 | リモート優先。本格的なオフラインファーストは対象外 |
| ページングの `Key` 型 | カーソル文字列方式なら `String`、オフセット/ページ番号方式なら `Int` |

```kotlin
interface XxxApi {
    @GET("api/xxx")
    suspend fun fetchXxx(@Query("page") page: String? = null): XxxResponse
}

@Serializable
data class XxxResponse(
    @SerialName("id") val id: Int? = null
)

object XxxConverter {
    operator fun invoke(response: XxxResponse): DomainModel {
        return DomainModel(id = response.id.validateNotNull("id"))
    }
}
```

```kotlin
class XxxRepositoryImpl(
    private val xxxApi: XxxApi
) : XxxRepository {
    override suspend fun fetchXxx(page: String?): DomainModel {
        return XxxConverter(xxxApi.fetchXxx(page))
    }
}
```

## 3. domain 層

モジュール固有の配置・テンプレは `composeApp/<layer>/<name>/AGENTS.md` を参照。

- **必須** Service interface は `domain:service` モジュールに置き、`Service` マーカー interface を継承する。実装は機能ごとの `domain:<name>` モジュールに `XxxServiceImpl` として置く。命名は `Service`（`UseCase` は使わない）。
- **必須** 1つの Service interface は1つの外部リソース/機能ドメインに対応する複数メソッドの集約とする。画面単位・操作単位で細分化しない。
- **必須** メソッドシグネチャは `suspend fun ...: Result<T>`（非同期）または `fun ...: Result<T>`（同期）で統一する。
- **必須** Service の各メソッド本体は必ず `ResultHandler.async {}` / `.immediate {}`（`Unit` は `asyncUnit`/`immediateUnit`）で包む。`async` は例外捕捉後に `ensureActive()` を呼びキャンセル例外を再送出してから `Result.failure` を返す。例外を `Result` に変換するのは Service 層だけの責務にする（→ decisions.md D-03）。
- **必須** Service（`domain:<name>`）は `data:repository` の Repository interface にのみ依存する。`data:<source>` には依存しない。feature/ViewModel も Service（`domain:service`）にのみ依存し、Repository を直接注入しない（→ decisions.md D-10）。
- **必須** フォールバック処理、複数 Repository を跨ぐ集約、副作用（永続化等）といったビジネスロジックは Service に書く。1つの Service メソッドが複数 Repository を必要とする場合はコンストラクタ注入で束ねる。
- **必須** domain モデルは `domain/*` モジュールではなく `base` の `org.starter.project.base.data.model.<feature>` パッケージに置く。`@Immutable data class` で non-null フィールドのみを持ち、DTO とは常に別型にする（→ decisions.md D-04）。
- **必須** 入力値の業務バリデーションは Service 層、レスポンスの整合性検証（必須フィールド欠落等）は data 層の Converter が担う（→ decisions.md D-14）。
- **必須** DI 登録は `composeApp/app` の `Koin.kt` に集約する。domain 側に DI モジュール定義ファイルを置かない。
- **必須**（新規決定） 複数画面・アプリ全体で共有する状態（カート、ログイン状態、テーマ設定等）は `domain:service` に状態保持 Service の interface（`StateFlow` 公開）を置き、実装が Preferences 系 Repository に永続化する。ViewModel を画面間で共有しない（→ decisions.md D-23）。
- **推奨** `@OpenForTesting open class` は、具象クラスを直接モックする必要があるときだけ Service 実装クラスに付ける。通常は interface をモックする。

判断基準:

| 状況 | 判断 |
|---|---|
| 既存 Service と同じ外部リソースへの取得パターンを追加する | 既存の `XxxService` interface にメソッドを追加する |
| 新しい外部リソース/機能ドメインを追加する | `domain:service` に新しい `YyyService` interface を追加し、実装は新規 `domain:yyy` モジュールに置く |
| 複数の外部リソースを1画面で業務ロジックとして合成する | 合成結果を表す新しい Service（例: `DashboardService`）を作り複数 Repository をコンストラクタ注入で束ねる（→ decisions.md D-11） |

```kotlin
class XxxServiceImpl(
    private val resultHandler: ResultHandler,
    private val xxxRepository: XxxRepository
) : XxxService {
    override suspend fun fetchXxx(id: String) = resultHandler.async {
        require(id.isNotBlank()) { "id must not be blank" }
        xxxRepository.fetchXxx(id)
    }
}
```

## 4. ui / feature 層

モジュール固有の配置・テンプレは `composeApp/<layer>/<name>/AGENTS.md` を参照。

- **必須** 1 feature モジュールは1機能ドメイン（ユーザーフロー）を表す。1画面ならパッケージ直下に `XxxScreen.kt`/`XxxScreenState.kt`/`XxxScreenEvent.kt`/`XxxScreenViewModel.kt` の4点セットを置く。複数画面なら `<screen>/` サブパッケージごとに4点セットを置く。
- **必須** `XxxScreen`（stateful・public）と `XxxScreenContent`（stateless・private）は同一ファイルに定義する。Content から ViewModel を直接呼ばず、すべて `dispatch(event)` → `XxxScreenEventHandler` 経由にする。
- **必須** UiState は `@Immutable internal data class` とし、先頭フィールドに共通 `ScreenState` を持つ `screenState: ScreenState` を含める。トップレベルフィールドが8を超えたらネストした `data class` にグルーピングする。
- **必須** ViewModel は `_screenState`/`_state` を `combine(...).stateIn(viewModelScope, SharingStarted.Eagerly, ...)` で読み取り専用公開する。状態更新は `update {}` のみで行い、直接代入しない。
- **必須** ViewModel のコンストラクタ注入は `domain:service` のインターフェースのみ（Repository 直接注入は禁止、→ decisions.md D-10）。画面は NavHost から `koinViewModel()` で ViewModel を取得して `XxxScreen` に渡す。
- **必須** ユーザー操作は `sealed interface XxxScreenEvent : ScreenEvent` として型定義し、同ファイルの `internal object XxxScreenEventHandler` の `when` 式で処理を集約する。
- **必須** 画面遷移は `XxxScreenEventHandler` が `appRouter.navigate(...)`/`popBackStack()` を直接呼ぶ。ViewModel のコンストラクタに `AppRouter` を注入しない（→ decisions.md D-16）。
- **必須** 一回性の通知（Snackbar 等）は `ScreenState.snackBarState` に値をセットし、表示後は ViewModel 側の関数で `null` に戻す。専用 Effect ストリーム（Channel/SharedFlow）は新設しない（→ decisions.md D-16）。現状は表示後のクリア配線が未実装のため、導入時は表示後コールバックから ViewModel の `clearSnackBar()` を呼ぶ配線を追加する。
- **推奨** StateFlow の収集は `collectAsStateWithLifecycle()` を使う。
- **必須** ローディング/エラー表示は `SystemScaffold` と共通 `ScreenLoadingState`（`Initial`/`Loading`/`Success`/`Failure`）で表現する。画面ごとに独自の Loading/Error sealed 型を作らない。
- **必須** ページングは `Pager` + 自作 `PagingSource` を `ui` モジュールの `shared/component/<domain>/` に置き、Screen 側で `collectAsLazyPagingItems()`、ViewModel 側で `.cachedIn(viewModelScope)` する。`Key` 型はカーソル文字列方式なら `String`、オフセット方式なら `Int` にする。
- **推奨** `LazyColumn`/`LazyListScope` の各要素には安定した ID を `key` に指定する（index 連結は既知の逸脱）。
- **必須** 共通コンポーネントは「ドメインモデル依存の有無」×「再利用範囲」で置き分ける（判断基準表参照）。`design/system/`・`shared/component/` 配下の Composable は `modifier: Modifier = Modifier` を第一引数に持つ。
- **必須** ViewModel/`LazyPagingItems` 等の外部依存を持たない stateless な末端コンポーネントには `@Preview` を付ける。`@Preview` 関数は対象と同一ファイルに `private fun <Component>Preview()` として定義し、`SystemTheme { }` でラップする。
- **必須** 表示文言はすべて Compose Resources に切り出す。ベース言語は日本語に統一する（→ decisions.md D-21）。他モジュールから参照される共通モジュール（例: `ui`）は `compose.resources { publicResClass = true }`、feature 単体モジュールは `false`（既定）にする。
- **必須** テーマはアプリのルートで一度だけ `SystemTheme { }` にラップし、以降は `SystemTheme.colors`/`typography`/`shapes` を参照する。Material 2 を維持し、Material 3 への移行は人間が判断する（→ decisions.md D-20）。
- **必須** `ui`・`feature:*` モジュールに `expect`/`actual` を置かない。UI から呼ぶプラットフォーム固有機能は interface を `ui` に置き、`app` の `platformModule` で実装を注入する（→ decisions.md D-24）。
- **必須** ViewModel のテストから直接呼びたい内部ロジックは `private` にせず `@VisibleForTesting internal fun` として公開する。

判断基準（共通コンポーネントの置き場所）:

| 条件 | 置き場所 | 命名 |
|---|---|---|
| ドメインモデルに依存しない汎用 UI プリミティブ | `ui` の `design/system/<category>/` | `System` 接頭辞 |
| ドメインモデルに依存し2箇所以上で再利用される | `ui` の `shared/component/<domain>/` | 接頭辞なし |
| 画面専用で他画面から再利用されない | 各 feature の `component/` | 接頭辞なし |

```kotlin
@Immutable
internal data class XxxScreenState(val screenState: ScreenState)

internal sealed interface XxxScreenEvent : ScreenEvent {
    data object OnClickErrorScreenAction : XxxScreenEvent
}

internal val state = combine(_screenState, _state) { s, st ->
    st.copy(screenState = s)
}.stateIn(viewModelScope, SharingStarted.Eagerly, _state.value)
```

## 5. テスト

- **必須** テストダブルは Mokkery（`dev.mokkery`）に統一する。手書きの代替クラスは作らない（→ decisions.md D-06）。
- **必須** テスト必須範囲は Converter・Repository 実装・Service 実装・ViewModel とする。
- **必須** テストは対象実装モジュールの `commonTest` に、ソースとミラーのパッケージ構成で置く。`androidUnitTest`/`iosTest` は使わない。
- **必須** テストクラス名は `対象クラス名 + Test`、メソッド名は `<methodName>_<condition>_<expectedResult>`（lowerCamelCase を `_` で連結）にする。本体は `// arrange` `// act` `// assert` の3コメントで区切る。
- **必須** アサーションは `assertEquals(expected, actual)` の引数順で書く。
- **必須** モック対象は interface とする。具象クラスをモックする必要があるときだけ実装クラスに `@OpenForTesting open class` を付ける。
- **必須** Coroutine を含むクラスは `CoroutineDispatcher` をデフォルト引数付きコンストラクタで受け取り、テストでは `StandardTestDispatcher()` を注入して `runTest(testDispatcher)` を使う。
- **必須**（新規決定） ViewModel のテストは `Dispatchers.setMain(StandardTestDispatcher())` を `@BeforeTest`、`Dispatchers.resetMain()` を `@AfterTest` に置く。
- **必須** `commonTest.dependencies` を宣言したモジュールは、対応するテストファイルを最低1つ持つ。
- **必須** 全体実行は `./gradlew testAndroidHostTest`、モジュール単位は `./gradlew :composeApp:<layer>:<name>:testAndroidHostTest` を使う。

判断基準（`mock<T>()` と `MockMode.autofill`）:

| 状況 | モード |
|---|---|
| 呼び出す依存側メソッドを漏れなく `every`/`everySuspend` でスタブできる | 既定モード `mock<T>()` |
| 戻り値を定義せず `verify`/`verifySuspend` で「呼ばれたこと」だけ確認したいメソッドを含む | `mock<T>(MockMode.autofill)` |

```kotlin
private val mockXxxRepository = mock<XxxRepository>(MockMode.autofill)
private val testDispatcher = StandardTestDispatcher()
private val subject = XxxServiceImpl(ResultHandler(testDispatcher), mockXxxRepository)

@Test
fun fetchXxx_success_returnsXxx() = runTest(testDispatcher) {
    // arrange
    everySuspend { mockXxxRepository.fetchXxx(any()) } returns expected

    // act
    val actual = subject.fetchXxx("id")

    // assert
    assertEquals(Result.success(expected), actual)
}
```

## 6. Git

- **必須** コミットメッセージは Conventional Commits 形式 `<type>(<scope>): <Subject>` で書く。Subject は英語・先頭大文字・命令形・末尾ピリオドなしにする。
- **必須** 本文（body）で変更の「なぜ」を英語で説明する。Subject だけで自明な軽微な変更は body を省略してよい。
- **必須** AI（Claude Code）が作成したコミットの末尾には `Co-Authored-By` フッターを付ける。
- **必須** `main` ブランチへ直接コミットしない。必ずブランチを切り、PR 経由でマージする。
- **必須**（AI 作業） ブランチ名は `claude/<kebab-case-summary>` にする。
- **必須** PR タイトルはコミットメッセージと同じ形式にする。PR 本文は `## Summary`（箇条書き）/`## Verification`（実施した検証）で構成し、意図的に対応しなかった箇所は `## Intentionally left as-is` に書く。末尾に生成ツールフッターを付ける。
- **必須** PR は可能な限り小さく、1 PR = 1 つの目的、他の PR を読まなくてもレビューできる（コンテキストが閉じた）粒度にする。**推奨**（新規決定） 目安は変更モジュール 1〜2 個・差分 300 行以内。超えるなら分割を検討する。
- **必須** 依存関係のある連続した変更は GitHub の Stacked PR で分割する。手順: (1) 先行ブランチから後続ブランチを切る (2) `gh pr create --base <先行ブランチ>` で起票する (3) PR 本文冒頭に `Stack n/N: #A → #B → #C` を書く (4) 先行 PR のマージ後、後続 PR の base が `main` になっていることを確認し（切り替わっていなければ `gh pr edit <番号> --base main`）、`git rebase origin/main` する。例: データソース追加は 契約（`base`/`data:repository`/`domain:service`）→ 実装（`data/<source>`, `domain/<name>`）→ `app` 登録 の 3 段。
- **推奨**（新規決定） 同一ワークツリーに別セッション/別ブランチの未コミット変更がある状態で新しい作業を始める場合は `git worktree add -b <branch> .claude/worktrees/<name> main` で隔離する。`git stash` は全セッション/全ワークツリーで共有されるため使わない。
- **必須** 依存ライブラリの更新は安定版を優先する（→ decisions.md D-07）。更新コミットは scope=`deps`/`gradle` の実例形式に合わせる。

| type | 用途 |
|---|---|
| `feat` | 新機能の追加 |
| `fix` | バグ修正 |
| `refactor` | 挙動を変えないコードの再構成 |
| `chore` | ビルド成果物に影響しない雑務 |
| `docs` | ドキュメントのみの変更 |
| `build` | ビルド設定・依存関係の変更 |
| `style` | 挙動を変えないフォーマット・整形 |
| `test` | テストの追加・変更のみ |

```bash
git commit -m "$(cat <<'EOF'
<type>(<scope>): <Subject>

<なぜこの変更が必要かを英語で>

Co-Authored-By: Claude <model-name> <noreply@anthropic.com>
EOF
)"
```

### PR 前チェック

- **必須** 変更差分が1つの目的に閉じており、シークレットや `println` を含んでいないか確認する。
- **必須** 変更した層（data/domain/ui）が本書の該当章の規約に沿い、依存が契約モジュール経由になっているか確認する。
- **必須** 変更・追加した Converter/Repository 実装/Service 実装/ViewModel にテストがあり、`./gradlew testAndroidHostTest` がローカルで通ることを確認する。
- **必須** 8節の既知の逸脱を新規コードにコピーしていないか確認する。
- **必須** ガイド・`decisions.md` と矛盾する変更をした場合、同じ PR でドキュメントも更新する。
- **必須** UI を変更した場合、修正箇所ごとに before / after のスクリーンショットを表形式（修正箇所 | Before | After）で PR 本文の `## Verification` に添付する（手順は `.claude/skills/capture-screenshots`）。

## 7. 手順（skills）

手順は skills（`.claude/skills/<name>/SKILL.md`）を使う。型テンプレは本書と各モジュール直下の `AGENTS.md` を参照する。

| skill | 用途 | 主に触るモジュール |
|---|---|---|
| `add-feature-screen` | 新しい画面と feature モジュールを追加する | `feature/<name>`, `ui`, `app` |
| `add-data-source` | 新しい API/Preferences のデータソースと Repository/Service を追加する | `data/<source>`, `data/repository`, `domain/<name>`, `domain/service`, `app` |
| `remove-sample-code` | Zenn サンプル実装を削除して雛形化する | `data/zenn`, `domain/zenn`, `feature/home`, `feature/user` |
| `capture-screenshots` | UI 変更の before / after スクリーンショットを取得し、PR 本文用の比較表を作る | `feature/<name>`, `ui`（成果物は PR 本文） |

## 8. 既知の逸脱

- `feature/home`/`feature/user` の Screen は `collectAsState()` を使用している（`collectAsStateWithLifecycle()` 推奨から逸脱）。
- `feature/user` の表示文言が英語になっている（ベース言語=日本語の規約から逸脱）。
- 複数行引数リストの trailing comma が一部箇所（`feature/user` と `data/zenn` の DTO）に残存している。
- `UserProfile.kt` の `@Preview` 関数名が `UserProfileHeaderPreview`（`<Component>Preview` 規則から逸脱）。
- `SnackBarThrowableHandler` がどの ViewModel からも呼ばれておらず未配線。
- `articleList`（`ArticleList.kt` で定義された `LazyListScope` 拡張関数）の `items` の `key` が `"..._$index"` という index 連結になっている。
- `ArticlesPagingSource` の `catch` は到達不能（追加ページ失敗も全経路 `ErrorScreenThrowableHandler` を通るため）。
- `ResultHandler` の例外捕捉ログが `Napier.d`（`Napier.e` 推奨から逸脱）。
- `domain/service` モジュールにテストファイルが0件（Mokkery 適用済みだが未整備）。
- `base/build.gradle.kts` は UI を持たないが `kmp-compose-library` を適用している（`@Immutable` 利用のための意図的な例外であり逸脱ではない）。
- `data/zenn` の `ZennRepositoryTest`/`ZennServiceTest` で `MockMode.autofill` の使用有無が混在している（判断基準に沿った意図的な使い分けであり逸脱ではない）。
