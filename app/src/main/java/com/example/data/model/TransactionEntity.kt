package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val weekKey: String,
    val type: String, // "INCOME" or "EXPENSE"
    val description: String,
    val amount: Double,
    val category: String,
    val date: String, // "yyyy-MM-dd"
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
