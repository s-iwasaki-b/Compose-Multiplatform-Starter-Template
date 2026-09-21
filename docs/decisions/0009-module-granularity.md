# ADR-0009: feature は機能ドメイン（ユーザーフロー）単位、data/domain は外部リソース単位

> ステータス: 採用
> 日付: 2026-09-21
> 実例: あり（非対称。`composeApp/data/zenn/`, `composeApp/domain/zenn/`, `composeApp/feature/home/`, `composeApp/feature/user/`）
> 関連: [module-guide.md](../architecture/module-guide.md), [overview.md](../architecture/overview.md), [add-feature-screen.md](../playbooks/add-feature-screen.md), [add-data-source.md](../playbooks/add-data-source.md)

## Context

新機能を追加する際、feature/data/domain の 3 層を常に 1:1 で新設すべきかを判断できないと、不要なモジュール増殖や、逆に既存モジュールへの無秩序な追記が起きる。唯一の実例（Zenn 機能）は `data:zenn` + `domain:zenn` という外部リソース単位の 1 モジュールずつに対し、`feature:home`/`feature:user` という 2 つの画面（ユーザーフロー）単位のモジュールに分かれており、**UI と data/domain の粒度は非対称**になっている。

## Decision

- **必須**: `feature:<name>` モジュールは機能ドメイン（ユーザーが辿る一連の画面/フロー）単位で分割する。1 画面 = 1 モジュールを機械的に強制しない。
- **必須**: `data:<source>`/`domain:<name>` モジュールは外部リソース（API・永続化対象）単位で分割する。同じ外部リソースを複数の feature モジュールから利用する場合、新たに `data`/`domain` モジュールを複製せず既存モジュールを再利用する。
- **推奨**: 新機能が既存の外部リソースを使うだけなら、`data`/`domain` は新設せず既存の Service（例: `ZennService`）にメソッドを追記し、`feature:<screen>` のみを新設する。
- **推奨**: 新規画面が既存のユーザーフローと強く関連する場合（一覧→詳細等）は、新しい `feature` モジュールを切らず既存 `feature` モジュール内にパッケージを追加してもよい（新規決定）。

## 判断基準

新機能追加時の判断表:

| 新規の外部リソースが必要か | 新規画面が必要か | 判断 |
|---|---|---|
| あり | あり | `data:<source>`・`domain:<name>`・`feature:<name>` をすべて新設する |
| あり | なし | `data:<source>`・`domain:<name>` を新設し、既存の `feature:<name>` にメソッド呼び出しを追加する |
| なし | あり | 既存の `data`/`domain` モジュールを再利用し、`feature:<name>`（または既存 feature 内の新しいユーザーフロー）のみ新設する |
| なし | なし | 新規モジュールは不要。既存の `feature`/`domain` にメソッド・画面を追記する |

## Consequences

- メリット: data/domain を外部リソース単位に保つことで、1 つの API 変更が影響するモジュール数を最小化できる。feature をユーザーフロー単位にすることで、画面遷移が密接な複数画面を 1 モジュールにまとめられ、Gradle モジュール数の増殖を抑えられる。
- デメリット / トレードオフ: 「機能ドメイン」の境界線引きに主観が入りやすく、feature 分割の判断基準が data/domain ほど機械的ではない。
- 守らせる手段: レビュー（pr-checklist.md）で確認（Gradle では強制できない）

## Alternatives

| 代替案 | 却下理由 |
|---|---|
| feature/data/domain を常に 1:1 で新設する（1 機能 = 3 モジュール） | 唯一の実例（Zenn）がすでに非対称（data/domain 1 つに対し feature 2 つ）であり、1:1 を強制すると実例と矛盾する上、画面ごとに不要な data/domain モジュールが複製される |
| feature も外部リソース単位で分割する（`feature:zenn` のように） | UI のユーザーフロー（ホーム画面／ユーザー画面）と外部リソースの単位は本質的に異なる関心事であり、1 つの外部リソースを複数の独立した画面フローが利用する構成（実例どおり）を表現できなくなる |

## サンプル削除後の扱い

Zenn サンプル（`data:zenn`/`domain:zenn`/`feature:home`/`feature:user`）が削除されても、この判断表と非対称性の原則だけで新規機能のモジュール分割を再現できる。具体的なモジュール名の実例は `module-guide.md` のテンプレートを参照する。
