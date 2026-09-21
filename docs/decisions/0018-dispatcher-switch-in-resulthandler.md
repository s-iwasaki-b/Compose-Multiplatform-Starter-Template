# ADR-0018: Dispatcher 切替は `ResultHandler` の 1 箇所のみ。Dispatcher はコンストラクタ注入

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（`composeApp/domain/service/src/commonMain/kotlin/org/starter/project/domain/service/ResultHandler.kt`）
> 関連: [testing.md](../guides/testing.md), [error-handling.md](../architecture/error-handling.md), [0003-result-at-service-boundary.md](0003-result-at-service-boundary.md)

## Context

`withContext` の呼び出し箇所が複数層に分散すると、不要な二重コンテキストスイッチや、テストでの Dispatcher 差し替えが困難になる。現状は `ResultHandler.async`/`immediate` の 1 箇所のみに Dispatcher 切替が集約されており、Repository/DataSource/Converter/ViewModel は Dispatcher を切り替えない。

## Decision

- **必須**: `withContext` による Dispatcher 切り替えは `domain/service` の `ResultHandler`（`async`/`immediate`）の 1 箇所のみに集約する。Repository/DataSource/Converter/ViewModel は自前で `withContext` や `Dispatchers.IO` を参照しない。
- **必須**: `ResultHandler` は Dispatcher を `val dispatcher: CoroutineDispatcher = Dispatchers.IO` としてコンストラクタのデフォルト引数で受け取る。将来同様の I/O 境界クラスを追加する場合も同じパターン（デフォルト値付きコンストラクタ注入）を踏襲する（新規決定）。
- **必須**: `Dispatchers.IO` は `kotlinx.coroutines.IO`（マルチプラットフォーム版、iOS でも動作する limited-parallelism 実装）を import して使う。Android 専用の Dispatcher と誤認しない。
- **推奨**: テストでは `StandardTestDispatcher()` を `ResultHandler(testDispatcher)` のコンストラクタ引数として渡し、`runTest(testDispatcher) { ... }` を使う。ViewModel テストを追加する場合は `Dispatchers.setMain(StandardTestDispatcher())` を `@BeforeTest`、`Dispatchers.resetMain()` を `@AfterTest` に置く（新規決定）。

## 判断基準

| 状況 | 判断 |
|---|---|
| Repository 実装でネットワーク呼び出しをする | `withContext` を書かない。`suspend fun` としてそのまま呼ぶ（呼び出し元の `ResultHandler` が境界を切る） |
| Service 実装の各メソッド | `resultHandler.async{}`/`immediate{}` で包む。中で `withContext` を書かない |
| `ResultHandler` 以外に新しい I/O 境界クラスが必要になった | 極力 `ResultHandler` 経由に統一する。どうしても必要な場合はコンストラクタ注入可能な `CoroutineDispatcher` を持たせる |
| ViewModel のテストでコルーチンを制御したい | `Dispatchers.setMain(StandardTestDispatcher())` をセットする（`ResultHandler` のコンストラクタ注入とは別の仕組み） |

## Consequences

- メリット: Dispatcher 切り替え箇所が 1 つに定まり、二重切り替えによる無駄なコンテキストスイッチを避けられる。`StandardTestDispatcher` の注入によりテストの決定性を保てる。
- デメリット / トレードオフ: 新しい横断的関心事（別の I/O 境界）が生まれたとき、`ResultHandler` 以外の場所に `withContext` が必要になるケースが将来出てくる可能性があり、その際は本 ADR の例外として明記が必要になる。
- 守らせる手段: レビュー（pr-checklist.md）で確認。lint 等の機械的な強制は導入していない。

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| 各層（Repository/Converter）が個別に `withContext(Dispatchers.IO)` を挟む | 呼び出しチェーンの途中で複数回 `withContext` が呼ばれ、不要なスレッド切り替えコストが発生する。既存実装では `ResultHandler` 以外に `withContext` の使用箇所がない（grep 確認済み）。 |
| Dispatcher を Koin からグローバルに注入し、コンストラクタのデフォルト値を使わない | テストごとに異なる Dispatcher を注入する際に Koin モジュールの差し替えが必要になり記述コストが上がる。既存の `ZennServiceTest` はコンストラクタ引数で直接注入しており DI を経由していない。 |

## サンプル削除後の扱い

`ResultHandler` 自体は `domain/service` モジュールにありサンプル（Zenn）削除の対象外のため、この実装はそのまま残る。新規 Service を実装する際も `ResultHandler` を再利用すればよく、本 ADR の規約は実装として維持され続ける。
