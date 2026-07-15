package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Category
import com.example.data.model.Task
import com.example.ui.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: TodoViewModel,
    categories: List<Category>,
    tasks: List<Task>
) {
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    var currentSavedPin by remember { mutableStateOf(viewModel.getSavedPin()) }
    var showHiddenUnlocked by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var securityDialogPurpose by remember { mutableStateOf("") } // "reveal" or "toggle_hide"
    var pendingCategoryToToggle by remember { mutableStateOf<Category?>(null) }
    var activeCategoryToEdit by remember { mutableStateOf<Category?>(null) }

    val displayedCategories = remember(categories, showHiddenUnlocked) {
        if (showHiddenUnlocked) categories else categories.filter { !it.isHidden }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Organize tasks by topic & focus area",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { showAddCategoryDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("add_category_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Category")
                }
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
            // SECURED PRIVATE FOLDER HEADER
            item {
                Card(
                    onClick = { viewModel.navigateTo("private") },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("private_vault_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Private Vault",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Private Folder",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val privateCount = tasks.filter { it.isPrivate }.size
                                Text(
                                    text = "$privateCount confidential targets",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Unlock",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // TOPIC CATEGORIES HEADER WITH LOCK ICON
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Topic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Topic Categories",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    TextButton(
                        onClick = {
                            if (showHiddenUnlocked) {
                                showHiddenUnlocked = false
                            } else {
                                securityDialogPurpose = "reveal"
                                showSecurityDialog = true
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (showHiddenUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("toggle_hidden_categories_lock")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (showHiddenUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showHiddenUnlocked) "Hide Locked" else "Unlock Hidden",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            if (displayedCategories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (categories.any { it.isHidden } && !showHiddenUnlocked) {
                                "Some categories are hidden. Unlock to view."
                            } else {
                                "No custom categories created yet."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(displayedCategories) { category ->
                    val taskCount = tasks.filter { it.categoryId == category.id && !it.isPrivate }.size
                    val completedCount = tasks.filter { it.categoryId == category.id && !it.isPrivate && it.isCompleted }.size

                    Card(
                        onClick = { viewModel.selectCategory(category.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (category.isHidden) MaterialTheme.colorScheme.surfaceVariant
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        )
                                ) {
                                    Icon(
                                        imageVector = if (category.isHidden) Icons.Default.VisibilityOff else Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = if (category.isHidden) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (category.isHidden) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (category.isHidden) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Hidden",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        category.defaultSection?.let { section ->
                                            Spacer(modifier = Modifier.width(6.dp))
                                            val badgeText = when (section) {
                                                "mini" -> "MINI"
                                                "daily" -> "DAILY"
                                                "target" -> "TARGET"
                                                else -> section.uppercase()
                                            }
                                            Text(
                                                text = badgeText,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$taskCount targets ($completedCount completed)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (category.isHidden) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { activeCategoryToEdit = category },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Category",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(2.dp))

                                IconButton(
                                    onClick = {
                                        if (showHiddenUnlocked) {
                                            viewModel.toggleCategoryVisibility(category)
                                        } else {
                                            pendingCategoryToToggle = category
                                            securityDialogPurpose = "toggle_hide"
                                            showSecurityDialog = true
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (category.isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility",
                                        tint = if (category.isHidden) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(2.dp))

                                IconButton(
                                    onClick = { viewModel.deleteCategory(category) },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Category", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    placeholder = { Text("Category name (e.g. Health, Book)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_category_input_field")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addCategory(newCategoryName.trim())
                            newCategoryName = ""
                            showAddCategoryDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_category_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Category Dialog
    if (activeCategoryToEdit != null) {
        var editName by remember { mutableStateOf(activeCategoryToEdit!!.name) }
        var editSection by remember { mutableStateOf(activeCategoryToEdit!!.defaultSection) }

        AlertDialog(
            onDismissRequest = { activeCategoryToEdit = null },
            title = { Text("Category Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Category Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_category_name_input")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Default Time Horizon",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Assigning a time horizon automatically applies it to all new and existing tasks in this category.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val sections = listOf(
                            Triple(null, "Mixed / None", Icons.Default.Topic),
                            Triple("mini", "Mini Today", Icons.Default.Bolt),
                            Triple("daily", "Daily Habits", Icons.Default.Event),
                            Triple("target", "Target Goals", Icons.Default.Flag)
                        )

                        sections.forEach { (secKey, secLabel, secIcon) ->
                            Card(
                                onClick = { editSection = secKey },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (editSection == secKey) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    }
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (editSection == secKey) MaterialTheme.colorScheme.primary else Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = secIcon,
                                        contentDescription = null,
                                        tint = if (editSection == secKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = secLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (editSection == secKey) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            val original = activeCategoryToEdit!!
                            if (original.name != editName.trim()) {
                                viewModel.updateCategory(original.copy(name = editName.trim()))
                            }
                            if (original.defaultSection != editSection) {
                                viewModel.updateCategoryDefaultSection(original, editSection)
                            }
                            activeCategoryToEdit = null
                        }
                    },
                    modifier = Modifier.testTag("save_category_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeCategoryToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Security Pin Dialog
    if (showSecurityDialog) {
        var pinInput by remember { mutableStateOf("") }
        var setupStep by remember { mutableStateOf(if (currentSavedPin.isEmpty()) 1 else 0) }
        var setupFirstPin by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSecurityDialog = false },
            title = {
                Text(
                    text = when (setupStep) {
                        1 -> "Setup Vault PIN"
                        2 -> "Confirm Vault PIN"
                        else -> "Security Authentication"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = when (setupStep) {
                            1 -> "Create a 4-digit security PIN to protect hidden categories."
                            2 -> "Re-enter the 4-digit PIN to confirm."
                            else -> "Enter your security PIN to proceed."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 4) {
                                pinInput = input
                            }
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        singleLine = true,
                        placeholder = { Text("••••") },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.width(120.dp).testTag("security_pin_input")
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length < 4) {
                            errorMessage = "PIN must be 4 digits"
                            return@Button
                        }
                        if (setupStep == 1) {
                            setupFirstPin = pinInput
                            pinInput = ""
                            setupStep = 2
                            errorMessage = ""
                        } else if (setupStep == 2) {
                            if (pinInput == setupFirstPin) {
                                viewModel.saveVaultPin(pinInput)
                                currentSavedPin = pinInput
                                // Succeeded setting up PIN! Now perform original action
                                if (securityDialogPurpose == "reveal") {
                                    showHiddenUnlocked = true
                                } else if (securityDialogPurpose == "toggle_hide" && pendingCategoryToToggle != null) {
                                    viewModel.toggleCategoryVisibility(pendingCategoryToToggle!!)
                                    pendingCategoryToToggle = null
                                }
                                showSecurityDialog = false
                            } else {
                                errorMessage = "PINs do not match!"
                                pinInput = ""
                                setupStep = 1
                            }
                        } else {
                            if (pinInput == currentSavedPin) {
                                if (securityDialogPurpose == "reveal") {
                                    showHiddenUnlocked = true
                                } else if (securityDialogPurpose == "toggle_hide" && pendingCategoryToToggle != null) {
                                    viewModel.toggleCategoryVisibility(pendingCategoryToToggle!!)
                                    pendingCategoryToToggle = null
                                }
                                showSecurityDialog = false
                            } else {
                                errorMessage = "Incorrect PIN"
                                pinInput = ""
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_security_pin")
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSecurityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
