# Git ワークフローガイド

> 対象: コミットメッセージ・ブランチ命名・PR 本文・レビュー観点・並行作業（worktree）・依存更新コミットの運用ルール
> 関連: [playbooks/pr-checklist.md](../playbooks/pr-checklist.md), [decisions/0007-stable-dependencies.md](../decisions/0007-stable-dependencies.md), [guides/coding-style.md](./coding-style.md)
> 最終確認コミット: ac56102

## 要点

- コミットメッセージは Conventional Commits 形式 `<type>(<scope>): <Subject>`。Subject は英語・先頭大文字・命令形・末尾ピリオドなし。
- 本文（body）で変更の「なぜ」を英語で説明する。AI（Claude Code）が作成したコミットには `Co-Authored-By` フッターが自動で付く。
- ブランチは `claude/<kebab-case-summary>`（AI 作業）。`main` に直接コミットしない。
- PR タイトルはコミットと同じ形式、本文は `## Summary` / `## Verification` / 生成ツールフッターの構成にする（`## Intentionally left as-is` は該当する場合のみ追加）。
- lint・CI が存在しないため、PR レビュー（[pr-checklist.md](../playbooks/pr-checklist.md)）が規約遵守を確認する唯一の手段。
- 同一ワークツリーで別セッション/別ブランチの未コミット変更がある状態で新しい作業をするときは `git worktree` で隔離する。`git stash` は全セッションで共有されるため使わない。
- 依存ライブラリの更新は安定版を優先する（ADR-0007）。更新コミットは `build(deps): ...` のような実例の scope に合わせる。

## ルール

| ID | 規範 | ルール |
|---|---|---|
| GIT-1 | 必須 | コミットメッセージは `<type>(<scope>): <Subject>` 形式にする。type は下表から選ぶ。 |
| GIT-2 | 必須 | scope はモジュール名（`data`, `ios`, `android`, `ui` 等）または横断的な関心事（`deps`, `build`, `viewmodel`, `navigation`, `deeplink` 等）を1つ付ける。複数モジュールにまたがる横断的な変更は scope を省略してよい。 |
| GIT-3 | 必須 | Subject は英語・先頭大文字・命令形（"Add", "Fix", "Remove", "Migrate" 等）で書き、末尾にピリオドを付けない。 |
| GIT-4 | 必須 | 本文（body）で変更の「なぜ」を英語の箇条書きまたは段落で説明する。Subject だけで自明な軽微な変更（1行で完結する `chore` 等）は body を省略してよい。 |
| GIT-5 | 必須 | AI（Claude Code）が作成したコミットの末尾には `Co-Authored-By: Claude <モデル名> <noreply@anthropic.com>` フッターを付ける（Claude Code の既定動作。手動で削除しない）。 |
| GIT-6 | 必須 | `main` ブランチへ直接コミットしない。必ずブランチを切り、PR 経由でマージする。 |
| GIT-7 | AI 作業は必須 / 人間の作業は推奨（新規決定） | ブランチ名は `claude/<kebab-case-summary>` にする。人間の作業ブランチの命名は実例が無いため、同じ形式を推奨する。 |
| GIT-8 | 必須 | PR タイトルはコミットメッセージと同じ Conventional Commits 形式にする。 |
| GIT-9 | 必須 | PR 本文は `## Summary`（箇条書き）で構成する。実施したテスト・動作確認は `## Verification` にフリーテキストまたは `- [ ]`/`- [x]` のチェックボックス形式で書く。意図的に対応しなかった箇所がある場合は `## Intentionally left as-is` にその旨と理由を書く（任意、該当する場合のみ）。 |
| GIT-10 | 必須 | PR 本文の末尾に `🤖 Generated with [Claude Code](https://claude.com/claude-code)` フッターを付ける。 |
| GIT-11 | 推奨 | lint・CI が無いため、PR 作成前後に [pr-checklist.md](../playbooks/pr-checklist.md) の項目で各ガイドの規約遵守を確認する。 |
| GIT-12 | 推奨（新規決定） | 同一ワークツリーに別セッション/別ブランチの未コミット変更がある状態で新しい作業を始める場合は `git worktree add -b <branch> .claude/worktrees/<name> main` でブランチ作業を隔離する。`git stash` は使わない（判断基準を参照）。 |
| GIT-13 | 必須 | 依存ライブラリの更新は安定版を優先する（ADR-0007）。Alpha/Beta 版への更新は評価記録にとどめ、通常の更新は行わない。更新コミットは `build(deps): ...` / `chore(deps): ...` のような scope=`deps`/`gradle` の実例形式に合わせる。 |

### type 一覧

