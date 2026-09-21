# AGENTS.md

本リポジトリは Kotlin Multiplatform（KMP）+ Compose Multiplatform による Android/iOS 共通実装の**スターターテンプレート**です。本ファイルは AI コーディングエージェントの入口であり、`docs/`（設計・実装・判断基準）、`.claude/skills/`（手順）、各モジュール直下の `AGENTS.md`（実装ガイド）の 3 層で構成されます。`CLAUDE.md` は本ファイルを import しているだけです。

## 絶対ルール

1. `feature:<name>` は `domain:service` と `ui` にのみ依存し data 層に触れない。ViewModel に Repository を直接注入しない（→ [docs/design-guide.md](docs/design-guide.md) §3、[docs/decisions.md](docs/decisions.md) D-10）。
2. 実装モジュール同士を直接依存させない。契約モジュール（`data:repository`/`domain:service`）か `ui` を経由する（→ [docs/design-guide.md](docs/design-guide.md) §1・§3、[docs/decisions.md](docs/decisions.md) D-01）。
3. Repository interface は `data:repository` に置く（→ [docs/decisions.md](docs/decisions.md) D-02）。
4. Repository は例外透過、`Result` 化は `domain:service` の `ResultHandler` のみの責務（→ [docs/decisions.md](docs/decisions.md) D-03）。
5. domain モデルは `base` に置き、DTO とは常に別の型にする（→ [docs/decisions.md](docs/decisions.md) D-04）。
6. レスポンス DTO は全フィールド `nullable = null` にする（→ [docs/coding-guide.md](docs/coding-guide.md) §2）。
7. ViewModel のコンストラクタ注入は `domain:service` の interface のみ。画面遷移は `XxxScreenEventHandler` が `AppRouter` を直接呼ぶ（→ [docs/decisions.md](docs/decisions.md) D-16）。
8. テストダブルは Mokkery に統一する。手書き Fake クラスやテスト専用 Fake モジュールは作らない（→ [docs/decisions.md](docs/decisions.md) D-06）。
9. テストの必須範囲は Converter / Repository 実装 / Service 実装 / ViewModel（→ [docs/decisions.md](docs/decisions.md) D-17）。
10. コミットは Conventional Commits 形式（`<type>(<scope>): <Subject>`）、`main` に直接コミットしない（→ [docs/coding-guide.md](docs/coding-guide.md) §6）。

## 実行体制（オーケストレーション）

1. 監督者（オーケストレーター）は分解・委譲・整合性検証を行い、モジュール内の実装コードを自分で書かない。
2. 実装は対象モジュールごとに実装エージェントへ委譲する。実装エージェントには**必ず対象モジュールの `AGENTS.md` を最初に読ませる**。
3. 依存関係のないモジュール作業は並列に実行する。
4. モデル階層は下表のとおり。
5. 実装エージェントは自モジュール外を編集せず、必要な他モジュールの変更を「他モジュールとの接点」として監督者へ報告する。
6. 監督者は報告された接点の反映・Koin/Route/Gradle 依存の整合確認・テスト実行を行って完了とする。

| 役割 | 責務 | Claude | OpenAI |
|---|---|---|---|
| オーケストレーター | 分解・委譲・整合性検証・統合 | Fable | GPT-6 Astra または GPT-5.6 Sol |
| 実装エージェント | 1 モジュール内の実装とテスト | Sonnet | GPT-5.6 Luna |

- Claude Code は祖先ディレクトリの `AGENTS.md`/`CLAUDE.md` を連結して読むが、Codex 等は編集対象に最も近い `AGENTS.md` のみを読む。ルールその 2 はこの差を吸収するためのものである。
- skills の配置は `.claude/skills/` が正。`.agents/skills` はそこへのシンボリックリンク。

## 層マップ

