package com.example.myapplication.services

import com.example.myapplication.data.model.Category
import retrofit2.http.*

data class CategoryRequest(
    val userId: String,
    val category: Category
)

data class InitialCategoriesRequest(
    val userId: String,
    val categories: List<Category>
)

data class UserIdRequest(val userId: String)

interface CategoryApiService {

    @POST("api/categories/get")
    suspend fun getCategories(@Body userId: String): List<Category>

    @POST("api/categories/create")
    suspend fun createCategory(@Body request: CategoryRequest): Map<String, Any>

    @POST("api/categories/initial")
    suspend fun createInitialCategories(@Body initialCategoriesRequest: InitialCategoriesRequest): Map<String, Any>

    @PUT("api/categories/{categoryId}")
    suspend fun updateCategory(@Path("categoryId") categoryId: Long, @Body request: CategoryRequest): Map<String, Any>

    @POST("api/categories/{categoryId}")
    suspend fun deleteCategory(@Path("categoryId") categoryId: Long, @Body request: UserIdRequest): Map<String, Any>
}