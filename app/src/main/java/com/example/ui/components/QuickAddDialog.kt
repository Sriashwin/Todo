package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Category
import com.example.ui.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddDialog(
    viewModel: TodoViewModel,
    categories: List<Category>,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedSection by remember { mutableStateOf<String?>(null) }
    var showOnHome by remember { mutableStateOf(true) }
    var isPrivate by remember { mutableStateOf(false) }
    var isStarred by remember { mutableStateOf(false) }

    // Keyboard Shortcuts Auto-parser
    LaunchedEffect(title) {
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
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
                                onClick = { selectedCategoryId = category.id },
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
                            "mini" to "Mini Today",
                            "daily" to "Daily Habit",
                            "target" to "Long Target"
                        ).forEach { (value, label) ->
                            val isSelected = selectedSection == value
                            ElevatedFilterChip(
                                selected = isSelected,
                                onClick = { selectedSection = if (isSelected) null else value },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                // Flags (Show on Home, Private, Starred)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Show on Home Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showOnHome = !showOnHome }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Show on Home", style = MaterialTheme.typography.bodyMedium)
                                Text("Visible in daily sections", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = showOnHome, onCheckedChange = { showOnHome = it })
                    }

                    // Private Vault Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPrivate = !isPrivate }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Private Task", style = MaterialTheme.typography.bodyMedium)
                                Text("Hidden in encrypted folder", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
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
                }

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
                                    showOnHome = showOnHome,
                                    isPrivate = isPrivate,
                                    isStarred = isStarred,
                                    notes = notes
                                )
                                onDismiss()
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
                        Text("Add Task")
                    }
                }
            }
        }
    }
}
