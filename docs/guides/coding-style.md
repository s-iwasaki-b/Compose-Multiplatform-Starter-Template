# コーディングスタイルガイド

> 対象: Kotlin のコーディングスタイル（命名・可視性・型選択・`@OptIn`・`expect`/`actual`・ログ・設定値・コメント）を規定する。
> 関連: [testing.md](testing.md), [data-layer.md](data-layer.md), [domain-layer.md](domain-layer.md), [ui-layer.md](ui-layer.md), [module-guide.md](../architecture/module-guide.md), [ADR-0004](../decisions/0004-domain-models-in-base.md), [ADR-0015](../decisions/0015-uistate-structure.md), [ADR-0022](../decisions/0022-shared-component-placement.md)
> 最終確認コミット: ac56102

## 要点

- ベースは Kotlin 公式スタイル（`gradle.properties` の `kotlin.code.style=official`）。lint/formatter（detekt, ktlint, spotless, `.editorconfig`）は導入されていない。整合性はレビューで確認する。
- 複数行の関数引数・パラメータリスト・コンストラクタ引数の末尾にカンマを付けない（trailing comma なし）ことを推奨する。ワイルドカード import は禁止。
- クラス名・関数名・パッケージ名・ファイル名には固定の命名規約がある（下記表）。`UseCase` や `FakeXxx` は本テンプレートの規約にない命名なので使わない。
- 可視性は既定 public。feature 内部専用の型（State/Event/EventHandler 等）と、DI を経由しない実装クラスは `internal`。テストのために公開が必要な内部ロジックは `@VisibleForTesting internal`。
- DTO/domain モデル/UiState は `data class`、ステートレスな変換は `object` + `operator fun invoke`、`@Immutable` は domain モデルと Compose に渡す State に付ける。
- `@OptIn` は関数・プロパティ単位。`@file:OptIn` は使わない。
- `expect`/`actual` は `core`（HTTP エンジン）と `app`（`platformModule` 等）に限定する。
- ログは Napier のみ。`println`/`android.util.Log` は禁止。
- 設定値は `internal object XxxConfig { const val ... }` に集約する。シークレットはクライアントに埋め込まない。

## ルール

### フォーマット

| ID | 規約 | 規範 |
|---|---|---|
| STYLE-1 | `kotlin.code.style=official`（`gradle.properties`）に従う。 | **必須** |
| STYLE-2 | detekt/ktlint/spotless 等のフォーマッタ・静的解析は導入されていない。本ガイドとの整合はコードレビューで確認する。 | **事実**（ルールではない） |
| STYLE-3 | インデントは4スペース。 | **必須** |
| STYLE-4 | 複数行にわたる関数引数・パラメータリスト・コンストラクタ引数の末尾にカンマを付けない（trailing comma なし）。 | **推奨**（判断基準参照） |
| STYLE-5 | import はワイルドカードを禁止し、個別 import で書く。 | **必須** |
| STYLE-6 | 1行あたりの長さは100〜120字程度を目安にする。 | **推奨** |

### 命名

| ID | 規約 | 規範 |
|---|---|---|
| STYLE-7 | クラス命名は下表（後述）に従う。 | **必須** |
| STYLE-8 | 関数名は `fetchXxx`＝リモート取得（`suspend`）、`getXxx`＝ローカル同期取得、`updateXxx`＝更新、で意味を使い分ける。 | **必須** |
| STYLE-9 | パッケージは `org.starter.project.<layer>.<name>.<role>` の形式で、role（`repository`/`converter`/`datasource.api.response` 等）まで一貫して細分化する。 | **必須** |
| STYLE-10 | ファイル名は主クラス名と一致させる。1ファイルに主クラス＋密結合の小型クラス（DTO のネスト等）を許可する。 | **必須**（判断基準参照） |

### 可視性・型

| ID | 規約 | 規範 |
|---|---|---|
| STYLE-11 | 可視性の既定は public。`internal`/`@VisibleForTesting internal` の適用範囲は下記判断基準に従う。 | **必須** |
| STYLE-12 | DTO・domain モデル・UiState は `data class`。ステートレスな変換ロジックは `object` + `operator fun invoke`。振る舞いを持つ実装は `class`。 | **必須** |
| STYLE-13 | `@Immutable` は domain モデル（`base` の `data.model.<feature>`）と Compose に渡す UiState に付ける。 | **必須** |
| STYLE-14 | プロパティは `val` を優先する。永続ストレージの読み書き等、実体として書き換えが必要な場合のみ `var` を使う。 | **必須** |

