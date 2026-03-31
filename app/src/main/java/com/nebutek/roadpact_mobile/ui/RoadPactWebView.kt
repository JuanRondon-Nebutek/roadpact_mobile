package com.nebutek.roadpact_mobile.ui

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun RoadPactWebView(
    loadUrl: String,
    webViewClient: WebViewClient,
    webChromeClient: WebChromeClient,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                this.webViewClient = webViewClient
                this.webChromeClient = webChromeClient
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                loadUrl(loadUrl)
            }
        },
        modifier = modifier,
        onRelease = { webView -> webView.destroy() },
    )
}
