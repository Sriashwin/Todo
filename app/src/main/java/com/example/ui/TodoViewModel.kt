package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Category
import com.example.data.model.CompletionHistory
import com.example.data.model.Task
import com.example.data.repository.TodoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TodoRepository(
            database.categoryDao(),
            database.taskDao(),
            database.completionHistoryDao()
        )
        checkAndPerformDailyReset()
        insertDefaultCategories()
    }

    val categories: StateFlow<List<Category>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val homeTasks: StateFlow<List<Task>> = repository.homeTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val privateTasks: StateFlow<List<Task>> = repository.privateTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completionHistory: StateFlow<List<CompletionHistory>> = repository.completionHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App Navigation States
    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _selectedSection = MutableStateFlow<String?>(null) // "daily", "mini", "target"
    val selectedSection: StateFlow<String?> = _selectedSection.asStateFlow()

    // Private Vault Security
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _vaultPin = MutableStateFlow("")
    val vaultPin: StateFlow<String> = _vaultPin.asStateFlow()

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryFilterId = MutableStateFlow<Int?>(null)
    val categoryFilterId: StateFlow<Int?> = _categoryFilterId.asStateFlow()

    private val _showCompletedInHome = MutableStateFlow(false)
    val showCompletedInHome: StateFlow<Boolean> = _showCompletedInHome.asStateFlow()

    // Reordering state
    private val _isReorderMode = MutableStateFlow(false)
    val isReorderMode: StateFlow<Boolean> = _isReorderMode.asStateFlow()

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
        // Lock private vault when leaving private section
        if (screen != "private" && screen != "categories") {
            _isVaultUnlocked.value = false
        }
    }

    fun selectCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
        navigateTo("category_detail")
    }

    fun selectSection(section: String?) {
        _selectedSection.value = section
        navigateTo("section_detail")
    }

    fun setVaultUnlocked(unlocked: Boolean) {
        _isVaultUnlocked.value = unlocked
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(id: Int?) {
        _categoryFilterId.value = id
    }

    fun toggleShowCompletedInHome() {
        _showCompletedInHome.value = !_showCompletedInHome.value
    }

    fun toggleReorderMode() {
        _isReorderMode.value = !_isReorderMode.value
    }

    // Task CRUD Operations
    fun addTask(
        title: String,
        categoryId: Int?,
        section: String?,
        showOnHome: Boolean,
        isPrivate: Boolean,
        isStarred: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val maxOrder = allTasks.value.maxOfOrNull { it.displayOrder } ?: 0
            val task = Task(
                title = title,
                categoryId = categoryId,
                section = section,
                showOnHome = showOnHome,
                isPrivate = isPrivate,
                isStarred = isStarred,
                notes = notes,
                displayOrder = maxOrder + 1
            )
            repository.insertTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            if (task.isCompleted) {
                repository.restoreTask(task)
            } else {
                repository.completeTask(task)
            }
        }
    }

    // Category Operations
    fun addCategory(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertCategory(name)
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // Completed Screen Action
    fun restoreHistoryItem(history: CompletionHistory) {
        viewModelScope.launch {
            repository.restoreHistoryItem(history)
        }
    }

    fun deleteHistoryItemPermanently(history: CompletionHistory) {
        viewModelScope.launch {
            repository.deleteHistoryItemPermanently(history)
        }
    }

    // Drag-and-Drop / Reordering actions
    fun moveTaskUp(tasks: List<Task>, index: Int) {
        if (index <= 0 || index >= tasks.size) return
        viewModelScope.launch {
            val list = tasks.toMutableList()
            val temp = list[index]
            list[index] = list[index - 1]
            list[index - 1] = temp
            saveTasksOrder(list)
        }
    }

    fun moveTaskDown(tasks: List<Task>, index: Int) {
        if (index < 0 || index >= tasks.size - 1) return
        viewModelScope.launch {
            val list = tasks.toMutableList()
            val temp = list[index]
            list[index] = list[index + 1]
            list[index + 1] = temp
            saveTasksOrder(list)
        }
    }

    private suspend fun saveTasksOrder(orderedList: List<Task>) {
        orderedList.forEachIndexed { i, task ->
            repository.updateTask(task.copy(displayOrder = i))
        }
    }

    // PIN Authentication Settings
    fun getSavedPin(): String {
        val prefs = getApplication<Application>().getSharedPreferences("vault_prefs", Context.MODE_PRIVATE)
        return prefs.getString("vault_pin", "") ?: ""
    }

    fun saveVaultPin(pin: String) {
        val prefs = getApplication<Application>().getSharedPreferences("vault_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("vault_pin", pin).apply()
    }

    // Daily Midnight Reset Implementation
    private fun checkAndPerformDailyReset() {
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("vault_prefs", Context.MODE_PRIVATE)
            val lastResetDate = prefs.getString("last_reset_date", "") ?: ""
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            if (lastResetDate != todayDate) {
                repository.resetDailyTasks()
                prefs.edit().putString("last_reset_date", todayDate).apply()
            }
        }
    }

    // Seed default categories if database is empty
    private fun insertDefaultCategories() {
        viewModelScope.launch {
            val current = repository.categories.firstOrNull() ?: emptyList()
            if (current.isEmpty()) {
                repository.insertCategory("Personal")
                repository.insertCategory("Work")
                repository.insertCategory("Projects")
                repository.insertCategory("Health")
                repository.insertCategory("Study")
            }
        }
    }

    // Grouping completed tasks by timeframe
    fun groupHistoryByDate(historyList: List<CompletionHistory>): Map<String, List<CompletionHistory>> {
        val groups = LinkedHashMap<String, MutableList<CompletionHistory>>()
        val today = Calendar.getInstance().apply { setTimeToMidnight() }
        val yesterday = Calendar.getInstance().apply {
            setTimeToMidnight()
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val lastWeek = Calendar.getInstance().apply {
            setTimeToMidnight()
            add(Calendar.DAY_OF_YEAR, -7)
        }

        val currentMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            setTimeToMidnight()
        }

        historyList.forEach { item ->
            val itemCal = Calendar.getInstance().apply { timeInMillis = item.completedAt }
            val label = when {
                itemCal.after(today) -> "Today"
                itemCal.after(yesterday) -> "Yesterday"
                itemCal.after(lastWeek) -> "Last Week"
                itemCal.after(currentMonth) -> "This Month"
                else -> {
                    val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    sdf.format(Date(item.completedAt))
                }
            }
            if (!groups.containsKey(label)) {
                groups[label] = mutableListOf()
            }
            groups[label]?.add(item)
        }
        return groups
    }

    private fun Calendar.setTimeToMidnight() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}
