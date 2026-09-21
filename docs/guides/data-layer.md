# data 層ガイド

> 対象: data 層（Repository / DataSource / DTO / Converter / HttpClient）の設計判断とコーディング規約
> 関連: [error-handling.md](../architecture/error-handling.md), [domain-layer.md](domain-layer.md), [testing.md](testing.md), [overview.md](../architecture/overview.md), [module-guide.md](../architecture/module-guide.md), [dependency-injection.md](../architecture/dependency-injection.md)
> 最終確認コミット: ac56102

## 要点

- Repository interface は `composeApp/data/repository` に置き、マーカー interface `Repository` を継承する。実装は `composeApp/data/<source>` に `XxxRepositoryImpl` として置く。
- Repository の公開メソッドは API エンドポイント（またはローカルストレージの1操作）単位。戻り値は素の domain モデルで、例外は握りつぶさずそのまま伝播させる（`Result` にしない、`withContext` しない）。
- リモート API は Ktorfit interface（`@GET`/`@Query`/`@Path`）で定義し、実装は書かない。KSP がビルド時に `Ktorfit.createXxxApi(): XxxApi` を生成する。
- レスポンス DTO は全フィールド `Xxx? = null`。実運用障害の再発防止策として確立した規約であり、省略してはならない。
- DTO → domain モデル変換は `object XxxConverter { operator fun invoke(...) }`。必須項目は `validateNotNull`、一覧変換は `mapNotNull` + `try/catch(ConversionError)` で1件ずつ許容する。
- HttpClient の生成・設定は `composeApp/core` の `ApiClientImpl` 1箇所のみ。タイムアウト・リトライ・認証は要件が出るまで実装しない。
- ページングは Repository では扱わない。カーソル付き単発取得のみ提供し、Paging3 の組み立ては UI 層の責務。
- キャッシュは要件が出るまで実装しない。Flow を返すのは継続監視が明示的要件のときだけ。
- テストは Converter と Repository 実装に必須。書き方・モック（Mokkery）の使い方は [testing.md](testing.md) を参照。

## ルール

