# 新しい API/データソースを追加する

> 対象: 新しい外部 API（データソース）を追加する手順。`data:<source>` / `domain:<name>` モジュールの新設から DTO/API/Converter/Repository/Service の実装、DI 登録、テストまでを一気通貫で扱う。
> 関連: [module-guide.md](../architecture/module-guide.md), [dependency-injection.md](../architecture/dependency-injection.md), [error-handling.md](../architecture/error-handling.md), [data-layer.md](../guides/data-layer.md), [domain-layer.md](../guides/domain-layer.md), [testing.md](../guides/testing.md), [0001-contract-and-impl-modules.md](../decisions/0001-contract-and-impl-modules.md), [0002-repository-interface-in-data-layer.md](../decisions/0002-repository-interface-in-data-layer.md), [0003-result-at-service-boundary.md](../decisions/0003-result-at-service-boundary.md), [add-feature-screen.md](add-feature-screen.md)
> 最終確認コミット: ac56102

このドキュメントは Zenn サンプル（`composeApp/data/zenn`, `composeApp/data/repository`, `composeApp/domain/zenn`, `composeApp/domain/service`）から一般化したテンプレートです。`Xxx` = PascalCase のリソース名、`<source>`/`<name>` = 小文字のモジュール名（同じ名前でよい）に置き換えてください。サンプルは削除される前提なので、コード全文を埋め込んであります。

## 前提と所要ファイル

- 既存の Repository にメソッドを足すか、新しい `data:<source>`/`domain:<name>` を新設するかを判断する（**ADR-0009**、[data-layer.md](../guides/data-layer.md) 参照）。
  | 状況 | 判断 |
  |---|---|
  | 既存の外部リソース（同じ API/サービス）に新しいエンドポイントを足すだけ | 既存 `XxxApi`/`XxxRepository`/`XxxService` にメソッドを追加する（本ドキュメントの手順3〜4, 10〜11相当を該当ファイルに適用するだけでよい） |
  | 新しい外部リソース（別の API/サービス、または別のローカル永続化） | 本ドキュメントの全手順で `data:<source>`/`domain:<name>` を新設する |
- Repository の分割粒度は **API/外部サービス単位を既定**とする。1つの Repository が肥大化したらドメイン概念単位への分割を検討する（実例に基づく推奨。判断が難しい場合は [data-layer.md](../guides/data-layer.md) を参照）。
- **Preferences のみ（API 無し）の場合**: API を持たずローカル設定のみを扱うデータソースを追加する場合も、Repository + Service は作る（[ADR-0010](../decisions/0010-feature-depends-on-service-only.md) に例外はない）。DTO/API/Converter を省略できる分、手順を以下のように読み替える。
  | 本ドキュメントの手順 | Preferences のみの場合の対応 |
  |---|---|
  | 手順2（`build.gradle.kts`） | `ktorfit`/`ksp` プラグインは不要。`kmp-library` と `mokkery` のみを適用する |
  | 手順3〜4（DTO・Ktorfit API） | 省略する |
  | 手順5（domain モデル） | 単純な `Boolean`/`String` 設定値のみなら省略し、Repository/Service のメソッドの型をそのまま使ってよい |
  | 手順6（Converter） | 省略する |
  | 手順7〜8（Repository interface・実装） | 作る。`XxxRepositoryImpl` は Ktorfit API ではなく `XxxPreferences` に依存する |
  | （追加） | `composeApp/data/<source>/src/commonMain/kotlin/org/starter/project/data/<source>/datasource/preferences/XxxPreferences.kt`（新規。`ZennPreferences.kt`/`ZennPreferencesImpl` と同じパターンで `interface XxxPreferences` + `class XxxPreferencesImpl(settings: Settings) : XxxPreferences` を定義し、`multiplatform-settings` の委譲プロパティで実装する）を作成する。`composeApp/core/src/commonMain/kotlin/org/starter/project/core/preferences/PreferencesConfig.kt` に `val xxxPreferences: Settings = factory.create("xxx_preferences")` を追記し、使うキーを `composeApp/core/src/commonMain/kotlin/org/starter/project/core/preferences/PreferencesKey.kt` の `Preference` enum に追加する（[data-layer.md](../guides/data-layer.md) DATA-8） |
  | 手順9〜11（domain モジュール・Service） | 通常どおり作る |
  | 手順13（DI） | `dataSourceModule` に `single { XxxPreferencesImpl(get<PreferencesConfig>().xxxPreferences) }`、`repositoryModule` に `single<XxxRepository> { XxxRepositoryImpl(get()) }` を登録する |

  画面（feature モジュール）の追加手順は [add-feature-screen.md](add-feature-screen.md) を参照。
