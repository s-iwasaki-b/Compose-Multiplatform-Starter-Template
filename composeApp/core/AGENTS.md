# composeApp/core

インフラ層モジュール。Ktor による `ApiClient`（HTTP 通信）と `PreferencesConfig`（ローカル設定の名前空間管理）を置く。

本ファイルはこのモジュールで作業する実装エージェントの入口である。ルートの [AGENTS.md](../../AGENTS.md)（絶対ルール・実行体制）を前提とし、ツールが自動で読み込まない場合は先に読む。実装エージェントは本モジュール外のファイルを編集せず、必要な他モジュールの変更は「他モジュールとの接点」に沿ってオーケストレーターへ報告する。読み順は本ファイル → 「参照」節の docs。

## 責務

- **置く**: `ApiClient` interface と `ApiClientImpl`（`HttpClient` 生成、`ContentNegotiation`/`Logging`/`HttpResponseValidator` の設定を1箇所に集約）、HTTP エンジンの `expect`/`actual`（Android は `Android`、iOS は `Darwin`）、`ApiConfig`（baseUrl 等の設定値、`internal object`）
- **置く**: `PreferencesConfig`（`Settings.Factory` から名前空間ごとの `Settings` を生成）、`Preference`（設定キーの enum）
- **置かない**: domain モデル、DTO、ビジネスロジック（Repository/Service の実装）、個別の API 呼び出し（Ktorfit interface は `data:<source>` に置く）

## 依存

- **必須** 依存してよいのは `base` のみ（→ design-guide.md §1）。
- convention plugin は `kmp-library`（Compose UI/Resources を持たないため）。
- `commonMain.dependencies`: `api(projects.composeApp.base)`、`api(libs.bundles.core)`（ktorfit/ktor-client-content-negotiation/ktor-serialization-kotlinx-json/ktor-client-logging/multiplatform-settings）。
- `androidMain.dependencies`: `implementation(libs.ktor.client.android)`。`iosMain.dependencies`: `implementation(libs.ktor.client.darwin)`（いずれも `expect val engine` の `actual` 実装用）。

## 構成

```
composeApp/core/src/
  commonMain/kotlin/org/starter/project/core/
    api/
      ApiClient.kt        # ApiClient interface, ApiClientImpl, expect val engine
      ApiConfig.kt         # internal object ApiConfig（API_BASE_URL）
    preferences/
      PreferencesConfig.kt # 名前空間ごとの Settings 集約
      PreferencesKey.kt    # Preference enum（設定キー）
  androidMain/kotlin/org/starter/project/core/api/
    ApiClient.android.kt   # actual engine = Android
  iosMain/kotlin/org/starter/project/core/api/
    ApiClient.ios.kt       # actual engine = Darwin
```

## 実装パターン

HTTP エンジンの `expect`/`actual`（新しいターゲットを追加する場合のみ変更）と Preferences の名前空間追加（設定キーは `Preference` に追記する）:

```kotlin
// commonMain
internal expect val engine: HttpClientEngineFactory<HttpClientEngineConfig>
// androidMain(actual = Android) / iosMain(actual = Darwin)
```

```kotlin
class PreferencesConfig(factory: Settings.Factory) {
    val xxxPreferences: Settings = factory.create("xxx_preferences")
}

enum class Preference(val key: String) {
    LastKeyword("last_keyword"),
}
```

タイムアウト・リトライ・認証・キャッシュは要件が出るまで追加しない（→ decisions.md D-13）。追加する場合は `ApiClientImpl` の `HttpClient { }` ブロックに `install` を1箇所だけ足す。

## テスト

- テスト必須範囲（Converter/Repository 実装/Service 実装/ViewModel）に含まれない（→ coding-guide.md §5）。`commonTest` は現状無い。
- 実行コマンド: `./gradlew :composeApp:core:testAndroidHostTest`（モジュール単位）。

## 他モジュールとの接点

- `ApiConfig` の baseUrl を変更、または別ホストの API に対応する場合、`ApiClientImpl` を `baseUrl` 引数化し Koin の `named("<host>")` で複数登録する対応が必要になる（現状未実装、→ decisions.md D-25）。実装した場合は `app` の `Koin.kt` の登録が対になる。新しい Preferences 名前空間・キーの追加は `data:<source>` の `XxxPreferencesImpl` に影響する。実装エージェントは自モジュール外を編集せず、この影響範囲を完了報告に申し送り事項として記載する。

## 完了条件

- 依存が `base` のみであることを確認した。`HttpClient` の生成・設定変更を `ApiClientImpl` の1箇所に閉じたことを確認した。
- `commonTest` を追加した場合 `./gradlew :composeApp:core:testAndroidHostTest` が通ることを確認した。
- `../../docs/coding-guide.md` §8 の既知の逸脱を新規コードに複製していない。

## 参照

- ../../docs/design-guide.md §1, §4, §6
- ../../docs/coding-guide.md §1, §2
- ../../docs/decisions.md D-13, D-25
