package com.positivefinancial.app.data.repository

import com.positivefinancial.app.data.local.dao.BudgetDao
import com.positivefinancial.app.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
    fun observeAll(): Flow<List<BudgetEntity>> = budgetDao.getAll()

    suspend fun getByCategoryId(categoryId: Long): BudgetEntity? = budgetDao.getByCategoryId(categoryId)

    suspend fun setBudget(categoryId: Long, monthlyLimit: Long) {
        budgetDao.upsert(BudgetEntity(categoryId = categoryId, monthlyLimit = monthlyLimit))
    }

    suspend fun deleteBudget(budget: BudgetEntity) = budgetDao.delete(budget)
}
