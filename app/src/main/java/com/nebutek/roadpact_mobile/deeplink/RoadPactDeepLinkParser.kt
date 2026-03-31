package com.nebutek.roadpact_mobile.deeplink

import android.content.Intent
import android.net.Uri
import com.nebutek.roadpact_mobile.launch.SurveyLaunchParams

sealed class DeepLinkParseResult {
    data class Success(val params: SurveyLaunchParams) : DeepLinkParseResult()
    data class Failure(val reason: String) : DeepLinkParseResult()
}

object RoadPactDeepLinkParser {
    private const val SCHEME = "roadpact"

    fun parse(intent: Intent): DeepLinkParseResult {
        val uri = intent.data ?: return DeepLinkParseResult.Failure("No intent data URI")
        return parseUri(uri)
    }

    fun parseUri(uri: Uri): DeepLinkParseResult {
        if (!SCHEME.equals(uri.scheme, ignoreCase = true)) {
            return DeepLinkParseResult.Failure("Unsupported scheme: ${uri.scheme}")
        }

        val host = uri.host
        if (host.isNullOrBlank()) {
            return DeepLinkParseResult.Failure("Missing host (tenant_id)")
        }

        if (host.equals("survey-complete", ignoreCase = true)) {
            return DeepLinkParseResult.Failure("survey-complete is not a launch deep link")
        }

        val segments = uri.pathSegments.filter { it.isNotBlank() }
        val surveyId = segments.firstOrNull()
            ?: return DeepLinkParseResult.Failure("Missing survey_id path segment")

        val tenantId = host

        val driverId = uri.getQueryParameter("driver_id")
            ?: return DeepLinkParseResult.Failure("Missing driver_id")
        val assetId = uri.getQueryParameter("asset_id")
            ?: return DeepLinkParseResult.Failure("Missing asset_id")
        val jobId = uri.getQueryParameter("job_id")
            ?: return DeepLinkParseResult.Failure("Missing job_id")

        val lat = uri.getQueryParameter("lat")?.toDoubleOrNull()
        val lon = uri.getQueryParameter("lon")?.toDoubleOrNull()

        return try {
            val params = SurveyLaunchParams(
                tenantId = tenantId,
                surveyId = surveyId,
                driverId = driverId,
                assetId = assetId,
                jobId = jobId,
                latitude = lat,
                longitude = lon,
                callbackRequested = true,
            )
            DeepLinkParseResult.Success(params)
        } catch (e: IllegalArgumentException) {
            DeepLinkParseResult.Failure(e.message ?: "Invalid parameters")
        }
    }
}
