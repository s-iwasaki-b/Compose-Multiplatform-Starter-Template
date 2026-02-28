package org.starter.project.feature.user.component

import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.starter.project.base.data.model.zenn.User
import org.starter.project.feature.user.resources.Res
import org.starter.project.feature.user.resources.user_stat_articles
import org.starter.project.feature.user.resources.user_stat_followers
import org.starter.project.feature.user.resources.user_stat_following
import org.starter.project.ui.design.system.theme.SystemTheme

@Composable
internal fun UserProfile(user: User) {
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
            UserStat(label = stringResource(Res.string.user_stat_articles), count = user.articlesCount)
            UserStat(label = stringResource(Res.string.user_stat_followers), count = user.followerCount)
            UserStat(label = stringResource(Res.string.user_stat_following), count = user.followingCount)
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

@Preview
@Composable
private fun UserProfileHeaderPreview() {
    SystemTheme {
        UserProfile(
            user = User(
                id = 1,
                username = "johndoe",
                name = "John Doe",
                avatarSmallUrl = "",
                avatarUrl = "",
                bio = "Android & iOS developer",
                followerCount = 120,
                followingCount = 45,
                articlesCount = 30,
            )
        )
    }
}
