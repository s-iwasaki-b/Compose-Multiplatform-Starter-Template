# domain 層ガイド

> 対象: domain 層（Service interface/実装、domain モデルの置き場所、`Result` 化、ビジネスロジックの置き場所）
> 関連: [architecture/overview.md](../architecture/overview.md), [architecture/error-handling.md](../architecture/error-handling.md), [architecture/dependency-injection.md](../architecture/dependency-injection.md), [architecture/module-guide.md](../architecture/module-guide.md), [guides/data-layer.md](./data-layer.md), [guides/ui-layer.md](./ui-layer.md), [guides/testing.md](./testing.md), [decisions/README.md](../decisions/README.md), [decisions/0023-shared-state-in-service.md](../decisions/0023-shared-state-in-service.md), [decisions/0024-platform-capabilities-via-app-module.md](../decisions/0024-platform-capabilities-via-app-module.md), [decisions/0025-multi-host-api-clients.md](../decisions/0025-multi-host-api-clients.md)
> 最終確認コミット: ac56102

## 要点

- Service interface は `domain:service` モジュールに置き `Service` マーカー interface を継承する。実装は機能ごとの `domain:<name>` モジュールに `XxxServiceImpl` として置く。命名は `Service`（`UseCase` は使わない）。
- 1つの Service interface は「1つの外部リソース/機能ドメイン」に対応し、複数メソッドを持つ。1クラス1メソッドの UseCase パターンは採らない。
- メソッドシグネチャは `suspend fun ...: Result<T>`（非同期）または `fun ...: Result<T>`（同期）で統一する。`Flow` は継続監視が要件のときのみ使う。
- Service メソッド本体は必ず `ResultHandler.async {}` / `.immediate {}`（`Unit` は `asyncUnit` / `immediateUnit`）で包む。Repository は例外透過であり、`Result` 化は Service 層だけの責務。
- フォールバック・複数 Repository の束ね・副作用（永続化など）といったビジネスロジックは Service に書く。複数 Repository を扱う場合は Service のコンストラクタ注入で束ねる。
- domain モデルは `domain/*` モジュールではなく `base` の `org.starter.project.base.data.model.<feature>` パッケージに置く。`@Immutable` かつ non-null フィールドのみ。
- 入力値の業務バリデーションは Service 層、レスポンスの整合性検証（必須フィールド欠落など）は data 層の Converter が担う。
- Service は interface をモックしてテストする。実装クラスへの `@OpenForTesting` は具象クラスを直接モックする必要があるときだけの任意設定。

## ルール

