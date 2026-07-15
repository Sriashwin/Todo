package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completion_history")
data class CompletionHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val taskTitle: String,
    val categoryName: String? = null,
    val section: String? = null,
    val completedAt: Long = System.currentTimeMillis()
)
