package org.starter.project.data.zenn.datasource.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    @SerialName("user")
    val user: UserInnerResponse? = null
)

@Serializable
data class UserInnerResponse(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("username")
    val username: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("avatar_small_url")
    val avatarSmallUrl: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("bio")
    val bio: String? = null,
    @SerialName("follower_count")
    val followerCount: Int? = null,
    @SerialName("following_count")
    val followingCount: Int? = null,
    @SerialName("articles_count")
    val articlesCount: Int? = null,
)
