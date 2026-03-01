package com.cypherchat.core.common

/**
 * Typed result wrapper that prevents leaking exceptions across module boundaries.
 * Errors are explicit domain types rather than thrown exceptions.
 */
sealed class SecureResult<out T> {

    data class Success<T>(val value: T) : SecureResult<T>()

    data class Failure(val error: CypherError) : SecureResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.value

    fun errorOrNull(): CypherError? = (this as? Failure)?.error

    fun <R> map(transform: (T) -> R): SecureResult<R> = when (this) {
        is Success  -> Success(transform(value))
        is Failure  -> this
    }

    suspend fun <R> mapSuspend(transform: suspend (T) -> R): SecureResult<R> = when (this) {
        is Success  -> Success(transform(value))
        is Failure  -> this
    }

    fun onSuccess(block: (T) -> Unit): SecureResult<T> {
        if (this is Success) block(value)
        return this
    }

    fun onFailure(block: (CypherError) -> Unit): SecureResult<T> {
        if (this is Failure) block(error)
        return this
    }
}

sealed class CypherError {
    data class CryptoError(val message: String, val cause: Throwable? = null) : CypherError()
    data class DatabaseError(val message: String, val cause: Throwable? = null) : CypherError()
    data class NetworkError(val message: String, val cause: Throwable? = null) : CypherError()
    data class AuthError(val message: String) : CypherError()
    data class Unknown(val message: String, val cause: Throwable? = null) : CypherError()
}

/** Wrap a suspending block, catching all exceptions as [CypherError.Unknown]. */
suspend fun <T> runSecure(block: suspend () -> T): SecureResult<T> = try {
    SecureResult.Success(block())
} catch (e: Exception) {
    SecureResult.Failure(CypherError.Unknown(e.message ?: "Unknown error", e))
}
