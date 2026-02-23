package org.starter.project.feature.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import org.starter.project.base.data.model.zenn.Article
import org.starter.project.feature.user.component.UserProfile
import org.starter.project.ui.shared.component.article.articleList
import org.starter.project.ui.design.system.scaffold.SystemScaffold
import org.starter.project.ui.design.system.theme.SystemTheme
import org.starter.project.ui.route.AppRoute
import org.starter.project.ui.route.AppRouter
import org.starter.project.ui.shared.event.ScreenEvent

@Composable
fun UserScreen(
    viewModel: UserScreenViewModel,
    appRouter: AppRouter,
    navArgs: AppRoute.User.NavArgs
) {
    val state by viewModel.state.collectAsState()
    val articlesPagingItems = viewModel.articlesPagingFlow.collectAsLazyPagingItems()

    LaunchedEffect(navArgs.username) {
        viewModel.refreshUser(navArgs.username)
    }

    UserScreenContent(
        state = state,
        articlesPagingItems = articlesPagingItems
    ) { event ->
        UserScreenEventHandler(
            event = event,
            appRouter = appRouter,
            articlesPagingItems = articlesPagingItems
        )
    }
}

@Composable
private fun UserScreenContent(
    state: UserScreenState,
    articlesPagingItems: LazyPagingItems<Article>,
    dispatch: (event: ScreenEvent) -> Unit,
) {
    val listState = rememberLazyListState()

    // UserProfile の expanded/collapsed 高さ差分を閾値として使用
    var collapseThreshold by remember { mutableIntStateOf(1) }

    // LazyColumn の累積スクロール量を追跡
    val accumulatedScroll = remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                // consumed.y < 0 でスクロール↓、> 0 でスクロール↑
                accumulatedScroll.floatValue =
                    (accumulatedScroll.floatValue - consumed.y).coerceAtLeast(0f)
                return Offset.Zero
            }
        }
    }

    val collapseFraction by remember {
        derivedStateOf {
            val threshold = collapseThreshold.coerceAtLeast(1)
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo

            // 全アイテムが画面内に収まり、スクロール可能量が閾値未満なら常に展開
            if (visibleItems.isNotEmpty() && visibleItems.size >= layoutInfo.totalItemsCount) {
                val lastItem = visibleItems.last()
                val contentBottom = lastItem.offset + lastItem.size + layoutInfo.afterContentPadding
                val maxScroll = (contentBottom - layoutInfo.viewportSize.height).coerceAtLeast(0)
                if (maxScroll < threshold) {
                    return@derivedStateOf 0f
                }
            }

            (accumulatedScroll.floatValue / threshold).coerceIn(0f, 1f)
        }
    }

    SystemScaffold(
        modifier = Modifier.fillMaxSize(),
        screenState = state.screenState,
        onClickErrorActionButton = {
            dispatch(UserScreenEvent.OnClickErrorScreenAction)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.user?.name.orEmpty(),
                        style = SystemTheme.typography.subtitle1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { dispatch(UserScreenEvent.OnClickBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                backgroundColor = SystemTheme.colors.background,
                elevation = 0.dp,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            // UserProfile は LazyColumn の外に配置し、常に画面上部に固定
            state.user?.let { user ->
                UserProfile(
                    user = user,
                    collapseFraction = collapseFraction,
                    onHeightDeltaCalculated = { delta ->
                        if (collapseThreshold != delta) {
                            collapseThreshold = delta
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }
            Divider()
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(top = 16.dp),
                modifier = Modifier
                    .nestedScroll(nestedScrollConnection)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                articleList(articlesPagingItems)
            }
        }
    }
}
