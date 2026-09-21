# モジュールガイド

> 対象: 各モジュールの責務カタログ、新規モジュール追加手順、convention plugin 選択基準、namespace/package 規則、バージョンカタログ運用、`composeApp` 動的スキャンの制約、`api`/`implementation` の使い分け
> 関連: [overview.md](overview.md), [dependency-injection.md](dependency-injection.md), [../guides/coding-style.md](../guides/coding-style.md), [../decisions/0007-stable-dependencies.md](../decisions/0007-stable-dependencies.md), [../decisions/0009-module-granularity.md](../decisions/0009-module-granularity.md)
> 最終確認コミット: ac56102

## 要点

- 新規モジュールは `settings.gradle.kts` への `include` → `build.gradle.kts` 作成 → ソース配置 → `composeApp/app/build.gradle.kts` への依存追加 → `Koin.kt` 登録、の順で追加する。`composeApp`（集約シェル）自体は編集不要。
- convention plugin は「Compose UI または Compose Resources を使うか」で機械的に選ぶ（`kmp-compose-library` / `kmp-library`）。`base` はこの原則の唯一の例外。
- namespace は `deriveNamespace(project)` が `org.starter.project.<layer>.<name>` を自動導出する。ソースパッケージもこれに一致させる。`composeApp:app` のみ関心事別パッケージ分割が許される例外。
- バージョンカタログの `[bundles]` は `base`/`core`/`data`/`domain`/`ui`/`test` のレイヤー単位で定義する。新しい `data:<source>`/`domain:<name>` を追加しても新規 bundle は作らず、既存の `libs.bundles.<layer>` を再利用する。ライブラリバージョンは安定版優先。
- `composeApp/build.gradle.kts` の動的スキャンは `maxDepth=3` の制約があり、モジュール階層は `composeApp/<layer>/<name>` までに収める。
- Compose Resources（`composeResources/`）を自モジュールに持つ場合、`ui` 経由で transitively 利用可能でも **`compose-components-resources` への直接依存が必須**。省略するとビルドが壊れる。
- 契約モジュール（`data:repository`, `domain:service`）は常に単一。新しい外部リソースが増えても分割せず、interface を追記する。実装モジュール（`data:<source>`, `domain:<name>`）だけが外部リソース単位で増える。

## モジュール責務カタログ

| モジュール | ディレクトリ | 役割 | convention plugin |
|---|---|---|---|
| `:androidApp` | `androidApp/` | Android エントリポイント（`Application`/`MainActivity` のみ） | `project` + `androidApplication`(AGP) + `jetbrainsCompose` + `compose.compiler` |
| `:composeApp`（集約シェル） | `composeApp/` | ソースを持たない。配下の全サブモジュールを動的スキャンして `api` 依存に束ねる | `kmp-compose-library` |
| `:composeApp:app` | `composeApp/app/` | 共有アプリ層。起動 Composable（`Main()`）、DI 起動（`Koin.kt`）、Navigation（`AppNavHost`/`AppRouter` 実装）、iOS エントリ（`MainViewController`）。iOS フレームワークもここから生成 | `kmp-compose-library` + `serialization` |
| `:composeApp:base` | `composeApp/base/` | 共有カーネル。domain モデル、共通エラー型（`ApiError`/`ConversionError`）、拡張関数 | `kmp-compose-library`（例外、後述） + `serialization` |
| `:composeApp:core` | `composeApp/core/` | インフラ層。`ApiClient`（Ktor、expect/actual）、`PreferencesConfig` | `kmp-library` |
| `:composeApp:data:repository` | `composeApp/data/repository/` | Repository interface のみを持つ契約モジュール | `kmp-library` |
| `:composeApp:data:<source>` | `composeApp/data/<source>/` | 外部データソース（API/DB 等）1つ分の実装（サンプル: `data/zenn`。削除後は存在しない） | `kmp-library` + 必要なプラグイン |
| `:composeApp:domain:service` | `composeApp/domain/service/` | Service interface + `ResultHandler` を持つ契約モジュール | `kmp-library` + `mokkery` |
| `:composeApp:domain:<name>` | `composeApp/domain/<name>/` | Service 実装（サンプル: `domain/zenn`。削除後は存在しない） | `kmp-library` + `mokkery` |
| `:composeApp:feature:<name>` | `composeApp/feature/<name>/` | 1つの機能ドメイン（ユーザーフロー）の画面一式（サンプル: `feature/home`, `feature/user`。削除後は存在しない） | `kmp-compose-library` |
| `:composeApp:ui` | `composeApp/ui/` | 共有 UI 部品（デザインシステム、共通コンポーネント）、`AppRoute`/`AppRouter` interface、エラーハンドラ | `kmp-compose-library` + `serialization` |

