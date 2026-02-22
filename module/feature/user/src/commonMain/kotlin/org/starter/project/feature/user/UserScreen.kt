package org.starter.project.feature.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import org.starter.project.base.data.model.zenn.Article
import org.starter.project.base.data.model.zenn.User
import org.starter.project.ui.shared.component.article.articleList
import org.starter.project.ui.design.system.scaffold.SystemScaffold
import org.starter.project.ui.design.system.theme.SystemTheme
import org.starter.project.ui.route.AppRouter
import org.starter.project.ui.shared.event.ScreenEvent

@Composable
fun UserScreen(
    viewModel: UserScreenViewModel,
    appRouter: AppRouter,
) {
    val state by viewModel.state.collectAsState()
    val articlesPagingItems = viewModel.articlesPagingFlow.collectAsLazyPagingItems()

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
                            contentDescription = "Back",
                        )
                    }
                },
                backgroundColor = SystemTheme.colors.background,
                elevation = 0.dp,
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            state.user?.let { user ->
                item(key = "user_profile_header") {
                    UserProfileHeader(user = user)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            articleList(articlesPagingItems)
        }
    }
}

@Composable
private fun UserProfileHeader(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AsyncImage(
            model = user.avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
        )
        Text(
            style = SystemTheme.typography.h6,
            text = user.name,
        )
        Text(
            style = SystemTheme.typography.caption,
            color = Color.Gray,
            text = "@${user.username}",
        )
        if (user.bio.isNotEmpty()) {
            Text(
                style = SystemTheme.typography.body2,
                text = user.bio,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            UserStat(label = "Articles", count = user.articlesCount)
            UserStat(label = "Followers", count = user.followerCount)
            UserStat(label = "Following", count = user.followingCount)
        }
    }
}

@Composable
private fun UserStat(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            style = SystemTheme.typography.subtitle2,
            text = count.toString(),
        )
        Text(
            style = SystemTheme.typography.caption,
            color = Color.Gray,
            text = label,
        )
    }
}
