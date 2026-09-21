# ADR-0003: Repository は例外透過、Service が `ResultHandler` で `Result` 化する

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（`composeApp/domain/service/src/commonMain/kotlin/org/starter/project/domain/service/ResultHandler.kt`）
> 関連: [error-handling.md](../architecture/error-handling.md), [data-layer.md](../guides/data-layer.md), [domain-layer.md](../guides/domain-layer.md)

## Context

エラーハンドリングの責務がどの層にあるか不明確だと、Repository が早期に `Result` を返す実装や、Service 層での二重ラップが起きる。本テンプレートは Repository 層を例外透過（生の型を返す・例外はそのまま throw）とし、Service 層のみが `ResultHandler` で `Result<T>` に変換するという一貫した境界を持つ。

## Decision

- **必須**: Repository interface/実装は `Result` を使わず、素の domain モデルを返すか例外をそのまま throw する。
- **必須**: Service 実装の全公開メソッドは `resultHandler.async { }`（suspend）または `.immediate { }`（同期）で本体を包み、戻り値は必ず `Result<T>` にする。
- **必須**: `withContext` による Dispatcher 切り替えは `ResultHandler` 内のみで行う。Repository/DataSource/Converter/ViewModel で Dispatcher を切り替えない。
- **推奨**: 例外ログは `Napier.e` で出す（現状の `ResultHandler` 実装は `Napier.d` になっており既知の逸脱）。

## 判断基準

| 状況 | 判断 |
|---|---|
| Repository で通信/変換エラーが起きた | 握りつぶさずそのまま throw する（Service まで伝播させる） |
| Service で複数 Repository 呼び出しをまとめる | 各呼び出しを同じ `resultHandler.async { }` ブロック内で行い、まとめて 1 回だけ Result 化する |
| CancellationException が発生した | `ResultHandler` 内の `coroutineContext.ensureActive()` により再送出させる。握りつぶさない |

## Consequences

- メリット: `Result` 化のロジックが 1 箇所に閉じ、テストでの差し替え・仕様変更が容易。ViewModel は常に `Result<T>` を受け取ればよく、型が層をまたいで統一される。
- デメリット / トレードオフ: Repository のテストでは例外がそのまま throw されるかを検証し、Service のテストでは `Result.failure` への変換を別途検証する必要があり、2 層でテストが必要になる。
- 守らせる手段: レビュー（pr-checklist.md）で確認 | なし（Gradle では強制できない）

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| Repository も `Result<T>` を返す | Service 層で「Repository が返した Result をさらに Result でラップする」二重ラップが発生しやすく、実例（`ZennRepositoryImpl`）とも矛盾する |
| `runCatching` を Repository/Converter でも使う | 例外の発生源を分散させると `ResultHandler` の `ensureActive()` によるキャンセル保護が効かなくなり、構造化並行性が壊れる箇所が増える |

## サンプル削除後の扱い

`ResultHandler` は `domain/service` モジュールに残るユーティリティであり、Zenn サンプルが消えてもこの境界規約はコードと共に残る。新規 Service は既存の `ResultHandler` をそのまま再利用すればよい。
