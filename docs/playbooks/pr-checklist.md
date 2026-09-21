# PR 前セルフレビュー チェックリスト

> 対象: PR を作成する前に、実装が本リポジトリの規約に沿っているかを自己点検するためのチェックリスト。lint/CI（`.github/workflows`）が意図的に存在しないため、このチェックリストがその代替となる。
> 関連: [architecture/overview.md](../architecture/overview.md), [architecture/module-guide.md](../architecture/module-guide.md), [architecture/dependency-injection.md](../architecture/dependency-injection.md), [architecture/navigation.md](../architecture/navigation.md), [architecture/error-handling.md](../architecture/error-handling.md), [guides/data-layer.md](../guides/data-layer.md), [guides/domain-layer.md](../guides/domain-layer.md), [guides/ui-layer.md](../guides/ui-layer.md), [guides/testing.md](../guides/testing.md), [guides/coding-style.md](../guides/coding-style.md), [guides/git-workflow.md](../guides/git-workflow.md), [decisions/README.md](../decisions/README.md), [remove-sample-code.md](./remove-sample-code.md)
> 最終確認コミット: ac56102

各項目は `- [ ]` で1文判定可能に書いてある。末尾の括弧内は根拠となる ADR またはガイドへのリンク。該当項目が対象外（例: UI を変更しない PR で UI 層の項目）の場合はチェックせず読み飛ばしてよい。

## 全 PR 共通

- [ ] 変更差分が1つの目的に閉じている（無関係なリファクタや整形を混ぜていない）。
- [ ] `println` を使っていない（[guides/coding-style.md](../guides/coding-style.md)）。
- [ ] APIキー・パスワード・トークン等のシークレットがコード・設定ファイルに含まれていない。
- [ ] 追加した `TODO` コメントは実装のプレースホルダであり、放置されたバグ修正待ちの記述になっていない。

## 構造・依存

- [ ] `feature` モジュールが `data` 層（`data:repository` / `data:<source>`）に依存していない（[ADR-0010](../decisions/0010-feature-depends-on-service-only.md)）。
- [ ] 実装モジュール同士（例: `data:<source>` と `domain:<name>`）が直接依存していない。実装モジュールは契約モジュール（`data:repository` / `domain:service`）経由でのみ参照される（[ADR-0001](../decisions/0001-contract-and-impl-modules.md), [ADR-0002](../decisions/0002-repository-interface-in-data-layer.md)）。
- [ ] `feature` は機能ドメイン（ユーザーフロー）単位、`data`/`domain` は外部リソース単位という粒度で分割されている（[ADR-0009](../decisions/0009-module-granularity.md)）。
- [ ] 新規モジュールを追加した場合、`settings.gradle.kts` に `include` している（[architecture/module-guide.md](../architecture/module-guide.md)）。
- [ ] 新規モジュールを追加した場合、利用側から見える形で `composeApp/app/build.gradle.kts` の依存に追加している（[architecture/module-guide.md](../architecture/module-guide.md)）。
- [ ] 新規モジュールの convention plugin は Compose UI / Compose Resources の有無に応じて `kmp-library` か `kmp-compose-library` を選んでいる（[architecture/module-guide.md](../architecture/module-guide.md)）。
- [ ] 新規に DI 登録が必要な場合、`composeApp/app` の `Koin.kt` の該当レイヤーモジュール（`coreModule`/`dataSourceModule`/`repositoryModule`/`serviceModule`/`appModule`）に追加している（[ADR-0008](../decisions/0008-di-centralized-in-app.md)）。

## data 層

- [ ] Repository の実装が `Result` でラップしていない、かつ `withContext`/`Dispatchers` を使っていない（[ADR-0003](../decisions/0003-result-at-service-boundary.md), [ADR-0018](../decisions/0018-dispatcher-switch-in-resulthandler.md)）。
- [ ] レスポンス DTO の全フィールドが nullable かつ `= null` デフォルトを持つ（[guides/data-layer.md](../guides/data-layer.md)）。
- [ ] DTO → domain モデル変換は `object` + `operator fun invoke` 形式の Converter で行っている（[guides/data-layer.md](../guides/data-layer.md)）。
- [ ] 必須フィールドの欠落検出に `validateNotNull` と `ConversionError` を使っている（[guides/data-layer.md](../guides/data-layer.md)）。
- [ ] Repository/Service が `Flow` を返す場合、継続監視が要件であることが明確である。単発取得には使っていない（[ADR-0012](../decisions/0012-flow-only-for-observation.md)）。
- [ ] キャッシュ・オフライン・認証・リトライを新規実装した場合、`core`（HttpClient 設定）か `data:<source>` に置いている（[ADR-0013](../decisions/0013-defer-cache-auth-retry.md)）。
- [ ] ページングを追加した場合、Repository はカーソル付き単発取得のみを行い、`Pager`/`PagingSource` の組み立ては UI 層に置いている（[ADR-0005](../decisions/0005-paging-in-ui-layer.md)）。
- [ ] 既存 API と別ホストの API を追加した場合、`ApiClientImpl` を baseUrl 引数化し Koin の `named("<host>")` で複数登録している（[ADR-0025](../decisions/0025-multi-host-api-clients.md)）。

