package org.starter.project.domain.service

import org.starter.project.base.data.model.zenn.Articles
import org.starter.project.base.data.model.zenn.User

interface ZennService : Service {
    suspend fun fetchArticles(
        keyword: String,
        nextPage: String?
    ): Result<Articles>
    fun getLastKeyword(): Result<String?>
    suspend fun fetchUser(username: String): Result<User>
    suspend fun fetchUserArticles(username: String, nextPage: String?): Result<Articles>
}