| ID | 規範 | ルール |
|---|---|---|
| DOM-1 | 必須 | Service interface は `domain:service` モジュールに置き、`Service` マーカー interface を継承する（`interface XxxService : Service`）。実装クラスは機能ごとの `domain:<name>` モジュールに置く。新しい外部リソース/機能ドメインを追加するときも interface の追記先は既存の `domain:service`、実装のみ新規 `domain:<name>` モジュールに作る。 |
| DOM-2 | 必須 | 命名は `XxxService`（interface）/ `XxxServiceImpl`（実装）で統一する。`XxxUseCase` という命名は使わない。 |
| DOM-3 | 必須 | 1つの Service interface は1つの外部リソース/機能ドメインに対応する複数メソッドの集約とする。画面単位・操作単位で Service を細分化しない（判断基準は後述）。 |
| DOM-4 | 必須 | メソッドシグネチャは `suspend fun ...: Result<T>`（非同期）または `fun ...: Result<T>`（同期）で統一する。`Flow<T>` を公開 API にするのは継続監視が要件のときのみ（実例なし・新規決定）。 |
| DOM-5 | 必須 | Service の各メソッド本体は必ず `ResultHandler.async { }` / `ResultHandler.immediate { }` で包む。戻り値が `Unit` の場合は `asyncUnit { }` / `immediateUnit { }` を使う。例外を `Result` に変換するのは Service 層だけの責務。 |
| DOM-6 | 必須 | Service（`domain:<name>`）は `data:repository` の Repository interface にのみ依存する。`data:<source>`（Repository 実装モジュール）には依存しない。ViewModel/feature も同様に Service（`domain:service`）にのみ依存し、Repository を直接注入しない。 |
| DOM-7 | 必須 | フォールバック処理、複数 Repository を跨ぐ集約、副作用（検索キーワードの永続化など）といったビジネスロジックは Service に書く。Repository・ViewModel に書かない。 |
| DOM-8 | 推奨（新規決定） | 1つの Service メソッドが複数の Repository を必要とする場合は、Service のコンストラクタに複数 Repository を注入して束ねる。束ねる専用の中間層（Coordinator/Aggregator）は新設しない。 |
| DOM-9 | 必須 | domain モデルは `domain/*` モジュールではなく `base` の `org.starter.project.base.data.model.<feature>` パッケージに置く。`domain/<name>` 配下に `model` パッケージを作らない。 |
| DOM-10 | 必須 | domain モデルは `@Immutable data class` で non-null フィールドのみを持つ。DTO とは常に別型にし、DTO→domain モデルの変換は data 層の Converter が担う。domain 層は DTO を import しない。 |
| DOM-11 | 必須（新規決定） | `base` が機能追加で肥大化しても、機能ごとのサブパッケージ（`data.model.<feature>`）で整理し、モジュール分割はしない。 |
| DOM-12 | 必須（新規決定） | 入力値の業務バリデーション（必須項目・文字数制限など）は Service 層で行う。レスポンスの整合性検証（必須フィールド欠落など）は data 層の Converter（`validateNotNull`）が行う。Service は `ConversionError` を扱わない。 |
| DOM-13 | 必須 | DI 登録は `composeApp/app` の `Koin.kt` に集約する。domain 側に DI モジュール定義ファイルを置かない。`single<XxxService> { XxxServiceImpl(get(), get()) }` のように interface 型を明示する（詳細は [dependency-injection.md](../architecture/dependency-injection.md)）。 |
| DOM-14 | 必須 | パッケージは `org.starter.project.domain.service` / `org.starter.project.domain.<feature>` の直下にフラットに置く。data 層のような役割別サブパッケージ（`converter`/`datasource` 等）は domain 層には作らない。 |
| DOM-15 | 任意 | Service 実装クラスに `@OpenForTesting open class` を付けるのは、具象クラスを直接モックする必要があるときだけ。通常は interface（`XxxService`/`XxxRepository`）をモックするため不要。 |
| DOM-16 | 必須（配置）/ 詳細は testing.md | Service 実装のテストは実装と同一モジュール（`domain/<name>`）の `commonTest` に `XxxServiceTest` として置く（`Impl` サフィックスは付けない）。依存する Repository interface は Mokkery でモックする。モックの作り方・命名・`// arrange`/`// act`/`// assert` 構造の詳細は [testing.md](./testing.md) を参照。 |
| DOM-17 | 必須（新規決定） | 複数画面・アプリ全体で共有する状態（カート、ログイン状態、テーマ設定等）は `domain:service` に状態保持 Service の interface（例: `AppSettingsService { val themeMode: StateFlow<ThemeMode>; suspend fun setThemeMode(mode: ThemeMode): Result<Unit> }`）を置き、実装 `domain:<name>` の `XxxServiceImpl` が `MutableStateFlow` を保持して Preferences 系 Repository に永続化する。ViewModel を画面間で共有しない（`koinViewModel` は画面スコープ）。`StateFlow` の公開は DOM-4/ADR-0012 の「継続監視が要件」に該当する（[ADR-0023](../decisions/0023-shared-state-in-service.md)）。 |

## 判断基準

サンプル（Zenn 実装）が1機能分しか無いため、以下は今後の機能追加時に迷いやすい分岐点を明文化したものです。

### 1. Service の粒度（新しい取得パターンをどう追加するか）

| 状況 | 判断 |
|---|---|
| 既存 Service が扱う外部リソース/機能ドメインと同じもの（例: 既存の Zenn API に新しいエンドポイントを追加）に対する取得パターンを追加する | 既存の `XxxService` interface にメソッドを追加する |
| 新しい外部リソース/機能ドメイン（新しい API・新しい永続化対象など）を追加する | `domain:service` に新しい `YyyService` interface を追加し、実装は新規 `domain:yyy` モジュールに置く |

### 2. 複数 Repository を束ねるロジックの置き場所（gap 9-3）

1つの Service メソッドが複数の Repository を呼ぶ必要がある場合、Service のコンストラクタに複数の Repository を注入し、メソッド内で順に呼び出す（DOM-8）。中間 Coordinator 層は作らない。実例が無いため、複雑化した場合の再検討は将来の ADR に委ねる。

### 3. domain モデルの置き場所（gap 9-4）

