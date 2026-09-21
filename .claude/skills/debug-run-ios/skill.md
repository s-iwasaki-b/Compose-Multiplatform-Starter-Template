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
- **Run Scriptが呼ぶGradleタスク**: `:composeApp:app:embedAndSignAppleFrameworkForXcode`（`iosApp` ターゲットのビルドフェーズから `./gradlew` 経由で呼ばれる。Gradleは直列で1つだけ実行する）
- **フレームワーク出力パス**: `composeApp/app/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`
- **フレームワーク単体確認**: `./gradlew :composeApp:app:linkDebugFrameworkIosSimulatorArm64`

## 前提条件

- macOS環境
- Xcode がインストールされている
- `xcrun` および `xcodebuild` コマンドが利用可能

## タイムアウト方針

macOS標準には `timeout` コマンドが無い。**すべての `xcrun simctl` 呼び出しにタイムアウトを付ける**ため、次のいずれかを使う:

- `perl -e 'alarm <秒>; exec @ARGV' xcrun simctl ...`
- `gtimeout <秒> xcrun simctl ...`（Homebrewの`coreutils`導入済みの場合のみ利用可能）

**exit code 142 はタイムアウト（SIGALRMによる終了）と解釈する。**

推奨タイムアウト秒数:

| 操作 | 推奨秒数 |
|------|----------|
| `simctl boot` | 90 |
| `simctl bootstatus` | 120 |
| `simctl install` | 180 |
| `simctl launch` | 45 |
| `simctl io ... screenshot` | 30 |
| `simctl spawn ... log` | 60 |

上記以外の軽量な参照系コマンド（`simctl list` など）にも同様にタイムアウトを付ける。

## 実行手順

### 0. 実行モードの判定

このスキルがdebug-runスキルから呼び出されたかどうかを判定する。
判定方法: 環境変数 `CALLED_FROM_DEBUG_RUN` が設定されているかチェック。

- 設定されている場合: **JSON出力モード** （マークダウン出力なし）
- 設定されていない場合: **マークダウン出力モード** （ユーザー向け出力）

### 1. 起動中のシミュレーターを検出

```bash
perl -e 'alarm 30; exec @ARGV' xcrun simctl list devices booted 2>/dev/null | grep -E "iPhone|iPad"
```

**検証**:
- 起動中のシミュレーターが見つからない場合:
  - **`CALLED_FROM_DEBUG_RUN` が設定されている場合**: 親スキルで起動確認済みのため、エラーとして報告して終了
  - **単体実行の場合**: 利用可能なシミュレーター一覧を取得:
    ```bash
    perl -e 'alarm 30; exec @ARGV' xcrun simctl list devices available | grep -E "iPhone|iPad"
    ```
    - 利用可能なシミュレーターが0件の場合は終了
    - ある場合は `AskUserQuestion` でユーザーに選択させ、起動:
      ```bash
      perl -e 'alarm 90; exec @ARGV' xcrun simctl boot <simulator-uuid> && open -a Simulator
      ```
      起動完了は待ちコマンドで確認する:
      ```bash
      perl -e 'alarm 120; exec @ARGV' xcrun simctl bootstatus <simulator-uuid> -b
      ```

### 2. SDKとシミュレーターランタイムの整合性を確認

ビルド前に、Xcodeが要求するSDKビルド番号と、インストール済みシミュレーターランタイムのビルド番号を突き合わせる:

```bash
xcrun --sdk iphonesimulator --show-sdk-build-version
```
（例: `23E252`）

```bash
xcrun simctl runtime list
```

**検証**:
- SDKビルド番号以上のビルド番号を持つランタイムが存在しない場合、ビルドは `CompileAssetCatalogVariant` ステップで
  `No simulator runtime version ... available to use with iphonesimulator SDK version ...` エラーにより失敗する。
- この場合、`xcodebuild -downloadPlatform iOS` の実行可否をユーザーに確認する。**このコマンドは数GBの外部通信を伴い、10分以上かかる**。ユーザーの明示的な許可なしに実行しない。
- 同一識別子のランタイムが2つ以上並んでいる場合、`simctl install`/`simctl launch` がハングする原因になり得る。古い方のランタイムの削除を検討するようユーザーに注意喚起する。

### 3. シミュレーターUUIDを取得

```bash
perl -e 'alarm 30; exec @ARGV' xcrun simctl list devices booted | grep -E "iPhone|iPad" | grep -oE '\([A-F0-9-]+\)' | tr -d '()' | head -1
```

### 4. ビルド

xcodebuildの呼び出しは次の形式に固定する。**generic destination（`generic/platform=iOS Simulator`）は使わない**（Compose Multiplatform 1.11以降、iOSシミュレーター向けターゲットにx86_64が含まれないため、genericでは解決に失敗し得る）。

```bash
start_time=$(date +%s)

xcodebuild -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination 'id=<起動中のsimulator-uuid>' \
  -derivedDataPath <スクラッチパッド>/DerivedData \
  CODE_SIGNING_ALLOWED=NO \
  CODE_SIGNING_REQUIRED=NO \
  build 2>&1

exit_code=$?
end_time=$(date +%s)
build_time=$((end_time - start_time))
```

