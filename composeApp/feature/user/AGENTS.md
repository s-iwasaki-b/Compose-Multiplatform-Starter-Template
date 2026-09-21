# composeApp/feature/user

引数付き Route（識別子）から起動する詳細画面 1 つを持つ feature モジュールで、`feature/<name>` 系モジュール一般の実装パターンを詳細画面 + 画面専用コンポーネント + Compose Resources の具体例として示す。本ファイルはこのモジュールで作業する実装エージェントの入口である。ルートの [AGENTS.md](../../../AGENTS.md)（絶対ルール・実行体制）を前提とし、ツールが自動で読み込まない場合は先に読む。実装エージェントは本モジュール外のファイルを編集せず、必要な他モジュールの変更は「他モジュールとの接点」に沿ってオーケストレーターへ報告する。読み順は本ファイル → 「参照」節の docs。

## 責務

- **置く**: 1 機能ドメイン（ユーザーフロー）の画面一式。1 画面なら 4 点セットをパッケージ直下に置く。
- **置く**: 画面専用コンポーネント（`component/`）。他画面から再利用されない、または `base` のドメインモデルに依存する部品はここに留める（→ coding-guide.md §4 判断基準）。
- **置く**: 本モジュールでのみ使う表示文言（`composeResources/values/string.xml`）。
- **置かない**: Repository/Service の実装、DTO、`data`/`domain:<name>` の型。
- **置かない**: 他 feature モジュールへの直接参照、`AppRoute` の定義そのもの（`ui` に集約）。

## 依存

`build.gradle.kts`（現物）と一致:

| 依存 | 種別 | 用途 |
|---|---|---|
| `projects.composeApp.ui` | `api` | `AppRoute`/`AppRouter`、`SystemScaffold` 等の共通コンポーネント |
| `projects.composeApp.domain.service` | `implementation` | `XxxService`（ViewModel のコンストラクタ注入） |
| `libs.compose.components.resources` | `implementation` | 自モジュールの `composeResources/` を使うため必須（→ design-guide.md §2。省略するとビルドが壊れる） |

- **必須** convention plugin は `kmp-compose-library`。`android { androidResources { enable = true } }` と `compose.resources { publicResClass = false }` は `composeResources/` を自モジュールに持つ場合の必須設定（feature モジュールは他モジュールから `Res` を参照されないため `false`）。
- **必須** `data:<source>`・`domain:<name>`・他 `feature:<name>` への依存を追加しない（→ decisions.md D-10）。

## 構成

```
composeApp/feature/<name>/src/commonMain/
  kotlin/org/starter/project/feature/<name>/
    XxxScreen.kt      # XxxScreen（stateful）/ XxxScreenContent（stateless）
    XxxScreenState.kt # XxxScreenState
    XxxScreenEvent.kt # XxxScreenEvent / XxxScreenEventHandler
    XxxViewModel.kt   # XxxViewModel
    component/
      XxxHeader.kt    # 画面専用コンポーネント（例: プロフィールヘッダ）
  composeResources/values/
    string.xml          # 本モジュール専用の表示文言
```

## 実装パターン

4 点セットの命名・骨格は coding-guide.md §4 の一般形に従う。本モジュール固有の差分は Route 引数から画面固有の状態を取得する初期化と、画面専用コンポーネントの置き方:

```kotlin
// XxxScreen.kt（Route 引数をトリガーに ViewModel を初期化する）
@Composable
fun XxxScreen(viewModel: XxxViewModel, appRouter: AppRouter, navArgs: AppRoute.Xxx.NavArgs) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(navArgs.id) { viewModel.refresh(navArgs.id) }
    XxxScreenContent(state = state, itemsPagingItems = itemsPagingItems) { event ->
        XxxScreenEventHandler(event = event, appRouter = appRouter, itemsPagingItems = itemsPagingItems)
    }
}
```

