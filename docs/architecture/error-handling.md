# エラーハンドリング

> 対象: 例外・エラー型の発生から UI 表示までの層別責務、`Result` 化、エラー表示経路の使い分け
> 関連: [data-layer.md](../guides/data-layer.md), [domain-layer.md](../guides/domain-layer.md), [ui-layer.md](../guides/ui-layer.md), [testing.md](../guides/testing.md), [overview.md](overview.md)
> 最終確認コミット: ac56102

## 要点

- 独自エラー型は `ApiError`（HTTP 由来）と `ConversionError`（変換由来）の2つの sealed class。どちらも `base` モジュールに置き `Throwable` を継承する。
- HTTP ステータス→独自エラー型への変換は `core` の `ApiClientImpl` 内の `HttpResponseValidator` 1箇所に集約する。
- Repository・Converter（一覧変換以外）は例外を透過する。**`Result` 化するのは `domain:service` の `ResultHandler` だけ**（ADR-0003）。
- `ResultHandler` は `catch(e: Throwable)` の直後に `coroutineContext.ensureActive()` を呼び、`CancellationException` を再送出してから `Result.failure` を返す。
- ViewModel は `Result<T>.handle(handler)` で UI 状態に変換する。`handler` は `ErrorScreenThrowableHandler`/`SnackBarThrowableHandler`/`IgnoreThrowableHandler` の3種から選ぶ。
- エラー表示は4経路を用途で使い分ける: **初回ロード失敗＝全画面 `ScreenLoadingState.Failure`** / **追加ページ失敗＝Paging の `LoadState.Error`** / **軽微・ユーザー操作起因＝Snackbar** / **入力バリデーションエラー＝UiState のフィールド（例: `emailError: String?`）に載せ入力欄直下に表示**。
- ログは Napier。例外捕捉は `Napier.e`、追跡ログは `Napier.d`、通信詳細は `Napier.v`（現状 `ResultHandler` の `Napier.d` は既知の逸脱。§ アンチパターン参照）。
- サンプルの `ArticlesPagingSource` の `try/catch` と `SnackBarThrowableHandler` はどちらも現状うまく機能していない（§ アンチパターン参照）。**そのまま真似しない**こと。

## ルール

