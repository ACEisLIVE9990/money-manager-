package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.components.FinancialSummaryCard
import com.example.ui.model.FinancialSummaryUiModel
import com.example.ui.theme.WeeklyBudgetTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun greeting_screenshot() {
        composeTestRule.setContent {
            WeeklyBudgetTheme(themeMode = "LIGHT") {
                FinancialSummaryCard(
                    summary = FinancialSummaryUiModel(
                        moneyIn = 25000.0,
                        moneyOut = 8500.0,
                        remaining = 16500.0,
                        totalBudget = 10000.0,
                        budgetUsedPercent = 85.0
                    ),
                    currencySymbol = "₹"
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
