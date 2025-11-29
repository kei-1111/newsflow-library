package io.github.kei_1111.newsflow.library.core.logger

actual object Logger {
    actual fun v(tag: String, message: String) {
        println("V/$tag: $message")
    }

    actual fun d(tag: String, message: String) {
        println("D/$tag: $message")
    }

    actual fun i(tag: String, message: String) {
        println("I/$tag: $message")
    }

    actual fun w(tag: String, message: String) {
        println("W/$tag: $message")
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            println("E/$tag: $message")
            throwable.printStackTrace()
        } else {
            println("E/$tag: $message")
        }
    }
}