## ルール

- **MOD-1（必須）**: 新規モジュールは以下の6手順で追加する（詳細は「実装パターン」参照）。
  1. `settings.gradle.kts` に `include(":composeApp:<layer>:<name>")` を追加する。
  2. `composeApp/<layer>/<name>/build.gradle.kts` を作成する（テンプレートは下記）。
  3. `src/commonMain/kotlin/org/starter/project/<layer>/<name>/...` にソースを配置する。
  4. 他モジュール（通常は `composeApp/app`）から利用する場合、その `build.gradle.kts` の `commonMain.dependencies` に追記する。
  5. DI が必要なら `composeApp/app/src/commonMain/kotlin/org/starter/project/di/Koin.kt` に登録する（詳細は [dependency-injection.md](dependency-injection.md)）。
  6. `composeApp`（集約シェル）自体は編集しない。`maxDepth=3` の制約内であれば動的スキャンが自動検出する。
- **MOD-2（必須）**: convention plugin は「そのモジュールが Compose UI（`@Composable` 関数）または `compose.resources` を使うか」で選ぶ。使う場合は `kmp-compose-library`、使わない場合は `kmp-library`。
- **MOD-3（必須）**: `base` は `kmp-compose-library` を適用する唯一の例外。`base` 自身は `@Composable` 関数を持たないが、domain モデルに付与する `@Immutable`（`androidx.compose.runtime.Immutable`、`compose-runtime` 由来）を使うために Compose Gradle プラグインの適用とランタイム依存が必要であり、そのために `kmp-compose-library` を使う。`libs.bundles.base` が `api` 公開する `compose-foundation`/`compose-ui` は Compose UI の型を公開シグネチャに含める場合向けの依存であり、`@Immutable` 自体の直接の提供元ではない。
- **MOD-4（必須）**: モジュールのソースコードのパッケージ宣言は `deriveNamespace(project)` が導出する namespace（`org.starter.project.<layer>.<name>`）と一致させる。
- **MOD-5（必須）**: `composeApp:app` のみ例外的に namespace（`org.starter.project.app`）と異なる関心事別パッケージ分割（`app`/`navigation`/`di`/`log`）を許可する。DI・Navigation・エントリポイントという複数の横断的関心事を集約する特殊なモジュールのため。
- **MOD-6（必須）**: `gradle/libs.versions.toml` の `[bundles]` はレイヤー単位（`base`/`core`/`data`/`domain`/`ui`/`test`）で定義する。同じレイヤーに属する新規モジュール（例: 新しい `data:<source>`）は既存の `libs.bundles.<layer>` をそのまま再利用し、モジュールごとに新規 bundle を作らない（`domain` のように中身が空の bundle でもよい。`composeApp/domain/zenn/build.gradle.kts` の `libs.bundles.domain` が実例）。
- **MOD-7（必須）**: 新規ライブラリを追加する際は `[versions]`/`[libraries]` の既存カテゴリコメント（`# Build tools`, `# AndroidX`, `# DI`, `# Networking`, `# UI`, `# Utilities / Storage`, `# Logging / Testing` 等）のいずれかに追記する。どれにも当てはまらない新カテゴリの場合のみ新しいコメント見出しを追加する。
- **MOD-8（必須）**: バージョン更新は安定版（stable）を優先する。Alpha/Beta/RC 版は採用しない（[../decisions/0007-stable-dependencies.md](../decisions/0007-stable-dependencies.md)）。
- **MOD-9（必須）**: `composeApp/<layer>/<name>`（2階層）を超える階層を作らない。`maxDepth=3` の動的スキャンは3階層目（例: `composeApp/feature/user/detail`。実在しないパス）までは検出できるが、本テンプレートは検出可否とは別に「2階層まで」を設計方針として定める（判断基準参照）。4階層目以降は自動検出から漏れる。
- **MOD-10（必須）**: モジュール内で `composeResources/` ディレクトリ（`values/string.xml` 等）を自ら持つ場合、`commonMain.dependencies` に `implementation(libs.compose.components.resources)`（または `compose-components-resources` を含む bundle）を明示的に追加する。他モジュールの `api` 依存を通じて transitively 利用可能な場合でも省略しない。
- **MOD-11（必須）**: 他モジュールから `Res` クラスを参照させたい共有モジュール（例: `ui`）は `compose.resources { publicResClass = true }`、自モジュール内でのみ使う場合（`feature:<name>` 等）は `false` にする。`packageOfResClass` は convention plugin が自動設定するため手動指定しない。
- **MOD-12（必須）**: 契約モジュール（`data:repository`, `domain:service`）は常に単一モジュールとして維持する。新しい外部リソースが増えても分割せず、既存の契約モジュールに interface を追記する。
- **MOD-13（必須）**: 実装モジュール（`data:<source>`, `domain:<name>`）は外部リソース（API/サービス）単位で追加する。1つの `feature:<name>` に対して必ず1組の `data:<source>`/`domain:<name>` を新設するわけではない（判断基準は後述）。
- **MOD-14（必須）**: `api` は依存先の型が自モジュールの公開シグネチャ（interface のメソッド戻り値・引数、Composable 関数の引数）にそのまま登場する場合に使う。それ以外は `implementation` を使い、依存の詳細を外部に漏らさない。

