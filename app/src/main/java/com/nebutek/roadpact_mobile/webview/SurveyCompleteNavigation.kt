package com.nebutek.roadpact_mobile.webview

import android.net.Uri

data class SurveyCompleteSignal(
    val tenantId: String?,
    val surveyId: String?,
    val driverId: String?,
    val sessionId: String?,
    val status: String,
    val reason: String?,
)

object SurveyCompleteNavigation {
    private const val SCHEME = "roadpact"
    private const val HOST = "survey-complete"

    fun parse(uri: Uri): SurveyCompleteSignal? {
        if (!SCHEME.equals(uri.scheme, ignoreCase = true)) return null
        if (!HOST.equals(uri.host, ignoreCase = true)) return null
        return SurveyCompleteSignal(
            tenantId = uri.getQueryParameter("tenant_id"),
            surveyId = uri.getQueryParameter("survey_id"),
            driverId = uri.getQueryParameter("driver_id"),
            sessionId = uri.getQueryParameter("session_id"),
            status = uri.getQueryParameter("status").orEmpty(),
            reason = uri.getQueryParameter("reason"),
        )
    }
}