このビルドはRun Script経由で `./gradlew :composeApp:app:embedAndSignAppleFrameworkForXcode` を呼び出す（Gradleは直列で1つだけ実行すること）。フレームワーク単体の確認が必要な場合は `./gradlew :composeApp:app:linkDebugFrameworkIosSimulatorArm64` を使う。

**ビルドは10分以上かかることがあるため、バックグラウンド実行で待機し、完了してから後続処理に進む**（Claude CodeのBashツールの `run_in_background` を使う）。

**ビルド失敗時**: エラーハンドリングセクションに従ってエラーを解析

### 5. 署名とインストール

`CODE_SIGNING_ALLOWED=NO` でビルドした `.app` は未署名のため、そのまま `simctl install` するとインストールがハングすることがある。**インストール前に必ずad-hoc署名を行う**:

```bash
app_path=$(find <スクラッチパッド>/DerivedData -path "*/Debug-iphonesimulator/StarterProject.app" -maxdepth 5 2>/dev/null | head -1)
codesign --force --deep --sign - "$app_path"
```

署名後、タイムアウト付きでインストールする:

```bash
perl -e 'alarm 180; exec @ARGV' xcrun simctl install <simulator-uuid> "$app_path"
install_exit=$?
```

`install_exit` が `142` の場合はタイムアウト。トラブルシューティングセクションの復旧手順に従う。

### 6. アプリ起動とランタイムエラー監視

**アプリを起動**（`simctl launch` はハングし得るためタイムアウトを付ける）:
```bash
perl -e 'alarm 45; exec @ARGV' xcrun simctl launch <simulator-uuid> org.starter.project.StarterProject
launch_exit=$?
```
`launch_exit` が `142` の場合はタイムアウト。トラブルシューティングセクションの復旧手順に従う。

**起動確認（ホスト側プロセス生存確認）**:
シミュレーター上のアプリはホストのプロセスとして観測できるため、`simctl launch` が正常終了してもホスト側で生存確認する:
```bash
ps -eo pid,etime,command | grep "[S]tarterProject.app/StarterProject"
```

**ログ確認**:
```bash
perl -e 'alarm 60; exec @ARGV' xcrun simctl spawn <simulator-uuid> log show --last 2m --style compact --predicate 'process == "StarterProject"'
```
出力から `Exception`、`Uncaught Kotlin exception`、`JsonConvertException` をgrepし、ランタイムエラーの有無を判定する。

**スクリーンショット確認**:
```bash
perl -e 'alarm 30; exec @ARGV' xcrun simctl io <simulator-uuid> screenshot <path>
```
撮影した画像は `Read` ツールで目視確認し、正常な画面かエラー画面かを判定する（結果テーブルの `Loading` 列に使用）。
**真っ黒な画面の場合**は起動処理がフリーズしている可能性があるため、メインスレッドの状態を採取する:
```bash
sample <pid> 3
```

## 出力フォーマット

このスキルは、単体実行時とdebug-runから呼び出された時で異なる出力を行う。

### 単体実行時: ユーザー向けマークダウン出力

単体で実行された場合、ユーザーフレンドリーなマークダウン形式で出力する。

**成功時**:

```markdown
## Build & Install Results

| Platform | Device | Build | Launch | Loading | Build Time |
|----------|--------|-------|--------|---------|------------|
| iOS      | <simulator-name> | Success | Success | OK | <XX>s |
```

**ビルド失敗時**:

```markdown
## Build & Install Results

| Platform | Device | Build | Launch | Loading | Build Time |
|----------|--------|-------|--------|---------|------------|
| iOS      | <simulator-name> | Failed | N/A | N/A | <XX>s |

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

| Platform | Device | Build | Launch | Loading | Build Time |
|----------|--------|-------|--------|---------|------------|
| iOS      | <simulator-name> | Success | Failed | Error | <XX>s |

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
  "loading": "OK",
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
  "loading": "N/A",
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
  "loading": "Error",
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
perl -e 'alarm 60; exec @ARGV' xcrun simctl spawn <simulator-uuid> log show --predicate 'processImagePath contains "StarterProject"' --last 10s --style compact
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

## トラブルシューティング

`simctl launch` / `simctl install` がタイムアウト（exit code 142）した場合、以下の順で復旧する:

1. `pkill -f "simctl (install|launch|spawn)"` — ハングしているsimctlプロセスを終了
2. `xcrun simctl shutdown <simulator-uuid>` — シミュレーターをシャットダウン
3. `killall -9 com.apple.CoreSimulator.CoreSimulatorService` — CoreSimulatorサービスを再起動
4. シミュレーターを再度boot

それでも改善しない場合は、ランタイムの重複（同一識別子のランタイムが2つ以上インストールされている状態）を疑い、別のランタイムに紐づくデバイスで試す。