### `@OptIn` / `expect`・`actual`

| ID | 規約 | 規範 |
|---|---|---|
| STYLE-15 | `@OptIn` は使用箇所を含む最小の関数・プロパティ単位で付与する。`@file:OptIn` は使わない。 | **必須** |
| STYLE-16 | `expect`/`actual` は `core`（HTTP エンジン）と `app`（`platformModule` 等のプラットフォーム初期化）に限定する。UI/feature/domain/data 層には置かない。UI から呼ぶプラットフォーム固有機能（共有シート等）は `expect`/`actual` ではなく interface + `app` 実装で配線する（[ADR-0024](../decisions/0024-platform-capabilities-via-app-module.md)、[ui-layer.md](ui-layer.md) UI-G16） | **必須** |

### ログ・設定値・コメント

| ID | 規約 | 規範 |
|---|---|---|
| STYLE-17 | ログは Napier のみを使う。`println`・`android.util.Log` は使わない。 | **必須** |
| STYLE-18 | ログレベルは、例外捕捉＝`Napier.e`、追跡（ライフサイクル等）＝`Napier.d`、通信詳細＝`Napier.v`、で使い分ける。 | **必須**（判断基準参照。新規決定。`Napier.e` の実例は本リポジトリに無い） |
| STYLE-19 | 設定値は `internal object XxxConfig { const val ... }` に集約する。 | **必須** |
| STYLE-20 | シークレット（APIキー等）をクライアントに平文で埋め込まない。 | **必須**（判断基準参照） |
| STYLE-21 | TODO コメントは `// TODO: <何をすべきか>` の形式で、英語で書く。 | **必須** |
| STYLE-22 | public な interface（Repository/Service 等の契約）には KDoc を書く。 | **推奨**（新規決定・実例なし） |

## 判断基準

### クラス命名表（STYLE-7）

| 役割 | 命名 | 配置 |
|---|---|---|
| Repository interface | `XxxRepository` | `data/repository` |
| Repository 実装 | `XxxRepositoryImpl` | `data/<source>/repository` |
| Service interface | `XxxService` | `domain/service` |
| Service 実装 | `XxxServiceImpl` | `domain/<name>` |
| API interface（Ktorfit） | `XxxApi` | `data/<source>/datasource/api` |
| レスポンス DTO | `XxxResponse` | `data/<source>/datasource/api/response` |
| DTO→domain マッパー | `XxxConverter` | `data/<source>/converter` |
| ローカル設定 interface/実装 | `XxxPreferences`/`XxxPreferencesImpl` | `data/<source>/datasource/preferences` |
| 画面（stateful, public） | `XxxScreen` | `feature/<name>` |
| 画面本体（stateless, private） | `XxxScreenContent` | `feature/<name>` |
| Preview 関数（private） | `<対象>Preview` | Preview 対象と同一ファイル |
| 汎用 UI プリミティブ（`design/system` 配下限定） | `SystemXxx` | `ui/design/system` |
| 設定値オブジェクト | `XxxConfig` | 値を使うモジュール直下 |

`FakeXxx` は使わない（[testing.md](testing.md) の方針によりテストダブルは Mokkery に統一するため、Fake クラス自体を作らない）。`UseCase` サフィックスも使わない（本テンプレートは `Service` に統一、[domain-layer.md](domain-layer.md) 参照）。

根拠（サンプルでの根拠。削除後は存在しない）: `ZennRepository`/`ZennRepositoryImpl`（`composeApp/data/repository`, `composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/repository/`）、`ZennService`/`ZennServiceImpl`（`composeApp/domain/service`, `composeApp/domain/zenn`）、`ZennApi`（`composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/datasource/api/ZennApi.kt`）、`ArticlesResponse`/`ArticleResponse`（`composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/datasource/api/response/ArticlesResponse.kt`）、`ArticlesConverter`/`UserConverter`、`ZennPreferences`/`ZennPreferencesImpl`、`HomeScreen`/`HomeScreenContent`、`UserProfileHeaderPreview`、`SystemScaffold`/`SystemSearchBar`/`SystemLoadingIndicator`、`ApiConfig`/`DeepLinkConfig`。

### ファイル分割の基準（STYLE-10）

| 状況 | 対応 |
|---|---|
| 型が単独で他モジュール・他ファイルから広く参照される | 別ファイルに分ける |
| DTO のネスト構造（親レスポンスに従属する子レスポンス）や domain モデルに従属する値オブジェクト | 同一ファイルにまとめてよい |

