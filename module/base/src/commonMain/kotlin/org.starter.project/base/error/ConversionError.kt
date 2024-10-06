package org.starter.project.base.error

sealed class ConversionError(override val message: String) : Throwable() {
    class ResponseNotNullValidation(message: String) : ConversionError(message)
}