- `refresh(id)` は `_state.id` を更新する。ViewModel 内の `fetchDetail()`（`init` で起動する `collect`）と `itemsPagingFlow` はいずれもこの `id` を `distinctUntilChanged()` で監視し連動する。
- `XxxHeader` は `Xxx`（`base` のドメインモデル）に依存し、かつ本画面でしか使わないため `component/` に置く。2 箇所以上で再利用する必要が出たら `ui` の `shared/component/` へ昇格する（→ coding-guide.md §4 判断基準）。
- 文言を `string.xml` に追加する場合、ベース言語は日本語に統一する（→ decisions.md D-21）。

## テスト

- 対象は `XxxViewModel`。Mokkery で `XxxService` を mock し、`Dispatchers.setMain(StandardTestDispatcher())`/`resetMain()` を使う（→ coding-guide.md §5, decisions.md D-06, D-17, D-18）。
- `init { fetchDetail() }` が `viewModelScope.launch` を起動するため、`subject` はプロパティ初期化子ではなく `@BeforeTest` 内で `Dispatchers.setMain(...)` の**後**に生成する（`lateinit var` にする）。
- `fetchDetail`/`updateScreenLoading`/`updateScreenSuccess`/`fetchItems` は `@VisibleForTesting internal` のため直接呼べる。
- 検証すること: 初期状態、`refresh(id)` 呼び出し後に `xxxService.fetchDetail(id)` が呼ばれ `state` が更新されること、取得失敗時に `screenState.screenLoadingState` が `Failure` になること。
- 配置: `src/commonTest/kotlin/org/starter/project/feature/<name>/XxxViewModelTest.kt`（本モジュール配下）。
- 実行: `./gradlew :composeApp:feature:<name>:testAndroidHostTest`（モジュール単位）/ `./gradlew testAndroidHostTest`（全体）。Gradle はこのエージェントからは実行しない。

## 他モジュールとの接点

- 新しい画面を同じユーザーフローとして追加する場合、本モジュール内に `<screen>/` サブパッケージを切る（→ design-guide.md §3 判断表）。独立した機能ドメインなら新しい `feature:<name>` を新設する。
- Route 引数を増やす場合: `ui` の `AppRoute.kt` の `AppRoute.Xxx`/`NavArgs` を編集する。引数はプリミティブ型のみにする（→ design-guide.md §5）。
- 遷移元（`feature:<name>` 等）から本画面への遷移を追加・変更する場合、遷移元の `XxxScreenEventHandler` の分岐を編集する（本モジュールは編集しない）。
- 新しい `feature:<name>` を新設する場合（本モジュールをテンプレートとしてコピーする場合）は `settings.gradle.kts` への `include` が手動で必要。`composeApp/build.gradle.kts` は `maxDepth=3` の動的スキャンで自動検出するため編集不要（→ design-guide.md §2）。
- 実装エージェントは自モジュール外を編集せず、上記をオーケストレーターへの完了報告に申し送り事項として記載する。

## 完了条件

- `XxxViewModel` のコンストラクタ引数が `domain:service` の interface のみであることを確認した。
- 画面遷移（戻る操作を含む）が `XxxScreenEventHandler` からのみ発火し、ViewModel に `AppRouter` を注入していないことを確認した。
- `component/` に追加したコンポーネントの再利用範囲を確認し、置き場所の判断基準（coding-guide.md §4）に沿っていることを確認した。
- 変更した ViewModel ロジックに対応するテストがあり、`./gradlew :composeApp:feature:<name>:testAndroidHostTest` がローカルで通ることを確認した（Gradle 実行不可の環境では確認方法を報告に明記する）。
- `../../../docs/coding-guide.md` §8 の既知の逸脱を新規コードに複製していない。

## 参照

- ../../../docs/design-guide.md §1, §3, §5, §6
- ../../../docs/coding-guide.md §1, §4, §5
- ../../../docs/decisions.md D-09, D-10, D-15, D-16, D-19, D-21, D-22