## domain 層

- [ ] Service interface の公開メソッドは `Result<T>` を返している（`Flow<T>` や生の型を返していない）（[ADR-0003](../decisions/0003-result-at-service-boundary.md)）。
- [ ] Service の実装は `ResultHandler.async`/`immediate` で本体を包んでいる（[ADR-0003](../decisions/0003-result-at-service-boundary.md), [ADR-0018](../decisions/0018-dispatcher-switch-in-resulthandler.md)）。
- [ ] domain モデルは `domain/*` モジュールではなく `base` に置かれている（[ADR-0004](../decisions/0004-domain-models-in-base.md)）。
- [ ] 入力バリデーションは Service 層、レスポンスの整合性検証は Converter に置かれている（[ADR-0014](../decisions/0014-validation-placement.md)）。
- [ ] 1つの Service は対象の外部リソース単位で複数メソッドを持つ設計になっている。1メソッド1クラスの UseCase パターンにしていない（[ADR-0011](../decisions/0011-service-per-resource.md)）。
- [ ] 複数 Repository を束ねる Service を書いた場合、コンストラクタ注入で束ねている（[guides/domain-layer.md](../guides/domain-layer.md)）。
- [ ] 画面間・アプリ全体で共有する状態（カート、ログイン状態、テーマ設定など）は `domain:service` の状態保持 Service に置いている。ViewModel を画面間で共有していない（[ADR-0023](../decisions/0023-shared-state-in-service.md)）。

## UI 層

- [ ] ViewModel は Service のみを注入されている。Repository を直接注入していない（[ADR-0010](../decisions/0010-feature-depends-on-service-only.md)）。
- [ ] Composable から ViewModel のメソッドを直接呼んでいない。`Event` を `dispatch` して `EventHandler` 経由で処理している（[ADR-0016](../decisions/0016-one-shot-events.md)）。
- [ ] 画面遷移は `EventHandler` が `AppRouter` を呼んでいる。ViewModel は `AppRouter` を注入されていない（[ADR-0016](../decisions/0016-one-shot-events.md)）。
- [ ] UiState は `screenState: ScreenState` を先頭フィールドに持つ `@Immutable data class` である（[ADR-0015](../decisions/0015-uistate-structure.md)）。
- [ ] `collectAsStateWithLifecycle()` を使っている（`collectAsState()` は既知の逸脱であり新規コードでは使わない）（[guides/ui-layer.md](../guides/ui-layer.md)）。
- [ ] 画面に表示する文言はハードコードせず Compose Resources（`composeResources/values/string.xml` 等）に切り出している（[ADR-0021](../decisions/0021-string-resources-base-language.md)）。
- [ ] Route（`AppRoute` のフィールド）に渡す引数はプリミティブ型のみである。複雑なオブジェクトは渡さず、遷移先で再取得している（[architecture/navigation.md](../architecture/navigation.md)）。
- [ ] `design/system` または `shared/component` 配下の再利用可能なコンポーネントは `modifier: Modifier = Modifier` を第一引数付近に持つ（[guides/ui-layer.md](../guides/ui-layer.md)）。
- [ ] ViewModel/Paging に依存しない末端コンポーネントに `@Preview` を付けている（[guides/ui-layer.md](../guides/ui-layer.md)）。
- [ ] `LazyColumn` の `key` は安定 ID である（index の文字列連結は既知の逸脱であり新規コードでは使わない）（[guides/ui-layer.md](../guides/ui-layer.md)）。
- [ ] エラー表示は「初回ロード失敗=全画面 Failure」「追加ページ失敗=`LoadState.Error`」「軽微なエラー=Snackbar」「入力バリデーションエラー=UiState のフィールド」の 4 経路の使い分けに従っている（[ADR-0019](../decisions/0019-error-display-routes.md)）。
- [ ] 新しい共通コンポーネントを追加した場合、置き場所（`design/system` はドメイン非依存、`shared/component/<domain>` はドメイン依存かつ2箇所以上で再利用、それ以外は feature 内 `component/`）が判断基準に沿っている（[ADR-0022](../decisions/0022-shared-component-placement.md)）。
- [ ] Material 3 への独断的な移行やテーマ値の恒久決定をしていない。`SystemTheme` の初期値はプレースホルダのままか、意図を PR に明記した差し替えである（[ADR-0020](../decisions/0020-material2-and-theme-placeholders.md)）。
- [ ] UI から呼ぶプラットフォーム固有機能（共有シート・外部ブラウザ起動・クリップボード等）は `ui` に interface を置き、`app` の `platformModule` で実装を注入している（[ADR-0024](../decisions/0024-platform-capabilities-via-app-module.md)）。

