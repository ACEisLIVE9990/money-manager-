package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category_budgets",
    indices = [Index(value = ["weekKey", "categoryName"], unique = true)]
)
data class CategoryBudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val weekKey: String,
    val categoryName: String,
    val budgetAmount: Double
)
