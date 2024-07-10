package com.example.flightapp.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

interface SkyscannerApi {
    @GET("flights/browse")
    fun searchFlights(
        @Query("apiKey") apiKey: String,
        @Query("country") country: String,
        @Query("currency") currency: String,
        @Query("locale") locale: String,
        @Query("originPlace") originPlace: String,
        @Query("destinationPlace") destinationPlace: String,
        @Query("outboundDate") outboundDate: String,
        @Query("adults") adults: Int,
        @Query("airlines") airlines: String  // Specific to Emirates (EK)
    ): Call<FlightSearchResponse>
}

