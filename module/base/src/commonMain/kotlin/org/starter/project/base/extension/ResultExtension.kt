package org.starter.project.base.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

inline fun <Success> Result<Success>.handle(
    handler: (Throwable) -> Unit,
    shouldCancelScope: CoroutineScope? = null
): Success? {
    return this.getOrElse {
        handler(it)
        shouldCancelScope?.cancel(it.message.orEmpty())
        null
    }
}
