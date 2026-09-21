# composeApp/data/repository

`data:repository` は Repository interface のみを持つ契約モジュールである。本ファイルはこのモジュールで作業する実装エージェントの入口である。ルートの [AGENTS.md](../../../AGENTS.md)（絶対ルール・実行体制）を前提とし、ツールが自動で読み込まない場合は先に読む。実装エージェントは本モジュール外のファイルを編集せず、必要な他モジュールの変更は「他モジュールとの接点」に沿ってオーケストレーターへ報告する。読み順は本ファイル → 「参照」節の docs。

## 責務

- 置くもの: マーカー interface `Repository`、各データソースの Repository interface（`XxxRepository`）。
- 置かないもの: 実装クラス・DTO・Converter・Ktorfit API・Preferences 実装・Koin 登録（`data/<source>` または `app` に置く）。

## 依存

```kotlin
plugins {
    id("kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.composeApp.base)
        }
    }
}
```

- **必須** 依存先は `base` のみ（`api` 公開。domain モデル型が公開シグネチャに登場するため）。
- **必須** `core`・`data:<source>`・`domain:*` には依存しない。`commonTest.dependencies` も宣言しない（interface のみのため）。

## 構成

```
src/commonMain/kotlin/org/starter/project/data/repository/
  Repository.kt         # マーカー interface（空、3行）
  XxxRepository.kt       # Repository interface の実例
```

新しい外部リソースの追加時はこのディレクトリに `XxxRepository.kt` を追記する。モジュール自体は分割しない。

## 実装パターン

```kotlin
package org.starter.project.data.repository

import org.starter.project.base.data.model.<domain>.DomainModel

interface XxxRepository : Repository {
    suspend fun fetchXxx(param: String? = null, page: String? = null): DomainModel
    fun getLastXxx(): String?
    fun updateLastXxx(value: String)
}
```

- **必須** 命名は `XxxRepository`。`Repository` を継承し、戻り値は素の domain モデル（`Result` で包まない）。
- 1外部サービスにつき1 Repository を既定とする。ローカル設定など小規模な付帯情報は同居させてよい。

## テスト

interface のみで検証対象のロジックを持たないためテストは書かない。振る舞いのテストは実装側（`data/<source>`）の `XxxRepositoryTest` に置く。

## 他モジュールとの接点

新しい `XxxRepository` を追加した実装エージェントは、次をオーケストレーターへの完了報告に含める（自モジュール外は編集しない）。

- 実装クラス `XxxRepositoryImpl` を `data/<source>` に作成する必要がある。
- `app` の Koin `repositoryModule` への登録が必要。
- interface が参照する domain モデルが未定義なら `base` への追加が必要。

## 完了条件

- 追加・変更した interface が `Repository` を継承し、戻り値が `base` の domain モデル型で `Result` を返していない。
- `build.gradle.kts` の依存が `base` のみのままである。
- 「他モジュールとの接点」の項目をオーケストレーターへの報告に含めた。
- `../../../docs/coding-guide.md` §8 の既知の逸脱を新規コードに複製していない。

## 参照

- ../../../docs/design-guide.md §3（層の境界と実装粒度: Repository の配置とデータソース単位の分割）
- ../../../docs/coding-guide.md §2（data 層の共通実装パターン）
- ../../../docs/decisions.md D-02（Repository interface を data 層に置く理由）
