package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Category
import com.example.data.model.Task
import com.example.ui.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddDialog(
    viewModel: TodoViewModel,
    categories: List<Category>,
    initialCategoryId: Int? = null,
    initialSection: String? = null,
    initialPrivate: Boolean = false,
    onDismiss: () -> Unit
) {
    val allTasks by viewModel.allTasks.collectAsState()

    var isBulkAdd by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var bulkText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(initialCategoryId) }
    var selectedSection by remember { mutableStateOf<String?>(initialSection ?: categories.find { it.id == initialCategoryId }?.defaultSection) }
    var isStarred by remember { mutableStateOf(false) }

    // 3-way visibility option: "public" (on home), "hidden" (store only), "private" (vault)
    var taskType by remember { mutableStateOf(if (initialPrivate) "private" else "public") }

    // Target configuration fields
    var isPassiveTarget by remember { mutableStateOf(false) }
    var targetTimeframe by remember { mutableStateOf<String?>(null) }

    // Linking target field
    var linkedTargetId by remember { mutableStateOf<Int?>(null) }

    // Suggestion logic for categories and duplicate warning
    val existingTaskWithCategory = remember(title, allTasks) {
        if (title.isBlank()) null else {
            allTasks.find { 
                it.title.equals(title.trim(), ignoreCase = true) && it.categoryId != null
            }
        }
    }
    
    val recommendedCategory = remember(existingTaskWithCategory, categories) {
        existingTaskWithCategory?.categoryId?.let { catId ->
            categories.find { it.id == catId }
        }
    }

    val isDuplicate = remember(title, allTasks) {
        if (title.isBlank()) false else {
            allTasks.any { it.title.equals(title.trim(), ignoreCase = true) }
        }
    }

    // Keyboard Shortcuts Auto-parser for single mode
    LaunchedEffect(title) {
        if (!isBulkAdd) {
            // Parse hashtag for Category (e.g., #Work)
            val categoryRegex = Regex("#(\\w+)")
            val matchCategory = categoryRegex.find(title)
            if (matchCategory != null) {
                val catName = matchCategory.groupValues[1]
                val matchedCat = categories.find { it.name.equals(catName, ignoreCase = true) }
                if (matchedCat != null) {
                    selectedCategoryId = matchedCat.id
                }
            }

            // Parse slashes for Section (e.g., /daily, /mini, /target)
            if (title.contains("/daily", ignoreCase = true)) {
                selectedSection = "daily"
            } else if (title.contains("/mini", ignoreCase = true)) {
                selectedSection = "mini"
            } else if (title.contains("/target", ignoreCase = true)) {
                selectedSection = "target"
            }
        }
    }

    // Get incomplete targets for linking
    val targetTasksList = remember(allTasks) {
        allTasks.filter { it.section == "target" && !it.isCompleted && !it.isPrivate }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("quick_add_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Task",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Single vs Bulk tab selector
                TabRow(
                    selectedTabIndex = if (isBulkAdd) 1 else 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = !isBulkAdd,
                        onClick = { isBulkAdd = false },
                        text = { Text("Single Goal") }
                    )
                    Tab(
                        selected = isBulkAdd,
                        onClick = { isBulkAdd = true },
                        text = { Text("Bulk Notepad") }
                    )
                }

                if (!isBulkAdd) {
                    // Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("What needs to be done?") },
                        placeholder = { Text("Use #Category or /daily /mini /target shortcuts") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_title_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    // Suggestion & Warning Engine
                    if (isDuplicate) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Task already exists (Duplicate warning)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (recommendedCategory != null && selectedCategoryId != recommendedCategory.id) {
                        Surface(
                            onClick = { selectedCategoryId = recommendedCategory.id },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Recommendation",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Auto-suggest category: #${recommendedCategory.name} (Click to apply)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    // Bulk Text Area
                    OutlinedTextField(
                        value = bulkText,
                        onValueChange = { bulkText = it },
                        label = { Text("Paste task list (one per line)") },
                        placeholder = { Text("Example:\nInception\nInterstellar\nThe Dark Knight\nTenet") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("bulk_tasks_input"),
                        maxLines = 10,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                // Category Selector Chips
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { selectedCategoryId = null },
                            label = { Text("None") },
                            leadingIcon = {
                                if (selectedCategoryId == null) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                        categories.forEach { category ->
                            FilterChip(
                                selected = selectedCategoryId == category.id,
                                onClick = { 
                                    selectedCategoryId = category.id
                                    category.defaultSection?.let {
                                        selectedSection = it
                                    }
                                },
                                label = { Text(category.name) }
                            )
                        }
                    }
                }

                // Section Selector Segmented Buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Time Horizon (Section)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            null to "None (Inbox)",
                            "mini" to "Mini Today",
                            "daily" to "Daily Habit",
                            "target" to "Long Target"
                        ).forEach { (value, label) ->
                            val isSelected = selectedSection == value
                            ElevatedFilterChip(
                                selected = isSelected,
                                onClick = { selectedSection = value },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                // Conditionally render Target Config if section is target
                if (selectedSection == "target") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Target Configuration",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Passive Target", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("No daily routines needed, just an end timeframe goal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = isPassiveTarget, onCheckedChange = { isPassiveTarget = it })
                        }

                        Text("Achieve within timeframe:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                null to "General",
                                "week" to "This Week",
                                "month" to "This Month",
                                "year" to "This Year"
                            ).forEach { (value, label) ->
                                FilterChip(
                                    selected = targetTimeframe == value,
                                    onClick = { targetTimeframe = value },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }

                // Conditionally render Linking selector if section is mini or daily
                if ((selectedSection == "daily" || selectedSection == "mini") && targetTasksList.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Link to Long-Term Target (Goal)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = linkedTargetId == null,
                                onClick = { linkedTargetId = null },
                                label = { Text("None") }
                            )
                            targetTasksList.forEach { target ->
                                FilterChip(
                                    selected = linkedTargetId == target.id,
                                    onClick = { linkedTargetId = target.id },
                                    label = { Text(target.title) }
                                )
                            }
                        }
                    }
                }

                // 3-Way Security & Visibility selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Visibility & Security Type",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            "public" to Icons.Default.Visibility to "Public (On Home)",
                            "hidden" to Icons.Default.VisibilityOff to "Stored Only (Hide)",
                            "private" to Icons.Default.Lock to "Private Vault"
                        ).forEach { (pair, label) ->
                            val (type, icon) = pair
                            val isSelected = taskType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { taskType = type },
                                label = { Text(label) },
                                leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }

                // Starred Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isStarred = !isStarred }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Star/Pin", style = MaterialTheme.typography.bodyMedium)
                            Text("Highlight at top of lists", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(checked = isStarred, onCheckedChange = { isStarred = it })
                }

                if (!isBulkAdd) {
                    // Notes Description
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes/Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                // Save Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val showHome = taskType == "public"
                            val isPrivate = taskType == "private"

                            if (isBulkAdd) {
                                val lines = bulkText.split("\n")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                if (lines.isNotEmpty()) {
                                    lines.forEach { taskTitle ->
                                        viewModel.addTask(
                                            title = taskTitle,
                                            categoryId = selectedCategoryId,
                                            section = selectedSection,
                                            showOnHome = showHome,
                                            isPrivate = isPrivate,
                                            isStarred = isStarred,
                                            notes = "",
                                            linkedTargetId = if (selectedSection == "daily" || selectedSection == "mini") linkedTargetId else null,
                                            isPassiveTarget = if (selectedSection == "target") isPassiveTarget else false,
                                            targetTimeframe = if (selectedSection == "target") targetTimeframe else null
                                        )
                                    }
                                    onDismiss()
                                }
                            } else {
                                if (title.isNotBlank()) {
                                    // Clean up title shortcuts
                                    var cleanedTitle = title
                                    cleanedTitle = cleanedTitle.replace(Regex("#\\w+"), "").trim()
                                    cleanedTitle = cleanedTitle.replace("/daily", "", ignoreCase = true).trim()
                                    cleanedTitle = cleanedTitle.replace("/mini", "", ignoreCase = true).trim()
                                    cleanedTitle = cleanedTitle.replace("/target", "", ignoreCase = true).trim()

                                    viewModel.addTask(
                                        title = if (cleanedTitle.isNotBlank()) cleanedTitle else title,
                                        categoryId = selectedCategoryId,
                                        section = selectedSection,
                                        showOnHome = showHome,
                                        isPrivate = isPrivate,
                                        isStarred = isStarred,
                                        notes = notes,
                                        linkedTargetId = if (selectedSection == "daily" || selectedSection == "mini") linkedTargetId else null,
                                        isPassiveTarget = if (selectedSection == "target") isPassiveTarget else false,
                                        targetTimeframe = if (selectedSection == "target") targetTimeframe else null
                                    )
                                    onDismiss()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("save_task_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBulkAdd) "Add Tasks" else "Add Task")
                    }
                }
            }
        }
    }
}
