package com.barakwebsoft.shopzen.data.local

import com.barakwebsoft.shopzen.data.local.dao.CategoryDao
import com.barakwebsoft.shopzen.data.local.dao.HistoryDao
import com.barakwebsoft.shopzen.data.local.dao.ShoppingItemDao
import com.barakwebsoft.shopzen.data.local.dao.ShoppingListDao
import com.barakwebsoft.shopzen.data.local.database.ShopZenDatabase
import kotlinx.coroutines.flow.Flow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.lang.reflect.Modifier

class RoomDaoStructureTest {

    private fun resolveSourceFile(relativePath: String): File {
        return listOf(
            File(relativePath),
            File("app", relativePath)
        ).firstOrNull { it.exists() } ?: error("Source file not found: $relativePath")
    }

    @Test
    fun shoppingListDao_declaresRequiredObservableQueriesAndGeneratedImplementation() {
        assertTrue("ShoppingListDao must be an interface", ShoppingListDao::class.java.isInterface)

        val flowMethods = listOf(
            "getAllLists",
            "getListsByStatus",
            "getActiveLists",
            "getCompletedLists",
            "getArchivedLists",
            "observeListById",
            "observeListWithItems",
            "searchLists",
            "observeActiveListCount",
            "observeTotalListCount"
        )

        for (methodName in flowMethods) {
            val method = ShoppingListDao::class.java.methods.find { it.name == methodName }
            assertNotNull("ShoppingListDao must declare $methodName", method)
            assertTrue(
                "$methodName must return Flow",
                Flow::class.java.isAssignableFrom(method!!.returnType)
            )
        }

        val mutationAndLookupMethods = listOf(
            "getListById",
            "getListWithItemsById",
            "getActiveListCount",
            "getTotalListCount",
            "insertList",
            "insertLists",
            "updateList",
            "updateListStatus",
            "updateListCompletion",
            "deleteList",
            "deleteListById"
        )
        for (methodName in mutationAndLookupMethods) {
            val method = ShoppingListDao::class.java.methods.find { it.name == methodName }
            assertNotNull("ShoppingListDao must declare $methodName", method)
        }

        // Verify Room KSP compiler generated implementation
        val implClass = Class.forName("com.barakwebsoft.shopzen.data.local.dao.ShoppingListDao_Impl")
        assertTrue("ShoppingListDao_Impl must implement ShoppingListDao", ShoppingListDao::class.java.isAssignableFrom(implClass))

        // Verify source file contains @Dao and query annotations
        val daoSource = resolveSourceFile("src/main/java/com/barakwebsoft/shopzen/data/local/dao/ShoppingListDao.kt").readText()
        assertTrue("Must be annotated with @Dao", daoSource.contains("@Dao"))
        assertTrue("observeListWithItems must have @Transaction", daoSource.contains("@Transaction\n    @Query(\"SELECT * FROM shopping_lists WHERE id = :id\")\n    fun observeListWithItems"))
        assertTrue("Must have status query", daoSource.contains("WHERE status = :status"))
        assertTrue("Must have active query", daoSource.contains("WHERE status = 'ACTIVE'"))
        assertTrue("Must have updateListStatus query", daoSource.contains("UPDATE shopping_lists SET status = :status"))
    }

