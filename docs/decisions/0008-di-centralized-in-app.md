# ADR-0008: Koin の DI 定義は `composeApp:app` の `Koin.kt` に層別で集約する

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（`composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt`）
> 関連: [dependency-injection.md](../architecture/dependency-injection.md), [0001](0001-contract-and-impl-modules.md)

## Context

新機能の DI 登録をどこに書くべきかが不明だと、機能モジュールごとに DI 定義ファイルが分散し、初期化順序やバインドの重複を見落としやすくなる。本テンプレートは全ての Koin モジュール定義を `composeApp/app` の `Koin.kt` 1 ファイルに、レイヤー別（`coreModule`/`dataSourceModule`/`repositoryModule`/`serviceModule`/`appModule`）に分けて集約している。

## Decision

- **必須**: 新しい interface→実装のバインドは、対応するレイヤーの `module { }` ブロック（`coreModule`/`dataSourceModule`/`repositoryModule`/`serviceModule`/`appModule`）に `single<Interface> { Impl(get(), get()) }` の形式で追加する。interface 型を必ず明示する。
- **必須**: DI 定義ファイルを機能モジュール（`data:<source>`, `domain:<name>`, `feature:<name>`）内に新設しない。`composeApp/app` の `Koin.kt` に集約する。
- **推奨**: ViewModel は `viewModelOf(::XxxScreenViewModel)` で `appModule` に登録する。
- **任意**: プラットフォーム差分が必要な依存は `expect`/`actual val platformModule` に分離する。

## 判断基準

| 状況 | 判断 |
|---|---|
| 新しい Repository/Service のバインドを追加する | 対応する `repositoryModule`/`serviceModule` に `single<Interface> { Impl(...) }` を追記する |
| 新しい ViewModel を追加する | `appModule` に `viewModelOf(::XxxScreenViewModel)` を追記する |
| プラットフォームごとに実体が異なる依存を登録したい | `expect val platformModule` の `actual` 実装（Android/iOS）に書く |

## Consequences

- メリット: DI 登録の全体像が 1 ファイルで見渡せ、モジュール追加のたびに新しい DI ファイルを探す必要がない。レイヤー順（core → dataSource → repository → service → app）と `modules(...)` の登録順が一致しており、依存解決順序を把握しやすい。
- デメリット / トレードオフ: 機能が増えるほど `Koin.kt` が長くなり、複数人での同時編集がコンフリクトしやすくなる。
- 守らせる手段: レビュー（pr-checklist.md）で機能モジュール内に DI 定義ファイルが増えていないかを確認

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| 各機能モジュール内に個別の Koin モジュール定義ファイルを置く（`data:<source>/di/Module.kt` 等） | 実例は一貫して `composeApp/app` への集約であり、分散させると登録漏れ・重複バインドに気づきにくくなる。DI の初期化順序（`modules(...)` への渡し順）も 1 箇所で管理できなくなる |
| `factory` を使いリクエストごとに新しいインスタンスを生成する | 既存のバインドは全て `single`（アプリ全体で 1 インスタンス）であり、`factory` の使用例は無い。状態を持たない Repository/Service に対して不要な複雑性を持ち込む |

## サンプル削除後の扱い

`Koin.kt` の `coreModule` は Zenn 依存部分を除去しても構造ごと残るため、新規リソース追加時は空になった `dataSourceModule`/`repositoryModule`/`serviceModule` に同じ形式でバインドを追記すればよい。
