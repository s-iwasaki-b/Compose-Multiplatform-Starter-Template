# composeApp/data/zenn

`data/<source>` 系モジュール一般の書き方を示す。具体名は本モジュールのディレクトリと `build.gradle.kts` を見る。本ファイルはこのモジュールで作業する実装エージェントの入口である。ルートの [AGENTS.md](../../../AGENTS.md)（絶対ルール・実行体制）を前提とし、ツールが自動で読み込まない場合は先に読む。実装エージェントは本モジュール外のファイルを編集せず、必要な他モジュールの変更は「他モジュールとの接点」に沿ってオーケストレーターへ報告する。読み順は本ファイル → 「参照」節の docs。

## 責務

- 置くもの: Ktorfit API interface（`datasource/api`）、レスポンス DTO（`datasource/api/response`）、ローカル設定（`datasource/preferences`）、DTO→domain モデル変換（`converter`）、Repository 実装（`repository`）。
- 置かないもの: Repository interface（`data/repository` に置く）、Service（`domain/service`・`domain/<name>`）、Koin 登録（`app`）、Paging3 の `PagingSource`（`ui` に置く。詳細は実装パターン参照）。

## 依存

```kotlin
plugins {
    id("kmp-library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.mokkery)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.data)

            api(projects.composeApp.base)
            implementation(projects.composeApp.core)
            implementation(projects.composeApp.data.repository)
        }
        commonTest.dependencies {
            implementation(libs.bundles.test)
        }
    }
}
```

- **必須** DTO を持つ場合は `jetbrains.kotlin.serialization`、Ktorfit を持つ場合は `ksp`+`ktorfit`、テストを持つ場合は `mokkery` を適用する。
- **必須** `base` は `api`（domain モデル型が公開シグネチャに登場するため）、`core`・`data:repository` は `implementation` で依存する。`domain:*` には依存しない。

## 構成

```
src/commonMain/kotlin/org/starter/project/data/<source>/
  datasource/api/XxxApi.kt                     # Ktorfit interface（実装は書かない）
  datasource/api/response/XxxListResponse.kt   # 一覧 DTO（ラッパー + 要素 + ネスト）
  datasource/api/response/XxxResponse.kt       # 単体 DTO
  datasource/preferences/XxxPreferences.kt     # interface + Impl
  converter/XxxListConverter.kt                # 一覧変換（mapNotNull）
  converter/XxxConverter.kt                    # 単体変換
  repository/XxxRepositoryImpl.kt              # XxxRepository の実装
src/commonTest/kotlin/org/starter/project/data/<source>/  # 上記とミラーのパッケージ
```

## 実装パターン

Ktorfit API/DTO/Converter/Repository の一般的な型テンプレは重複させず [coding-guide.md](../../../docs/coding-guide.md) §2 を参照。本節はこのモジュール固有の一覧 API パターンのみ示す: レスポンスをラッパー型にし、要素は `mapNotNull` + `ConversionError` 捕捉で1件ずつ変換失敗を許容する。単体 API は `validateNotNull` の例外をそのまま伝播させる。具体名は本モジュールのディレクトリと `build.gradle.kts` を見る。

```kotlin
object XxxListConverter {
    operator fun invoke(response: XxxListResponse): XxxList {
        return XxxList(
            items = response.items?.mapNotNull {
                try { createXxx(it) } catch (e: ConversionError) { null }
            }.orEmpty(),
            nextPage = response.nextPage
        )
    }
}
```

**PagingSource の置き場所**: 本モジュールは単発取得＋カーソルのみ提供する。`Pager`/`PagingSource` の組み立ては行わず `ui` に置く（→ decisions.md D-05）。

## テスト

- 対象: Converter（一覧変換の1件失敗許容、単体変換の例外伝播）と Repository 実装（API/Preferences をモック）。`src/commonTest` にソースとミラーのパッケージ構成で置く。
- Mokkery: モック対象は interface（`mock<XxxApi>()`）。「呼ばれたこと」だけ検証するメソッドがあれば `mock<XxxPreferences>(MockMode.autofill)` にする。スタブは `every`/`everySuspend { } returns/throws`、副作用の検証は `verify(VerifyMode.exactly(n)) { }`。
- 実行コマンド: モジュール単位 `./gradlew :composeApp:data:<source>:testAndroidHostTest`、全体 `./gradlew testAndroidHostTest`（Gradle 自体はここでは実行しない）。

## 他モジュールとの接点

新しい `data/<name>` モジュールを作る、またはこのモジュールに新しいエンドポイントを追加した実装エージェントは、次をオーケストレーターへの完了報告に含める（自モジュール外は編集しない）。

- Repository interface（新規/追記）が `data/repository` に必要。domain モデルが `base` に無ければ追加が必要。
- ローカル設定の新しいキーは `core` の `Preference` enum・`PreferencesConfig` への追記、`app` の Koin `dataSourceModule`/`repositoryModule` への登録が必要。
- 新規モジュールの場合は `settings.gradle.kts` に `include(":composeApp:data:<name>")` を明示的に追加する必要がある（動的 include ではない）。`app/build.gradle.kts` への依存追加も必要。`composeApp/build.gradle.kts`（集約シェル）は動的スキャンのため編集不要。
- ページング対応の画面が必要なら `ui` に `PagingSource` の追加が必要。

## 完了条件

- `./gradlew :composeApp:data:<source>:testAndroidHostTest` 相当のテストコマンドで検証済み。
- DTO が全フィールド `Xxx? = null` になっている。
- Repository/Converter（一覧変換以外）が例外を透過し、`PagingSource` を本モジュールに追加していない。
- 「他モジュールとの接点」の項目をオーケストレーターへの報告に含めた。
- `../../../docs/coding-guide.md` §8 の既知の逸脱を新規コードに複製していない。

## 参照

- ../../../docs/design-guide.md §3（層の境界と実装粒度: Repository の例外透過・粒度）
- ../../../docs/coding-guide.md §2（data 層の共通実装パターン: DTO/Converter/Repository の型テンプレ）
- ../../../docs/decisions.md D-02（Repository interface を data 層に置く理由）、D-05（ページング組み立ての責務分離）
