# ドキュメント索引

> 対象: 本リポジトリの設計ガイド・コーディングガイド全体の入口。目的・規範レベル・ルール ID 体系・タスク別の参照先をまとめる。
> 関連: [../AGENTS.md](../AGENTS.md), [../README.md](../README.md), [decisions/README.md](decisions/README.md)
> 最終確認コミット: ac56102

## 目的

このリポジトリは Compose Multiplatform の**スターターテンプレート**であり、同梱されている Zenn 記事ビューワーのサンプル実装（`data:zenn`, `domain:zenn`, `feature:home`, `feature:user` など）はテンプレート利用開始時に置き換えられる前提です。`feature:home` はプレースホルダ画面に書き換え、`data:zenn`/`domain:zenn`/`feature:user` など他のサンプルモジュールは削除します（[playbooks/remove-sample-code.md](playbooks/remove-sample-code.md)）。

`docs/` 配下のガイド群は、**AI コーディングエージェント（Claude Code など）と人間の開発者が、サンプルコードが削除された後も同じ設計判断を再現できる**ことを最重要要件として書かれています。ルール本文はサンプルの実コードへの参照に依存せず、プレースホルダ名（`Xxx`, `<feature>`, `<source>`）を使ったコードテンプレートを本文中に埋め込んであります。サンプルの実ファイルへの参照は各ドキュメントの「参考実装」節に「サンプル。削除後は存在しない」と明示してまとめてあります。

本文は日本語、識別子・ファイルパス・コマンド・コード・Git のコミット/PR は英語で書きます。各ドキュメントは 2026-09-21 にリポジトリのサンプル実装を分析して作成しました。「サンプルが暗黙に決めていた規約（実例あり）」と「サンプルにも実例が無く本ガイドで新たに決めた規約」を区別しており、後者はルール文末に **（新規決定）** と付記しています。分析の出自の詳細は本ページ末尾の「分析の出自」を参照してください。

## ディレクトリ構成

```text
docs/
  README.md            # このファイル（索引）
  architecture/         # アーキテクチャ全体（5ファイル）
  guides/                # 層別コーディング規約（6ファイル）
  playbooks/             # 手順書（4ファイル）
  decisions/             # ADR（索引・テンプレート・25決定）
```

各ディレクトリの内訳は下記「ドキュメント地図」を参照してください。

## 規範レベル

各ルールは次の3段階の規範レベルで書かれています。

| レベル | 意味 |
|---|---|
| **必須** | 違反はレビューで差し戻される |
| **推奨** | 原則従う。逸脱する場合は理由を PR に書く |
| **任意** | 従うかどうかは実装者の判断に委ねる |

## ルール ID 体系

各ルールには `<接頭辞>-<連番>` の参照用 ID が付いています。接頭辞はドキュメントごとに固定です。

| 接頭辞 | 定義ファイル | 対象領域 |
|---|---|---|
| `ARCH` | [architecture/overview.md](architecture/overview.md) | レイヤー構成・モジュール依存許可 |
| `MOD` | [architecture/module-guide.md](architecture/module-guide.md) | モジュール責務・新規モジュール追加 |
| `DI` | [architecture/dependency-injection.md](architecture/dependency-injection.md) | Koin による依存性注入 |
| `NAV` | [architecture/navigation.md](architecture/navigation.md) | 画面遷移・ルーティング |
| `ERR` | [architecture/error-handling.md](architecture/error-handling.md) | エラー型・`Result` 化・表示経路 |
| `DATA` | [guides/data-layer.md](guides/data-layer.md) | Repository/DataSource/DTO/Converter |
| `DOM` | [guides/domain-layer.md](guides/domain-layer.md) | Service・domain モデル |
| `UI` | [guides/ui-layer.md](guides/ui-layer.md) | feature モジュール・状態管理・共通コンポーネント |
| `TEST` | [guides/testing.md](guides/testing.md) | テストダブル(Mokkery)・命名・実行 |
| `STYLE` | [guides/coding-style.md](guides/coding-style.md) | Kotlin コーディングスタイル |
| `GIT` | [guides/git-workflow.md](guides/git-workflow.md) | コミット・ブランチ・PR |