| ID | 規範 | ルール |
|---|---|---|
| DATA-1 | 必須 | Repository interface は `composeApp/data/repository` モジュールに置き、空のマーカー interface `Repository` を継承する。 |
| DATA-2 | 必須 | Repository 実装クラスは `composeApp/data/<source>/src/commonMain/kotlin/org/starter/project/data/<source>/repository/` パッケージに置き、`XxxRepositoryImpl` と命名して対応する `XxxRepository` を implements する。 |
| DATA-3 | 必須 | Repository の公開メソッドは API 1エンドポイント（またはローカルストレージの1操作）に対応させ、戻り値は素の domain モデル（`base` の型）とする。`Result` や独自 sealed class でラップしない。 |
| DATA-4 | 必須 | Repository 実装内で例外を `try/catch` しない。例外は呼び出し元（`domain:service` の `ResultHandler`）にそのまま伝播させる（詳細は [error-handling.md](../architecture/error-handling.md)）。 |
| DATA-5 | 必須 | Repository / DataSource / Converter 内で `withContext` や `Dispatchers` の切り替えを行わない。Dispatcher 切替は `ResultHandler` の1箇所のみ。 |
| DATA-6 | 必須 | リモート API は `datasource/api` パッケージに `interface XxxApi` として Ktorfit アノテーション（`@GET`/`@POST`/`@Query`/`@Path` 等）のみで定義する。実装クラスを手書きしない（KSP が `Ktorfit.createXxxApi(): XxxApi` を生成する）。例外は DATA-22（Ktorfit アノテーションで表現できない呼び出しのみ）。 |
| DATA-7 | 必須 | API モジュールの `build.gradle.kts` に `alias(libs.plugins.google.devtools.ksp)` と `alias(libs.plugins.ktorfit)` を追加する。DTO を持つモジュールには `alias(libs.plugins.jetbrains.kotlin.serialization)` も追加する。 |
| DATA-8 | 必須 | ローカル設定は `datasource/preferences` パッケージに `interface XxxPreferences` + `class XxxPreferencesImpl(settings: Settings) : XxxPreferences` として定義し、`multiplatform-settings` の委譲プロパティ（`by settings.nullableString(...)` 等）で実装する。キーは `core` の `Preference` enum（`composeApp/core/src/commonMain/kotlin/org/starter/project/core/preferences/PreferencesKey.kt`）に追加する。新しい Preferences 名前空間（`Settings` インスタンス）を追加する場合は `composeApp/core/src/commonMain/kotlin/org/starter/project/core/preferences/PreferencesConfig.kt` にプロパティを追記する。 |
| DATA-9 | 必須 | レスポンス DTO は `datasource/api/response` パッケージに `@Serializable data class XxxResponse` として定義し、**全フィールドを `Xxx? = null` にする**（省略しない。§ 判断基準・アンチパターン参照）。 |
| DATA-10 | 必須 | DTO の各フィールドには `@SerialName("snake_case")` を付与する。クラス名サフィックスは `Response`（`Dto`/`Entity` は使わない）。ネストする DTO は親の文脈語を前置する（例: `ArticleUserResponse`）。 |
| DATA-11 | 必須 | DTO → domain モデル変換は `converter` パッケージの `object XxxConverter { operator fun invoke(response: XxxResponse): DomainModel }` に書く。拡張関数形式（`fun XxxResponse.toDomain()`）は使わない。一覧を返す API では、レスポンス DTO 自体を一覧を包むラッパー型（例: `XxxListResponse { val items: List<XxxItemResponse>?, val nextPage: String? }`）にし、Converter の `invoke` はそのラッパー DTO 1つを受け取りラッパー domain モデル（例: `XxxList`）1つを返す。`List<XxxResponse>` を `invoke` の引数に直接取らない。 |
| DATA-12 | 必須 | 必須フィールドの取り出しは `response.field.validateNotNull("field_name")` を使う。null なら `ConversionError.ResponseNotNullValidation` が送出される。 |
| DATA-13 | 必須 | 一覧を扱う Converter（DATA-11 のラッパー型パターン）は、ラッパー DTO 内の `items: List<XxxItemResponse>?` を `.mapNotNull { try { createXxx(it) } catch (e: ConversionError) { null } }.orEmpty()` で1件ずつ変換失敗を許容し、リスト全体を失敗させない。単体変換（1件のみを扱う変換）は `validateNotNull` の例外をそのまま伝播させてよい。 |
| DATA-14 | 必須 | HttpClient の生成・`ContentNegotiation`/`Logging`/`HttpResponseValidator` の設定は `composeApp/core` の `ApiClientImpl` 1箇所に集約する。data モジュールで独自の `HttpClient` を生成しない。 |
| DATA-15 | 必須 | baseUrl 等の設定値は `internal object XxxConfig { const val ... }` にまとめる（例: `core` の `ApiConfig`）。 |
| DATA-16 | 必須 | タイムアウト・リトライ・認証（Ktor の `HttpTimeout`/`HttpRequestRetry`/`Auth` プラグイン）は要件が明確になるまで追加しない。追加する場合は `core` の `ApiClientImpl` の `HttpClient { ... }` ブロックに1箇所だけ `install` する。 |
| DATA-17 | 必須 | ページングを伴う API でも Repository は単発取得のみを提供する（`suspend fun fetchXxx(..., page: String? = null): Xxx` の形で、戻り値にカーソル `nextPage` を含める）。`Pager`/`PagingSource`/`Flow<PagingData<T>>` の組み立ては UI 層（`composeApp/ui`）に置く。 |
| DATA-18 | 必須 | Repository/Service が `Flow` を返すのは、値の変化を継続的に観測する要件が明示されているときのみ（[ADR-0012](../decisions/0012-flow-only-for-observation.md)、[domain-layer.md](domain-layer.md) DOM-4 と規範レベルを統一）。単発取得や「呼ばれた時点の1回だけ必要」な値は `suspend fun`（非同期）または同期関数で返す。 |
| DATA-19 | 必須 | 新しい data モジュールの `build.gradle.kts` は convention plugin `id("kmp-library")` を必須で適用し、そのモジュールが必要とする機能別プラグインのみを追加する。依存は `commonMain.dependencies { api(...) / implementation(...) }` と `commonTest.dependencies { implementation(libs.bundles.test) }` の2ブロックで完結させる（§ 実装パターン参照）。 |
| DATA-20 | 必須 | テストを持つモジュールの `build.gradle.kts` には `alias(libs.plugins.mokkery)` を適用する。テストダブルは Mokkery で統一し、手書き Fake は使わない（[testing.md](testing.md) 参照）。 |
| DATA-21 | 推奨 | 可視性: Repository 実装クラス・Converter・Preferences 実装クラスは `public`（DI のコンストラクタ参照のため）。設定値は `internal object`。Converter 内のヘルパー関数はテストから呼べるよう `@VisibleForTesting internal fun` にする。 |
| DATA-22 | 任意（新規決定） | Ktorfit アノテーションで表現できない呼び出し（`multipart` 等）に限り、`XxxApi` interface の当該メソッドを手書き実装クラス `XxxApiImpl(private val client: HttpClient)` で実装してよい（DATA-6 の例外）。この場合 `HttpClient` を利用できるよう、`composeApp/core/src/commonMain/kotlin/org/starter/project/core/api/ApiClient.kt` の `ApiClient` interface に `val httpClient: HttpClient` を追加して公開する（現状未実装の差分）。 |