- 触るファイル一覧:
  - `settings.gradle.kts`
  - `composeApp/data/<source>/build.gradle.kts`（新規）
  - `composeApp/data/<source>/src/commonMain/kotlin/org/starter/project/data/<source>/datasource/api/response/XxxResponse.kt`（新規）
  - `composeApp/data/<source>/src/commonMain/kotlin/org/starter/project/data/<source>/datasource/api/XxxApi.kt`（新規）
  - `composeApp/base/src/commonMain/kotlin/org/starter/project/base/data/model/<feature>/Xxx.kt`（新規）
  - `composeApp/data/<source>/src/commonMain/kotlin/org/starter/project/data/<source>/converter/XxxConverter.kt`（新規）
  - `composeApp/data/repository/src/commonMain/kotlin/org/starter/project/data/repository/XxxRepository.kt`（新規）
  - `composeApp/data/<source>/src/commonMain/kotlin/org/starter/project/data/<source>/repository/XxxRepositoryImpl.kt`（新規）
  - `composeApp/domain/<name>/build.gradle.kts`（新規）
  - `composeApp/domain/service/src/commonMain/kotlin/org/starter/project/domain/service/XxxService.kt`（新規）
  - `composeApp/domain/<name>/src/commonMain/kotlin/org/starter/project/domain/<name>/XxxServiceImpl.kt`（新規）
  - `composeApp/app/build.gradle.kts`（編集）
  - `composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt`（編集）
  - `composeApp/data/<source>/src/commonTest/kotlin/org/starter/project/data/<source>/converter/XxxConverterTest.kt`（新規）
  - `composeApp/data/<source>/src/commonTest/kotlin/org/starter/project/data/<source>/repository/XxxRepositoryTest.kt`（新規）
  - `composeApp/domain/<name>/src/commonTest/kotlin/org/starter/project/domain/<name>/XxxServiceTest.kt`（新規）

## 手順

### 1. `settings.gradle.kts` に2モジュールを登録する

```kotlin
// settings.gradle.kts
include(":composeApp:data:<source>")
include(":composeApp:domain:<name>")
```

`composeApp`（シェル）自体は動的スキャンのため編集不要。`<source>` と `<name>` は同じ名前でよい（サンプルは両方 `zenn`）。

### 2. `composeApp/data/<source>/build.gradle.kts` を作成する

`composeApp/data/zenn/build.gradle.kts`（サンプル）を一般化したもの:

```kotlin
plugins {
    id("kmp-library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.mokkery)
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

- Compose を使わないので convention plugin は `kmp-library`。
- `serialization`（DTO の `@Serializable`）、`ksp` + `ktorfit`（API インターフェースからの実装コード生成）、`mokkery`（テスト用モック）の4プラグインを追加する。
- 依存は `base`（`api`）、`core`（`ApiClient` を使うため `implementation`）、`data:repository`（実装する interface のため `implementation`）。**`domain:*` への依存は持たない。**

### 3. DTO（レスポンス）を定義する

`composeApp/data/<source>/src/commonMain/kotlin/org/starter/project/data/<source>/datasource/api/response/XxxResponse.kt`（新規）。`UserResponse.kt`（サンプル）と同じパターン:

```kotlin
package org.starter.project.data.<source>.datasource.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class XxxResponse(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("name")
    val name: String? = null,
    // TODO: API レスポンスの残りのフィールドを追加する
)
```

- **全フィールドを nullable にして `= null` のデフォルト値を必ず付ける。** これはこのリポジトリで実際に障害が起きた末の規約（コミット `4aefc64`: Zenn API が特定のキーを返さなくなり、デフォルト値の無い nullable フィールドが `JsonConvertException` でデコード失敗した）。「必須フィールドは non-null で書くべき」という一般的な直感に従わないこと。
- `@SerialName` で API の snake_case とプロパティの camelCase を対応させる。
- ネストしたオブジェクトは別の `data class`（例: `XxxUserResponse`）として同じファイルに定義してよい（`ArticlesResponse.kt` は `ArticlesResponse`/`ArticleResponse`/`ArticleUserResponse`/`ArticlePublicationResponse` の4つを1ファイルにまとめている）。クラス名サフィックスは `Response`（`Dto`/`Entity` は使わない）。

### 4. Ktorfit API インターフェースを定義する

`composeApp/data/<source>/src/commonMain/kotlin/org/starter/project/data/<source>/datasource/api/XxxApi.kt`（新規）:

```kotlin
package org.starter.project.data.<source>.datasource.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import org.starter.project.data.<source>.datasource.api.response.XxxResponse

