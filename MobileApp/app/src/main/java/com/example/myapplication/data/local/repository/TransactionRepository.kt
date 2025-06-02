package com.example.myapplication.data.local.repository

import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.data.local.model.Transaction
import com.example.myapplication.data.local.model.TransactionWithCategory

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {

    fun getAllTransactions(): Flow<List<Transaction>> = dao.getAllTransactions()

    suspend fun insertTransaction(transaction: Transaction) {
        dao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        dao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        dao.deleteTransaction(transaction)
    }


    suspend fun reassignCategory(oldCategoryId: Long, newCategoryId: Long) {
        dao.reassignCategory(oldCategoryId, newCategoryId)
    }

    fun getTransactionsWithCategory(): Flow<List<TransactionWithCategory>> = dao.getAllTransactionsWithCategory()

    fun getTotalAmountForCategory(categoryId: Long): Double = dao.getTotalAmountForCategory(categoryId)
}