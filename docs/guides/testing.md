# テストガイド

> 対象: テストダブル（Mokkery）の使い方、テスト必須範囲、命名規約、AAA 構造、Coroutine テストのセットアップ、実行コマンドを規定する。
> 関連: [coding-style.md](coding-style.md), [data-layer.md](data-layer.md), [domain-layer.md](domain-layer.md), [ui-layer.md](ui-layer.md), [ADR-0006](../decisions/0006-mokkery-for-test-doubles.md), [ADR-0017](../decisions/0017-test-scope.md), [ADR-0018](../decisions/0018-dispatcher-switch-in-resulthandler.md)
> 最終確認コミット: ac56102

## 要点

- テストダブルは **Mokkery（`dev.mokkery`）に統一する**。手書き Fake クラスは作らない。テスト専用の共有モジュールも作らない。
- テスト必須範囲は **Converter / Repository 実装 / Service 実装 / ViewModel**。Compose UI テストは任意。
- テストは対象実装モジュールの `commonTest` に、ソースとミラーのパッケージ構成で置く。`androidUnitTest`/`iosTest` は使わない。
- クラス名は `対象クラス名 + Test`、メソッド名は `<methodName>_<condition>_<expectedResult>`（lowerCamelCase を `_` で連結）。本体は `// arrange` `// act` `// assert` の3コメントで区切る（Given/When/Then は使わない）。
- モック対象は **interface**。具象クラスをモックする必要があるときだけ実装クラスに `@OpenForTesting open class` を付ける。
- Coroutine を含むクラスは `CoroutineDispatcher` をデフォルト引数付きコンストラクタで受け取り、テストでは `StandardTestDispatcher()` を注入する。ViewModel テストのみ `Dispatchers.setMain`/`resetMain` を使う。
- `commonTest.dependencies` を宣言したモジュールは、テストファイルを最低1つ持つ（`domain/service` の `ResultHandler` は現状これに違反しており是正対象）。
- 実行は `./gradlew testAndroidHostTest`。CI（`.github/workflows`）は本テンプレートに意図的に含まれていない。

## ルール

| ID | 規約 | 規範 |
|---|---|---|
| TEST-1 | テストダブルは Mokkery で統一する。手書き Fake クラスは作らない。 | **必須** |
| TEST-2 | テストを持つモジュールの `build.gradle.kts` に `alias(libs.plugins.mokkery)` を適用する。 | **必須** |
| TEST-3 | モック対象は interface（`XxxRepository`, `XxxService`, `XxxApi`, `XxxPreferences` 等）とする。 | **必須** |
| TEST-4 | 具象クラスをモックする必要がある場合のみ、そのクラスに `@OpenForTesting open class` を付ける（判断基準参照）。 | **必須** |
| TEST-5 | テスト必須範囲は Converter・Repository 実装・Service 実装・ViewModel とする。 | **必須** |
| TEST-6 | Compose UI テスト（`ComposeUiTest` 等）は書く。 | **任意** |
| TEST-7 | `commonTest.dependencies` を宣言したモジュールは、対応するテストファイルを少なくとも1つ持つ。 | **必須** |
| TEST-8 | テストは対象実装モジュールの `src/commonTest/kotlin/<ソースとミラーのパッケージ>/XxxTest.kt` に置く。`androidUnitTest`/`iosTest` は使わない。 | **必須** |
| TEST-9 | テストクラス名は `対象クラス名 + Test` とする。 | **必須** |
| TEST-10 | テストメソッド名は `<methodName>_<condition>_<expectedResult>`（lowerCamelCase を `_` で連結）とする。 | **必須** |
| TEST-11 | テスト本体は `// arrange` `// act` `// assert` の3コメントで区切る。 | **必須** |
| TEST-12 | アサーションは `assertEquals(expected, actual)` の引数順で書く。 | **必須** |
| TEST-13 | Coroutine を含むクラスは `CoroutineDispatcher` をデフォルト引数 `Dispatchers.IO` 付きコンストラクタで受け取り、テストでは `StandardTestDispatcher()` を注入して `runTest(testDispatcher)` を使う。 | **必須** |
| TEST-14 | ViewModel のテストは `Dispatchers.setMain(StandardTestDispatcher())` を `@BeforeTest`、`Dispatchers.resetMain()` を `@AfterTest` に置く（新規決定）。 | **必須** |
| TEST-15 | 全体実行は `./gradlew testAndroidHostTest`、モジュール単位は `./gradlew :composeApp:<layer>:<name>:testAndroidHostTest` を使う。 | **必須** |
| TEST-16 | `iosSimulatorArm64Test` など iOS 側のテストタスクを個別に実行する。 | **任意** |
| TEST-17 | CI（`.github/workflows`）は用意しない。テンプレート利用者が自分のプロジェクトに合わせて追加する。 | **事実**（ルールではない） |

