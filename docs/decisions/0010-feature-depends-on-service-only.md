# ADR-0010: feature は `domain:service` と `ui` にのみ依存し data 層に触れない（Gradle で強制）

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（`composeApp/feature/home/build.gradle.kts`）
> 関連: [overview.md](../architecture/overview.md), [0002](0002-repository-interface-in-data-layer.md), [0008](0008-di-centralized-in-app.md)

## Context

ViewModel が Repository を直接呼び出してよいかが不明だと、ビジネスロジックが UI 層に漏れ出し、レイヤー境界が崩れる。本テンプレートは `feature:home`/`feature:user` の `build.gradle.kts` で `data.repository`・`domain.<name>`（実装）への依存を一切宣言しておらず、Gradle のモジュール境界レベルで Repository 直呼びが物理的に不可能になっている。

## Decision

- **必須**: `feature:<name>` モジュールの依存は `api(projects.composeApp.ui)` と `implementation(projects.composeApp.domain.service)` のみとする。`data:repository`・`data:<source>`・`domain:<name>`（実装）への依存を追加しない。
- **必須**: ViewModel のコンストラクタ注入対象は `domain:service` の interface（`XxxService`）のみとする。Repository を直接注入しない。
- **推奨**: 単純な CRUD に見える処理であっても Service を経由させる（将来複数 Repository を束ねる拡張に備える）。

## 判断基準

| 状況 | 判断 |
|---|---|
| 新しい feature モジュールを作る | `build.gradle.kts` の依存は `ui` と `domain.service` のみにする |
| ViewModel が単純な 1 メソッド呼び出ししか必要としない | それでも Service を経由させる。Repository 直接注入は行わない |
| Service にまだ必要なメソッドが無い | `domain:service` の interface にメソッドを追加してから使う（feature モジュール側で `data` への依存を追加しない） |

## Consequences

- メリット: Gradle の依存グラフ自体が「feature → data」の依存を持ち込めないため、コードレビューを待たずビルド時に境界違反を検出できる（実装を追加した瞬間コンパイルエラーになる）。
- デメリット / トレードオフ: Service にまだ存在しないメソッドが必要な場合、feature モジュールの変更だけでなく `domain:service`/`domain:<name>` の変更も同時に必要になり、変更範囲が 2 モジュールにまたがる。
- 守らせる手段: Gradle の依存境界で強制（`feature` の `build.gradle.kts` に `data` 系依存を追加しない限りコンパイルできない）

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| 単純な CRUD 画面に限り ViewModel が Repository を直接注入する | 「単純」の基準が曖昧になりやすく、一部 feature だけ例外を認めると境界がなし崩しになる。実例（`HomeScreenViewModel`/`UserScreenViewModel`）はいずれも Service のみに依存しており例外が無い |
| レイヤー境界をコードレビューのみで運用し、Gradle 依存は緩めておく | 本テンプレートは CI（静的解析・アーキテクチャテスト）を持たないため、Gradle の依存境界こそが唯一の機械的な強制手段。緩めると境界違反がレビューをすり抜けるリスクが高い |

## サンプル削除後の扱い

`feature:home`/`feature:user` の `build.gradle.kts` 自体は削除されるが、この依存許可ルールは `module-guide.md` の依存許可表として残る。新規 feature モジュールを作る際はそのテンプレート（`api(projects.composeApp.ui)` + `implementation(projects.composeApp.domain.service)`）をそのまま複製すればよい。
