package org.starter.project.data.zenn.repository

import org.starter.project.base.data.model.zenn.Articles
import org.starter.project.data.repository.ZennRepository
import org.starter.project.data.zenn.converter.ArticlesConverter
import org.starter.project.data.zenn.datasource.api.ZennApi
import org.starter.project.data.zenn.datasource.preferences.ZennPreferences

class ZennRepositoryImpl(
    private val zennApi: ZennApi,
    private val zennPreferences: ZennPreferences
) : ZennRepository {
    override suspend fun fetchArticles(
        userName: String?,
        publicationName: String?,
        order: String?,
        page: String?
    ): Articles {
        return ArticlesConverter(zennApi.fetchArticles(userName, publicationName, order, page))
    }

    override fun getLastKeyword(): String? = zennPreferences.lastKeyword

    override fun updateLastKeyword(keyword: String) {
        zennPreferences.lastKeyword = keyword
    }
}