Architecture Decision Record は `ADR-NNNN`（4桁ゼロ埋め連番）で、[decisions/](decisions/) 配下に 1 決定 1 ファイルで存在します。索引は [decisions/README.md](decisions/README.md) を参照してください。

## ドキュメント地図

### architecture/（アーキテクチャ全体の設計）

| ファイル | 目的 | 行数 | いつ読むか |
|---|---|---|---|
| [overview.md](architecture/overview.md) | レイヤー構成・モジュール依存許可表・縦断データフロー | 220 | 初めてこのリポジトリを触るとき最初に読む |
| [module-guide.md](architecture/module-guide.md) | モジュール責務カタログ・新規モジュール追加手順・convention plugin 選択基準 | 230 | 新規モジュールを作る・依存関係で迷ったとき |
| [dependency-injection.md](architecture/dependency-injection.md) | Koin の層別モジュール・登録パターン・新規バインド追加手順 | 251 | DI 登録を追加・変更するとき |
| [navigation.md](architecture/navigation.md) | `AppRoute`/`AppRouter`・NavHost 登録手順・ディープリンク | 275 | 画面遷移を追加・変更するとき |
| [error-handling.md](architecture/error-handling.md) | エラー型・`Result` 化・エラー表示4経路の使い分け | 256 | エラー処理を追加・変更するとき |

### guides/（層別のコーディング規約）

| ファイル | 目的 | 行数 | いつ読むか |
|---|---|---|---|
| [data-layer.md](guides/data-layer.md) | Repository/DataSource/DTO/Converter の設計判断とコーディング規約 | 439 | data 層を実装するとき |
| [domain-layer.md](guides/domain-layer.md) | Service の責務・粒度・`Result` 化、domain モデルの置き場所 | 300 | domain 層を実装するとき |
| [ui-layer.md](guides/ui-layer.md) | feature モジュール構成・状態管理・Event・共通コンポーネント | 576 | UI（画面）を実装・修正するとき |
| [testing.md](guides/testing.md) | Mokkery の使い方・必須範囲・命名・実行コマンド | 376 | テストを書くとき |
| [coding-style.md](guides/coding-style.md) | 命名・可視性・型選択・ログ・`@OptIn`・設定値 | 254 | スタイルで迷ったとき、レビュー前 |
| [git-workflow.md](guides/git-workflow.md) | コミット・ブランチ・PR 本文・worktree 運用 | 196 | コミットや PR を作成するとき |

### playbooks/（手順書）

| ファイル | 目的 | 行数 | いつ読むか |
|---|---|---|---|
| [add-feature-screen.md](playbooks/add-feature-screen.md) | 新しい画面（feature モジュール）を1つ追加する手順 | 482 | 新しい画面を追加するとき |
| [add-data-source.md](playbooks/add-data-source.md) | 新しい API/データソースを追加する手順 | 506 | 新しい外部リソースに接続するとき |
| [remove-sample-code.md](playbooks/remove-sample-code.md) | Zenn サンプルを安全に削除し骨格だけ残す手順 | 405 | テンプレートで新規プロジェクトを始めるとき |
| [pr-checklist.md](playbooks/pr-checklist.md) | PR 前セルフレビュー用チェックリスト | 102 | PR を出す直前 |

### decisions/（ADR: 設計判断の記録）

| ファイル | 目的 | 行数 | いつ読むか |
|---|---|---|---|
| [decisions/README.md](decisions/README.md) | ADR 索引（25件）・新規 ADR の起票手順・タスク別「どの ADR を読むか」 | 72 | 特定の設計判断の理由・代替案の却下理由を確認したいとき |
| [decisions/template.md](decisions/template.md) | 新規 ADR 起票用テンプレート | 41 | 新しい設計判断を ADR として記録するとき |
| `decisions/0001-...md` 〜 `0025-...md` | 各設計判断（Context/Decision/判断基準/Consequences/Alternatives） | 各 40〜90 | 個別の ADR 番号を [decisions/README.md](decisions/README.md) の一覧から辿って読む |

## タスク別: 何を読むか

各行は読む順序でファイルを列挙しています。

