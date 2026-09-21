---
name: remove-sample-code
description: Remove the bundled Zenn sample implementation and replace feature/home with a minimal placeholder screen, leaving only the architecture skeleton (layers, DI, navigation types) in place. Use when starting a new project from this template.
---

# サンプルコード（Zenn ビューワー）を削除する

**オーケストレーター（監督者）視点**の手順です。削除は破壊的操作のため、必ず対象一覧を提示しユーザーの明示的な確認を得てから実行する。本テンプレートで新規プロジェクトを始める最初のタイミングで実施する。

## 手順

### 1. 削除対象の確定と提示（監督者が直接行う。委譲なし。**確認必須**）

`find`/`grep` で以下を再確認し、一覧をユーザーに提示して承認を得るまで次のステップに進まない。

**削除するモジュール（ディレクトリごと）**: `composeApp/data/zenn/`, `composeApp/domain/zenn/`, `composeApp/feature/user/`

**削除する個別ファイル・ディレクトリ**: `composeApp/data/repository/.../ZennRepository.kt`, `composeApp/domain/service/.../ZennService.kt`, `composeApp/base/.../data/model/zenn/`（ディレクトリごと）, `composeApp/ui/.../shared/component/article/`（ディレクトリごと）, `composeApp/core/.../preferences/PreferencesKey.kt`

**書き換える（削除ではない）**: `composeApp/feature/home/` の Screen/State/Event/ViewModel 4点セット

### 2. モジュール・ファイルの削除（監督者が直接行う。委譲なし。ステップ1承認後、**直列必須**）

承認後、`git rm -r`/`git rm` でステップ1の一覧を削除する。部分的に消さず、モジュール・ディレクトリ単位でまとめて実行する（`base` の参照元と `ui` の参照先を消し忘れるとビルドが壊れる）。

### 3. 参照の更新（実装エージェントへ委譲。ステップ2完了後）

- **3a. ui モジュールの更新**
  - **対象モジュール**: `composeApp/ui`
  - **指示の要点**: まず `composeApp/ui/AGENTS.md` を読む（未作成の場合は docs/design-guide.md §5 を参照）。`AppRoute` から `User` ルートを削除し、引数を持たない `Home` を `data object` に簡略化する。
  - **並列可否**: 3b・ステップ4と並列可。
- **3b. app モジュールの更新**
  - **対象モジュール**: `composeApp/app`
  - **指示の要点**: まず `composeApp/app/AGENTS.md` を読む。`Koin.kt` から Zenn 固有の import・バインドを削除し層別モジュールの空骨格（`// TODO: register your ... bindings here.`）を残す。`AppNavHost.kt` から `User` の `composable` ブロックを削除する。`ApiConfig.kt`/`PreferencesConfig.kt` をプレースホルダに差し替える（→ docs/coding-guide.md §8 の既知の逸脱は引き継がない）。
  - **並列可否**: 3a・ステップ4と並列可。

### 4. プレースホルダ画面の作成（実装エージェントへ委譲。ステップ2完了後、ステップ3と並列可）

- **対象モジュール**: `composeApp/feature/home`
- **指示の要点**: まず `composeApp/feature/home/AGENTS.md` を読む。依存を持たない最小の4点セット（`screenState` のみの State、`OnClickErrorScreenAction` のみの Event、引数なし ViewModel）に書き換える。`build.gradle.kts` の依存は変更しない（次の機能追加のための骨格として残す）（→ docs/coding-guide.md §4）。
- **完了条件**: `HomeScreen` がプレースホルダ文言を表示できる状態であること。

### 5. 監督者による整合性反映（委譲なし）

`settings.gradle.kts` から削除したモジュールの `include` を3行削除し、`composeApp/app/build.gradle.kts` から対応する `implementation` を3行削除する。`README.md` のサンプル固有の記述を更新する。ステップ3・4の実装エージェントからの「他モジュールとの接点」報告をここで反映する。

### 6. リネーム（任意。監督者が直接行う。ステップ5完了後）

プロジェクト名・パッケージ名の変更は `README.md` の `## How to Rename` にある Gradle タスク（`ChangeProjectName`/`ChangePackageName`）で行う。本ステップは削除作業と独立だが、削除対象パスが本 skill 記載のパスと一致した状態で作業できるため、削除完了後に行う。

### 7. 検証・整合性確認（監督者が直接行う）

- `grep -rn -i zenn --include=*.kt --include=*.kts --include=*.xml --include=*.md .` を実行し、`docs/` 配下（根拠として実名に言及する箇所）を除きヒット0件であることを確認する。
- テスト実行: `./gradlew testAndroidHostTest`（`data/zenn`/`domain/zenn` のテストが削除済みのため、0件成功で失敗しないことのみ確認する）。
- 実機確認: `.claude/skills/debug-run-android/skill.md` でプレースホルダ文言の表示を確認する。
- `git status` で削除・変更したファイルがステップ1〜4の一覧と過不足なく一致していることを確認する。PR 前チェックは docs/coding-guide.md §6 に従う。
