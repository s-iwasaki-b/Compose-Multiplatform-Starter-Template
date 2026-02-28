package org.starter.project.ui.shared.handler

object IgnoreThrowableHandler {
    operator fun invoke(): (Throwable) -> Unit = { _ ->
        // no-op
    }
}
