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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
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

    // ── collapse 可否判定 ──
    // derivedStateOf 内で直接 maxScroll を評価すると、以下のフィードバックループが発生する:
    //   collapse → ビューポート拡大 → maxScroll 減少 → expand → ビューポート縮小 → maxScroll 増加 → collapse …
    // これを避けるため、snapshotFlow で「非スクロール中 + LazyColumn 先頭」の安定状態でのみ評価し、
    // スクロール中やiOSバウンス中の不安定な layoutInfo には依存しない設計にしている。
    var collapseEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(collapseThreshold) {
        snapshotFlow {
            // スクロール中は再評価しない（キャッシュされた値を維持）。
            // iOS のバウンス（ラバーバンド）中に layoutInfo が不安定になる問題も回避できる。
            if (listState.isScrollInProgress) return@snapshotFlow null
            // LazyColumn が先頭にあるとき＝UserProfile が Expanded 状態のときのみ評価。
            // Expanded 状態のビューポートサイズを基準に maxScroll を計算することで、
            // collapse 状態のビューポート拡大を考慮した正確な判定ができる。
            if (listState.firstVisibleItemIndex != 0 ||
                listState.firstVisibleItemScrollOffset != 0
            ) {
                return@snapshotFlow null
            }

            val threshold = collapseThreshold.coerceAtLeast(1)
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@snapshotFlow false

            // LazyColumn は画面外アイテムを measure しないため、
            // 全アイテムが画面内にある場合は正確に、そうでなければ平均高さから推定する。
            val totalItemsCount = layoutInfo.totalItemsCount
            val maxScroll = if (visibleItems.size >= totalItemsCount) {
                val lastItem = visibleItems.last()
                val contentBottom =
                    lastItem.offset + lastItem.size + layoutInfo.afterContentPadding
                (contentBottom - layoutInfo.viewportSize.height).coerceAtLeast(0)
            } else {
                val avgItemHeight = visibleItems.sumOf { it.size } / visibleItems.size
                val estimatedContentHeight = avgItemHeight * totalItemsCount +
                    layoutInfo.beforeContentPadding + layoutInfo.afterContentPadding
                (estimatedContentHeight - layoutInfo.viewportSize.height).coerceAtLeast(0)
            }
            // UserProfile が Collapsed になるとビューポートが threshold 分広がる。
            // そのため Expanded 時点で threshold * 2 以上の余地がないと、
            // Collapsed 後にスクロール可能量が不足し、iOS のバウンスで
            // Expanded/Collapsed 間の不安定な遷移が発生する。
            maxScroll >= threshold * 2
        }.collect { result ->
            if (result != null) {
                collapseEnabled = result
            }
        }
    }

    // ── collapseFraction: collapseEnabled をゲートとし、スクロール位置から導出 ──
    val collapseFraction by remember {
        derivedStateOf {
            if (!collapseEnabled) return@derivedStateOf 0f

            val threshold = collapseThreshold.coerceAtLeast(1)
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf 0f

            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset
            val actualScroll = if (firstIndex == 0) {
                firstOffset.toFloat()
            } else {
                // 画面外アイテムの高さは表示中アイテムの平均で推定
                val avgItemHeight =
                    visibleItems.sumOf { it.size }.toFloat() / visibleItems.size
                firstIndex * avgItemHeight + firstOffset
            }
            (actualScroll / threshold).coerceIn(0f, 1f)
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
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                articleList(articlesPagingItems)
            }
        }
    }
}
