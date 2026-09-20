package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CategoryBudgetEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.WeekSelectorBar
import com.example.ui.model.CategoryBudgetUiModel
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.ExceededRose
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.BudgetViewModel
import com.example.util.CurrencyUtil
import com.example.util.WeekUtil
import kotlinx.coroutines.launch

@Composable
fun BudgetsScreen(
    viewModel: BudgetViewModel,
    onSetBudget: (initialCategory: String?, initialAmount: Double?) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val selectedWeekKey by viewModel.selectedWeekKey.collectAsStateWithLifecycle()
    val isWeekMonday by viewModel.weekStartMonday.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val budgets by viewModel.categoryBudgetsWithSpending.collectAsStateWithLifecycle()
    val rawBudgets by viewModel.weekBudgets.collectAsStateWithLifecycle()

    val dateRange = WeekUtil.getWeekRangeDisplay(selectedWeekKey, isWeekMonday)
    val isCurrent = viewModel.isCurrentWeek(selectedWeekKey)
    val coroutineScope = rememberCoroutineScope()

    val totalBudget = budgets.sumOf { it.budgetAmount }
    val totalSpent = budgets.sumOf { it.spentAmount }
    val totalRemaining = totalBudget - totalSpent
    val overallPercent = if (totalBudget > 0) (totalSpent / totalBudget * 100) else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("budgets_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Week selector
        item {
            WeekSelectorBar(
                weekDateRange = dateRange,
                isCurrentWeek = isCurrent,
                onPreviousClick = { viewModel.goToPreviousWeek() },
                onNextClick = { viewModel.goToNextWeek() },
                onCurrentWeekClick = { viewModel.goToCurrentWeek() }
            )
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL WEEKLY BUDGET",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyUtil.format(totalBudget, currencySymbol),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = { onSetBudget(null, null) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("set_new_budget_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Set Budget", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (totalBudget > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Spent: ${CurrencyUtil.format(totalSpent, currencySymbol)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Remaining: ${CurrencyUtil.format(totalRemaining, currencySymbol)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (totalRemaining >= 0) Emerald600 else ExceededRose
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val progressFraction = (overallPercent / 100.0).toFloat().coerceIn(0f, 1f)
                        val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "total_budget_prog")

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when {
                                overallPercent > 100 -> ExceededRose
                                overallPercent >= 80 -> WarningAmber
                                else -> Emerald500
                            },
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Copy from last week action
                    OutlinedButton(
                        onClick = {
                            viewModel.copyLastWeekBudget { count ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (count > 0) "Copied $count budget(s) from last week!" else "No budgets found in previous week."
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("copy_last_week_budget_button")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Last Week's Budgets")
                    }
                }
            }
        }

        // Budget Items or Empty State
        if (budgets.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.PieChart,
                    title = "No Category Budgets Set",
                    description = "Setting weekly limits for Food, Transport, and other categories keeps you in control.",
                    actionButtonText = "+ Set Category Budget",
                    onActionClick = { onSetBudget(null, null) }
                )
            }
        } else {
            items(budgets, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_item_${item.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isOverBudget) {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                CategoryIconBadge(iconName = item.iconName, colorHex = item.colorHex, size = 40)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.categoryName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Limit: ${CurrencyUtil.format(item.budgetAmount, currencySymbol)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = { onSetBudget(item.categoryName, item.budgetAmount) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Budget",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val rawBudget = rawBudgets.firstOrNull { it.id == item.id }
                                        if (rawBudget != null) {
                                            viewModel.deleteCategoryBudget(rawBudget)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Budget",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar
                        val progressFraction = (item.percentageUsed / 100.0).toFloat().coerceIn(0f, 1f)
                        val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "cat_prog")

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when {
                                item.isOverBudget -> ExceededRose
                                item.isApproachingLimit -> WarningAmber
                                else -> Emerald500
                            },
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Spent: ${CurrencyUtil.format(item.spentAmount, currencySymbol)} (${String.format(java.util.Locale.US, "%.0f%%", item.percentageUsed)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (item.isOverBudget) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ExceededRose,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Over by ${CurrencyUtil.format(item.overBudgetAmount, currencySymbol)} (+${String.format(java.util.Locale.US, "%.0f%%", item.overBudgetPercentage)})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ExceededRose
                                    )
                                }
                            } else {
                                Text(
                                    text = "${CurrencyUtil.format(item.remainingAmount, currencySymbol)} left",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isApproachingLimit) WarningAmber else Emerald600
                                )
                            }
                        }

                        if (item.isApproachingLimit && !item.isOverBudget) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⚠ Approaching spending limit (≥80% used)",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningAmber,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
