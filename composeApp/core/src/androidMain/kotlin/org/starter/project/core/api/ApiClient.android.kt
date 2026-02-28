package org.starter.project.core.api

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.android.Android

internal actual val engine: HttpClientEngineFactory<HttpClientEngineConfig> = Android
