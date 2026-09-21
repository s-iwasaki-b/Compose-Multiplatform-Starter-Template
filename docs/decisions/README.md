# ADR 索引

> 対象: 本リポジトリの Architecture Decision Record（ADR）一覧と、新規 ADR の起票手順
> 関連: [../README.md](../README.md)
> 最終確認コミット: ac56102

## ADR の目的

このリポジトリはスターターテンプレートであり、サンプル実装（Zenn 記事ビューワー: `data:zenn`, `domain:zenn`, `feature:home`, `feature:user` 等）はテンプレート利用時に削除される前提である。ADR は **サンプルコードが存在しなくても、AI コーディングエージェントや人間の開発者が同じ設計判断を再現できる** ようにするための決定記録である。「サンプルが暗黙に決めていた規約」と「サンプルにも実例がなく本ガイドで新たに決めた規約」を区別し、後者は各 ADR 内で「新規決定」と明記している。

新しい設計判断を行う際は、まずこの索引で既存の ADR を確認し、既存の決定と矛盾しないかを確認すること。矛盾する場合は既存 ADR を「廃止（→ ADR-XXXX に置換）」にした上で新しい ADR を起票する。

## 新規 ADR の起票手順

1. `template.md` をコピーし、`decisions/NNNN-kebab-title.md`（`NNNN` は次の連番、4 桁ゼロ埋め）として保存する。
2. `Context` / `Decision` / `判断基準` / `Consequences` / `Alternatives` / `サンプル削除後の扱い` の全セクションを埋める。規範レベル（**必須**/**推奨**/**任意**）を明示する。
3. 本ファイルの表に 1 行追加する（番号・タイトルへのリンク・ステータス・実例の有無・一言要約）。
4. 変更を PR で提案し、レビューを経てから `main` にマージする（ADR 自体も通常の PR フローに従う。緊急の一方的な確定は行わない）。

## 規範レベル

| 用語 | 意味 |
|---|---|
| **必須** | 違反はレビューで差し戻される |
| **推奨** | 原則従う。逸脱する場合は理由を PR に書く |
| **任意** | 従うかどうかは実装者の判断に委ねる |

## ADR 一覧

