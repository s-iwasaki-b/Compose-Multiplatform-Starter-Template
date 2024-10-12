package org.starter.project.feature.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import io.github.aakira.napier.Napier
import org.koin.compose.viewmodel.koinViewModel
import org.starter.project.base.data.model.zenn.Article
import org.starter.project.feature.home.component.article.articleList
import org.starter.project.ui.design.system.scaffold.DesignSystemScaffold
import org.starter.project.ui.design.system.search.SearchBar
import org.starter.project.ui.shared.event.ScreenEvent

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val articlesPagingItems = viewModel.articlesPagingFlow.collectAsLazyPagingItems()

    // This is an example of lifecycle event listener
    // cf. https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-lifecycle.html#mapping-android-lifecycle-to-other-platforms
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        Napier.d { "HomeScreen.onStart" }
        viewModel.initKeyword()
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
                focusManager = focusManager,
                articlesPagingItems = articlesPagingItems
            )
        }
    )
}

@Composable
private fun HomeScreenContent(
    state: HomeScreenState,
    articlesPagingItems: LazyPagingItems<Article>,
    dispatch: (event: ScreenEvent) -> Unit
) {
    DesignSystemScaffold(
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
                .padding(horizontal = 16.dp)
        ) {
            item(key = "search_bar") {
                Spacer(modifier = Modifier.height(16.dp))
                SearchBar(
                    value = state.searchKeyword,
                    onValueChange = { dispatch(HomeScreenEvent.OnChangeSearchKeyword(it)) },
                    onTapClear = { dispatch(HomeScreenEvent.OnTapClearSearchKeyword) },
                    onTapAction = { dispatch(HomeScreenEvent.OnTapActionSearchKeyword) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            articleList(articlesPagingItems)
        }
    }
}
