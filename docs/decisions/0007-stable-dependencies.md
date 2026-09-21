# ADR-0007: 依存ライブラリは安定版優先。Alpha/Beta は評価記録のみ

> ステータス: 採用
> 日付: 2026-09-21
> 実例: 運用実績（`gradle/libs.versions.toml`。個別の見送り判断はコミット履歴・PR に残る）
> 関連: [module-guide.md](../architecture/module-guide.md), [git-workflow.md](../guides/git-workflow.md)

## Context

Kotlin/Compose Multiplatform（CMP）/AGP の更新方針、および個別ライブラリの Alpha/Beta 採用可否が不明だと、AI エージェントが最新の Preview 機能を無条件に採用してしまう。本プロジェクトでは実際に複数の Alpha/Beta 機能評価が行われ、いずれも安定版優先の判断で見送られてきた実績がある。

- Swift Export（Kotlin 2.4 の Alpha 機能）: CMP 1.12.0 のリソース同期で失敗するため採用見送り。
- Navigation 3（Jetpack Navigation の次世代版、Alpha）: 採用見送り、`navigation-compose` 2.9.2（安定版）を継続利用。
- targetSdk 37: 安定版リリース後、追従判断を即決（`gradle/libs.versions.toml` の `android-targetSdk = "37"`）。

## Decision

- **必須**: Kotlin/CMP/AGP/主要ライブラリのバージョン更新は安定版（Stable）を対象とする。Alpha/Beta 版は原則採用しない。
- **必須**: Alpha/Beta 機能を評価した場合は、採用の可否に関わらず評価内容（バージョン、ブロッカーとなった問題、再検討条件）を PR またはコミットメッセージに記録してから見送る。
- **推奨**: 安定版の更新は破壊的変更が無ければ速やかに追従する（`targetSdk` のように追従コストが低いものは即決してよい）。

## 判断基準

| 状況 | 判断 |
|---|---|
| 対象バージョンが Alpha/Beta | 原則見送る。ただしブロッカーとなる不具合が修正され、当該バージョンが Beta へ昇格した場合は再評価する |
| 対象バージョンが Stable（安定版） | 破壊的変更（Breaking Changes）を確認した上で速やかに追従する |
| Alpha 機能を評価だけして採用しない場合 | 評価記録（バージョン・見送り理由・再検討条件）を PR 本文またはコミットメッセージに残す |

## Consequences

- メリット: 未検証の Alpha/Beta 機能に起因するビルド不安定化・ドキュメントとの乖離を避けられる。評価記録を残すことで将来の再評価コストが下がる。
- デメリット / トレードオフ: Swift Export や Navigation 3 のような魅力的な新機能の恩恵を早期に得られない。
- 守らせる手段: レビュー（pr-checklist.md）でバージョンカタログの変更が安定版かを確認

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| 積極的に Alpha/Beta 機能を採用し最新機能を追従する | 実際に Swift Export が CMP 1.12.0 のリソース同期で失敗した実績があり、テンプレート利用者に不安定なビルドを引き継がせるリスクが高い |
| バージョン更新を都度個別ライブラリで判断し、方針を明文化しない | AI エージェントが Alpha 版ライブラリに気づかず採用してしまう事故を防げない。判断基準を明文化することで再現性を持たせる |

## サンプル削除後の扱い

この決定は Zenn サンプルコードに依存しない運用方針であり、サンプル削除後もそのまま適用できる。再検討条件（対象機能の Beta 昇格、ブロッカー修正）を満たした場合のみ、個別に再評価の PR を起こす。
