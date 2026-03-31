package com.nebutek.roadpact_mobile.webview

import android.webkit.ConsoleMessage
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.nebutek.roadpact_mobile.logging.LogTags
import com.nebutek.roadpact_mobile.logging.RoadPactLogger

class RoadPactWebChromeClient(
    private val logger: RoadPactLogger,
) : WebChromeClient() {

    override fun onConsoleMessage(message: ConsoleMessage): Boolean {
        val level = message.messageLevel()
        val text = "JS [${message.sourceId()?.substringAfterLast('/')}:${message.lineNumber()}] ${message.message()}"
        when (level) {
            ConsoleMessage.MessageLevel.ERROR -> logger.e(LogTags.WEBVIEW, text)
            ConsoleMessage.MessageLevel.WARNING -> logger.w(LogTags.WEBVIEW, text)
            ConsoleMessage.MessageLevel.DEBUG -> logger.d(LogTags.WEBVIEW, text)
            else -> logger.i(LogTags.WEBVIEW, text)
        }
        return true
    }

    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        logger.w(LogTags.WEBVIEW, "JS alert: $message")
        result?.confirm()
        return true
    }

    override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        logger.w(LogTags.WEBVIEW, "JS confirm: $message — auto-confirming")
        result?.confirm()
        return true
    }
}