```
composeApp/
  base/              domain モデル・共通エラー型（AGENTS.md あり）
  core/              Ktor ApiClient・Preferences 等のインフラ基盤（AGENTS.md あり）
  data/
    repository/      Repository interface の契約モジュール（AGENTS.md あり）
    zenn/            Zenn API のデータソース実装。data/<name> 系の雛形（サンプル。AGENTS.md あり）
  domain/
    service/         Service interface の契約モジュール（AGENTS.md あり）
    zenn/            Zenn 向け Service 実装。domain/<name> 系の雛形（サンプル。AGENTS.md あり）
  ui/                画面間で共有する UI コンポーネント・Route 型（AGENTS.md あり）
  feature/
    home/            ホーム画面（サンプル。プレースホルダ化対象。AGENTS.md あり）
    user/            ユーザー画面（サンプル。削除対象。AGENTS.md あり）
  app/               Koin 登録・NavHost・エントリポイント統合（AGENTS.md あり）
androidApp/          Android エントリポイント
iosApp/              iOS エントリポイント（Xcode プロジェクト）
docs/                design-guide / coding-guide / decisions（読み物）
.claude/skills/      手順（skills）
```

## タスク別の読み順

| タスク | 使う skill | 読む docs 節 | 対象モジュールの AGENTS.md |
|---|---|---|---|
| 画面を追加する | [add-feature-screen](.claude/skills/add-feature-screen/SKILL.md) | [design-guide.md](docs/design-guide.md) §3・§5、[coding-guide.md](docs/coding-guide.md) §4 | `feature/<name>`, `ui` |
| データソースを追加する | [add-data-source](.claude/skills/add-data-source/SKILL.md) | [design-guide.md](docs/design-guide.md) §2・§3・§4、[coding-guide.md](docs/coding-guide.md) §2・§3 | `data/<name>`, `data/repository`, `domain/<name>`, `domain/service` |
| 既存画面を修正する | なし | [coding-guide.md](docs/coding-guide.md) §4、[design-guide.md](docs/design-guide.md) §5 | `feature/<name>`, `ui` |
| テストを追加する | なし | [coding-guide.md](docs/coding-guide.md) §5 | 対象モジュール |
| エラー処理を追加する | なし | [design-guide.md](docs/design-guide.md) §6 | 発生源の `data/<name>` または `domain/<name>`、`ui`（ハンドラ）、`feature/<name>`（ViewModel の `.handle()`） |
| 依存ライブラリを更新する | なし | [decisions.md](docs/decisions.md) D-07 | 変更対象モジュール |
| サンプルコードを削除する | [remove-sample-code](.claude/skills/remove-sample-code/SKILL.md) | — | SKILL.md 参照（実質全モジュール） |
| 動作確認する | `.claude/skills/debug-run`（`debug-run-android`/`debug-run-ios`） | — | `app` |
| PR を出す | なし | [coding-guide.md](docs/coding-guide.md) §6 | — |

## 作業ルール

- 並行セッション対応: 同一ワークツリーで別セッション/別ブランチの未コミット変更がある状態でブランチ作業をするときは `git worktree` で隔離する。`git stash` は全セッションで共有されるため使わない。
- コミットは Conventional Commits 形式、ブランチ名は `claude/<kebab-case>`。
- PR 本文は `## Summary` / `## Verification` / `## Intentionally left as-is` の構成にする。
- 削除は `git rm`/`rm` の前に対象一覧を提示し、明示的な確認を得てから実行する。
- 規約を変更する場合は、同じ PR で該当する docs（[docs/design-guide.md](docs/design-guide.md)、[docs/coding-guide.md](docs/coding-guide.md)、[docs/decisions.md](docs/decisions.md)）とモジュール `AGENTS.md` を更新する。
- 新規コードに [docs/coding-guide.md](docs/coding-guide.md) §8 の既知の逸脱を複製しない（PR 前チェックで確認）。
- 詳細は [docs/coding-guide.md](docs/coding-guide.md) §6 を参照。
