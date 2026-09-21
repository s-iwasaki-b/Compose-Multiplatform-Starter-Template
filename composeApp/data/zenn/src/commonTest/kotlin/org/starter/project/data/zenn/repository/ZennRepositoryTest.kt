package org.starter.project.data.zenn.repository

import kotlinx.coroutines.test.runTest
import org.starter.project.data.zenn.converter.ArticlesConverter
import org.starter.project.data.zenn.datasource.api.FakeZennApi
import org.starter.project.data.zenn.datasource.api.response.ArticleResponse
import org.starter.project.data.zenn.datasource.api.response.ArticleUserResponse
import org.starter.project.data.zenn.datasource.api.response.ArticlesResponse
import org.starter.project.data.zenn.datasource.preferences.FakeZennPreferences
import kotlin.test.Test
import kotlin.test.assertEquals

class ZennRepositoryTest {
    private val fakeZennApi = FakeZennApi()
    private val zennPreferences = FakeZennPreferences()
    private val subject = ZennRepositoryImpl(fakeZennApi, zennPreferences)

    @Test
    fun fetchArticles() = runTest {
        // arrange
        val userResponse = ArticleUserResponse(
            id = 0,
            username = "username",
            name = "name",
            avatarSmallUrl = "avatar_small_url"
        )
        val articleResponse = ArticleResponse(
            id = 0,
            emoji = "emoji",
            title = "title",
            commentsCount = 1,
            likedCount = 2,
            publishedAt = "published_at",
            user = userResponse
        )
        val response = ArticlesResponse(
            articles = listOf(articleResponse),
            nextPage = "next_page"
        )
        fakeZennApi.articlesByPublicationName["publication"] = response

        // act
        val actual = subject.fetchArticles(publicationName = "publication")

        // assert
        val expected = ArticlesConverter(response)
        assertEquals(expected, actual)
    }

    @Test
    fun getLastKeyword() {
        // arrange
        zennPreferences.lastKeyword = "keyword"

        // act
        val actual = subject.getLastKeyword()

        // assert
        val expected = "keyword"
        assertEquals(expected, actual)
    }

    @Test
    fun updateLastKeyword() {
        // arrange
        val keyword = "keyword"

        // act
        subject.updateLastKeyword(keyword)

        // assert
        val expected = "keyword"
        assertEquals(expected, zennPreferences.lastKeyword)
    }
}
