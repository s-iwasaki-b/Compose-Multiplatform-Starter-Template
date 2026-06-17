package org.starter.project.domain.ai.action

sealed interface AppAction {
    data class SearchArticles(val keyword: String) : AppAction
    data class NavigateToUser(val username: String) : AppAction
}
