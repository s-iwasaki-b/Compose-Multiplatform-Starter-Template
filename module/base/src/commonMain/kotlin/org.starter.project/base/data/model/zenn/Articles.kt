package org.starter.project.base.data.model.zenn

import androidx.compose.runtime.Immutable

@Immutable
data class Articles(
    val articles: List<Article>,
    val nextPage: String?
)

@Immutable
data class Article(
    val id: Int,
    val emoji: String,
    val title: String,
    val commentsCount: Int,
    val likedCount: Int,
    val publishedAt: String,
    val user: User,
)

@Immutable
data class User(
    val id: Int,
    val username: String,
    val name: String,
    val avatarSmallUrl: String
)