| 番号 | タイトル | ステータス | 実例 | 一言要約 |
|---|---|---|---|---|
| 0001 | [契約モジュール（interface のみ）と実装モジュールを分離する](0001-contract-and-impl-modules.md) | 採用 | あり | interface と実装を別モジュールに置きテスト容易性とモジュール境界を確保する |
| 0002 | [Repository interface は `data:repository` に置く（古典的 DIP は採用しない）](0002-repository-interface-in-data-layer.md) | 採用 | あり | Repository interface を domain ではなく data 層に置く非標準構成を明文化する |
| 0003 | [Repository は例外透過、Service が `ResultHandler` で `Result` 化する](0003-result-at-service-boundary.md) | 採用 | あり | 例外→`Result` 変換の責務を Service 層に一元化する |
| 0004 | [domain モデルは `base` に置き、DTO と常に分離する](0004-domain-models-in-base.md) | 採用 | あり | 共有カーネル `base` に domain モデルを置き DTO と常に別型にする |
| 0005 | [Repository はカーソル付き単発取得、Paging3 の組み立ては UI 層](0005-paging-in-ui-layer.md) | 採用 | あり | ページングの構築責務を UI 層に閉じ込める |
| 0006 | [テストダブルは Mokkery（モックライブラリ）で統一する。手書き Fake は併用しない](0006-mokkery-for-test-doubles.md) | 採用 | あり | モックライブラリで統一し手書き Fake と混在させない |
| 0007 | [依存ライブラリは安定版優先。Alpha/Beta は評価記録のみ](0007-stable-dependencies.md) | 採用 | 運用実績 | 依存ライブラリのバージョン更新方針 |
| 0008 | [Koin の DI 定義は `composeApp:app` の `Koin.kt` に層別で集約する](0008-di-centralized-in-app.md) | 採用 | あり | DI 定義を 1 ファイルに層別集約する |
| 0009 | [feature は機能ドメイン（ユーザーフロー）単位、data/domain は外部リソース単位](0009-module-granularity.md) | 採用 | あり（非対称） | モジュール分割の粒度基準を層ごとに使い分ける |
| 0010 | [feature は `domain:service` と `ui` にのみ依存し data 層に触れない（Gradle で強制）](0010-feature-depends-on-service-only.md) | 採用 | あり | モジュール境界を Gradle 依存で物理的に強制する |
| 0011 | [Service は外部リソース単位の interface に複数メソッド（1 クラス 1 メソッドの UseCase は採用しない）](0011-service-per-resource.md) | 採用 | あり | Service の粒度をリソース単位に統一し UseCase 分割を避ける |
| 0012 | [Repository/Service が `Flow` を返すのは継続監視が要件のときのみ](0012-flow-only-for-observation.md) | 採用 | 実例なし（新規決定） | 継続監視が要件の時のみ `Flow` を使い、それ以外は単発取得にする |
| 0013 | [キャッシュ・オフライン・認証・リトライは必要になるまで実装しない](0013-defer-cache-auth-retry.md) | 採用 | 実例なし（新規決定） | 必要になるまで実装せず、必要時は `core`/`data:<source>` に置く |
| 0014 | [入力バリデーションは Service 層、レスポンス整合性検証は Converter](0014-validation-placement.md) | 採用 | 実例なし（新規決定） | 入力バリデーションと DTO 整合性検証の責務を分離する |
| 0015 | [UiState は画面ごとに 1 つの `@Immutable data class`、先頭に共通 `ScreenState`](0015-uistate-structure.md) | 採用 | あり | UiState の構造と共通 `ScreenState` 合成パターンを統一する |
| 0016 | [画面遷移は EventHandler が `AppRouter` を直接呼ぶ。Snackbar は `ScreenState.snackBarState` で表現する](0016-one-shot-events.md) | 採用 | あり（Snackbar は未配線） | 一回性イベント（遷移/通知）の実装方式を統一する |
| 0017 | [テスト必須範囲は Converter/Repository impl/Service impl/ViewModel、テストダブルは Mokkery で統一する](0017-test-scope.md) | 採用 | 一部あり | テストの必須範囲とテストダブル方針（Mokkery 維持）を明文化する |
| 0018 | [Dispatcher 切替は `ResultHandler` の 1 箇所のみ。Dispatcher はコンストラクタ注入](0018-dispatcher-switch-in-resulthandler.md) | 採用 | あり | Dispatcher 切替を `ResultHandler` に集約しテスト容易性を確保する |
| 0019 | [エラー表示 4 経路（全画面/Paging 末尾/Snackbar/入力フィールド）の使い分け](0019-error-display-routes.md) | 採用 | 一部あり | 用途別のエラー表示経路の選択基準を明文化する |
| 0020 | [Material 2 を維持し、テーマ値はプレースホルダとして利用者が差し替える](0020-material2-and-theme-placeholders.md) | 採用 | あり | Material 2 を維持しテーマ値は差し替え前提のプレースホルダとする |
| 0021 | [表示文言は Compose Resources に必ず切り出し、ベース言語はプロジェクトで 1 つ（本テンプレートは日本語）](0021-string-resources-base-language.md) | 採用 | あり（不統一） | 文言のリソース化を必須化しベース言語を日本語に統一する |
| 0022 | [共通コンポーネントの置き場所は「ドメイン依存の有無」と「再利用範囲」で判定する](0022-shared-component-placement.md) | 採用 | あり | 共通コンポーネントの置き場所判定基準を明文化する |
| 0023 | [画面間・アプリ全体で共有する状態は `domain:service` の状態保持 Service に置く](0023-shared-state-in-service.md) | 採用 | 実例なし（新規決定） | 共有状態（カート・ログイン・テーマ設定等）を状態保持 Service に集約する |
| 0024 | [UI から呼ぶプラットフォーム固有機能は interface を `ui` に置き、`app` の `platformModule` で実装を注入する](0024-platform-capabilities-via-app-module.md) | 採用 | 実例なし（新規決定） | プラットフォーム固有機能の配線パターンを `AppRouter` と同じ形に統一する |
| 0025 | [別ホストの API は `ApiClientImpl` を baseUrl 引数化し Koin `named("<host>")` で複数登録する](0025-multi-host-api-clients.md) | 採用 | 実例なし（新規決定） | 複数ホストの API クライアントを Koin の named qualifier で区別する |

## どのタスクでどの ADR を読むか

| タスク | 読む ADR |
|---|---|
| 新しい画面を追加する | 0009, 0010, 0015, 0016, 0019, 0021, 0022, 0023 |
| 新しい API/外部リソースを追加する | 0001〜0005, 0011〜0014, 0025 |
| テストを追加する | 0006, 0017, 0018 |
| 依存ライブラリを更新する / 新規モジュールを追加する | 0007, 0008, 0009 |
| エラーハンドリングを変更・拡張する | 0003, 0013, 0014, 0018, 0019 |
| DI（Koin）の登録を追加・変更する | 0008, 0010, 0024, 0025 |
| テーマ・デザインシステムを変更する | 0020, 0022, 0023 |
| 文言・リソースを追加する | 0021 |
| 複数画面・アプリ全体で共有する状態を扱う | 0012, 0023 |
| UI からプラットフォーム固有機能を呼ぶ | 0024 |
| Zenn サンプルを削除して骨格だけ残す | 各 ADR の「サンプル削除後の扱い」セクションすべて |
