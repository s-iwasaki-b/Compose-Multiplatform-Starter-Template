package org.starter.project.ui.shared.handler

object IgnoreExceptionHandler {
    operator fun invoke(): (Throwable) -> Unit = { _ ->
        // no-op
    }
}
