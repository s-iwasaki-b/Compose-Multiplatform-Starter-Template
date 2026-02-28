package org.starter.project.data.zenn.repository

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import kotlinx.coroutines.test.runTest
import org.starter.project.data.zenn.converter.ArticlesConverter
import org.starter.project.data.zenn.datasource.api.ZennApi
import org.starter.project.data.zenn.datasource.api.response.ArticlesResponse
import org.starter.project.data.zenn.datasource.preferences.ZennPreferences
import kotlin.test.Test
import kotlin.test.assertEquals

class ZennRepositoryTest {
    private val mockZennApi = mock<ZennApi>()
    private val mockZennPreferences = mock<ZennPreferences>(MockMode.autofill)
    private val subject = ZennRepositoryImpl(mockZennApi, mockZennPreferences)

    @Test
    fun fetchArticles() = runTest {
        // arrange
        val response = ArticlesResponse(emptyList(), "next_page")
        everySuspend { mockZennApi.fetchArticles(any(), any(), any(), any()) } returns response

        // act
        val actual = subject.fetchArticles("", "", "", "")

        // assert
        val expected = ArticlesConverter(response)
        assertEquals(expected, actual)
    }

    @Test
    fun getLastKeyword() {
        // arrange
        val keyword = "keyword"
        every { mockZennPreferences.lastKeyword } returns keyword

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
        verify(VerifyMode.exactly(1)) { mockZennPreferences.lastKeyword = keyword }
    }
}
