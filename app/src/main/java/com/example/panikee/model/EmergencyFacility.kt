package com.example.panikee.model

import com.google.gson.annotations.SerializedName

data class EmergencyFacility(
    val EmergencyFacilityName : String?,
    val EmergencyFacilityNumber : String?,
    val EmergencyFacilityLat : Double?,
    val EmergencyFacilityLong : Double?,
    val EmergencyFacilityCategory: String?,
)