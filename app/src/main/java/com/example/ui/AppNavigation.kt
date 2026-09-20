package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CategoryEntity
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.AddEditCategoryDialog
import com.example.ui.components.AddEditChecklistDialog
import com.example.ui.components.AddEditTransactionDialog
import com.example.ui.components.DataViewerDialog
import com.example.ui.components.DeleteCategoryDialog
import com.example.ui.components.ImportDataDialog
import com.example.ui.components.ResetConfirmationDialog
import com.example.ui.components.SetBudgetDialog
import com.example.ui.model.NavDestination
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.ChecklistScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MoneyInScreen
import com.example.ui.screens.MoneyOutScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.Emerald600
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.CurrencyUtil
import com.example.util.WeekUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyBudgetApp(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(NavDestination.DASHBOARD) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val selectedWeekKey by viewModel.selectedWeekKey.collectAsStateWithLifecycle()
    val isWeekMonday by viewModel.weekStartMonday.collectAsStateWithLifecycle()
    val summary by viewModel.financialSummary.collectAsStateWithLifecycle()

    // Dialog States
    var showTransactionDialog by remember { mutableStateOf(false) }
    var transactionDialogType by remember { mutableStateOf("EXPENSE") }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryDialogType by remember { mutableStateOf("EXPENSE") }
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }

    var showDeleteCategoryDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryUsageCount by remember { mutableStateOf(0) }

    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInitialCategory by remember { mutableStateOf<String?>(null) }
    var budgetInitialAmount by remember { mutableStateOf<Double?>(null) }

    var showChecklistDialog by remember { mutableStateOf(false) }
    var checklistItemToEdit by remember { mutableStateOf<ChecklistItemEntity?>(null) }

    var showDataViewerDialog by remember { mutableStateOf(false) }
    var dataViewerTitle by remember { mutableStateOf("") }
    var dataViewerContent by remember { mutableStateOf("") }

    var showImportDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                // Drawer Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_app_logo),
                            contentDescription = "Weekly Budget Planner Logo",
                            modifier = Modifier
                                .size(56.dp)
                        )
                        Column {
                            Text(
                                text = "Weekly Budget",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Planner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Plan Today • A Better Tomorrow",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = WeekUtil.getWeekRangeDisplay(selectedWeekKey, isWeekMonday),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Remaining: ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyUtil.format(summary.remaining, currencySymbol),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.remaining >= 0) Emerald600 else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Navigation Items
                DrawerItem(
                    label = "Dashboard",
                    icon = Icons.Default.Dashboard,
                    selected = currentDestination == NavDestination.DASHBOARD,
                    onClick = {
                        currentDestination = NavDestination.DASHBOARD
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerItem(
                    label = "Money In",
                    icon = Icons.Default.ArrowDownward,
                    selected = currentDestination == NavDestination.MONEY_IN,
                    onClick = {
                        currentDestination = NavDestination.MONEY_IN
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerItem(
                    label = "Money Out",
                    icon = Icons.Default.ArrowUpward,
                    selected = currentDestination == NavDestination.MONEY_OUT,
                    onClick = {
                        currentDestination = NavDestination.MONEY_OUT
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerItem(
                    label = "Weekly Budgets",
                    icon = Icons.Default.PieChart,
                    selected = currentDestination == NavDestination.BUDGETS,
                    onClick = {
                        currentDestination = NavDestination.BUDGETS
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerItem(
                    label = "Weekly Checklist",
                    icon = Icons.Default.Checklist,
                    selected = currentDestination == NavDestination.CHECKLIST,
                    onClick = {
                        currentDestination = NavDestination.CHECKLIST
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerItem(
                    label = "Categories",
                    icon = Icons.Default.Category,
                    selected = currentDestination == NavDestination.CATEGORIES,
                    onClick = {
                        currentDestination = NavDestination.CATEGORIES
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerItem(
                    label = "Weekly History",
                    icon = Icons.Default.CalendarMonth,
                    selected = currentDestination == NavDestination.HISTORY,
                    onClick = {
                        currentDestination = NavDestination.HISTORY
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerItem(
                    label = "Settings & Backup",
                    icon = Icons.Default.Settings,
                    selected = currentDestination == NavDestination.SETTINGS,
                    onClick = {
                        currentDestination = NavDestination.SETTINGS
                        coroutineScope.launch { drawerState.close() }
                    }
                )

                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "100% Local & Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentDestination.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = WeekUtil.getWeekRangeDisplay(selectedWeekKey, isWeekMonday),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("app_menu_button")
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Navigation Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    NavigationBarItem(
                        selected = currentDestination == NavDestination.DASHBOARD,
                        onClick = { currentDestination = NavDestination.DASHBOARD },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") },
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )
                    NavigationBarItem(
                        selected = currentDestination == NavDestination.MONEY_IN,
                        onClick = { currentDestination = NavDestination.MONEY_IN },
                        icon = { Icon(Icons.Default.ArrowDownward, contentDescription = "Money In") },
                        label = { Text("Income") },
                        modifier = Modifier.testTag("nav_item_money_in")
                    )
                    NavigationBarItem(
                        selected = currentDestination == NavDestination.MONEY_OUT,
                        onClick = { currentDestination = NavDestination.MONEY_OUT },
                        icon = { Icon(Icons.Default.ArrowUpward, contentDescription = "Money Out") },
                        label = { Text("Expenses") },
                        modifier = Modifier.testTag("nav_item_money_out")
                    )
                    NavigationBarItem(
                        selected = currentDestination == NavDestination.BUDGETS,
                        onClick = { currentDestination = NavDestination.BUDGETS },
                        icon = { Icon(Icons.Default.PieChart, contentDescription = "Budgets") },
                        label = { Text("Budgets") },
                        modifier = Modifier.testTag("nav_item_budgets")
                    )
                    NavigationBarItem(
                        selected = currentDestination == NavDestination.CHECKLIST,
                        onClick = { currentDestination = NavDestination.CHECKLIST },
                        icon = { Icon(Icons.Default.Checklist, contentDescription = "Checklist") },
                        label = { Text("Checklist") },
                        modifier = Modifier.testTag("nav_item_checklist")
                    )
                }
            },
            floatingActionButton = {
                if (currentDestination == NavDestination.DASHBOARD) {
                    FloatingActionButton(
                        onClick = {
                            transactionDialogType = "EXPENSE"
                            transactionToEdit = null
                            showTransactionDialog = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("fab_add_transaction")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentDestination,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { dest ->
                    when (dest) {
                        NavDestination.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigate = { currentDestination = it },
                            onAddTransaction = { type ->
                                transactionDialogType = type
                                transactionToEdit = null
                                showTransactionDialog = true
                            },
                            onAddTask = {
                                checklistItemToEdit = null
                                showChecklistDialog = true
                            },
                            onEditTransaction = { tx ->
                                transactionToEdit = tx
                                transactionDialogType = tx.type
                                showTransactionDialog = true
                            }
                        )

                        NavDestination.MONEY_IN -> MoneyInScreen(
                            viewModel = viewModel,
                            onAddIncome = {
                                transactionDialogType = "INCOME"
                                transactionToEdit = null
                                showTransactionDialog = true
                            },
                            onEditIncome = { tx ->
                                transactionToEdit = tx
                                transactionDialogType = tx.type
                                showTransactionDialog = true
                            }
                        )

                        NavDestination.MONEY_OUT -> MoneyOutScreen(
                            viewModel = viewModel,
                            onAddExpense = {
                                transactionDialogType = "EXPENSE"
                                transactionToEdit = null
                                showTransactionDialog = true
                            },
                            onEditExpense = { tx ->
                                transactionToEdit = tx
                                transactionDialogType = tx.type
                                showTransactionDialog = true
                            }
                        )

                        NavDestination.BUDGETS -> BudgetsScreen(
                            viewModel = viewModel,
                            onSetBudget = { cat, amt ->
                                budgetInitialCategory = cat
                                budgetInitialAmount = amt
                                showBudgetDialog = true
                            },
                            snackbarHostState = snackbarHostState
                        )

                        NavDestination.CHECKLIST -> ChecklistScreen(
                            viewModel = viewModel,
                            onAddTask = {
                                checklistItemToEdit = null
                                showChecklistDialog = true
                            },
                            onEditTask = { item ->
                                checklistItemToEdit = item
                                showChecklistDialog = true
                            },
                            snackbarHostState = snackbarHostState
                        )

                        NavDestination.CATEGORIES -> CategoriesScreen(
                            viewModel = viewModel,
                            onAddCategory = { type ->
                                categoryDialogType = type
                                categoryToEdit = null
                                showCategoryDialog = true
                            },
                            onEditCategory = { cat ->
                                categoryToEdit = cat
                                showCategoryDialog = true
                            },
                            onDeleteCategory = { cat ->
                                viewModel.checkCategoryUsage(cat.name) { count ->
                                    categoryToDelete = cat
                                    categoryUsageCount = count
                                    showDeleteCategoryDialog = true
                                }
                            }
                        )

                        NavDestination.HISTORY -> HistoryScreen(
                            viewModel = viewModel,
                            onNavigate = { currentDestination = it }
                        )

                        NavDestination.SETTINGS -> SettingsScreen(
                            viewModel = viewModel,
                            onShowDataViewer = { title, content ->
                                dataViewerTitle = title
                                dataViewerContent = content
                                showDataViewerDialog = true
                            },
                            onOpenImportDialog = { showImportDialog = true },
                            onConfirmResetDialog = { showResetConfirmDialog = true },
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    }

    // 1. Add / Edit Transaction Dialog
    if (showTransactionDialog) {
        AddEditTransactionDialog(
            initialType = transactionDialogType,
            transactionToEdit = transactionToEdit,
            categories = categories,
            currencySymbol = currencySymbol,
            onDismiss = { showTransactionDialog = false },
            onConfirm = { type, desc, amount, category, date, note ->
                if (transactionToEdit == null) {
                    viewModel.addTransaction(type, desc, amount, category, date, note)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            if (type == "INCOME") "Added Income: $desc" else "Added Expense: $desc"
                        )
                    }
                } else {
                    viewModel.updateTransaction(transactionToEdit!!.id, type, desc, amount, category, date, note)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Transaction updated!")
                    }
                }
                showTransactionDialog = false
            }
        )
    }

    // 2. Add / Edit Category Dialog
    if (showCategoryDialog) {
        AddEditCategoryDialog(
            initialType = categoryDialogType,
            categoryToEdit = categoryToEdit,
            existingCategories = categories,
            onDismiss = { showCategoryDialog = false },
            onConfirm = { name, type, iconName, colorHex ->
                if (categoryToEdit == null) {
                    viewModel.addCategory(name, type, iconName, colorHex)
                    coroutineScope.launch { snackbarHostState.showSnackbar("Category \"$name\" created!") }
                } else {
                    viewModel.updateCategory(categoryToEdit!!, name)
                    coroutineScope.launch { snackbarHostState.showSnackbar("Category renamed to \"$name\"!") }
                }
                showCategoryDialog = false
            }
        )
    }

    // 3. Delete Category Dialog
    if (showDeleteCategoryDialog && categoryToDelete != null) {
        val cat = categoryToDelete!!
        val fallbackCats = categories.filter { it.type == cat.type && it.name != cat.name }
        DeleteCategoryDialog(
            category = cat,
            usageCount = categoryUsageCount,
            fallbackCategories = fallbackCats,
            onDismiss = {
                showDeleteCategoryDialog = false
                categoryToDelete = null
            },
            onConfirmReassign = { reassignTo ->
                viewModel.deleteCategory(cat, reassignTo = reassignTo, deleteTransactions = false)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Category deleted. Transactions reassigned to \"$reassignTo\".")
                }
                showDeleteCategoryDialog = false
                categoryToDelete = null
            },
            onConfirmDeleteTransactions = {
                viewModel.deleteCategory(cat, reassignTo = null, deleteTransactions = true)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Category and associated transactions deleted.")
                }
                showDeleteCategoryDialog = false
                categoryToDelete = null
            }
        )
    }

    // 4. Set Budget Dialog
    if (showBudgetDialog) {
        val expenseCats = categories.filter { it.type == "EXPENSE" }
        SetBudgetDialog(
            expenseCategories = expenseCats,
            currencySymbol = currencySymbol,
            initialCategory = budgetInitialCategory,
            initialAmount = budgetInitialAmount,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { categoryName, amount ->
                viewModel.setCategoryBudget(categoryName, amount)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Budget limit for $categoryName set to ${CurrencyUtil.format(amount, currencySymbol)}!")
                }
                showBudgetDialog = false
            }
        )
    }

    // 5. Add / Edit Checklist Dialog
    if (showChecklistDialog) {
        AddEditChecklistDialog(
            itemToEdit = checklistItemToEdit,
            onDismiss = { showChecklistDialog = false },
            onConfirm = { title, dueDate, priority ->
                if (checklistItemToEdit == null) {
                    viewModel.addChecklistItem(title, dueDate, priority)
                    coroutineScope.launch { snackbarHostState.showSnackbar("Task added to weekly checklist!") }
                } else {
                    viewModel.updateChecklistItem(checklistItemToEdit!!, title, dueDate, priority)
                    coroutineScope.launch { snackbarHostState.showSnackbar("Task updated!") }
                }
                showChecklistDialog = false
            }
        )
    }

    // 6. Data Viewer Dialog (JSON / CSV export preview & copy)
    if (showDataViewerDialog) {
        DataViewerDialog(
            title = dataViewerTitle,
            dataText = dataViewerContent,
            onDismiss = { showDataViewerDialog = false }
        )
    }

    // 7. Import Data Dialog
    if (showImportDialog) {
        ImportDataDialog(
            onDismiss = { showImportDialog = false },
            onConfirmImport = { jsonString ->
                viewModel.importBackupJson(jsonString) { success, msg ->
                    coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                    if (success) showImportDialog = false
                }
            }
        )
    }

    // 8. Reset Confirm Dialog
    if (showResetConfirmDialog) {
        ResetConfirmationDialog(
            onDismiss = { showResetConfirmDialog = false },
            onConfirmReset = {
                viewModel.resetAllData {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("All data has been reset to defaults.")
                    }
                    showResetConfirmDialog = false
                    currentDestination = NavDestination.DASHBOARD
                }
            }
        )
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        icon = { Icon(imageVector = icon, contentDescription = null) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        shape = RoundedCornerShape(12.dp)
    )
}
