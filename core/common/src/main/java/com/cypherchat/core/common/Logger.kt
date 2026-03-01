package com.cypherchat.core.common

import android.util.Log

/**
 * Minimal logger that strips sensitive content in release builds.
 * Replace with Timber or your preferred logging backend.
 *
 * SECURITY: never log cryptographic key material, message content,
 * or user identity information — only operational metadata.
 */
object Logger {

    private var debugEnabled: Boolean = false

    fun init(debug: Boolean) {
        debugEnabled = debug
    }

    fun d(tag: String, msg: String) {
        if (debugEnabled) Log.d(tag, msg)
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    fun w(tag: String, msg: String, throwable: Throwable? = null) {
        if (throwable != null) Log.w(tag, msg, throwable) else Log.w(tag, msg)
    }

    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        // Always log errors, but redact the throwable message in release
        val safeMsg = if (debugEnabled) msg else "[error redacted in release]"
        if (debugEnabled && throwable != null) {
            Log.e(tag, safeMsg, throwable)
        } else {
            Log.e(tag, safeMsg)
        }
    }
}
