package org.starter.project.ui.route

import kotlinx.serialization.Serializable

sealed class AppRoute {
    @Serializable
    data class Home(val keyword: String? = null) : AppRoute() {
        @Serializable
        data class NavArgs(val keyword: String?)
        val navArgs: NavArgs
            get() = NavArgs(keyword)
    }

    @Serializable
    data class User(val username: String) : AppRoute() {
        @Serializable
        data class NavArgs(val username: String)
        val navArgs: NavArgs
            get() = NavArgs(username)
    }
}

interface AppRouter {
    fun navigate(route: AppRoute)
    fun popBackStack()
}
