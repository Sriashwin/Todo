package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Category
import com.example.data.model.Task
import com.example.ui.TodoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TodoViewModel,
    tasks: List<Task>,
    categories: List<Category>,
    onOpenQuickAdd: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categoryFilterId by viewModel.categoryFilterId.collectAsState()
    val isReorderMode by viewModel.isReorderMode.collectAsState()
    val showCompletedInHome by viewModel.showCompletedInHome.collectAsState()

    var activeTaskToEdit by remember { mutableStateOf<Task?>(null) }

    // Section Collapsed States
    var miniExpanded by remember { mutableStateOf(true) }
    var dailyExpanded by remember { mutableStateOf(true) }
    var targetExpanded by remember { mutableStateOf(true) }

    // Filter tasks based on settings
    val filteredTasks = tasks.filter { task ->
        val matchesSearch = task.title.contains(searchQuery, ignoreCase = true) || 
                            task.notes.contains(searchQuery, ignoreCase = true)
        val matchesCategory = categoryFilterId == null || task.categoryId == categoryFilterId
        val matchesPrivacy = !task.isPrivate
        val matchesVisibility = task.showOnHome || searchQuery.isNotBlank() // show hidden if searching
        val matchesCompletion = !task.isCompleted || showCompletedInHome

        matchesSearch && matchesCategory && matchesPrivacy && matchesVisibility && matchesCompletion
    }

    val miniTasks = filteredTasks.filter { it.section == "mini" }
    val dailyTasks = filteredTasks.filter { it.section == "daily" }
    val targetTasks = filteredTasks.filter { it.section == "target" }

    // Analytics computation
    val totalHomeTasks = tasks.filter { !it.isPrivate && it.showOnHome }.size
    val completedHomeTasks = tasks.filter { !it.isPrivate && it.showOnHome && it.isCompleted }.size
    val progressRatio = if (totalHomeTasks > 0) completedHomeTasks.toFloat() / totalHomeTasks else 0f

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                // Main Header Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Home Tasks",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        val currentDate = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
                        Text(
                            text = currentDate,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Toolbar actions
                    Row {
                        IconButton(
                            onClick = { viewModel.toggleShowCompletedInHome() },
                            modifier = Modifier.testTag("toggle_completed_home")
                        ) {
                            Icon(
                                imageVector = if (showCompletedInHome) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle completed visibility",
                                tint = if (showCompletedInHome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { viewModel.toggleReorderMode() },
                            modifier = Modifier.testTag("toggle_reorder_mode")
                        ) {
                            Icon(
                                imageVector = if (isReorderMode) Icons.Default.SortByAlpha else Icons.Default.Reorder,
                                contentDescription = "Toggle reorder mode",
                                tint = if (isReorderMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Beautiful Search Bar & Category filters row
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search title, notes...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                        .testTag("home_search_input"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Category Chips Selector
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = categoryFilterId == null,
                            onClick = { viewModel.setCategoryFilter(null) },
                            label = { Text("All Categories") }
                        )
                    }
                    items(categories.size) { index ->
                        val category = categories[index]
                        FilterChip(
                            selected = categoryFilterId == category.id,
                            onClick = { viewModel.setCategoryFilter(category.id) },
                            label = { Text(category.name) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenQuickAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("floating_add_task_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(28.dp))
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
            // High Fidelity Progress Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Today's Focus",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (totalHomeTasks > 0) {
                                    "$completedHomeTasks of $totalHomeTasks targets completed"
                                } else {
                                    "No tasks set for home. Add one to start!"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (totalHomeTasks > 0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { progressRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Text(
                                text = "${(progressRatio * 100).toInt()}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // SECTION 1: MINI TARGETS
            item {
                SectionHeader(
                    title = "Mini (Today's Targets)",
                    count = miniTasks.size,
                    expanded = miniExpanded,
                    onToggle = { miniExpanded = !miniExpanded }
                )
            }
            if (miniExpanded) {
                if (miniTasks.isEmpty()) {
                    item { SectionEmptyState(tip = "No mini goals left. Set some quick tasks!") }
                } else {
                    itemsIndexed(miniTasks) { index, task ->
                        TaskListItem(
                            task = task,
                            index = index,
                            tasksInList = miniTasks,
                            isReorderMode = isReorderMode,
                            categories = categories,
                            onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                            onEdit = { activeTaskToEdit = task },
                            onMoveUp = { viewModel.moveTaskUp(miniTasks, index) },
                            onMoveDown = { viewModel.moveTaskDown(miniTasks, index) }
                        )
                    }
                }
            }

            // SECTION 2: DAILY HABITS
            item {
                SectionHeader(
                    title = "Daily (Routines)",
                    count = dailyTasks.size,
                    expanded = dailyExpanded,
                    onToggle = { dailyExpanded = !dailyExpanded }
                )
            }
            if (dailyExpanded) {
                if (dailyTasks.isEmpty()) {
                    item { SectionEmptyState(tip = "Habits are empty. Add a recurring daily routine!") }
                } else {
                    itemsIndexed(dailyTasks) { index, task ->
                        TaskListItem(
                            task = task,
                            index = index,
                            tasksInList = dailyTasks,
                            isReorderMode = isReorderMode,
                            categories = categories,
                            onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                            onEdit = { activeTaskToEdit = task },
                            onMoveUp = { viewModel.moveTaskUp(dailyTasks, index) },
                            onMoveDown = { viewModel.moveTaskDown(dailyTasks, index) }
                        )
                    }
                }
            }

            // SECTION 3: TARGET GOALS
            item {
                SectionHeader(
                    title = "Target (Long-Term Goals)",
                    count = targetTasks.size,
                    expanded = targetExpanded,
                    onToggle = { targetExpanded = !targetExpanded }
                )
            }
            if (targetExpanded) {
                if (targetTasks.isEmpty()) {
                    item { SectionEmptyState(tip = "No long term target goals defined.") }
                } else {
                    itemsIndexed(targetTasks) { index, task ->
                        TaskListItem(
                            task = task,
                            index = index,
                            tasksInList = targetTasks,
                            isReorderMode = isReorderMode,
                            categories = categories,
                            onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                            onEdit = { activeTaskToEdit = task },
                            onMoveUp = { viewModel.moveTaskUp(targetTasks, index) },
                            onMoveDown = { viewModel.moveTaskDown(targetTasks, index) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Active Edit Dialog if needed
    if (activeTaskToEdit != null) {
        EditTaskDialog(
            task = activeTaskToEdit!!,
            categories = categories,
            onDismiss = { activeTaskToEdit = null },
            onSave = { updated ->
                viewModel.updateTask(updated)
                activeTaskToEdit = null
            },
            onDelete = {
                viewModel.deleteTask(it)
                activeTaskToEdit = null
            }
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val formattedTitle = remember(title) {
        when {
            title.contains("Mini", ignoreCase = true) -> "MINI • TODAY"
            title.contains("Daily", ignoreCase = true) -> "DAILY • ROUTINE"
            title.contains("Target", ignoreCase = true) -> "TARGET • GOALS"
            else -> title.uppercase()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formattedTitle,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count remaining",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = "Collapse Section",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun SectionEmptyState(tip: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tip,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun TaskListItem(
    task: Task,
    index: Int,
    tasksInList: List<Task>,
    isReorderMode: Boolean,
    categories: List<Category>,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val category = categories.find { it.id == task.categoryId }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onEdit,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Geometric Custom Square Checkbox
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (task.isCompleted) MaterialTheme.colorScheme.primary 
                        else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onToggleComplete() }
                    .testTag("task_checkbox_${task.id}")
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (task.isStarred) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Starred",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // If this is a Target section task, show a gorgeous pill progress tracker underneath
                if (task.section == "target") {
                    Spacer(modifier = Modifier.height(8.dp))
                    val progressFraction = if (task.isCompleted) 1f else 0.33f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                if (category != null || task.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (category != null) {
                            Text(
                                text = "#${category.name.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        if (task.notes.isNotBlank() && task.section != "target") {
                            Icon(
                                imageVector = Icons.Default.Notes,
                                contentDescription = "Has notes",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Reorder controls vs arrow edit
            if (isReorderMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Move Up",
                            tint = if (index > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < tasksInList.size - 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Move Down",
                            tint = if (index < tasksInList.size - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Edit details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EditTaskDialog(
    task: Task,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var notes by remember { mutableStateOf(task.notes) }
    var categoryId by remember { mutableStateOf(task.categoryId) }
    var section by remember { mutableStateOf(task.section) }
    var showOnHome by remember { mutableStateOf(task.showOnHome) }
    var isPrivate by remember { mutableStateOf(task.isPrivate) }
    var isStarred by remember { mutableStateOf(task.isStarred) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Task",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { categoryId = null },
                            label = { Text("None") },
                            leadingIcon = { if (categoryId == null) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        categories.forEach { cat ->
                            FilterChip(
                                selected = categoryId == cat.id,
                                onClick = { categoryId = cat.id },
                                label = { Text(cat.name) }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Section", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("mini" to "Mini Today", "daily" to "Daily Habit", "target" to "Long Target").forEach { (value, label) ->
                            FilterChip(
                                selected = section == value,
                                onClick = { section = if (section == value) null else value },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Checkbox(checked = showOnHome, onCheckedChange = { showOnHome = it })
                        Text("Show Home", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Checkbox(checked = isPrivate, onCheckedChange = { isPrivate = it })
                        Text("Private", style = MaterialTheme.typography.bodySmall)
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onDelete(task) },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onSave(
                                        task.copy(
                                            title = title,
                                            notes = notes,
                                            categoryId = categoryId,
                                            section = section,
                                            showOnHome = showOnHome,
                                            isPrivate = isPrivate,
                                            isStarred = isStarred
                                        )
                                    )
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
