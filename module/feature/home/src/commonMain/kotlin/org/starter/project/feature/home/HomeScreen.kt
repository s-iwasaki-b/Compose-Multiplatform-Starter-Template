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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import org.starter.project.base.data.model.zenn.Article
import org.starter.project.ui.design.system.scaffold.SystemScaffold
import org.starter.project.ui.design.system.search.SystemSearchBar
import org.starter.project.ui.design.system.theme.SystemTheme
import org.starter.project.ui.route.AppRoute
import org.starter.project.ui.route.AppRouter
import org.starter.project.ui.shared.component.article.articleList
import org.starter.project.ui.shared.event.ScreenEvent

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    appRouter: AppRouter,
    navArgs: AppRoute.Home.NavArgs
) {
    val state by viewModel.state.collectAsState()
    val articlesPagingItems = viewModel.articlesPagingFlow.collectAsLazyPagingItems()

    // This is an example of lifecycle event listener
    // cf. https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-lifecycle.html#mapping-android-lifecycle-to-other-platforms
    LifecycleEventEffect(Lifecycle.Event.ON_START) { Napier.d { "HomeScreen.onStart" } }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { Napier.d { "HomeScreen.onResume" } }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { Napier.d { "HomeScreen.onPause" } }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { Napier.d { "HomeScreen.onStop" } }

    LaunchedEffect(navArgs.keyword) {
        navArgs.keyword?.let { viewModel.updateSearchKeyword(it) }
    }

    HomeScreenContent(
        state = state,
        articlesPagingItems = articlesPagingItems,
    ) { event ->
        HomeScreenEventHandler(
            event = event,
            appRouter = appRouter,
            viewModel = viewModel,
            articlesPagingItems = articlesPagingItems
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeScreenContent(
    state: HomeScreenState,
    articlesPagingItems: LazyPagingItems<Article>,
    dispatch: (event: ScreenEvent) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        snapshotFlow { articlesPagingItems.loadState.refresh }
            .drop(1) // 画面復帰時のスクロール位置リセットを防ぐ
            .filter { it is LoadState.NotLoading }
            .collect { listState.scrollToItem(0) }
    }

    SystemScaffold(
        modifier = Modifier.fillMaxSize(),
        screenState = state.screenState,
        onClickErrorActionButton = {
            dispatch(HomeScreenEvent.OnClickErrorScreenAction)
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
                    onClickClear = { dispatch(HomeScreenEvent.OnClickClearSearchKeyword) },
                    onClickAction = { dispatch(HomeScreenEvent.OnClickActionSearchKeyword) }
                )
            }
            articleList(
                articlesPagingItems = articlesPagingItems,
                onClickUser = { dispatch(HomeScreenEvent.OnClickUser(it)) },
            )
        }
    }
}
