package org.starter.project.data.zenn.datasource.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    @SerialName("user")
    val user: UserInnerResponse?
)

@Serializable
data class UserInnerResponse(
    @SerialName("id")
    val id: Int?,
    @SerialName("username")
    val username: String?,
    @SerialName("name")
    val name: String?,
    @SerialName("avatar_small_url")
    val avatarSmallUrl: String?,
    @SerialName("avatar_url")
    val avatarUrl: String?,
    @SerialName("bio")
    val bio: String?,
    @SerialName("follower_count")
    val followerCount: Int?,
    @SerialName("following_count")
    val followingCount: Int?,
    @SerialName("articles_count")
    val articlesCount: Int?,
)
