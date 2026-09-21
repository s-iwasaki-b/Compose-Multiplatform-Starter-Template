# ADR-0015: UiState は画面ごとに 1 つの `@Immutable data class`、先頭に共通 `ScreenState`

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（`composeApp/feature/home/src/commonMain/kotlin/org/starter/project/feature/home/HomeScreenState.kt`, `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/state/ScreenState.kt`）
> 関連: [ui-layer.md](../guides/ui-layer.md), [0016-one-shot-events.md](0016-one-shot-events.md), [0019-error-display-routes.md](0019-error-display-routes.md)

## Context

画面のロード状態・エラー状態・データ状態をどう表現するかは画面設計の起点になる決定であり、画面ごとにバラバラに実装すると `SystemScaffold` のような共通コンポーネントがローディング/エラー表示を一律に扱えなくなる。既存の `HomeScreenState`/`UserScreenState` はいずれも「画面固有のフィールドを持つ `data class`」＋「共通 `ScreenState`」という二層構成で統一されている。

## Decision

- **必須**: 画面固有の UiState は `internal data class XxxScreenState(val screenState: ScreenState, ...)` とし `@Immutable` を付与する。`screenState: ScreenState` を先頭フィールドとして必ず持つ。
- **必須**: ローディング/成功/失敗は画面ごとの独自 sealed class を作らず、`composeApp/ui` 共通の `ScreenLoadingState`（`Initial`/`Loading`/`Success`/`Failure`）を使う。
- **必須**: ViewModel 内部は `private val _screenState: MutableStateFlow<ScreenState>` と `private val _state: MutableStateFlow<XxxScreenState>` を分離して持ち、公開する `state` は `combine(_screenState, _state) { ... }.stateIn(viewModelScope, SharingStarted.Eagerly, ...)` として読み取り専用 `StateFlow` にする。更新は必ず `update { it.copy(...) }`。
- **推奨**（新規決定）: フィールドが 8 を超えて肥大化したら、関連フィールドをネストした `data class` にグルーピングする（`state` プロパティ自体は 1 つのまま維持する）。

## 判断基準

| 状況 | 判断 |
|---|---|
| 新規画面のロード中/成功/失敗を表現したい | 独自 sealed class を作らず共通 `ScreenLoadingState` を使う |
| フィールド数が 8 を超え可読性が落ちてきた | ネストした `data class` でグルーピングする。`state` プロパティ自体は分割しない |
| `SharingStarted` を `Eagerly` にするか `WhileSubscribed` にするか迷う | 既存 2 画面とも `Eagerly` を使用しているため、明確な理由がない限り `Eagerly` を踏襲する |

## Consequences

- メリット: `SystemScaffold` がすべての画面で共通のローディング/エラー表示ロジックを再利用できる。sealed interface による分岐の複雑化を避けられる。
- デメリット / トレードオフ: `data class` にフラグや `screenState` を詰め込むため、理論上の状態の組み合わせ数は増える（実際には `screenLoadingState` が排他的に管理するため実害は小さい）。
- 守らせる手段: レビュー（pr-checklist.md）で確認。Gradle では強制できない。

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| `sealed interface UiState { Loading; Success; Error }` のように画面全体を排他的な sealed interface で表現する | ローディング中でも検索欄を表示し続けたいなど、データとロード状態を同時に扱う場面でブランチごとにデータを複製する必要が生じ、既存 2 画面の実装（`data class` + 共通 `ScreenState`）と乖離する。 |
| 画面ごとに独自の LoadingState/ErrorState を定義する | `SystemScaffold` のような共通コンポーネントが画面ごとに異なる型を扱えず、Loading/Error 表示ロジックが画面数だけ重複する。 |

## サンプル削除後の扱い

`ScreenState`/`ScreenLoadingState`/`SnackBarState`（`composeApp/ui`）自体はサンプル削除後も残る共通型のため、この構造は実装として維持される。新規画面はこのテンプレートの `data class` パターンをそのままコピーして使える。
