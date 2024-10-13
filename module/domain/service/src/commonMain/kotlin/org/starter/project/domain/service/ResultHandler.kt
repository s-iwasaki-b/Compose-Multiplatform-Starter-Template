package org.starter.project.domain.service

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ResultHandler(
    val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend inline fun <Success> async(
        dispatcher: CoroutineDispatcher = this.dispatcher,
        crossinline block: suspend () -> Success
    ): Result<Success> {
        return withContext(dispatcher) {
            try {
                val value = block()
                Result.success(value)
            } catch (e: Throwable) {
                // TODO: report error to your analytics
                Napier.d { e.message.orEmpty() }
                Result.failure(e)
            }
        }
    }

    suspend inline fun asyncUnit(
        dispatcher: CoroutineDispatcher = this.dispatcher,
        crossinline block: suspend () -> Unit
    ): Result<Unit> = async(dispatcher) { block() }

    inline fun <Success> immediate(
        crossinline block: () -> Success
    ): Result<Success> {
        return try {
            val value = block()
            Result.success(value)
        } catch (e: Throwable) {
            // TODO: report error to your analytics
            Napier.d { e.message.orEmpty() }
            Result.failure(e)
        }
    }

    inline fun immediateUnit(
        crossinline block: () -> Unit
    ): Result<Unit> = immediate { block() }
}
