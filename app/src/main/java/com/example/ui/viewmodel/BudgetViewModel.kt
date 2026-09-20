package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BudgetDatabase
import com.example.data.model.AppSettingsEntity
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.BudgetRepository
import com.example.ui.model.CategoryBudgetUiModel
import com.example.ui.model.CategorySpendingUiModel
import com.example.ui.model.FilterState
import com.example.ui.model.FinancialSummaryUiModel
import com.example.ui.model.WeeklyHistoryItemUiModel
import com.example.util.BackupUtil
import com.example.util.WeekUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BudgetDatabase.getDatabase(application, viewModelScope)
    private val repository = BudgetRepository(database)

    // Settings
    val settings: StateFlow<Map<String, String>> = repository.getAllSettings()
        .combine(MutableStateFlow(Unit)) { list, _ ->
            list.associate { it.key to it.value }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = mapOf("currency" to "₹", "theme" to "SYSTEM", "weekStart" to "MONDAY")
        )

    val currencySymbol: StateFlow<String> = settings
        .combine(MutableStateFlow(Unit)) { s, _ -> s["currency"] ?: "₹" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")

    val themeMode: StateFlow<String> = settings
        .combine(MutableStateFlow(Unit)) { s, _ -> s["theme"] ?: "SYSTEM" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val weekStartMonday: StateFlow<Boolean> = settings
        .combine(MutableStateFlow(Unit)) { s, _ -> (s["weekStart"] ?: "MONDAY") == "MONDAY" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Week state
    val selectedWeekKey = MutableStateFlow(WeekUtil.getCurrentWeekKey(true))

    fun isCurrentWeek(weekKey: String): Boolean {
        return weekKey == WeekUtil.getCurrentWeekKey(weekStartMonday.value)
    }

    fun goToPreviousWeek() {
        selectedWeekKey.value = WeekUtil.getPreviousWeekKey(selectedWeekKey.value, weekStartMonday.value)
    }

    fun goToNextWeek() {
        selectedWeekKey.value = WeekUtil.getNextWeekKey(selectedWeekKey.value, weekStartMonday.value)
    }

    fun goToCurrentWeek() {
        selectedWeekKey.value = WeekUtil.getCurrentWeekKey(weekStartMonday.value)
    }

    fun selectWeek(weekKey: String) {
        selectedWeekKey.value = weekKey
    }

    // Categories
    val allCategories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<CategoryEntity>> = allCategories
        .combine(MutableStateFlow(Unit)) { cats, _ -> cats.filter { it.type == "INCOME" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories: StateFlow<List<CategoryEntity>> = allCategories
        .combine(MutableStateFlow(Unit)) { cats, _ -> cats.filter { it.type == "EXPENSE" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions for selected week
    val weekTransactions: StateFlow<List<TransactionEntity>> = selectedWeekKey
        .flatMapLatest { weekKey -> repository.getTransactionsForWeek(weekKey) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weekIncomeTransactions: StateFlow<List<TransactionEntity>> = weekTransactions
        .combine(MutableStateFlow(Unit)) { txs, _ -> txs.filter { it.type == "INCOME" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weekExpenseTransactions: StateFlow<List<TransactionEntity>> = weekTransactions
        .combine(MutableStateFlow(Unit)) { txs, _ -> txs.filter { it.type == "EXPENSE" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category Budgets for selected week
    val weekBudgets: StateFlow<List<CategoryBudgetEntity>> = selectedWeekKey
        .flatMapLatest { weekKey -> repository.getBudgetsForWeek(weekKey) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Checklist for selected week
    val weekChecklist: StateFlow<List<ChecklistItemEntity>> = selectedWeekKey
        .flatMapLatest { weekKey -> repository.getChecklistForWeek(weekKey) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Summary
    val financialSummary: StateFlow<FinancialSummaryUiModel> = combine(
        weekTransactions,
        weekBudgets
    ) { txs, budgets ->
        val incomeList = txs.filter { it.type == "INCOME" }
        val expenseList = txs.filter { it.type == "EXPENSE" }

        val moneyIn = incomeList.sumOf { it.amount }
        val moneyOut = expenseList.sumOf { it.amount }
        val remaining = moneyIn - moneyOut
        val totalBudget = budgets.sumOf { it.budgetAmount }
        val budgetUsedPercent = if (totalBudget > 0) (moneyOut / totalBudget * 100) else 0.0

        FinancialSummaryUiModel(
            moneyIn = moneyIn,
            moneyOut = moneyOut,
            remaining = remaining,
            totalBudget = totalBudget,
            budgetUsedPercent = budgetUsedPercent.coerceAtLeast(0.0),
            savingsSurplus = remaining,
            transactionCount = txs.size,
            incomeCount = incomeList.size,
            expenseCount = expenseList.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummaryUiModel())

    // Category Budgets with Spending
    val categoryBudgetsWithSpending: StateFlow<List<CategoryBudgetUiModel>> = combine(
        weekBudgets,
        weekExpenseTransactions,
        allCategories
    ) { budgets, expenses, categories ->
        val catColorMap = categories.associate { it.name to Pair(it.colorHex, it.iconName) }
        val spendingMap = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        budgets.map { budget ->
            val spent = spendingMap[budget.categoryName] ?: 0.0
            val remaining = budget.budgetAmount - spent
            val percentageUsed = if (budget.budgetAmount > 0) (spent / budget.budgetAmount * 100) else 0.0
            val isOverBudget = spent > budget.budgetAmount
            val isApproaching = !isOverBudget && percentageUsed >= 80.0
            val overAmount = if (isOverBudget) (spent - budget.budgetAmount) else 0.0
            val overPercent = if (isOverBudget && budget.budgetAmount > 0) ((spent - budget.budgetAmount) / budget.budgetAmount * 100) else 0.0

            val catMeta = catColorMap[budget.categoryName] ?: Pair(0xFF3B82F6, "category")

            CategoryBudgetUiModel(
                id = budget.id,
                categoryName = budget.categoryName,
                budgetAmount = budget.budgetAmount,
                spentAmount = spent,
                remainingAmount = remaining,
                percentageUsed = percentageUsed,
                isApproachingLimit = isApproaching,
                isOverBudget = isOverBudget,
                overBudgetAmount = overAmount,
                overBudgetPercentage = overPercent,
                colorHex = catMeta.first,
                iconName = catMeta.second
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Top spending categories for this week
    val topSpendingCategories: StateFlow<List<CategorySpendingUiModel>> = combine(
        weekExpenseTransactions,
        allCategories
    ) { expenses, categories ->
        val totalExpense = expenses.sumOf { it.amount }
        val catMap = categories.associate { it.name to Pair(it.colorHex, it.iconName) }

        expenses.groupBy { it.category }
            .map { (catName, txList) ->
                val spent = txList.sumOf { it.amount }
                val percent = if (totalExpense > 0) (spent / totalExpense * 100) else 0.0
                val meta = catMap[catName] ?: Pair(0xFF6B7280, "category")
                CategorySpendingUiModel(
                    categoryName = catName,
                    amount = spent,
                    percentageOfTotal = percent,
                    colorHex = meta.first,
                    iconName = meta.second
                )
            }
            .sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and filter state
    val filterState = MutableStateFlow(FilterState())

    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        weekTransactions,
        filterState
    ) { txs, filter ->
        txs.filter { tx ->
            val matchesType = when (filter.typeFilter) {
                "INCOME" -> tx.type == "INCOME"
                "EXPENSE" -> tx.type == "EXPENSE"
                else -> true
            }
            val matchesCat = filter.categoryFilter == "ALL" || tx.category.equals(filter.categoryFilter, ignoreCase = true)
            val matchesDate = filter.dateFilter.isBlank() || tx.date == filter.dateFilter
            val matchesQuery = filter.searchQuery.isBlank() ||
                    tx.description.contains(filter.searchQuery, ignoreCase = true) ||
                    tx.note.contains(filter.searchQuery, ignoreCase = true) ||
                    tx.category.contains(filter.searchQuery, ignoreCase = true)

            matchesType && matchesCat && matchesDate && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weekly history overview
    val weeklyHistory: StateFlow<List<WeeklyHistoryItemUiModel>> = combine(
        allTransactions,
        selectedWeekKey,
        weekStartMonday
    ) { allTxs, currentSelectedWeek, startsMonday ->
        val currentWeek = WeekUtil.getCurrentWeekKey(startsMonday)
        val allWeeks = (allTxs.map { it.weekKey } + listOf(currentWeek, currentSelectedWeek)).distinct()

        allWeeks.map { weekKey ->
            val txsInWeek = allTxs.filter { it.weekKey == weekKey }
            val inAmount = txsInWeek.filter { it.type == "INCOME" }.sumOf { it.amount }
            val outAmount = txsInWeek.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            WeeklyHistoryItemUiModel(
                weekKey = weekKey,
                dateRange = WeekUtil.getWeekRangeDisplay(weekKey, startsMonday),
                moneyIn = inAmount,
                moneyOut = outAmount,
                netRemaining = inAmount - outAmount,
                totalBudget = 0.0,
                transactionCount = txsInWeek.size,
                isCurrentWeek = weekKey == currentWeek
            )
        }.sortedByDescending { it.weekKey }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transaction Actions
    fun addTransaction(
        type: String,
        description: String,
        amount: Double,
        category: String,
        date: String,
        note: String
    ) {
        viewModelScope.launch {
            val weekKey = WeekUtil.getWeekKeyForDate(date, weekStartMonday.value)
            repository.insertTransaction(
                TransactionEntity(
                    weekKey = weekKey,
                    type = type,
                    description = description.trim(),
                    amount = amount,
                    category = category.trim(),
                    date = date,
                    note = note.trim()
                )
            )
        }
    }

    fun updateTransaction(
        id: Int,
        type: String,
        description: String,
        amount: Double,
        category: String,
        date: String,
        note: String
    ) {
        viewModelScope.launch {
            val weekKey = WeekUtil.getWeekKeyForDate(date, weekStartMonday.value)
            repository.updateTransaction(
                TransactionEntity(
                    id = id,
                    weekKey = weekKey,
                    type = type,
                    description = description.trim(),
                    amount = amount,
                    category = category.trim(),
                    date = date,
                    note = note.trim()
                )
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Category Actions
    fun addCategory(name: String, type: String, iconName: String = "category", colorHex: Long = 0xFF10B981) {
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name.trim(),
                    type = type,
                    iconName = iconName,
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateCategory(category: CategoryEntity, newName: String) {
        viewModelScope.launch {
            val trimmedName = newName.trim()
            if (trimmedName != category.name) {
                repository.reassignCategory(category.name, trimmedName)
                repository.updateCategory(category.copy(name = trimmedName))
            }
        }
    }

    fun checkCategoryUsage(categoryName: String, onResult: (count: Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.getTransactionCountForCategory(categoryName)
            onResult(count)
        }
    }

    fun deleteCategory(
        category: CategoryEntity,
        reassignTo: String? = null,
        deleteTransactions: Boolean = false
    ) {
        viewModelScope.launch {
            if (deleteTransactions) {
                repository.deleteTransactionsByCategory(category.name)
            } else if (!reassignTo.isNullOrBlank()) {
                repository.reassignCategory(category.name, reassignTo)
            }
            repository.deleteCategory(category)
        }
    }

    // Budget Actions
    fun setCategoryBudget(categoryName: String, amount: Double) {
        viewModelScope.launch {
            repository.insertBudget(
                CategoryBudgetEntity(
                    weekKey = selectedWeekKey.value,
                    categoryName = categoryName,
                    budgetAmount = amount
                )
            )
        }
    }

    fun deleteCategoryBudget(budget: CategoryBudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    fun copyLastWeekBudget(onCopied: (count: Int) -> Unit) {
        viewModelScope.launch {
            val prevWeek = WeekUtil.getPreviousWeekKey(selectedWeekKey.value, weekStartMonday.value)
            val count = repository.copyBudgetsFromWeek(prevWeek, selectedWeekKey.value)
            onCopied(count)
        }
    }

    // Checklist Actions
    fun addChecklistItem(title: String, dueDate: String, priority: String) {
        viewModelScope.launch {
            repository.insertChecklistItem(
                ChecklistItemEntity(
                    weekKey = selectedWeekKey.value,
                    title = title.trim(),
                    dueDate = dueDate.trim(),
                    priority = priority
                )
            )
        }
    }

    fun toggleChecklistItem(item: ChecklistItemEntity) {
        viewModelScope.launch {
            repository.updateChecklistItem(item.copy(isCompleted = !item.isCompleted))
        }
    }

    fun updateChecklistItem(item: ChecklistItemEntity, title: String, dueDate: String, priority: String) {
        viewModelScope.launch {
            repository.updateChecklistItem(
                item.copy(
                    title = title.trim(),
                    dueDate = dueDate.trim(),
                    priority = priority
                )
            )
        }
    }

    fun deleteChecklistItem(item: ChecklistItemEntity) {
        viewModelScope.launch {
            repository.deleteChecklistItem(item)
        }
    }

    fun copyLastWeekChecklist(onCopied: (count: Int) -> Unit) {
        viewModelScope.launch {
            val prevWeek = WeekUtil.getPreviousWeekKey(selectedWeekKey.value, weekStartMonday.value)
            val count = repository.copyChecklistFromWeek(prevWeek, selectedWeekKey.value)
            onCopied(count)
        }
    }

    // Settings Actions
    fun updateCurrency(symbol: String) {
        viewModelScope.launch {
            repository.setSetting("currency", symbol)
        }
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            repository.setSetting("theme", theme)
        }
    }

    fun updateWeekStart(startDay: String) {
        viewModelScope.launch {
            repository.setSetting("weekStart", startDay)
            val isMonday = startDay == "MONDAY"
            selectedWeekKey.value = WeekUtil.getCurrentWeekKey(isMonday)
        }
    }

    // Export & Backup
    fun exportBackupJson(callback: (String) -> Unit) {
        viewModelScope.launch {
            val backupData = repository.getBackupData()
            val json = BackupUtil.exportToJson(backupData)
            callback(json)
        }
    }

    fun importBackupJson(jsonString: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val data = BackupUtil.importFromJson(jsonString)
                repository.restoreBackupData(data)
                onComplete(true, "Data imported successfully!")
            } catch (e: Exception) {
                onComplete(false, "Failed to import: ${e.message}")
            }
        }
    }

    fun exportTransactionsCsv(onlyCurrentWeek: Boolean, callback: (String) -> Unit) {
        viewModelScope.launch {
            val txs = if (onlyCurrentWeek) weekTransactions.value else allTransactions.value
            val csv = BackupUtil.exportTransactionsToCsv(txs)
            callback(csv)
        }
    }

    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.resetAllData()
            selectedWeekKey.value = WeekUtil.getCurrentWeekKey(true)
            onComplete()
        }
    }
}