| ID | 規範 | ルール |
|---|---|---|
| ERR-1 | 必須 | 独自エラー型は `ApiError`（HTTP 由来）と `ConversionError`（変換由来）の2つの `sealed class`（`Throwable` を継承）とし、いずれも `base` モジュール（`org.starter.project.base.error`）に置く。 |
| ERR-2 | 必須 | エラー型を拡張するときは、汎用的な HTTP 分類（404/5xx/ネットワーク到達不可等）を `ApiError` に追加する。ビジネス固有のエラー（例: 「該当ユーザーが見つからない」）は `ConversionError` に倣った別の `sealed class` を該当ドメインモジュールに新設する。既存の `ApiError`/`ConversionError` に無関係な分岐を追加しない。 |
| ERR-3 | 必須 | HTTP ステータスコード→独自エラー型への変換は `core` の `ApiClientImpl` の `HttpResponseValidator { handleResponseExceptionWithRequest { ... } }` 1箇所に書く。Repository・API interface・Converter で同様の変換を重複実装しない。 |
| ERR-4 | 必須 | DTO→domain モデル変換時の必須フィールド欠落チェックは `response.field.validateNotNull("field_name")` を使い、`ConversionError.ResponseNotNullValidation` を送出させる（詳細は [data-layer.md](../guides/data-layer.md) DATA-12）。 |
| ERR-5 | 必須 | Repository 層は例外を `try/catch` しない。素の domain モデルを返し、例外はそのまま呼び出し元に伝播させる（例外透過）。 |
| ERR-6 | 必須 | 例外→`kotlin.Result` への変換は `domain:service` の `ResultHandler.async`/`ResultHandler.immediate` に一元化する。Repository・Converter・ViewModel で `Result` を手動構築しない。 |
| ERR-7 | 必須 | `ResultHandler` の `catch (e: Throwable)` ブロックでは、`Result.failure(e)` を返す**前**に `coroutineContext.ensureActive()`（`async`）を呼び、`CancellationException` を再送出させる。これを省略すると構造化並行性が壊れる。 |
| ERR-8 | 必須 | Dispatcher の切り替え（`withContext`）は `ResultHandler` の1箇所のみで行う。Repository・DataSource・Converter・ViewModel では `withContext`/`Dispatchers` を直接使わない（[data-layer.md](../guides/data-layer.md) DATA-5）。 |
| ERR-9 | 必須 | `Dispatcher` は `ResultHandler(dispatcher: CoroutineDispatcher = Dispatchers.IO)` のようにコンストラクタのデフォルト引数として注入可能にする。テストでは `StandardTestDispatcher()` を注入して差し替える。 |
| ERR-10 | 必須 | ViewModel は Service から返る `Result<T>` を `.handle(handler)` で UI 状態に変換する。`Result.getOrThrow()`/`getOrNull()` を直接使って独自にハンドリングしない。 |
| ERR-11 | 必須 | `ThrowableHandler` は用途に応じて3種から選ぶ（§ 判断基準の表）。新しい種類のエラー表示 UI（例: ダイアログ）が必要な場合のみ4種目を追加する。 |
| ERR-12 | 必須 | エラー表示は4経路を用途で使い分ける（§ 判断基準の表）。1つの失敗に対して複数の経路を同時に発火させない。 |
| ERR-13 | 必須 | 例外捕捉時のログは `Napier.e`、処理追跡ログは `Napier.d`、通信詳細（HTTP リクエスト/レスポンス全体）は `Napier.v` を使う。`println`/`android.util.Log` は使わない。 |
| ERR-14 | 推奨 | analytics へのエラー送信を実装する場合は `ResultHandler` の `catch` 節（`async`/`immediate` それぞれ1箇所ずつ）に集約する。個別の Converter・ViewModel でエラー送信コードを分散させない。**例外**: 一覧変換で `mapNotNull` により1件ずつ握りつぶす `ConversionError`（DATA-13）は例外として再送出されず `ResultHandler` に到達しないため、この場合に限り Converter の `catch` 節で非致命エラーとして analytics に報告してよい。 |
| ERR-15 | 必須 | `@OptIn` は関数/プロパティ単位で付与する。ファイル単位の `@file:OptIn` は使わない。 |
| ERR-16 | 必須 | フォーム入力のバリデーションエラーは、Service が `Result.failure(ValidationError)` を返し、ViewModel が `.handle { throwable -> ... }` で `UiState` の対応するフィールド（例: `emailError: String?`）へマッピングする。入力欄直下にインライン表示し、`ErrorScreenThrowableHandler`/`SnackBarThrowableHandler` の全画面・Snackbar 経路には流さない（[../decisions/0014-validation-placement.md](../decisions/0014-validation-placement.md)、新規決定）。 |

## 判断基準

### 層別責務表（発生・変換・捕捉）

| 層 | 発生 | 変換 | 捕捉 |
|---|---|---|---|
| `core`（`ApiClientImpl` の `HttpResponseValidator`） | HTTP ステータスエラー（`ClientRequestException`） | `ClientRequestException` → `ApiError.Unauthorized`/`ApiError.Unknown` | なし（変換して re-throw のみ） |
| Converter（`data:<source>`） | 必須フィールド欠落 | `null` → `ConversionError.ResponseNotNullValidation`（`validateNotNull`） | 一覧変換のみ `mapNotNull` + `try/catch(ConversionError)` で1件単位に握りつぶす。単体変換は素通し。 |
| Repository（`data:<source>`） | なし | なし | なし（例外透過。ERR-5） |
| Service（`domain:service` の `ResultHandler`） | なし | あらゆる `Throwable` → `Result.failure(e)` | `catch (e: Throwable)` で最終捕捉。`ensureActive()` で `CancellationException` のみ再送出（ERR-7）。 |
| ViewModel | なし | `Result<T>` → UI 状態（`.handle(handler)`） | `handler`（`ThrowableHandler`）に委譲 |
| UI（`SystemScaffold`） | なし | なし | `ScreenLoadingState.Failure` を検知して全画面エラー表示に切り替える |

C-9-8（`Result` 型のラップ方針）: **Repository は `Result` を返さない。`Result` 化は Service 層のみの責務**というレイヤー境界を上表のとおり明文ルール化する。この境界はコード全体で一貫しており、`ResultHandler` という専用ユーティリティの存在自体がこの境界の意図を裏付けている（ADR-0003）。

