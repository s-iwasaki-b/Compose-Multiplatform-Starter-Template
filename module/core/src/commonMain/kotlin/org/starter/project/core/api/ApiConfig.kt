package org.starter.project.core.api

import io.ktor.http.HttpMessageBuilder

internal object ApiConfig {
    const val API_BASE_URL = "https://zenn.dev/"

    inline fun HttpMessageBuilder.authHeader() {
        // TODO: add auth header
    }

    inline fun HttpMessageBuilder.commonHeader() {
        // TODO: add common header
    }
}
