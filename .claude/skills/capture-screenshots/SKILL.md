---
name: capture-screenshots
description: Capture before/after screenshots of changed screens on the Android emulator and iOS simulator and produce the per-change comparison table for the PR body. Use when a PR changes any UI.
---

# UI変更のbefore/afterスクリーンショット取得とPR添付

オーケストレーター視点の手順。各ステップは委譲不要な軽量作業のため、自分で直接実行する。起動・ビルド手順は `debug-run-android`/`debug-run-ios` の該当ステップを流用し、同じ内容を書き写さない。

## 1. 対象の決定

差分（画面・状態）を洗い出す。`composeApp/app` や `ui` など共通コードの変更なら Android/iOS 両方、`androidApp`/`iosApp` 配下などプラットフォーム固有の変更なら該当側のみを対象にする。各対象に `<screen>-<state>`（例: `home-default`, `home-dark`）の識別子を付ける。ダークモードなど状態差は表の別行にする。

## 2. beforeの取得

作業ツリーを汚さないため、ベースブランチ（通常 `main`）用の別 worktree を作る（`git stash` は使わない）。

```bash
git worktree add <before-path> origin/main
```

その worktree に `cd` し、対象プラットフォームに応じて起動する:

- Android: `debug-run-android` の手順1（起動中エミュレーター検出）・手順2（`./gradlew :androidApp:installDebug`）を `<before-path>` から実行する。
- iOS: `debug-run-ios` の手順1（起動中シミュレーター検出）・手順3（UUID取得）・手順4（ビルド）・手順5（署名・インストール）を `<before-path>` から実行する。

起動後、目的の画面を deep link で開く（スキームは `composeApp/app/src/commonMain/kotlin/org/starter/project/navigation/DeepLinkConfig.kt` の `SCHEME`。本リポジトリでは `cmp-starter`、パスは `AppNavHost.kt` の `basePath` に対応。例: `home` → `AppRoute.Home`、`user` → `AppRoute.User`）:

```bash
# Android
adb -s <serial> shell am start -W -a android.intent.action.VIEW -d "cmp-starter://<path>"
# iOS
xcrun simctl openurl <udid> "cmp-starter://<path>"
```

`<serial>` は `adb devices`（debug-run-android 手順1）、`<udid>` は `xcrun simctl list devices booted`（debug-run-ios 手順3）の出力から取る。

スクリーンショットを撮る:

```bash
adb -s <serial> exec-out screencap -p > <dir>/<id>-android-before.png
xcrun simctl io <udid> screenshot <dir>/<id>-ios-before.png
```

`<dir>` はセッションのスクラッチパッド（無ければ `mktemp -d`）配下に作る作業用ディレクトリ。個人情報や実データが写る画面はダミーデータで撮る。

## 3. afterの取得

現在の作業ツリー（PRブランチ）で同じデバイス・同じ画面・同じ状態・同じ向きで撮り直す。手順は手順2と同じで、`<before-path>` の代わりに通常の作業ディレクトリからビルド・起動し、ファイル名だけ `-after.png` にする。解像度・向きは before と揃える。

## 4. 保存先とURL

PR本文から参照できる恒久URLが要る。ディレクトリ名に PR 番号を使うため、未起票なら先に PR（draft 可）を起票して番号を確定する。**推奨**: 画像専用の orphan ブランチ `screenshots` に `pr-<番号>/` ディレクトリで push し、`https://raw.githubusercontent.com/<owner>/<repo>/screenshots/pr-<番号>/<file>.png` で参照する。`<owner>/<repo>` は `gh repo view --json nameWithOwner -q .nameWithOwner` で取得。

初回（`screenshots` ブランチが無い場合）:

```bash
git worktree add --detach <ss-path>
cd <ss-path> && git checkout --orphan screenshots
git rm -rf . 2>/dev/null || true
git commit --allow-empty -m "chore: initialize screenshots branch"
git push -u origin screenshots
```

2回目以降:

```bash
git worktree add <ss-path> origin/screenshots
cd <ss-path>
mkdir -p pr-<番号> && cp <dir>/*.png pr-<番号>/
git add pr-<番号> && git commit -m "docs(screenshots): add PR #<番号> screenshots"
git push
```

代替: PRブランチ内に直接コミットしてもよい。ただしマージ後もリポジトリに画像ファイルが残り続ける点に注意。

## 5. 表の作成と添付

修正箇所×プラットフォームごとに1行、`## Verification` に表を入れる。`![]()` は原寸表示になるため `<img>` で幅を揃える。

```markdown
| 修正箇所 | Before | After |
|---|---|---|
| home-android | <img src="https://raw.githubusercontent.com/<owner>/<repo>/screenshots/pr-<番号>/home-android-before.png" width="300"> | <img src="https://raw.githubusercontent.com/<owner>/<repo>/screenshots/pr-<番号>/home-android-after.png" width="300"> |
| home-ios | <img src="https://raw.githubusercontent.com/<owner>/<repo>/screenshots/pr-<番号>/home-ios-before.png" width="300"> | <img src="https://raw.githubusercontent.com/<owner>/<repo>/screenshots/pr-<番号>/home-ios-after.png" width="300"> |
```

反映:

```bash
gh pr edit <番号> --body-file <file>
# gh pr edit が Projects classic の GraphQL エラーで失敗する場合の代替
gh api -X PATCH repos/<owner>/<repo>/pulls/<番号> -F body=@<file>
```

## 6. 後片付けと確認

- 一時 worktree を削除する: `git worktree remove <before-path>`、`git worktree remove <ss-path>`。
- PR本文の画像がブラウザで表示されることを確認する。
- 表の行数が「修正箇所数 × 対象プラットフォーム数」と一致することを確認する。
