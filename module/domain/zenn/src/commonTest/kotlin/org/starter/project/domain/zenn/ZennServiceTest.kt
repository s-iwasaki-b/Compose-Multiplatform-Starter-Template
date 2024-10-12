package org.starter.project.domain.zenn

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.starter.project.base.data.model.zenn.Article
import org.starter.project.base.data.model.zenn.Articles
import org.starter.project.base.data.model.zenn.User
import org.starter.project.data.repository.ZennRepository
import org.starter.project.domain.service.ResultHandler
import kotlin.test.Test
import kotlin.test.assertEquals

class ZennServiceTest {
    private val mockUser = User(4, "username", "name", "avatar_small_url")
    private val mockArticle = Article(1, "emoji", "title", 2, 3, "published_at", mockUser)
    private val mockArticles = Articles(listOf(mockArticle), "next_page")
    private val mockZennRepository = mock<ZennRepository>(MockMode.autofill)

    private val testDispatcher = StandardTestDispatcher()
    private val testResultHandler = ResultHandler(testDispatcher)
    private val subject = ZennServiceImpl(testResultHandler, mockZennRepository)

    @Test
    fun fetchArticles_exist_publication_articles() = runTest(testDispatcher) {
        // arrange
        val keyword = "keyword"
        everySuspend {
            mockZennRepository.fetchArticles(any(), keyword, any(), any())
        } returns mockArticles

        // act
        val actual = subject.fetchArticles(keyword, "next_page")

        // assert
        val expected = Result.success(mockArticles)
        assertEquals(expected, actual)

        verifySuspend {
            mockZennRepository.updateLastKeyword(keyword)
            mockZennRepository.fetchArticles(publicationName = keyword, page = "next_page")
        }
    }

    @Test
    fun fetchArticles_not_exist_publication_articles() = runTest(testDispatcher) {
        // arrange
        val keyword = "keyword"
        everySuspend {
            mockZennRepository.fetchArticles(any(), keyword, any(), any())
        } returns mockArticles.copy(articles = emptyList())
        everySuspend {
            mockZennRepository.fetchArticles(keyword, any(), any(), any())
        } returns mockArticles

        // act
        val actual = subject.fetchArticles(keyword, "next_page")

        // assert
        val expected = Result.success(mockArticles)
        assertEquals(expected, actual)

        verifySuspend {
            mockZennRepository.updateLastKeyword(keyword)
            mockZennRepository.fetchArticles(publicationName = keyword, page = "next_page")
            mockZennRepository.fetchArticles(userName = keyword, page = "next_page")
        }
    }

    @Test
    fun fetchArticles_failure_shouldReturnThrowable() = runTest(testDispatcher) {
        // arrange
        val error = Throwable()
        everySuspend {
            mockZennRepository.fetchArticles(any(), any(), any(), any())
        } throws error

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
        everySuspend { mockZennRepository.getLastKeyword() } returns keyword

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
        everySuspend { mockZennRepository.getLastKeyword() } throws error

        // act
        val actual = subject.getLastKeyword()

        // assert
        val expected: Result<*> = Result.failure<Throwable>(error)
        assertEquals(expected, actual)
    }
}