C-9-9（`ApiError` の throw 箇所）: `ApiError` は `core` の `ApiClientImpl` の `HttpResponseValidator` でのみ throw される（`composeApp/core/src/commonMain/kotlin/org/starter/project/core/api/ApiClient.kt`）。Service 層はエラー型ごとに分岐して独自の domain エラーに変換し直すことはせず、`Result.failure` に包んだまま ViewModel まで素通しする。`ApiError`/`ConversionError` の判別が必要な場合は `ThrowableHandler` 実装側（UI 層）で `when (throwable) { is ApiError.Unauthorized -> ...; else -> ... }` のように分岐する。

### `ThrowableHandler` の使い分け

| Handler | 配置 | 用途 | 効果 |
|---|---|---|---|
| `ErrorScreenThrowableHandler` | `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/handler/ErrorScreenThrowableHandler.kt` | 初回ロード（画面表示に必須なデータ取得）の失敗 | `ScreenState.screenLoadingState` を `ScreenLoadingState.Failure(throwable)` に更新し、`SystemScaffold` が全画面エラー表示に切り替える |
| `SnackBarThrowableHandler` | `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/handler/SnackBarThrowableHandler.kt` | 軽微・ユーザー操作起因の失敗（画面は表示済みで継続利用可能） | `ScreenState.snackBarState` に `SnackBarState(message)` をセットし、`SystemScaffold` がスナックバー表示する |
| `IgnoreThrowableHandler` | `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/handler/IgnoreThrowableHandler.kt` | 失敗しても UI に影響を与えなくてよい処理（例: 起動時の任意設定読み込み） | 何もしない（no-op） |

### エラー表示4経路の使い分け（E-6 の解消、ADR-0019、ADR-0014）

| 経路 | 対象 | 発火条件 | 実装 |
|---|---|---|---|
| 全画面 `ScreenLoadingState.Failure` | 初回ロード（画面の主要データが1件も取得できていない） | `currentKey == null`（Paging の先頭ページ、または非 Paging 画面の初回取得） | `resultHandler` から返る `Result` を `.handle(ErrorScreenThrowableHandler(_screenState))` で変換 |
| Paging の `LoadState.Error` | 追加ページ（スクロールによる次ページ取得） | `currentKey != null` | `fetcher` が例外を **投げる**（`Result.getOrThrow()` などで再送出する）と `ArticlesPagingSource.load()` の `try/catch` が `LoadResult.Error(e)` を返す |
| Snackbar | ユーザー操作起因の軽微な失敗（例: お気に入り登録の失敗など、画面全体には影響しない操作） | 任意 | `.handle(SnackBarThrowableHandler(_screenState))` で `ScreenState.snackBarState` にセットし、表示後 `null` にクリアする（§ アンチパターン参照） |
| `UiState` のフィールド（例: `emailError: String?`） | フォーム入力のバリデーションエラー（画面は表示済みで、特定の入力欄に紐づく） | Service が `Result.failure(ValidationError)`（[ADR-0014](../decisions/0014-validation-placement.md)）を返したとき | `.handle { throwable -> _screenState.update { it.copy(emailError = (throwable as? ValidationError)?.message) } }` のようにフィールドへ直接マッピングし、入力欄直下にインライン表示する（ERR-16。実例なし・新規決定） |

**重要**: 上記の「初回ロード」と「追加ページ」の切り分けは、`currentKey`（Paging のページキー）の有無で判定する。詳細な実装パターンは次節。フォーム入力バリデーションエラーは共通 `ThrowableHandler`（`ErrorScreenThrowableHandler`/`SnackBarThrowableHandler`/`IgnoreThrowableHandler`）のいずれにも該当しない、画面固有の `.handle {}` ラムダで扱う。

## 実装パターン

### `ResultHandler`（`domain:service`）

