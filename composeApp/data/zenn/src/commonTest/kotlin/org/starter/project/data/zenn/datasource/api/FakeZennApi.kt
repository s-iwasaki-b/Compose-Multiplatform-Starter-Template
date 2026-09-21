package org.starter.project.data.zenn.datasource.api

import org.starter.project.data.zenn.datasource.api.response.ArticlesResponse
import org.starter.project.data.zenn.datasource.api.response.UserResponse

/**
 * [ZennApi] の Fake。ネットワークを使わないインメモリ実装。
 *
 * - order / page は無視する。
 * - userName と publicationName の両方が指定された場合は publicationName を優先する。
 * - [error] に [Throwable] を設定すると、すべての操作がそれを throw する（通信失敗を模倣）。
 */
class FakeZennApi : ZennApi {
    val articlesByUserName = mutableMapOf<String, ArticlesResponse>()
    val articlesByPublicationName = mutableMapOf<String, ArticlesResponse>()
    val usersByName = mutableMapOf<String, UserResponse>()
    var error: Throwable? = null

    override suspend fun fetchArticles(
        userName: String?,
        publicationName: String?,
        order: String?,
        page: String?
    ): ArticlesResponse {
        error?.let { throw it }
        return publicationName?.let { articlesByPublicationName[it] }
            ?: userName?.let { articlesByUserName[it] }
            ?: ArticlesResponse(articles = emptyList(), nextPage = null)
    }

    override suspend fun fetchUser(username: String): UserResponse {
        error?.let { throw it }
        return usersByName[username] ?: throw NoSuchElementException("User not found: $username")
    }
}
