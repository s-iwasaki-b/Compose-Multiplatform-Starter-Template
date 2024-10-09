package org.starter.project.data.zenn.datasource.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticlesResponse(
    @SerialName("articles")
    val articles: List<ArticleResponse>?,
    @SerialName("next_page")
    val nextPage: String?
)

@Serializable
data class ArticleResponse(
    @SerialName("id")
    val id: Int?,
    @SerialName("post_type")
    val postType: String?,
    @SerialName("title")
    val title: String?,
    @SerialName("slug")
    val slug: String?,
    @SerialName("comments_count")
    val commentsCount: Int?,
    @SerialName("liked_count")
    val likedCount: Int?,
    @SerialName("body_letters_count")
    val bodyLettersCount: Int?,
    @SerialName("article_type")
    val articleType: String?,
    @SerialName("emoji")
    val emoji: String?,
    @SerialName("is_suspending_private")
    val isSuspendingPrivate: Boolean?,
    @SerialName("published_at")
    val publishedAt: String?,
    @SerialName("body_updated_at")
    val bodyUpdatedAt: String?,
    @SerialName("source_repo_updated_at")
    val sourceRepoUpdatedAt: String?,
    @SerialName("pinned")
    val pinned: Boolean?,
    @SerialName("path")
    val path: String?,
    @SerialName("user")
    val user: UserResponse?,
    @SerialName("publication")
    val publication: PublicationResponse?
)

@Serializable
data class UserResponse(
    @SerialName("id")
    val id: Int?,
    @SerialName("username")
    val username: String?,
    @SerialName("name")
    val name: String?,
    @SerialName("avatar_small_url")
    val avatarSmallUrl: String?
)

@Serializable
data class PublicationResponse(
    @SerialName("id")
    val id: Int?,
    @SerialName("name")
    val name: String?,
    @SerialName("display_name")
    val displayName: String?,
    @SerialName("avatar_small_url")
    val avatarSmallUrl: String?,
    @SerialName("avatar_url")
    val avatarUrl: String?,
    @SerialName("pro")
    val pro: Boolean?,
    @SerialName("avatar_registered")
    val avatarRegistered: Boolean?
)