根拠（サンプルでの根拠。削除後は存在しない）: `composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/datasource/api/response/ArticlesResponse.kt` は `ArticlesResponse`・`ArticleResponse`・`ArticleUserResponse`・`ArticlePublicationResponse` の4クラスを1ファイルに持つ。`composeApp/base/src/commonMain/kotlin/org/starter/project/base/data/model/zenn/Articles.kt` は `Articles`・`Article` に加え、`Article` にネストした `Article.User`（`@Immutable data class`）を同一ファイルに持つ。

### trailing comma（STYLE-4）

複数行の引数・パラメータリストの大多数（実測131件）は trailing comma を付けていない。主要な実例（`ZennRepositoryImpl` のコンストラクタ、`ArticleResponse(...)` の呼び出し等）も付けない形であり、これを既定とする。ただし少数（実測29件）の trailing comma 付きコードが実在し、完全に一貫しているわけではない。内訳は29件中15件が `feature/user` モジュール、残りは `ui`/`app`/`base`/`data/zenn` に分散しており、単一モジュールへの偏りとまでは言えない。特に `composeApp/base/src/commonMain/kotlin/org/starter/project/base/extension/ResultExtension.kt` は同一ファイル内にほぼ同一シグネチャの `Result<Success>.handle` オーバーロードを2つ持つが、1つ目は trailing comma なし、2つ目は trailing comma ありで書かれており、単一ファイル内でも統一されていないことが確認できる。`kotlin.code.style=official` を適用した IntelliJ の既定フォーマット（trailing comma 設定を有効化しない状態）とも整合するため、**trailing comma を付けない形を推奨とする**（lint/formatter が無いため強制はできない）。（以下はサンプルでの根拠。削除後は存在しない）`feature/user` モジュール（`UserScreen.kt`, `UserScreenState.kt`, `UserProfile.kt` 等）や `composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/datasource/api/response/UserResponse.kt`、`composeApp/base/src/commonMain/kotlin/org/starter/project/base/data/model/zenn/User.kt`、`ResultExtension.kt` の2つ目のオーバーロードに見られる trailing comma 付きの箇所は、統一されていない既存コードとして扱う（新規コードでは trailing comma を付けない）。

### `internal` / `@VisibleForTesting internal` の適用範囲（STYLE-11, B-G12）

| 状況 | 可視性 |
|---|---|
| 他モジュールから参照される契約（interface）や DI（Koin）で解決される実装クラス（`XxxRepositoryImpl`, `XxxServiceImpl`, `XxxPreferencesImpl`, `ApiClientImpl` 等） | public |
| feature モジュール内部でのみ使う型（`XxxScreenState`, `XxxScreenEvent`, `XxxScreenEventHandler`） | internal |
| DI を経由せず、単一モジュール内の呼び出し元（`Composable` 等）からのみ生成される実装クラス（`AppRouterImpl` 等） | internal |
| HTTP エンジンや設定値のように、モジュール外に公開する必要のない `object`/`expect`/`actual` 宣言（`ApiConfig`, `expect val engine`） | internal |
| private にしたいが、テストから直接呼びたい内部ロジック（ViewModel の状態更新関数、Converter のヘルパー関数等） | `@VisibleForTesting internal` |

根拠（サンプルでの根拠。削除後は存在しない）: `ZennRepositoryImpl`/`ZennServiceImpl`/`ZennPreferencesImpl`/`ApiClientImpl` は public（Koin の `single<Interface> { Impl(...) }` で解決されるため）。`AppRouterImpl`（`composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/AppRouter.kt`）は `internal class`（Koin ではなく `rememberAppRouter` という `@Composable` 関数経由で `remember` により生成されるため）。`HomeScreenState`/`HomeScreenEvent`/`HomeScreenEventHandler`、`UserScreenState`/`UserScreenEvent`/`UserScreenEventHandler` は `internal`。`ApiConfig`（`internal object`）、`internal expect val engine`。`HomeScreenViewModel.updateScreenLoading`/`updateScreenSuccess`/`fetchArticles` や `ArticlesConverter.createArticle`/`UserConverter.createUser` は `@VisibleForTesting internal`（`androidx.annotation.VisibleForTesting` を KMP で流用）。

### `@Immutable` を付けるかどうか（STYLE-13）

| 型 | `@Immutable` |
|---|---|
| domain モデル（`base` の `data.model.<feature>`） | 付ける |
| UiState / `ScreenState` / `SnackBarState` / `ScreenLoadingState`（Compose に渡す状態） | 付ける |
| DTO（`XxxResponse`） | 付けない |

