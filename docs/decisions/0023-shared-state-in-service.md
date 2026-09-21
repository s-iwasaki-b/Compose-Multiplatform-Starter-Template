# ADR-0023: 画面間・アプリ全体で共有する状態は `domain:service` の状態保持 Service に置く

> ステータス: 採用
> 日付: 2026-09-21
> 実例: 実例なし（本 ADR で新規決定）
> 関連: [domain-layer.md](../guides/domain-layer.md), [ui-layer.md](../guides/ui-layer.md), [dependency-injection.md](../architecture/dependency-injection.md), [0010-feature-depends-on-service-only.md](0010-feature-depends-on-service-only.md), [0012-flow-only-for-observation.md](0012-flow-only-for-observation.md), [0016-one-shot-events.md](0016-one-shot-events.md)

## Context

`koinViewModel<XxxScreenViewModel>()`（`composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/AppNavHost.kt` から呼ばれる）は画面（Route）ごとに ViewModel を生成するため、複数画面で同一の ViewModel インスタンスを共有できない。しかしカート、ログイン状態、テーマ設定（ダークモードの手動トグルなど）のように、アプリ全体・複数画面で同じ状態を参照・更新したいケースは今後必ず発生する。既存のドキュメント（`dependency-injection.md`/`navigation.md`）には画面をまたぐ状態共有パターンの記述が一切なかった。ダークモードの手動トグルについても、`SystemTheme()`（`composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/design/system/theme/SystemTheme.kt`）は引数を取らない `@Composable fun SystemTheme(content: @Composable () -> Unit)` で、`Main()`（`composeApp/app/src/commonMain/kotlin/org/starter/project/app/MainApp.kt`）が1回だけラップする構成しかなく、ユーザー操作でトグルした値をアプリ全体へ反映する経路が存在しない。

## Decision

- **必須**: 複数画面・アプリ全体で共有する状態（カート、ログイン状態、テーマ設定など）は `domain:service` に状態保持用の Service interface を置き、実装 `domain:<name>` の `XxxServiceImpl` が `MutableStateFlow` を保持する。状態の永続化が必要な場合は Preferences 系 Repository（`data:repository`）に保存する。
- **必須**: ViewModel を画面間で共有しない。`koinViewModel` は画面スコープであることを前提とし、共有が必要な状態は ViewModel ではなく Service（`StateFlow` を公開する interface）で表現する。
- **必須**: 状態保持 Service の `StateFlow` 公開は [0012-flow-only-for-observation.md](0012-flow-only-for-observation.md) の「継続監視が要件のとき」に該当する。単発取得の Service に無条件で `Flow` を追加してはならない。
- **推奨**（現状未実装の差分）: ダークモードの手動トグルは、`SystemTheme` に `darkTheme: Boolean = isSystemInDarkTheme()` 引数を追加し、`Main()`（`composeApp/app` に属し、`domain:service` に依存できる唯一の Compose ルート）が `koinInject<AppSettingsService>()` で取得した `themeMode` を `collectAsStateWithLifecycle()` して `SystemTheme(darkTheme = ...)` に渡す形で実装する。

## 判断基準

| 状況 | 判断 |
|---|---|
| 状態が1画面内でしか使われない | 通常どおり ViewModel の `UiState` に持たせる（本 ADR の対象外） |
| 状態を複数画面・アプリ全体で共有し、購読者が変化をリアルタイムに受け取る必要がある | `domain:service` に状態保持 Service（`val xxx: StateFlow<T>`）を置く |
| 状態をアプリ再起動後も保持したい | Service 実装が Preferences 系 Repository（`data:repository`）に永続化する |
| どの Compose ルートから Service の `StateFlow` を購読するか | `Main()`（`composeApp/app`）のようなアプリルートで `koinInject<XxxService>()` + `collectAsStateWithLifecycle()` を使う。feature 内の Composable から直接 `koinInject` しない（ADR-0010 の DI 境界と整合させるため、feature は ViewModel 経由でのみ Service に触れる） |

## Consequences

- メリット: 画面をまたぐ状態共有のパターンが一元化され、AI エージェントが Koin のスコープ機構にない「共有 ViewModel」のような構成を誤って提案するのを防げる。テーマ設定の手動トグルという既存の未解決ギャップに具体的な実装経路を与える。
- デメリット / トレードオフ: 状態保持 Service は他の Service（単発取得の `Result<T>` 中心）とシグネチャの性質が異なる（`StateFlow` を持つ）ため、interface 内でメソッドの性質が混在しないよう設計時に注意が必要。`Main()` が `domain:service` に依存する点は、feature モジュールの依存境界（`feature` は `domain:service` と `ui` にのみ依存）とは別の話であることを明示する必要がある（`Main()` は feature モジュールではなく `app` モジュールに属する）。
- 守らせる手段: レビュー（pr-checklist.md）で確認。Gradle での機械的強制はできない。

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| 複数画面で同じ ViewModel インスタンスを共有する（Navigation の親グラフスコープ等） | `koinViewModel` は画面（Route）スコープが既定であり、共有スコープを導入すると DI 登録・ライフサイクル管理が複雑化する。既存の `AppNavHost.kt` の登録パターンとも乖離する。 |
| グローバルな `object` シングルトンで状態を保持する | Koin の DI 管理外になり、テストでの差し替え（Mokkery でのモック）ができなくなる。 |

## サンプル削除後の扱い

Zenn サンプルには状態保持 Service の実例が無い。本 ADR の Decision と判断基準表のみが、新規に共有状態を設計する際の唯一の参照先になる。`AppSettingsService` はあくまで例示であり、実装時は対象ドメインに応じた名前を付ける。
