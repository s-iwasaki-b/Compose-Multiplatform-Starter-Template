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
import androidx.compose.ui.layout.Placeable
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

// ─── 定数 ───────────────────────────────────────────────────────────

private val ExpandedAvatarSize = 80.dp
private val CollapsedAvatarSize = 40.dp
private val ExpandedVerticalPadding = 16.dp
private val CollapsedVerticalPadding = 8.dp
private val ExpandedElementSpacing = 8.dp
private val CollapsedGap = 8.dp
private val ExpandedStatsSpacing = 24.dp
private val CollapsedStatsSpacing = 16.dp

// ─── レイアウト座標モデル ────────────────────────────────────────────

/** 要素の配置座標 (px) */
private data class Position(val x: Int, val y: Int)

/** 全要素の配置座標と合計高さ */
private data class LayoutPositions(
    val avatar: Position,
    val name: Position,
    val username: Position,
    val bio: Position,
    val stats: Position,
    val totalHeight: Int,
)

// ─── メイン Composable ──────────────────────────────────────────────

@Composable
internal fun UserProfile(
    user: User,
    collapseFraction: Float = 0f,
    onHeightDeltaCalculated: ((Int) -> Unit)? = null,
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
            // 3: Bio（フェードアウト対象）
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
        val totalWidth = constraints.maxWidth

        // ── 1. Measure ──────────────────────────────────────────
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val avatarPlaceable = measurables[0].measure(looseConstraints)
        val namePlaceable = measurables[1].measure(looseConstraints)
        val usernamePlaceable = measurables[2].measure(looseConstraints)
        val bioPlaceable = measurables[3].measure(looseConstraints)
        val statsPlaceable = measurables[4].measure(looseConstraints)

        // ── 2. 各状態の座標を算出 ───────────────────────────────
        val expanded = calculateExpandedPositions(
            totalWidth = totalWidth,
            avatarSize = ExpandedAvatarSize.roundToPx(),
            spacing = ExpandedElementSpacing.roundToPx(),
            namePlaceable = namePlaceable,
            usernamePlaceable = usernamePlaceable,
            bioPlaceable = bioPlaceable,
            statsPlaceable = statsPlaceable,
        )
        val collapsed = calculateCollapsedPositions(
            totalWidth = totalWidth,
            avatarSize = CollapsedAvatarSize.roundToPx(),
            gap = CollapsedGap.roundToPx(),
            namePlaceable = namePlaceable,
            usernamePlaceable = usernamePlaceable,
            statsPlaceable = statsPlaceable,
        )

        // ── 3. 高さの差分を報告（verticalPadding を含めた実際の表示高さ）
        val expandedPaddingPx = ExpandedVerticalPadding.roundToPx() * 2
        val collapsedPaddingPx = CollapsedVerticalPadding.roundToPx() * 2
        val heightDelta =
            (expanded.totalHeight + expandedPaddingPx) -
            (collapsed.totalHeight + collapsedPaddingPx)
        onHeightDeltaCalculated?.invoke(heightDelta)

        // ── 4. 高さを補間して配置 ──────────────────────────────
        val height = lerpRound(expanded.totalHeight, collapsed.totalHeight, fraction)

        layout(totalWidth, height) {
            placeWithLerp(avatarPlaceable, expanded.avatar, collapsed.avatar, fraction)
            placeWithLerp(namePlaceable, expanded.name, collapsed.name, fraction)
            placeWithLerp(usernamePlaceable, expanded.username, collapsed.username, fraction)
            placeWithLerp(bioPlaceable, expanded.bio, collapsed.bio, fraction)
            placeWithLerp(statsPlaceable, expanded.stats, collapsed.stats, fraction)
        }
    }
}

// ─── Expanded レイアウト計算 ─────────────────────────────────────────
//
//  中央揃え縦並び:
//    [       Avatar       ]
//    [        Name        ]
//    [      Username      ]
//    [        Bio         ]  ← fraction に応じてフェードアウト
//    [       Stats        ]
//

