package com.nebutek.roadpact_mobile.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

private const val ROADPACT_INITIAL_URL =
    "https://app.roadpact.com/login"

@Composable
fun RoadPactWebView(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                loadUrl(ROADPACT_INITIAL_URL)
            }
        },
        modifier = modifier,
        onRelease = { webView -> webView.destroy() }
    )
}
