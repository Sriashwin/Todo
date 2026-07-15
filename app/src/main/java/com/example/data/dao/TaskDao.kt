package com.example.data.dao

import androidx.room.*
import com.example.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY displayOrder ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE showOnHome = 1 AND isPrivate = 0 ORDER BY displayOrder ASC")
    fun getHomeTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isPrivate = 1 ORDER BY displayOrder ASC")
    fun getPrivateTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE section = :section AND isPrivate = 0 ORDER BY displayOrder ASC")
    fun getTasksBySection(section: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId AND isPrivate = 0 ORDER BY displayOrder ASC")
    fun getTasksByCategory(categoryId: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Transaction
    suspend fun performDailyReset() {
        resetDailyTasks()
        archiveCompletedMiniTasks()
    }

    @Query("UPDATE tasks SET isCompleted = 0, completedAt = NULL WHERE section = 'daily'")
    suspend fun resetDailyTasks()

    @Query("UPDATE tasks SET section = NULL, showOnHome = 0 WHERE section = 'mini' AND isCompleted = 1")
    suspend fun archiveCompletedMiniTasks()
}
