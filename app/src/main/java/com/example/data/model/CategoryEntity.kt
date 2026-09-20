package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME,
    EXPENSE
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: String, // "INCOME" or "EXPENSE"
    val iconName: String = "category",
    val colorHex: Long = 0xFF10B981,
    val isDefault: Boolean = false
)
