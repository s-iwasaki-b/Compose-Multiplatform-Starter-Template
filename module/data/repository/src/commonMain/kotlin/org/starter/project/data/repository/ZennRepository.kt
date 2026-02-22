package org.starter.project.data.repository

import org.starter.project.base.data.model.zenn.Articles
import org.starter.project.base.data.model.zenn.User

interface ZennRepository : Repository {
    suspend fun fetchArticles(
        userName: String? = null,
        publicationName: String? = null,
        order: String? = "latest",
        page: String? = null
    ): Articles
    suspend fun fetchUser(username: String): User
    fun getLastKeyword(): String?
    fun updateLastKeyword(keyword: String)
}
