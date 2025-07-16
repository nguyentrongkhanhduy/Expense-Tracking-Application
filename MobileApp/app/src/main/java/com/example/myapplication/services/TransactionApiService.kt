package com.example.myapplication.services

import com.example.myapplication.data.model.Transaction
import retrofit2.http.*

data class TransactionRequest(
    val userId: String,
    val transaction: Transaction
)

data class RequestedImage (
    val imageName: String,
    val imageData: String,
    val contentType: String = "image/jpeg"
)

data class ImageRequest(
    val userId: String,
    val requestedImage: RequestedImage
)

data class ImageResponse(
    val success: Boolean,
    val imageUrl: String?,       // null if upload failed
    val error: String? = null    // non-null if error occurred
)

data class RemoveImageRequest(
    val userId: String,
    val imageName: String
)

data class ReassignCategoryRequest(
    val userId: String,
    val oldCategoryId: Long,
    val newCategoryId: Long
)


interface TransactionApiService {

    @POST("api/transactions/get")
    suspend fun getTransactions(@Body request: UserIdRequest): List<Transaction>

    @POST("api/transactions/create")
    suspend fun createTransaction(@Body request: TransactionRequest): Map<String, Any>

    @PUT("api/transactions/{transactionId}")
    suspend fun updateTransaction(@Path("transactionId") transactionId: Long, @Body request: TransactionRequest): Map<String, Any>

    @POST("api/transactions/reassign-category")
    suspend fun reassignCategory(@Body request: ReassignCategoryRequest): Map<String, Any>

    @POST("api/transactions/{transactionId}")
    suspend fun deleteTransaction(@Path("transactionId") transactionId: Long, @Body request: UserIdRequest): Map<String, Any>

    @POST("api/transactions/upload-image")
    suspend fun uploadImage(@Body request: ImageRequest): ImageResponse

    @PUT("api/transactions/update-image")
    suspend fun updateImage(@Body request: ImageRequest): ImageResponse

    @POST("api/transactions/remove-image")
    suspend fun deleteImage(@Body request: RemoveImageRequest): Map<String, Any>

    @POST("api/transactions/send-test-notification")
    suspend fun sendTestNotification(@Body request: UserIdRequest): Map<String, Any>
}