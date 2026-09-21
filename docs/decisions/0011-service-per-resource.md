# ADR-0011: Service は外部リソース単位の interface に複数メソッド（1 クラス 1 メソッドの UseCase は採用しない）

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（`composeApp/domain/service/src/commonMain/kotlin/org/starter/project/domain/service/ZennService.kt`）
> 関連: [domain-layer.md](../guides/domain-layer.md), [0003](0003-result-at-service-boundary.md)

## Context

AI エージェントは一般知識から「UseCase/Interactor は 1 クラス 1 責務（1 メソッド）にすべき」という設計原則を提案しがちである。本テンプレートの唯一の実例 `ZennService` は、記事取得・ユーザー取得・キーワード保存など関連する 4 メソッドを 1 つの interface に集約しており、画面単位・操作単位の細分化は行っていない。

## Decision

- **必須**: Service は対象とする外部リソース（ドメイン）ごとに 1 つの interface とし、関連する複数の操作を複数メソッドとして持たせる。`FetchXxxUseCase` のような 1 メソッドのみを持つクラスを新設しない。
- **必須**: Service interface の命名は `XxxService`（`XxxUseCase` は使わない）とし、共通マーカー interface `Service` を継承する。
- **推奨**: 同じ外部リソースに対する新しい取得パターンは、新しい Service を作らず既存 Service にメソッドを追加する。
- **推奨**: 内部的に類似した Repository 呼び出しでも、ViewModel 側の用途が異なる場合は用途別に公開メソッドを分ける（例: `fetchArticles` と `fetchUserArticles`）。汎用的なパラメータ分岐メソッド 1 本にまとめない。

## 判断基準

| 状況 | 判断 |
|---|---|
| 既存の外部リソースに対する新しい取得パターンが必要 | 既存 Service に新しい public メソッドを追加する |
| 新しい外部リソース（API/永続化対象）を追加する | 新しい Service interface を `domain:service` に追加し、実装モジュール `domain:<name>` を新設する |
| 複数の Repository を束ねる必要がある | 束ねる専用の中間層（Coordinator 等）を新設せず、Service のコンストラクタに複数 Repository を注入して束ねる（推奨。実例なし、新規決定） |
| 画面ごとに別の Service を作りたくなった | 作らない。Service は画面単位ではなく外部リソース単位を維持する |
| 画面が2つ以上の独立した外部リソース（別々の Service が扱うデータ）を表示する | 表示上の単純な並置なら ViewModel が複数 Service を個別に注入して呼ぶ（任意）。フィルタ・キー結合・派生値算出などの業務ロジックとしての結合が必要なら、合成結果の概念名を持つ新しい Service（例: `DashboardService`）を `domain:service`/`domain:<name>` に作り、複数 Repository をコンストラクタ注入で束ねる（推奨・新規決定。詳細は [domain-layer.md](../guides/domain-layer.md) 判断基準8） |

## Consequences

- メリット: 1 つの外部リソースに対する操作がすべて 1 つの interface に集約され、ViewModel からの依存注入がシンプルになる（複数の細かい UseCase クラスを大量に注入する必要がない）。
- デメリット / トレードオフ: 1 つの Service が多くのメソッドを持つと肥大化しやすく、責務が広がりすぎた場合の分割基準は実例から判断できず別途運用で補う必要がある。
- 守らせる手段: レビュー（pr-checklist.md）で確認（Gradle では強制できない）

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| 1 クラス 1 メソッドの UseCase/Interactor パターン（Clean Architecture の一般的な解説でよく見る構成） | AI エージェントが一般知識から最も提案しがちな代替案だが、実例（`ZennService` の 4 メソッド構成）と明確に矛盾し、機能追加のたびに `FetchXxxUseCase` のような単機能クラスが大量生成され、DI 登録・ViewModel の注入対象が肥大化する |
| Service の戻り値を `Flow<T>` にして継続監視できるようにする | 実例は全メソッドが `Result<T>`（単発）であり、`Flow` の使用例が無い。継続監視が要件として明示された場合のみ個別に検討する（本 ADR の範囲外） |

## サンプル削除後の扱い

`domain:service` モジュールと `Service` マーカー interface は Zenn サンプル削除後も残るため、新規外部リソースを追加する際はこの粒度基準（1 リソース 1 interface・複数メソッド）に従って interface を追記すればよい。具体的なメソッドシグネチャのテンプレートは `domain-layer.md` を参照する。
