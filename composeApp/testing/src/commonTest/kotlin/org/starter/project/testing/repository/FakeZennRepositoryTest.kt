package org.starter.project.testing.repository

import kotlinx.coroutines.test.runTest
import org.starter.project.base.data.model.zenn.Article
import org.starter.project.base.data.model.zenn.Articles
import org.starter.project.base.data.model.zenn.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class FakeZennRepositoryTest {
    private val articleUser = Article.User(4, "username", "name", "avatar_small_url")
    private val publicationArticle = Article(1, "emoji", "publication title", 2, 3, "published_at", articleUser)
    private val publicationArticles = Articles(listOf(publicationArticle), "next_page")
    private val userArticle = Article(5, "emoji", "user title", 6, 7, "published_at", articleUser)
    private val userArticles = Articles(listOf(userArticle), "next_page")
    private val user = User(4, "username", "name", "avatar_small_url", "avatar_url", "bio", 1, 2, 3)

    private val subject = FakeZennRepository()

    @Test
    fun fetchArticles_returns_publication_articles_for_publicationName() = runTest {
        // arrange
        subject.publicationArticles["keyword"] = publicationArticles

        // act
        val actual = subject.fetchArticles(publicationName = "keyword")

        // assert
        assertEquals(publicationArticles, actual)
    }

    @Test
    fun fetchArticles_returns_user_articles_when_publicationName_not_registered() = runTest {
        // arrange
        subject.userArticles["keyword"] = userArticles

        // act
        val actual = subject.fetchArticles(userName = "keyword")

        // assert
        assertEquals(userArticles, actual)
    }

    @Test
    fun fetchArticles_prioritizes_publicationName_when_both_specified() = runTest {
        // arrange
        subject.publicationArticles["keyword"] = publicationArticles
        subject.userArticles["keyword"] = userArticles

        // act
        val actual = subject.fetchArticles(userName = "keyword", publicationName = "keyword")

        // assert
        assertEquals(publicationArticles, actual)
    }

    @Test
    fun fetchArticles_returns_empty_articles_when_not_registered() = runTest {
        // act
        val actual = subject.fetchArticles(userName = "keyword", publicationName = "keyword")

        // assert
        assertEquals(Articles(articles = emptyList(), nextPage = null), actual)
    }

    @Test
    fun fetchArticles_throws_fetchError_when_set() = runTest {
        // arrange
        val error = Throwable()
        subject.fetchError = error

        // act & assert
        val actual = assertFailsWith<Throwable> {
            subject.fetchArticles(publicationName = "keyword")
        }
        assertSame(error, actual)
    }

    @Test
    fun fetchUser_returns_registered_user() = runTest {
        // arrange
        subject.users["username"] = user

        // act
        val actual = subject.fetchUser("username")

        // assert
        assertEquals(user, actual)
    }

    @Test
    fun fetchUser_throws_NoSuchElementException_when_not_registered() = runTest {
        // act & assert
        assertFailsWith<NoSuchElementException> {
            subject.fetchUser("username")
        }
    }

    @Test
    fun updateLastKeyword_then_getLastKeyword_roundtrips() {
        // act
        subject.updateLastKeyword("keyword")

        // assert
        assertEquals("keyword", subject.getLastKeyword())
    }

    @Test
    fun getLastKeyword_returns_null_by_default() {
        // assert
        assertNull(subject.getLastKeyword())
    }

    @Test
    fun getLastKeyword_throws_preferencesError_when_set() {
        // arrange
        val error = Throwable()
        subject.preferencesError = error

        // act & assert
        val actual = assertFailsWith<Throwable> {
            subject.getLastKeyword()
        }
        assertSame(error, actual)
    }

    @Test
    fun updateLastKeyword_throws_preferencesError_when_set() {
        // arrange
        val error = Throwable()
        subject.preferencesError = error

        // act & assert
        val actual = assertFailsWith<Throwable> {
            subject.updateLastKeyword("keyword")
        }
        assertSame(error, actual)
    }
}
