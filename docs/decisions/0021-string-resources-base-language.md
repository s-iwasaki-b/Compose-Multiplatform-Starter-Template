# ADR-0021: 表示文言は Compose Resources に必ず切り出し、ベース言語はプロジェクトで 1 つ（本テンプレートは日本語）

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（不統一。`composeApp/ui/src/commonMain/composeResources/values/string.xml`, `composeApp/feature/user/src/commonMain/composeResources/values/string.xml`）
> 関連: [ui-layer.md](../guides/ui-layer.md)

## Context

現状 `composeApp/ui` の文字列は日本語（`string.xml`）、`composeApp/feature/user` の文字列は英語（`string.xml`）で、プロジェクト内で表示言語が統一されていない。ロケール別ファイル（`values-ja`/`values-en` 等）もなく多言語対応もされていない。このままでは新規画面追加時にどちらの言語に合わせるべきか AI エージェントが判断できない。

## Decision

- **必須**: 画面に表示する固定文言はハードコードせず、必ず `composeResources/values/string.xml` に切り出し `stringResource(Res.string.xxx)` で参照する。
- **必須**: プロジェクト全体でベース言語を 1 つに統一する。本テンプレートは `composeApp/ui`（共通基盤モジュール）が日本語であるため、ベース言語は日本語とする。
- **必須**（既知の逸脱の是正）: `composeApp/feature/user` の英語文言（`user_stat_articles` 等）は既知の逸脱であり、新規開発時はこれを模倣せず日本語に統一する。
- **推奨**: Res クラスの `publicResClass` は、他モジュールから参照される共通モジュール（`composeApp/ui` 等）のみ `true` にし、feature 固有モジュールは `false` にする（ADR-0022 とも関連）。

## 判断基準

| 状況 | 判断 |
|---|---|
| 新規画面に固定文言を追加する | `composeResources/values/string.xml` に日本語で追加し `stringResource(Res.string.xxx)` で参照する |
| `composeApp/feature/user` の既存の英語文言を参考にしたい | 参考にしない。既知の逸脱であり新規追加は日本語で統一する |
| 動的データ（例: `"@${user.username}"` のような組み立て文字列）を表示したい | リソース化の対象外（ユーザー向け固定文言ではないため） |
| 多言語対応（`values-en` 等）を追加すべきか | 本 ADR のスコープ外。ベース言語（日本語）を 1 つ用意することのみを必須とし、多言語化は要件が明示されたときに追加する |

## Consequences

- メリット: 文言の一元管理ができ、将来の多言語対応（`values-ja`/`values-en` 追加）への移行コストを抑えられる。プロジェクト全体で言語が統一され、レビュー時の違和感がなくなる。
- デメリット / トレードオフ: 既存の `feature:user` の英語文言は是正が必要になり、既存のリソースキー（`user_stat_articles` 等）を参照している Composable の修正も伴う。
- 守らせる手段: レビュー（pr-checklist.md）で確認。

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| モジュールごとに言語を自由にする（現状追認） | 現に `ui` が日本語、`feature:user` が英語で混在しており、AI エージェントが新規モジュールの言語を決められず、プロジェクトとしての一貫性が失われる。 |
| ベース言語を英語に統一する | `composeApp/ui`（最も参照範囲が広い共通モジュール）が既に日本語で確立されており、`ui` の文言を英語に翻訳し直すコストの方が `feature:user` の 3 件を日本語に直すコストより大きい。 |

## サンプル削除後の扱い

`composeApp/ui` の `string.xml`（日本語）はサンプル削除後も共通基盤として残るため、ベース言語＝日本語という前例はそのまま維持される。`feature:user`（英語）はサンプル削除時に一緒に消えるため、既知の逸脱も同時に解消される。
