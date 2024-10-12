package org.starter.project.data.zenn.converter

import org.starter.project.base.data.model.zenn.Article
import org.starter.project.base.data.model.zenn.Articles
import org.starter.project.base.data.model.zenn.User
import org.starter.project.base.error.ConversionError.ResponseNotNullValidation
import org.starter.project.data.zenn.datasource.api.response.ArticleResponse
import org.starter.project.data.zenn.datasource.api.response.ArticlesResponse
import org.starter.project.data.zenn.datasource.api.response.UserResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ArticlesConverterTest {
    private val subject = ArticlesConverter
    private val userResponse = UserResponse(
        id = 0,
        username = "username",
        name = "name",
        avatarSmallUrl = "avatar_small_url"
    )
    private val articleResponse = ArticleResponse(
        id = 0,
        postType = "post_type",
        title = "title",
        slug = "slug",
        commentsCount = 1,
        likedCount = 2,
        bodyLettersCount = 3,
        articleType = "article_type",
        emoji = "emoji",
        isSuspendingPrivate = false,
        publishedAt = "published_at",
        bodyUpdatedAt = "body_updated_at",
        sourceRepoUpdatedAt = "source_repo_updated_at",
        pinned = true,
        path = "path",
        user = userResponse,
        publication = null
    )
    private val articles = listOf(
        articleResponse.copy(id = 1),
        articleResponse.copy(id = 2),
        articleResponse.copy(id = 3)
    )
    private val articlesResponse = ArticlesResponse(
        articles = articles,
        nextPage = "next_page"
    )

    @Test
    fun invoke_success() {
        // act
        val actual = subject(articlesResponse)

        // assert
        val expected = Articles(
            articles = articles.map { subject.createArticle(it) },
            nextPage = "next_page"
        )
        assertEquals(expected, actual)
    }

    @Test
    fun invoke_success_exclude_error() {
        // arrange
        val articlesResponse = articlesResponse.copy(
            articles = listOf(
                articleResponse.copy(id = 1),
                articleResponse.copy(id = null),
                articleResponse.copy(id = 3)
            )
        )

        // act
        val actual = subject(articlesResponse)

        // assert
        val expected = Articles(
            articles = listOf(
                subject.createArticle(articles[0]),
                subject.createArticle(articles[2])
            ),
            nextPage = "next_page"
        )
        assertEquals(expected, actual)
    }

    @Test
    fun createArticle_success() {
        // act
        val actual = subject.createArticle(articleResponse)

        // assert
        val expected = Article(
            id = 0,
            emoji = "emoji",
            title = "title",
            commentsCount = 1,
            likedCount = 2,
            publishedAt = "published_at",
            user = subject.createUser(userResponse)
        )
        assertEquals(expected, actual)
    }

    @Test
    fun createArticle_failure_notNullValidation() {
        listOf(
            articleResponse.copy(id = null),
            articleResponse.copy(emoji = null),
            articleResponse.copy(title = null),
            articleResponse.copy(commentsCount = null),
            articleResponse.copy(likedCount = null),
            articleResponse.copy(publishedAt = null),
            articleResponse.copy(user = null)
        ).forEach {
            assertFailsWith<ResponseNotNullValidation>(message = it.toString()) {
                subject.createArticle(it)
            }
        }
    }

    @Test
    fun createUser_success() {
        // act
        val actual = subject.createUser(userResponse)

        // assert
        val expected = User(
            id = 0,
            username = "username",
            name = "name",
            avatarSmallUrl = "avatar_small_url"
        )
        assertEquals(expected, actual)
    }

    @Test
    fun createUser_failure_notNullValidation() {
        listOf(
            userResponse.copy(id = null),
            userResponse.copy(username = null),
            userResponse.copy(name = null),
            userResponse.copy(avatarSmallUrl = null)
        ).forEach {
            assertFailsWith<ResponseNotNullValidation>(message = it.toString()) {
                subject.createUser(it)
            }
        }
    }
}
