package org.starter.project.feature.home.component.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.starter.project.base.data.model.zenn.Article
import org.starter.project.base.data.model.zenn.Articles


class ArticlesPagingSource(
    private val onLoadedFirstPage: () -> Unit = {},
    private val fetcher: suspend (key: String?) -> Articles?,
) : PagingSource<String, Article>() {

    companion object {
        const val PAGE_SIZE = 20
    }

    override fun getRefreshKey(state: PagingState<String, Article>): String? {
        return null
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Article> {
        return try {
            val currentKey = params.key
            val data = fetcher(currentKey)
            val nextKey = data?.nextPage
            currentKey ?: onLoadedFirstPage()
            LoadResult.Page(
                data = data?.articles.orEmpty(),
                prevKey = currentKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