サンプルの `ZennRepositoryTest`/`ZennServiceTest` は TEST-10 の命名規則に完全には従っていない（`fetchArticles()`/`getLastKeyword()` のように条件・期待結果サフィックスが無いメソッドが混在する）。TEST-10 は新規決定であり、新規コードから統一する。既存サンプルのメソッド名を真似しない。

## 判断基準

### モック対象を interface にするか具象クラスにするか（TEST-3, TEST-4）

| 状況 | 対応 |
|---|---|
| 依存が interface として存在する（`XxxRepository`, `XxxService`, `XxxApi`, `XxxPreferences` 等） | その interface を `mock<Interface>()` する。実装クラスには何も付けない。 |
| どうしても具象クラス自体をモックする必要がある（interface を経由できない依存関係） | そのクラスを `@OpenForTesting open class` にし、`mock<ConcreteClass>()` する。 |

現状 `ZennServiceImpl`（`composeApp/domain/zenn/src/commonMain/kotlin/org/starter/project/domain/zenn/ZennServiceImpl.kt`）に `@OpenForTesting`（`androidx.annotation.OpenForTesting`）が付与されているが、`ZennServiceImpl` 自体をモックするテストは現状存在しない（`ZennServiceTest` は `ZennRepository` interface をモックし `ZennServiceImpl` をテスト対象本体として使っている）。新規クラスでは、まず interface 経由でモックできないかを検討し、それでも要る場合だけ `@OpenForTesting` を付ける。

### `mock<T>()`（既定モード）と `mock<T>(MockMode.autofill)` の使い分け（TEST-3 関連、E-2/E-3 は Mokkery 統一により解消）

Mokkery の既定モードは、スタブされていないメソッド呼び出しがあると失敗する（strict）。`MockMode.autofill` は、スタブされていない呼び出しでも自動生成された値（0、空リスト、`Unit` 等）で処理を続行する。

| 状況 | モード |
|---|---|
| テスト対象が呼び出す依存側メソッドを、そのテストメソッド内で漏れなく `every`/`everySuspend { } returns/throws` でスタブできる | 既定モード（`mock<T>()`） |
| テスト対象が呼び出す依存側メソッドの中に、戻り値を定義せず `verify`/`verifySuspend` で「呼ばれたこと」だけを確認したいメソッドが含まれる（`Unit` を返す副作用メソッドなど） | `MockMode.autofill` |

根拠: `ZennRepositoryTest.kt` の `mockZennApi = mock<ZennApi>()`（`fetchArticles` は全テストで `everySuspend { } returns` されるため既定モードで足りる）に対し、`mockZennPreferences = mock<ZennPreferences>(MockMode.autofill)`（`updateLastKeyword` テストでは setter が明示スタブされずに呼ばれ `verify` だけで検証される）。`ZennServiceTest.kt` の `mockZennRepository = mock<ZennRepository>(MockMode.autofill)` も同様に、`updateLastKeyword` は `everySuspend { } returns` されずに呼ばれ `verifySuspend` でのみ検証される。

### テスト必須範囲（E-1）

| 対象 | 必須/任意 |
|---|---|
| Converter（`object` + `operator fun invoke`） | **必須** |
| Repository 実装（`XxxRepositoryImpl`） | **必須** |
| Service 実装（`XxxServiceImpl`） | **必須** |
| ViewModel（`XxxScreenViewModel`） | **必須** |
| Compose UI（`XxxScreen`/`XxxScreenContent`、Preview 対象コンポーネント） | **任意**（KMP での環境構築コストが高いため） |
| `domain:service` の `ResultHandler` | **必須**（現状0件。最優先で追加すべき対象。E-10） |

`domain/service/build.gradle.kts` は `commonTest.dependencies { implementation(libs.bundles.test) }` と `mokkery` プラグインを持つが、テストファイルは1つも存在しない。`ResultHandler` はすべての例外を `Result` に変換する横断的関心事の中心であり、テスト未整備のまま放置しない（TEST-7）。