## 判断基準

### 新機能追加時のモジュール分割（A-7-1 / C-9-6 / D-G1）

| 状況 | 判断 |
|---|---|
| 新しい外部データソース（新しい API・新しいドメイン）を追加する | `data:<source>` と `domain:<name>` を新設する。対応する Repository/Service の interface は既存の `data:repository`/`domain:service` に追記する（MOD-12）。 |
| 既存の外部データソースを別画面で使うだけ | 新しい `data:<source>`/`domain:<name>` は作らない。既存の実装モジュールを再利用し、`feature:<name>` のみ追加または既存 `feature` にサブパッケージを追加する。 |
| 新しい画面が既存機能と同じユーザーフロー内（一覧→詳細等、強く関連する画面群） | 既存の `feature:<name>` モジュール内に `<screen>/` サブパッケージを追加し、そこに4点セット（Screen/State/Event/ViewModel）を置く。 |
| 新しい画面が独立した機能ドメイン（既存画面と遷移的に無関係） | 新しい `feature:<name>` モジュールを追加する。単一画面なら4点セットをパッケージ直下に置く（新規決定）。 |
| `domain/<name>` と `domain/service` のどちらに実装ロジックを書くか迷う | `domain/service` には interface と機能非依存の共通ユーティリティ（`ResultHandler` 等）のみを置く。機能固有の実装ロジックは必ず `domain/<name>` に置く。 |

この非対称性（`feature` は機能ドメイン単位、`data`/`domain` は外部リソース単位）は [../decisions/0009-module-granularity.md](../decisions/0009-module-granularity.md) の決定に従う。

### バージョン更新方針（A-7-4）

- 新しいバージョンが **stable** リリースの場合 → 通常の更新 PR として取り込む。
- 新しいバージョンが **Alpha/Beta/RC** の場合 → 採用しない。評価した場合はその判断根拠（採用しない理由）を PR 説明または ADR に記録してから見送る。Kotlin 2.4 Alpha 版の Swift Export や Navigation 3 のような Preview 機能は、CMP 1.12.0 との組み合わせで問題が出た実績があるため、安定版がリリースされるまで待つ。

### `publicResClass` の true/false 基準（A-7-6 / D-G8）

| 状況 | 判断 |
|---|---|
| 他モジュールから `Res` クラスを import させたい共有モジュール（例: `ui`） | `publicResClass = true` |
| `feature:<name>` のように自モジュール内でのみ `Res` を使う | `publicResClass = false` |
| モジュールが `composeResources/` を持つ（true/false どちらでも） | `commonMain.dependencies` に `compose-components-resources`（または含む bundle）への直接依存を追加する（MOD-10）。省略すると Res クラス生成に失敗しビルドが壊れる。 |

