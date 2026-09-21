---
name: remove-sample-code
description: Remove the bundled Zenn sample implementation and replace feature/home with a minimal placeholder screen, leaving only the architecture skeleton (layers, DI, navigation types) in place. Use when starting a new project from this template.
---

# サンプルコード（Zenn ビューワー）を削除する

**オーケストレーター（監督者）視点**の手順です。削除は破壊的操作のため、必ず対象一覧を提示しユーザーの明示的な確認を得てから実行する。本テンプレートで新規プロジェクトを始める最初のタイミングで実施する。

## 手順

### 1. 削除対象の確定と提示（監督者が直接行う。委譲なし。**確認必須**）

`find`/`grep` で以下を再確認し、一覧をユーザーに提示して承認を得るまで次のステップに進まない。以降のステップで削除・書き換える全ファイルを漏れなく載せる。

**削除するモジュール（ディレクトリごと）**: `composeApp/data/zenn/`, `composeApp/domain/zenn/`, `composeApp/feature/user/`

**削除する個別ファイル・ディレクトリ**: `composeApp/data/repository/.../ZennRepository.kt`, `composeApp/domain/service/.../ZennService.kt`, `composeApp/base/.../data/model/zenn/`（ディレクトリごと）, `composeApp/ui/.../shared/component/article/`（ディレクトリごと）

**書き換える（削除ではない）**:
- `composeApp/ui/.../route/AppRoute.kt`（`User` ルート削除、`Home` を引数なしの `data object` に変更）
- `composeApp/app/.../di/Koin.kt`（Zenn 固有の import・バインドを削除し層別モジュールを空骨格で残す）
- `composeApp/app/.../navigation/AppNavHost.kt`（`User` の `composable` ブロック削除、`Home` から `toRoute`/`navArgs` 参照を除去）
- `composeApp/core/.../preferences/PreferencesKey.kt`（`LastKeyword` エントリを除去し空の enum として残す。`add-data-source` が後でこの enum にキーを追記する前提を崩さない）
- `composeApp/core/.../api/ApiConfig.kt`, `composeApp/core/.../preferences/PreferencesConfig.kt`（プレースホルダに差し替え）
- `composeApp/feature/home/` の Screen/State/Event/ViewModel 4点セット
- `settings.gradle.kts`（削除モジュールの `include` を3行削除）, `composeApp/app/build.gradle.kts`（対応する `implementation` を3行削除）
- `README.md`（Demo節・末尾の Zenn リンク等サンプル固有の記述を更新）

### 2. モジュール・ファイルの削除（監督者が直接行う。委譲なし。ステップ1承認後、**直列必須**）

承認後、`git rm -r`/`git rm` でステップ1の「削除するモジュール」「削除する個別ファイル・ディレクトリ」を削除する。部分的に消さず、モジュール・ディレクトリ単位でまとめて実行する。

### 3. 新しい `AppRoute` の形を確定する（監督者が直接行う。委譲なし。ステップ2完了後）

以降の 4a/4c/5 を並列委譲し 4b を同時に進めるため、`AppRoute` の確定形を先に固定し各指示に含めて渡す（各担当が独立に設計すると整合しなくなるため）。

```kotlin
sealed class AppRoute {
    @Serializable
    data object Home : AppRoute()
}
```

`AppRoute.kt` には `AppRouter` interface も同居しており、これは変更せず残す。

### 4. 参照の更新（4a・4c は実装エージェントへ委譲、4b は監督者が直接行う。ステップ3完了後）

- **4a. ui モジュールの更新**（実装エージェントへ委譲）
  - **対象モジュール**: `composeApp/ui`
  - **指示の要点**: まず `composeApp/ui/AGENTS.md` を読む。`AppRoute.kt` をステップ3の確定形に書き換える。
  - **並列可否**: 4b・4c・ステップ5と並列可。
- **4b. app モジュールの Koin/AppNavHost 更新**（**監督者が直接行う**。ルート AGENTS.md 実行体制ルール1 の統合作業。委譲なし）
  - `Koin.kt` から Zenn 固有の import・バインドを削除し層別モジュールの空骨格（`// TODO: register your ... bindings here.`）を残す。`AppNavHost.kt` から `User` の `composable` ブロックを削除し、`Home` の `composable` ブロックはステップ3の確定形に合わせて `toRoute`/`route.navArgs` 参照と不要になった import（`UserScreen`/`UserScreenViewModel`/`toRoute`）を除去して `HomeScreen(viewModel = viewModel, appRouter = appRouter)` 呼び出しにする。`NavHost` の `startDestination` も `AppRoute.Home`（引数なし）に変更する。
  - **並列可否**: 4a・4c・ステップ5の委譲と同時に進めてよい。
- **4c. core モジュールの更新**（実装エージェントへ委譲）
  - **対象モジュール**: `composeApp/core`
  - **指示の要点**: まず `composeApp/core/AGENTS.md` を読む。`PreferencesKey.kt` から `LastKeyword` エントリを除去し空の `enum class Preference` として残す（削除しない。`add-data-source` skill が後でここにキーを追記する）。`ApiConfig.kt`/`PreferencesConfig.kt` をプレースホルダに差し替える（→ docs/coding-guide.md §8 の既知の逸脱は引き継がない）。
  - **並列可否**: 4a・4b・ステップ5と並列可。

### 5. プレースホルダ画面の作成（実装エージェントへ委譲。ステップ3完了後、ステップ4と並列可）

- **対象モジュール**: `composeApp/feature/home`
- **指示の要点**: まず `composeApp/feature/home/AGENTS.md` を読む。ステップ3の確定形（`Home` は引数なし）を前提に、依存を持たない最小の4点セット（`screenState` のみの State、`OnClickErrorScreenAction` のみの Event、引数なし ViewModel）に書き換える。`HomeScreen` は `navArgs` パラメータを持たない。`build.gradle.kts` の依存は変更しない（次の機能追加のための骨格として残す）（→ docs/coding-guide.md §4）。
- **完了条件**: `HomeScreen` がプレースホルダ文言を表示できる状態であること。

### 6. 監督者による整合性反映（委譲なし）

`settings.gradle.kts` から削除したモジュールの `include` を3行削除し、`composeApp/app/build.gradle.kts` から対応する `implementation` を3行削除する。`README.md` のサンプル固有の記述を更新する。ステップ4・5の実装エージェントからの「他モジュールとの接点」報告をここで反映する。

### 7. リネーム（任意。監督者が直接行う。ステップ6完了後）

プロジェクト名・パッケージ名の変更は `README.md` の `## How to Rename` にある Gradle タスク（`ChangeProjectName`/`ChangePackageName`）で行う。本ステップは削除作業と独立だが、削除対象パスが本 skill 記載のパスと一致した状態で作業できるため、削除完了後に行う。

### 8. 検証・整合性確認（監督者が直接行う）

- `grep -rn -i zenn --include=*.kt --include=*.kts --include=*.xml --include=*.md .` を実行し、`docs/` 配下（根拠として実名に言及する箇所）を除きヒット0件であることを確認する。
- テスト実行: `./gradlew testAndroidHostTest`（`data/zenn`/`domain/zenn` のテストが削除済みのため、0件成功で失敗しないことのみ確認する）。
- 実機確認: `.claude/skills/debug-run-android/skill.md` でプレースホルダ文言の表示を確認する。
- `git status` で削除・変更したファイルがステップ1の一覧と過不足なく一致していることを確認する。PR 前チェックは docs/coding-guide.md §6 に従う。