interface XxxApi {
    @GET("api/xxx/{id}")
    suspend fun fetchXxx(
        @Path("id") id: String,
        @Query("order") order: String? = null
    ): XxxResponse
}
```

- **実装クラスを手で書かない。** `ksp`/`ktorfit` プラグイン（手順2）がビルド時に `Ktorfit.createXxxApi(): XxxApi` という拡張関数を自動生成する。手動で `class XxxApiImpl : XxxApi` のようなクラスを書かないこと（KSP 生成コードと衝突する）。
- HTTP クライアント自体（`HttpClient`/`ContentNegotiation`/`Logging`/エラー変換）は `composeApp/core` の `ApiClientImpl` に一元化されている。新しいモジュール側で独自の `HttpClient` を作らず、DI で注入された `ApiClient` を使い回す（手順13）。
- **別ホスト（別 baseUrl）の API を追加する場合**: `ApiClientImpl`（`composeApp/core/src/commonMain/kotlin/org/starter/project/core/api/ApiClient.kt`）は1つしかなく、`defaultRequest { url(ApiConfig.API_BASE_URL) }` で baseUrl が固定されている。既定は `ApiClientImpl` を baseUrl 引数化し、ホストごとに Koin の `named("<host>")` で複数登録する方式（[ADR-0025](../decisions/0025-multi-host-api-clients.md)）: `class ApiClientImpl(private val baseUrl: String, ...) : ApiClient` とし、`single<ApiClient>(named("zenn")) { ApiClientImpl(ApiConfig.API_BASE_URL) }` / `single<ApiClient>(named("github")) { ApiClientImpl(GitHubApiConfig.API_BASE_URL) }` のように登録し、`dataSourceModule` では `get<ApiClient>(named("github")).ktorfit.createGitHubApi()` のように取得する。設定値は `core` に新しい `internal object <Host>ApiConfig` を追加する。**この複数ホストパターンはこのリポジトリに実装済みの実例が無い（現状未実装の差分）。** `@GET`/`@POST` 等のアノテーションに絶対 URL を書く方式は、`defaultRequest` との合成挙動をこのリポジトリで検証していないため既定にしない。
- **別ホストのエラーレスポンス形式が Zenn と異なる場合**: 共通の `HttpResponseValidator`（`exceptionResponse.body<ApiErrorResponse>()` で固定デコード、[error-handling.md](../architecture/error-handling.md) ERR-3）は Zenn のエラーボディ形式専用であり、形式が異なるホストのレスポンスをそのままデコードすると失敗する。この場合は `ApiClientImpl` のコンストラクタ引数（例: `errorMapper: suspend (HttpResponse) -> Throwable`）でエラー変換処理をホストごとに差し替える（**現状未実装の差分**）。

### 5. domain モデルを `base` に定義する

`composeApp/base/src/commonMain/kotlin/org/starter/project/base/data/model/<feature>/Xxx.kt`（新規）。`User.kt`（サンプル）と同じパターン:

```kotlin
package org.starter.project.base.data.model.<feature>

import androidx.compose.runtime.Immutable

@Immutable
data class Xxx(
    val id: Int,
    val name: String,
    // TODO: non-null な domain モデルのフィールドを追加する
)
```

- **domain モデルは `domain/<name>` モジュールではなく `base` モジュールの `data.model.<feature>` パッケージに置く。** 一般的な Clean Architecture の知識（domain モデルは domain 層）に従うと誤る。`domain/service`・`data/repository` の双方が `base` にしか依存していないため、共有カーネルである `base` に置く必要がある。
- DTO とは常に別型。フィールドは non-null。DTO → domain モデルの変換は手順6の Converter が担う。

### 6. Converter を作成する

`composeApp/data/<source>/src/commonMain/kotlin/org/starter/project/data/<source>/converter/XxxConverter.kt`（新規）。`UserConverter.kt`（サンプル）と同じパターン:

```kotlin
package org.starter.project.data.<source>.converter

