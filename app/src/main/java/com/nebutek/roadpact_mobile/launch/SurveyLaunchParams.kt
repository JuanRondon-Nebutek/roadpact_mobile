package com.nebutek.roadpact_mobile.launch

data class SurveyLaunchParams(
    val tenantId: String,
    val surveyId: String,
    val driverId: String,
    val assetId: String,
    val jobId: String,
    val latitude: Double?,
    val longitude: Double?,
    val callbackRequested: Boolean,
) {
    init {
        require(tenantId.isNotBlank()) { "tenant_id is required" }
        require(surveyId.isNotBlank()) { "survey_id is required" }
        require(driverId.isNotBlank()) { "driver_id is required" }
        require(assetId.isNotBlank()) { "asset_id is required" }
        require(jobId.isNotBlank()) { "job_id is required" }
    }

    companion object {
        fun fromDefaults(): SurveyLaunchParams = SurveyLaunchDefaults.defaultParams()
    }
}
