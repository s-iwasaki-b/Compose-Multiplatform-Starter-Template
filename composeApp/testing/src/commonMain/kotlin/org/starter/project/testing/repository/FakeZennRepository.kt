package org.starter.project.testing.repository

import org.starter.project.base.data.model.zenn.Articles
import org.starter.project.base.data.model.zenn.User
import org.starter.project.data.repository.ZennRepository

/**
 * [ZennRepository] の Fake。インメモリで動作する軽量実装であり、
 * テストから状態を直接読み書きできる。order / page は無視する。
 * userName と publicationName の両方が指定された場合は publicationName を優先する。
 * fetchError / preferencesError に Throwable を設定すると、該当操作がそれを throw する
 * （API 境界と Preferences 境界の失敗をそれぞれ模倣する）。
 * 最後に保存したキーワードは [updateLastKeyword] / [getLastKeyword] を通じて読み書きする。
 */
class FakeZennRepository : ZennRepository {
    val publicationArticles = mutableMapOf<String, Articles>()
    val userArticles = mutableMapOf<String, Articles>()
    val users = mutableMapOf<String, User>()

    private var lastKeyword: String? = null
    var fetchError: Throwable? = null
    var preferencesError: Throwable? = null

    override suspend fun fetchArticles(
        userName: String?,
        publicationName: String?,
        order: String?,
        page: String?
    ): Articles {
        fetchError?.let { throw it }
        return publicationName?.let { publicationArticles[it] }
            ?: userName?.let { userArticles[it] }
            ?: Articles(articles = emptyList(), nextPage = null)
    }

    override suspend fun fetchUser(username: String): User {
        fetchError?.let { throw it }
        return users[username] ?: throw NoSuchElementException("User not found: $username")
    }

    override fun getLastKeyword(): String? {
        preferencesError?.let { throw it }
        return lastKeyword
    }

    override fun updateLastKeyword(keyword: String) {
        preferencesError?.let { throw it }
        lastKeyword = keyword
    }
}
