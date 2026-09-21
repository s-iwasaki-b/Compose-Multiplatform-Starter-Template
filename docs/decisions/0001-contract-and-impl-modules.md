# ADR-0001: 契約モジュール（interface のみ）と実装モジュールを分離する

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（`composeApp/data/repository/src/commonMain/kotlin/org/starter/project/data/repository/Repository.kt`）
> 関連: [overview.md](../architecture/overview.md), [module-guide.md](../architecture/module-guide.md), [0002](0002-repository-interface-in-data-layer.md)

## Context

data 層・domain 層はいずれも「interface のみを持つ契約モジュール」（`data:repository`, `domain:service`）と「実装モジュール」（`data:zenn`, `domain:zenn`）に分離されている。新しい外部リソースやドメインを追加する AI エージェントが、この分離を踏襲すべきか、実装と interface を同一モジュールにまとめてよいかを判断できないと、モジュール構成に一貫性がなくなる。

## Decision

- **必須**: 新しい外部リソース/ドメインを data 層に追加する場合、interface は `data:repository` に置き、実装は新設する `data:<source>` モジュールに置く。domain 層も同様に、interface は `domain:service`、実装は `domain:<name>` に置く。
- **必須**: 実装クラスは `Impl` サフィックス（`XxxRepositoryImpl`, `XxxServiceImpl`）とし、interface と同一モジュールに同居させない。
- **必須**: 全 Repository/Service interface は空のマーカー interface（`Repository`/`Service`）を継承する。
- **推奨**: 契約モジュール（`data:repository`, `domain:service`）は `kmp-library` を基本とし、Ktorfit/KSP など実装専用のコード生成系プラグインは持ち込まない。テストを持つ契約モジュール（例: `domain:service`）には `mokkery` を適用してよい（`composeApp/domain/service/build.gradle.kts` は `kmp-library` + `mokkery` の2プラグイン構成）。

## 判断基準

| 状況 | 判断 |
|---|---|
| 新しい外部リソースの Repository を追加する | interface は `data:repository` に追記、実装は新設する `data:<source>` に置く |
| 新しい Service を追加する | interface は `domain:service` に追記、実装は新設する `domain:<name>` に置く |
| 実装を持たない小さなユーティリティを追加する | 契約モジュールへの配置は不要。`base` または実装モジュール内に留める |

## Consequences

- メリット: 依存境界を Gradle レベルで強制でき、feature 層は実装コードを一切巻き込まずに interface のみに依存できる。実装の差し替え・テストダブル注入が容易。
- デメリット / トレードオフ: モジュール数が増え、小規模な追加機能でも新規モジュール作成のオーバーヘッドが発生する。
- 守らせる手段: Gradle の依存境界で強制（実装モジュール同士は直接依存できない） | レビュー（pr-checklist.md）で確認

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| interface と実装を同一モジュールに同居させる（多くの Android サンプルで見られる構成） | 実装差し替え・テストダブル注入時にコンパイル依存を絞れず、feature モジュールが実装コードまで巻き込んでしまう。Gradle で依存境界を強制できなくなる |
| 機能ごとに `<feature>/api`・`<feature>/impl` のような対称ペアモジュールを作る | 本テンプレートは契約を役割単位（`data:repository`/`domain:service`）に一元集約する構成であり、機能ごとに契約モジュールを増殖させると集約先が分散し、DI 登録の一貫した置き場所が失われる |

## サンプル削除後の扱い

Zenn サンプルが削除されても `data:repository`/`domain:service` モジュールとマーカー interface（`Repository`/`Service`）は空のまま残るため、新規実装時にそのまま interface を追記すればこのパターンを再現できる。
