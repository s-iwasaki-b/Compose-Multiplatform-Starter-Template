package org.starter.project.feature.user.component

import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import kotlin.math.roundToInt
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
private val ExpandedElementSpacing = 8.dp
private val CollapsedGap = 12.dp
private val CollapsedRowSpacing = 4.dp
private val ExpandedStatsSpacing = 24.dp
private val CollapsedStatsSpacing = 16.dp

@Composable
internal fun UserProfile(
    user: User,
    collapseFraction: Float = 0f,
    modifier: Modifier = Modifier,
) {
    val fraction = collapseFraction.coerceIn(0f, 1f)

    val avatarSizeDp = lerpDp(ExpandedAvatarSize, CollapsedAvatarSize, fraction)
    val verticalPadding = lerpDp(ExpandedVerticalPadding, CollapsedVerticalPadding, fraction)
    val bioAlpha = lerp(1f, 0f, (fraction * 2f).coerceAtMost(1f))
    val statsSpacing = lerpDp(ExpandedStatsSpacing, CollapsedStatsSpacing, fraction)

    Layout(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding)
            .clipToBounds(),
        content = {
            // 0: Avatar
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(avatarSizeDp)
                    .clip(CircleShape),
            )
            // 1: Name
            Text(
                style = SystemTheme.typography.subtitle1,
                text = user.name,
            )
            // 2: Username
            Text(
                style = SystemTheme.typography.caption,
                color = Color.Gray,
                text = "@${user.username}",
            )
            // 3: Bio (フェードアウト、常に emit して高さ 0 で対応)
            Box(modifier = Modifier.alpha(bioAlpha)) {
                if (user.bio.isNotEmpty()) {
                    Text(
                        style = SystemTheme.typography.body2,
                        text = user.bio,
                    )
                }
            }
            // 4: Stats Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(statsSpacing),
            ) {
                UserStat(
                    label = stringResource(Res.string.user_stat_articles),
                    count = user.articlesCount,
                )
                UserStat(
                    label = stringResource(Res.string.user_stat_followers),
                    count = user.followerCount,
                )
                UserStat(
                    label = stringResource(Res.string.user_stat_following),
                    count = user.followingCount,
                )
            }
        }
    ) { measurables, constraints ->
        val expandedAvatarPx = ExpandedAvatarSize.roundToPx()
        val collapsedAvatarPx = CollapsedAvatarSize.roundToPx()
        val spacing = ExpandedElementSpacing.roundToPx()
        val gap = CollapsedGap.roundToPx()
        val totalWidth = constraints.maxWidth

        // ── Measure ──
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val avatarPlaceable = measurables[0].measure(looseConstraints)
        val namePlaceable = measurables[1].measure(looseConstraints)
        val usernamePlaceable = measurables[2].measure(looseConstraints)
        val bioPlaceable = measurables[3].measure(looseConstraints)
        val statsPlaceable = measurables[4].measure(looseConstraints)

        // ── Expanded: 中央揃え縦並び ──
        val expAvatarX = (totalWidth - expandedAvatarPx) / 2
        val expAvatarY = 0

        val expNameX = (totalWidth - namePlaceable.width) / 2
        val expNameY = expandedAvatarPx + spacing

        val expUsernameX = (totalWidth - usernamePlaceable.width) / 2
        val expUsernameY = expNameY + namePlaceable.height + spacing

        val expBioX = (totalWidth - bioPlaceable.width) / 2
        val expBioY = expUsernameY + usernamePlaceable.height +
            if (bioPlaceable.height > 0) spacing else 0

        val expStatsX = (totalWidth - statsPlaceable.width) / 2
        val expStatsY = expBioY + bioPlaceable.height + spacing

        val expandedHeight = expStatsY + statsPlaceable.height

        // ── Collapsed: 中央揃え2段レイアウト ──
        // Row 1: [Avatar][gap][Name / Username 縦並び] を中央揃え
        val rowSpacing = CollapsedRowSpacing.roundToPx()
        val nameUsernameWidth = maxOf(namePlaceable.width, usernamePlaceable.width)
        val row1BlockWidth = collapsedAvatarPx + gap + nameUsernameWidth
        val row1BlockX = (totalWidth - row1BlockWidth) / 2

        val nameUsernameHeight = namePlaceable.height + usernamePlaceable.height
        val row1Height = maxOf(collapsedAvatarPx, nameUsernameHeight)

        val colAvatarX = row1BlockX
        val colAvatarY = (row1Height - collapsedAvatarPx) / 2

        val textStartX = row1BlockX + collapsedAvatarPx + gap
        val textBlockTopY = (row1Height - nameUsernameHeight) / 2

        val colNameX = textStartX
        val colNameY = textBlockTopY

        // Username: Name の直下
        val colUsernameX = textStartX
        val colUsernameY = textBlockTopY + namePlaceable.height

        // Bio: 下方向へスライドして clipToBounds で隠れる
        val colBioX = textStartX
        val colBioY = row1Height + rowSpacing + statsPlaceable.height

        // Stats: Row 2 中央揃え
        val colStatsX = (totalWidth - statsPlaceable.width) / 2
        val colStatsY = row1Height + rowSpacing

        // ── Lerp ──
        val collapsedHeight = colStatsY + statsPlaceable.height
        val height = lerp(
            expandedHeight.toFloat(),
            collapsedHeight.toFloat(),
            fraction,
        ).roundToInt()

        layout(totalWidth, height) {
            avatarPlaceable.place(
                x = lerpRound(expAvatarX, colAvatarX, fraction),
                y = lerpRound(expAvatarY, colAvatarY, fraction),
            )
            namePlaceable.place(
                x = lerpRound(expNameX, colNameX, fraction),
                y = lerpRound(expNameY, colNameY, fraction),
            )
            usernamePlaceable.place(
                x = lerpRound(expUsernameX, colUsernameX, fraction),
                y = lerpRound(expUsernameY, colUsernameY, fraction),
            )
            bioPlaceable.place(
                x = lerpRound(expBioX, colBioX, fraction),
                y = lerpRound(expBioY, colBioY, fraction),
            )
            statsPlaceable.place(
                x = lerpRound(expStatsX, colStatsX, fraction),
                y = lerpRound(expStatsY, colStatsY, fraction),
            )
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

private fun lerpRound(start: Int, stop: Int, fraction: Float): Int {
    return lerp(start.toFloat(), stop.toFloat(), fraction).roundToInt()
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
