# ADR-0014: 入力バリデーションは Service 層、レスポンス整合性検証は Converter

> ステータス: 採用
> 日付: 2026-09-21
> 実例: 実例なし（本 ADR で新規決定。レスポンス整合性検証の `validateNotNull` 自体は実例あり `composeApp/base/src/commonMain/kotlin/org/starter/project/base/extension/ConversionErrorExtension.kt`）
> 関連: [data-layer.md](../guides/data-layer.md), [domain-layer.md](../guides/domain-layer.md), [ui-layer.md](../guides/ui-layer.md), [0003-result-at-service-boundary.md](0003-result-at-service-boundary.md), [0004-domain-models-in-base.md](0004-domain-models-in-base.md), [0019-error-display-routes.md](0019-error-display-routes.md)

## Context

現状唯一のバリデーション実装は `validateNotNull`（`composeApp/base/src/commonMain/kotlin/org/starter/project/base/extension/ConversionErrorExtension.kt`）で、これは DTO → domain モデル変換時に Converter が必須フィールドの欠落を検出するためのものである。ユーザー入力（keyword, username 等）に対するビジネスルールとしてのバリデーション（空文字チェック、文字数制限等）の実例はコード上に存在しない。この区別が明文化されていないと、AI エージェントがバリデーションを UI 層・Repository 層・Converter に無秩序に分散させてしまう。

## Decision

- **必須**: レスポンス DTO の整合性検証（必須フィールドの null 検査）は data 層の Converter で `validateNotNull` を使って行う。`ConversionError` を送出し、一覧変換は `mapNotNull` で 1 件ずつ許容する（既存パターン踏襲）。
- **必須**（新規決定）: ユーザー入力値のビジネスルールバリデーション（必須項目、文字数制限、フォーマット検証等）は domain 層の Service で行う。ViewModel や UI 層にバリデーションロジックを持たせない。
- **推奨**（新規決定）: バリデーション失敗は `ConversionError` とは別の sealed class（例: `ValidationError`）を `base` モジュールに新設し、Service の `Result.failure` として返す。
- **必須**（新規決定）: フォーム入力のバリデーションエラー（例: メールアドレス形式不正）は `UiState` のフィールド（例: `emailError: String?`）に載せ、入力欄直下に表示する。Service の `Result.failure(ValidationError)` を ViewModel が `.handle()` 内でフィールドへマッピングする（[ADR-0019](0019-error-display-routes.md) の経路表を参照）。

## 判断基準

| 状況 | 判断 |
|---|---|
| API レスポンスの必須フィールドが null かもしれない | Converter で `validateNotNull` |
| ユーザーが入力したキーワード/ユーザー名の空文字・文字数チェック | Service 層でバリデーションし `Result.failure(ValidationError)` を返す |
| UI 側の即時フィードバック（送信ボタンの活性制御等）に使う軽量な入力チェック | Composable/ViewModel 側で行ってよいが、最終検証は必ず Service でも行う（UI 側のチェックは体験向上の補助であり Service の検証を代替しない） |
| バリデーション失敗をどう画面に表示するか | `UiState` のフィールド（例: `emailError: String?`）に載せ、入力欄直下に表示する（新規決定。[ADR-0019](0019-error-display-routes.md) 参照） |

## Consequences

- メリット: 「DTO 都合の検証」と「ビジネスルールとしての入力検証」の責務が明確に分かれ、同じ場所への実装の集中を避けられる。
- デメリット / トレードオフ: サンプルにユーザー入力バリデーションの実例がなく、本決定は実装で検証されていない新規ルールである。
- 守らせる手段: レビュー（pr-checklist.md）で確認。

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| 入力バリデーションも `validateNotNull` 拡張関数で統一する | `validateNotNull` は DTO の null 検査専用に設計されており（`ConversionError` 送出）、ユーザー入力の文字数制限等の意味論とは異なる。同じ例外型を使うと発生源（Converter 起因かユーザー入力起因か）の切り分けができなくなる。 |
| ViewModel/UI 層でバリデーションを完結させる | Service を経由しないビジネスルールが生まれ、同じ Service を複数画面から呼ぶ場合にバリデーションが重複または漏れるリスクがある。ADR-0010（feature は service のみに依存する境界）とも整合しない。 |

## サンプル削除後の扱い

`validateNotNull` 自体は `base` モジュールに残るため Converter の検証パターンは実装として生き続ける。一方ユーザー入力バリデーションには実例がないため、本 ADR の判断基準表のみが削除後の根拠になる。
