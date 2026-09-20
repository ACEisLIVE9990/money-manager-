package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryEntity
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.TransactionEntity
import com.example.ui.theme.Emerald600
import com.example.ui.theme.ExceededRose
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.MoneyOutRed
import com.example.ui.theme.WarningAmber
import com.example.util.CurrencyUtil
import com.example.util.WeekUtil

@Composable
fun AddEditTransactionDialog(
    initialType: String = "EXPENSE",
    transactionToEdit: TransactionEntity? = null,
    categories: List<CategoryEntity>,
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onConfirm: (type: String, desc: String, amount: Double, category: String, date: String, note: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(transactionToEdit?.type ?: initialType) }
    var description by remember { mutableStateOf(transactionToEdit?.description ?: "") }
    var amountText by remember { mutableStateOf(transactionToEdit?.amount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var selectedCategory by remember {
        val matchingCats = categories.filter { it.type == (transactionToEdit?.type ?: initialType) }
        val currentCat = transactionToEdit?.category ?: matchingCats.firstOrNull()?.name ?: "Other"
        mutableStateOf(currentCat)
    }
    var dateText by remember { mutableStateOf(transactionToEdit?.date ?: WeekUtil.todayDateString()) }
    var noteText by remember { mutableStateOf(transactionToEdit?.note ?: "") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val availableCategories = categories.filter { it.type == selectedType }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_edit_transaction_dialog"),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (transactionToEdit == null) "Add Transaction" else "Edit Transaction",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isIncome = selectedType == "INCOME"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedType = "INCOME"
                                val incCats = categories.filter { it.type == "INCOME" }
                                if (availableCategories.none { it.name == selectedCategory }) {
                                    selectedCategory = incCats.firstOrNull()?.name ?: "Salary"
                                }
                            }
                            .border(
                                width = if (isIncome) 2.dp else 1.dp,
                                color = if (isIncome) MoneyInGreen else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        color = if (isIncome) MoneyInGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "Money In",
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Normal,
                            color = if (isIncome) MoneyInGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val isExpense = selectedType == "EXPENSE"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedType = "EXPENSE"
                                val expCats = categories.filter { it.type == "EXPENSE" }
                                if (availableCategories.none { it.name == selectedCategory }) {
                                    selectedCategory = expCats.firstOrNull()?.name ?: "Food"
                                }
                            }
                            .border(
                                width = if (isExpense) 2.dp else 1.dp,
                                color = if (isExpense) MoneyOutRed else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        color = if (isExpense) MoneyOutRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "Money Out",
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Normal,
                            color = if (isExpense) MoneyOutRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        validationError = null
                    },
                    label = { Text("Amount ($currencySymbol)") },
                    placeholder = { Text("e.g. 500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        validationError = null
                    },
                    label = { Text("Description") },
                    placeholder = { Text("e.g. Grocery shopping, Freelance payment") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_desc_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            IconButton(onClick = { categoryDropdownExpanded = true }) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryDropdownExpanded = true }
                            .testTag("transaction_category_select"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        availableCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CategoryIconBadge(iconName = cat.iconName, colorHex = cat.colorHex, size = 28)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(cat.name)
                                    }
                                },
                                onClick = {
                                    selectedCategory = cat.name
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    placeholder = { Text("2026-09-20") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Note
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Optional Note") },
                    placeholder = { Text("Add any reminder or detail") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Validation error display
                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = CurrencyUtil.parseAmount(amountText)
                    when {
                        parsedAmount == null || parsedAmount <= 0.0 -> {
                            validationError = "Please enter a valid amount greater than 0."
                        }
                        description.trim().isBlank() -> {
                            validationError = "Please enter a transaction description."
                        }
                        selectedCategory.trim().isBlank() -> {
                            validationError = "Please select a category."
                        }
                        dateText.trim().length < 8 -> {
                            validationError = "Please enter a valid date in YYYY-MM-DD format."
                        }
                        else -> {
                            onConfirm(
                                selectedType,
                                description.trim(),
                                parsedAmount,
                                selectedCategory.trim(),
                                dateText.trim(),
                                noteText.trim()
                            )
                        }
                    }
                },
                modifier = Modifier.testTag("save_transaction_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (transactionToEdit == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditCategoryDialog(
    initialType: String = "EXPENSE",
    categoryToEdit: CategoryEntity? = null,
    existingCategories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, iconName: String, colorHex: Long) -> Unit
) {
    var name by remember { mutableStateOf(categoryToEdit?.name ?: "") }
    var selectedType by remember { mutableStateOf(categoryToEdit?.type ?: initialType) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val iconOptions = listOf(
        "restaurant", "directions_car", "receipt_long", "shopping_bag",
        "movie", "school", "favorite", "savings", "work", "laptop", "account_balance_wallet"
    )
    var selectedIcon by remember { mutableStateOf(categoryToEdit?.iconName ?: iconOptions.first()) }

    val colorOptions = listOf(
        0xFF10B981, 0xFF3B82F6, 0xFFF97316, 0xFFEF4444, 0xFFEC4899,
        0xFF8B5CF6, 0xFF6366F1, 0xFFEAB308, 0xFF14B8A6, 0xFF64748B
    )
    var selectedColor by remember { mutableStateOf(categoryToEdit?.colorHex ?: colorOptions.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("category_dialog"),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (categoryToEdit == null) "New Category" else "Rename Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type selector
                if (categoryToEdit == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedType = "INCOME" }
                                .border(
                                    width = if (selectedType == "INCOME") 2.dp else 1.dp,
                                    color = if (selectedType == "INCOME") MoneyInGreen else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            color = if (selectedType == "INCOME") MoneyInGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "Income",
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = if (selectedType == "INCOME") FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedType == "INCOME") MoneyInGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedType = "EXPENSE" }
                                .border(
                                    width = if (selectedType == "EXPENSE") 2.dp else 1.dp,
                                    color = if (selectedType == "EXPENSE") MoneyOutRed else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            color = if (selectedType == "EXPENSE") MoneyOutRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "Expense",
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = if (selectedType == "EXPENSE") FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedType == "EXPENSE") MoneyOutRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validationError = null
                    },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g. Gym, Subscriptions, Books") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Icons preview
                Text(
                    text = "Choose Icon",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    iconOptions.take(5).forEach { iconName ->
                        val isSelected = selectedIcon == iconName
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedIcon = iconName },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getVectorForIconName(iconName),
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Color preview
                Text(
                    text = "Choose Color",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    colorOptions.take(6).forEach { colorVal ->
                        val isSelected = selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .clickable { selectedColor = colorVal }
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = name.trim()
                    val duplicate = existingCategories.any {
                        it.name.equals(trimmed, ignoreCase = true) && it.id != (categoryToEdit?.id ?: 0)
                    }
                    when {
                        trimmed.isBlank() -> validationError = "Category name cannot be empty."
                        duplicate -> validationError = "A category with this name already exists."
                        else -> onConfirm(trimmed, selectedType, selectedIcon, selectedColor)
                    }
                },
                modifier = Modifier.testTag("save_category_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (categoryToEdit == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteCategoryDialog(
    category: CategoryEntity,
    usageCount: Int,
    fallbackCategories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirmReassign: (reassignTo: String) -> Unit,
    onConfirmDeleteTransactions: () -> Unit
) {
    var selectedFallback by remember {
        val defaultFallback = fallbackCategories.firstOrNull { it.name == "Other" }?.name
            ?: fallbackCategories.firstOrNull()?.name ?: "Other"
        mutableStateOf(defaultFallback)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("delete_category_dialog"),
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text("Delete Category: \"${category.name}\"")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (usageCount > 0) {
                    Text(
                        text = "This category is currently being used by $usageCount existing transaction(s).",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "What would you like to do with these transactions?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Are you sure you want to delete this category? This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            if (usageCount > 0) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onConfirmReassign(selectedFallback) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reassign to \"$selectedFallback\"")
                    }
                    OutlinedButton(
                        onClick = onConfirmDeleteTransactions,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete all $usageCount transactions")
                    }
                }
            } else {
                Button(
                    onClick = { onConfirmDeleteTransactions() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SetBudgetDialog(
    expenseCategories: List<CategoryEntity>,
    currencySymbol: String,
    initialCategory: String? = null,
    initialAmount: Double? = null,
    onDismiss: () -> Unit,
    onConfirm: (categoryName: String, amount: Double) -> Unit
) {
    var selectedCategory by remember {
        mutableStateOf(initialCategory ?: expenseCategories.firstOrNull()?.name ?: "Food")
    }
    var amountText by remember {
        mutableStateOf(initialAmount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("set_budget_dialog"),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (initialAmount == null) "Set Category Budget" else "Edit Category Budget",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            IconButton(onClick = { categoryDropdownExpanded = true }) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryDropdownExpanded = true },
                        shape = RoundedCornerShape(12.dp)
                    )

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        expenseCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CategoryIconBadge(iconName = cat.iconName, colorHex = cat.colorHex, size = 28)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(cat.name)
                                    }
                                },
                                onClick = {
                                    selectedCategory = cat.name
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Budget Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        validationError = null
                    },
                    label = { Text("Weekly Budget Limit ($currencySymbol)") },
                    placeholder = { Text("e.g. 1500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = CurrencyUtil.parseAmount(amountText)
                    if (parsed == null || parsed <= 0.0) {
                        validationError = "Please enter a budget limit greater than 0."
                    } else {
                        onConfirm(selectedCategory, parsed)
                    }
                },
                modifier = Modifier.testTag("save_budget_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditChecklistDialog(
    itemToEdit: ChecklistItemEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, dueDate: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf(itemToEdit?.title ?: "") }
    var dueDate by remember { mutableStateOf(itemToEdit?.dueDate ?: "") }
    var priority by remember { mutableStateOf(itemToEdit?.priority ?: "MEDIUM") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("checklist_dialog"),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (itemToEdit == null) "Add Checklist Task" else "Edit Task",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        validationError = null
                    },
                    label = { Text("Task") },
                    placeholder = { Text("e.g. Pay electricity bill, Transfer savings") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checklist_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Optional Due Date") },
                    placeholder = { Text("e.g. Wednesday or 2026-09-23") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val priorities = listOf("HIGH", "MEDIUM", "LOW")
                    priorities.forEach { p ->
                        val isSelected = priority == p
                        val pColor = when (p) {
                            "HIGH" -> ExceededRose
                            "MEDIUM" -> WarningAmber
                            else -> Emerald600
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { priority = p }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) pColor else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            color = if (isSelected) pColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = p.lowercase().replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) pColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.trim().isBlank()) {
                        validationError = "Please enter a task description."
                    } else {
                        onConfirm(title.trim(), dueDate.trim(), priority)
                    }
                },
                modifier = Modifier.testTag("save_checklist_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (itemToEdit == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DataViewerDialog(
    title: String,
    dataText: String,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Data has been prepared locally. You can copy it to your clipboard:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dataText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                if (copied) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✓ Copied to clipboard!",
                        style = MaterialTheme.typography.labelSmall,
                        color = Emerald600,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(dataText))
                    copied = true
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy to Clipboard")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ImportDataDialog(
    onDismiss: () -> Unit,
    onConfirmImport: (jsonString: String) -> Unit
) {
    var jsonInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Import Backup Data", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Paste your previously exported JSON backup below. This will restore your data locally:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = {
                        jsonInput = it
                        errorMsg = null
                    },
                    placeholder = { Text("{\"version\": 1, ...}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMsg ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (jsonInput.trim().isBlank()) {
                        errorMsg = "Please paste valid JSON backup data."
                    } else {
                        onConfirmImport(jsonInput.trim())
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ResetConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmReset: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { Text("Reset All Data?") },
        text = {
            Text("This will permanently delete all your transactions, custom budgets, and checklist items, resetting the planner to defaults. All operations are 100% local.")
        },
        confirmButton = {
            Button(
                onClick = onConfirmReset,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reset Everything")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
