package org.starter.project.feature.home

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.starter.project.base.data.model.zenn.Articles
import org.starter.project.base.extension.handle
import org.starter.project.domain.service.ZennService
import org.starter.project.ui.shared.component.article.ArticlesPagingSource
import org.starter.project.ui.shared.handler.ErrorScreenThrowableHandler
import org.starter.project.ui.shared.handler.IgnoreThrowableHandler
import org.starter.project.ui.shared.state.ScreenLoadingState
import org.starter.project.ui.shared.state.ScreenState
import kotlin.time.Duration.Companion.milliseconds

class HomeScreenViewModel(
    private val zennService: ZennService
) : ViewModel() {
    private val _screenState = MutableStateFlow(
        ScreenState(ScreenLoadingState.Initial(true), null)
    )
    private val _state = MutableStateFlow(
        HomeScreenState(screenState = _screenState.value)
    )
    internal val state = combine(
        _screenState,
        _state
    ) { screenState, state ->
        state.copy(screenState = screenState)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        _state.value
    )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val articlesPagingFlow = _state
        .map { it.searchKeyword }
        .distinctUntilChanged()
        .debounce(300.milliseconds)
        .flatMapLatest { searchKeyword ->
            Pager(
                PagingConfig(ArticlesPagingSource.PAGE_SIZE)
            ) {
                ArticlesPagingSource(
                    onRefresh = ::updateScreenLoading,
                    onLoadedFirstPage = ::updateScreenSuccess,
                    fetcher = { key -> fetchArticles(searchKeyword, key) }
                )
            }.flow
        }.cachedIn(viewModelScope)

    @VisibleForTesting
    internal fun updateScreenLoading() {
        _screenState.update {
            it.copy(screenLoadingState = ScreenLoadingState.Loading())
        }
    }

    @VisibleForTesting
    internal fun updateScreenSuccess() {
        if (_screenState.value.screenLoadingState !is ScreenLoadingState.Failure) {
            _screenState.update {
                it.copy(screenLoadingState = ScreenLoadingState.Success())
            }
        }
    }

    @VisibleForTesting
    internal suspend fun fetchArticles(keyword: String, key: String?): Articles? {
        return zennService.fetchArticles(
            keyword = keyword,
            nextPage = key
        ).handle(ErrorScreenThrowableHandler(_screenState))
    }

    fun initSearchKeyword() {
        val lastKeyword = zennService.getLastKeyword().handle(IgnoreThrowableHandler()).orEmpty()
        _state.update {
            it.copy(
                searchKeyword = lastKeyword
            )
        }
    }

    fun updateSearchKeyword(keyword: String) {
        _state.update {
            it.copy(
                searchKeyword = keyword
            )
        }
    }
}