## 判断基準

新しい API/データソースを追加するときに迷う分岐を、レポートのギャップ項目（B-G1〜B-G12, B-G14）に加え、レビューで洗い出した追加項目（B-G15〜B-G17）ごとに整理する。

| # | 問い | 既定（if-then） | 理由 |
|---|---|---|---|
| B-G1 | Repository を分割する単位は？ | 既定は **外部サービス（API）単位**。DI 定義（`single<XxxApi>` → `single<XxxRepository>` の1:1対応）を崩さないため。1つの Repository が肥大化してきたら概念（ドメイン）単位に分割を検討する。 | 現行の DI 構成（`Koin.kt`）が API 1本 = Repository 1本の対応を前提にしている。 |
| B-G2 | リモート API とローカル設定を同一 Repository に混在させてよいか？ | 検索キーワードの記憶のような**小規模な付帯情報は同居してよい**（既定）。キャッシュ戦略やオフライン対応が要件になったら Remote/Local DataSource に分割し、Repository がそれらを束ねる設計に切り替える。 | 現状の規模で `RemoteXxxRepository`/`LocalXxxRepository` に先取りで分割すると過剰設計になる。 |
| B-G3 | リモートとローカルキャッシュを両方持つ Repository をどう設計するか？ | **リモート優先**を既定にし、ローカルは設定値などの補助データに限定する。本格的なオフラインファースト（Single Source of Truth をローカル DB にする等）が必要になった場合は本ガイドの範囲外として別途設計する。 | 軽量な KMP スターターの前提で、オフラインファーストの実例・要件が現状ない。 |
| B-G4 | API レスポンスをキャッシュすべきか？ | **キャッシュなしを既定**とする（ADR-0013）。必要になった時点で `core` にキャッシュユーティリティを追加する。 | 先取りでキャッシュ層を作ると過剰設計になりやすい。 |
| B-G5 | ページングは Repository/domain/UI のどの層で扱うか？ | **Repository/Service はページングに関与しない**。単発取得＋カーソルのみ提供し、`Pager`/`PagingSource` の組み立ては UI 層で行う（DATA-17、ADR-0005）。カーソルの型はオフセット/ページ番号方式なら `Int`、文字列カーソル方式なら `String` とし、Repository のメソッド引数・戻り値の「次のキー」もその型に統一する（新規決定。詳細は [ui-layer.md](ui-layer.md) Paging 節）。 | 実例（`ZennRepository.fetchArticles` + UI 層の `ArticlesPagingSource`）が既に明確な層分離を示している（サンプルでの根拠。削除後は存在しない）。 |
| B-G6 | エラー型をどこまで拡張するか？ | 汎用的な HTTP 分類（404/5xx/ネットワーク到達不可等）は `ApiError` に追加する。ビジネス固有のエラー（例: 「該当ユーザーが見つからない」）は `ConversionError` に倣った別の sealed class をドメインモジュールに置く。詳細は [error-handling.md](../architecture/error-handling.md) の ERR-2。 | `ApiError` は現状 `Unauthorized`/`Unknown` の2種類のみで、コード中コメントで「一例」と明言されている。 |
| B-G7 | DTO と domain モデルの構造がほぼ同一でも分離すべきか？ | **必ず分離する**（例外なし）。DTO を使い回さない。 | 既存コードは例外なく分離しており、`base` の domain モデルに `kotlinx.serialization` の import が一切ない。API 変更に対する耐性が高い。 |
| B-G8 | Converter は `object` + `invoke` 形式か拡張関数形式か？ | **`object` + `operator fun invoke` 形式を既定**とする（DATA-11）。 | 既存2例（`ArticlesConverter`, `UserConverter`）が同一形式で統一されており、テストコードも `subject(response)` という呼び出し方に依存している（サンプルでの根拠。削除後は存在しない）。 |
| B-G9 | Repository が `Flow` を返すべき条件は？ | **継続監視が明示的要件のときのみ** `Flow`。それ以外は `suspend fun`/同期関数（DATA-18、ADR-0012）。 | `getLastKeyword()` のような「起動時に1回だけ読む」値まで `Flow` 化する必然性がない。 |
| B-G10 | 認証が必要な API を追加する場合、トークンをどこで付与するか？ | Ktor の `Auth`/`Bearer` プラグインを `core` の `ApiClientImpl` に追加し、トークンプロバイダーを DI で注入する（既存の `install {}` パターンに統合）。トークンの永続化は `ZennPreferences` と同様に `multiplatform-settings` を使う専用 Preferences クラスに置く。Repository・API interface を認証の詳細から隔離する。 | `ApiClientImpl` は全 API 共通の `HttpClient` を1つだけ生成する設計であり、認証もここに追加するのが既存構造と整合する。（新規決定） |
| B-G11 | リトライ・タイムアウトの既定値は？ | **既定では設定しない**（DATA-16）。「明示的な設定がない＝無制限」という現状の落とし穴を認識した上で、要件が出たら `core` の `ApiClientImpl` に `HttpTimeout`/`HttpRequestRetry` を追加する。 | `ApiClientImpl` は全 API 共通の設定を1箇所に持つ設計であり、追加する場合も同じ場所に置くのが既存構造と整合する。（新規決定、ADR-0013） |
| B-G12 | Repository 実装クラス・Converter を `internal` にすべきか？ | **`public`（デフォルト）のまま**にする（DATA-21）。DI 集約先が `composeApp/app` の1箇所である現行アーキテクチャでは、`internal` にすると別モジュールからコンストラクタ参照できず破綻する。将来 DI 定義を各モジュールに分散させる設計に切り替える場合のみ `internal` 化とセットで検討する。 | `single<ZennRepository> { ZennRepositoryImpl(get(), get()) }` が `composeApp/app` から直接コンストラクタ参照している。 |
| B-G14 | KSP が生成する API 実装関数の命名規則は？ | `interface XxxApi` を定義すれば、KSP がビルド時に `Ktorfit.createXxxApi(): XxxApi` という拡張関数を自動生成する。この関数はリポジトリ全体を検索しても定義が見つからない（生成コードのため）。手動で実装クラスを書いてはならない。 | 新規 API 追加時に最も誤りやすい箇所（二重定義エラーや存在しないシンボルの探索）のため明文化する。 |
| B-G15 | 既存と別ホストの API を追加する場合、`ApiClientImpl`/DI 登録をどうするか？ | [ADR-0025](../decisions/0025-multi-host-api-clients.md) の既定に従う。`ApiClientImpl` を `baseUrl` 引数化し、Koin の `named("<host>")` でホストごとに複数登録する（現状未実装の差分）。 | 現状の `ApiClientImpl` は `defaultRequest { url(ApiConfig.API_BASE_URL) }` で単一ホストに固定されている。 |
| B-G16 | Ktorfit アノテーションで表現できない呼び出し（multipart 等）はどうするか？ | 該当メソッドのみ `XxxApiImpl(private val client: HttpClient)` として手書き実装する（DATA-22、DATA-6 の例外）。`ApiClient.httpClient` の公開が前提（現状未実装の差分）。 | Ktorfit は `@Multipart`/`@Part` 相当の表現力を本リポジトリのバージョンで検証しておらず、無理に既存パターンへ寄せると実装できないケースがあるため例外を設ける。 |
| B-G17 | オフセット/ページ番号方式のページングで `Pager` の `Key` 型はどうするか？ | `PagingSource<Key, Value>` の `Key` はカーソル文字列方式なら `String`、ページ番号方式なら `Int` にする。Repository のメソッドはそのカーソル型に合わせた引数を受け取り、戻り値に「次のキー」を含める（新規決定）。 | 既存の `ArticlesConverter`/`ArticlesPagingSource` はカーソル文字列 `String` 固定のため、オフセット方式を採用する場合の型選択基準が別途必要（サンプルでの根拠。削除後は存在しない）。 |

