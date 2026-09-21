# ADR-0013: キャッシュ・オフライン・認証・リトライは必要になるまで実装しない

> ステータス: 採用
> 日付: 2026-09-21
> 実例: 実例なし（本 ADR で新規決定）
> 関連: [data-layer.md](../guides/data-layer.md), [module-guide.md](../architecture/module-guide.md), [0024-platform-capabilities-via-app-module.md](0024-platform-capabilities-via-app-module.md)

## Context

`composeApp/core` の `ApiClientImpl` にはタイムアウト・リトライ（`HttpTimeout`/`HttpRequestRetry` プラグイン）の `install` がなく、認証（`Auth` プラグイン）も設定されていない。Repository 実装にもキャッシュ層はない。Zenn API が認証不要な公開 API であるため実例がなく、これが意図的な未整備か単なる省略かはコードから判断できない。将来これらが必要になったとき、実装の置き場所と方針が明文化されていないと、AI エージェントが Repository 層や UI 層に個別実装をばらまいてしまう。

## Decision

- **必須**: キャッシュ・オフライン対応・認証（トークン付与）・リトライは、要件として明示されるまで実装しない。先取りでの汎用化は行わない。
- **必須**: 認証やリトライ・タイムアウトが必要になった場合は `composeApp/core` の `ApiClientImpl`（`HttpClient` の `install {}` ブロック）に追加し、全 API 共通で適用する。Repository や個々の API interface で個別に実装しない。
- **推奨**: キャッシュが必要になった場合は、まず `data:<source>` モジュール内で Remote/Local DataSource に分割し、Repository がそれらを束ねる形にする。`core` に汎用 Cache ユーティリティを新設するのは、複数の `data` モジュールで同種のキャッシュが実際に重複してから検討する。
- **推奨**: 認証トークンの永続化は、既存の `ZennPreferences` と同様に `multiplatform-settings` を使う専用クラス（例: `TokenPreferences`）として `core` かトークンを必要とする `data:<source>` に置く。

## 判断基準

| 状況 | 判断 |
|---|---|
| 新しい外部 API に認証が必要になった | `core` の `ApiClientImpl` に Ktor `Auth` プラグインを追加し、トークンプロバイダを DI 注入する |
| リクエストが無期限に待ち続ける問題が顕在化した | `core` の `ApiClientImpl` に `HttpTimeout` を追加する（全 API 共通） |
| 特定のレスポンスだけキャッシュしたい | まず `data:<source>` 内で Remote/Local DataSource に分割し、Repository で束ねる |
| 複数の `data` モジュールで同じキャッシュロジックが重複し始めた | `core` に共通 Cache ユーティリティを新設するかどうかを別途検討する |
| ローカル DB（オフラインキャッシュ等）が必要になった | SQLDelight/Room のどちらを採用するかは未決定。導入時は別途 ADR を起票し、`data:<source>/datasource/local/` に置く |
| バックグラウンド定期同期が必要になった | 本テンプレートでは未導入。導入時は `app` の `androidMain`（WorkManager）/`iosMain`（BGTaskScheduler）にスケジューラを置き Service を呼ぶ方式を検討し、別途 ADR を起票する（未決定。[ADR-0024](0024-platform-capabilities-via-app-module.md) の判断基準も参照） |

## Consequences

- メリット: スターターテンプレートとしての軽量さを保てる。追加時の実装箇所（`core`）が既存構造と一致しているため後付けしやすい。
- デメリット / トレードオフ: 「必要になるまで実装しない」の着手判断が遅れると、タイムアウト無期限のような問題が本番運用まで気づかれないリスクがある。
- 守らせる手段: レビュー（pr-checklist.md）で確認。強制はできない（文書のみ）。

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| テンプレート提供時点でタイムアウト・リトライ・認証をあらかじめ実装しておく | Zenn API が認証不要な公開 API であるため実例を伴わない実装になり、利用者の実際の要件（認証方式・リトライポリシー）と合わない過剰設計になりやすい。 |
| Repository 層でトークン取得・付与を個別に行う | `core` の `HttpClient` 設定という一元化ポイントを迂回し、API 追加のたびに認証コードが重複する。 |

## サンプル削除後の扱い

Zenn API 自体が認証・キャッシュ不要なため実例がない。本 ADR の判断基準表と「`core:ApiClientImpl` / `data:<source>`」という置き場所の指定が、サンプル削除後に残る唯一の指針になる。
