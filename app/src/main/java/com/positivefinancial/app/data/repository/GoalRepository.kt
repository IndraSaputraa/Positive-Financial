package com.positivefinancial.app.data.repository

import com.positivefinancial.app.data.local.dao.GoalDao
import com.positivefinancial.app.data.local.entity.GoalEntity
import com.positivefinancial.app.data.model.GoalType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao
) {
    fun observeActive(): Flow<List<GoalEntity>> = goalDao.getActive()

    suspend fun getById(id: Long): GoalEntity? = goalDao.getById(id)

    suspend fun addGoal(
        name: String,
        goalType: GoalType,
        targetAmount: Long,
        linkedAccountId: Long,
        targetDate: Long?,
        iconKey: String,
        colorHex: String
    ): Long = goalDao.insert(
        GoalEntity(
            name = name,
            goalType = goalType,
            targetAmount = targetAmount,
            linkedAccountId = linkedAccountId,
            targetDate = targetDate,
            iconKey = iconKey,
            colorHex = colorHex
        )
    )

    suspend fun updateGoal(goal: GoalEntity) = goalDao.update(goal)

    suspend fun deleteGoal(goal: GoalEntity) = goalDao.delete(goal)
}
