# ADR-0019: エラー表示 4 経路（全画面 / Paging 末尾 / Snackbar / 入力フィールド）の使い分け

> ステータス: 採用
> 日付: 2026-09-21
> 実例: 一部あり（`composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/component/article/ArticlesPagingSource.kt`, `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/handler/ErrorScreenThrowableHandler.kt`）
> 関連: [error-handling.md](../architecture/error-handling.md), [ui-layer.md](../guides/ui-layer.md), [0005-paging-in-ui-layer.md](0005-paging-in-ui-layer.md), [0016-one-shot-events.md](0016-one-shot-events.md), [0014-validation-placement.md](0014-validation-placement.md)

## Context

現状 `ScreenState.Failure`（全画面）、Paging の `LoadState.Error`（一覧末尾）、`SnackBarThrowableHandler`（軽量通知）の 3 系統が共存している。しかし `ArticlesPagingSource.load()` の `try/catch` は、`fetcher`（`HomeScreenViewModel.fetchArticles` 経由）が `zennService.xxx(...).handle(...)` によって既に例外を `null` に変換済みのため実質到達しない（既知の逸脱）。加えて、入力バリデーションエラーはこの 3 系統のいずれにも当てはまらない。4 経路をいつ使うかの対応表がないと、新機能実装時に AI エージェントが誤った経路を選ぶ。

## Decision

- **必須**: 初回ロード失敗（画面を開いた直後のデータ取得失敗）は `ScreenState.screenLoadingState` を `ScreenLoadingState.Failure` にし、`SystemScaffold` の全画面 `ErrorContent` で表示する。
- **必須**: 追加ページ（2 ページ目以降）のロード失敗は Paging の `LoadState.Error` として一覧の末尾に表示する。ViewModel は `fetcher` に渡す取得処理で、初回ページと追加ページを区別し、追加ページの失敗は `Result.handle()` で握りつぶさず `ArticlesPagingSource` の `try/catch` まで例外を伝播させる（新規決定。既知の逸脱: 現状の `HomeScreenViewModel.fetchArticles` は初回・追加ページを区別せず同じ `ErrorScreenThrowableHandler` を使っており、追加ページ失敗時も全画面 `Failure` になってしまう）。
- **必須**: 軽微・ユーザー操作起因のエラー（例: 検索キーワードのローカル保存失敗など、画面全体を止めるほどではないエラー）は `SnackBarThrowableHandler` 経由で `ScreenState.snackBarState` に載せる（ADR-0016 参照）。
- **必須**（新規決定）: 入力バリデーションエラー（例: メールアドレス形式不正）は `UiState` のフィールド（例: `emailError: String?`）に載せ、入力欄直下に表示する。Service の `Result.failure(ValidationError)` を ViewModel が `.handle()` 内でフィールドへマッピングする（[ADR-0014](0014-validation-placement.md) 参照）。

## 判断基準

| 経路 | 使う場面 | 実装 |
|---|---|---|
| `ScreenLoadingState.Failure`（全画面） | 初回ロード失敗。画面の主データが取得できない致命的なエラー | `ErrorScreenThrowableHandler(_screenState)` を `Result.handle()` に渡す |
| Paging `LoadState.Error`（一覧末尾） | 2 ページ目以降の追加ロード失敗。既に表示済みの一覧は維持したい | `fetcher` が例外を `Result.handle()` で握りつぶさず `ArticlesPagingSource` の `try/catch` まで伝播させる |
| Snackbar（`snackBarState`） | ユーザー操作起因の軽微なエラー。画面遷移や再描画を伴わない通知で十分 | `SnackBarThrowableHandler(_screenState)` を `Result.handle()` に渡す |
| `UiState` のフィールド（例: `emailError: String?`） | 入力バリデーションエラー。特定の入力欄に紐づけて直下に表示したい（新規決定） | Service の `Result.failure(ValidationError)` を ViewModel の `.handle()` 内でフィールドへマッピングする（[ADR-0014](0014-validation-placement.md) 参照） |

## Consequences

- メリット: 3 つの既存の仕組みを廃止せず用途で使い分けることで、既存資産（`ScreenState`/Paging/`SnackBarState`）をすべて活かせる。
- デメリット / トレードオフ: 現状の `HomeScreenViewModel.fetchArticles` は初回・追加ページの区別なく `ErrorScreenThrowableHandler` を使っており、追加ページ失敗時も全画面エラーになる（既知の逸脱）。この使い分けを実装するには、呼び出し元でページ番号（初回かどうか）による分岐が新たに必要になる。
- 守らせる手段: レビュー（pr-checklist.md）で確認。

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| 全て `ScreenState.Failure` に統一し、Paging の `LoadState.Error` と Snackbar を廃止する | 追加ページ失敗のたびに画面全体がエラー表示になり、既に読み込み済みの一覧が消えてしまう。既存コードに用意された `SnackBarState`/Paging 機構も無駄になる。 |
| Paging `LoadState.Error` のみに統一し、初回ロードも一覧の空状態＋エラー表示にする | 初回ロード失敗時は表示すべきデータが何もないため、一覧の枠自体を出さず全画面エラーで明確に伝える方がユーザーに分かりやすい。既存の `SystemScaffold` の `Failure` 分岐とも整合しない。 |

## サンプル削除後の扱い

`ScreenState`/`SystemScaffold`/`ArticlesPagingSource` のテンプレートは `composeApp/ui` に残るが、`ArticlesPagingSource` の `try/catch` は現状到達しない実装（既知の逸脱）である。新規のページング画面を作る際は本 ADR の Decision に従い、追加ページの例外を `Result.handle()` で握りつぶさず `PagingSource` まで伝播させる設計を自分で組む必要がある。
