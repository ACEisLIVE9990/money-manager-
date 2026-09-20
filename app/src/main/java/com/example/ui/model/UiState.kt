package com.example.ui.model

import com.example.data.model.CategoryEntity
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.TransactionEntity

enum class NavDestination(val label: String) {
    DASHBOARD("Dashboard"),
    MONEY_IN("Money In"),
    MONEY_OUT("Money Out"),
    BUDGETS("Budgets"),
    CHECKLIST("Checklist"),
    CATEGORIES("Categories"),
    HISTORY("History"),
    SETTINGS("Settings")
}

data class FinancialSummaryUiModel(
    val moneyIn: Double = 0.0,
    val moneyOut: Double = 0.0,
    val remaining: Double = 0.0,
    val totalBudget: Double = 0.0,
    val budgetUsedPercent: Double = 0.0,
    val savingsSurplus: Double = 0.0,
    val transactionCount: Int = 0,
    val incomeCount: Int = 0,
    val expenseCount: Int = 0
)

data class CategoryBudgetUiModel(
    val id: Int,
    val categoryName: String,
    val budgetAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val percentageUsed: Double,
    val isApproachingLimit: Boolean,
    val isOverBudget: Boolean,
    val overBudgetAmount: Double,
    val overBudgetPercentage: Double,
    val colorHex: Long,
    val iconName: String
)

data class CategorySpendingUiModel(
    val categoryName: String,
    val amount: Double,
    val percentageOfTotal: Double,
    val colorHex: Long,
    val iconName: String
)

data class WeeklyHistoryItemUiModel(
    val weekKey: String,
    val dateRange: String,
    val moneyIn: Double,
    val moneyOut: Double,
    val netRemaining: Double,
    val totalBudget: Double,
    val transactionCount: Int,
    val isCurrentWeek: Boolean
)

data class FilterState(
    val searchQuery: String = "",
    val typeFilter: String = "ALL", // ALL, INCOME, EXPENSE
    val categoryFilter: String = "ALL",
    val dateFilter: String = ""
)
