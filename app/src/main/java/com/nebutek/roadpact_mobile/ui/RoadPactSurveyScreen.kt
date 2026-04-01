package com.nebutek.roadpact_mobile.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nebutek.roadpact_mobile.logging.LogTags
import com.nebutek.roadpact_mobile.logging.RoadPactLogger
import com.nebutek.roadpact_mobile.webview.RoadPactWebChromeClient
import com.nebutek.roadpact_mobile.webview.RoadPactWebViewClient
import com.nebutek.roadpact_mobile.webview.SurveyCompleteSignal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadPactSurveyScreen(
    loadUrl: String,
    logger: RoadPactLogger,
    onSurveyComplete: (SurveyCompleteSignal) -> Unit,
    onCloseClick: () -> Unit,
    surveyCompletionError: String?,
    onSurveyCompletionErrorConsumed: () -> Unit,
    onFinishAfterSurveyError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isLoading by remember(loadUrl) { mutableStateOf(true) }
    var pageError by remember(loadUrl) { mutableStateOf<String?>(null) }

    val latestOnSurveyComplete by rememberUpdatedState(onSurveyComplete)
    val webChromeClient = remember(logger) { RoadPactWebChromeClient(logger) }
    val webViewClient = remember(logger, loadUrl) {
        RoadPactWebViewClient(
            logger = logger,
            onPageStarted = {
                isLoading = true
                pageError = null
            },
            onPageFinished = { isLoading = false },
            onReceivedError = { description, _ ->
                pageError = description
                isLoading = false
                logger.w(LogTags.WEBVIEW, "Page error: $description")
            },
            onSurveyComplete = { signal ->
                latestOnSurveyComplete(signal)
            },
        )
    }

    LaunchedEffect(pageError) {
        val err = pageError ?: return@LaunchedEffect
        scope.launch {
            snackbarHostState.showSnackbar(
                message = "Failed to load: $err",
                withDismissAction = true,
            )
        }
    }

    LaunchedEffect(surveyCompletionError) {
        val msg = surveyCompletionError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = msg,
            withDismissAction = true,
        )
        onSurveyCompletionErrorConsumed()
        onFinishAfterSurveyError()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("RoadPact") },
                navigationIcon = {
                    IconButton(onClick = onCloseClick) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            key(loadUrl) {
                RoadPactWebView(
                    loadUrl = loadUrl,
                    webViewClient = webViewClient,
                    webChromeClient = webChromeClient,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