```kotlin
class ResultHandler(
    val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend inline fun <Success> async(
        dispatcher: CoroutineDispatcher = this.dispatcher,
        crossinline block: suspend () -> Success
    ): Result<Success> {
        return withContext(dispatcher) {
            try {
                val value = block()
                Result.success(value)
            } catch (e: Throwable) {
                // analytics へのエラー送信はここに集約する（ERR-14）
                Napier.e { e.message.orEmpty() } // 本ガイドの規約: e（サンプルの d は既知の逸脱）
                coroutineContext.ensureActive()
                Result.failure(e)
            }
        }
    }

    suspend inline fun asyncUnit(
        dispatcher: CoroutineDispatcher = this.dispatcher,
        crossinline block: suspend () -> Unit
    ): Result<Unit> = async(dispatcher) { block() }

    inline fun <Success> immediate(
        crossinline block: () -> Success
    ): Result<Success> {
        return try {
            val value = block()
            Result.success(value)
        } catch (e: Throwable) {
            Napier.e { e.message.orEmpty() }
            Result.failure(e)
        }
    }

    inline fun immediateUnit(
        crossinline block: () -> Unit
    ): Result<Unit> = immediate { block() }
}
```

戻り値が不要な操作（`Unit` を返す更新系処理など）には `async`/`immediate` をそのまま `Result<Unit>` として使う代わりに `asyncUnit`/`immediateUnit` を使う。いずれも内部で `async`/`immediate` に委譲するだけで、独自の `catch` ブロックは持たない。

Service 実装での使い方:

```kotlin
class XxxServiceImpl(
    private val resultHandler: ResultHandler,
    private val xxxRepository: XxxRepository
) : XxxService {
    override suspend fun fetchXxx(param: String): Result<DomainModel> = resultHandler.async {
        xxxRepository.fetchXxx(param)
    }

    override fun getLastXxx(): Result<String?> = resultHandler.immediate {
        xxxRepository.getLastXxx()
    }
}
```

### ViewModel での `Result.handle` の使い方

```kotlin
// composeApp/base/src/commonMain/kotlin/org/starter/project/base/extension/ResultExtension.kt
inline fun <Success> Result<Success>.handle(
    handler: (Throwable) -> Unit,
    shouldCancelScope: CoroutineScope? = null
): Success? {
    return this.getOrElse {
        handler(it)
        shouldCancelScope?.cancel(it.message.orEmpty())
        null
    }
}

inline fun <Success> Result<Success>.handle(
    handler: (Throwable) -> Unit,
    default: Success,
    shouldCancelScope: CoroutineScope? = null,
): Success {
    return this.getOrElse {
        handler(it)
        shouldCancelScope?.cancel(it.message.orEmpty())
        default
    }
}
```

`shouldCancelScope` は「このエラーが致命的で、これ以上の後続処理（同じ `CoroutineScope` 内の他の収集処理など）を継続する意味が無い」場合にのみ渡す。通常のエラー表示だけであれば省略してよい。失敗時に `null` ではなく既定値を使いたい場合は `default` 引数付きの第2オーバーロードを使う。

初回ロードとページング追加ロードを切り分ける ViewModel の実装（**新規決定**。サンプルの `HomeScreenViewModel` は初回・追加ページを区別せず常に `ErrorScreenThrowableHandler` を使っており、これは § アンチパターンで扱う既知の逸脱）:

```kotlin
@VisibleForTesting
internal suspend fun fetchXxx(keyword: String, key: String?): DomainModel? {
    val result = xxxService.fetchXxx(keyword, key)
    return if (key == null) {
        // 初回ロード: 全画面エラーに変換する
        result.handle(ErrorScreenThrowableHandler(_screenState))
    } else {
        // 追加ページ: ここでは変換せず、例外として PagingSource に伝播させる
        result.getOrThrow()
    }
}
```

`ArticlesPagingSource`（UI 層）側は変更不要で、`fetcher` が投げた例外をそのまま `LoadState.Error` に変換する:

```kotlin
override suspend fun load(params: LoadParams<String>): LoadResult<String, Article> {
    return try {
        params.key ?: onRefresh() // 初回ロード（key == null）時のみ呼ばれる、リフレッシュ状態通知用コールバック
        val currentKey = params.key
        val data = fetcher(currentKey)
        val nextKey = data?.nextPage
        currentKey ?: onLoadedFirstPage() // 初回ページ取得完了の通知用コールバック（HomeScreenViewModel.updateScreenSuccess 等に接続）
        LoadResult.Page(
            data = data?.articles.orEmpty(),
            prevKey = currentKey,
            nextKey = nextKey
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }
}
```

