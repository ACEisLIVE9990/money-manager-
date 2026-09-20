package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppSettingsEntity
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        CategoryBudgetEntity::class,
        ChecklistItemEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BudgetDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: BudgetDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BudgetDatabase::class.java,
                    "weekly_budget_planner.db"
                )
                    .addCallback(BudgetDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val DEFAULT_INCOME_CATEGORIES = listOf(
            CategoryEntity(name = "Salary", type = "INCOME", iconName = "work", colorHex = 0xFF10B981, isDefault = true),
            CategoryEntity(name = "Freelance", type = "INCOME", iconName = "laptop", colorHex = 0xFF059669, isDefault = true),
            CategoryEntity(name = "Allowance", type = "INCOME", iconName = "account_balance_wallet", colorHex = 0xFF14B8A6, isDefault = true),
            CategoryEntity(name = "Gift", type = "INCOME", iconName = "card_giftcard", colorHex = 0xFF06B6D4, isDefault = true),
            CategoryEntity(name = "Refund", type = "INCOME", iconName = "replay", colorHex = 0xFF3B82F6, isDefault = true),
            CategoryEntity(name = "Other", type = "INCOME", iconName = "more_horiz", colorHex = 0xFF6B7280, isDefault = true)
        )

        val DEFAULT_EXPENSE_CATEGORIES = listOf(
            CategoryEntity(name = "Food", type = "EXPENSE", iconName = "restaurant", colorHex = 0xFFF97316, isDefault = true),
            CategoryEntity(name = "Transport", type = "EXPENSE", iconName = "directions_car", colorHex = 0xFF3B82F6, isDefault = true),
            CategoryEntity(name = "Bills", type = "EXPENSE", iconName = "receipt_long", colorHex = 0xFFEF4444, isDefault = true),
            CategoryEntity(name = "Shopping", type = "EXPENSE", iconName = "shopping_bag", colorHex = 0xFFEC4899, isDefault = true),
            CategoryEntity(name = "Entertainment", type = "EXPENSE", iconName = "movie", colorHex = 0xFF8B5CF6, isDefault = true),
            CategoryEntity(name = "Education", type = "EXPENSE", iconName = "school", colorHex = 0xFF6366F1, isDefault = true),
            CategoryEntity(name = "Health", type = "EXPENSE", iconName = "favorite", colorHex = 0xFF10B981, isDefault = true),
            CategoryEntity(name = "Savings", type = "EXPENSE", iconName = "savings", colorHex = 0xFFEAB308, isDefault = true),
            CategoryEntity(name = "Other", type = "EXPENSE", iconName = "more_horiz", colorHex = 0xFF6B7280, isDefault = true)
        )
    }

    private class BudgetDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }
    }
}

suspend fun populateInitialData(database: BudgetDatabase) {
    val categoryDao = database.categoryDao()
    if (categoryDao.getAllCategoriesSync().isEmpty()) {
        categoryDao.insertCategories(BudgetDatabase.DEFAULT_INCOME_CATEGORIES)
        categoryDao.insertCategories(BudgetDatabase.DEFAULT_EXPENSE_CATEGORIES)
    }
    val settingsDao = database.appSettingsDao()
    if (settingsDao.getAllSettingsSync().isEmpty()) {
        settingsDao.setSetting(AppSettingsEntity("currency", "₹"))
        settingsDao.setSetting(AppSettingsEntity("theme", "SYSTEM"))
        settingsDao.setSetting(AppSettingsEntity("weekStart", "MONDAY"))
    }
}
