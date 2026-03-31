package com.nebutek.roadpact_mobile.logging

interface RoadPactLogger {
    fun d(tag: String, message: String)

    fun i(tag: String, message: String)

    fun w(tag: String, message: String, throwable: Throwable? = null)

    fun e(tag: String, message: String, throwable: Throwable? = null)
}

class AndroidRoadPactLogger : RoadPactLogger {
    override fun d(tag: String, message: String) {
        android.util.Log.d(tag, message)
    }

    override fun i(tag: String, message: String) {
        android.util.Log.i(tag, message)
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            android.util.Log.w(tag, message, throwable)
        } else {
            android.util.Log.w(tag, message)
        }
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            android.util.Log.e(tag, message, throwable)
        } else {
            android.util.Log.e(tag, message)
        }
    }
}

object LogTags {
    const val LAUNCH = "RoadPact:Launch"
    const val WEBVIEW = "RoadPact:WebView"
}

fun RoadPactLogger.redactId(value: String?, maxPrefix: Int = 6): String {
    if (value.isNullOrEmpty()) return "(empty)"
    return if (value.length <= maxPrefix) {
        "***"
    } else {
        "${value.take(maxPrefix)}…(len=${value.length})"
    }
}