## 実装パターン

### パッケージ階層テンプレート

```text
composeApp/data/<source>/src/commonMain/kotlin/org/starter/project/data/<source>/
  datasource/
    api/
      <Source>Api.kt                  # Ktorfit interface（実装は書かない）
      response/
        <Xxx>Response.kt              # DTO（全フィールド nullable + = null）
    preferences/
      <Source>Preferences.kt          # interface + Impl
  converter/
    <Xxx>Converter.kt                 # object + operator fun invoke
  repository/
    <Source>RepositoryImpl.kt         # XxxRepository の実装

composeApp/data/repository/src/commonMain/kotlin/org/starter/project/data/repository/
  <Source>Repository.kt               # interface。Repository を継承
```

### Repository interface（`composeApp/data/repository`）

```kotlin
package org.starter.project.data.repository

import org.starter.project.base.data.model.<domain>.DomainModel

interface XxxRepository : Repository {
    suspend fun fetchXxx(param: String? = null, page: String? = null): DomainModel
    // ローカル設定など、リモート API を持たない1操作を混在させてよい（B-G2 参照）
    fun getLastXxx(): String?
    fun updateLastXxx(value: String)
}
```

### Repository 実装（`composeApp/data/<source>`）

```kotlin
package org.starter.project.data.<source>.repository

import org.starter.project.base.data.model.<domain>.DomainModel
import org.starter.project.data.repository.XxxRepository
import org.starter.project.data.<source>.converter.XxxConverter
import org.starter.project.data.<source>.datasource.api.XxxApi
import org.starter.project.data.<source>.datasource.preferences.XxxPreferences

class XxxRepositoryImpl(
    private val xxxApi: XxxApi,
    private val xxxPreferences: XxxPreferences
) : XxxRepository {
    override suspend fun fetchXxx(param: String?, page: String?): DomainModel {
        return XxxConverter(xxxApi.fetchXxx(param, page))
    }

    override fun getLastXxx(): String? = xxxPreferences.lastXxx

    override fun updateLastXxx(value: String) {
        xxxPreferences.lastXxx = value
    }
}
```

