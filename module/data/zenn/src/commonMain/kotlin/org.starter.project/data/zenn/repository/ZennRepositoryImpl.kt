package org.starter.project.data.zenn.repository

import org.starter.project.base.data.model.zenn.Articles
import org.starter.project.core.api.ApiClient
import org.starter.project.data.repository.ZennRepository
import org.starter.project.data.zenn.converter.ArticlesConverter
import org.starter.project.data.zenn.datasource.api.ZennApi
import org.starter.project.data.zenn.datasource.api.createZennApi

class ZennRepositoryImpl(
    private val zennApi: ZennApi = ApiClient.ktorfit.createZennApi()
) : ZennRepository {
    override suspend fun fetchArticles(
        userName: String?,
        publicationName: String?,
        order: String?,
        page: String?
    ): Articles {
        return ArticlesConverter(zennApi.fetchArticles(userName, publicationName, order, page))
    }
}
