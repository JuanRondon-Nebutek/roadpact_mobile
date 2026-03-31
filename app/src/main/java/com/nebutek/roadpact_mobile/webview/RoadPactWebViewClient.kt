package com.nebutek.roadpact_mobile.webview

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.nebutek.roadpact_mobile.logging.LogTags
import com.nebutek.roadpact_mobile.logging.RoadPactLogger
import com.nebutek.roadpact_mobile.logging.redactId

class RoadPactWebViewClient(
    private val logger: RoadPactLogger,
    private val onPageStarted: () -> Unit,
    private val onPageFinished: () -> Unit,
    private val onReceivedError: (description: String, errorCode: Int?) -> Unit,
    private val onSurveyComplete: (SurveyCompleteSignal) -> Unit,
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        logger.d(LogTags.WEBVIEW, "onPageStarted url=${url?.take(80)}")
        onPageStarted()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        logger.d(LogTags.WEBVIEW, "onPageFinished url=${url?.take(80)}")
        onPageFinished()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame != true) return
        val desc = error?.description?.toString() ?: "Unknown web error"
        val code = error?.errorCode
        logger.e(LogTags.WEBVIEW, "onReceivedError mainFrame code=$code desc=$desc")
        onReceivedError(desc, code)
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return handleUrl(url?.let { Uri.parse(it) })
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val uri = request?.url ?: return false
        return handleUrl(uri)
    }

    private fun handleUrl(uri: Uri?): Boolean {
        if (uri == null) return false
        val scheme = uri.scheme ?: return false
        if (!SCHEME_ROADPACT.equals(scheme, ignoreCase = true)) return false

        val signal = SurveyCompleteNavigation.parse(uri)
        if (signal != null) {
            logger.i(
                LogTags.WEBVIEW,
                "Survey complete signal status=${signal.status} session=${logger.redactId(signal.sessionId)}",
            )
            onSurveyComplete(signal)
            return true
        }

        val host = uri.host.orEmpty()
        val hasQuery = !uri.query.isNullOrEmpty()
        logger.w(
            LogTags.WEBVIEW,
            "roadpact distinto de survey-complete: host=${host.take(24)}… hasQuery=$hasQuery — URL consumida (evita relanzar actividad)",
        )
        return true
    }

    companion object {
        private const val SCHEME_ROADPACT = "roadpact"
    }
}
