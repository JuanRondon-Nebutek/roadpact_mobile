package com.nebutek.roadpact_mobile.launch

/**
 * Test values when there is no Platform Science deep link.
 * Example IDs taken from the demo URL; missing fields use "0000".
 */
object SurveyLaunchDefaults {
    const val TENANT_ID: String = "69b16c7d92ba492eac0f3799"
    const val SURVEY_ID: String = "698d194cb115e3ca759cde05"
    const val DRIVER_ID: String = "30"
    const val PLACEHOLDER_ID: String = "0000"

    fun defaultParams(): SurveyLaunchParams = SurveyLaunchParams(
        tenantId = TENANT_ID,
        surveyId = SURVEY_ID,
        driverId = DRIVER_ID,
        assetId = PLACEHOLDER_ID,
        jobId = PLACEHOLDER_ID,
        latitude = null,
        longitude = null,
        callbackRequested = true,
    )
}