private fun calculateExpandedPositions(
    totalWidth: Int,
    avatarSize: Int,
    spacing: Int,
    namePlaceable: Placeable,
    usernamePlaceable: Placeable,
    bioPlaceable: Placeable,
    statsPlaceable: Placeable,
): LayoutPositions {
    var y = 0

    val avatar = Position(x = (totalWidth - avatarSize) / 2, y = y)
    y += avatarSize + spacing

    val name = Position(x = (totalWidth - namePlaceable.width) / 2, y = y)
    y += namePlaceable.height + spacing

    val username = Position(x = (totalWidth - usernamePlaceable.width) / 2, y = y)
    y += usernamePlaceable.height + if (bioPlaceable.height > 0) spacing else 0

    val bio = Position(x = (totalWidth - bioPlaceable.width) / 2, y = y)
    y += bioPlaceable.height + spacing

    val stats = Position(x = (totalWidth - statsPlaceable.width) / 2, y = y)
    y += statsPlaceable.height

    return LayoutPositions(
        avatar = avatar,
        name = name,
        username = username,
        bio = bio,
        stats = stats,
        totalHeight = y,
    )
}

// ─── Collapsed レイアウト計算 ────────────────────────────────────────
//
//  1行配置:
//    [Avatar][gap][Name   ]          [  Stats  ]
//                 [Username]
//
//  Avatar + Name/Username は左端、Stats は右端。
//  Bio は行の下へ押し出して clipToBounds で隠す。
//

private fun calculateCollapsedPositions(
    totalWidth: Int,
    avatarSize: Int,
    gap: Int,
    namePlaceable: Placeable,
    usernamePlaceable: Placeable,
    statsPlaceable: Placeable,
): LayoutPositions {
    val nameUsernameHeight = namePlaceable.height + usernamePlaceable.height
    val rowHeight = maxOf(avatarSize, nameUsernameHeight, statsPlaceable.height)

    // Avatar: 左端、垂直中央
    val avatar = Position(
        x = 0,
        y = (rowHeight - avatarSize) / 2,
    )

    // Name / Username: Avatar の右、垂直中央に2行ブロック
    val textStartX = avatarSize + gap
    val textBlockTopY = (rowHeight - nameUsernameHeight) / 2

    val name = Position(x = textStartX, y = textBlockTopY)
    val username = Position(x = textStartX, y = textBlockTopY + namePlaceable.height)

    // Bio: 行の下に押し出す（clipToBounds で不可視）
    val bio = Position(x = textStartX, y = rowHeight)

    // Stats: 右端、垂直中央
    val stats = Position(
        x = totalWidth - statsPlaceable.width,
        y = (rowHeight - statsPlaceable.height) / 2,
    )

    return LayoutPositions(
        avatar = avatar,
        name = name,
        username = username,
        bio = bio,
        stats = stats,
        totalHeight = rowHeight,
    )
}

// ─── ヘルパー関数 ───────────────────────────────────────────────────

/** expanded/collapsed 座標を fraction で補間して配置する */
private fun Placeable.PlacementScope.placeWithLerp(
    placeable: Placeable,
    expanded: Position,
    collapsed: Position,
    fraction: Float,
) {
    placeable.place(
        x = lerpRound(expanded.x, collapsed.x, fraction),
        y = lerpRound(expanded.y, collapsed.y, fraction),
    )
}

private fun lerpDp(start: Dp, stop: Dp, fraction: Float): Dp {
    return Dp(lerp(start.value, stop.value, fraction))
}

private fun lerpRound(start: Int, stop: Int, fraction: Float): Int {
    return lerp(start.toFloat(), stop.toFloat(), fraction).roundToInt()
}

// ─── サブ Composable ────────────────────────────────────────────────

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

// ─── Preview ────────────────────────────────────────────────────────

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
