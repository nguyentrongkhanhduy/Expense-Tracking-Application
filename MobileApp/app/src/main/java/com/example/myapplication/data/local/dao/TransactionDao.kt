package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: com.example.myapplication.data.local.model.Transaction)

    @Update
    suspend fun updateTransaction(transaction: com.example.myapplication.data.local.model.Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: com.example.myapplication.data.local.model.Transaction)

    @Query("SELECT * FROM 'Transaction' ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<com.example.myapplication.data.local.model.Transaction>>

    @Query("UPDATE 'Transaction' SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignCategory(oldCategoryId: Long, newCategoryId: Long)

    @Transaction
    @Query("SELECT * FROM 'Transaction' ORDER BY date DESC")
    fun getAllTransactionsWithCategory(): Flow<List<TransactionWithCategory>>
}