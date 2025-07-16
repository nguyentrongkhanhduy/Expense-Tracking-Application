package com.example.myapplication.services

import com.example.myapplication.data.model.User
import retrofit2.http.Body
import retrofit2.http.POST

data class SignUpRequest(
    val email: String,
    val password: String,
    val displayName: String
)

data class TokenRequest(val idToken: String)

data class FcmTokenRequest(
    val userId: String,
    val fcmToken: String
)

data class UserMessagePreference (
    val userId: String,
    val messagePreference: String
)

interface AuthApiService {
    @POST("api/auth/signin")
    suspend fun signIn(@Body request: TokenRequest): User

    @POST("api/auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): User

    @POST("api/auth/update-fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): Map<String, String>

    @POST("api/auth/update-message-preference")
    suspend fun updateMessagePreference(@Body request: UserMessagePreference): Map<String, String>
}