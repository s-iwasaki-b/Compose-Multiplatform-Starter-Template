# ADR-0004: domain モデルは `base` に置き、DTO と常に分離する

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（`composeApp/base/src/commonMain/kotlin/org/starter/project/base/data/model/zenn/Articles.kt`）
> 関連: [data-layer.md](../guides/data-layer.md), [domain-layer.md](../guides/domain-layer.md), [module-guide.md](../architecture/module-guide.md)

## Context

新しい外部リソースを追加する際、domain モデルをどこに定義すべきか、DTO と同一視してよいかが不明だと、DTO が UI 層まで漏れ出す設計崩れを招く。本テンプレートは `Articles`/`User` のような domain モデルを `domain/*` モジュールではなく `composeApp/base` に置き、DTO とは常に別型にしている。

## Decision

- **必須**: domain モデルは `composeApp/base` の `org.starter.project.base.data.model.<feature>` パッケージに `@Immutable data class`（non-null フィールドのみ）で定義する。`domain/<name>` モジュール配下に独自の `model` パッケージを作らない。
- **必須**: DTO と domain モデルは構造が一致していても必ず別クラスにし、`data/<source>` の `object XxxConverter { operator fun invoke(...) }` で変換する。
- **必須**: DTO は全フィールド `@Serializable`・nullable・`= null` デフォルトで定義し（kotlinx.serialization の required フィールド起因のデコード失敗を防ぐため）、domain モデルへ `kotlinx.serialization` の型やアノテーションを漏らさない。
- **推奨**: 必須フィールドの欠落検出は `validateNotNull` 拡張関数＋`ConversionError` で行う。一覧変換は `mapNotNull` で 1 件ずつ失敗を許容する。

## 判断基準

| 状況 | 判断 |
|---|---|
| 新しい外部リソースの domain モデルを追加する | `base` の `data.model.<feature>` パッケージに追加する |
| DTO と domain モデルのフィールドが完全一致している | それでも別クラスにする（使い回さない） |
| 一覧のうち一部要素の変換が失敗しうる | `mapNotNull` + `try/catch (ConversionError)` で 1 件のみ除外し、全体を失敗させない |

## Consequences

- メリット: `data:repository`/`domain:service` など複数モジュールから依存できる共有カーネルとして機能し、モデルの置き場所を一意に決められる。DTO の nullable 性やシリアライズ都合が UI 層まで伝播しない。
- デメリット / トレードオフ: 機能が増えるほど `base` が肥大化するリスクがある（複数箇所から参照される場合のみ昇格、という粒度基準は別途運用で補う必要がある）。
- 守らせる手段: レビュー（pr-checklist.md）で確認（DTO を domain として使い回すコードは Gradle では検出できず、コンパイルは通ってしまう）

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| domain モデルを `domain/<name>` モジュール配下の `model` パッケージに置く（一般的な Clean Architecture 知識） | `domain/service` と `data/repository` の双方が `base` にしか依存していないため、`domain/<name>` に置くと契約モジュール側から参照できなくなる |
| DTO を直接 domain モデルとして使い回す（フィールドが一致する場合） | API 都合の変更（nullable 化・フィールド追加）が UI 層まで即座に伝播する。実際に発生した障害（コミット `4aefc64` の nullable フィールド欠落問題）の再発を招く |

## サンプル削除後の扱い

`base` モジュールと `ConversionError`/`validateNotNull` の仕組みは Zenn サンプル削除後も残るため、新規リソース追加時は本 ADR のテンプレート（`base` にモデル、`data/<source>` に Converter）をそのまま複製すればよい。