### commonTest 依存を宣言したら何をテストするか（E-10）

新規モジュールに `commonTest.dependencies { implementation(libs.bundles.test) }` を追加した時点で、そのモジュールに対応するテストファイルを最低1つ作る。「依存だけ宣言してテストは後回し」は禁止する（TEST-7）。

### Compose UI テストを書くかどうか（E-9）

書かない（デフォルト）。ロジックは ViewModel / Service / Repository に寄せ、Composable は薄く保つ既存方針と整合する。Compose UI テスト用ライブラリ（`ComposeUiTest` 等）はバージョンカタログに存在しない。テンプレート利用者が Compose UI テストを導入する場合は、この判断を上書きしてよい（TEST-6 は任意）。

### CI を用意するかどうか（E-11）

用意しない。本リポジトリはスターターテンプレートであり、CI（`.github/workflows`）は意図的に含まれていない。「CI が無い＝テストが要らない」という意味ではなく、テストの健全性はローカル実行（`./gradlew testAndroidHostTest`）に依存する。テンプレート利用者が自分のプロジェクトに合わせて CI を追加する想定（TEST-17）。

### Dispatcher の注入とテストでの差し替え（E-5）

| クラスの種類 | 方法 |
|---|---|
| I/O 境界を持つクラス（`ResultHandler` など、`withContext` を呼ぶクラス） | コンストラクタで `CoroutineDispatcher = Dispatchers.IO` を受け取る。テストでは `StandardTestDispatcher()` を渡し、`runTest(testDispatcher) { }` を使う（TEST-13）。`Dispatchers.setMain` は使わない。 |
| ViewModel（`viewModelScope` を使うクラス） | `viewModelScope` は内部で `Dispatchers.Main.immediate` を使うため、コンストラクタ注入では差し替えられない。`Dispatchers.setMain(StandardTestDispatcher())` を `@BeforeTest`、`Dispatchers.resetMain()` を `@AfterTest` に置く（TEST-14、新規決定・実例なし）。 |

`withContext` の呼び出しは `ResultHandler.async`/`immediate` の1箇所のみに集約されている（[coding-style.md](coding-style.md) の横断ルール参照）。新しい I/O 境界クラスを作る場合も、独自に `withContext` せず `ResultHandler` を経由するため、通常はこのクラス自身に Dispatcher 注入を追加する必要はない。

## 実装パターン

### 新規モジュールの `build.gradle.kts`

```kotlin
plugins {
    id("kmp-library") // Compose や Compose Resources を使うモジュールは "kmp-compose-library"
    // 必要な機能別プラグインをここに追加（kotlin.serialization, ktorfit, ksp 等）
    alias(libs.plugins.mokkery)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // ...
        }
        commonTest.dependencies {
            implementation(libs.bundles.test)
        }
    }
}
```

### Converter のテスト

DTO を直接構築し、`.copy()` でバリエーションを作る。モックは使わない。

```kotlin
// composeApp/data/<source>/src/commonTest/kotlin/org/starter/project/data/<source>/converter/XxxConverterTest.kt
package org.starter.project.data.<source>.converter

import org.starter.project.base.error.ConversionError.ResponseNotNullValidation
import org.starter.project.data.<source>.datasource.api.response.XxxResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class XxxConverterTest {
    private val subject = XxxConverter
    private val response = XxxResponse(
        id = 0,
        name = "name"
    )

    @Test
    fun invoke_success() {
        // arrange
        // response をそのまま使う

        // act
        val actual = subject(response)

        // assert
        val expected = Xxx(id = 0, name = "name")
        assertEquals(expected, actual)
    }

    @Test
    fun invoke_failure_notNullValidation() {
        // arrange
        val invalid = response.copy(id = null)

        // act & assert
        assertFailsWith<ResponseNotNullValidation> {
            subject(invalid)
        }
    }
}
```

### Repository 実装のテスト

API と Preferences（存在する場合）を interface としてモックする。