### DataSource: Ktorfit API

```kotlin
package org.starter.project.data.<source>.datasource.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import org.starter.project.data.<source>.datasource.api.response.XxxResponse

interface XxxApi {
    @GET("api/xxx")
    suspend fun fetchXxx(
        @Query("param") param: String? = null,
        @Query("page") page: String? = null
    ): XxxResponse

    @GET("api/xxx/{id}")
    suspend fun fetchXxxById(@Path("id") id: String): XxxResponse
}
```

DI での取得は `Ktorfit.createXxxApi()`（KSP 生成、手書き不要）:

```kotlin
single<XxxApi> { get<ApiClient>().ktorfit.createXxxApi() }
```

### 例外: Ktorfit で表現できない呼び出し（DATA-22、現状未実装の差分）

multipart 送信などで Ktorfit アノテーションでは表現できないメソッドのみ、`XxxApi` の該当メソッドを手書き実装クラスに切り出す。他のメソッドは通常どおり `interface XxxApi` に残す。

```kotlin
package org.starter.project.data.<source>.datasource.api

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class XxxApiImpl(private val client: HttpClient) {
    suspend fun uploadXxx(bytes: ByteArray, fileName: String): HttpResponse {
        return client.post("api/xxx/upload") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("file", bytes, Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        })
                    }
                )
            )
        }
    }
}
```

`HttpClient` を利用するため、`ApiClient` interface（`composeApp/core/src/commonMain/kotlin/org/starter/project/core/api/ApiClient.kt`）に `val httpClient: HttpClient` を追加し、`ApiClientImpl` で公開する（現状未実装の差分）。DI 登録は次のように行う。

```kotlin
single { XxxApiImpl(get<ApiClient>().httpClient) }
```

### DataSource: Preferences

```kotlin
package org.starter.project.data.<source>.datasource.preferences

import com.russhwolf.settings.Settings
import com.russhwolf.settings.nullableString
import org.starter.project.core.preferences.Preference

interface XxxPreferences {
    var lastXxx: String?
}

class XxxPreferencesImpl(settings: Settings) : XxxPreferences {
    override var lastXxx: String? by settings.nullableString(Preference.LastXxx.key)
}
```

`Preference`（`composeApp/core/src/commonMain/kotlin/org/starter/project/core/preferences/PreferencesKey.kt`）にキーを追加し、`PreferencesConfig`（`composeApp/core/src/commonMain/kotlin/org/starter/project/core/preferences/PreferencesConfig.kt`）で名前空間ごとの `Settings` を作る:

```kotlin
class PreferencesConfig(factory: Settings.Factory) {
    val xxxPreferences: Settings = factory.create("xxx_preferences")
}
```

### DTO

