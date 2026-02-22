package org.starter.project.base.data.model.zenn

import androidx.compose.runtime.Immutable

@Immutable
data class User(
    val id: Int,
    val username: String,
    val name: String,
    val avatarSmallUrl: String,
    val avatarUrl: String,
    val bio: String,
    val followerCount: Int,
    val followingCount: Int,
    val articlesCount: Int,
)
