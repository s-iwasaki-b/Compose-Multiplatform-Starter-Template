# composeApp/feature/home

一覧表示・検索・pull-to-refresh を持つ 1 画面の feature モジュールで、`feature/<name>` 系モジュール一般の実装パターンを Paging3 を伴う一覧画面の具体例として示す。本ファイルはこのモジュールで作業する実装エージェントの入口である。ルートの [AGENTS.md](../../../AGENTS.md)（絶対ルール・実行体制）を前提とし、ツールが自動で読み込まない場合は先に読む。実装エージェントは本モジュール外のファイルを編集せず、必要な他モジュールの変更は「他モジュールとの接点」に沿ってオーケストレーターへ報告する。読み順は本ファイル → 「参照」節の docs。

## 責務

- **置く**: 1 機能ドメイン（ユーザーフロー）の画面一式。1 画面なら `XxxScreen`/`XxxScreenState`/`XxxScreenEvent`/`XxxScreenViewModel` の 4 点セットをパッケージ直下に置く。
- **置く**: 画面専用の小コンポーネント。ただし 2 箇所以上で再利用される、または `base` のドメインモデルに依存しない汎用部品になった場合は `ui` へ昇格する（→ coding-guide.md §4 判断基準）。
- **置かない**: Repository/Service の実装、DTO、`data`/`domain:<name>` の型。
- **置かない**: 他 feature モジュールへの直接参照。画面間の連携は Route 経由（`AppRouter.navigate`）にする。
- **置かない**: `AppRoute` の定義そのもの。ルートは `ui` に集約する。

## 依存

`build.gradle.kts`（現物）と一致:

| 依存 | 種別 | 用途 |
|---|---|---|
| `projects.composeApp.ui` | `api` | `AppRoute`/`AppRouter`、`SystemScaffold` 等の共通コンポーネント、`ArticlesPagingSource` |
| `projects.composeApp.domain.service` | `implementation` | `ZennService`（ViewModel のコンストラクタ注入） |

- **必須** convention plugin は `kmp-compose-library`。
- **必須** `data:<source>`・`domain:<name>`（実装モジュール）・他 `feature:<name>` への依存を追加しない（→ decisions.md D-10。Gradle 依存で物理的に禁止されている）。
- 本モジュールは `composeResources/` を持たないため `compose-components-resources` への依存は不要（文言を追加する場合は `feature/user` の依存構成を参照）。

## 構成

```
composeApp/feature/home/src/commonMain/kotlin/org/starter/project/feature/home/
  HomeScreen.kt          # HomeScreen（stateful）/ HomeScreenContent（stateless）
  HomeScreenState.kt     # HomeScreenState
  HomeScreenEvent.kt     # HomeScreenEvent / HomeScreenEventHandler
  HomeScreenViewModel.kt # HomeScreenViewModel
```

1 画面のみのため `<screen>/` サブパッケージは切らない。画面専用コンポーネントが必要になったら `component/` を追加する（現状は無し）。

## 実装パターン

4 点セットの命名・骨格は coding-guide.md §4 の一般形に従う。本モジュール固有の差分は検索キーワードに連動する Paging3 の組み立て:

```kotlin
// HomeScreenViewModel.kt（検索キーワード → debounce → Pager 切替）
val articlesPagingFlow = _state
    .map { it.searchKeyword }
    .distinctUntilChanged()
    .debounce(300.milliseconds)
    .flatMapLatest { keyword ->
        Pager(PagingConfig(ArticlesPagingSource.PAGE_SIZE)) {
            ArticlesPagingSource(
                onRefresh = ::updateScreenLoading,
                onLoadedFirstPage = ::updateScreenSuccess,
                fetcher = { key -> fetchArticles(keyword, key) }
            )
        }.flow
    }.cachedIn(viewModelScope)
```

