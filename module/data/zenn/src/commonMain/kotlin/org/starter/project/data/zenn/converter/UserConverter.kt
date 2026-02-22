package org.starter.project.data.zenn.converter

import androidx.annotation.VisibleForTesting
import org.starter.project.base.data.model.zenn.User
import org.starter.project.base.extension.validateNotNull
import org.starter.project.data.zenn.datasource.api.response.UserInnerResponse
import org.starter.project.data.zenn.datasource.api.response.UserResponse

object UserConverter {
    operator fun invoke(response: UserResponse): User {
        val user = response.user.validateNotNull("user")
        return createUser(user)
    }

    @VisibleForTesting
    internal fun createUser(response: UserInnerResponse): User {
        return User(
            id = response.id.validateNotNull("id"),
            username = response.username.validateNotNull("username"),
            name = response.name.validateNotNull("name"),
            avatarSmallUrl = response.avatarSmallUrl.validateNotNull("avatar_small_url"),
            avatarUrl = response.avatarUrl.validateNotNull("avatar_url"),
            bio = response.bio.orEmpty(),
            followerCount = response.followerCount.validateNotNull("follower_count"),
            followingCount = response.followingCount.validateNotNull("following_count"),
            articlesCount = response.articlesCount.validateNotNull("articles_count"),
        )
    }
}
