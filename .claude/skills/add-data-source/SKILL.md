---
name: add-data-source
description: Add a new external data source (API or local-settings-backed resource) end to end, from data:<source>/domain:<name> module scaffolding through Repository/Service contracts, DI wiring, and tests. Use when asked to add a new API integration, data source, or backend endpoint.
---

# 新しい API/データソースを追加する

**オーケストレーター（監督者）視点**の手順です。監督者は分解・委譲・整合性検証を行い、モジュール内部の実装コードは実装エージェントに委譲します。実装エージェントは自モジュール外を編集せず、他モジュールに必要な変更は完了報告に含めます。

## 手順

### 1. 方針決定（監督者が直接行う。委譲なし）

既存 Repository へのメソッド追加か、新しい `data:<source>`/`domain:<name>` の新設かを判断する（→ docs/decisions.md D-09）。API を持たずローカル設定のみを扱う場合も Repository + Service の作成は省略できない（→ docs/decisions.md D-10）。この場合 DTO/API/Converter は省略し、`composeApp/core` の `Preference` 列挙型（`PreferencesKey.kt`）にキーを追記したうえで、`data/<source>/datasource/preferences/` に `interface XxxPreferences` + `class XxxPreferencesImpl(settings: Settings)` を追加する分岐になる（→ docs/coding-guide.md §2）。

別ホストの API を追加する場合は、`composeApp/core` の `ApiClientImpl` を `baseUrl` 引数化し、`composeApp/app` の Koin で `named("<host>")` 登録する分岐になる（現状未実装。→ docs/decisions.md D-25、docs/design-guide.md §4）。この場合は `composeApp/core`/`composeApp/app` も対象モジュールに含め、実装エージェントには `composeApp/core/AGENTS.md` を最初に読ませる。

### 2. 新規モジュールの雛形作成（監督者が直接行う。委譲なし）

新設する場合、`settings.gradle.kts` に `include(":composeApp:data:<source>")`/`include(":composeApp:domain:<name>")` を追記し、両ディレクトリを作成する。同系の既存モジュール（`composeApp/data/<source>/`、`composeApp/domain/<name>/` 等）から、それぞれ `AGENTS.md`/`CLAUDE.md` をコピーして調整する。無ければ `docs/coding-guide.md` の該当節（§2 / §3）を基に新規作成する。実装エージェントが最初に読むファイルなので、このステップで用意しておく。

### 3. 契約整備（実装エージェントへ委譲。**直列必須**、実装フェーズ全体の前提）

- **3a. base へドメインモデル追加**
  - **対象モジュール**: `composeApp/base`
  - **指示の要点**: まず `composeApp/base/AGENTS.md` を読む。DTO とは別の non-null なドメインモデルを追加する（→ docs/design-guide.md §3）。
  - **並列可否**: 直列必須。3b/3c より先に完了させる。
- **3b. data:repository へ Repository interface 追加**
  - **対象モジュール**: `composeApp/data/repository`
  - **指示の要点**: まず `composeApp/data/repository/AGENTS.md` を読む。`Repository` マーカーを継承する interface を追加する。戻り値は素のドメインモデル、例外は透過させる（→ docs/design-guide.md §3）。
  - **並列可否**: 3a 完了後、3c と並列可。
- **3c. domain:service へ Service interface 追加**
  - **対象モジュール**: `composeApp/domain/service`
  - **指示の要点**: まず `composeApp/domain/service/AGENTS.md` を読む。`Service` マーカーを継承する interface を追加する。戻り値は `Result<T>`（→ docs/design-guide.md §3, §6）。
  - **並列可否**: 3a 完了後、3b と並列可。

### 4. 実装（実装エージェントへ委譲。ステップ3完了後に実行）

- **4a. data:<source> の実装**
  - **対象モジュール**: `composeApp/data/<source>`
  - **指示の要点**: まず `composeApp/data/<source>/AGENTS.md` を読む。DTO（全フィールド nullable `= null`）、Ktorfit API interface、Converter、Repository 実装を作る。Converter/Repository のテストを追加する。完了条件はテストが揃っていること（→ docs/coding-guide.md §2, §5）。
  - **並列可否**: 4b と並列可。
- **4b. domain:<name> の実装**
  - **対象モジュール**: `composeApp/domain/<name>`
  - **指示の要点**: まず `composeApp/domain/<name>/AGENTS.md` を読む。`ResultHandler.async`/`immediate` で包んだ Service 実装を作る。Service のテストを追加する（→ docs/coding-guide.md §3, §5）。
  - **並列可否**: 4a と並列可。

### 5. 監督者による統合（委譲なし）

- `composeApp/app/build.gradle.kts` に `data:<source>`/`domain:<name>` への依存を追加する。
- `composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt` の `dataSourceModule`/`repositoryModule`/`serviceModule` にバインドを追加する（→ docs/design-guide.md §4）。
- 実装エージェントからの「他モジュールとの接点」報告をここで反映する。

### 6. 検証・整合性確認（監督者が直接行う）

- テスト実行: `./gradlew :composeApp:data:<source>:testAndroidHostTest` と `./gradlew :composeApp:domain:<name>:testAndroidHostTest`。
- Koin 登録漏れ確認: 新しい Service を使う画面に遷移し `NoDefinitionFoundException` が出ないこと（[add-feature-screen](../add-feature-screen/SKILL.md) 実施後）。実機/シミュレータ確認は `.claude/skills/debug-run/skill.md`。
- PR 分割: 契約（`base` モデル / `data:repository` / `domain:service`）→ 実装（`data/<source>`, `domain/<name>`）→ `app` 登録 の段階ごとに Stacked PR で起票する（後続 PR の base は先行ブランチ。→ AGENTS.md 作業ルール、docs/coding-guide.md §6）。
- PR 前チェック: 実装モジュール同士が直接依存していないこと、Repository が例外透過であること、DTO が全 nullable であることを確認する（→ docs/coding-guide.md §6）。
