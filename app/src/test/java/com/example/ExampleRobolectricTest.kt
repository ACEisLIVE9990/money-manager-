package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CategoryEntity
import com.example.util.AppBackupData
import com.example.util.BackupUtil
import com.example.util.CurrencyUtil
import com.example.util.WeekUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Weekly Budget", appName)
    }

    @Test
    fun `currency formatting works correctly`() {
        assertEquals("₹ 1,500", CurrencyUtil.format(1500.0, "₹"))
        assertEquals("$ 250.50", CurrencyUtil.format(250.50, "$"))
        assertEquals(500.0, CurrencyUtil.parseAmount("500"))
        assertEquals(1250.75, CurrencyUtil.parseAmount("1,250.75"))
    }

    @Test
    fun `week utility produces valid week ranges`() {
        val weekKey = "2026-W38"
        val rangeMon = WeekUtil.getWeekRangeDisplay(weekKey, weekStartsOnMonday = true)
        assertNotNull(rangeMon)
        assertTrue(rangeMon.contains("–"))

        val nextWeek = WeekUtil.getNextWeekKey(weekKey, weekStartsOnMonday = true)
        assertEquals("2026-W39", nextWeek)

        val prevWeek = WeekUtil.getPreviousWeekKey(weekKey, weekStartsOnMonday = true)
        assertEquals("2026-W37", prevWeek)
    }

    @Test
    fun `backup and restore json serialization`() {
        val backupData = AppBackupData(
            categories = listOf(
                CategoryEntity(name = "Food", type = "EXPENSE", iconName = "restaurant", colorHex = 0xFFEF4444, isDefault = true)
            ),
            transactions = emptyList(),
            budgets = emptyList(),
            checklistItems = emptyList(),
            settings = emptyList()
        )
        val json = BackupUtil.exportToJson(backupData)
        assertTrue(json.contains("Weekly Budget Planner"))
        assertTrue(json.contains("Food"))

        val restored = BackupUtil.importFromJson(json)
        assertEquals(1, restored.categories.size)
        assertEquals("Food", restored.categories[0].name)
    }
}
