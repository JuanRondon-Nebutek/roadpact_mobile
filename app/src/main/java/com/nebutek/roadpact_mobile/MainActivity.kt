package com.nebutek.roadpact_mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
            "MainActivity onCreate — si la app cierra sola, buscar en Logcat líneas RoadPact:WebView (Survey complete / roadpact) o AndroidRuntime (crash)",
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RoadPactSurveyScreen(
                        loadUrl = loadUrl,
                        logger = roadPactLogger,
                        onSurveyComplete = onSurveyComplete,
                        onCloseClick = { finish() },
                        surveyCompletionError = surveyError,
                        onSurveyCompletionErrorConsumed = { surveyCompletionErrorMessage.value = null },
                        onFinishAfterSurveyError = { finish() },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
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
                        logger.w(LogTags.LAUNCH, "Deep link inválido: ${parsed.reason}; usando valores por defecto")
                        SurveyLaunchParams.fromDefaults()
                    }
                }
            }
            else -> SurveyLaunchParams.fromDefaults()
        }
        val url = RoadPactUrlBuilder.build(params)
        logger.i(
            LogTags.LAUNCH,
            "Carga encuesta tenant=${logger.redactId(params.tenantId)} survey=${logger.redactId(params.surveyId)}",
        )
        logger.d(LogTags.LAUNCH, "URL (sin secretos): ${url.take(160)}")
        return url
    }

    private fun handleSurveyComplete(signal: SurveyCompleteSignal) {
        val status = signal.status.trim().lowercase()
        when {
            status == "success" -> {
                logger.i(LogTags.LAUNCH, "Encuesta completada (status=success)")
                finish()
            }
            status == "error" -> {
                val message = buildString {
                    append("La encuesta terminó con error")
                    if (!signal.reason.isNullOrBlank()) append(": ${signal.reason}")
                }
                logger.w(LogTags.LAUNCH, "Encuesta error reason=${signal.reason}")
                surveyCompletionErrorMessage.value = message
            }
            status.isEmpty() -> {
                logger.w(
                    LogTags.LAUNCH,
                    "Callback roadpact://survey-complete sin parámetro status; no se cierra la actividad (revisar integración web)",
                )
            }
            else -> {
                logger.w(
                    LogTags.LAUNCH,
                    "Callback survey-complete con status desconocido: $status; no se cierra la actividad",
                )
            }
        }
    }
}