## テスト

- [ ] テストダブルは Mokkery で作成している。手書き Fake を新規に追加していない（[ADR-0006](../decisions/0006-mokkery-for-test-doubles.md)）。
- [ ] モック対象は interface である。具象クラスをモックする場合のみ対象クラスに `@OpenForTesting` を付けている（[guides/testing.md](../guides/testing.md)）。
- [ ] Converter・Repository 実装・Service 実装・ViewModel の必須範囲にテストを追加している（[ADR-0017](../decisions/0017-test-scope.md)）。
- [ ] テストクラス名は `対象クラス名Test`、メソッド名は `<methodName>_<condition>_<expectedResult>` 形式である（[guides/testing.md](../guides/testing.md)）。
- [ ] テストメソッド内に `// arrange` `// act` `// assert` のコメントがある（[guides/testing.md](../guides/testing.md)）。
- [ ] Dispatcher を使うテストは `StandardTestDispatcher` を注入している。ViewModel テストは `Dispatchers.setMain`/`resetMain` を使っている（[guides/testing.md](../guides/testing.md)）。
- [ ] 新規にテストを追加したモジュールの `build.gradle.kts` に `alias(libs.plugins.mokkery)` が適用されている（[ADR-0006](../decisions/0006-mokkery-for-test-doubles.md)）。

## スタイル

- [ ] `@OptIn` は関数・プロパティ・クラス単位で付けている。`@file:OptIn` を使っていない（[guides/coding-style.md](../guides/coding-style.md)）。
- [ ] `withContext`/`Dispatchers` の切り替えは `ResultHandler` の中にのみ書かれている（[ADR-0018](../decisions/0018-dispatcher-switch-in-resulthandler.md)）。
- [ ] ログは Napier を使っている。例外捕捉のログは `Napier.e`、追跡ログは `Napier.d`、通信詳細は `Napier.v` を使っている（[guides/coding-style.md](../guides/coding-style.md)）。
- [ ] 設定値は `internal object XxxConfig { const val ... }` の形に集約している（[guides/coding-style.md](../guides/coding-style.md)）。
- [ ] import はワイルドカードを使わずアルファベット順である。複数行引数の末尾に trailing comma を付けていない（[guides/coding-style.md](../guides/coding-style.md)）。
- [ ] 新規に追加・更新した依存ライブラリは安定版である。Alpha/Beta を採用した場合は評価記録を PR に残している（[ADR-0007](../decisions/0007-stable-dependencies.md)）。

## Git

- [ ] コミットメッセージが Conventional Commits 形式（`<type>(<scope>): <大文字始まりの命令形 Subject>`）である（[guides/git-workflow.md](../guides/git-workflow.md)）。
- [ ] ブランチ名が `claude/<kebab-case>` 形式である（[guides/git-workflow.md](../guides/git-workflow.md)）。
- [ ] PR 本文に `## Summary`（箇条書き）と `## Verification`（実施したテスト・動作確認をフリーテキストまたはチェックボックス形式で）があり、意図的に対応しなかった箇所があれば `## Intentionally left as-is` に書いている（[guides/git-workflow.md](../guides/git-workflow.md)）。
- [ ] `docs/README.md` に載っている既知の逸脱（例: `collectAsState()`、index キー、`Napier.d` での例外ログ）を新規コードにそのままコピーしていない。

## ドキュメント

- [ ] ガイド・ADR に書かれた規約と矛盾する変更をした場合、同じ PR で対応する `docs/` 配下のファイルも更新している（[decisions/README.md](../decisions/README.md)）。
- [ ] サンプルにも実例が無い新しい判断をガイドに加えた場合、ルール文末に「（新規決定）」と明記している。

---

## AI エージェント向けの実行例

- PR を作成する直前に、このチェックリストを自分の変更内容に対して1項目ずつ自己適用する。該当しない項目は理由とともに読み飛ばしてよい（例: 「UI 層の変更なしのため UI 層セクションは対象外」）。
- チェックが付かない項目が見つかった場合は、PR を作成する前に実装を修正するか、修正しない場合はその理由を PR 本文に明記する。
- PR 本文の `## Verification` に、実行した検証コマンド（例: `./gradlew testAndroidHostTest`）とその結果、および本チェックリストで確認した主要項目を箇条書きで記載する。
- [remove-sample-code.md](./remove-sample-code.md) の手順を実行した PR では、検証セクションの `grep -rn -i zenn ...` の結果もあわせて記載する。
