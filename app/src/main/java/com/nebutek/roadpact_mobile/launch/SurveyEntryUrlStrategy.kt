package com.nebutek.roadpact_mobile.launch

import android.net.Uri

interface SurveyEntryUrlStrategy {
    fun buildUrl(params: SurveyLaunchParams): String
}

private const val HTTPS = "https"
private const val ROADPACT_HOST = "app.roadpact.com"

/**
 * Current demo: query params on /burst-demo-roadpact/
 */
class BurstDemoRoadPactUrlStrategy : SurveyEntryUrlStrategy {
    override fun buildUrl(params: SurveyLaunchParams): String {
        val builder = Uri.Builder()
            .scheme(HTTPS)
            .authority(ROADPACT_HOST)
            .appendPath("burst-demo-roadpact")
            .appendPath("")
        builder.appendQueryParameter("driver_id", params.driverId)
        builder.appendQueryParameter("tenant_id", params.tenantId)
        builder.appendQueryParameter("survey_id", params.surveyId)
        params.latitude?.let { builder.appendQueryParameter("lat", it.toString()) }
        params.longitude?.let { builder.appendQueryParameter("lon", it.toString()) }
        builder.appendQueryParameter("asset_id", params.assetId)
        builder.appendQueryParameter("job_id", params.jobId)
        if (params.callbackRequested) {
            builder.appendQueryParameter("callback", "yes")
        }
        return builder.build().toString()
    }
}

/**
 * Production handoff: /ps-survey/{tenant_id}/{survey_id}?...
 */
class PsSurveyRoadPactUrlStrategy : SurveyEntryUrlStrategy {
    override fun buildUrl(params: SurveyLaunchParams): String {
        val builder = Uri.Builder()
            .scheme(HTTPS)
            .authority(ROADPACT_HOST)
            .appendPath("ps-survey")
            .appendPath(params.tenantId)
            .appendPath(params.surveyId)
        builder.appendQueryParameter("driver_id", params.driverId)
        params.latitude?.let { builder.appendQueryParameter("lat", it.toString()) }
        params.longitude?.let { builder.appendQueryParameter("lon", it.toString()) }
        builder.appendQueryParameter("asset_id", params.assetId)
        builder.appendQueryParameter("job_id", params.jobId)
        if (params.callbackRequested) {
            builder.appendQueryParameter("callback", "yes")
        }
        return builder.build().toString()
    }
}

object RoadPactUrlBuilder {
    private val defaultStrategy: SurveyEntryUrlStrategy = BurstDemoRoadPactUrlStrategy()

    fun build(
        params: SurveyLaunchParams,
        strategy: SurveyEntryUrlStrategy = defaultStrategy,
    ): String = strategy.buildUrl(params)
}
