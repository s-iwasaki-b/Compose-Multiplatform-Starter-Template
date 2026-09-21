# ADR-0020: Material 2 を維持し、テーマ値はプレースホルダとして利用者が差し替える

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（`composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/design/system/theme/SystemTheme.kt`）
> 関連: [ui-layer.md](../guides/ui-layer.md), [coding-style.md](../guides/coding-style.md)

## Context

現状 `SystemTheme` は `androidx.compose.material`（Material 2）のみを使用し、`LocalCustomColors`/`LocalCustomTypography`/`LocalCustomShapes` はそれぞれ `lightColors()`/`Typography()`/`Shapes()` の初期値のまま `// TODO: replace ... with your custom theme` というコメントが付いている。ダークモード対応も未実装。この状態を「バグ」として直すべきか「意図的なプレースホルダ」として維持すべきかが不明だと、AI エージェントが独断で Material 3 移行やテーマ値の変更を行ってしまうリスクがある。

## Decision

- **必須**: Material 2（`androidx.compose.material`）を維持する。Material 3 への移行はライブラリ選定レベルの意思決定であり、AI エージェントが自律的に行わない。
- **必須**: `SystemTheme.kt` の `LocalCustomColors`/`LocalCustomTypography`/`LocalCustomShapes` の 3 つの初期値（`lightColors()`/`Typography()`/`Shapes()`）は、本テンプレートを利用するプロジェクト固有の値へ差し替える前提のプレースホルダである。TODO コメントは削除せず残し、値の差し替えは利用者の作業として明示する。
- **推奨**: ダークモード対応が必要な場合は、`LocalCustomColors` 等の生成箇所に `isSystemInDarkTheme()` による分岐を追加する（現状は `lightColors()` 固定でダークモード未対応）。

## 判断基準

| 状況 | 判断 |
|---|---|
| 新しい Composable で色・タイポグラフィ・シェイプを参照したい | `MaterialTheme.colors` 等を直接使わず `SystemTheme.colors`/`SystemTheme.typography`/`SystemTheme.shapes` 経由で参照する |
| プロジェクト固有の配色に差し替えたい | `SystemTheme.kt` の `LocalCustomColors` 等の初期値を書き換える（AI エージェントが要求に応じて行ってよい） |
| Material 3 への移行を検討したい | 人間の判断を仰ぐ。AI エージェント単独で移行を実施しない |
| ダークモード対応を追加したい | `isSystemInDarkTheme()` の分岐を `LocalCustomColors` 等の生成箇所に追加する |

## Consequences

- メリット: テーマ実装のスコープが明確になり、AI エージェントが要求されていない Material 3 移行やテーマ全面刷新を行うリスクを防げる。
- デメリット / トレードオフ: Material 2 は今後の Compose Multiplatform エコシステムで非推奨方向に進む可能性があり、テンプレートとして長期的には Material 3 対応ガイドが別途必要になりうる。
- 守らせる手段: レビュー（pr-checklist.md）で確認。

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| Material 3 へ本 ADR の時点で移行する | サンプル実装全体（`androidx.compose.material.*` の import）が Material 2 前提であり、移行は大規模リファクタになる。ライブラリ選定は利用者ごとの判断に委ねるべき事項。 |
| プレースホルダのテーマ値をこのタイミングで具体的なブランドカラーに決めてしまう | 本リポジトリはスターターテンプレートであり、特定のブランド色を決め打ちすると利用者全員がそれを上書きする前提になり、プレースホルダとしての意図（TODO コメント）と矛盾する。 |

## サンプル削除後の扱い

`SystemTheme.kt` は Zenn サンプルの一部ではなく `composeApp/ui` の共通基盤として残るため、この決定はテンプレート利用者に直接影響する。利用者は本 ADR に従い TODO コメントの 3 箇所を差し替えるだけで済む。
