package com.example.myapplication.services

import retrofit2.http.GET
import retrofit2.http.Query

data class CurrencyResponse(
    val success: Boolean,
    val source: String,
    val quotes: Map<String, Double>
)

interface CurrencyApiService {
    @GET("api/live")
    suspend fun getExchangeRates(
        @Query("access_key") accessKey: String,
        @Query("currencies") currencies: String,
        @Query("source") source: String,
        @Query("format") format: Int = 1
    ): CurrencyResponse
}