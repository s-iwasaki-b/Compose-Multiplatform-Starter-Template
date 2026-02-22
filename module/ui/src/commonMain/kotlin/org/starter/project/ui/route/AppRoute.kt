package org.starter.project.ui.route

import kotlinx.serialization.Serializable

sealed class AppRoute {
    @Serializable
    data object Home : AppRoute()

    @Serializable
    data class User(val username: String) : AppRoute()
}

interface AppRouter {
    fun navigate(route: AppRoute)
    fun popBackStack()
}