根拠（サンプルでの根拠。削除後は存在しない）: `Articles`/`Article`/`Article.User`/`User`（`composeApp/base/src/commonMain/kotlin/org/starter/project/base/data/model/zenn/`）と `HomeScreenState`/`UserScreenState`/`ScreenState` に `@Immutable` が付き、`ArticlesResponse`/`ArticleResponse` 等の DTO には付かない。DTO を持つ `data/<source>` モジュールは `@Immutable` を使わないため convention plugin は `kmp-library` でよく、domain モデルを持つ `base` と UiState を持つ `feature`/`ui` は `kmp-compose-library` を使う（`base` は UI を持たないが `@Immutable` 利用のための意図的な例外、[module-guide.md](../architecture/module-guide.md) 参照）。

### ログレベルの使い分け（STYLE-18, E-7）

| 状況 | レベル |
|---|---|
| 例外を捕捉した箇所（`try/catch`、`Result.failure` に変換する箇所） | `Napier.e` |
| ライフサイクルイベントなど、処理の追跡目的 | `Napier.d` |
| HTTP 通信の詳細（リクエスト/レスポンスの内容） | `Napier.v` |

現状 `ResultHandler.async`/`immediate`（`composeApp/domain/service/src/commonMain/kotlin/org/starter/project/domain/service/ResultHandler.kt`）は例外捕捉時に `Napier.d` を使っており、上記基準からは逸脱している（既知の逸脱。本ガイド適用後の新規コードでは `Napier.e` を使う）。（以下はサンプルでの根拠。削除後は存在しない）`ApiClient.kt` の Ktor `Logging` プラグイン経由の出力は `Napier.v` で基準どおり。`HomeScreen.kt` の `LifecycleEventEffect` は `Napier.d` で基準どおり。

### シークレット・設定値の扱い（STYLE-19, STYLE-20, E-8）

| 状況 | 対応 |
|---|---|
| baseURL のように秘匿不要な設定値 | `internal object XxxConfig { const val ... }` に定数として置く（`core` モジュール等）。 |
| APIキー等のシークレットをサーバー経由の呼び出しに変更できる | サーバーサイドプロキシを介し、クライアントにシークレットを持たせない（第一候補）。 |
| クライアントへの埋め込みがどうしても避けられない | 埋め込み方式（`local.properties` → `buildConfigField` 等）は本テンプレートで未決定。ただし埋め込む値を含むファイルは必ず `.gitignore` 対象にする（必須）。 |

APIキーの発行・埋め込みを提案する場面では、埋め込みのリスク（クライアントバイナリからの漏洩）をユーザーに説明したうえで判断を仰ぐ。本テンプレートの現行サンプル（Zenn API）は認証不要のため、シークレットを扱う実例はない。

### `@OptIn` の付与範囲（STYLE-15）

`@file:OptIn` は使わず、使用箇所を含む関数またはプロパティにのみ `@OptIn` を付ける。根拠（サンプルでの根拠。削除後は存在しない）: `HomeScreen.kt` の `private fun HomeScreenContent` にのみ `@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)`、`HomeScreenViewModel.kt` の `val articlesPagingFlow` プロパティにのみ `@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)`。クラス全体・ファイル全体への付与は無い。

## 実装パターン

### 設定値オブジェクト

```kotlin
// composeApp/core/src/commonMain/kotlin/org/starter/project/core/api/XxxConfig.kt
internal object XxxConfig {
    const val BASE_URL = "https://example.com/"
}
```

### ログ出力

```kotlin
import io.github.aakira.napier.Napier

try {
    // ...
} catch (e: Throwable) {
    Napier.e(throwable = e) { "failed to fetch Xxx" } // 例外捕捉
    throw e
}

Napier.d { "XxxScreen.onStart" } // 追跡ログ
Napier.v(message, null, "ApiClient") // 通信詳細
```

### `@VisibleForTesting internal` で内部ロジックを公開する

```kotlin
class XxxScreenViewModel(
    private val xxxService: XxxService
) : ViewModel() {
    @VisibleForTesting
    internal fun updateScreenLoading() {
        // ...
    }
}
```

### `@Immutable data class`（domain モデル / UiState）

```kotlin
// composeApp/base/src/commonMain/kotlin/org/starter/project/base/data/model/<feature>/Xxx.kt
@Immutable
data class Xxx(
    val id: Int,
    val name: String
)
```

### TODO コメント

```kotlin
// TODO: report error to your analytics
```

### KDoc（public interface、新規決定・実例なし）

