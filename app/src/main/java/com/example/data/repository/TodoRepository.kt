package com.example.data.repository

import com.example.data.dao.CategoryDao
import com.example.data.dao.TaskDao
import com.example.data.dao.CompletionHistoryDao
import com.example.data.model.Category
import com.example.data.model.Task
import com.example.data.model.CompletionHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodoRepository(
    private val categoryDao: CategoryDao,
    private val taskDao: TaskDao,
    private val completionHistoryDao: CompletionHistoryDao
) {
    val categories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val homeTasks: Flow<List<Task>> = taskDao.getHomeTasks()
    val privateTasks: Flow<List<Task>> = taskDao.getPrivateTasks()
    val completionHistory: Flow<List<CompletionHistory>> = completionHistoryDao.getAllHistory()

    fun getTasksBySection(section: String): Flow<List<Task>> = taskDao.getTasksBySection(section)
    fun getTasksByCategory(categoryId: Int): Flow<List<Task>> = taskDao.getTasksByCategory(categoryId)

    suspend fun getCategoryById(id: Int): Category? = categoryDao.getCategoryById(id)
    suspend fun getTaskById(id: Int): Task? = taskDao.getTaskById(id)

    suspend fun insertCategory(name: String): Long {
        return categoryDao.insertCategory(Category(name = name))
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
        // Also delete history items for this task
        completionHistoryDao.deleteHistoryByTaskId(task.id)
    }

    suspend fun completeTask(task: Task, timestamp: Long = System.currentTimeMillis()) {
        val categoryName = task.categoryId?.let { catId ->
            categoryDao.getCategoryById(catId)?.name
        }

        val updatedTask = task.copy(
            isCompleted = true,
            completedAt = timestamp,
            updatedAt = timestamp
        )
        taskDao.updateTask(updatedTask)

        val history = CompletionHistory(
            taskId = task.id,
            taskTitle = task.title,
            categoryName = categoryName,
            section = task.section,
            completedAt = timestamp
        )
        completionHistoryDao.insertHistory(history)
    }

    suspend fun restoreTask(task: Task) {
        val updatedTask = task.copy(
            isCompleted = false,
            completedAt = null,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.updateTask(updatedTask)

        // Find and delete the latest completion history entry for this task
        val latest = completionHistoryDao.getLatestHistoryForTask(task.id)
        if (latest != null) {
            completionHistoryDao.deleteHistory(latest)
        }
    }

    suspend fun restoreHistoryItem(history: CompletionHistory) {
        // Delete history entry
        completionHistoryDao.deleteHistory(history)

        // Find associated task
        val task = taskDao.getTaskById(history.taskId) ?: return

        // Check if we should mark the task as incomplete
        if (task.section == "daily") {
            // Only uncomplete if this history item was completed today
            if (isToday(history.completedAt)) {
                taskDao.updateTask(task.copy(isCompleted = false, completedAt = null))
            }
        } else {
            // For non-daily tasks, restore them to incomplete state
            taskDao.updateTask(task.copy(isCompleted = false, completedAt = null))
        }
    }

    suspend fun deleteHistoryItemPermanently(history: CompletionHistory) {
        completionHistoryDao.deleteHistory(history)
    }

    suspend fun resetDailyTasks() {
        taskDao.performDailyReset()
    }

    private fun isToday(timestamp: Long): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return fmt.format(Date(timestamp)) == fmt.format(Date())
    }
}
