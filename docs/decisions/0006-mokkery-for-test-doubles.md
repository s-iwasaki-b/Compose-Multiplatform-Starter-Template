# ADR-0006: テストダブルは Mokkery（モックライブラリ）で統一する。手書き Fake は併用しない

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（`composeApp/domain/zenn/src/commonTest/kotlin/org/starter/project/domain/zenn/ZennServiceTest.kt`）
> 関連: [testing.md](../guides/testing.md), [domain-layer.md](../guides/domain-layer.md)

## Context

PR #9 では手書き Fake パターン（`:composeApp:testing` 共有モジュール、`FakeXxx` クラス群）への移行が試行されたが、ユーザーの最終判断によりこの移行は撤回され（PR #9 はクローズ）、Mokkery（モックライブラリ）を継続利用する方針が確定した。AI エージェントが「テストのベストプラクティス」として Fake への移行を再提案しないよう、決定の経緯を明記する必要がある。

## Decision

- **必須**: テストダブルは `dev.mokkery`（Mokkery）で統一する。手書き Fake（`FakeXxx` クラス）を新たに追加しない。
- **必須**: テストを持つモジュールの `build.gradle.kts` に `alias(libs.plugins.mokkery)` を適用する。
- **必須**: モック対象は interface（`XxxRepository`, `XxxService`, `XxxApi`, `XxxPreferences`）とする。具象クラスをモックする必要がある場合のみ `@OpenForTesting open class` にする。
- **推奨**: テストが呼び出しを明示的に検証する依存は既定モード（`mock<T>()`）、呼ばれても結果に影響しない協調オブジェクトは `mock<T>(MockMode.autofill)` を使う（新規決定）。

## 判断基準

| 状況 | 判断 |
|---|---|
| Service 実装のテストで依存 Repository をモックする | `mock<XxxRepository>(MockMode.autofill)` ＋ `everySuspend { } returns/throws` ＋ `verifySuspend { }` |
| テストが呼び出し内容自体を明示的に検証したい依存 | 既定モード（autofill なし）の `mock<T>()` にする |
| 共有のテストダブルが欲しくなった | 作らない。`:composeApp:testing` のような共有 Fake モジュールは新設しない |

## Consequences

- メリット: モックライブラリに統一することでテストダブルの手書きメンテナンスコストがゼロになり、`every`/`everySuspend`/`verify` という一貫した API で振る舞い・呼び出し回数の両方を検証できる。
- デメリット / トレードオフ: Mokkery はコンパイル時のコード生成に依存するため、KMP のビルド設定変更時にプラグインの互換性を確認する必要がある。具象クラスをモックする場合は `@OpenForTesting` で open 化する必要があり、final クラスの利点（不変性の保証）が一部失われる。
- 守らせる手段: レビュー（pr-checklist.md）で新規 Fake クラスが追加されていないかを確認

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| 手書き Fake への移行（`FakeXxxRepository` 等を `:composeApp:testing` に配置） | **決定の経緯**: PR #9（`composeApp/testing` モジュール、`FakeZennRepository`/`FakeZennApi`/`FakeZennPreferences` を追加）で実際に試行されたが、2026-09-21 にユーザー判断で撤回された（PR #9 はクローズ）。**Mokkery 維持の利点（本 ADR の評価）**: Mokkery で統一する方が呼び出し回数検証（`verify`）を含めテスト記述コストが低く、Fake 自体のテスト（`FakeZennRepositoryTest`）という二重のメンテナンス対象が増えることを避けられる |
| モックライブラリと Fake を状況に応じて併用する | 1 プロジェクト内でテストダブルの書き方が 2 種類存在すると、AI エージェントがどちらを新規に選ぶべきか判断できず一貫性が崩れる。統一ルールとして一方に絞る |

## サンプル削除後の扱い

Mokkery プラグイン自体は `gradle/libs.versions.toml`・各モジュールの `build.gradle.kts` に残るため、Zenn サンプルが消えても新規モジュールに `alias(libs.plugins.mokkery)` を適用し、この判断基準に従ってモックを書けば同じパターンを再現できる。
