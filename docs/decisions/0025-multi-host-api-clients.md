# ADR-0025: 別ホストの API は `ApiClientImpl` を baseUrl 引数化し Koin `named("<host>")` で複数登録する

> ステータス: 採用
> 日付: 2026-09-21
> 実例: 実例なし（本 ADR で新規決定）
> 関連: [data-layer.md](../guides/data-layer.md), [dependency-injection.md](../architecture/dependency-injection.md), [add-data-source.md](../playbooks/add-data-source.md), [0013-defer-cache-auth-retry.md](0013-defer-cache-auth-retry.md)

## Context

`ApiClientImpl`（`composeApp/core/src/commonMain/kotlin/org/starter/project/core/api/ApiClient.kt`）は `HttpClient` を1つだけ生成し、`defaultRequest { url(ApiConfig.API_BASE_URL) }` で baseUrl を固定している。別ホストの API（例: GitHub API）を追加する場合にこの制約をどう解消するかについて、これまで実例・決定が存在しなかった。

## Decision

- **必須**（現状未実装の差分）: 既定は `class ApiClientImpl(private val baseUrl: String, ...) : ApiClient` のようにコンストラクタ引数で baseUrl を受け取る形にする。
- **必須**（現状未実装の差分）: ホストごとに Koin の `named()` 修飾子で複数登録する。例: `single<ApiClient>(named("zenn")) { ApiClientImpl(ApiConfig.API_BASE_URL) }` / `single<ApiClient>(named("github")) { ApiClientImpl(GitHubApiConfig.API_BASE_URL) }`。
- **必須**: `dataSourceModule` での取得も対応する named qualifier を指定する。例: `single<GitHubApi> { get<ApiClient>(named("github")).ktorfit.createGitHubApi() }`。
- **必須**: ホストごとの baseUrl 設定値は `core` に `internal object <Host>ApiConfig { const val API_BASE_URL = "..." }` として追加する（[data-layer.md](../guides/data-layer.md) DATA-15 と同じ形式）。
- **推奨**（現状未実装の差分）: エラーボディ形式がホストによって異なる場合、`HttpResponseValidator` の変換ロジックを `ApiClientImpl` のコンストラクタ引数（例: `errorMapper: suspend (HttpResponse) -> Throwable`）で差し替え可能にする。

## 判断基準

| 状況 | 判断 |
|---|---|
| 新しい API がこれまでと同じホストのエンドポイント追加 | 既存の `ApiClientImpl` インスタンス（既定の named qualifier）をそのまま使う |
| 新しい API が別ホスト | `ApiClientImpl(baseUrl)` を新しい `named("<host>")` で追加登録し、`<Host>ApiConfig` を `core` に追加する |
| 別ホストのエラーレスポンス形式が既存の `ApiErrorResponse`（`exceptionResponse.body<ApiErrorResponse>()` で固定デコード）と異なる | `errorMapper` をホストごとに差し替える（現状未実装の差分。導入するまでは、そのホストのエラーレスポンスがデコード失敗しうることを認識した上で実装する） |
| `@GET` アノテーションに絶対 URL を直接書く方式（代替案 (a)）を使いたい | 既定にしない。`defaultRequest` との合成挙動を本リポジトリで検証していないため（Alternatives 参照） |

## Consequences

- メリット: 複数ホストの API 追加という既知のギャップに対し、Koin の `named()` という標準機構だけで解決でき、新しい DI パターンを追加で学習する必要がない。`ApiClientImpl` の設定集約（DATA-14）という既存原則とも整合する。
- デメリット / トレードオフ: `ApiClient` を取得する箇所すべてで named qualifier を意識する必要があり、qualifier を書き忘れると既定（Zenn 用）の `ApiClientImpl` が誤って注入される。`errorMapper` は本テンプレートでは未実装のため、導入時に `ApiClientImpl` のコンストラクタシグネチャを変更する破壊的変更が発生する。
- 守らせる手段: レビュー（pr-checklist.md）で確認。Gradle での機械的強制はできない。

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| (a) Ktorfit の `@GET`/`@POST` アノテーションに絶対 URL を直接書く | `defaultRequest { url(ApiConfig.API_BASE_URL) }` との合成挙動（絶対 URL がベース URL を上書きするか、パスとして連結されるか）を本リポジトリで検証していないため、既定にはしない。単一エンドポイントのみを別ホストから呼ぶ軽量なケースでは今後検討の余地がある。 |
| ホストごとに完全に独立した `HttpClient`/Ktorfit インスタンスを都度 `Ktorfit.Builder()` で組み立てる | `ApiClientImpl` への設定集約（DATA-14、`ContentNegotiation`/`Logging`/`HttpResponseValidator` の一元化）が崩れ、ホストが増えるたびに同じ設定コードが重複する。 |

## サンプル削除後の扱い

Zenn サンプルは単一ホストのみを扱うため実例が無い。本 ADR の Decision と判断基準表、および [data-layer.md](../guides/data-layer.md) の B-G15 が、複数ホストの API を追加する際の唯一の参照先になる。