### Snackbar のクリア配線

Snackbar 配線（`SystemScaffold` → `onSnackBarShown` → ViewModel の `clearSnackBar()`）の正本は [../guides/ui-layer.md](../guides/ui-layer.md) UI-13 に一本化されている。本書では実装パターンを重複させず、UI-13（Event 経由での `dispatch(XxxScreenEvent.OnSnackBarShown)` → `XxxScreenEventHandler` → `viewModel.clearSnackBar()` という配線と、`SystemScaffold` に `onSnackBarShown` コールバックを追加する差分）を参照すること。

## アンチパターン

| アンチパターン | なぜダメか | 正しい形 |
|---|---|---|
| `ArticlesPagingSource` の `try/catch` をそのまま前提にする | サンプルの `HomeScreenViewModel.fetchArticles` は `key`（ページ）に関わらず常に `zennService.fetchArticles(...).handle(ErrorScreenThrowableHandler(_screenState))` を呼ぶ。この `.handle()` は失敗時に例外を投げず `null` を返すため、`ArticlesPagingSource.load()` の `try/catch` は fetcher からは実質到達しない（デッドコードに近い）。結果としてページング失敗時も常に全画面 `Failure` が優先表示される。 | 初回ロード（`key == null`）だけ `ErrorScreenThrowableHandler` で変換し、追加ページ（`key != null`）は `Result.getOrThrow()` で例外をそのまま `PagingSource` に伝播させる（§ 実装パターン、ERR-12）。 |
| `SnackBarThrowableHandler` を「用意されているから」といって配線せずに放置する | サンプルの `SnackBarThrowableHandler` はどの ViewModel からも呼ばれておらず未使用コードになっている。加えて `ScreenState.snackBarState` を表示後に `null` へ戻す処理がどこにも無く、配線してもスナックバーが繰り返し表示され続ける不具合を招く。 | 軽微なエラーには `.handle(SnackBarThrowableHandler(_screenState))` を使い、[../guides/ui-layer.md](../guides/ui-layer.md) UI-13 の配線（`SystemScaffold` の `onSnackBarShown` → `dispatch` → `EventHandler` → `viewModel.clearSnackBar()`）まで完成させる。 |
| `ResultHandler` の例外ログに `Napier.d` を使う | 例外捕捉は重要度の高いログであり、本番ビルドで `d` レベルがフィルタされると障害の把握が遅れる。 | `Napier.e` を使う（ERR-13）。 |
| Repository・Converter・ViewModel で `Result` を自前構築する（`Result.success(...)`/`Result.failure(...)` を各所に書く） | `Result` 化の境界が `ResultHandler` の1箇所という規約が崩れ、`ensureActive()` によるキャンセル再送出などの共通処理が各所で欠落するリスクが生まれる。 | `Result` 化は `ResultHandler.async`/`immediate` にのみ任せる（ERR-6）。 |
| `catch (e: Throwable)` の後に `ensureActive()` を呼ばない、または `runCatching` で無条件に握りつぶす | `CancellationException` まで `Result.failure` に握りつぶすと、コルーチンのキャンセルが正しく伝播せず構造化並行性が壊れる。 | `ResultHandler` の実装（§ 実装パターン）のとおり、`Result.failure(e)` を返す前に `coroutineContext.ensureActive()` を呼ぶ（ERR-7）。 |
| ViewModel が `Result.getOrThrow()`/`getOrNull()` を素で使い、独自に `try/catch` する | `ThrowableHandler` による表示経路の統一（全画面/Snackbar/無視）が失われ、画面ごとにエラー表示のばらつきが生まれる。 | `.handle(handler)` を使い、用途に応じた `ThrowableHandler` を選ぶ（ERR-10、ERR-11）。ページング追加ロードのみ例外として `getOrThrow()` を使う（§ 実装パターン参照）。 |

## 参考実装（サンプル。削除後は存在しない）

