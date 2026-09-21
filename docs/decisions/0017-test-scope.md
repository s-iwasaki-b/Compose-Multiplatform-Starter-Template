# ADR-0017: テスト必須範囲は Converter / Repository impl / Service impl / ViewModel、テストダブルは Mokkery で統一する

> ステータス: 採用
> 日付: 2026-09-21
> 実例: 一部あり（`composeApp/data/zenn/src/commonTest/kotlin/org/starter/project/data/zenn/repository/ZennRepositoryTest.kt`, `composeApp/domain/zenn/src/commonTest/kotlin/org/starter/project/domain/zenn/ZennServiceTest.kt`）
> 関連: [testing.md](../guides/testing.md), [0006-mokkery-for-test-doubles.md](0006-mokkery-for-test-doubles.md), [0018-dispatcher-switch-in-resulthandler.md](0018-dispatcher-switch-in-resulthandler.md)

## Context

現状 Converter・Repository 実装・Service 実装にはテストがあるが、ViewModel・Compose UI・DI 設定にはテストがない。特に `domain/service` は `commonTest.dependencies` と `mokkery` プラグインを宣言しているにもかかわらず、全例外を集約する `ResultHandler` のテストファイルが 1 つも存在しない。「どこまで書けば十分か」の基準がないと、AI エージェントは新機能実装時にテストを省略しがちになる。なおテストダブルについては、本ワークツリー時点で Mokkery（モックライブラリ）を使った実装が正であり、これを維持することがユーザーの最終決定である（メインツリーで進行していた手書き Fake / `composeApp:testing` への移行は採用しない）。

## Decision

- **必須**: Converter、Repository 実装、Service 実装、ViewModel の 4 層はテストを書く。Compose UI テスト（`ComposeUiTest` 等）は任意とする。
- **必須**: テストダブルは Mokkery（`dev.mokkery`）で統一する。テストを持つモジュールの `build.gradle.kts` に `alias(libs.plugins.mokkery)` を適用する。手書き Fake や共有 Fake モジュール（`:composeApp:testing`）は導入しない（ADR-0006 参照）。
- **必須**: モック対象は interface（`XxxRepository`/`XxxService`/`XxxApi`/`XxxPreferences`）。具象クラスをモックする必要がある場合のみ `@OpenForTesting open class` にする。
- **推奨**: `domain/service` の `ResultHandler` はテストを追加する。全例外を集約する横断的関心事であり優先度が高い。
- **推奨**（新規決定）: モックのモード選択は「テストが呼び出しを明示的に定義する依存は既定モード（`mock<T>()`）、呼ばれても結果に影響しない協調オブジェクトは `MockMode.autofill`」を基準にする。

## 判断基準

| 状況 | 判断 |
|---|---|
| Converter/Repository 実装/Service 実装/ViewModel を新規追加した | テスト必須 |
| 新規 Composable（Screen/Content）を追加した | Compose UI テストは任意 |
| `commonTest.dependencies` と Mokkery プラグインを宣言したがテストファイルが 0 件 | 既知の逸脱。特に `ResultHandler` はテスト追加を推奨 |
| モックする依存の戻り値がテストの成否に影響する | `mock<T>()`（既定モード）を使い `every`/`everySuspend` で明示的にスタブする |
| モックする依存の戻り値がテストの成否に影響しない協調オブジェクト | `mock<T>(MockMode.autofill)` を使う |

## Consequences

- メリット: data/domain 層のロジック（変換・エラーハンドリング・ビジネスロジック分岐）を確実に検証できる。Mokkery を維持することで既存の `ZennRepositoryTest`/`ZennServiceTest` をそのまま新規テストの手本にできる。
- デメリット / トレードオフ: Compose UI テストが任意のため UI 層のリグレッションは ViewModel テストとレビューに頼る。`ResultHandler` が無テストのまま放置されるリスクが残る（推奨止まりのため強制力が弱い）。
- 守らせる手段: レビュー（pr-checklist.md）で確認。CI は意図的に含まれていないため実行は開発者の裁量による。

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| テストダブルを手書き Fake に全面移行し、共有 Fake を `:composeApp:testing` に集約する | メインツリーで並行して進められていた移行案だが、最終的にユーザーの判断で Mokkery 維持が確定した。ドキュメントには採用しない。 |
| 全層（Compose UI テスト含む）を必須にする | KMP での Compose UI テストは環境構築コスト（Robolectric 等が現状未導入）が高く、既存方針（ロジックを ViewModel/Service/Repository に寄せ Composable を薄く保つ）と比べて費用対効果が低い。 |
| モックのモード基準を設けず全て既定モードで統一する | 呼び出し結果に影響しない協調オブジェクトまで毎回 `every` でスタブする必要が生じ記述量が増える。既存の `ZennRepositoryTest`/`ZennServiceTest` が `MockMode.autofill` を使い分けている実例と矛盾する。 |

## サンプル削除後の扱い

`ZennRepositoryTest`/`ZennServiceTest` は Mokkery の書き方（`mock<T>()`, `every`/`everySuspend`, `verify`/`verifySuspend`）の実例として使われていたが、削除後は本 ADR の判断基準と `guides/testing.md` のコード例のみが根拠になる。
