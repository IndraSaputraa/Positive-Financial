package com.positivefinancial.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.positivefinancial.app.data.model.CategoryType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: CategoryType,
    val iconKey: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0
)
