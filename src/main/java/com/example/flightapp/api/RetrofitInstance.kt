package com.example.flightapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: SkyscannerApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://partners.api.skyscanner.net/apiservices/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SkyscannerApi::class.java)
    }
}
