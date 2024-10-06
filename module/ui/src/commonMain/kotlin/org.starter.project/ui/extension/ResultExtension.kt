package org.starter.project.ui.extension

inline fun <Success> Result<Success>.handle(handler: (Throwable) -> Unit): Success? {
    return this.getOrElse {
        handler(it)
        null
    }
}