| タスク | 読む順序 |
|---|---|
| 新しい画面を追加する | [architecture/overview.md](architecture/overview.md) → [guides/ui-layer.md](guides/ui-layer.md) → [architecture/navigation.md](architecture/navigation.md) → [architecture/dependency-injection.md](architecture/dependency-injection.md) → [playbooks/add-feature-screen.md](playbooks/add-feature-screen.md) |
| 新しい API・データソースを追加する | [architecture/overview.md](architecture/overview.md) → [guides/data-layer.md](guides/data-layer.md) → [guides/domain-layer.md](guides/domain-layer.md) → [architecture/error-handling.md](architecture/error-handling.md) → [playbooks/add-data-source.md](playbooks/add-data-source.md) |
| 既存画面を修正する | [guides/ui-layer.md](guides/ui-layer.md) → [architecture/navigation.md](architecture/navigation.md) → [guides/coding-style.md](guides/coding-style.md) |
| テストを書く | [guides/testing.md](guides/testing.md) → 対象層のガイド（[data-layer.md](guides/data-layer.md) / [domain-layer.md](guides/domain-layer.md) / [ui-layer.md](guides/ui-layer.md)）の「参考実装」節 |
| エラー処理を追加する | [architecture/error-handling.md](architecture/error-handling.md) → 発生源の層のガイド（[data-layer.md](guides/data-layer.md) または [domain-layer.md](guides/domain-layer.md)） |
| 依存ライブラリを更新する | [decisions/0007-stable-dependencies.md](decisions/0007-stable-dependencies.md) → [architecture/module-guide.md](architecture/module-guide.md) → [guides/git-workflow.md](guides/git-workflow.md) |
| サンプルコードを削除してプロジェクトを始める | [playbooks/remove-sample-code.md](playbooks/remove-sample-code.md) → [../README.md](../README.md)（`## How to Rename`）→ [architecture/overview.md](architecture/overview.md) |
| PR を出す | [guides/git-workflow.md](guides/git-workflow.md) → [playbooks/pr-checklist.md](playbooks/pr-checklist.md) |

## 推奨読書順（初見のエージェント向け）

このリポジトリを初めて触る AI エージェントは、次の5ステップで読み進めてください。

1. [../AGENTS.md](../AGENTS.md) — エージェント向けの絶対ルールと全体像
2. [architecture/overview.md](architecture/overview.md) — レイヤー構成とモジュール依存許可表
3. 着手するタスクに該当する `guides/` の1〜2本（上記「タスク別: 何を読むか」表を参照）
4. 着手するタスクに該当する `playbooks/` の手順書（存在する場合）
5. [playbooks/pr-checklist.md](playbooks/pr-checklist.md) — PR を出す前のセルフレビュー

## 既知の逸脱一覧（サンプルコードのこれらの箇所は真似しない）

以下はサンプル実装（Zenn ビューワー）に実在する、ガイドの規約からの逸脱です。新規実装ではガイド側の規約に従ってください。表中のパスの多くは [playbooks/remove-sample-code.md](playbooks/remove-sample-code.md) の削除対象に含まれており、サンプル削除後はそのファイルごと消滅します（該当行も削除して構いません）。

