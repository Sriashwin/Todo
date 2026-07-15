package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val categoryId: Int? = null,
    val section: String? = null, // "daily", "mini", "target", "none"
    val showOnHome: Boolean = true,
    val isPrivate: Boolean = false,
    val isStarred: Boolean = false,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val displayOrder: Int = 0,
    val completedAt: Long? = null,
    val completedOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val linkedTargetId: Int? = null,
    val isPassiveTarget: Boolean = false,
    val targetTimeframe: String? = null
)