```kotlin
// composeApp/data/<source>/src/commonTest/kotlin/org/starter/project/data/<source>/repository/XxxRepositoryTest.kt
package org.starter.project.data.<source>.repository

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import kotlinx.coroutines.test.runTest
import org.starter.project.data.<source>.converter.XxxConverter
import org.starter.project.data.<source>.datasource.api.XxxApi
import org.starter.project.data.<source>.datasource.api.response.XxxResponse
import org.starter.project.data.<source>.datasource.preferences.XxxPreferences
import kotlin.test.Test
import kotlin.test.assertEquals

class XxxRepositoryTest {
    private val mockXxxApi = mock<XxxApi>()
    private val mockXxxPreferences = mock<XxxPreferences>(MockMode.autofill)
    private val subject = XxxRepositoryImpl(mockXxxApi, mockXxxPreferences)

    @Test
    fun fetchXxx() = runTest {
        // arrange
        val response = XxxResponse(id = 0, name = "name")
        everySuspend { mockXxxApi.fetchXxx(any()) } returns response

        // act
        val actual = subject.fetchXxx("param")

        // assert
        val expected = XxxConverter(response)
        assertEquals(expected, actual)
    }
}
```

戻り値を持たない副作用メソッド（ローカル設定の書き込み等）を「呼ばれたこと」だけ検証したい場合は `verify(VerifyMode.exactly(n)) { }`（同期メソッド）/ `verifySuspend(VerifyMode.exactly(n)) { }`（suspend メソッド）を使う。

```kotlin
    @Test
    fun updateLastKeyword() {
        // arrange
        val keyword = "keyword"

        // act
        subject.updateLastKeyword(keyword)

        // assert
        verify(VerifyMode.exactly(1)) { mockXxxPreferences.lastKeyword = keyword }
    }
```

### Service 実装のテスト

Repository を interface としてモックし、`ResultHandler` にはテスト用 `StandardTestDispatcher` を注入する。

```kotlin
// composeApp/domain/<name>/src/commonTest/kotlin/org/starter/project/domain/<name>/XxxServiceTest.kt
package org.starter.project.domain.<name>

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.starter.project.data.repository.XxxRepository
import org.starter.project.domain.service.ResultHandler
import kotlin.test.Test
import kotlin.test.assertEquals

class XxxServiceTest {
    private val mockXxxRepository = mock<XxxRepository>(MockMode.autofill)
    private val testDispatcher = StandardTestDispatcher()
    private val testResultHandler = ResultHandler(testDispatcher)
    private val subject = XxxServiceImpl(testResultHandler, mockXxxRepository)

    @Test
    fun fetchXxx_success_returnsXxx() = runTest(testDispatcher) {
        // arrange
        val expected = Xxx(id = 0, name = "name")
        everySuspend { mockXxxRepository.fetchXxx(any()) } returns expected

        // act
        val actual = subject.fetchXxx("param")

        // assert
        assertEquals(Result.success(expected), actual)

        verifySuspend {
            mockXxxRepository.fetchXxx("param")
        }
    }

    @Test
    fun fetchXxx_failure_shouldReturnThrowable() = runTest(testDispatcher) {
        // arrange
        val error = Throwable()
        everySuspend { mockXxxRepository.fetchXxx(any()) } throws error

        // act
        val actual = subject.fetchXxx("param")

        // assert
        val expected: Result<*> = Result.failure<Throwable>(error)
        assertEquals(expected, actual)
    }
}
```

### ViewModel のテスト（新規決定・実例なし）

依存する Service を interface としてモックし、`Dispatchers.setMain`/`resetMain` を `@BeforeTest`/`@AfterTest` に置く。

```kotlin
// composeApp/feature/<name>/src/commonTest/kotlin/org/starter/project/feature/<name>/XxxScreenViewModelTest.kt
package org.starter.project.feature.<name>

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
    private val mockXxxService = mock<XxxService>()
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
        val expected = Xxx(id = 0, name = "name")
        everySuspend { mockXxxService.fetchXxx() } returns Result.success(expected)

        // act
        // @VisibleForTesting internal な suspend fun（ui-layer.md の4点セットテンプレートの fetch()）を直接呼ぶ場合、advanceUntilIdle は不要
        subject.fetch()

        // assert
        assertEquals(expected, subject.state.value.xxx)
    }
}
```

上記は [ui-layer.md](ui-layer.md) の4点セット ViewModel テンプレート（公開トリガー `reload()`、テスト対象の内部 suspend 関数 `fetch()`）を対象にしたテスト例である。`fetch()` は `@VisibleForTesting internal suspend fun` なので、`viewModelScope.launch` を経由せずテストコルーチンから直接呼び出せる。

