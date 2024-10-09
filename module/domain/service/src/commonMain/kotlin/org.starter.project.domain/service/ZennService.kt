package org.starter.project.domain.service

import org.starter.project.base.data.model.zenn.Articles

interface ZennService : Service {
    suspend fun fetchArticles(
        keyword: String,
        nextPage: String?
    ): Result<Articles>
}
