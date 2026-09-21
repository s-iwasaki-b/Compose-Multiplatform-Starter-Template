package org.starter.project.domain.zenn

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.starter.project.base.data.model.zenn.Article
import org.starter.project.base.data.model.zenn.Articles
import org.starter.project.domain.service.ResultHandler
import org.starter.project.testing.repository.FakeZennRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ZennServiceTest {
    private val user = Article.User(4, "username", "name", "avatar_small_url")
    private val article = Article(1, "emoji", "title", 2, 3, "published_at", user)
    private val articles = Articles(listOf(article), "next_page")
    private val fakeZennRepository = FakeZennRepository()

    private val testDispatcher = StandardTestDispatcher()
    private val testResultHandler = ResultHandler(testDispatcher)
    private val subject = ZennServiceImpl(testResultHandler, fakeZennRepository)

    @Test
    fun fetchArticles_exist_publication_articles() = runTest(testDispatcher) {
        // arrange
        val keyword = "keyword"
        val otherArticle = Article(5, "emoji", "other title", 6, 7, "published_at", user)
        val otherArticles = Articles(listOf(otherArticle), "next_page")
        fakeZennRepository.publicationArticles[keyword] = articles
        fakeZennRepository.userArticles[keyword] = otherArticles

        // act
        val actual = subject.fetchArticles(keyword, "next_page")

        // assert
        val expected = Result.success(articles)
        assertEquals(expected, actual)
        assertEquals(keyword, fakeZennRepository.getLastKeyword())
    }

    @Test
    fun fetchArticles_not_exist_publication_articles() = runTest(testDispatcher) {
        // arrange
        val keyword = "keyword"
        fakeZennRepository.userArticles[keyword] = articles

        // act
        val actual = subject.fetchArticles(keyword, "next_page")

        // assert
        val expected = Result.success(articles)
        assertEquals(expected, actual)
        assertEquals(keyword, fakeZennRepository.getLastKeyword())
    }

    @Test
    fun fetchArticles_failure_shouldReturnThrowable() = runTest(testDispatcher) {
        // arrange
        val error = Throwable()
        fakeZennRepository.fetchError = error

        // act
        val actual = subject.fetchArticles("", "")

        // assert
        val expected: Result<*> = Result.failure<Throwable>(error)
        assertEquals(expected, actual)
    }

    @Test
    fun getLastKeyword() = runTest(testDispatcher) {
        // arrange
        val keyword = "keyword"
        fakeZennRepository.updateLastKeyword(keyword)

        // act
        val actual = subject.getLastKeyword()

        // assert
        val expected = Result.success(keyword)
        assertEquals(expected, actual)
    }

    @Test
    fun getLastKeyword_failure_shouldReturnThrowable() = runTest(testDispatcher) {
        // arrange
        val error = Throwable()
        fakeZennRepository.preferencesError = error

        // act
        val actual = subject.getLastKeyword()

        // assert
        val expected: Result<*> = Result.failure<Throwable>(error)
        assertEquals(expected, actual)
    }
}
