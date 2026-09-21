# ADR-0022: 共通コンポーネントの置き場所は「ドメイン依存の有無」と「再利用範囲」で判定する

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（`composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/design/system/scaffold/SystemScaffold.kt`, `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/component/article/ArticleListItem.kt`, `composeApp/feature/user/src/commonMain/kotlin/org/starter/project/feature/user/component/UserProfile.kt`）
> 関連: [ui-layer.md](../guides/ui-layer.md), [0009-module-granularity.md](0009-module-granularity.md)

## Context

新しい再利用可能な Composable をどこに置くか（`design/system`/`shared/component/<domain>`/feature 内 `component/`）の基準がないと、コンポーネントの置き場所がプロジェクトの成長とともに乱雑になる。既存コードは `SystemScaffold`（ドメイン非依存）と `ArticleListItem`（ドメイン依存）を `ui` モジュール内の異なるサブパッケージに、`UserProfile`（画面専用）を `feature/user` 内に置いており、この使い分けが実例として存在する。

## Decision

- **必須**: ドメインモデル（`base` モジュールの型、例: `Article`/`User`）に依存しない汎用 UI プリミティブは `composeApp/ui` の `design/system/<カテゴリ>/` に `System` 接頭辞を付けて置く（例: `SystemScaffold`, `SystemSearchBar`）。
- **必須**: ドメインモデルに依存し、かつ 2 箇所以上の画面/モジュールから再利用されるコンポーネントは `composeApp/ui` の `shared/component/<domain>/` に接頭辞なしで置く（例: `ArticleListItem`）。
- **必須**: 特定の 1 画面でしか使われないコンポーネントは、feature モジュール内の `component/` サブパッケージに留める（例: `feature/user/component/UserProfile.kt`）。他画面での再利用が実際に発生した時点で `ui` モジュールへ昇格させる。
- **推奨**: `design/system/` と `shared/component/` 配下のコンポーネントは `modifier: Modifier = Modifier` を第一引数付近の必須パラメータとして持つ（feature 内 `component/` は省略可）。

## 判断基準

| 状況 | 判断 |
|---|---|
| ドメインモデルに依存せず複数画面から使う汎用 UI（ローディング表示等） | `design/system/<カテゴリ>/` に `System` 接頭辞で配置 |
| ドメインモデル（Article/User 等）に依存し、2 箇所以上の画面で使う | `shared/component/<domain>/` に配置（接頭辞なし） |
| 特定の 1 画面でしか使わない | feature 内の `component/` サブパッケージに留める |
| feature 内 `component/` のコンポーネントを別画面でも使いたくなった | `shared/component/<domain>/`（ドメイン依存）または `design/system/`（ドメイン非依存）へ昇格する |

## Consequences

- メリット: 新規コンポーネントの置き場所が機械的に決まり、`ui` モジュールが肥大化・混乱するのを防げる。
- デメリット / トレードオフ: 「2 箇所以上で使われたら昇格」の判断は実装時点で完全には予測できず、後からのモジュール間移動（feature から `ui` への移動）が発生しうる。
- 守らせる手段: レビュー（pr-checklist.md）で確認。

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| 全ての再利用可能コンポーネントを `ui` モジュールに集約する（ドメイン依存の有無を区別しない） | `design/system/`（汎用 UI プリミティブ）と `shared/component/<domain>/`（ドメイン依存コンポーネント）が実際に区別して配置されており（`SystemScaffold` vs `ArticleListItem`）、区別をなくすとドメインモデルへの依存が `ui` モジュール全体に広がる。 |
| 常に 1 回使っただけでも `ui` モジュールへ配置する | `UserProfile` のように 1 画面でしか使われないコンポーネントまで `ui` に置くと、`ui` モジュールが feature 固有の関心事で埋まり、共通モジュールとしての再利用性が損なわれる。 |

## サンプル削除後の扱い

`design/system/`（`SystemScaffold` 等）は `composeApp/ui` に残る共通基盤としてそのまま使えるが、`shared/component/article/`（`ArticleListItem` 等）は Zenn サンプル固有のためサンプル削除時に一緒に消える。新規ドメインのコンポーネントを追加する際は、本 ADR の判断基準に従い `shared/component/<新ドメイン>/` を新設すればよい。
