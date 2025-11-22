package io.github.kei_1111.newsflow.library.core.logger

import platform.Foundation.NSLog

actual object Logger {
    actual fun v(tag: String, message: String) {
        NSLog("VERBOSE [$tag] $message")
    }

    actual fun d(tag: String, message: String) {
        NSLog("DEBUG [$tag] $message")
    }

    actual fun i(tag: String, message: String) {
        NSLog("INFO [$tag] $message")
    }

    actual fun w(tag: String, message: String) {
        NSLog("WARN [$tag] $message")
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            NSLog("ERROR [$tag] $message: ${throwable.message}\n${throwable.stackTraceToString()}")
        } else {
            NSLog("ERROR [$tag] $message")
        }
    }
}
