package org.starter.project.data.zenn.datasource.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import org.starter.project.data.zenn.datasource.api.response.ArticlesResponse

interface ZennApi {
    @GET("api/articles")
    suspend fun fetchArticles(
        @Query("username") userName: String? = null,
        @Query("publication_name") publicationName: String? = null,
        @Query("order") order: String? = null,
        @Query("page") page: String? = null
    ): ArticlesResponse
}