| type | 用途 | 実例（本リポジトリのコミット） |
|---|---|---|
| `feat` | 新機能の追加 | `feat(home): Add pull-to-refresh to HomeScreen article list` |
| `fix` | バグ修正 | `fix(data): Default nullable Zenn response fields to null` |
| `refactor` | 挙動を変えないコードの再構成 | `refactor(ios): Build ComposeApp.framework from composeApp:app` |
| `chore` | ビルド成果物に影響しない雑務（CI/エージェント設定等） | `chore(claude): Add debug-run skill definitions with project-specific config` |
| `docs` | ドキュメントのみの変更 | `docs: Keep README to current repository facts` |
| `build` | ビルド設定・依存関係の変更 | `build(ios): Exclude x86_64 for simulator builds` |
| `style` | 挙動を変えないフォーマット・整形 | `style(deps): Group compose library entries together in version catalog` |
| `test` | テストの追加・変更のみ | 実例はこのワークツリーの履歴には確認できない。テスト自体の書き方は [testing.md](./testing.md) を参照 |
| `perf` | パフォーマンス改善のみ | 本リポジトリでの実例なし。使う場合も本表の他 type との使い分け基準（判断基準）に従う |

古い PR（#2, #4）は `## Test plan` セクションを使っていたが、直近の PR（#8, #10）は `## Verification` を使っている。プロジェクトの慣習は `Test plan` → `Verification` へ移行しており、新規 PR は `## Verification` を使う（GIT-9）。

## 判断基準

### 1. type の選び方

| 変更の性質 | 選ぶ type |
|---|---|
| ユーザーから見て新しい振る舞いを追加する | `feat` |
| 既存の振る舞いの誤りを直す | `fix` |
| 外部から見た振る舞いを変えずにコードを整理する | `refactor` |
| ビルド設定・依存バージョンを変更する | `build` |
| CI 設定・エージェント設定・雑務的な変更（上記いずれにも当てはまらない） | `chore` |
| ドキュメントのみを変更する | `docs` |
| 挙動を変えないフォーマット・整形のみ | `style` |
| テストコードのみを追加・変更する | `test` |
| 計測を伴うパフォーマンス改善のみ | `perf` |

1つのコミット/PR が複数の性質にまたがる場合は、最も主目的に近い type を1つ選ぶ（例: バグ修正にテスト追加が伴う場合は `fix` のままにし、body でテスト追加にも触れる）。

### 2. scope を付けるか省略するか

| 状況 | 判断 |
|---|---|
| 変更が単一モジュール/単一機能に閉じている | そのモジュール名（`data`, `ios`, `android`）または機能名（`home`, `user`, `article`）を scope にする |
| 変更が特定の関心事に紐づく（依存更新・ビルド設定・特定の横断機能） | 関心事名（`deps`, `build`, `gradle`, `viewmodel`, `navigation`, `deeplink`）を scope にする |
| 複数モジュールにまたがる横断的な変更で、単一の scope に絞れない | scope を省略する（実例: `docs: Keep README to current repository facts`） |

### 3. worktree を使うかどうか

| 状況 | 判断 |
|---|---|
| `git status` で自分が着手していない未コミット変更（他セッション由来）が確認できる | `git worktree add -b <branch> .claude/worktrees/<name> main` で新しいブランチ作業を隔離する（GIT-12） |
| 単一セッションで直列に作業しており、他の未コミット変更が無い | 通常どおり `main` のワークツリーでブランチを切って作業してよい |
| 他セッションの変更を一時的に退避したい | `git stash` は使わない。`git stash` は同一リポジトリの全セッション/全ワークツリーで共有されるスタックであり、他セッションが気づかないまま変更を退避・消失させる事故につながる |

## 実装パターン

### コミット

```bash
git add <対象ファイル>
git commit -m "$(cat <<'EOF'
<type>(<scope>): <Subject>

<なぜこの変更が必要かを英語の箇条書き・段落で>

Co-Authored-By: Claude <model-name> <noreply@anthropic.com>
EOF
)"
```

### ブランチ作成

```bash
git checkout -b claude/<kebab-case-summary> main
```

### 並行作業時の worktree 隔離（GIT-12）

```bash
git worktree add -b claude/<kebab-case-summary> .claude/worktrees/<name> main
```

### PR 作成

```bash
gh pr create --title "<type>(<scope>): <Subject>" --body "$(cat <<'EOF'
## Summary
- <変更点1>
- <変更点2>

## Verification
- <実行した検証コマンドと結果>
- <確認できなかった場合はその理由>

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

### PR 本文テンプレート

```markdown
## Summary
- <変更点を1つずつ箇条書きで>

