# ADR-0005: Repository はカーソル付き単発取得、Paging3 の組み立ては UI 層

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（`composeApp/ui/src/commonMain/kotlin/org/starter/project/ui/shared/component/article/ArticlesPagingSource.kt`）
> 関連: [data-layer.md](../guides/data-layer.md), [ui-layer.md](../guides/ui-layer.md), [0001](0001-contract-and-impl-modules.md)

## Context

一覧系 API を追加する際、ページングを Repository/Service 層で `Flow<PagingData<T>>` として提供すべきか、UI 層で組み立てるべきかが不明だと、Paging ライブラリへの依存が層をまたいで混入する。本テンプレートは `ZennRepository.fetchArticles` が `nextPage` カーソルを含む単発の `suspend fun` を提供し、Paging3 の `Pager`/`PagingSource` は UI 層（`composeApp/ui`）が組み立てる、という層分離を確立している。

## Decision

- **必須**: Repository/Service はページングを意識せず、`nextPage` のようなカーソル文字列を含む単発の取得結果を返す。`Flow<PagingData<T>>` や `Pager` を Repository/Service の interface に含めない。
- **必須**: Paging3 の `PagingSource`・`Pager` の組み立ては UI 層（`composeApp/ui` の共有コンポーネントまたは feature モジュール）に置く。`fetcher: suspend (key) -> T?` のようなラムダで Service 呼び出しを受け取る。
- **推奨**: ページサイズなどの Paging 定数は `PagingSource` 側の `companion object` に定義する。

## 判断基準

| 状況 | 判断 |
|---|---|
| 新しい一覧系 API を追加する | Service メソッドは Paging 型を返さず、カーソル引数＋単発取得のシグネチャにする |
| 複数画面で同じ PagingSource を再利用したい | `composeApp/ui` の `shared/component/<domain>/` に置く |
| 一覧の更新をリアルタイムに継続監視したい | 単発取得の枠組みでは対応しない。継続監視が要件として明示された場合のみ、別途 Repository/Service の `Flow` 対応を検討する（本 ADR の範囲外） |

## Consequences

- メリット: Repository/Service のテストが単発の suspend 関数として書け、Paging ライブラリへの依存を data/domain 層に持ち込まずに済む。
- デメリット / トレードオフ: UI 層で PagingSource を毎回組み立てる分、feature 追加時のボイラープレートが増える。
- 守らせる手段: レビュー（pr-checklist.md）で確認（`data:repository`/`domain:service` は Paging ライブラリに依存していないため、依存を追加しようとした時点で気づきやすい）

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| Repository または Service が `Flow<PagingData<T>>` を直接返す（Android の Paging ガイドでよく見る構成） | 実例と異なり、`data:repository`/`domain:service` が Paging ライブラリに依存することになり、契約モジュールを軽量に保つ方針（[ADR-0001](0001-contract-and-impl-modules.md)）と衝突する |

## サンプル削除後の扱い

`ArticlesPagingSource` 自体はサンプル削除時に消えるため、この決定だけから判断を再現するには「Repository/Service は単発取得のみ提供し、Paging3 の組み立ては UI 層」というルール文に加え、`fetcher` ラムダを受け取る PagingSource のプレースホルダテンプレートが必要（`ui-layer.md` にテンプレートを用意する）。
