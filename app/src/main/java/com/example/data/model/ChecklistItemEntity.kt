package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val weekKey: String,
    val title: String,
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val timestamp: Long = System.currentTimeMillis()
)