    @Test
    fun shoppingItemDao_declaresRequiredObservableQueriesAndGeneratedImplementation() {
        assertTrue("ShoppingItemDao must be an interface", ShoppingItemDao::class.java.isInterface)

        val flowMethods = listOf(
            "getItemsForList",
            "getItemsWithCategoryForList",
            "getPurchasedItemsForList",
            "getPendingItemsForList",
            "observeItemById",
            "observeItemWithCategory",
            "observeItemCount",
            "observePurchasedItemCount",
            "searchItemsInList",
            "searchItemsWithCategoryInList"
        )

        for (methodName in flowMethods) {
            val method = ShoppingItemDao::class.java.methods.find { it.name == methodName }
            assertNotNull("ShoppingItemDao must declare $methodName", method)
            assertTrue(
                "$methodName must return Flow",
                Flow::class.java.isAssignableFrom(method!!.returnType)
            )
        }

        val mutationAndLookupMethods = listOf(
            "getItemById",
            "getItemWithCategoryById",
            "getItemsForListSync",
            "getItemsWithCategoryForListSync",
            "getItemCount",
            "getPurchasedItemCount",
            "getMaxSortOrder",
            "insertItem",
            "insertItems",
            "updateItem",
            "updateItems",
            "updatePurchasedStatus",
            "updateActualUnitPrice",
            "updateSortOrder",
            "deleteItem",
            "deleteItemById",
            "deleteItemsForList"
        )
        for (methodName in mutationAndLookupMethods) {
            val method = ShoppingItemDao::class.java.methods.find { it.name == methodName }
            assertNotNull("ShoppingItemDao must declare $methodName", method)
        }

        // Verify Room KSP compiler generated implementation
        val implClass = Class.forName("com.barakwebsoft.shopzen.data.local.dao.ShoppingItemDao_Impl")
        assertTrue("ShoppingItemDao_Impl must implement ShoppingItemDao", ShoppingItemDao::class.java.isAssignableFrom(implClass))

        // Verify source file contains @Dao and query annotations
        val daoSource = resolveSourceFile("src/main/java/com/barakwebsoft/shopzen/data/local/dao/ShoppingItemDao.kt").readText()
        assertTrue("Must be annotated with @Dao", daoSource.contains("@Dao"))
        assertTrue("getItemsWithCategoryForList must have @Transaction", daoSource.contains("@Transaction\n    @Query(\"SELECT * FROM shopping_items WHERE list_id = :listId"))
        assertTrue("Must have updatePurchasedStatus", daoSource.contains("UPDATE shopping_items SET is_purchased = :isPurchased"))
        assertTrue("Must have updateActualUnitPrice", daoSource.contains("UPDATE shopping_items SET actual_unit_price_minor = :actualUnitPriceMinor"))
    }

    @Test
    fun categoryDao_declaresRequiredMethodsAndGeneratedImplementation() {
        assertTrue("CategoryDao must be an interface", CategoryDao::class.java.isInterface)

        val getAllCategories = CategoryDao::class.java.methods.find { it.name == "getAllCategories" }
        assertNotNull("getAllCategories must exist", getAllCategories)
        assertTrue("getAllCategories must return Flow", Flow::class.java.isAssignableFrom(getAllCategories!!.returnType))

        val observeCategoryById = CategoryDao::class.java.methods.find { it.name == "observeCategoryById" }
        assertNotNull("observeCategoryById must exist", observeCategoryById)
        assertTrue("observeCategoryById must return Flow", Flow::class.java.isAssignableFrom(observeCategoryById!!.returnType))

        val requiredMethods = listOf(
            "getCategoryById",
            "getCategoryByName",
            "getAllCategoriesSync",
            "getCategoryCount",
            "insertCategory",
            "insertCategories",
            "updateCategory",
            "deleteCategory",
            "deleteCategoryById"
        )
        for (methodName in requiredMethods) {
            val method = CategoryDao::class.java.methods.find { it.name == methodName }
            assertNotNull("CategoryDao must declare $methodName", method)
        }

        // Verify Room KSP compiler generated implementation
        val implClass = Class.forName("com.barakwebsoft.shopzen.data.local.dao.CategoryDao_Impl")
        assertTrue("CategoryDao_Impl must implement CategoryDao", CategoryDao::class.java.isAssignableFrom(implClass))

        val daoSource = resolveSourceFile("src/main/java/com/barakwebsoft/shopzen/data/local/dao/CategoryDao.kt").readText()
        assertTrue("Must be annotated with @Dao", daoSource.contains("@Dao"))
        assertTrue("Must query categories ordered by sort_order", daoSource.contains("ORDER BY sort_order ASC"))
    }