```kotlin
package org.starter.project.data.<source>.datasource.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class XxxResponse(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("some_field")
    val someField: String? = null,
    @SerialName("nested")
    val nested: XxxNestedResponse? = null
)

@Serializable
data class XxxNestedResponse(
    @SerialName("id")
    val id: Int? = null
)
```

全フィールドが `Xxx? = null` である理由は「なぜ全 nullable か」を参照（アンチパターン節）。

一覧を返す API のレスポンス DTO は、一覧本体とカーソルを一緒に包むラッパー型にする（`ArticlesResponse` の実例に忠実な形。DATA-17 のカーソル付き単発取得と対応する）:

```kotlin
package org.starter.project.data.<source>.datasource.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class XxxListResponse(
    @SerialName("items")
    val items: List<XxxItemResponse>? = null,
    @SerialName("next_page")
    val nextPage: String? = null
)

@Serializable
data class XxxItemResponse(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("some_field")
    val someField: String? = null
)
```

### Converter

単体を扱う Converter（DTO 1件 → domain モデル1件）と、一覧を扱う Converter（ラッパー DTO → ラッパー domain モデル）は、いずれも `object XxxConverter { operator fun invoke(response: XxxResponse): DomainModel }` という同じ形の `invoke` 1本をエントリポイントに持つ。「一覧」を扱う場合でも `invoke` の引数は `List<XxxResponse>` ではなく、一覧を包むラッパー DTO（DATA-9 のとおり全フィールド `nullable = null`）である点に注意する（DATA-11）。

単体を扱う場合（`UserConverter` の実例に忠実な形。`validateNotNull` の例外をそのまま伝播させてよい）:

```kotlin
package org.starter.project.data.<source>.converter

import androidx.annotation.VisibleForTesting
import org.starter.project.base.data.model.<domain>.DomainModel
import org.starter.project.base.extension.validateNotNull
import org.starter.project.data.<source>.datasource.api.response.XxxResponse

object XxxConverter {
    operator fun invoke(response: XxxResponse): DomainModel {
        return DomainModel(
            id = response.id.validateNotNull("id"),
            someField = response.someField.validateNotNull("some_field")
        )
    }
}
```

一覧を扱う場合（`ArticlesConverter` の実例に忠実な形。ラッパー DTO `XxxListResponse { items, nextPage }` → ラッパー domain モデル `XxxList { items, nextPage }`。`items` は `mapNotNull` + `try/catch(ConversionError)` + `.orEmpty()` で1件ずつ変換失敗を許容する、DATA-13）:

```kotlin
package org.starter.project.data.<source>.converter

import androidx.annotation.VisibleForTesting
import org.starter.project.base.data.model.<domain>.DomainModel
import org.starter.project.base.data.model.<domain>.XxxList
import org.starter.project.base.error.ConversionError
import org.starter.project.base.extension.validateNotNull
import org.starter.project.data.<source>.datasource.api.response.XxxItemResponse
import org.starter.project.data.<source>.datasource.api.response.XxxListResponse

object XxxListConverter {
    operator fun invoke(response: XxxListResponse): XxxList {
        return XxxList(
            items = response.items?.mapNotNull {
                try {
                    createXxx(it)
                } catch (e: ConversionError) {
                    // TODO: report conversion error as non-fatal to your analytics
                    null
                }
            }.orEmpty(),
            nextPage = response.nextPage
        )
    }

    @VisibleForTesting
    internal fun createXxx(response: XxxItemResponse): DomainModel {
        return DomainModel(
            id = response.id.validateNotNull("id"),
            someField = response.someField.validateNotNull("some_field")
        )
    }
}
```

### 新規 data モジュールの `build.gradle.kts`

Zenn サンプル（`composeApp/data/zenn/build.gradle.kts`）を基にしたテンプレート。プラグインは実際に使うものだけ足す（KSP/Ktorfit はリモート API を持つモジュールのみ、`serialization` は DTO を持つモジュールのみ、`mokkery` はテストを持つモジュールのみ）。

```kotlin
plugins {
    id("kmp-library")
    alias(libs.plugins.jetbrains.kotlin.serialization) // DTO（@Serializable）を持つ場合
    alias(libs.plugins.google.devtools.ksp)             // Ktorfit interface を持つ場合
    alias(libs.plugins.ktorfit)                         // 同上
    alias(libs.plugins.mokkery)                         // テストを持つ場合
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.data)

            api(projects.composeApp.base)
            implementation(projects.composeApp.core)
            implementation(projects.composeApp.data.repository)
        }
        commonTest.dependencies {
            implementation(libs.bundles.test)
        }
    }
}
```