| 状況 | 判断 |
|---|---|
| 新しい domain 概念の型を追加する | 常に `base` の `data.model.<feature>` パッケージに置く（`domain/<name>` に `model` パッケージを作らない） |
| `base` が機能追加で肥大化してきた | 機能ごとのサブパッケージ（`data.model.<feature>`）で整理する。`base` モジュール自体を分割しない（DOM-11） |

### 4. バリデーションの置き場所（gap 9-7）

| バリデーションの種類 | 置き場所 |
|---|---|
| 入力値の業務ルール（必須・文字数・フォーマットなど） | Service 層（DOM-12） |
| レスポンスの必須フィールド欠落など、DTO→domain モデル変換時の整合性チェック | data 層の Converter（`validateNotNull`。実例踏襲） |

### 5. `operator fun invoke` パターンの適用範囲（gap 9-10）

| 対象 | パターン |
|---|---|
| 単一の変換処理（DTO→domain モデル1つ） | `object` + `operator fun invoke`（data 層の Converter のみ） |
| 複数の公開メソッドを持つ契約（Service） | `interface` を継承した `class` + 通常のメソッド名。Service に `invoke` パターンは使わない |

### 6. Service の公開 API 粒度（gap 9-12）

| 状況 | 判断 |
|---|---|
| Repository 呼び出しの組み合わせ・分岐ロジック（フォールバックの有無など）が用途ごとに異なる | 用途別に公開メソッドを分ける（現状踏襲。例: `fetchArticles` はフォールバックあり、`fetchUserArticles` はフォールバックなし） |
| 内部処理が同一でパラメータの違いのみ | 既存メソッドにパラメータを追加する。用途別メソッドを増やさない |

### 7. `Flow<T>` を使ってよいか（DOM-4 の補足）

| 状況 | 判断 |
|---|---|
| 単発の取得・更新（典型的な REST API 呼び出し） | `suspend fun ...: Result<T>` |
| DB の変更監視・WebSocket など継続的な監視が要件そのもの | `Flow<T>` を検討する（実例なし。採用時は ADR を追記する） |
| 複数画面・アプリ全体で共有する状態を公開する | `Flow<T>`（`StateFlow<T>`）を使う。継続監視の要件に該当する（DOM-17、[ADR-0023](../decisions/0023-shared-state-in-service.md)） |

### 8. 2つ以上の外部リソースを1画面で合成する場合（新規決定）

| 状況 | 判断 |
|---|---|
| 表示上の単純な並置（同一画面に別々のセクションとして表示するだけ） | ViewModel が複数の Service を個別に注入し、それぞれ呼び出す（任意） |
| 業務ロジックとしての結合（キーで結合する、フィルタする、派生値を算出する等） | 合成結果の概念を表す新しい Service（例: `DashboardService`）を `domain:service`/`domain:<name>` に作り、複数 Repository をコンストラクタ注入で束ねる（推奨。[ADR-0011](../decisions/0011-service-per-resource.md) 判断基準を参照） |

既存の DOM-8（1つの Service メソッドが複数 Repository を必要とする場合）は「同一 Service 内で複数 Repository を束ねる」ケースを扱う。本項目は「元々別ドメインの2つの Service にまたがる合成」を扱う点で異なる。

## 実装パターン

### パッケージ構成テンプレート

```text
composeApp/domain/service/                          # 契約 + 共通ユーティリティ（全機能で共有・1つのみ）
  src/commonMain/kotlin/org/starter/project/domain/service/
    Service.kt                                       # マーカー interface
    ResultHandler.kt                                 # Result 化ユーティリティ
    XxxService.kt                                    # 機能ごとの Service interface

composeApp/domain/<feature>/                         # 機能ごとの実装モジュール（新機能ごとに追加）
  build.gradle.kts
  src/commonMain/kotlin/org/starter/project/domain/<feature>/
    XxxServiceImpl.kt
  src/commonTest/kotlin/org/starter/project/domain/<feature>/
    XxxServiceTest.kt
```

### 新規 domain 実装モジュールの `build.gradle.kts` テンプレート

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

`domain:service` 自体は機能非依存の共有モジュールで、新規に作るものではない。新しい Service interface はこのモジュールに追記する。

```kotlin
// composeApp/domain/service/build.gradle.kts（参考。新規作成しない）
plugins {
    id("kmp-library")
    alias(libs.plugins.mokkery)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.composeApp.base)
        }
        commonTest.dependencies {
            implementation(libs.bundles.test)
        }
    }
}
```