import androidx.annotation.VisibleForTesting
import org.starter.project.base.data.model.<feature>.Xxx
import org.starter.project.base.extension.validateNotNull
import org.starter.project.data.<source>.datasource.api.response.XxxResponse

object XxxConverter {
    operator fun invoke(response: XxxResponse): Xxx {
        return createXxx(response)
    }

    @VisibleForTesting
    internal fun createXxx(response: XxxResponse): Xxx {
        return Xxx(
            id = response.id.validateNotNull("id"),
            name = response.name.validateNotNull("name"),
        )
    }
}
```

- `object` + `operator fun invoke` パターンで統一する（`Mapper`/拡張関数 `toDomain()` は使わない）。呼び出し側は `XxxConverter(response)` のように関数っぽく呼べる。
- 必須フィールドの欠落は `response.field.validateNotNull("field_name")`（`composeApp/base/src/commonMain/kotlin/org/starter/project/base/extension/ConversionErrorExtension.kt`）でチェックし、null なら `ConversionError.ResponseNotNullValidation` を送出させる。
- 一覧を変換する場合は `mapNotNull` + `try/catch (ConversionError)` で1件ずつ変換失敗を許容し、リスト全体を失敗させない（`ArticlesConverter.invoke` の実例。**個々の要素の欠落で一覧全体を落とす設計にしない**）。1件のみ扱う変換（例: ユーザー詳細）は伝播させてよい。

### 7. Repository interface を `data:repository` に追加する

`composeApp/data/repository/src/commonMain/kotlin/org/starter/project/data/repository/XxxRepository.kt`（新規）。`ZennRepository.kt`（サンプル）と同じパターン:

```kotlin
package org.starter.project.data.repository

import org.starter.project.base.data.model.<feature>.Xxx

interface XxxRepository : Repository {
    suspend fun fetchXxx(id: String): Xxx
}
```

- **Repository interface は実装モジュール（`data/<source>`）ではなく、共通の `data/repository` モジュールに置く。** 空のマーカー interface `Repository`（`composeApp/data/repository/src/commonMain/kotlin/org/starter/project/data/repository/Repository.kt`）を必ず継承する。
- メソッドは API エンドポイント単位。戻り値は素の domain モデル（`Result` でラップしない）。**例外は透過させる**（`try/catch` しない） — `Result` 化は手順11の Service 層の責務。

### 8. Repository 実装を `data/<source>` に作成する

`composeApp/data/<source>/src/commonMain/kotlin/org/starter/project/data/<source>/repository/XxxRepositoryImpl.kt`（新規）。`ZennRepositoryImpl.kt`（サンプル）と同じパターン:

```kotlin
package org.starter.project.data.<source>.repository

import org.starter.project.base.data.model.<feature>.Xxx
import org.starter.project.data.repository.XxxRepository
import org.starter.project.data.<source>.converter.XxxConverter
import org.starter.project.data.<source>.datasource.api.XxxApi

class XxxRepositoryImpl(
    private val xxxApi: XxxApi
) : XxxRepository {
    override suspend fun fetchXxx(id: String): Xxx {
        return XxxConverter(xxxApi.fetchXxx(id))
    }
}
```

- 命名は `XxxRepositoryImpl`（`XxxRepository` を implements）。実装は必ず interface とは別モジュールに置く。
- `withContext`/`Dispatchers` の切り替えをここで行わない。Dispatcher の切り替えは `ResultHandler`（`domain:service`、手順11）の1箇所のみに集約する。
- ローカル設定（`ZennPreferences` 相当）も同じ Repository に同居させてよいのは付帯的な小規模データのみ（例: 最後に検索したキーワード）。本格的なキャッシュ・オフライン対応が必要になったら Remote/Local DataSource に分割して Repository が束ねる方針に切り替える（[data-layer.md](../guides/data-layer.md) 参照）。

### 9. `composeApp/domain/<name>/build.gradle.kts` を作成する

`composeApp/domain/zenn/build.gradle.kts`（サンプル）と同じパターン:

```kotlin
plugins {
    id("kmp-library")
    alias(libs.plugins.mokkery)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.domain)

            api(projects.composeApp.base)
            implementation(projects.composeApp.data.repository)
            implementation(projects.composeApp.domain.service)
        }
        commonTest.dependencies {
            implementation(libs.bundles.test)
        }
    }
}
```

- `libs.bundles.domain` は現状（`gradle/libs.versions.toml`）中身が空だが、モジュール名と bundle 名を一致させる規約に合わせてサンプルのまま記述する。
- 依存は `base`（`api`）、`data:repository`（interface のみ、`implementation`）、`domain:service`（interface のみ、`implementation`）。**`data:<source>`（実装モジュール）には依存しない。**

### 10. Service interface を `domain:service` に追加する

`composeApp/domain/service/src/commonMain/kotlin/org/starter/project/domain/service/XxxService.kt`（新規）。`ZennService.kt`（サンプル）と同じパターン:

```kotlin
package org.starter.project.domain.service

