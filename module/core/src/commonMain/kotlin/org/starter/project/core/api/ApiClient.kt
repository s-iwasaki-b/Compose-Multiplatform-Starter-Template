package org.starter.project.core.api

import de.jensklingenberg.ktorfit.Ktorfit
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.starter.project.base.error.ApiError
import org.starter.project.base.error.ApiErrorResponse

object ApiClient {
    private val client = HttpClient {
        defaultRequest {
            url(ApiConfig.API_BASE_URL)
        }
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        expectSuccess = true
        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, _ ->
                val clientException = exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
                val exceptionResponse = clientException.response
                val errorResponse = exceptionResponse.body<ApiErrorResponse>()
                throw when (exceptionResponse.status) {
                    // This is an example of converting HTTP status codes into a custom error class.
                    HttpStatusCode.Unauthorized -> ApiError.Unauthorized(errorResponse.message)
                    else -> ApiError.Unknown(errorResponse.message)
                }
            }
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Napier.v(message, null, "ApiClient")
                }
            }
            level = LogLevel.ALL
        }
    }

    val ktorfit: Ktorfit = Ktorfit.Builder().httpClient(client).build()
}
