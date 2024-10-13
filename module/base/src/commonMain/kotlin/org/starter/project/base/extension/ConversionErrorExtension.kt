package org.starter.project.base.extension

import org.starter.project.base.error.ConversionError

fun <T> T?.validateNotNull(name: String): T {
    if (this == null) {
        throw ConversionError.ResponseNotNullValidation(name)
    } else {
        return this
    }
}
