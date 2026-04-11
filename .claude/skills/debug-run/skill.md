---
name: debug-run
description: モバイルアプリ(iOS/Android)を起動中のシミュレーター/エミュレーターにビルド・インストールする。デバッグ実行、アプリのテスト、動作確認、手動検証時に使用。ビルドエラーを検出して報告。
argument-hint: "[android|ios] (省略時は両方のプラットフォーム)"
allowed-tools:
  - Skill
  - Bash
  - AskUserQuestion
---

# 起動中のシミュレーターへビルド・インストール

現在のブランチをビルドし、起動中のすべてのiOSシミュレーターおよびAndroidエミュレーターにインストールする。
`$ARGUMENTS` に "android" または "ios" が指定された場合、そのプラットフォームのみを対象とする。

## プロジェクト固有情報

| 項目 | Android | iOS |
|------|---------|-----|
| パッケージ名/Bundle ID | `org.starter.project` | `org.starter.project.StarterProject` |
| アプリモジュール | `androidApp` | `iosApp/iosApp.xcodeproj` |
| Gradleタスク/スキーム | `:androidApp:installDebug` | `iosApp` |
| Activity/起動コマンド | `org.starter.project/.app.MainActivity` | `xcrun simctl launch <uuid> org.starter.project.StarterProject` |

## 実行手順

### 0. シミュレーター/エミュレーターの起動確認（最優先）

**ビルドの前に、まず対象プラットフォームのシミュレーター/エミュレーターが起動しているか確認する。**
この確認は最初に素早く行い、未起動の場合はユーザーに起動の要否を確認する。

対象プラットフォームに応じて、以下を**並列**で実行:

**iOS（対象に含まれる場合）**:
```bash
xcrun simctl list devices booted 2>/dev/null | grep -E "iPhone|iPad"
```

**Android（対象に含まれる場合）**:
```bash
adb devices 2>/dev/null | grep -E "device$|emulator"
```

#### 結果に応じた処理

**両方起動済みの場合**: → ステップ1へ進む

**いずれかが未起動の場合**:

1. 未起動のプラットフォームについて、利用可能なデバイス一覧を取得:
   - iOS: `xcrun simctl list devices available | grep -E "iPhone|iPad"`
   - Android: `emulator -list-avds 2>/dev/null`
2. `AskUserQuestion` で未起動のシミュレーター/エミュレーターを起動するか確認し、起動する場合はデバイスを選択してもらう
3. 選択されたデバイスを起動:
   - iOS: `xcrun simctl boot <simulator-uuid> && open -a Simulator`
   - Android: `emulator -avd <avd-name> -no-snapshot-load &` → `adb wait-for-device shell getprop sys.boot_completed | grep -m 1 '1'`
4. 起動完了を待ってからステップ1へ進む

**利用可能なデバイスが0件の場合**: 該当プラットフォームはスキップ

### 1. プラットフォームの決定

`$ARGUMENTS` に基づいて実行するプラットフォームを決定する:

- `$ARGUMENTS` が "android" → Androidのみ実行
- `$ARGUMENTS` が "ios" → iOSのみ実行
- `$ARGUMENTS` が空または上記以外 → 両方を並列実行

### 2. プラットフォーム別スキルを並列実行

**重要**: 各スキルを呼び出す前に、環境変数 `CALLED_FROM_DEBUG_RUN=true` を設定する。
これにより、サブスキルはJSON形式のみで出力し、マークダウン出力を行わない。

**Androidのみの場合**:
```bash
export CALLED_FROM_DEBUG_RUN=true
```
その後、スキルを実行:
```
debug-run-android
```

**iOSのみの場合**:
```bash
export CALLED_FROM_DEBUG_RUN=true
```
その後、スキルを実行:
```
debug-run-ios
```

**両方の場合（並列実行）**:
```bash
export CALLED_FROM_DEBUG_RUN=true
```
その後、以下を**並列**で実行:
```
debug-run-android
```
と
```
debug-run-ios
```

**重要**:
- 両プラットフォームを実行する場合は、完全に並列で実行する
- 一方の完了を待たずに、もう一方も同時に実行する
- 各スキルは独立して動作し、それぞれJSON形式で結果を返す
- マークダウン出力はこのスキル（debug-run）でのみ行う

### 3. 結果の統合と出力

各スキルからのJSON出力を収集し、統合されたテーブル形式で出力する。

**出力フォーマット**:

