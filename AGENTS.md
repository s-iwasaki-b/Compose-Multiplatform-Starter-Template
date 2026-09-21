# AGENTS.md

AI コーディングエージェント（Claude Code など）向けの入口ドキュメントです。`CLAUDE.md` はこのファイルを import しているだけなので、Claude Code から読み込まれるのは実質このファイルです。

## プロジェクト概要

本リポジトリは Kotlin Multiplatform（KMP）+ Compose Multiplatform による Android/iOS 共通実装の**スターターテンプレート**です。`composeApp/` 配下を層（`base`/`core`/`data`/`domain`/`feature`/`ui`/`app`）ごとのモジュールに分割し、`androidApp`/`iosApp` がプラットフォーム側のエントリポイントになります。モジュール構成の詳細は [docs/architecture/overview.md](docs/architecture/overview.md) と [docs/architecture/module-guide.md](docs/architecture/module-guide.md) を参照してください。同梱の Zenn 記事ビューワー（`data:zenn`, `domain:zenn`, `feature:home`, `feature:user` など）は動作サンプルです。テンプレート利用開始時に、`feature:home` はプレースホルダ画面に書き換え、`data:zenn`/`domain:zenn`/`feature:user` など他のサンプルモジュールは削除します（[docs/playbooks/remove-sample-code.md](docs/playbooks/remove-sample-code.md)）。

## 最初に読む順序

1. このファイル（`AGENTS.md`）— 絶対ルールと全体像
2. [docs/architecture/overview.md](docs/architecture/overview.md) — レイヤー構成とモジュール依存許可表
3. 着手するタスクに該当する `docs/guides/` のガイド（下記「タスク別の参照先」を参照）
4. 着手するタスクに該当する `docs/playbooks/` の手順書（存在する場合）
5. [docs/playbooks/pr-checklist.md](docs/playbooks/pr-checklist.md) — PR を出す前のセルフレビュー

ドキュメント全体の索引・規範レベルの定義・ルール ID 体系は [docs/README.md](docs/README.md) にまとまっています。

## 絶対ルール10か条

1. **`feature:<name>` は `domain:service` と `ui` にのみ依存し、data 層には一切触れない。** ViewModel に Repository を直接注入しない（[ADR-0010](docs/decisions/0010-feature-depends-on-service-only.md)）。
2. **実装モジュール同士を直接依存させない。** `data:<source>` と `domain:<name>`、`feature:<a>` と `feature:<b>` のような組み合わせは契約モジュール（`data:repository`/`domain:service`）か `ui` を経由する（[docs/architecture/overview.md](docs/architecture/overview.md) ARCH-1）。
3. **Repository interface は `data:repository` に置く。** domain 側が interface を所有する古典的 DIP は採用しない（[ADR-0002](docs/decisions/0002-repository-interface-in-data-layer.md)）。
4. **Repository は例外透過、`Result` 化は `domain:service` の `ResultHandler` だけの責務。** Repository/Converter で `Result` に包まない（例外: 一覧変換で1件ずつの変換エラーを許容する `try/catch`（[data-layer.md](docs/guides/data-layer.md) DATA-13）は対象外）（[ADR-0003](docs/decisions/0003-result-at-service-boundary.md)）。
5. **domain モデルは `base` に置き、DTO とは常に別の型にする。** DTO を UI やビジネスロジックにそのまま渡さない（[ADR-0004](docs/decisions/0004-domain-models-in-base.md)）。
6. **レスポンス DTO は全フィールド `nullable = null`。** 省略しない（[docs/guides/data-layer.md](docs/guides/data-layer.md) DATA-9）。
7. **ViewModel のコンストラクタ注入は `domain:service` の interface のみ。** 画面遷移は `XxxScreenEventHandler` が `AppRouter` を直接呼び、ViewModel は Navigation を知らない（[ADR-0016](docs/decisions/0016-one-shot-events.md)）。
8. **テストダブルは Mokkery に統一する。** 手書き Fake クラスやテスト専用の共有 Fake モジュールは作らない（[ADR-0006](docs/decisions/0006-mokkery-for-test-doubles.md)）。
9. **テストの必須範囲は Converter / Repository 実装 / Service 実装 / ViewModel。** 変更した層に対応するテストを書く（[ADR-0017](docs/decisions/0017-test-scope.md)）。
10. **コミットは Conventional Commits 形式（`<type>(<scope>): <Subject>`、Subject は英語・命令形）。** `main` に直接コミットしない（[docs/guides/git-workflow.md](docs/guides/git-workflow.md)）。

