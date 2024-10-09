package org.starter.project.data.zenn.converter

import androidx.annotation.VisibleForTesting
import org.starter.project.base.data.model.zenn.Article
import org.starter.project.base.data.model.zenn.Articles
import org.starter.project.base.data.model.zenn.User
import org.starter.project.base.error.ConversionError
import org.starter.project.base.extension.validateNotNull
import org.starter.project.data.zenn.datasource.api.response.ArticleResponse
import org.starter.project.data.zenn.datasource.api.response.ArticlesResponse
import org.starter.project.data.zenn.datasource.api.response.UserResponse

object ArticlesConverter {
    operator fun invoke(response: ArticlesResponse): Articles {
        return Articles(
            articles = response.articles?.mapNotNull {
                try {
                    createArticle(it)
                } catch (e: ConversionError) {
                    // TODO: report conversion error as non-fatal to your analytics
                    null
                }
            }.orEmpty(),
            nextPage = response.nextPage
        )
    }

    @VisibleForTesting
    internal fun createArticle(response: ArticleResponse): Article {
        return Article(
            id = response.id.validateNotNull("id"),
            emoji = response.emoji.validateNotNull("emoji"),
            title = response.title.validateNotNull("title"),
            commentsCount = response.commentsCount.validateNotNull("comments_count"),
            likedCount = response.likedCount.validateNotNull("liked_count"),
            publishedAt = response.publishedAt.validateNotNull("published_at"),
            user = createUser(response.user.validateNotNull("user"))
        )
    }

    @VisibleForTesting
    internal fun createUser(response: UserResponse): User {
        return User(
            id = response.id.validateNotNull("id"),
            username = response.username.validateNotNull("username"),
            name = response.name.validateNotNull("name"),
            avatarSmallUrl = response.avatarSmallUrl.validateNotNull("avatar_small_url")
        )
    }
}
