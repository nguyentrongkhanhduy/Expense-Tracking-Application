package com.example.myapplication.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
//    val instance: AuthApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl("http://10.0.2.2:3000")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(AuthApiService::class.java)
//    }
    fun <T> createService(serviceClass: Class<T>, baseUrl: String): T {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(serviceClass)
    }
}