## タスク別の参照先

| タスク | 読む順序 |
|---|---|
| 新しい画面を追加する | [docs/architecture/overview.md](docs/architecture/overview.md) → [docs/guides/ui-layer.md](docs/guides/ui-layer.md) → [docs/architecture/navigation.md](docs/architecture/navigation.md) → [docs/architecture/dependency-injection.md](docs/architecture/dependency-injection.md) → [docs/playbooks/add-feature-screen.md](docs/playbooks/add-feature-screen.md) |
| 新しい API・データソースを追加する | [docs/architecture/overview.md](docs/architecture/overview.md) → [docs/guides/data-layer.md](docs/guides/data-layer.md) → [docs/guides/domain-layer.md](docs/guides/domain-layer.md) → [docs/architecture/error-handling.md](docs/architecture/error-handling.md) → [docs/playbooks/add-data-source.md](docs/playbooks/add-data-source.md) |
| 既存画面を修正する | [docs/guides/ui-layer.md](docs/guides/ui-layer.md) → [docs/architecture/navigation.md](docs/architecture/navigation.md) → [docs/guides/coding-style.md](docs/guides/coding-style.md) |
| テストを書く | [docs/guides/testing.md](docs/guides/testing.md) → 対象層のガイド（[data-layer.md](docs/guides/data-layer.md) / [domain-layer.md](docs/guides/domain-layer.md) / [ui-layer.md](docs/guides/ui-layer.md)）の「参考実装」節 |
| エラー処理を追加する | [docs/architecture/error-handling.md](docs/architecture/error-handling.md) → 発生源の層のガイド（[data-layer.md](docs/guides/data-layer.md) または [domain-layer.md](docs/guides/domain-layer.md)） |
| 依存ライブラリを更新する | [docs/decisions/0007-stable-dependencies.md](docs/decisions/0007-stable-dependencies.md) → [docs/architecture/module-guide.md](docs/architecture/module-guide.md) → [docs/guides/git-workflow.md](docs/guides/git-workflow.md) |
| サンプルコードを削除してプロジェクトを始める | [docs/playbooks/remove-sample-code.md](docs/playbooks/remove-sample-code.md) → [README.md](README.md)（`## How to Rename`）→ [docs/architecture/overview.md](docs/architecture/overview.md) |
| PR を出す | [docs/guides/git-workflow.md](docs/guides/git-workflow.md) → [docs/playbooks/pr-checklist.md](docs/playbooks/pr-checklist.md) |

すべてのタスクの一覧と規範レベル・ルール ID 体系は [docs/README.md](docs/README.md) を参照してください。

## 作業ルール

- **並行セッション対応**: 同一ワークツリーで別セッション/別ブランチの未コミット変更がある状態でブランチ作業をするときは `git worktree` で作業を隔離する。`git stash` は全セッションで共有されるため使わない（[docs/guides/git-workflow.md](docs/guides/git-workflow.md)）。
- **ビルド・実機確認**: Gradle コマンド（テスト実行など）は [README.md](README.md) を正とする。シミュレーター/エミュレーターへのビルド・インストールは `.claude/skills/debug-run`（および `debug-run-android`/`debug-run-ios`）を使う。
- **シークレットをコミットしない**: API キー・パスワード・トークン・署名鍵（`.jks` 等）はコミットしない。埋め込みが避けられない場合も `.gitignore` 対象にすることが必須。
- **削除は対象一覧を提示してから**: `git rm` や `rm` でファイルを削除する前に、削除対象の一覧を提示し明示的な確認を得る（[docs/playbooks/remove-sample-code.md](docs/playbooks/remove-sample-code.md)）。
- **サンプルコードの既知の逸脱を真似しない**: `feature/home`/`feature/user` の一部実装は、分析時点で見つかった既知の逸脱を含みます。一覧は [docs/README.md](docs/README.md) の「既知の逸脱一覧」を参照し、新規実装ではガイド側の規約に従う。

## ドキュメント更新義務

コードとドキュメントの記述に矛盾を見つけたら、実装を直すかドキュメントを直すかを判断したうえで、ドキュメントを修正する PR を出してください。コードの規約を変更する場合は、同じ PR で該当ガイド・ADR を更新します（詳細は [docs/README.md](docs/README.md) の「ドキュメント更新ルール」）。
