package com.positivefinancial.app.data.repository

import com.positivefinancial.app.data.local.dao.CategoryDao
import com.positivefinancial.app.data.local.entity.CategoryEntity
import com.positivefinancial.app.data.model.CategoryType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun observeAll(): Flow<List<CategoryEntity>> = categoryDao.getAll()

    fun observeByType(type: CategoryType): Flow<List<CategoryEntity>> = categoryDao.getByType(type)

    suspend fun getCategory(id: Long): CategoryEntity? = categoryDao.getById(id)

    suspend fun addCategory(name: String, type: CategoryType, iconKey: String, colorHex: String): Long =
        categoryDao.insert(CategoryEntity(name = name, type = type, iconKey = iconKey, colorHex = colorHex))

    suspend fun updateCategory(category: CategoryEntity) = categoryDao.update(category)

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.delete(category)

    suspend fun isEmpty(): Boolean = categoryDao.count() == 0

    suspend fun seedDefaults(categories: List<CategoryEntity>) = categoryDao.insertAll(categories)
}