import org.starter.project.base.data.model.<feature>.Xxx

interface XxxService : Service {
    suspend fun fetchXxx(id: String): Result<Xxx>
}
```

- 命名は `XxxService`（`XxxUseCase` は使わない）。空のマーカー interface `Service` を継承する。
- **1インターフェースに、そのリソースに関する複数メソッドを持たせる**（「1クラス1メソッド」の UseCase パターンは採用しない）。画面単位ではなく外部リソース単位で切る。
- 戻り値は必ず `Result<T>`。非同期は `suspend fun`、同期（キャッシュ読み取り等）は `fun`。継続監視が要件でない限り `Flow<T>` は使わない（ADR-0012、新規決定）。

### 11. Service 実装を `domain/<name>` に作成する

`composeApp/domain/<name>/src/commonMain/kotlin/org/starter/project/domain/<name>/XxxServiceImpl.kt`（新規）。`ZennServiceImpl.kt`（サンプル）と同じパターン:

```kotlin
package org.starter.project.domain.<name>

import org.starter.project.data.repository.XxxRepository
import org.starter.project.domain.service.ResultHandler
import org.starter.project.domain.service.XxxService

class XxxServiceImpl(
    private val resultHandler: ResultHandler,
    private val xxxRepository: XxxRepository
) : XxxService {
    override suspend fun fetchXxx(id: String) = resultHandler.async {
        xxxRepository.fetchXxx(id)
    }
}
```

- 全メソッドを `resultHandler.async { ... }`（非同期）または `resultHandler.immediate { ... }`（同期）で包む。**`Result` へのラップは Service 層のみの責務**。Repository 側で例外を握りつぶしたり `Result` を返したりしない。
- 複数の Repository を束ねる場合はコンストラクタ引数を増やす（新規決定。実例は単一 Repository のみ）。
- サンプルの `ZennServiceImpl` には `@OpenForTesting open class`（`androidx.annotation.OpenForTesting`）が付いているが、これをモックする既存テストは無い（`ZennServiceTest` は `ZennServiceImpl` を直接インスタンス化し、`ZennRepository` の方をモックしている）。**Service 実装のテストは Repository をモックする形にするため、`@OpenForTesting` は不要。** 他モジュールから具象クラスを直接モックする必要が生じた場合のみ付与する。

### 12. `composeApp/app/build.gradle.kts` に依存を追加する

```kotlin
// composeApp/app/build.gradle.kts の commonMain.dependencies { } に追加
implementation(projects.composeApp.data.<source>)
implementation(projects.composeApp.domain.<name>)
```

### 13. `Koin.kt` に DI 定義を追加する

`composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt`（編集）。DI 定義は `data:<source>`/`domain:<name>` 側には置かず、`composeApp:app` に集約する。

```kotlin
// import 追加
import org.starter.project.data.repository.XxxRepository
import org.starter.project.data.<source>.datasource.api.XxxApi
import org.starter.project.data.<source>.datasource.api.createXxxApi
import org.starter.project.data.<source>.repository.XxxRepositoryImpl
import org.starter.project.domain.service.XxxService
import org.starter.project.domain.<name>.XxxServiceImpl
```

```kotlin
val dataSourceModule = module {
    // 既存のバインドがあれば残す（下に追加する）。
    single<XxxApi> { get<ApiClient>().ktorfit.createXxxApi() } // 追加
}

val repositoryModule = module {
    // 既存のバインドがあれば残す（下に追加する）。
    single<XxxRepository> { XxxRepositoryImpl(get()) } // 追加
}

