package org.starter.project.base.error

import kotlinx.serialization.Serializable

sealed class ApiError(override val message: String) : Throwable() {
    class Unauthorized(message: String) : ApiError(message)
    class Unknown(message: String) : ApiError(message)
}

@Serializable
data class ApiErrorResponse(
    val title: String,
    val message: String
)
