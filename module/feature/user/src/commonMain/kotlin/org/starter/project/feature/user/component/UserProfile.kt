package org.starter.project.feature.user.component

import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import org.starter.project.base.data.model.zenn.User
import org.starter.project.feature.user.resources.Res
import org.starter.project.feature.user.resources.user_stat_articles
import org.starter.project.feature.user.resources.user_stat_followers
import org.starter.project.feature.user.resources.user_stat_following
import org.starter.project.ui.design.system.theme.SystemTheme

private val ExpandedAvatarSize = 80.dp
private val CollapsedAvatarSize = 40.dp
private val ExpandedVerticalPadding = 16.dp
private val CollapsedVerticalPadding = 8.dp

@Composable
internal fun UserProfile(
    user: User,
    collapseFraction: Float = 0f,
) {
    val fraction = collapseFraction.coerceIn(0f, 1f)

    val avatarSize = lerpDp(ExpandedAvatarSize, CollapsedAvatarSize, fraction)
    val verticalPadding = lerpDp(ExpandedVerticalPadding, CollapsedVerticalPadding, fraction)
    val bioAlpha = lerp(1f, 0f, (fraction * 2f).coerceAtMost(1f))
    val statsAlpha = lerp(1f, 0f, ((fraction - 0.3f) * 2.5f).coerceIn(0f, 1f))
    val verticalSpacing = lerpDp(8.dp, 0.dp, fraction)

    // fraction=0: 純粋な縦並び, fraction=1: 横並び
    if (fraction < 1f) {
        // 縦並び → 横並びの過渡レイアウト
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = verticalPadding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // fraction に応じて左寄せ幅を補間
                // 0f: avatar は中央配置, 1f: avatar は左端
                val horizontalWeight = lerp(1f, 0f, fraction)
                if (horizontalWeight > 0.01f) {
                    Spacer(modifier = Modifier.weight(horizontalWeight))
                }

                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape),
                )

                if (fraction > 0.01f) {
                    // 横並び時に表示されるテキスト群
                    Spacer(modifier = Modifier.width(lerpDp(0.dp, 12.dp, fraction)))
                    Column(
                        modifier = Modifier
                            .alpha(fraction)
                            .weight(1f),
                    ) {
                        Text(
                            style = SystemTheme.typography.subtitle1,
                            text = user.name,
                        )
                        Text(
                            style = SystemTheme.typography.caption,
                            color = Color.Gray,
                            text = "@${user.username}",
                        )
                    }
                }

                if (horizontalWeight > 0.01f) {
                    Spacer(modifier = Modifier.weight(horizontalWeight))
                }
            }

            // 縦並び時にのみ表示される要素群 (fraction の進行でフェードアウト)
            if (fraction < 0.99f) {
                Spacer(modifier = Modifier.height(verticalSpacing))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                ) {
                    // 名前・ユーザー名 (横並び側にもあるので、フェードアウト)
                    Column(
                        modifier = Modifier.alpha(1f - fraction),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            style = SystemTheme.typography.h6,
                            text = user.name,
                        )
                        Text(
                            style = SystemTheme.typography.caption,
                            color = Color.Gray,
                            text = "@${user.username}",
                        )
                    }

                    if (user.bio.isNotEmpty() && bioAlpha > 0.01f) {
                        Text(
                            style = SystemTheme.typography.body2,
                            text = user.bio,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .alpha(bioAlpha),
                        )
                    }

                    if (statsAlpha > 0.01f) {
                        Row(
                            modifier = Modifier.alpha(statsAlpha),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            UserStat(label = stringResource(Res.string.user_stat_articles), count = user.articlesCount)
                            UserStat(label = stringResource(Res.string.user_stat_followers), count = user.followerCount)
                            UserStat(label = stringResource(Res.string.user_stat_following), count = user.followingCount)
                        }
                    }
                }
            }
        }
    } else {
        // 完全にコラプスした状態: コンパクトな横並び
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(CollapsedAvatarSize)
                    .clip(CircleShape),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    style = SystemTheme.typography.subtitle1,
                    text = user.name,
                )
                Text(
                    style = SystemTheme.typography.caption,
                    color = Color.Gray,
                    text = "@${user.username}",
                )
            }
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

private fun lerpDp(start: Dp, stop: Dp, fraction: Float): Dp {
    return Dp(lerp(start.value, stop.value, fraction))
}

@Preview
@Composable
private fun UserProfileExpandedPreview() {
    SystemTheme {
        UserProfile(
            user = previewUser(),
            collapseFraction = 0f,
        )
    }
}

@Preview
@Composable
private fun UserProfileCollapsedPreview() {
    SystemTheme {
        UserProfile(
            user = previewUser(),
            collapseFraction = 1f,
        )
    }
}

private fun previewUser() = User(
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
