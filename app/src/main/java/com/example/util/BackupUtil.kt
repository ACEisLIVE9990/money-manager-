package com.example.util

import com.example.data.model.AppSettingsEntity
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ChecklistItemEntity
import com.example.data.model.TransactionEntity
import org.json.JSONArray
import org.json.JSONObject

data class AppBackupData(
    val categories: List<CategoryEntity>,
    val transactions: List<TransactionEntity>,
    val budgets: List<CategoryBudgetEntity>,
    val checklistItems: List<ChecklistItemEntity>,
    val settings: List<AppSettingsEntity>
)

object BackupUtil {

    fun exportToJson(data: AppBackupData): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("appName", "Weekly Budget Planner")

        // Categories
        val catsArray = JSONArray()
        data.categories.forEach { cat ->
            val obj = JSONObject()
            obj.put("name", cat.name)
            obj.put("type", cat.type)
            obj.put("iconName", cat.iconName)
            obj.put("colorHex", cat.colorHex)
            obj.put("isDefault", cat.isDefault)
            catsArray.put(obj)
        }
        root.put("categories", catsArray)

        // Transactions
        val txsArray = JSONArray()
        data.transactions.forEach { tx ->
            val obj = JSONObject()
            obj.put("weekKey", tx.weekKey)
            obj.put("type", tx.type)
            obj.put("description", tx.description)
            obj.put("amount", tx.amount)
            obj.put("category", tx.category)
            obj.put("date", tx.date)
            obj.put("note", tx.note)
            obj.put("timestamp", tx.timestamp)
            txsArray.put(obj)
        }
        root.put("transactions", txsArray)

        // Budgets
        val budgetsArray = JSONArray()
        data.budgets.forEach { b ->
            val obj = JSONObject()
            obj.put("weekKey", b.weekKey)
            obj.put("categoryName", b.categoryName)
            obj.put("budgetAmount", b.budgetAmount)
            budgetsArray.put(obj)
        }
        root.put("budgets", budgetsArray)

        // Checklist
        val checkArray = JSONArray()
        data.checklistItems.forEach { item ->
            val obj = JSONObject()
            obj.put("weekKey", item.weekKey)
            obj.put("title", item.title)
            obj.put("isCompleted", item.isCompleted)
            obj.put("dueDate", item.dueDate)
            obj.put("priority", item.priority)
            obj.put("timestamp", item.timestamp)
            checkArray.put(obj)
        }
        root.put("checklist", checkArray)

        // Settings
        val settingsArray = JSONArray()
        data.settings.forEach { s ->
            val obj = JSONObject()
            obj.put("key", s.key)
            obj.put("value", s.value)
            settingsArray.put(obj)
        }
        root.put("settings", settingsArray)

        return root.toString(2)
    }

    fun importFromJson(jsonStr: String): AppBackupData {
        val root = JSONObject(jsonStr)

        val categories = mutableListOf<CategoryEntity>()
        if (root.has("categories")) {
            val catsArray = root.getJSONArray("categories")
            for (i in 0 until catsArray.length()) {
                val obj = catsArray.getJSONObject(i)
                categories.add(
                    CategoryEntity(
                        name = obj.getString("name"),
                        type = obj.getString("type"),
                        iconName = obj.optString("iconName", "category"),
                        colorHex = obj.optLong("colorHex", 0xFF10B981),
                        isDefault = obj.optBoolean("isDefault", false)
                    )
                )
            }
        }

        val transactions = mutableListOf<TransactionEntity>()
        if (root.has("transactions")) {
            val txsArray = root.getJSONArray("transactions")
            for (i in 0 until txsArray.length()) {
                val obj = txsArray.getJSONObject(i)
                transactions.add(
                    TransactionEntity(
                        weekKey = obj.getString("weekKey"),
                        type = obj.getString("type"),
                        description = obj.getString("description"),
                        amount = obj.getDouble("amount"),
                        category = obj.getString("category"),
                        date = obj.getString("date"),
                        note = obj.optString("note", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        val budgets = mutableListOf<CategoryBudgetEntity>()
        if (root.has("budgets")) {
            val budgetsArray = root.getJSONArray("budgets")
            for (i in 0 until budgetsArray.length()) {
                val obj = budgetsArray.getJSONObject(i)
                budgets.add(
                    CategoryBudgetEntity(
                        weekKey = obj.getString("weekKey"),
                        categoryName = obj.getString("categoryName"),
                        budgetAmount = obj.getDouble("budgetAmount")
                    )
                )
            }
        }

        val checklistItems = mutableListOf<ChecklistItemEntity>()
        if (root.has("checklist")) {
            val checkArray = root.getJSONArray("checklist")
            for (i in 0 until checkArray.length()) {
                val obj = checkArray.getJSONObject(i)
                checklistItems.add(
                    ChecklistItemEntity(
                        weekKey = obj.getString("weekKey"),
                        title = obj.getString("title"),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        dueDate = obj.optString("dueDate", ""),
                        priority = obj.optString("priority", "MEDIUM"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        val settings = mutableListOf<AppSettingsEntity>()
        if (root.has("settings")) {
            val settingsArray = root.getJSONArray("settings")
            for (i in 0 until settingsArray.length()) {
                val obj = settingsArray.getJSONObject(i)
                settings.add(
                    AppSettingsEntity(
                        key = obj.getString("key"),
                        value = obj.getString("value")
                    )
                )
            }
        }

        return AppBackupData(categories, transactions, budgets, checklistItems, settings)
    }

    fun exportTransactionsToCsv(transactions: List<TransactionEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Week,Type,Description,Amount,Category,Date,Note\n")
        transactions.forEach { tx ->
            val cleanDesc = tx.description.replace("\"", "\"\"")
            val cleanNote = tx.note.replace("\"", "\"\"")
            val cleanCat = tx.category.replace("\"", "\"\"")
            sb.append("${tx.id},")
            sb.append("\"${tx.weekKey}\",")
            sb.append("\"${tx.type}\",")
            sb.append("\"$cleanDesc\",")
            sb.append("${tx.amount},")
            sb.append("\"$cleanCat\",")
            sb.append("\"${tx.date}\",")
            sb.append("\"$cleanNote\"\n")
        }
        return sb.toString()
    }
}
