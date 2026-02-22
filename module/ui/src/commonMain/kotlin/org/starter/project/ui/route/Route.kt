package org.starter.project.ui.route

import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable
    data class Home(val keyword: String? = null) : Route()

    @Serializable
    data class User(val username: String) : Route()
}

interface Router {
    fun navigate(route: Route)
    fun popBackStack()
}
