package com.nebutek.roadpact_mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.nebutek.roadpact_mobile.deeplink.DeepLinkParseResult
import com.nebutek.roadpact_mobile.deeplink.RoadPactDeepLinkParser
import com.nebutek.roadpact_mobile.launch.RoadPactUrlBuilder
import com.nebutek.roadpact_mobile.launch.SurveyLaunchParams
import com.nebutek.roadpact_mobile.logging.AndroidRoadPactLogger
import com.nebutek.roadpact_mobile.logging.LogTags
import com.nebutek.roadpact_mobile.logging.redactId
import com.nebutek.roadpact_mobile.ui.RoadPactSurveyScreen
import com.nebutek.roadpact_mobile.ui.theme.Roadpact_mobileTheme
import com.nebutek.roadpact_mobile.webview.SurveyCompleteSignal

class MainActivity : ComponentActivity() {

    private val logger = AndroidRoadPactLogger()
    private val loadUrlState = mutableStateOf("")
    private val surveyCompletionErrorMessage = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.d(
            LogTags.LAUNCH,
            "MainActivity onCreate — if the app closes on its own, check Logcat for RoadPact:WebView (Survey complete / roadpact) or AndroidRuntime (crash)",
        )
        loadUrlState.value = resolveAndBuildUrl(intent)
        enableEdgeToEdge()
        setContent {
            val loadUrl by loadUrlState
            val surveyError by surveyCompletionErrorMessage
            val roadPactLogger = remember { logger }
            val onSurveyComplete = remember {
                { signal: SurveyCompleteSignal ->
                    handleSurveyComplete(signal)
                }
            }
            Roadpact_mobileTheme {
                RoadPactSurveyScreen(
                    loadUrl = loadUrl,
                    logger = roadPactLogger,
                    onSurveyComplete = onSurveyComplete,
                    onCloseClick = { finish() },
                    surveyCompletionError = surveyError,
                    onSurveyCompletionErrorConsumed = { surveyCompletionErrorMessage.value = null },
                    onFinishAfterSurveyError = { finish() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        loadUrlState.value = resolveAndBuildUrl(intent)
    }

    private fun resolveAndBuildUrl(intent: Intent): String {
        val params = when {
            Intent.ACTION_VIEW == intent.action && intent.data != null -> {
                when (val parsed = RoadPactDeepLinkParser.parse(intent)) {
                    is DeepLinkParseResult.Success -> parsed.params
                    is DeepLinkParseResult.Failure -> {
                        logger.w(LogTags.LAUNCH, "Invalid deep link: ${parsed.reason}; using defaults")
                        SurveyLaunchParams.fromDefaults()
                    }
                }
            }
            else -> SurveyLaunchParams.fromDefaults()
        }
        val url = RoadPactUrlBuilder.build(params)
        logger.i(
            LogTags.LAUNCH,
            "Loading survey tenant=${logger.redactId(params.tenantId)} survey=${logger.redactId(params.surveyId)}",
        )
        logger.d(LogTags.LAUNCH, "URL (no secrets): ${url.take(160)}")
        return url
    }

    private fun handleSurveyComplete(signal: SurveyCompleteSignal) {
        val status = signal.status.trim().lowercase()
        when {
            status == "success" -> {
                logger.i(LogTags.LAUNCH, "Survey completed (status=success)")
                finish()
            }
            status == "error" -> {
                val message = buildString {
                    append("The survey ended with an error")
                    if (!signal.reason.isNullOrBlank()) append(": ${signal.reason}")
                }
                logger.w(LogTags.LAUNCH, "Survey error reason=${signal.reason}")
                surveyCompletionErrorMessage.value = message
            }
            status.isEmpty() -> {
                logger.w(
                    LogTags.LAUNCH,
                    "roadpact://survey-complete callback without status parameter; activity not closed (check web integration)",
                )
            }
            else -> {
                logger.w(
                    LogTags.LAUNCH,
                    "survey-complete callback with unknown status: $status; activity not closed",
                )
            }
        }
    }
}