### `composeApp` 動的スキャンの制約（A-7-8）

`composeApp/build.gradle.kts` の `allSubProjects` は `projectDir.walk().maxDepth(3)` で `build.gradle.kts` を直接持つ配下ディレクトリを機械的に列挙し、`api` 依存に変換する。`maxDepth(n)` は起点（`composeApp`、深さ0）から `n` 世代下まで訪問するため、`composeApp/<layer>/<name>` までの2階層（深さ2）はもちろん、3階層目（深さ3。例: `composeApp/feature/user/detail`。実在しないパス）も技術的には検出される。

- `composeApp/<layer>/<name>` までの階層（`data:zenn` 等）は自動検出される。3階層目（例: `composeApp/feature/user/detail`）自体も `maxDepth=3` では検出されるが、本テンプレートは MOD-9 の設計方針として2階層までに統一しており、検出されるかどうかに関わらず作らない。
- これを超える4階層目（例: `composeApp/feature/user/detail/sub`。実在しない）を作ると `maxDepth=3` の自動検出から実際に漏れる。
- 深い階層のモジュールがどうしても必要な場合は `composeApp/build.gradle.kts` の `maxDepth` 引数を手動で見直す。ただし本テンプレートの既定方針は「`composeApp/<layer>/<name>` を超えない」（MOD-9）。

### namespace とソースパッケージの一致（A-7-9）

| モジュール | 判断 |
|---|---|
| `composeApp:app` 以外の全モジュール | ソースパッケージは `deriveNamespace(project)` の出力と厳密に一致させる（MOD-4）。convention plugin は Android `namespace`（R クラス生成用）のみ自動設定し、Kotlin ソースの実パッケージ宣言までは強制しないため、レビューで確認する。 |
| `composeApp:app` | 例外的に `app`/`navigation`/`di`/`log` のような関心事別パッケージ分割を許可する（MOD-5）。 |

## 実装パターン

### 新規モジュール追加手順

```kotlin
// 1. settings.gradle.kts
include(":composeApp:<layer>:<name>")
```

```kotlin
// 4. composeApp/app/build.gradle.kts の commonMain.dependencies に追記（利用する場合のみ）
commonMain.dependencies {
    // ...既存の依存...
    implementation(projects.composeApp.<layer>.<name>)
}
```

### `build.gradle.kts` テンプレート（`kmp-library` 版。Compose UI を持たないモジュール向け）

`composeApp/data/zenn/build.gradle.kts` を基に一般化したもの。

```kotlin
plugins {
    id("kmp-library")
    alias(libs.plugins.jetbrains.kotlin.serialization) // DTO のシリアライズが必要な場合のみ
    alias(libs.plugins.google.devtools.ksp)             // Ktorfit を使う場合のみ
    alias(libs.plugins.ktorfit)                          // Ktorfit を使う場合のみ
    alias(libs.plugins.mokkery)                          // commonTest を持つ場合のみ
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.data) // レイヤー単位の既存 bundle を再利用する（MOD-6）。domain:<name> なら libs.bundles.domain

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

### `build.gradle.kts` テンプレート（`kmp-compose-library` 版。Compose UI または Compose Resources を持つモジュール向け）

`composeApp/feature/user/build.gradle.kts` を基に一般化したもの。

```kotlin
plugins {
    id("kmp-compose-library")
}

kotlin {
    android {
        androidResources {
            enable = true // composeResources を使う場合のみ
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.composeApp.ui)
            implementation(projects.composeApp.domain.service)
            implementation(libs.compose.components.resources) // composeResources/ を自モジュールに置く場合は必須（MOD-10）
        }
    }
}

compose.resources {
    publicResClass = false // 他モジュールから Res を参照させたい共有モジュールのみ true
}
```

### バージョンカタログへの追加テンプレート

```toml
# gradle/libs.versions.toml

[versions]
# 既存カテゴリ（例: # Networking）に追記、または新規カテゴリを追加
<library>-version = "x.y.z"

[libraries]
<library> = { module = "<group>:<artifact>", version.ref = "<library>-version" }

