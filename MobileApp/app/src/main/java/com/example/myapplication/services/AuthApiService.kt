package com.example.myapplication.services

import com.example.myapplication.models.User
import retrofit2.http.Body
import retrofit2.http.POST

data class SignUpRequest(
    val email: String,
    val password: String,
    val displayName: String
)

data class TokenRequest(val idToken: String)

interface AuthApiService {
    @POST("api/auth/signin")
    suspend fun signIn(@Body request: TokenRequest): User

    @POST("api/auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): User
}

