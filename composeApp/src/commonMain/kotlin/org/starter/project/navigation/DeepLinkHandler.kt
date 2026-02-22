package org.starter.project.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Bridge for passing deep link URIs from iOS (SwiftUI) to Compose Navigation.
 *
 * On Android, deep links are handled automatically via Intent -> NavController.
 * On iOS, there is no such mechanism, so this object acts as a bridge:
 * 1. SwiftUI's onOpenURL calls [onDeepLinkReceived]
 * 2. Compose observes [pendingDeepLink] and forwards to AppRouter
 */
object DeepLinkHandler {
    private val _pendingDeepLink = MutableStateFlow<String?>(null)
    val pendingDeepLink: StateFlow<String?> = _pendingDeepLink.asStateFlow()

    fun onDeepLinkReceived(uri: String) {
        _pendingDeepLink.value = uri
    }

    fun consumeDeepLink() {
        _pendingDeepLink.value = null
    }
}