    @Test
    fun historyDao_declaresRequiredMethodsAndGeneratedImplementation() {
        assertTrue("HistoryDao must be an interface", HistoryDao::class.java.isInterface)

        val flowMethods = listOf(
            "getAllHistory",
            "observeHistoryById",
            "observeHistoryWithItems",
            "searchHistory",
            "observeHistoryCount"
        )

        for (methodName in flowMethods) {
            val method = HistoryDao::class.java.methods.find { it.name == methodName }
            assertNotNull("HistoryDao must declare $methodName", method)
            assertTrue(
                "$methodName must return Flow",
                Flow::class.java.isAssignableFrom(method!!.returnType)
            )
        }

        val requiredMethods = listOf(
            "getHistoryById",
            "getHistoryWithItemsById",
            "getHistoryItems",
            "getHistoryCount",
            "insertHistory",
            "insertHistoryItems",
            "deleteHistory",
            "deleteHistoryById",
            "deleteAllHistory"
        )
        for (methodName in requiredMethods) {
            val method = HistoryDao::class.java.methods.find { it.name == methodName }
            assertNotNull("HistoryDao must declare $methodName", method)
        }

        // Verify atomic archive transaction method
        val insertHistoryWithItems = HistoryDao::class.java.methods.find { it.name.startsWith("insertHistoryWithItems") }
        assertNotNull("HistoryDao must provide insertHistoryWithItems", insertHistoryWithItems)

        // Verify Room KSP compiler generated implementation
        val implClass = Class.forName("com.barakwebsoft.shopzen.data.local.dao.HistoryDao_Impl")
        assertTrue("HistoryDao_Impl must implement HistoryDao", HistoryDao::class.java.isAssignableFrom(implClass))

        val daoSource = resolveSourceFile("src/main/java/com/barakwebsoft/shopzen/data/local/dao/HistoryDao.kt").readText()
        assertTrue("Must be annotated with @Dao", daoSource.contains("@Dao"))
        assertTrue("insertHistoryWithItems must have @Transaction", daoSource.contains("@Transaction\n    suspend fun insertHistoryWithItems"))
    }

    @Test
    fun defaultCategories_containsAllPrdRequiredCategories() {
        val categories = DefaultCategories.ALL
        assertEquals("Must contain 9 default categories", 9, categories.size)

        val expectedNames = setOf(
            "Vegetables",
            "Fruits",
            "Dairy",
            "Bakery",
            "Groceries",
            "Meat & Fish",
            "Cleaning",
            "Personal Care",
            "Others"
        )
        val actualNames = categories.map { it.name }.toSet()
        assertEquals(expectedNames, actualNames)

        // All should be marked isDefault = true
        assertTrue(categories.all { it.isDefault })

        // Sort orders must be sequential 0..8
        val sortOrders = categories.map { it.sortOrder }.sorted()
        assertEquals((0..8).toList(), sortOrders)

        // All IDs must be distinct
        assertEquals(9, categories.map { it.id }.distinct().size)

        // All icons and color tokens must be non-blank
        assertTrue(categories.all { !it.iconName.isNullOrBlank() })
        assertTrue(categories.all { !it.colorToken.isNullOrBlank() })
    }

    @Test
    fun shopZenDatabase_exposesAllFourDaos_andGeneratedImplementation() {
        val dbClass = ShopZenDatabase::class.java
        assertTrue("ShopZenDatabase must be abstract", Modifier.isAbstract(dbClass.modifiers))

        val listDaoMethod = dbClass.methods.find { it.name == "shoppingListDao" }
        assertNotNull("Database must expose shoppingListDao()", listDaoMethod)
        assertEquals(ShoppingListDao::class.java, listDaoMethod!!.returnType)

        val itemDaoMethod = dbClass.methods.find { it.name == "shoppingItemDao" }
        assertNotNull("Database must expose shoppingItemDao()", itemDaoMethod)
        assertEquals(ShoppingItemDao::class.java, itemDaoMethod!!.returnType)

        val categoryDaoMethod = dbClass.methods.find { it.name == "categoryDao" }
        assertNotNull("Database must expose categoryDao()", categoryDaoMethod)
        assertEquals(CategoryDao::class.java, categoryDaoMethod!!.returnType)

        val historyDaoMethod = dbClass.methods.find { it.name == "historyDao" }
        assertNotNull("Database must expose historyDao()", historyDaoMethod)
        assertEquals(HistoryDao::class.java, historyDaoMethod!!.returnType)

        val dbImplClass = Class.forName("com.barakwebsoft.shopzen.data.local.database.ShopZenDatabase_Impl")
        assertTrue("ShopZenDatabase_Impl must implement ShopZenDatabase", ShopZenDatabase::class.java.isAssignableFrom(dbImplClass))
    }
}
