# ADR-0002: Repository interface は `data:repository` に置く（古典的 DIP は採用しない）

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（`composeApp/data/repository/src/commonMain/kotlin/org/starter/project/data/repository/ZennRepository.kt`）
> 関連: [overview.md](../architecture/overview.md), [0001](0001-contract-and-impl-modules.md), [0010](0010-feature-depends-on-service-only.md)

## Context

一般的な Clean Architecture の解説では、Repository interface は domain 層が所有し、data 層がそれを実装する（依存関係逆転の原則、DIP）とされる。しかし本テンプレートでは Repository interface は `data:repository`（data 層）に置かれ、`domain:zenn`（実装モジュール）が `org.starter.project.data.repository.ZennRepository` を import して `implementation` 依存する。AI エージェントが一般知識に基づき「Repository interface は domain に置くべき」と判断すると、既存構成と矛盾したコードを書いてしまう。

## Decision

- **必須**: Repository interface は `data:repository` モジュールに置く。domain 層の実装モジュール（`domain:<name>`）はこのモジュールに `implementation` 依存し、interface を利用する。
- **必須**: Repository interface を domain 側のモジュールへ移動する「古典的 DIP」への変更は行わない。
- **推奨**: `domain:service`（Service interface）は data 層に依存しない独立モジュールのまま維持する。

## 判断基準

| 状況 | 判断 |
|---|---|
| 新しい Repository interface を追加する | `data:repository` に追記する（domain 側に新設しない） |
| Service がその Repository を使う | `domain:<name>` から `data:repository` へ `implementation` 依存を追加する |

## Consequences

- メリット: interface の置き場所が一意に定まり、機能追加のたびに「どちらに置くか」を再考する必要がない。実装モジュール（`data:<source>`）も `data:repository` のみに依存すればよく、循環依存を避けられる。
- デメリット / トレードオフ: Clean Architecture 解説記事が説く「domain が data に依存しない」という教科書的知識と表面上矛盾するため、AI エージェントが無指示で古典的 DIP を提案しがちで、都度この ADR を参照させる必要がある。
- 守らせる手段: Gradle の依存境界で強制（`domain:service` は `data:repository` に依存しない） | レビュー（pr-checklist.md）で確認

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| Repository interface を domain 側の契約モジュールに置く古典的 DIP（依存関係逆転の原則） | 本テンプレートの実例（[ADR-0001](0001-contract-and-impl-modules.md)）は一貫して data 側に interface を置いており、変更は大規模なモジュール再編を要する。既存の DI 登録（`Koin.kt`）・依存グラフとも不整合になる |
| `domain:zenn` が `data:zenn`（実装）に直接依存する | 実装同士の直接依存を許すと interface を介した差し替え・テストダブル注入ができなくなり、feature 層の依存境界（[ADR-0010](0010-feature-depends-on-service-only.md)）とも矛盾する |

## サンプル削除後の扱い

`data:repository` モジュールと `Repository` マーカー interface は Zenn サンプル削除後も残るため、新規 Repository の interface をそのまま追記すれば同じ配置パターンを再現できる。
