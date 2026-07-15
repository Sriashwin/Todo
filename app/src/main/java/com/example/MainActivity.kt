package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Category
import com.example.data.model.Task
import com.example.ui.TodoViewModel
import com.example.ui.components.QuickAddDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppMainContainer()
            }
        }
    }
}

@Composable
fun AppMainContainer() {
    val viewModel: TodoViewModel = viewModel()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val history by viewModel.completionHistory.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val selectedSection by viewModel.selectedSection.collectAsStateWithLifecycle()

    var showQuickAddDialog by remember { mutableStateOf(false) }
    var forceSectionInQuickAdd by remember { mutableStateOf<String?>(null) }
    var forceCategoryInQuickAdd by remember { mutableStateOf<Int?>(null) }
    var forcePrivateInQuickAdd by remember { mutableStateOf(false) }

    // Filter tasks for sub-screens
    val privateTasks = tasks.filter { it.isPrivate }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only show bottom navigation on primary root tabs
            if (currentScreen in listOf("home", "categories", "completed", "settings")) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "home",
                        onClick = { viewModel.navigateTo("home") },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_home_tab")
                    )
                    NavigationBarItem(
                        selected = currentScreen == "categories" || currentScreen == "category_detail" || currentScreen == "private",
                        onClick = { viewModel.navigateTo("categories") },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Categories") },
                        label = { Text("Categories") },
                        modifier = Modifier.testTag("nav_categories_tab")
                    )
                    NavigationBarItem(
                        selected = currentScreen == "completed",
                        onClick = { viewModel.navigateTo("completed") },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Completed History") },
                        label = { Text("Completed") },
                        modifier = Modifier.testTag("nav_completed_tab")
                    )
                    NavigationBarItem(
                        selected = currentScreen == "settings",
                        onClick = { viewModel.navigateTo("settings") },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("nav_settings_tab")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (currentScreen in listOf("home", "categories", "completed", "settings")) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            when (currentScreen) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    tasks = tasks,
                    categories = categories,
                    onOpenQuickAdd = {
                        forceSectionInQuickAdd = null
                        forceCategoryInQuickAdd = null
                        forcePrivateInQuickAdd = false
                        showQuickAddDialog = true
                    }
                )

                "categories" -> CategoriesScreen(
                    viewModel = viewModel,
                    categories = categories,
                    tasks = tasks
                )

                "completed" -> CompletedScreen(
                    viewModel = viewModel,
                    history = history,
                    categories = categories
                )

                "settings" -> SettingsScreen(viewModel = viewModel)

                "private" -> PrivateFolderScreen(
                    viewModel = viewModel,
                    privateTasks = privateTasks,
                    categories = categories,
                    onOpenQuickAddPrivate = {
                        forceSectionInQuickAdd = null
                        forceCategoryInQuickAdd = null
                        forcePrivateInQuickAdd = true
                        showQuickAddDialog = true
                    }
                )

                "category_detail" -> {
                    val category = categories.find { it.id == selectedCategoryId }
                    if (category != null) {
                        CategoryDetailScreen(
                            category = category,
                            viewModel = viewModel,
                            categories = categories,
                            tasks = tasks,
                            onBack = { viewModel.navigateTo("categories") },
                            onOpenQuickAdd = {
                                forceSectionInQuickAdd = null
                                forceCategoryInQuickAdd = category.id
                                forcePrivateInQuickAdd = false
                                showQuickAddDialog = true
                            }
                        )
                    } else {
                        viewModel.navigateTo("categories")
                    }
                }
            }
        }
    }

    // Quick Add dialog integration
    if (showQuickAddDialog) {
        QuickAddDialog(
            viewModel = viewModel,
            categories = categories,
            onDismiss = { showQuickAddDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    category: Category,
    viewModel: TodoViewModel,
    categories: List<Category>,
    tasks: List<Task>,
    onBack: () -> Unit,
    onOpenQuickAdd: () -> Unit
) {
    val categoryTasks = tasks.filter { it.categoryId == category.id && !it.isPrivate }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(category.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenQuickAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "CATEGORY • TARGETS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (categoryTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No goals set under #$category. Add one to get started!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                itemsIndexed(categoryTasks) { index, task ->
                    TaskListItem(
                        task = task,
                        index = index,
                        tasksInList = categoryTasks,
                        isReorderMode = false,
                        categories = categories,
                        onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                        onEdit = { },
                        onMoveUp = { },
                        onMoveDown = { }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
