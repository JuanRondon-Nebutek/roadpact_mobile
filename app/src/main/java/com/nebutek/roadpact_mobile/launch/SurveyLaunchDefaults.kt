package com.nebutek.roadpact_mobile.launch

/**
 * Valores de prueba cuando no hay deep link de Platform Science.
 * IDs de ejemplo tomados de la URL demo; campos no presentes usan "0000".
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
