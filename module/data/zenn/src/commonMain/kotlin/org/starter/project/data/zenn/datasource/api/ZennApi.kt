package org.starter.project.data.zenn.datasource.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import org.starter.project.data.zenn.datasource.api.response.ArticlesResponse
import org.starter.project.data.zenn.datasource.api.response.UserResponse

interface ZennApi {
    @GET("api/articles")
    suspend fun fetchArticles(
        @Query("username") userName: String? = null,
        @Query("publication_name") publicationName: String? = null,
        @Query("order") order: String? = null,
        @Query("page") page: String? = null
    ): ArticlesResponse

    @GET("api/users/{username}")
    suspend fun fetchUser(
        @Path("username") username: String
    ): UserResponse
}
