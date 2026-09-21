# composeApp/domain/zenn

`domain/<name>` は Service 実装（`XxxServiceImpl`）を持つモジュールです（本書は `domain/zenn` を具体例として一般化しています）。本ファイルはこのモジュールで作業する実装エージェントの入口である。ルートの [AGENTS.md](../../../AGENTS.md)（絶対ルール・実行体制）を前提とし、ツールが自動で読み込まない場合は先に読む。実装エージェントは本モジュール外のファイルを編集せず、必要な他モジュールの変更は「他モジュールとの接点」に沿ってオーケストレーターへ報告する。読み順は本ファイル → 「参照」節の docs。

## 責務

- 置くもの: Service 実装クラス（`XxxServiceImpl`）、Service 実装のテスト（`XxxServiceTest`）
- 置かないもの: Service interface（`domain:service`）、domain モデル（`base`）、Repository 実装（`data:<source>`）、DI 登録（`composeApp/app` の `Koin.kt`）

## 依存

`build.gradle.kts`:
```kotlin
commonMain.dependencies {
    implementation(libs.bundles.domain)
    api(projects.composeApp.base)
    implementation(projects.composeApp.data.repository)
    implementation(projects.composeApp.domain.service)
}
commonTest.dependencies { implementation(libs.bundles.test) }
```
plugins: `kmp-library` + `mokkery`。依存してよいのは `base`（`api`）、`data:repository`（Repository interface）、`domain:service`。`data:<source>`（Repository 実装モジュール）には依存しない。

## 構成

```text
src/commonMain/kotlin/org/starter/project/domain/zenn/
  ZennServiceImpl.kt
src/commonTest/kotlin/org/starter/project/domain/zenn/
  ZennServiceTest.kt
```
新規 `domain/<name>` モジュールもこれと同じ2ディレクトリ構成にする。

## 実装パターン

単一 Repository への委譲:
```kotlin
class XxxServiceImpl(
    private val resultHandler: ResultHandler,
    private val xxxRepository: XxxRepository
) : XxxService {
    override suspend fun fetchXxx(id: String) = resultHandler.async {
        xxxRepository.fetchXxx(id)
    }
}
```
複数 Repository の合成（コンストラクタ注入で束ねる。`ZennServiceImpl.fetchArticles` は publication 検索が空なら user 検索にフォールバックする）:
```kotlin
class XxxServiceImpl(
    private val resultHandler: ResultHandler,
    private val xxxRepository: XxxRepository,
    private val yyyRepository: YyyRepository
) : XxxService {
    override suspend fun fetchXxx(id: String) = resultHandler.async {
        val primary = xxxRepository.fetchXxx(id)
        if (primary.isEmpty()) yyyRepository.fetchFallback(id) else primary
    }
}
```
共有状態を持つ Service（画面間・アプリ全体で状態を共有する場合。interface は `domain:service` に置く。（新規決定）→ decisions.md D-23）:
```kotlin
class AppSettingsServiceImpl(
    private val resultHandler: ResultHandler,
    private val repository: AppSettingsRepository
) : AppSettingsService {
    private val _themeMode = MutableStateFlow(repository.getThemeMode())
    override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    override suspend fun setThemeMode(mode: ThemeMode) = resultHandler.asyncUnit {
        repository.setThemeMode(mode)
        _themeMode.value = mode
    }
}
```

## テスト

Service 実装（`XxxServiceImpl`）は必須テスト対象。依存する Repository interface を Mokkery でモックする（`mock<XxxRepository>(MockMode.autofill)`）。配置は `src/commonTest/kotlin/org/starter/project/domain/<name>/XxxServiceTest.kt`。`ResultHandler(testDispatcher)` に `StandardTestDispatcher()` を注入し `runTest(testDispatcher)` を使う。`everySuspend {} returns/throws`、`verifySuspend {}` で検証する。
実行コマンド: `./gradlew :composeApp:domain:zenn:testAndroidHostTest`（新規モジュールは `:composeApp:domain:<name>:testAndroidHostTest`）

## 他モジュールとの接点

以下をオーケストレーターへの完了報告に含める:
- Service interface の追加・変更が必要な場合、`domain:service` への追記を依頼する（このモジュールでは編集しない）
- 新規実装モジュールとして作成した場合、`settings.gradle.kts` に `include(":composeApp:domain:<name>")` を明示的に追加する（`composeApp/build.gradle.kts` は動的スキャンのため編集不要）。加えて `composeApp/app` の `Koin.kt`（serviceModule）への登録が必要
- feature からはこの実装クラスを直接 import させず、`domain:service` の interface 経由でのみ利用されること

## 完了条件

- `./gradlew :composeApp:domain:zenn:testAndroidHostTest` が通ること
- Service 実装の全メソッドが `resultHandler.async`/`immediate`（`Unit` は `asyncUnit`/`immediateUnit`）で包まれているか
- `data:repository` の interface にのみ依存し、`data:<source>` に依存していないか
- 複数 Repository を扱う場合、コンストラクタ注入で束ねているか
- Service 実装のテストを同一モジュールの `commonTest` に置いたか
- `../../../docs/coding-guide.md` §8 の既知の逸脱を新規コードに複製していない。

## 参照

- ../../../docs/design-guide.md §3
- ../../../docs/coding-guide.md §3, §5
- ../../../docs/decisions.md D-03, D-09, D-11, D-14, D-23