| 箇所 | 逸脱内容 | ガイドの規約 |
|---|---|---|
| `feature/home`, `feature/user` の Screen | `collectAsState()` を使用している | `collectAsStateWithLifecycle()` を推奨（[ui-layer.md](guides/ui-layer.md) UI-14） |
| `feature/user` の `composeResources` | 表示文言が英語 | ベース言語は日本語に統一（[ui-layer.md](guides/ui-layer.md) UI-29） |
| 複数行引数リストの trailing comma | 実測29箇所で使用（不使用が多数派）。うち15箇所が `feature/user`、残りは `ui`/`app`/`base`/`data/zenn` に分散しており、特定モジュールへの集中ではない | trailing comma を付けない形を推奨（[coding-style.md](guides/coding-style.md) STYLE-4、判断基準に実測件数の根拠あり） |
| `composeApp/feature/user/src/commonMain/kotlin/org/starter/project/feature/user/component/UserProfile.kt` | `@Preview` 関数名が `UserProfileHeaderPreview` であり `<Component>Preview`（`UserProfilePreview` のはず）の命名規則から外れている | `<Component>Preview` 形式にする（[ui-layer.md](guides/ui-layer.md) UI-27/UI-38） |
| `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/handler/SnackBarThrowableHandler.kt` | どの ViewModel からも呼ばれておらず未配線。表示後に `snackBarState` をクリアする処理も無い | [ADR-0016](decisions/0016-one-shot-events.md) の配線を完成させてから使う（[error-handling.md](architecture/error-handling.md)） |
| `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/component/article/ArticlesPagingSource.kt` | `try/catch` が、既に `.handle()` 済みで例外を投げない fetcher に対しては実質到達しない | [ADR-0019](decisions/0019-error-display-routes.md) のエラー表示4経路の設計に従う（[error-handling.md](architecture/error-handling.md)） |
| `composeApp/domain/service/src/commonMain/kotlin/org/starter/project/domain/service/ResultHandler.kt` | 例外捕捉ログが `Napier.d` | 例外捕捉は `Napier.e` を使う（[coding-style.md](guides/coding-style.md) STYLE-18） |
| `composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/component/article/ArticleList.kt` | `LazyColumn` の `key` が `"..._$index"` という index 連結 | 記事 ID 等の安定した値を `key` に使う（[ui-layer.md](guides/ui-layer.md) UI-21） |
| `domain/service` モジュール | `commonTest` 依存と Mokkery プラグインは適用済みだが、テストファイルが0件 | `ResultHandler` のテスト追加を推奨（[testing.md](guides/testing.md) TEST-7） |
| `data/zenn` の `ZennRepositoryTest`/`ZennServiceTest` | `MockMode.autofill` を使う箇所と使わない箇所が混在している | 逸脱ではなく実例。使い分けの判断基準に従って選ぶ（[testing.md](guides/testing.md) 判断基準） |
| `base/build.gradle.kts` | UI を持たないモジュールなのに `kmp-compose-library` を適用している | 逸脱ではなく意図的な例外。domain モデルへの `@Immutable` 付与のために必要（[module-guide.md](architecture/module-guide.md) MOD-3） |

上記のうち「逸脱ではなく実例／意図的な例外」と明記した2行（`MockMode.autofill` の混在、`base` の convention plugin）は、規約違反ではなく判断基準・例外として各ガイドが明文化済みです。なお、テストダブルの方針について分析レポート内で検討された「Mokkery から手書き Fake パターンへの移行」は採用していません。**Mokkery を維持**しており、これは逸脱ではありません（[ADR-0006](decisions/0006-mokkery-for-test-doubles.md)）。

## ドキュメント更新ルール

- コードの規約を変更するときは、**同じ PR で** 該当するガイド（`architecture/`, `guides/`）と、必要なら ADR（`decisions/`）を更新する。コードとドキュメントが矛盾した状態でマージしない。
- 新しい設計判断（既存のどの ADR にも当てはまらない判断）が必要になったら、[decisions/template.md](decisions/template.md) をコピーして新規 ADR を起票する。起票手順は [decisions/README.md](decisions/README.md) の「新規 ADR の起票手順」を参照。
- 既存の決定を覆す場合は、古い ADR を「廃止（→ ADR-XXXX に置換）」にした上で新しい ADR を起票する。ADR 自体を書き換えて上書きしない。
- Zenn サンプルコードを削除した後も、`docs/` 配下のドキュメントは削除しない。ガイドは骨格（テンプレート部分）を使い続けるために必要であり、[playbooks/remove-sample-code.md](playbooks/remove-sample-code.md) も参考実装への参照を除いて有効なまま残る。
- コードとドキュメントの矛盾を見つけたら、実装を直すか、規約自体を見直すかを判断したうえで、ドキュメントを修正する PR を出す（AI エージェントもこの義務を負う。[../AGENTS.md](../AGENTS.md) 参照）。

## 分析の出自

本ガイド群は 2026-09-21 にリポジトリのサンプル実装（コミット `4123810` 時点、その後 PR #10 による Gradle 依存整理を反映）を分析して作成しました。サンプルコードが暗黙に決めていた規約（実例あり）と、実例が無く本ガイドで新たに決めた規約（本文中に **（新規決定）** と表記）の両方を含みます。各ドキュメントの「参考実装」節に、根拠とした実ファイルを「サンプル。削除後は存在しない」と明示して列挙しています。