| パス | 説明 |
|---|---|
| `composeApp/base/src/commonMain/kotlin/org/starter/project/base/error/ApiError.kt` | `ApiError` の実例（`Unauthorized`/`Unknown` の2種類、`ApiErrorResponse`）。 |
| `composeApp/base/src/commonMain/kotlin/org/starter/project/base/error/ConversionError.kt` | `ConversionError` の実例（`ResponseNotNullValidation`）。 |
| `composeApp/base/src/commonMain/kotlin/org/starter/project/base/extension/ConversionErrorExtension.kt` | `validateNotNull` 拡張関数。 |
| `composeApp/base/src/commonMain/kotlin/org/starter/project/base/extension/ResultExtension.kt` | `Result<Success>.handle(handler, shouldCancelScope)` の実装。 |
| `composeApp/core/src/commonMain/kotlin/org/starter/project/core/api/ApiClient.kt` | `HttpResponseValidator` による HTTP ステータス→`ApiError` 変換の実例。 |
| `composeApp/domain/service/src/commonMain/kotlin/org/starter/project/domain/service/ResultHandler.kt` | `Result` 化の実例。`Napier.d`/analytics TODO は既知の逸脱（本文参照）。 |
| `composeApp/domain/service/src/commonMain/kotlin/org/starter/project/domain/service/ZennService.kt` | Service interface が `Result<T>` を返す実例。 |
| `composeApp/domain/zenn/src/commonMain/kotlin/org/starter/project/domain/zenn/ZennServiceImpl.kt` | `resultHandler.async`/`immediate` の呼び出し実例。 |
| `composeApp/data/zenn/src/commonMain/kotlin/org/starter/project/data/zenn/converter/ArticlesConverter.kt` | 一覧変換で `ConversionError` を1件ずつ握りつぶす実例。analytics TODO あり。 |
| `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/handler/ErrorScreenThrowableHandler.kt` | 全画面エラー表示ハンドラの実例。 |
| `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/handler/SnackBarThrowableHandler.kt` | Snackbar ハンドラの実例（未配線。§ アンチパターン参照）。 |
| `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/handler/IgnoreThrowableHandler.kt` | 無視ハンドラの実例。 |
| `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/state/ScreenState.kt` | `ScreenState`/`ScreenLoadingState`/`SnackBarState` の定義。 |
| `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/design/system/scaffold/SystemScaffold.kt` | `ScreenLoadingState.Failure` の全画面表示、Snackbar の `LaunchedEffect` 表示（クリア処理は無い。§ アンチパターン参照）。 |
| `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/component/article/ArticlesPagingSource.kt` | Paging の `try/catch`（fetcher 由来の例外に対しては到達しない。§ アンチパターン参照）。 |
| `composeApp/feature/home/src/commonMain/kotlin/org/starter/project/feature/home/HomeScreenViewModel.kt` | `.handle(ErrorScreenThrowableHandler(_screenState))` の呼び出し実例（初回・追加ページを区別していない既知の逸脱）。 |

## チェックリスト

- [ ] 新しいエラー分類は `ApiError`（汎用 HTTP）か、ドメイン固有の新規 `sealed class`（ビジネスエラー）かを判断して追加したか（ERR-2）。
- [ ] Repository・Converter（一覧変換以外）が例外を透過しているか（`try/catch` していないか）。
- [ ] `Result` 化を `ResultHandler` 以外の場所で行っていないか。
- [ ] `ResultHandler` の `catch` ブロックで `ensureActive()` を呼んでいるか。
- [ ] ViewModel が `Result.handle(handler)` を使い、用途に応じた `ThrowableHandler` を選んでいるか。
- [ ] 初回ロード失敗と追加ページ失敗を区別し、それぞれ正しい経路（全画面 / `LoadState.Error`）に流しているか。
- [ ] Snackbar を使う場合、表示後に `snackBarState` を `null` へ戻す配線（[../guides/ui-layer.md](../guides/ui-layer.md) UI-13）まで実装したか。
- [ ] フォーム入力のバリデーションエラーを全画面/Snackbar 経路に流さず、`UiState` のフィールドへマッピングしているか（ERR-16）。
- [ ] 例外ログに `Napier.e`（捕捉）/`Napier.d`（追跡）/`Napier.v`（通信詳細）を正しく使い分けているか。
- [ ] analytics 送信を実装する場合、`ResultHandler` の `catch` 節に集約したか（一覧変換の握りつぶしエラーは例外。ERR-14）。
