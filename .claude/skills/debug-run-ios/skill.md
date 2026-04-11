---
name: debug-run-ios
description: iOSアプリを起動中のシミュレーターにビルド・インストールする。デバッグ実行、動作確認時に使用。ビルド・ランタイムエラーを検出して報告。
allowed-tools:
  - Bash
  - Grep
  - AskUserQuestion
---

# iOSアプリのビルド・インストール・起動

現在のブランチをビルドし、起動中のiOSシミュレーターにインストールして起動する。

## プロジェクト固有情報

- **Bundle ID**: `org.starter.project.StarterProject`
- **Xcodeプロジェクト**: `iosApp/iosApp.xcodeproj`
- **スキーム**: `iosApp`
- **アプリ名**: `StarterProject`

## 前提条件

- macOS環境
- Xcode がインストールされている
- `xcrun` および `xcodebuild` コマンドが利用可能

## 実行手順

### 0. 実行モードの判定

このスキルがdebug-runスキルから呼び出されたかどうかを判定する。
判定方法: 環境変数 `CALLED_FROM_DEBUG_RUN` が設定されているかチェック。

- 設定されている場合: **JSON出力モード** （マークダウン出力なし）
- 設定されていない場合: **マークダウン出力モード** （ユーザー向け出力）

### 1. 起動中のシミュレーターを検出

```bash
xcrun simctl list devices booted 2>/dev/null | grep -E "iPhone|iPad"
```

**検証**:
- 起動中のシミュレーターが見つからない場合:
  - **`CALLED_FROM_DEBUG_RUN` が設定されている場合**: 親スキルで起動確認済みのため、エラーとして報告して終了
  - **単体実行の場合**: 利用可能なシミュレーター一覧を取得:
    ```bash
    xcrun simctl list devices available | grep -E "iPhone|iPad"
    ```
    - 利用可能なシミュレーターが0件の場合は終了
    - ある場合は `AskUserQuestion` でユーザーに選択させ、起動:
      ```bash
      xcrun simctl boot <simulator-uuid> && open -a Simulator
      ```

### 2. シミュレーターUUIDを取得

```bash
xcrun simctl list devices booted | grep -E "iPhone|iPad" | grep -oE '\([A-F0-9-]+\)' | tr -d '()' | head -1
```

### 3. ビルド

```bash
start_time=$(date +%s)

xcodebuild -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -sdk iphonesimulator \
  -destination 'id=<simulator-uuid>' \
  build 2>&1

exit_code=$?
end_time=$(date +%s)
build_time=$((end_time - start_time))
```

**ビルド失敗時**: エラーハンドリングセクションに従ってエラーを解析

### 4. アプリインストール・起動とランタイムエラー監視

**ビルド成果物をインストール**:

DerivedDataからビルド成果物のパスを特定してインストール:
```bash
app_path=$(find ~/Library/Developer/Xcode/DerivedData -path "*/Debug-iphonesimulator/StarterProject.app" -maxdepth 5 2>/dev/null | head -1)
xcrun simctl install <simulator-uuid> "$app_path"
```

**アプリを起動**:
```bash
xcrun simctl launch <simulator-uuid> org.starter.project.StarterProject
launch_exit=$?
```

**ランタイムエラー監視** (3秒待機):
```bash
sleep 3
xcrun simctl spawn <simulator-uuid> log show --predicate 'processImagePath contains "StarterProject"' --last 5s --style compact 2>&1 | grep -i "error\|crash\|exception" | head -20
```

## 出力フォーマット

このスキルは、単体実行時とdebug-runから呼び出された時で異なる出力を行う。

### 単体実行時: ユーザー向けマークダウン出力

単体で実行された場合、ユーザーフレンドリーなマークダウン形式で出力する。

**成功時**:

```markdown
## Build & Install Results

| Platform | Device | Build | Launch | Build Time |
|----------|--------|-------|--------|------------|
| iOS      | <simulator-name> | Success | Success | <XX>s |
```

**ビルド失敗時**:

```markdown
## Build & Install Results

| Platform | Device | Build | Launch | Build Time |
|----------|--------|-------|--------|------------|
| iOS      | <simulator-name> | Failed | N/A | <XX>s |

### iOS Build Error

**Failed target**: <target-name>

**Error type**: <compile/linker/signing/etc>

**Errors**:
- [File.swift:42](path/to/File.swift#L42): <error-message>

**Likely cause**: <推測>

**Suggested fix**:
- <具体的な解決手順1>
- <具体的な解決手順2>
```

**ランタイムエラー検出時**:

```markdown
## Build & Install Results

| Platform | Device | Build | Launch | Build Time |
|----------|--------|-------|--------|------------|
| iOS      | <simulator-name> | Success | Failed | <XX>s |

### iOS Launch Failed: Runtime Error

**Platform**: iOS

**Error type**: <例外タイプ>

**Exception**: <exception-type>: <message>

**Stack trace**:
```
<frame1>
<frame2>
<frame3>
```

**Likely cause**: <推測>

**Suggested fix**:
- <具体的な解決手順1>
- <具体的な解決手順2>
```

### debug-runから呼び出された時: JSON出力のみ

debug-runスキルから呼び出された場合、**JSON形式のみ**で出力する。
マークダウン出力はdebug-runスキル側で統合して表示されるため、このスキルでは出力しない。

**成功時**:
```json
{
  "platform": "iOS",
  "device": "<simulator-name>",
  "build_status": "Success",
  "launch_status": "Success",
  "build_time": "<XX>s"
}
```

**ビルド失敗時**:
```json
{
  "platform": "iOS",
  "device": "<simulator-name>",
  "build_status": "Failed",
  "launch_status": "N/A",
  "build_time": "<XX>s",
  "error": {
    "type": "Build Error",
    "failed_target": "<target-name>",
    "error_type": "<compile/linker/signing/etc>",
    "errors": ["<error1>", "<error2>"],
    "likely_cause": "<推測>",
    "suggested_fix": ["<fix1>", "<fix2>"]
  }
}
```

**ランタイムエラー検出時**:
```json
{
  "platform": "iOS",
  "device": "<simulator-name>",
  "build_status": "Success",
  "launch_status": "Failed",
  "build_time": "<XX>s",
  "error": {
    "type": "Runtime Error",
    "exception": "<exception-type>: <message>",
    "stack_trace": ["<frame1>", "<frame2>", "<frame3>"],
    "likely_cause": "<推測>",
    "suggested_fix": ["<fix1>", "<fix2>"]
  }
}
```

## エラーハンドリング

### ビルドエラー解析

**検索キーワード**:
- `error:`, `FAILED`, `BUILD FAILED`
- `clang:`, `ld:`, `Code Sign error`, `Provisioning profile`

**抽出する情報**:
- コンパイラエラー（ファイルパスと行番号）
- リンカーエラー（シンボル名）
- コード署名/プロビジョニングの問題

### ランタイムエラー解析

```bash
xcrun simctl spawn <simulator-uuid> log show --predicate 'processImagePath contains "StarterProject"' --last 10s --style compact
```

**抽出する情報**:
- クラッシュレポートの例外タイプ
- クラッシュしたメソッド/関数名
- スタックトレースの最初の5フレーム
- アサーション失敗メッセージ

**よくあるパターン**:
1. `EXC_BAD_ACCESS` → メモリアクセス違反
2. `precondition failure` → アサーション失敗
3. `fatalError` → 致命的エラー
