package org.starter.project.data.zenn.datasource.preferences

/**
 * [ZennPreferences] の Fake。プラットフォームの永続化に依存しないインメモリ実装。
 *
 * `com.russhwolf:multiplatform-settings` が提供するインメモリ実装（`MapSettings`）は、
 * このモジュールが依存する `multiplatform-settings` artifact（バージョン 1.3.0）には
 * 含まれていないため、代わりにこの Fake を用いる。
 */
class FakeZennPreferences : ZennPreferences {
    override var lastKeyword: String? = null
}