[bundles]
<layer> = [ # 既存のレイヤー単位 bundle（base/core/data/domain/ui/test のいずれか）に追記する（MOD-6）。モジュール名の新規 bundle は作らない
    # ...既存のライブラリ...
    "<library>",
]
```

## アンチパターン

- **feature モジュールを画面1つにつき機械的に新設する** → NG。ビルド設定の重複と Gradle 構成コストが増える。同一ユーザーフロー内の関連画面は既存 `feature:<name>` のサブパッケージにまとめる。
- **新しい外部データソースを追加するたびに `data:repository`/`domain:service` を分割する** → NG。契約モジュールは常に単一（MOD-12）。
- **`ui` を経由して transitively 利用可能だからと `compose-components-resources` への直接依存を省略する** → NG。`composeResources/` を自モジュールに持つ場合は直接依存が必須（MOD-10）。ビルド失敗の主要因。
- **`composeApp:app` 以外のモジュールで namespace と異なるパッケージ構成にする** → NG。`composeApp:app` だけの例外（MOD-5）を一般化しない。
- **Alpha/Beta 版ライブラリを通常の更新 PR と同じ扱いで取り込む** → NG。評価ログを残した上で見送るのが既定（MOD-8）。
- **`composeApp/<layer>/<name>` の2階層を超えるモジュール階層（例: `composeApp/feature/user/detail`。実在しないパス）を作る** → NG。`maxDepth=3` では3階層目自体は検出されるが、本テンプレートの方針（ARCH-8, MOD-9）に反する。さらに4階層目（例: `composeApp/feature/user/detail/sub`。実在しないパス）を作ると `maxDepth=3` の自動検出から実際に漏れる。

## 参考実装（サンプル。削除後は存在しない）

- `composeApp/data/zenn/build.gradle.kts` — `kmp-library` + 機能別プラグイン構成の実例。
- `composeApp/feature/user/build.gradle.kts` — `kmp-compose-library` + Compose Resources 直接依存の実例。
- `composeApp/feature/home/build.gradle.kts` — Compose Resources を持たない `kmp-compose-library` モジュールの実例（`compose-components-resources` 直接依存なし）。
- `composeApp/ui/build.gradle.kts` — `publicResClass = true` の実例。
- `composeApp/domain/zenn/build.gradle.kts` — `libs.bundles.domain`（空の bundle）を参照する実例。

## 参考実装（テンプレート基盤。削除されない）

- `settings.gradle.kts` — 全モジュールの `include` 一覧。
- `build.gradle.kts`（ルート） — `changeProjectName`/`changePackageName` タスク。プロジェクト名・パッケージ名変更時はこれらの Gradle タスクを使う（README.md の "How to Rename" 参照）。
- `build-logic/src/main/kotlin/BuildUtils.kt` — `deriveNamespace` の実装、`PACKAGE_NAME` の単一情報源。
- `build-logic/src/main/kotlin/kmp-library.gradle.kts` — 全 KMP モジュール共通の基盤（`androidTarget`, `iosArm64`, `iosSimulatorArm64`）。
- `build-logic/src/main/kotlin/kmp-compose-library.gradle.kts` — `kmp-library` + Compose、`compose.resources` の自動設定。
- `composeApp/build.gradle.kts` — 集約シェルの動的スキャン実装。
- `gradle/libs.versions.toml` — バージョンカタログ全体。

## チェックリスト

- [ ] `settings.gradle.kts` に `include` を追加した。
- [ ] `kmp-library`/`kmp-compose-library` のどちらを使うか、Compose UI/Resources の有無で判断した。
- [ ] ソースパッケージが `deriveNamespace` の出力と一致している（`composeApp:app` を除く）。
- [ ] `composeResources/` を持つ場合、`compose-components-resources` への直接依存を追加した。
- [ ] `publicResClass` を「他モジュールから参照されるか」で true/false 判定した。
- [ ] 新規ライブラリをバージョンカタログの既存カテゴリに追記し、既存のレイヤー単位 bundle（`libs.bundles.<layer>`）に追加した（モジュールごとに新規 bundle を作っていない）。
- [ ] 追加したモジュール階層が `composeApp/<layer>/<name>` を超えていない。
- [ ] `composeApp/app/build.gradle.kts` に依存を追加した（他モジュールから利用する場合）。
- [ ] Alpha/Beta 版ライブラリを採用していない、または採用しない判断を記録した。