val serviceModule = module {
    single { ResultHandler() }
    // 既存のバインドがあれば残す（下に追加する）。
    single<XxxService> { XxxServiceImpl(get(), get()) } // 追加
}
```

- [remove-sample-code.md](remove-sample-code.md) を実施済みの場合、`composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt` の各 `module { }` の中身は `// TODO: register your ... bindings here.` コメントのみになっている（`single<ZennApi> { ... }` 等の既存バインドは無い）。この場合は TODO コメントを上記のバインドに置き換える。サンプル削除前に本手順を実施する場合は、既存の `single<ZennApi> { ... }` 等のバインドの下に追加する。
- `createXxxApi` は KSP がビルド時に生成する拡張関数（手順4）。存在しないように見えても import して問題ない（ビルド前は IDE 上で未解決に見えることがある）。
- `single<Interface> { Impl(get(), get()) }` の形式でバインドする（`factory` は使わない）。`get()` の個数は実装クラスのコンストラクタ引数の数に合わせる（`XxxRepositoryImpl(get())` は `XxxApi` のみ、ローカル設定も使う場合は `XxxRepositoryImpl(get(), get())` のように増やす）。
- `ResultHandler` は既に `serviceModule` に1つだけ登録済み（`single { ResultHandler() }`）なので再登録不要。

### 14. テストを追加する

Mokkery（モックライブラリ）で統一する。テストクラス名は `対象クラス名+Test`、メソッド名は `<メソッド名>_<条件>_<期待結果>`、本体は `// arrange` `// act` `// assert` コメント必須。

**`XxxConverterTest.kt`**（新規、`composeApp/data/<source>/src/commonTest/kotlin/org/starter/project/data/<source>/converter/XxxConverterTest.kt`）— `ArticlesConverterTest.kt`（サンプル）と同じパターン。モックは使わず DTO を直接組み立てる:

```kotlin
package org.starter.project.data.<source>.converter

import org.starter.project.base.data.model.<feature>.Xxx
import org.starter.project.base.error.ConversionError.ResponseNotNullValidation
import org.starter.project.data.<source>.datasource.api.response.XxxResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class XxxConverterTest {
    private val subject = XxxConverter
    private val response = XxxResponse(id = 1, name = "name")

    @Test
    fun createXxx_success() {
        // act
        val actual = subject.createXxx(response)

        // assert
        val expected = Xxx(id = 1, name = "name")
        assertEquals(expected, actual)
    }

    @Test
    fun createXxx_failure_notNullValidation() {
        listOf(
            response.copy(id = null),
            response.copy(name = null),
        ).forEach {
            assertFailsWith<ResponseNotNullValidation> {
                subject.createXxx(it)
            }
        }
    }
}
```

**`XxxRepositoryTest.kt`**（新規、`composeApp/data/<source>/src/commonTest/kotlin/org/starter/project/data/<source>/repository/XxxRepositoryTest.kt`）— `ZennRepositoryTest.kt`（サンプル）と同じパターン。`XxxApi` をモックする:

```kotlin
package org.starter.project.data.<source>.repository

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import org.starter.project.data.<source>.converter.XxxConverter
import org.starter.project.data.<source>.datasource.api.XxxApi
import org.starter.project.data.<source>.datasource.api.response.XxxResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class XxxRepositoryTest {
    private val mockXxxApi = mock<XxxApi>()
    private val subject = XxxRepositoryImpl(mockXxxApi)

    @Test
    fun fetchXxx() = runTest {
        // arrange
        val response = XxxResponse(id = 1, name = "name")
        everySuspend { mockXxxApi.fetchXxx(any()) } returns response

        // act
        val actual = subject.fetchXxx("1")

        // assert
        val expected = XxxConverter(response)
        assertEquals(expected, actual)
    }
}
```

**`XxxServiceTest.kt`**（新規、`composeApp/domain/<name>/src/commonTest/kotlin/org/starter/project/domain/<name>/XxxServiceTest.kt`）— `ZennServiceTest.kt`（サンプル）と同じパターン。`XxxRepository` をモックし、`ResultHandler` に `StandardTestDispatcher()` を注入する:

