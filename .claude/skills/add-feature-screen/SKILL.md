---
name: add-feature-screen
description: Add a new screen to the app as a feature module or a sub-package of an existing one, from Route registration through Koin/NavHost wiring to tests. Use when asked to add a new screen, page, or user-facing flow.
---

# 新しい画面を追加する

このドキュメントは**オーケストレーター（監督者）視点**の手順です。監督者は分解・委譲・整合性検証を行い、モジュール内部の実装コードは実装エージェントに委譲します。実装エージェントは自モジュール外を編集せず、他モジュールに必要な変更は完了報告に含めます。

## 手順

### 1. 前提確認（監督者が直接行う。委譲なし）

画面が呼び出す Service interface が `composeApp/domain/service` に既に存在するか確認する。存在しない場合、まず [add-data-source](../add-data-source/SKILL.md) skill を実行し `data`/`domain` 側を作ってから本手順に進む（**直列必須**）。ローカル設定のみを扱う画面でも Repository/Service の作成は省略できない（→ docs/decisions.md D-10）。

### 2. モジュール方針の決定（監督者が直接行う。委譲なし）

新しい `feature` モジュールを新設するか、既存モジュールに `<screen>/` サブパッケージを追加するかを判断する（→ docs/decisions.md D-09）。新設する場合は `settings.gradle.kts` に `include(":composeApp:feature:<feature>")` を追記し、`composeApp/feature/<feature>/` を作成した上で、同系モジュール `composeApp/feature/home/` から `AGENTS.md` と `CLAUDE.md` をコピーして調整する。実装エージェントが最初に読むファイルなので、このステップで用意しておく。

### 3. 契約整備: `ui` への Route 追加（実装エージェントへ委譲。**直列必須**、後続すべての前提）

- **対象モジュール**: `composeApp/ui`
- **指示の要点**: まず `composeApp/ui/AGENTS.md` を読む。`AppRoute` に新しいルートを追加する（引数はプリミティブ型のみ、`NavArgs` パターン）。完了条件は Route 定義が型として完結していること（→ docs/design-guide.md §5、docs/coding-guide.md §4）。
- **並列可否**: 直列必須。この後の全ステップが本ステップの完了を前提とする。

### 4. feature モジュールの実装（実装エージェントへ委譲。ステップ3完了後に実行）

- **対象モジュール**: `composeApp/feature/<feature>`
- **指示の要点**: まず `composeApp/feature/<feature>/AGENTS.md` を読む。Screen/State/Event/ViewModel の4点セットと、文言があれば Compose Resources を実装する。ViewModel のコンストラクタ注入は `domain:service` の interface のみ。ViewModel テストを追加する。完了条件はテストが揃っていること（→ docs/coding-guide.md §4, §5）。
- 遷移元画面がある場合、遷移元の `XxxScreenEventHandler` への分岐追加も同じエージェントの担当に含める（遷移元が同一モジュール内であれば自モジュール内の変更で完結する）。
- **並列可否**: 単一画面の追加なら1系統。複数画面を同時に追加する場合、画面間に依存がなければ画面ごとに並列可。

### 5. 監督者による統合（委譲なし）

- 新設モジュールの場合、`composeApp/app/build.gradle.kts` の `commonMain.dependencies` に `implementation(projects.composeApp.feature.<feature>)` を追加する。
- `composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/AppNavHost.kt` に `composable<AppRoute.Xxx>` ブロックを追加する（→ docs/design-guide.md §5）。
- `composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt` の `appModule` に ViewModel を登録する（→ docs/design-guide.md §4）。
- 実装エージェントからの「他モジュールとの接点」報告（Route/Koin/NavHost に必要な変更）をここで反映する。

### 6. 検証・整合性確認（監督者が直接行う）

- テスト実行: `./gradlew :composeApp:feature:<feature>:testAndroidHostTest`（リポジトリ全体は `./gradlew testAndroidHostTest`）。
- 実機/シミュレータ確認: `.claude/skills/debug-run/skill.md`（DeepLink の `basePath` を設定した場合は起動確認も行う）。
- Koin 起動確認: 画面遷移時に `NoDefinitionFoundException` が出ないこと。
- PR 前チェック: 依存方向（feature は `domain:service` と `ui` のみ）、Route の引数がプリミティブ型のみであること、テスト必須範囲を満たしていることを確認する（→ docs/coding-guide.md §6）。
