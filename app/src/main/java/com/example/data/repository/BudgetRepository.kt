package com.example.data.repository

import com.example.data.local.BudgetDatabase
import com.example.data.local.populateInitialData
import com.example.data.model.AppSettingsEntity
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.TransactionEntity
import com.example.util.AppBackupData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BudgetRepository(private val database: BudgetDatabase) {
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val budgetDao = database.categoryBudgetDao()
    private val checklistDao = database.checklistDao()
    private val settingsDao = database.appSettingsDao()

    // Transactions
    fun getTransactionsForWeek(weekKey: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForWeek(weekKey)

    fun getAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    suspend fun insertTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun getTransactionCountForCategory(categoryName: String): Int = withContext(Dispatchers.IO) {
        transactionDao.getTransactionCountForCategory(categoryName)
    }

    suspend fun reassignCategory(oldCategory: String, newCategory: String) = withContext(Dispatchers.IO) {
        transactionDao.reassignCategory(oldCategory, newCategory)
    }

    suspend fun deleteTransactionsByCategory(categoryName: String) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransactionsByCategory(categoryName)
    }

    // Categories
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategory(category)
        budgetDao.deleteBudgetsForCategory(category.name)
    }

    // Category Budgets
    fun getBudgetsForWeek(weekKey: String): Flow<List<CategoryBudgetEntity>> =
        budgetDao.getBudgetsForWeek(weekKey)

    suspend fun insertBudget(budget: CategoryBudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: CategoryBudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: CategoryBudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun copyBudgetsFromWeek(fromWeek: String, toWeek: String): Int = withContext(Dispatchers.IO) {
        val previousBudgets = budgetDao.getAllBudgetsSync().filter { it.weekKey == fromWeek }
        val newBudgets = previousBudgets.map {
            CategoryBudgetEntity(
                weekKey = toWeek,
                categoryName = it.categoryName,
                budgetAmount = it.budgetAmount
            )
        }
        if (newBudgets.isNotEmpty()) {
            budgetDao.insertBudgets(newBudgets)
        }
        newBudgets.size
    }

    // Checklist
    fun getChecklistForWeek(weekKey: String): Flow<List<ChecklistItemEntity>> =
        checklistDao.getChecklistForWeek(weekKey)

    suspend fun insertChecklistItem(item: ChecklistItemEntity) = withContext(Dispatchers.IO) {
        checklistDao.insertChecklistItem(item)
    }

    suspend fun updateChecklistItem(item: ChecklistItemEntity) = withContext(Dispatchers.IO) {
        checklistDao.updateChecklistItem(item)
    }

    suspend fun deleteChecklistItem(item: ChecklistItemEntity) = withContext(Dispatchers.IO) {
        checklistDao.deleteChecklistItem(item)
    }

    suspend fun copyChecklistFromWeek(fromWeek: String, toWeek: String): Int = withContext(Dispatchers.IO) {
        val previousItems = checklistDao.getAllChecklistItemsSync().filter { it.weekKey == fromWeek }
        val newItems = previousItems.map {
            ChecklistItemEntity(
                weekKey = toWeek,
                title = it.title,
                isCompleted = false,
                dueDate = "",
                priority = it.priority
            )
        }
        if (newItems.isNotEmpty()) {
            checklistDao.insertChecklistItems(newItems)
        }
        newItems.size
    }

    // Settings
    fun getAllSettings(): Flow<List<AppSettingsEntity>> = settingsDao.getAllSettings()

    suspend fun setSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        settingsDao.setSetting(AppSettingsEntity(key, value))
    }

    // Backup & Restore
    suspend fun getBackupData(): AppBackupData = withContext(Dispatchers.IO) {
        AppBackupData(
            categories = categoryDao.getAllCategoriesSync(),
            transactions = transactionDao.getAllTransactionsSync(),
            budgets = budgetDao.getAllBudgetsSync(),
            checklistItems = checklistDao.getAllChecklistItemsSync(),
            settings = settingsDao.getAllSettingsSync()
        )
    }

    suspend fun restoreBackupData(data: AppBackupData) = withContext(Dispatchers.IO) {
        if (data.categories.isNotEmpty()) {
            categoryDao.insertCategories(data.categories)
        }
        if (data.transactions.isNotEmpty()) {
            transactionDao.insertTransactions(data.transactions)
        }
        if (data.budgets.isNotEmpty()) {
            budgetDao.insertBudgets(data.budgets)
        }
        if (data.checklistItems.isNotEmpty()) {
            checklistDao.insertChecklistItems(data.checklistItems)
        }
        data.settings.forEach {
            settingsDao.setSetting(it)
        }
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        transactionDao.deleteAllTransactions()
        budgetDao.deleteAllBudgets()
        checklistDao.deleteAllChecklistItems()
        categoryDao.deleteAllCategories()
        settingsDao.deleteAllSettings()
        populateInitialData(database)
    }
}