公開トリガー `reload()`（`viewModelScope.launch { fetch() }`）自体や、`init` ブロックからの `collect` 等を経由して間接的に状態が更新される場合は、`act` の直後に `kotlinx.coroutines.test.advanceUntilIdle()` を呼んでから `assert` する。テスト対象の内部関数（状態更新ロジック等）が `private` の場合は `@VisibleForTesting internal` に緩めて呼び出せるようにする（[coding-style.md](coding-style.md) STYLE-11 参照）。

## アンチパターン

- **手書きの `FakeXxx` クラスを作る** → Mokkery 統一（TEST-1, [ADR-0006](../decisions/0006-mokkery-for-test-doubles.md)）に反する。interface を `mock<T>()` する。
- **`should_returnXxx_when_yyy` やバッククォート日本語のテストメソッド名を使う** → TEST-10 の `<methodName>_<condition>_<expectedResult>` 形式と不統一になる。
- **`// arrange` `// act` `// assert` コメントを省略する** → TEST-11 に反する。全テストメソッドで一貫させる。
- **`mock<T>()`（既定モード）で、スタブしていないメソッドを呼び出すコードをテストする** → 実行時に Mokkery の strict モード例外で失敗する。呼び出されるが戻り値を定義しないメソッドがあるなら `MockMode.autofill` を使う。
- **`domain:service` のように `commonTest.dependencies` だけ宣言してテストファイルを書かない** → TEST-7 に反する。`ResultHandler` は最優先で追加する。
- **Repository/Service のテストで `Dispatchers.setMain` を使う** → `ResultHandler` パターン（TEST-13）から外れる。コンストラクタ注入の `StandardTestDispatcher` を使う。`Dispatchers.setMain`/`resetMain` は ViewModel テスト専用（TEST-14）。
- **`androidUnitTest`/`iosTest` にテストを置く** → TEST-8 に反する。`commonTest` に一本化する。
- **具象クラスに理由なく `@OpenForTesting` を付ける** → interface 経由でモックできないか先に検討する（判断基準参照）。

## 参考実装（サンプル。削除後は存在しない）

- `composeApp/data/zenn/src/commonTest/kotlin/org/starter/project/data/zenn/converter/ArticlesConverterTest.kt` — Converter テストの実例。DTO を `.copy()` でバリエーション化。
- `composeApp/data/zenn/src/commonTest/kotlin/org/starter/project/data/zenn/repository/ZennRepositoryTest.kt` — Repository 実装テストの実例。`mock<ZennApi>()`（既定）と `mock<ZennPreferences>(MockMode.autofill)` の使い分け。
- `composeApp/domain/zenn/src/commonTest/kotlin/org/starter/project/domain/zenn/ZennServiceTest.kt` — Service 実装テストの実例。`StandardTestDispatcher` を `ResultHandler` に注入し `runTest(testDispatcher)` を使う。`everySuspend`/`verifySuspend` の実例。
- `composeApp/domain/zenn/src/commonMain/kotlin/org/starter/project/domain/zenn/ZennServiceImpl.kt` — `@OpenForTesting` が付与されているが、実際にこのクラス自体をモックするテストは無い実例。
- ViewModel テストと Compose UI テストは、本リポジトリに実例が存在しない（`composeApp/feature/home`, `composeApp/feature/user`, `composeApp/ui`, `composeApp/app` に `commonTest` ディレクトリ自体が無い）。

## チェックリスト

- [ ] 変更・追加した Converter / Repository 実装 / Service 実装 / ViewModel に対応するテストがあるか
- [ ] `commonTest.dependencies` を新たに宣言したモジュールに、テストファイルが最低1つあるか
- [ ] テストクラス名が `対象クラス名 + Test` になっているか
- [ ] テストメソッド名が `<methodName>_<condition>_<expectedResult>` になっているか
- [ ] `// arrange` `// act` `// assert` コメントがあるか
- [ ] `assertEquals(expected, actual)` の引数順になっているか
- [ ] モック対象が interface になっているか（具象クラスをモックする場合のみ `@OpenForTesting` の理由を説明できるか）
- [ ] `mock<T>()` と `MockMode.autofill` の選択が判断基準に沿っているか
- [ ] Repository/Service のテストで `StandardTestDispatcher` をコンストラクタ注入しているか（`Dispatchers.setMain` を使っていないか）
- [ ] `./gradlew testAndroidHostTest` がローカルで通るか