### Service interface テンプレート（`domain:service`）

```kotlin
package org.starter.project.domain.service

import org.starter.project.base.data.model.<feature>.Xxx

interface XxxService : Service {
    suspend fun fetchXxx(id: String): Result<Xxx>
    fun getCachedXxx(): Result<Xxx?>
}
```

### Service 実装テンプレート（`domain:<feature>`）

```kotlin
package org.starter.project.domain.<feature>

import org.starter.project.data.repository.XxxRepository
import org.starter.project.data.repository.YyyRepository
import org.starter.project.domain.service.ResultHandler
import org.starter.project.domain.service.XxxService

class XxxServiceImpl(
    private val resultHandler: ResultHandler,
    private val xxxRepository: XxxRepository,
    private val yyyRepository: YyyRepository // 複数 Repository はコンストラクタ注入で束ねる（DOM-8）
) : XxxService {
    override suspend fun fetchXxx(id: String) = resultHandler.async {
        require(id.isNotBlank()) { "id must not be blank" } // 入力バリデーションは Service 層（DOM-12）

        val primary = xxxRepository.fetchXxx(id)
        val related = yyyRepository.fetchRelated(id)
        primary.copy(related = related)
    }

    override fun getCachedXxx() = resultHandler.immediate {
        xxxRepository.getCachedXxx()
    }
}

// 具象クラスを直接モックする必要があるときだけ付ける（任意。DOM-15）
// @OpenForTesting open class XxxServiceImpl(...)
```

### domain モデルテンプレート（`base`）

```kotlin
package org.starter.project.base.data.model.<feature>

import androidx.compose.runtime.Immutable

@Immutable
data class Xxx(
    val id: String,
    val name: String,
)
```

### テスト配置（詳細は [testing.md](./testing.md)）

```text
composeApp/domain/<feature>/src/commonTest/kotlin/org/starter/project/domain/<feature>/XxxServiceTest.kt
```

```kotlin
private val mockXxxRepository = mock<XxxRepository>(MockMode.autofill)
private val subject = XxxServiceImpl(testResultHandler, mockXxxRepository, mockYyyRepository)

@Test
fun fetchXxx_valid_shouldReturnSuccess() = runTest(testDispatcher) {
    // arrange
    everySuspend { mockXxxRepository.fetchXxx(any()) } returns mockXxx

    // act
    val actual = subject.fetchXxx("id")

    // assert
    assertEquals(Result.success(mockXxx), actual)
}
```

## アンチパターン

1. **interface 名を `XxxUseCase` にし、1クラス1メソッドで乱立させる**
   なぜダメか: 本テンプレートは外部リソース単位で Service を集約する規約（DOM-2, DOM-3）と矛盾し、画面追加のたびにクラスが増えて呼び出し側が煩雑になる。
   正しい形: 同じ外部リソース/機能ドメインなら既存の `XxxService` にメソッドを追加する。

2. **ViewModel や feature モジュールが Repository や `data:<source>` 実装を直接 import する**
   なぜダメか: Gradle の依存境界（`feature` → `domain:service` のみ）の意図（DOM-6）を回避し、テスト容易性・モジュール分離が崩れる。
   正しい形: 必ず `domain:service` の Service interface 経由で呼び出す。

3. **Repository 実装が例外を catch して独自の `Result` を返す**
   なぜダメか: 例外透過（DOM-5, DOM-6）の一貫性が崩れ、Service 側の `ResultHandler` と二重にラップされたり、失敗が握りつぶされたりする。
   正しい形: Repository は例外を投げっぱなしにし、`Result` 化は Service の `ResultHandler` だけで行う。

4. **domain モデルを `domain/<feature>/model` 配下に新設する**
   なぜダメか: `data:repository` と `domain:service` はいずれも `base` にしか依存しておらず、`domain/<feature>` 配下のモデルは他レイヤーから参照できない（DOM-9）。
   正しい形: `base` の `data.model.<feature>` パッケージに置く。

5. **継続監視の要件が無いのに `Flow<T>` を公開 API にする**
   なぜダメか: 単発取得 API に `Flow` を使うと呼び出し側で不要な `collect` が必要になり、既存の `Result<T>` 統一シグネチャ（DOM-4）と混在して一貫性が失われる。
   正しい形: 単発取得は `suspend fun ...: Result<T>` のままにする。継続監視が必要な機能が出た時点で改めて検討する。

