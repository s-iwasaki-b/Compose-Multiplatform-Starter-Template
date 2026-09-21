# ADR-0024: UI から呼ぶプラットフォーム固有機能は interface を `ui` に置き、`app` の `platformModule` で実装を注入する

> ステータス: 採用
> 日付: 2026-09-21
> 実例: 実例なし（本 ADR で新規決定）
> 関連: [coding-style.md](../guides/coding-style.md), [ui-layer.md](../guides/ui-layer.md), [dependency-injection.md](../architecture/dependency-injection.md), [module-guide.md](../architecture/module-guide.md)

## Context

`expect`/`actual` は `core`/`app` にのみ置く規約（coding-style.md STYLE-16）になっているが、共有シート・外部ブラウザ起動・クリップボードなど UI 起点で発火するプラットフォーム固有機能を `ui`/`feature` からどう呼び出すかの配線パターンが全ドキュメントに存在しなかった。現状唯一の `expect`/`actual` の実例は Koin の `platformModule`（`expect val platformModule: org.koin.core.module.Module` を `composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt` で宣言し、`composeApp/app/src/androidMain/kotlin/org/starter/project/di/Koin.android.kt` と `composeApp/app/src/iosMain/kotlin/org/starter/project/di/Koin.ios.kt` で実装する）であり、これは `Settings.Factory` のみを提供している。

## Decision

- **必須**（現状未実装の差分）: UI から呼び出すプラットフォーム固有機能は、`composeApp/ui` に interface（例: `ShareHandler { fun share(text: String) }`）を置く。
- **必須**（現状未実装の差分）: 実装クラスは `composeApp/app` の `androidMain`/`iosMain` に置き、既存の `platformModule`（`expect val platformModule: org.koin.core.module.Module`）にバインドを追加する。新しい `expect`/`actual` の宣言先を増やさず、既存の `platformModule` に集約する。
- **必須**: feature はこの interface を EventHandler または ViewModel のコンストラクタで注入して使う。Android/iOS の実装クラスを feature が直接 import しない。
- **推奨**: この分離は既存の `AppRouter`（interface は `ui`、実装は `app`）と同じパターンであり、命名・配置とも揃える。

## 判断基準

| 状況 | 判断 |
|---|---|
| UI から発火するプラットフォーム固有機能（共有シート、外部ブラウザ、クリップボード等）を追加したい | `ui` に interface を置き、`app` の `platformModule` に Android/iOS 実装を追加する |
| ロジックを持たない単純な値の差異（画面サイズ判定の定数等） | `expect`/`actual` の対象にせず、`core`/`ui` の通常のコードで分岐する（プラットフォーム固有 API 呼び出しが不要なため） |
| バックグラウンド定期同期のようにプラットフォーム機構（WorkManager/BGTaskScheduler）が必要 | 本 ADR の範囲外（未導入・未決定。[0013-defer-cache-auth-retry.md](0013-defer-cache-auth-retry.md) 参照）。必要になったら同様に `app` の `androidMain`/`iosMain` にスケジューラを置き Service を呼ぶ方式を検討し、別途 ADR を起票する |

## Consequences

- メリット: `expect`/`actual` の置き場所（STYLE-16: `core`/`app` のみ）と矛盾せず、既存の `AppRouter`/`platformModule` パターンをそのまま流用できるため学習コストが低い。
- デメリット / トレードオフ: プラットフォーム機能が増えるたびに `platformModule` という単一モジュールが肥大化する。肥大化した場合の分割基準は本 ADR では定めない。
- 守らせる手段: レビュー（pr-checklist.md）で確認。`ui`/`feature` に `expect`/`actual` を追加しようとすると STYLE-16 のレビュー指摘と重複して検出できる。

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| `ui`/`feature` モジュールに直接 `expect`/`actual` を書く | STYLE-16（`expect`/`actual` は `core`/`app` に限定）と矛盾する。`ui`/`feature` は `androidMain`/`iosMain` ソースセットを持たない構成を前提にしている。 |
| CompositionLocal でプラットフォーム実装を渡す | Koin による DI 集約（ADR-0008）という既存の一元化ポイントを迂回し、テストでの差し替え（Mokkery でのモック）がしづらくなる。 |

## サンプル削除後の扱い

Zenn サンプルにはプラットフォーム固有機能の呼び出し実例が無い。既存の `platformModule`（`Settings.Factory` のみ）と `AppRouter` の分離パターンが、新規のプラットフォーム機能を追加する際の唯一の参照先になる。
