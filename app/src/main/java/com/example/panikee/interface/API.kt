package com.example.panikee.`interface`

import com.example.panikee.model.EmergencyFacility
import retrofit2.http.GET
import retrofit2.Call

interface API {
    @GET("facility/emergency")
    fun getEmergencyFacility() : Call<ArrayList<EmergencyFacility>>
}