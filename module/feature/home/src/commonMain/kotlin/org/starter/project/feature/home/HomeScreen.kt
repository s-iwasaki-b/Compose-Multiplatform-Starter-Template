package org.starter.project.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.paging.LoadState
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import io.github.aakira.napier.Napier
import org.koin.compose.viewmodel.koinViewModel
import org.starter.project.base.data.model.zenn.Article
import org.starter.project.feature.home.component.article.articleList
import org.starter.project.ui.design.system.scaffold.SystemScaffold
import org.starter.project.ui.design.system.search.SystemSearchBar
import org.starter.project.ui.design.system.theme.SystemTheme
import org.starter.project.ui.route.Router
import org.starter.project.ui.shared.event.ScreenEvent

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = koinViewModel(),
    appRouter: Router
) {
    val state by viewModel.state.collectAsState()
    val articlesPagingItems = viewModel.articlesPagingFlow.collectAsLazyPagingItems()

    // This is an example of lifecycle event listener
    // cf. https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-lifecycle.html#mapping-android-lifecycle-to-other-platforms
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        Napier.d { "HomeScreen.onStart" }
        viewModel.initSearchKeyword()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { Napier.d { "HomeScreen.onResume" } }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { Napier.d { "HomeScreen.onPause" } }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { Napier.d { "HomeScreen.onStop" } }

    HomeScreenContent(
        state = state,
        articlesPagingItems = articlesPagingItems,
        dispatch = { event ->
            HomeScreenEventHandler(
                event = event,
                viewModel = viewModel,
                articlesPagingItems = articlesPagingItems
            )
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeScreenContent(
    state: HomeScreenState,
    articlesPagingItems: LazyPagingItems<Article>,
    dispatch: (event: ScreenEvent) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(articlesPagingItems.loadState.refresh) {
        if (articlesPagingItems.loadState.refresh is LoadState.NotLoading) {
            listState.scrollToItem(0)
        }
    }

    SystemScaffold(
        modifier = Modifier.fillMaxSize(),
        screenState = state.screenState,
        onTapErrorActionButton = {
            dispatch(HomeScreenEvent.OnTapErrorScreenAction)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            state = listState
        ) {
            stickyHeader(key = "search_bar") {
                SystemSearchBar(
                    modifier = Modifier
                        .background(SystemTheme.colors.background)
                        .padding(vertical = 16.dp),
                    value = state.searchKeyword,
                    onValueChange = { dispatch(HomeScreenEvent.OnChangeSearchKeyword(it)) },
                    onTapClear = { dispatch(HomeScreenEvent.OnTapClearSearchKeyword) },
                    onTapAction = { dispatch(HomeScreenEvent.OnTapActionSearchKeyword) }
                )
            }
            articleList(articlesPagingItems)
        }
    }
}