`libs.bundles.data`（`gradle/libs.versions.toml`）は `ktorfit`/`ktor-client-content-negotiation`/`ktor-serialization-kotlinx-json`/`ktor-client-logging`/`multiplatform-settings` をまとめたバンドル。新しい data モジュールが Ktor 通信とローカル設定の両方を使う場合はこのバンドルをそのまま使う。API のみ・Preferences のみの場合は、そのモジュールに必要なライブラリだけを個別に追加することも許容する。

Repository interface 専用モジュール（`composeApp/data/repository` 相当）は、依存を `base` のみに絞る:

```kotlin
plugins {
    id("kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.composeApp.base)
        }
    }
}
```

`settings.gradle.kts` への `include(":composeApp:data:<source>")` の追加、`composeApp/app/build.gradle.kts` への依存追加、`Koin.kt` への登録手順は [module-guide.md](../architecture/module-guide.md) を参照。

## アンチパターン

| アンチパターン | なぜダメか | 正しい形 |
|---|---|---|
| Repository が `Result<T>` や独自 sealed class を返す | 例外→`Result` 変換は `domain:service` の責務（ADR-0003）。Repository で行うと責務が重複し、Service 側の `ResultHandler` が機能しなくなる。 | Repository は素の domain モデルを返し、例外は投げっぱなしにする（DATA-3, DATA-4）。 |
| DTO の必須に見えるフィールドを non-null で定義する | Zenn API が `pinned` キーを返さなくなった際、デフォルト値のない nullable フィールドは kotlinx.serialization では依然 required 扱いとなり、`JsonConvertException` でデコードが失敗した（コミット `4aefc64`）。API 側のフィールド追加・削除に対して脆弱になる。 | レスポンス DTO は全フィールド `Xxx? = null` にし、必須項目のチェックは Converter の `validateNotNull` に任せる（DATA-9, DATA-12）。 |
| Converter を拡張関数（`fun XxxResponse.toDomain()`）で書く | 既存の2例（`ArticlesConverter`, `UserConverter`）が `object` + `operator fun invoke` で統一されており、混在すると一貫性が崩れる。テストコードも `subject(response)` の呼び出し方に依存している。 | `object XxxConverter { operator fun invoke(...) }` 形式で書く（DATA-11）。 |
| 一覧変換で `validateNotNull` の例外を握りつぶさずそのまま伝播させる | 1件の不正データでリスト全体（記事一覧など）がエラー表示になる退行を招く。 | `mapNotNull { try { createXxx(it) } catch (e: ConversionError) { null } }` で1件ずつ許容する（DATA-13）。 |
| `interface XxxApi` の実装クラスを手書きする | KSP が `Ktorfit.createXxxApi()` を自動生成するため、手書き実装は二重定義エラーになるか、DI が生成コードの代わりに手書きクラスを使ってしまう。 | 実装は書かず、`Ktorfit.createXxxApi()` を DI 経由で取得する（DATA-6, B-G14）。 |
| data モジュール内で独自の `HttpClient` を生成する | `ApiConfig`/`HttpResponseValidator`/`Logging` の設定が重複・不整合を起こし、エラー変換の一元化（[error-handling.md](../architecture/error-handling.md)）が崩れる。 | `core` の `ApiClientImpl` を DI で注入して使い回す（DATA-14）。 |
| Repository/Service が `Pager`/`Flow<PagingData<T>>` を直接返す | ページングの組み立てが UI 層の責務という層分離（B-G5、ADR-0005）に反する。Repository の再利用性・テスト容易性が下がる。 | Repository はカーソル付き単発取得のみ提供し、UI 層で `PagingSource` を組み立てる（DATA-17）。 |
| 要件が無いのに先取りでキャッシュ・リトライ・タイムアウト・認証を実装する | 過剰設計になりやすく、実際の要件と乖離した実装を後から作り直すコストが発生する。 | 要件が明確になってから `core` の `ApiClientImpl` に追加する（DATA-16、B-G4/B-G11、ADR-0013）。 |

## 参考実装（サンプル。削除後は存在しない）

Zenn 機能一式はテンプレート利用開始時に削除される前提のサンプル実装。以下のパスは本ガイド執筆時点で実在を確認済み。