6. **Service を Converter と同じ `object` + `operator fun invoke` にする**
   なぜダメか: Service は複数メソッドを持つ契約（DOM-3）であり、`invoke` パターンは単一変換の Converter 専用（判断基準5）。`invoke` 化すると複数のエントリポイントを表現できない。
   正しい形: interface を継承した `class` + 通常のメソッド名にする。

## 参考実装（サンプル。削除後は存在しない）

- `composeApp/domain/service/build.gradle.kts` — `domain:service`（契約+共通ユーティリティ）モジュールの依存構成
- `composeApp/domain/service/src/commonMain/kotlin/org/starter/project/domain/service/Service.kt` — マーカー interface
- `composeApp/domain/service/src/commonMain/kotlin/org/starter/project/domain/service/ZennService.kt` — Service interface の実例（4メソッド、`Result<T>` 統一シグネチャ）
- `composeApp/domain/service/src/commonMain/kotlin/org/starter/project/domain/service/ResultHandler.kt` — `async`/`immediate`/`asyncUnit`/`immediateUnit` の実装
- `composeApp/domain/zenn/build.gradle.kts` — `domain:<feature>` 実装モジュールの依存宣言
- `composeApp/domain/zenn/src/commonMain/kotlin/org/starter/project/domain/zenn/ZennServiceImpl.kt` — 単一 Repository へのフォールバック処理と副作用（`updateLastKeyword`）を持つ Service 実装の実例
- `composeApp/domain/zenn/src/commonTest/kotlin/org/starter/project/domain/zenn/ZennServiceTest.kt` — Mokkery を使った Service 実装テストの実例
- `composeApp/base/src/commonMain/kotlin/org/starter/project/base/data/model/zenn/Articles.kt` — domain モデルの実例（ネスト data class を含む）
- `composeApp/base/src/commonMain/kotlin/org/starter/project/base/data/model/zenn/User.kt` — domain モデルの実例
- `composeApp/base/src/commonMain/kotlin/org/starter/project/base/error/ConversionError.kt` と `composeApp/base/src/commonMain/kotlin/org/starter/project/base/extension/ConversionErrorExtension.kt` — レスポンス整合性検証（`validateNotNull`）の実例。data 層の Converter から呼ばれる
- `composeApp/data/repository/src/commonMain/kotlin/org/starter/project/data/repository/ZennRepository.kt` — Service が依存する Repository interface の実例
- `composeApp/feature/home/src/commonMain/kotlin/org/starter/project/feature/home/HomeScreenViewModel.kt`, `composeApp/feature/user/src/commonMain/kotlin/org/starter/project/feature/user/UserScreenViewModel.kt` — ViewModel から Service を呼ぶ実例
- `composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt` — Service 実装の DI 登録実例（`serviceModule`）

## チェックリスト

- [ ] Service interface を `domain:service` に、`Service` を継承して定義したか（DOM-1）
- [ ] 実装クラスを `XxxServiceImpl` として機能ごとの `domain:<name>` モジュールに置いたか（DOM-1, DOM-2）
- [ ] 追加/変更したメソッドは `suspend fun ...: Result<T>` または `fun ...: Result<T>` になっているか（DOM-4）
- [ ] メソッド本体を `ResultHandler.async`/`immediate`/`asyncUnit`/`immediateUnit` で包んだか（DOM-5）
- [ ] Service（`domain:<name>`）が `data:repository` の interface にのみ依存し、`data:<source>` に依存していないか（DOM-6）
- [ ] 複数 Repository を扱う場合、コンストラクタ注入で束ねたか（DOM-8）
- [ ] 追加した domain モデルは `base` の `data.model.<feature>` に置き、`@Immutable`・non-null になっているか（DOM-9, DOM-10）
- [ ] 入力値の業務バリデーションを Service 層に書いたか（DOM-12）
- [ ] DI 登録を `composeApp/app` の `Koin.kt` に `single<XxxService> { XxxServiceImpl(...) }` で追加したか（DOM-13）
- [ ] Service 実装のテストを同一モジュールの `commonTest` に `XxxServiceTest` として置き、Repository を Mokkery でモックしたか（DOM-16）
- [ ] 複数画面・アプリ全体で共有する状態を、ViewModel を画面間で共有する形ではなく `domain:service` の状態保持 Service（`StateFlow` 公開）で表現したか（DOM-17）
