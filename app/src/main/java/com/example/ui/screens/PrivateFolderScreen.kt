package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Task
import com.example.ui.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateFolderScreen(
    viewModel: TodoViewModel,
    privateTasks: List<Task>,
    categories: List<Category>,
    onOpenQuickAddPrivate: () -> Unit
) {
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val savedPin = remember { viewModel.getSavedPin() }

    var inputPin by remember { mutableStateOf("") }
    var setupStep by remember { mutableStateOf(if (savedPin.isEmpty()) 1 else 0) } // 1: set PIN, 2: confirm PIN
    var setupFirstPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    if (!isVaultUnlocked) {
        // Locked State: Numeric Keypad Overlay
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Secured Vault", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateTo("categories") }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Keypad Heading instruction
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val headingText = when (setupStep) {
                        1 -> "Create Private Vault PIN"
                        2 -> "Confirm Private Vault PIN"
                        else -> "Vault is Locked"
                    }
                    Text(
                        text = headingText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val subText = when (setupStep) {
                        1 -> "Enter a 4-digit PIN to secure private targets"
                        2 -> "Re-enter the 4-digit PIN to verify"
                        else -> "Enter PIN to access your private targets"
                    }
                    Text(
                        text = subText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // PIN Input Dot Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    repeat(4) { idx ->
                        val isFilled = idx < inputPin.length
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) MaterialTheme.colorScheme.tertiary 
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                // Numeric Pad Grid
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("Clear", "0", "OK")
                    )

                    keys.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            row.forEach { key ->
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (key == "Clear" || key == "OK") Color.Transparent
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .then(
                                            if (key != "Clear" && key != "OK") {
                                                Modifier.border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = CircleShape
                                                )
                                            } else Modifier
                                        )
                                        .clickable {
                                            when (key) {
                                                "Clear" -> {
                                                    if (inputPin.isNotEmpty()) {
                                                        inputPin = inputPin.dropLast(1)
                                                    }
                                                }
                                                "OK" -> {
                                                    if (inputPin.length == 4) {
                                                        when (setupStep) {
                                                            1 -> {
                                                                setupFirstPin = inputPin
                                                                inputPin = ""
                                                                setupStep = 2
                                                                errorMessage = ""
                                                            }
                                                            2 -> {
                                                                if (inputPin == setupFirstPin) {
                                                                    viewModel.saveVaultPin(inputPin)
                                                                    viewModel.setVaultUnlocked(true)
                                                                    errorMessage = ""
                                                                } else {
                                                                    errorMessage = "PINs do not match. Restarting."
                                                                    setupStep = 1
                                                                    inputPin = ""
                                                                }
                                                            }
                                                            else -> {
                                                                val saved = viewModel.getSavedPin()
                                                                if (inputPin == saved) {
                                                                    viewModel.setVaultUnlocked(true)
                                                                    errorMessage = ""
                                                                } else {
                                                                    errorMessage = "Incorrect PIN. Try again."
                                                                    inputPin = ""
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    if (inputPin.length < 4) {
                                                        inputPin += key
                                                    }
                                                }
                                            }
                                        }
                                ) {
                                    if (key == "Clear") {
                                        Icon(Icons.Default.Backspace, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    } else if (key == "OK") {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Submit", tint = MaterialTheme.colorScheme.tertiary)
                                    } else {
                                        Text(
                                            text = key,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Unlocked State: Display private targets
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Private Vault", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateTo("categories") }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.setVaultUnlocked(false) }) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock Vault", tint = MaterialTheme.colorScheme.tertiary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onOpenQuickAddPrivate,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("floating_add_private_task_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Private Target", modifier = Modifier.size(28.dp))
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
                        text = "Encrypted Targets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (privateTasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No private goals. Keep them secured here!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    itemsIndexed(privateTasks) { index, task ->
                        TaskListItem(
                            task = task,
                            index = index,
                            tasksInList = privateTasks,
                            isReorderMode = false,
                            categories = categories,
                            allTasks = allTasks,
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
}
