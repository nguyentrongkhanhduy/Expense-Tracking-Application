package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: com.example.myapplication.data.model.Transaction)

    @Update
    suspend fun updateTransaction(transaction: com.example.myapplication.data.model.Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: com.example.myapplication.data.model.Transaction)

    @Query("SELECT * FROM 'Transaction' ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<com.example.myapplication.data.model.Transaction>>

    @Query("UPDATE 'Transaction' SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignCategory(oldCategoryId: Long, newCategoryId: Long)

    @Transaction
    @Query("SELECT * FROM 'Transaction' ORDER BY date DESC")
    fun getAllTransactionsWithCategory(): Flow<List<TransactionWithCategory>>

    @Query("SELECT SUM(amount) FROM 'Transaction' WHERE type = 'Expense' AND categoryId = :categoryId")
    fun getTotalAmountForCategory(categoryId: Long): Double

    @Query("UPDATE 'Transaction' SET amount = amount * :exchangeRate")
    suspend fun updateAllAmountsByExchangeRate(exchangeRate: Double)
}