```kotlin
package org.starter.project.domain.<name>

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.starter.project.base.data.model.<feature>.Xxx
import org.starter.project.data.repository.XxxRepository
import org.starter.project.domain.service.ResultHandler
import kotlin.test.Test
import kotlin.test.assertEquals

class XxxServiceTest {
    private val mockXxx = Xxx(id = 1, name = "name")
    private val mockXxxRepository = mock<XxxRepository>(MockMode.autofill)

    private val testDispatcher = StandardTestDispatcher()
    private val testResultHandler = ResultHandler(testDispatcher)
    private val subject = XxxServiceImpl(testResultHandler, mockXxxRepository)

    @Test
    fun fetchXxx_success() = runTest(testDispatcher) {
        // arrange
        everySuspend { mockXxxRepository.fetchXxx("1") } returns mockXxx

        // act
        val actual = subject.fetchXxx("1")

        // assert
        val expected = Result.success(mockXxx)
        assertEquals(expected, actual)
    }

    @Test
    fun fetchXxx_failure_shouldReturnThrowable() = runTest(testDispatcher) {
        // arrange
        val error = Throwable()
        everySuspend { mockXxxRepository.fetchXxx("1") } throws error

        // act
        val actual = subject.fetchXxx("1")

        // assert
        val expected: Result<*> = Result.failure<Throwable>(error)
        assertEquals(expected, actual)
    }
}
```

- モック対象は常に **interface**（`XxxApi`, `XxxRepository`）。具象クラスをモックする必要はない（手順11参照）。
- `mock<T>()`（既定。スタブしていない呼び出しは失敗する）か `mock<T>(MockMode.autofill)`（戻り値が結果に影響しない協調オブジェクト用）かは [testing.md](../guides/testing.md) の判断基準に従う。

## 検証

1. モジュール単位のテスト: `./gradlew :composeApp:data:<source>:testAndroidHostTest` と `./gradlew :composeApp:domain:<name>:testAndroidHostTest`。リポジトリ全体なら `./gradlew testAndroidHostTest`。
2. KSP 生成物の確認: `composeApp/data/<source>/build/generated/ksp/**` に `createXxxApi` を含む生成ファイルができていることを、初回ビルド後に確認する（本ドキュメント執筆時点のワークツリーには `composeApp/data/zenn/build/` 自体が存在せず、ビルド前は確認できない。ビルドを実行して初めて生成される）。
3. `Koin.kt` の登録漏れが無いか: アプリを起動し、新しい Service を使う画面（[add-feature-screen.md](add-feature-screen.md)）に遷移して `NoDefinitionFoundException` が出ないことを確認する。実機/シミュレータでの起動は [`.claude/skills/debug-run/skill.md`](../../.claude/skills/debug-run/skill.md) を使う。

## よくある失敗と対処

| 失敗 | 症状 | 対処 |
|---|---|---|
| `createXxxApi` を手で実装しようとする | KSP 生成コードと衝突してコンパイルエラー、または生成物が握りつぶされる | `interface XxxApi` にアノテーションを付けるだけにする。実装は書かない（手順4） |
| DTO のフィールドを non-null にする | API 側がキーを返さなくなった瞬間に `JsonConvertException` でデコード全体が失敗する | 全フィールド `Xxx? = null` にする（手順3） |
| Repository が `Result<T>` を返す | Service 層で二重ラップになる、または `Result` の中身を剥がす処理が Repository と Service に分散する | Repository は素の domain モデルを返し例外を透過する。`Result` 化は Service のみ（手順7, 11） |
| domain モデルを `domain/<name>` モジュールに置く | `data/repository` と `domain/service` の双方が `base` にしか依存していないため、両モジュールから参照できず解決エラーになる | `composeApp/base/src/commonMain/kotlin/org/starter/project/base/data/model/<feature>/` に置く（手順5） |
| feature から `data:repository` に直接依存させる | `feature/<feature>/build.gradle.kts` に `implementation(projects.composeApp.data.repository)` を追加してしまい、ADR-0010 の依存許可表（feature は `domain:service` のみ）に反する | feature は `domain:service` の Service interface のみ利用する（[add-feature-screen.md](add-feature-screen.md) 参照） |
| `Koin.kt` の登録を一部忘れる（`dataSourceModule`/`repositoryModule`/`serviceModule` のいずれか） | 実行時に `NoDefinitionFoundException` | 手順13の3箇所（API/Repository/Service）をすべて登録する |
| 別ホストの API を `ApiConfig.API_BASE_URL` 前提で書いてしまう | 全リクエストが誤った baseUrl に飛ぶ | 手順4の「別ホストの注意」を確認し、[ADR-0025](../decisions/0025-multi-host-api-clients.md) の `ApiClientImpl` baseUrl 引数化 + Koin `named()` パターンで実装する |