## Intentionally left as-is（該当する場合のみ）
- <意図的に統一・修正しなかった箇所とその理由>

## Verification
- <実行した検証コマンドと結果、またはチェックボックス形式（`- [ ]`/`- [x]`）>
- <未検証の場合はその理由>

🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

## アンチパターン

1. **`main` ブランチに直接コミットする**
   なぜダメか: レビューなしに変更が入り、規約違反をチェックする機会が失われる。CI が無いため PR レビューが唯一の防波堤になる（GIT-6, GIT-11）。
   正しい形: 必ず `claude/<summary>` ブランチを切り、PR を作成してからマージする。

2. **コミット Subject を小文字始まりや過去形（"added X"）、末尾ピリオド付きで書く**
   なぜダメか: 実例（`fix(data): Default nullable Zenn response fields to null`）と不整合になり、`git log --oneline` の一覧性・機械可読性が崩れる（GIT-3）。
   正しい形: 先頭大文字の命令形、末尾ピリオドなしで書く。

3. **複数の無関係な変更を1コミット/1PR に混ぜ、type を `chore` で誤魔化す**
   なぜダメか: type 選択基準（判断基準1）を無視することになり、レビューで変更意図が追えなくなる。
   正しい形: 目的ごとにコミット・PR を分割する。

4. **他セッションの未コミット変更が残ったまま同じワークツリーでブランチを切り替える**
   なぜダメか: 他セッションの未コミット変更を巻き込んだり、意図せず上書きするリスクがある。
   正しい形: `git worktree` で作業ブランチを隔離する（GIT-12）。

5. **他セッションの変更を `git stash` で一時退避する**
   なぜダメか: `git stash` は同一リポジトリの全セッション・全ワークツリーで共有されるスタックであり、他セッションが気づかないまま変更が消えたように見える事故につながる。
   正しい形: `git stash` ではなく `git worktree` で隔離する。

6. **PR 本文に `## Summary` も生成ツールフッターも書かず、タイトルだけで PR を作る**
   なぜダメか: レビュー観点（[pr-checklist.md](../playbooks/pr-checklist.md)）を満たせず、何を検証すべきかレビュアーに伝わらない。
   正しい形: GIT-9, GIT-10 のテンプレートに従い `## Summary` / `## Verification` / フッターを書く。

## 参考実装（サンプル。削除後は存在しない）

Zenn サンプル実装に紐づくコミット・PR の実例。ソースコード（Zenn サンプル）がテンプレート利用時に削除されても、Git の履歴自体はスカッシュしない限り残るため、`git log`/`gh pr view` で参照し続けられる。

- `4aefc64` `fix(data): Default nullable Zenn response fields to null` — body で「なぜ」を英語で説明する実例
- `804b5d1` `build(gradle): Consolidate per-module Compose dependencies into bundles` — scope=`gradle` の `build` 実例
- `2c83bef` `chore(claude): Run debug-run platforms sequentially and harden the iOS/Android steps` — 箇条書き body の実例
- `789f491` `refactor(gradle): Organize version catalog with category comments and consistent syntax` — `refactor` 実例
- `bc37a56` `style(deps): Group compose library entries together in version catalog` — `style` 実例
- PR #10（`build(gradle): Consolidate per-module Compose dependencies into bundles`、`gh pr view 10`） — `## Summary` / `Intentionally left as-is` / `Verification` 構成の実例
- PR #8（`gh pr view 8`） — `## Summary` / `Verification` 構成の実例。タイトルが Conventional Commits 形式から外れる例外としても参照できる

## チェックリスト

- [ ] コミットメッセージが `<type>(<scope>): <Subject>` 形式になっているか（GIT-1〜GIT-3）
- [ ] type を判断基準1の表に従って選んだか
- [ ] body で変更の「なぜ」を英語で説明したか（軽微な変更を除く）（GIT-4）
- [ ] AI が作成したコミットに `Co-Authored-By` フッターが付いているか（GIT-5）
- [ ] `main` に直接コミットせず、`claude/<summary>` ブランチで作業したか（GIT-6, GIT-7）
- [ ] PR タイトルがコミットと同じ形式になっているか（GIT-8）
- [ ] PR 本文に `## Summary` と `## Verification` があるか（GIT-9）
- [ ] PR 本文の末尾に生成ツールフッターを付けたか（GIT-10）
- [ ] [pr-checklist.md](../playbooks/pr-checklist.md) で各ガイドの規約遵守を確認したか（GIT-11）
- [ ] 他セッションの未コミット変更がある状態で作業する場合、worktree で隔離したか（GIT-12）
- [ ] 依存更新は安定版を優先したか（GIT-13、ADR-0007）
