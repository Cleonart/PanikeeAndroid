package com.example.panikee.data.api

import com.example.panikee.data.vo.EmergencyFacility
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("facility/emergency")
    fun getEmergencyFacility(): Call<ArrayList<EmergencyFacility>>
}