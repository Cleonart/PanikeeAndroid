package com.example.panikee.adapters

import com.example.panikee.`interface`.API
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitAdapter {
    private val BASE_URL = "http://192.168.1.8:5000/"
    val instance : API by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(API::class.java)
    }
}