- `ArticlesPagingSource` は `ui` の `shared/component/article/` にある共通実装（→ decisions.md D-05）。Screen 側は `collectAsLazyPagingItems()` で受け取る。
- Pull-to-refresh 中は `updateScreenLoading()` が全画面ローディングを抑制する（`isPullToRefreshing` を見て早期 return）。新しい一覧画面で pull-to-refresh を実装する場合はこの分岐を踏襲する。
- ナビゲーション引数（`AppRoute.Home.NavArgs.keyword`）を検索欄の初期値に使う場合、`LaunchedEffect(keyword)` で `viewModel.updateSearchKeyword`/`initSearchKeyword` を呼び分ける（deep link 経由か通常遷移かの分岐）。

## テスト

- 対象は `HomeScreenViewModel`。Mokkery で `ZennService` を mock し、`Dispatchers.setMain(StandardTestDispatcher())`/`resetMain()` を使う（→ coding-guide.md §5, decisions.md D-06, D-17, D-18）。
- `updateScreenLoading`/`updateScreenSuccess`/`fetchArticles`/`initSearchKeyword`/`updateSearchKeyword` は `@VisibleForTesting internal` のため、`Pager`/`PagingSource` を経由せずテストから直接呼べる。
- 検証すること: 初期状態（`ScreenLoadingState.Initial`）、`updateSearchKeyword` 呼び出し後の `state.searchKeyword`、`isPullToRefreshing` が true の間 `updateScreenLoading` が状態を変えないこと、`fetchArticles` が `zennService.fetchArticles(keyword, nextPage)` を呼ぶこと。
- 配置: `composeApp/feature/home/src/commonTest/kotlin/org/starter/project/feature/home/HomeScreenViewModelTest.kt`。
- 実行: `./gradlew :composeApp:feature:home:testAndroidHostTest`（モジュール単位）/ `./gradlew testAndroidHostTest`（全体）。Gradle はこのエージェントからは実行しない。

## 他モジュールとの接点

- 新しい画面（例: 記事詳細）を同じユーザーフローとして追加する場合、本モジュール内に `<screen>/` サブパッケージを切る（→ design-guide.md §3 判断表）。独立した機能ドメインなら新しい `feature:<name>` を新設する。
- 新しい Route を追加する場合: `ui` の `AppRoute.kt` にネストクラスを追加し、`app` の `AppNavHost.kt` に 4 ステップ登録（`composable` → `toRoute` → `koinViewModel` → Screen 呼び出し）、`app` の `Koin.kt` の `appModule` に `viewModelOf` を追加する。
- 新しい `feature:<name>` を新設する場合（本モジュールをテンプレートとしてコピーする場合）は `settings.gradle.kts` への `include` が手動で必要。`composeApp/build.gradle.kts` は `maxDepth=3` の動的スキャンで自動検出するため編集不要（→ design-guide.md §2）。
- 実装エージェントは自モジュール外を編集せず、上記をオーケストレーターへの完了報告に申し送り事項として記載する。

## 完了条件

- `HomeScreenViewModel` のコンストラクタ引数が `domain:service` の interface のみであることを確認した。
- 追加・変更した公開状態が `combine(_screenState, _state).stateIn(...)` 経由で公開されている（直接代入していない）ことを確認した。
- 画面遷移が `HomeScreenEventHandler` からのみ発火し、ViewModel に `AppRouter` を注入していないことを確認した。
- 変更した ViewModel ロジックに対応するテストがあり、`./gradlew :composeApp:feature:home:testAndroidHostTest` がローカルで通ることを確認した（Gradle 実行不可の環境では確認方法を報告に明記する）。
- `../../../docs/coding-guide.md` §8 の既知の逸脱を新規コードに複製していない。

## 参照

- ../../../docs/design-guide.md §1, §3, §5, §6
- ../../../docs/coding-guide.md §1, §4, §5
- ../../../docs/decisions.md D-05, D-09, D-10, D-15, D-16, D-19
