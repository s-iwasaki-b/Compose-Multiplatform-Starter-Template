# composeApp/domain/service

`domain:service` は Service interface と `ResultHandler`（`Result` 化ユーティリティ）を持つ契約モジュールです。本ファイルはこのモジュールで作業する実装エージェントの入口である。ルートの [AGENTS.md](../../../AGENTS.md)（絶対ルール・実行体制）を前提とし、ツールが自動で読み込まない場合は先に読む。実装エージェントは本モジュール外のファイルを編集せず、必要な他モジュールの変更は「他モジュールとの接点」に沿ってオーケストレーターへ報告する。読み順は本ファイル → 「参照」節の docs。

## 責務

- 置くもの: Service interface（`interface XxxService : Service`）、`Service` マーカー interface、`ResultHandler`（`async`/`immediate`/`asyncUnit`/`immediateUnit`）
- 置かないもの: Service の実装クラス（`XxxServiceImpl`。`domain/<name>` に置く）、機能固有のビジネスロジック、domain モデル（`base` に置く）、DI 登録（`composeApp/app` の `Koin.kt` に集約する）

## 依存

`build.gradle.kts`:
```kotlin
commonMain.dependencies { api(projects.composeApp.base) }
commonTest.dependencies { implementation(libs.bundles.test) }
```
plugins: `kmp-library` + `mokkery`

依存してよいのは `base` のみ（`api` 公開。Service interface のメソッドシグネチャに domain モデルが登場するため）。`data:repository`/`data:<source>` には依存しない。

## 構成

```text
src/commonMain/kotlin/org/starter/project/domain/service/
  Service.kt        # マーカー interface
  ResultHandler.kt   # Result 化ユーティリティ
  XxxService.kt       # Service interface（サンプル。削除後は存在しない）
```

## 実装パターン

Service interface（機能ごとに追記する）:
```kotlin
interface XxxService : Service {
    suspend fun fetchXxx(id: String): Result<XxxModel>
    fun getCachedXxx(): Result<XxxModel?>
}
```

`ResultHandler` の公開 API（実装側から見た使い分け）:
```kotlin
class ResultHandler(val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    suspend fun <T> async(dispatcher: CoroutineDispatcher = this.dispatcher, block: suspend () -> T): Result<T>
    suspend fun asyncUnit(dispatcher: CoroutineDispatcher = this.dispatcher, block: suspend () -> Unit): Result<Unit>
    fun <T> immediate(block: () -> T): Result<T>
    fun immediateUnit(block: () -> Unit): Result<Unit>
}
```
`domain/<name>` の実装は suspend メソッドを `async`、同期メソッドを `immediate` で包む。戻り値が `Unit` なら `asyncUnit`/`immediateUnit` を使う。`dispatcher` はテストで `StandardTestDispatcher()` に差し替える。

**推奨** 呼び出し単位の dispatcher 上書きは原則使わず、既定の `Dispatchers.IO` に委ねる（切り替え箇所を `ResultHandler` の 1 箇所に保つため）。

## テスト

テスト対象は `ResultHandler` など、このモジュールに追加するロジック（Service interface 自体はメソッド定義のみのためテスト不要）。配置は `src/commonTest/kotlin/org/starter/project/domain/service/`。依存を持たないユーティリティが中心のため Mokkery のモックは通常不要（`runTest`/`StandardTestDispatcher` で直接検証する）。現状このモジュールは `commonTest` 依存を宣言しているがテストファイルが無い（既知の逸脱。→ `../../../docs/coding-guide.md` §8）。`ResultHandler` を変更する場合は同 PR でテストを追加する。

実行コマンド: `./gradlew :composeApp:domain:service:testAndroidHostTest`

## 他モジュールとの接点

Service interface を追加・変更したら、以下をオーケストレーターへの完了報告に含める:
- 追加した Service interface 名とメソッドシグネチャ
- 実装を置く `domain/<name>` モジュール（新規の場合は `settings.gradle.kts` に `include(":composeApp:domain:<name>")` を明示的に追加する。`composeApp/build.gradle.kts` は動的スキャンのため編集不要）
- `composeApp/app` の `Koin.kt`（serviceModule）への登録要否
- feature からはこの interface 経由でのみ利用されること（Repository の直接注入は禁止）

## 完了条件

- `./gradlew :composeApp:domain:service:testAndroidHostTest` が通ること（テストを追加・変更した場合）
- Service interface が `Service` を継承し、メソッドシグネチャが `Result<T>` を返しているか
- このモジュールに実装クラス・domain モデル・DI 定義を置いていないか
- 他モジュールとの接点をオーケストレーターへの報告に含めたか
- `../../../docs/coding-guide.md` §8 の既知の逸脱を新規コードに複製していない。

## 参照

- ../../../docs/design-guide.md §3
- ../../../docs/coding-guide.md §3
- ../../../docs/decisions.md D-03, D-11, D-12, D-23
