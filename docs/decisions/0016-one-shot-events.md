# ADR-0016: 画面遷移は EventHandler が `AppRouter` を直接呼ぶ。Snackbar は `ScreenState.snackBarState` で表現する

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（Snackbar は未配線。`composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/handler/SnackBarThrowableHandler.kt`）
> 関連: [ui-layer.md](../guides/ui-layer.md), [navigation.md](../architecture/navigation.md), [0015-uistate-structure.md](0015-uistate-structure.md), [0019-error-display-routes.md](0019-error-display-routes.md)

## Context

画面遷移や Snackbar 表示のような「一度だけ実行したい」処理を ViewModel に持たせるか Screen 層に持たせるかは、ViewModel のテスト容易性に直結する。既存コードは Navigation を `XxxScreenEventHandler`（Screen 層）に置き、ViewModel は `AppRouter` を注入されていない。一方 `SnackBarThrowableHandler` は定義されているが、**現状どの画面からも呼ばれておらず、表示後にクリアする処理もない**（既知の逸脱）。この配線の未完成が放置されると、新規画面でエラー通知をどう実装すべきか判断できない。

## Decision

- **必須**: 画面遷移（Navigation）は `XxxScreenEventHandler`（Screen 層）が `appRouter.navigate(...)` / `appRouter.popBackStack()` を直接・同期的に呼ぶ。ViewModel のコンストラクタに `AppRouter` を注入しない。
- **必須**: Snackbar 等の一回性通知は、Channel/SharedFlow の専用ストリームを新設せず、`ScreenState.snackBarState: SnackBarState?` に値をセットする「state 型」で表現する。
- **必須**（新規決定。既知の逸脱の是正）: Snackbar を実際に使う画面では、ViewModel のエラーハンドラチェーンに `SnackBarThrowableHandler(_screenState)` を追加して `snackBarState` をセットし、`SystemScaffold` の `LaunchedEffect(screenState.snackBarState)` で表示した後、表示完了時に `snackBarState` を `null` に戻す（ViewModel に `clearSnackBar()` 相当のメソッドを用意し、`showSnackbar()` 完了後に呼ぶ）配線を完成させてから使う。

## 判断基準

| 状況 | 判断 |
|---|---|
| ボタンタップ等で次画面へ遷移したい | `EventHandler` が `AppRouter.navigate` を直接呼ぶ。ViewModel は関与しない |
| エラー発生時に画面全体を差し替えたい（致命的） | `ScreenState.screenLoadingState` を `Failure` にする（ADR-0019 参照） |
| エラーや操作結果を一時的な通知として出したい（軽微） | `SnackBarThrowableHandler` 経由で `snackBarState` にセットし、表示後に `null` へ戻す |

## Consequences

- メリット: ViewModel が Navigation ライブラリに依存しないためユニットテストが容易。Snackbar も新しいストリーム型を増やさず `ScreenState` の合成パターン内に収まる。
- デメリット / トレードオフ: 現状 Snackbar 機構は定義のみで、どの画面からも呼ばれておらず表示後クリア処理もない（既知の逸脱）。この配線を新規実装者が完成させる必要がある。
- 守らせる手段: レビュー（pr-checklist.md）で確認。

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| ViewModel が Channel/SharedFlow で一回性イベント（Navigation/Snackbar）を発行し、Screen が `LaunchedEffect` で収集する | 既存 2 画面ともこのパターンの実例がなく、「ViewModel は `AppRouter` を知らない」という既存の責務分離と衝突する。Navigation は同期呼び出しで要件を満たせており、Channel 導入の必然性がない。 |
| Snackbar もエラー全画面表示（`ScreenState.Failure`）と同じ経路に統一する | 軽微なエラーまで全画面エラー表示にすると過剰な UX になる。既存コードに `SnackBarState` 型・`SnackBarThrowableHandler` が用意されている以上、この型を活かす方が既存資産と整合する。 |

## サンプル削除後の扱い

`AppRouter`/`ScreenState`/`SnackBarState`/`SnackBarThrowableHandler`/`SystemScaffold` はいずれも `composeApp/ui`/`composeApp/app` に残る共通型・共通コンポーネントであり、サンプル削除後もそのまま使える。ただし実際に呼び出す配線はサンプル（HomeScreen/UserScreen）側にしかなかったため、新規画面では本 ADR の Decision に従い呼び出しとクリア処理を自分で追加する必要がある。
