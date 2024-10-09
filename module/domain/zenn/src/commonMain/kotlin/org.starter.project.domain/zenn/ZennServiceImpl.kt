package org.starter.project.domain.zenn

import androidx.annotation.OpenForTesting
import org.starter.project.data.repository.ZennRepository
import org.starter.project.domain.service.ResultHandler
import org.starter.project.domain.service.ZennService

@OpenForTesting
open class ZennServiceImpl(
    private val resultHandler: ResultHandler,
    private val zennRepository: ZennRepository
) : ZennService {
    override suspend fun fetchArticles(
        keyword: String,
        nextPage: String?
    ) = resultHandler.async {
        zennRepository.fetchArticles(keyword, nextPage)
    }
}
