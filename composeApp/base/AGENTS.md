# composeApp/base

共有カーネルモジュール。domain モデル・共通エラー型・拡張関数のみを置く、全モジュールが依存してよい最下層。

本ファイルはこのモジュールで作業する実装エージェントの入口である。ルートの [AGENTS.md](../../AGENTS.md)（絶対ルール・実行体制）を前提とし、ツールが自動で読み込まない場合は先に読む。実装エージェントは本モジュール外のファイルを編集せず、必要な他モジュールの変更は「他モジュールとの接点」に沿ってオーケストレーターへ報告する。読み順は本ファイル → 「参照」節の docs。

## 責務

- **置く**: domain モデル（`data/model/<feature>/` 配下、`@Immutable data class`）、共通エラー型（`ApiError`/`ConversionError`、いずれも `Throwable` を継承する `sealed class`）、汎用拡張関数（`validateNotNull`、`Result<T>.handle` など）
- **置かない**: DTO（`kotlinx.serialization` 型。domain モデルとは常に別型にする）、ビジネスロジック（Repository/Service の実装）、UI コンポーネント
- **置かない**: 1 モジュールでしか使わない型・関数。2 箇所以上から参照される場合のみここに昇格させる

## 依存

- **必須** 他モジュールへの依存を持たない。`base` は全モジュールが依存してよい最下層（→ design-guide.md §1）。
- convention plugin は `kmp-compose-library`（例外的適用。`@Immutable` 利用のため。UI 自体は持たない）。`kotlin.serialization` プラグインも適用する（`ApiErrorResponse` の `@Serializable` のため）。
- `commonMain.dependencies` は `api(libs.bundles.base)` のみ（koin/coroutines/napier/compose-foundation・ui/lifecycle-viewmodel-compose を含む）。`projects.composeApp.*` への依存は持たない。

## 構成

```
composeApp/base/src/commonMain/kotlin/org/starter/project/base/
  data/model/zenn/
    Articles.kt              # Articles, Article（ネスト User を含む）
    User.kt                  # User domain モデル
  error/
    ApiError.kt              # ApiError sealed class、ApiErrorResponse（@Serializable）
    ConversionError.kt       # ConversionError sealed class
  extension/
    ConversionErrorExtension.kt  # validateNotNull
    ResultExtension.kt           # Result<Success>.handle
```

`data/model/zenn/` はサンプル実装（Zenn ビューワー）。新しい機能ドメインを追加する場合は `data/model/<feature>/` を新設する。

## 実装パターン

domain モデル（coding-guide.md §1 の命名規則に従う）と共通エラー型（`ApiError`/`ConversionError` に倣う。汎用 HTTP 分類の拡張は `ApiError` に、ビジネス固有エラーは新設の `sealed class` にする）:

```kotlin
package org.starter.project.base.data.model.<feature>

@Immutable
data class XxxModel(val id: Int, val name: String)
```

```kotlin
package org.starter.project.base.error

sealed class XxxError(override val message: String) : Throwable() {
    class SomeCase(message: String) : XxxError(message)
}
```

## テスト

- テスト必須範囲（Converter/Repository 実装/Service 実装/ViewModel）に含まれない（→ coding-guide.md §5）。`commonTest` は現状無いが、独立したロジックを持つ拡張関数を追加した場合は `src/commonTest/kotlin/org/starter/project/base/<ミラーパッケージ>/XxxTest.kt` にテストを書いてよい。
- 実行コマンド: `./gradlew :composeApp:base:testAndroidHostTest`（モジュール単位）。

## 他モジュールとの接点

- domain モデルの追加・変更は `data:<source>` の Converter と `domain:service` の Service interface の戻り値型に影響する。エラー型の追加は `core`（`ApiClientImpl`）や `ui`（`ThrowableHandler`）に影響する場合がある。実装エージェントは自モジュール外を編集せず、この影響範囲を完了報告に申し送り事項として記載する。

## 完了条件

- 追加した型が 2 箇所以上から参照される共通要素であることを確認した。domain モデルに `kotlinx.serialization` の import が無いことを確認した。
- `projects.composeApp.*` への依存を追加していないこと、`commonTest` 追加時は `./gradlew :composeApp:base:testAndroidHostTest` が通ることを確認した。
- `../../docs/coding-guide.md` §8 の既知の逸脱を新規コードに複製していない。

## 参照

- ../../docs/design-guide.md §1, §3, §6
- ../../docs/coding-guide.md §1
- ../../docs/decisions.md D-03, D-04
