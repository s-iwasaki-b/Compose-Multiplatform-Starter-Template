# ADR-0012: Repository/Service が `Flow` を返すのは継続監視が要件のときのみ

> ステータス: 採用
> 日付: 2026-09-21
> 実例: 実例なし（本 ADR で新規決定）
> 関連: [data-layer.md](../guides/data-layer.md), [error-handling.md](../architecture/error-handling.md), [0002-repository-interface-in-data-layer.md](0002-repository-interface-in-data-layer.md), [0003-result-at-service-boundary.md](0003-result-at-service-boundary.md), [0023-shared-state-in-service.md](0023-shared-state-in-service.md)

## Context

サンプル実装（`ZennRepository`/`ZennService`）は全メソッドが `suspend fun ... : T` または `Result<T>` を返す単発取得で、`Flow` の使用例は data/domain 層に一切ない。ローカル設定の変更を UI がリアルタイムに監視したい、Web ソケット等の継続的な通知を扱いたいといった要件が今後出てきたとき、どのメソッドを `Flow` 化すべきかの判断基準がないと、AI エージェントは一貫性のために全メソッドを `Flow` 化する、あるいは逆に継続監視が必要な場面でも単発取得のまま実装してしまう。

## Decision

- **必須**: Repository/Service の公開メソッドは、値の継続的な変化を画面が購読し続ける必要があると明示的に判断した場合のみ `Flow<T>` を返す。それ以外は既定の `suspend fun ... : T`（Repository）/ `suspend fun ... : Result<T>`（Service、ADR-0003 参照）にする。
- **必須**: API を 1 回だけ呼ぶ取得、起動時に 1 回だけ読むローカル設定（例: `getLastKeyword`）は `Flow` 化しない。
- **推奨**: `Flow` を公開する場合、例外処理の責務（Service 層で `Result` にラップするか、`Flow` 自体で例外を流すか）は都度コメントまたはガイドに明記する。

## 判断基準

| 状況 | 判断 |
|---|---|
| API から 1 回だけ値を取得する | `suspend fun ... : T`（Repository）/ `Result<T>`（Service） |
| ローカル設定を起動時に 1 回だけ読む | 同期 `fun ... : T?` |
| ローカル設定の変更を UI がリアルタイムに反映する必要がある | `Flow<T>` を検討する |
| WebSocket 等、外部からのプッシュ通知を継続監視する | `Flow<T>` |
| 複数画面・アプリ全体で共有する状態（カート、ログイン状態、テーマ設定等）を `StateFlow` として公開する | `Flow<T>`（`StateFlow<T>`）を使う。継続監視に該当する（[ADR-0023](0023-shared-state-in-service.md)） |

## Consequences

- メリット: API 呼び出しごとに `Flow` の収集・キャンセル管理コストを負わずに済み、既存の `suspend fun` 規約と一貫性を保てる。
- デメリット / トレードオフ: 「継続監視が必要か」の判断がやや主観的で、プルツーリフレッシュのような境界事例で解釈が割れうる。
- 守らせる手段: レビュー（pr-checklist.md）で確認。Gradle 等での機械的強制はできない。

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| 一貫性のため全メソッドを `Flow` に統一する | 単発取得に `Flow` を使うと呼び出し側で `.first()` 等の追加処理が必要になり、既存の `suspend fun`/`Result<T>` 規約（ADR-0003）と矛盾する。`ZennRepository`/`ZennService` の全メソッドが単発であるという実例とも乖離する。 |
| Repository 層は全て `Flow` で返し、Service 層で `combine` して `Result` にラップする | `Flow<Result<T>>` のような二重の非同期ラップになり呼び出し側の複雑性が増す。実例が存在しない。 |

## サンプル削除後の扱い

`ZennRepository`/`ZennService` は `Flow` を使わない実例のまま削除されるため、上記の判断基準表が唯一の参照先になる。新規 Repository/Service のメソッドを設計する際は、この if-then 表だけで `Flow` にするかどうかを判定できる。