```markdown
## Build & Install Results

| Platform | Device | Build | Launch | Build Time |
|----------|--------|-------|--------|------------|
| Android  | <device-name> | Success/Failed | Success/Failed/N/A | <XX>s |
| iOS      | <simulator-name> | Success/Failed | Success/Failed/N/A | <XX>s |
```

**フォーマット詳細**:
- **Platform**: `Android` または `iOS`
- **Device**: デバイス/シミュレーター名
- **Build**: `Success` または `Failed`
- **Launch**: `Success`、`Failed`、または `N/A` (ビルド失敗時)
- **Build Time**: ビルドにかかった秒数

### 4. エラーサマリーの出力

各スキルからエラー情報が返された場合、テーブルの下に詳細なエラーサマリーを出力する。

#### ビルドエラーの場合

```markdown
### <Platform> Build Error

**Failed task/target**: <タスクまたはターゲット名>

**Error type**: <依存関係/コンパイル/署名/リンカー など>

**Errors**:
- [File.kt:42](path/to/File.kt#L42): Unresolved reference: foo
- [File.swift:15](path/to/File.swift#L15): Type mismatch error

**Likely cause**: <エラーパターンに基づく推測>

**Suggested fix**:
- <具体的な解決手順1>
- <具体的な解決手順2>
```

#### ランタイムエラーの場合

```markdown
### <Platform> Launch Failed: Runtime Error

**Platform**: <Android/iOS>

**Error type**: <例外タイプまたはクラッシュ理由>

**Exception**: <例外クラス名とメッセージ>

**Stack trace**:
```
<スタックトレースの重要な部分 (最大10行)>
```

**Likely cause**: <エラーパターンに基づく推測>

**Suggested fix**:
- <具体的な解決手順1>
- <具体的な解決手順2>
```

## 出力例

### 例1: 両プラットフォーム成功

```markdown
## Build & Install Results

| Platform | Device | Build | Launch | Build Time |
|----------|--------|-------|--------|------------|
| Android  | Pixel 5 API 34 | Success | Success | 38s |
| iOS      | iPhone 15 Pro | Success | Success | 58s |
```

### 例2: Androidビルド失敗、iOS成功

```markdown
## Build & Install Results

| Platform | Device | Build | Launch | Build Time |
|----------|--------|-------|--------|------------|
| Android  | Pixel 5 API 34 | Failed | N/A | 12s |
| iOS      | iPhone 15 Pro | Success | Success | 58s |

### Android Build Error

**Failed task**: :androidApp:compileDebugKotlin

**Error type**: コンパイルエラー

**Errors**:
- [MainActivity.kt:25](app/src/main/kotlin/MainActivity.kt#L25): Unresolved reference: database

**Likely cause**: 未定義の変数を参照しています

**Suggested fix**:
- MainActivity.kt:25 で `database` プロパティを定義するか、正しい変数名を使用してください
```

### 例3: Androidランタイムエラー、iOS成功

```markdown
## Build & Install Results

| Platform | Device | Build | Launch | Build Time |
|----------|--------|-------|--------|------------|
| Android  | Pixel 5 API 34 | Success | Failed | 42s |
| iOS      | iPhone 15 Pro | Success | Success | 58s |

### Android Launch Failed: Runtime Error

**Platform**: Android

**Error type**: 初期化エラー

**Exception**: kotlin.UninitializedPropertyAccessException: lateinit property database has not been initialized

**Stack trace**:
```
at org.starter.project.app.MainActivity.onCreate(MainActivity.kt:25)
at android.app.Activity.performCreate(Activity.java:8000)
at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3785)
```

**Likely cause**: `database` プロパティが初期化される前にアクセスされています

**Suggested fix**:
- [MainActivity.kt:25](androidApp/src/main/kotlin/org/starter/project/app/MainActivity.kt#L25) で `database` プロパティを使用する前に初期化してください
- `::database.isInitialized` チェックを追加することを検討してください
```

## 制限事項

このスキルには以下の制限があります:

1. **デバッグビルドのみ対応**:
   - Androidは `installDebug` タスクのみ実行
   - iOSはデバッグ構成のみビルド

2. **シミュレーター/エミュレーターのみ**:
   - 実機デバイスへのインストールには対応していません

3. **環境依存性**:
   - Android: Android SDK、Gradle、adb が必要
   - iOS: macOS、Xcode、Command Line Tools が必要
