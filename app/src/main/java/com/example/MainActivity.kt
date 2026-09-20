package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.WeeklyBudgetApp
import com.example.ui.theme.WeeklyBudgetTheme
import com.example.ui.viewmodel.BudgetViewModel

class MainActivity : ComponentActivity() {
    private val budgetViewModel: BudgetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by budgetViewModel.themeMode.collectAsStateWithLifecycle()
            WeeklyBudgetTheme(themeMode = themeMode) {
                WeeklyBudgetApp(
                    viewModel = budgetViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

