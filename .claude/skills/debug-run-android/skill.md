---
name: debug-run-android
description: Androidアプリを起動中のエミュレーターにビルド・インストールする。デバッグ実行、動作確認時に使用。ビルド・ランタイムエラーを検出して報告。
allowed-tools:
  - Bash
  - Grep
  - AskUserQuestion
---

# Androidアプリのビルド・インストール・起動

現在のブランチをビルドし、起動中のAndroidエミュレーターにインストールして起動する。

## プロジェクト固有情報

- **パッケージ名**: `org.starter.project`
- **アプリモジュール**: `androidApp`
- **Gradleタスク**: `:androidApp:installDebug`
- **Activity**: `org.starter.project/.app.MainActivity`

## 前提条件

- Android SDK がインストールされている
- `adb` および `emulator` コマンドが利用可能
- プロジェクトルートに `build.gradle` または `build.gradle.kts` が存在

## 実行手順

### 0. 実行モードの判定

このスキルがdebug-runスキルから呼び出されたかどうかを判定する。
判定方法: 環境変数 `CALLED_FROM_DEBUG_RUN` が設定されているかチェック。

- 設定されている場合: **JSON出力モード** （マークダウン出力なし）
- 設定されていない場合: **マークダウン出力モード** （ユーザー向け出力）

### 1. 起動中のエミュレーターを検出

```bash
adb devices 2>/dev/null | grep -E "device$|emulator"
```

**検証**:
- 起動中のデバイスが見つからない場合:
  - **`CALLED_FROM_DEBUG_RUN` が設定されている場合**: 親スキルで起動確認済みのため、エラーとして報告して終了
  - **単体実行の場合**: 利用可能なエミュレーター一覧を取得:
    ```bash
    emulator -list-avds 2>/dev/null
    ```
    - 利用可能なエミュレーターが0件の場合は終了
    - ある場合は `AskUserQuestion` でユーザーに選択させ、起動:
      ```bash
      emulator -avd <avd-name> -no-snapshot-load &
      adb wait-for-device shell getprop sys.boot_completed | grep -m 1 '1'
      ```

### 2. ビルド・インストール

```bash
start_time=$(date +%s)
./gradlew :androidApp:installDebug 2>&1
exit_code=$?
end_time=$(date +%s)
build_time=$((end_time - start_time))
```

**ビルド失敗時**: エラーハンドリングセクションに従ってエラーを解析

### 3. アプリ起動とランタイムエラー監視

```bash
# ログ監視開始
adb logcat -c
adb logcat -v time 'AndroidRuntime:E' '*:S' > /tmp/android_runtime.log 2>&1 &
logcat_pid=$!
```

**アプリを起動**:
```bash
adb shell am start -n org.starter.project/.app.MainActivity
```

**ランタイムエラー監視** (3秒待機):
```bash
sleep 3
kill $logcat_pid 2>/dev/null
runtime_error=$(grep -A 20 "FATAL EXCEPTION\|AndroidRuntime" /tmp/android_runtime.log)
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
| Android  | <device-name> | Success | Success | <XX>s |
```

**ビルド失敗時**:

```markdown
## Build & Install Results

| Platform | Device | Build | Launch | Build Time |
|----------|--------|-------|--------|------------|
| Android  | <device-name> | Failed | N/A | <XX>s |

### Android Build Error

**Failed task**: <task-name>

**Error type**: <dependency/compile/etc>

**Errors**:
- [File.kt:42](path/to/File.kt#L42): <error-message>

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
| Android  | <device-name> | Success | Failed | <XX>s |

### Android Launch Failed: Runtime Error

**Platform**: Android

**Error type**: <例外タイプ>

**Exception**: <exception-class>: <message>

**Stack trace**:
```
<line1>
<line2>
<line3>
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
  "platform": "Android",
  "device": "<device-name>",
  "build_status": "Success",
  "launch_status": "Success",
  "build_time": "<XX>s"
}
```

**ビルド失敗時**:
```json
{
  "platform": "Android",
  "device": "<device-name>",
  "build_status": "Failed",
  "launch_status": "N/A",
  "build_time": "<XX>s",
  "error": {
    "type": "Build Error",
    "failed_task": "<task-name>",
    "error_type": "<dependency/compile/etc>",
    "errors": ["<error1>", "<error2>"],
    "likely_cause": "<推測>",
    "suggested_fix": ["<fix1>", "<fix2>"]
  }
}
```

**ランタイムエラー検出時**:
```json
{
  "platform": "Android",
  "device": "<device-name>",
  "build_status": "Success",
  "launch_status": "Failed",
  "build_time": "<XX>s",
  "error": {
    "type": "Runtime Error",
    "exception": "<exception-class>: <message>",
    "stack_trace": ["<line1>", "<line2>", "<line3>"],
    "likely_cause": "<推測>",
    "suggested_fix": ["<fix1>", "<fix2>"]
  }
}
```

## エラーハンドリング

### ビルドエラー解析

**検索キーワード**:
- `error:`, `FAILED`, `Could not resolve`, `Execution failed`, `BUILD FAILED`
- `Task.*failed`, `Dependency resolution`, `Compilation error`

**抽出する情報**:
- 失敗したタスクパス
- 依存関係解決エラー
- コンパイルエラー（ファイル名と行番号）

### ランタイムエラー解析

```bash
adb logcat -d -v time 'AndroidRuntime:E' '*:S' | grep -A 30 "FATAL EXCEPTION"
```

**抽出する情報**:
- 例外クラス名
- 例外メッセージ
- スタックトレースの最初の3行（アプリコード部分）
- クラッシュしたスレッド名

**よくあるパターン**:
1. `UninitializedPropertyAccessException` → 初期化エラー
2. `Resources$NotFoundException` → リソースエラー
3. `ClassNotFoundException` → クラスローディングエラー
