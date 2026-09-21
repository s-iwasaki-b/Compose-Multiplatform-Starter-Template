package org.starter.project.data.zenn.datasource.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticlesResponse(
    @SerialName("articles")
    val articles: List<ArticleResponse>? = null,
    @SerialName("next_page")
    val nextPage: String? = null
)

@Serializable
data class ArticleResponse(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("post_type")
    val postType: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("slug")
    val slug: String? = null,
    @SerialName("comments_count")
    val commentsCount: Int? = null,
    @SerialName("liked_count")
    val likedCount: Int? = null,
    @SerialName("body_letters_count")
    val bodyLettersCount: Int? = null,
    @SerialName("article_type")
    val articleType: String? = null,
    @SerialName("emoji")
    val emoji: String? = null,
    @SerialName("is_suspending_private")
    val isSuspendingPrivate: Boolean? = null,
    @SerialName("published_at")
    val publishedAt: String? = null,
    @SerialName("body_updated_at")
    val bodyUpdatedAt: String? = null,
    @SerialName("source_repo_updated_at")
    val sourceRepoUpdatedAt: String? = null,
    @SerialName("pinned")
    val pinned: Boolean? = null,
    @SerialName("path")
    val path: String? = null,
    @SerialName("user")
    val user: ArticleUserResponse? = null,
    @SerialName("publication")
    val publication: ArticlePublicationResponse? = null
)

@Serializable
data class ArticleUserResponse(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("username")
    val username: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("avatar_small_url")
    val avatarSmallUrl: String? = null
)

@Serializable
data class ArticlePublicationResponse(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("avatar_small_url")
    val avatarSmallUrl: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("pro")
    val pro: Boolean? = null,
    @SerialName("avatar_registered")
    val avatarRegistered: Boolean? = null
)