| パス | 説明 |
|---|---|
| `composeApp/data/repository/src/commonMain/kotlin/org/starter/project/data/repository/Repository.kt` | マーカー interface `Repository`（3行のみ）。 |
| `composeApp/data/repository/src/commonMain/kotlin/org/starter/project/data/repository/ZennRepository.kt` | Repository interface の実例。 |
| `composeApp/data/repository/build.gradle.kts` | Repository interface 専用モジュールの `build.gradle.kts`（`base` のみ依存）。 |
| `composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/repository/ZennRepositoryImpl.kt` | Repository 実装の実例（`XxxRepositoryImpl`、例外透過、リモート API とローカル設定の混在）。 |
| `composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/datasource/api/ZennApi.kt` | Ktorfit interface の実例（`@GET`/`@Query`/`@Path`）。 |
| `composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/datasource/api/response/ArticlesResponse.kt` | DTO の実例（全フィールド `Xxx? = null`、ネスト DTO の命名）。 |
| `composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/datasource/api/response/UserResponse.kt` | DTO の実例（`bio` のみ `orEmpty()` で空文字デフォルトにする特例あり）。 |
| `composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/datasource/preferences/ZennPreferences.kt` | Preferences interface + Impl の実例（`multiplatform-settings` の委譲プロパティ）。 |
| `composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/converter/ArticlesConverter.kt` | 一覧変換の実例（`mapNotNull` + `try/catch(ConversionError)`）。 |
| `composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/converter/UserConverter.kt` | 単体変換の実例（例外を素通しする側）。 |
| `composeApp/data/zenn/build.gradle.kts` | 新規 data モジュールの `build.gradle.kts` テンプレートの元になった実ファイル。 |
| `composeApp/core/src/commonMain/kotlin/org/starter/project/core/api/ApiClient.kt` | HttpClient 一元生成の実例（`ContentNegotiation`/`Logging`/`HttpResponseValidator`）。 |
| `composeApp/core/src/commonMain/kotlin/org/starter/project/core/api/ApiConfig.kt` | `internal object` による設定値集約の実例（`API_BASE_URL`）。 |
| `composeApp/core/src/commonMain/kotlin/org/starter/project/core/preferences/PreferencesConfig.kt` | Preferences の名前空間分割の実例。 |
| `composeApp/core/src/commonMain/kotlin/org/starter/project/core/preferences/PreferencesKey.kt` | Preferences キーの enum 管理の実例。 |
| `composeApp/base/src/commonMain/kotlin/org/starter/project/base/extension/ConversionErrorExtension.kt` | `validateNotNull` 拡張関数の実装。 |
| `composeApp/data/zenn/src/commonTest/kotlin/org/starter/project/data/zenn/repository/ZennRepositoryTest.kt` | Repository 実装テストの実例（Mokkery）。書き方は [testing.md](testing.md) 参照。 |
| `composeApp/data/zenn/src/commonTest/kotlin/org/starter/project/data/zenn/converter/ArticlesConverterTest.kt` | Converter テストの実例（モック不使用、DTO を直接構築）。 |
| `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/component/article/ArticlesPagingSource.kt` | UI 層でのページング組み立ての実例（B-G5 の層分離）。 |

## チェックリスト

- [ ] Repository interface は `composeApp/data/repository` に置き、`Repository` を継承しているか。
- [ ] Repository 実装は `XxxRepositoryImpl` と命名され、例外を握りつぶさず（`try/catch` を持たず）そのまま伝播させているか。
- [ ] Repository/DataSource/Converter に `withContext`/`Dispatchers` の切り替えが無いか。
- [ ] リモート API は Ktorfit interface のみで、手書き実装が無いか。KSP/Ktorfit プラグインを `build.gradle.kts` に追加したか。
- [ ] DTO は全フィールド `Xxx? = null` になっているか。`@SerialName` を snake_case で付けているか。
- [ ] Converter は `object` + `operator fun invoke` で、一覧変換は `mapNotNull` + `try/catch(ConversionError)` になっているか。
- [ ] HttpClient の設定を `core` の `ApiClientImpl` 以外で行っていないか。
- [ ] ページングを UI 層に委ね、Repository はカーソル付き単発取得のみになっているか。
- [ ] キャッシュ・リトライ・タイムアウト・認証を要件なしに先取り実装していないか。
- [ ] 新規 data モジュールの `build.gradle.kts` が `kmp-library` を適用し、必要なプラグインのみを追加しているか。
- [ ] Converter と Repository 実装にテストがあるか（[testing.md](testing.md) の必須範囲を参照）。
