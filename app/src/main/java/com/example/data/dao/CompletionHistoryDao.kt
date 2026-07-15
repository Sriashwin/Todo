package com.example.data.dao

import androidx.room.*
import com.example.data.model.CompletionHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionHistoryDao {
    @Query("SELECT * FROM completion_history ORDER BY completedAt DESC")
    fun getAllHistory(): Flow<List<CompletionHistory>>

    @Query("SELECT * FROM completion_history WHERE taskId = :taskId ORDER BY completedAt DESC")
    fun getHistoryByTask(taskId: Int): Flow<List<CompletionHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: CompletionHistory): Long

    @Delete
    suspend fun deleteHistory(history: CompletionHistory)

    @Query("DELETE FROM completion_history WHERE taskId = :taskId")
    suspend fun deleteHistoryByTaskId(taskId: Int)

    @Query("SELECT * FROM completion_history WHERE taskId = :taskId ORDER BY completedAt DESC LIMIT 1")
    suspend fun getLatestHistoryForTask(taskId: Int): CompletionHistory?
}