```kotlin
/**
 * Xxx に関するデータ操作をまとめた契約。
 */
interface XxxRepository : Repository {
    suspend fun fetchXxx(id: Int): Xxx
}
```

## アンチパターン

- **ワイルドカード import を使う（`import org.starter.project.base.*`）** → STYLE-5 に反する。個別 import に書き換える。
- **新規コードで複数行の引数・パラメータリストに trailing comma を付ける** → STYLE-4（判断基準）で推奨した形式（trailing comma なし）と不統一になる。既存の `feature/user` 等の混在箇所を模倣しない。
- **`println`・`android.util.Log` を直接使う** → STYLE-17 に反する。Napier に統一する。
- **`@file:OptIn` でファイル全体に実験的 API の使用を許可する** → STYLE-15 に反する。意図しない実験的 API 使用が同一ファイルの他コードに紛れ込む。使用箇所の関数・プロパティにのみ付ける。
- **新しい UseCase 系クラスを `XxxUseCase` として作る、テストダブルを `MockXxx`/`FakeXxx` として作る** → STYLE-7 の命名表・[testing.md](testing.md) の方針と不整合になる。`XxxService`、Mokkery の `mock<T>()` を使う。
- **APIキーを `ApiConfig` のような `internal object` に平文の `const val` として埋め込む** → STYLE-20 に反する。サーバー経由に変更できないか検討し、それでも埋め込む場合は `.gitignore` 対象にする。
- **`feature` の `State`/`Event`/`EventHandler` を `public` にする** → STYLE-11 の可視性基準に反する。`internal` にする。
- **`expect`/`actual` を `feature`/`domain`/`data` 層に持ち込む** → STYLE-16 に反する。プラットフォーム分岐は `core`（HTTP エンジン）と `app`（`platformModule` 等）に閉じ込める。

## 参考実装（サンプル。削除後は存在しない）

- `composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/repository/ZennRepositoryImpl.kt` — trailing comma なしのコンストラクタ記法の実例（多数派のパターン）。
- `composeApp/base/src/commonMain/kotlin/org/starter/project/base/extension/ResultExtension.kt` — 同一ファイル内で trailing comma の有無が統一されていない実例（`Result<Success>.handle` の2つのオーバーロード）。
- `composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/datasource/api/response/ArticlesResponse.kt` — DTO を1ファイルに複数クラスまとめる実例。
- `composeApp/base/src/commonMain/kotlin/org/starter/project/base/data/model/zenn/Articles.kt` — domain モデルのネストクラスと `@Immutable` の実例。
- `composeApp/core/src/commonMain/kotlin/org/starter/project/core/api/ApiConfig.kt` — `internal object XxxConfig` の実例。
- `composeApp/domain/service/src/commonMain/kotlin/org/starter/project/domain/service/ResultHandler.kt` — `Napier.d` を使っている箇所（ログレベルの既知の逸脱）と TODO コメント形式の実例。
- `composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/AppRouter.kt` — DI を経由しない `internal class XxxImpl` の実例。
- `composeApp/feature/home/src/commonMain/kotlin/org/starter/project/feature/home/HomeScreenViewModel.kt` — `@VisibleForTesting internal` と `@OptIn` の最小スコープ付与の実例。
- `composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/converter/UserConverter.kt` — `@VisibleForTesting internal fun` の実例。

## チェックリスト

- [ ] インデント4スペース、ワイルドカード import が無いか
- [ ] 複数行の引数・パラメータリストに trailing comma が付いていないか
- [ ] クラス名が命名規約表（STYLE-7）に沿っているか（`UseCase`/`FakeXxx` を使っていないか）
- [ ] 関数名が `fetchXxx`/`getXxx`/`updateXxx` の意味区分に沿っているか
- [ ] `internal`/`@VisibleForTesting internal`/public の使い分けが判断基準に沿っているか
- [ ] domain モデルと UiState に `@Immutable` が付いているか（DTO には付けていないか）
- [ ] ログに Napier を使い、レベル（`e`/`d`/`v`）を使い分けているか
- [ ] 設定値を `internal object XxxConfig` に集約し、シークレットを平文で埋め込んでいないか（埋め込む場合は `.gitignore` 対象か）
- [ ] `@OptIn` を関数・プロパティ単位で付与しているか（`@file:OptIn` を使っていないか）
- [ ] `expect`/`actual` を `core`/`app` 以外の層に持ち込んでいないか
- [ ] TODO コメントが `// TODO: <何をすべきか>` 形式で英語になっているか
