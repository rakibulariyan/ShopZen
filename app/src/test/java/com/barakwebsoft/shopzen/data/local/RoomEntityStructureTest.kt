package com.barakwebsoft.shopzen.data.local

import com.barakwebsoft.shopzen.data.local.entity.CategoryEntity
import com.barakwebsoft.shopzen.data.local.entity.HistoryItemEntity
import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.data.local.entity.ShoppingHistoryEntity
import com.barakwebsoft.shopzen.data.local.entity.ShoppingItemEntity
import com.barakwebsoft.shopzen.data.local.entity.ShoppingListEntity
import com.barakwebsoft.shopzen.data.local.entity.ShoppingListStatus
import com.barakwebsoft.shopzen.data.local.relation.HistoryWithItems
import com.barakwebsoft.shopzen.data.local.relation.ShoppingItemWithCategory
import com.barakwebsoft.shopzen.data.local.relation.ShoppingListWithItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class RoomEntityStructureTest {

    private lateinit var schemaContent: String

    @Before
    fun setUp() {
        val schemaFile = listOf(
            File("schemas/com.barakwebsoft.shopzen.data.local.database.ShopZenDatabase/1.json"),
            File("app/schemas/com.barakwebsoft.shopzen.data.local.database.ShopZenDatabase/1.json")
        ).firstOrNull { it.exists() }

        assertNotNull("Schema file 1.json must exist in schemas directory", schemaFile)
        schemaContent = schemaFile!!.readText()
    }

    @Test
    fun testSchemaVersionAndTables() {
        assertTrue("Database version must be 1", schemaContent.contains("\"version\": 1"))
        assertTrue("shopping_lists table must exist", schemaContent.contains("\"tableName\": \"shopping_lists\""))
        assertTrue("shopping_items table must exist", schemaContent.contains("\"tableName\": \"shopping_items\""))
        assertTrue("categories table must exist", schemaContent.contains("\"tableName\": \"categories\""))
        assertTrue("shopping_history table must exist", schemaContent.contains("\"tableName\": \"shopping_history\""))
        assertTrue("history_items table must exist", schemaContent.contains("\"tableName\": \"history_items\""))
    }

    @Test
    fun testShoppingListIndices() {
        assertTrue(schemaContent.contains("\"name\": \"index_shopping_lists_status\""))
        assertTrue(schemaContent.contains("\"name\": \"index_shopping_lists_updated_at\""))
    }

    @Test
    fun testCategoriesIndices() {
        assertTrue(schemaContent.contains("\"name\": \"index_categories_sort_order\""))
        assertTrue(schemaContent.contains("\"name\": \"index_categories_name\""))
    }

    @Test
    fun testShoppingItemsForeignKeysAndIndices() {
        // Foreign key to shopping_lists with CASCADE
        assertTrue(schemaContent.contains("\"table\": \"shopping_lists\""))
        assertTrue(schemaContent.contains("\"onDelete\": \"CASCADE\""))

        // Foreign key to categories with SET NULL
        assertTrue(schemaContent.contains("\"table\": \"categories\""))
        assertTrue(schemaContent.contains("\"onDelete\": \"SET NULL\""))

        // Indices
        assertTrue(schemaContent.contains("\"name\": \"index_shopping_items_list_id\""))
        assertTrue(schemaContent.contains("\"name\": \"index_shopping_items_category_id\""))
        assertTrue(schemaContent.contains("\"name\": \"index_shopping_items_list_id_sort_order\""))
    }

    @Test
    fun testShoppingHistorySnapshotIntegrity() {
        assertTrue(schemaContent.contains("\"name\": \"index_shopping_history_completed_at\""))
        assertTrue(schemaContent.contains("\"name\": \"index_shopping_history_original_list_id\""))
        assertTrue(schemaContent.contains("\"table\": \"shopping_history\""))
        assertTrue(schemaContent.contains("\"name\": \"index_history_items_history_id\""))
    }

    @Test
    fun testShoppingListStatusConstantsAndEnum() {
        assertEquals("ACTIVE", ShoppingListStatus.ACTIVE)
        assertEquals("COMPLETED", ShoppingListStatus.COMPLETED)
        assertEquals("ARCHIVED", ShoppingListStatus.ARCHIVED)

        assertEquals("ACTIVE", ListStatus.ACTIVE.name)
        assertEquals("COMPLETED", ListStatus.COMPLETED.name)
        assertEquals("ARCHIVED", ListStatus.ARCHIVED.name)
    }

    @Test
    fun testEntityInstantiationAndDefaults() {
        val now = 1700000000000L
        val list = ShoppingListEntity(
            id = "list-1",
            name = "Weekly Groceries",
            currencyCode = "USD",
            createdAt = now,
            updatedAt = now
        )
        assertEquals("list-1", list.id)
        assertEquals("Weekly Groceries", list.name)
        assertNull(list.description)
        assertNull(list.budgetMinor)
        assertEquals("USD", list.currencyCode)
        assertEquals(ShoppingListStatus.ACTIVE, list.status)
        assertNull(list.completedAt)

        val item = ShoppingItemEntity(
            id = "item-1",
            listId = "list-1",
            name = "Apples",
            quantity = "2.5",
            unit = "kg",
            createdAt = now,
            updatedAt = now
        )
        assertEquals("item-1", item.id)
        assertEquals("2.5", item.quantity)
        assertEquals("kg", item.unit)
        assertEquals(false, item.isPurchased)
        assertNull(item.categoryId)
        assertNull(item.estimatedUnitPriceMinor)
        assertNull(item.actualUnitPriceMinor)
        assertNull(item.notes)
        assertEquals(0, item.sortOrder)

        val category = CategoryEntity(
            id = "cat-1",
            name = "Fruits",
            iconName = "fruits"
        )
        assertEquals("cat-1", category.id)
        assertEquals("Fruits", category.name)
        assertEquals("fruits", category.iconName)
        assertEquals(false, category.isDefault)
        assertEquals(0, category.sortOrder)

        val history = ShoppingHistoryEntity(
            id = "hist-1",
            originalListId = "list-1",
            listName = "Weekly Groceries",
            currencyCode = "USD",
            budgetMinor = 5000L,
            totalSpentMinor = 4250L,
            purchasedCount = 5,
            unpricedPurchasedCount = 0,
            completedAt = now
        )
        assertEquals("hist-1", history.id)
        assertEquals(5000L, history.budgetMinor)
        assertEquals(4250L, history.totalSpentMinor)

        val historyItem = HistoryItemEntity(
            id = "hist-item-1",
            historyId = "hist-1",
            originalItemId = "item-1",
            itemName = "Apples",
            quantity = "2.5",
            unit = "kg",
            categoryName = "Fruits",
            actualUnitPriceMinor = 850L,
            lineTotalMinor = 2125L,
            isPurchased = true
        )
        assertEquals(850L, historyItem.actualUnitPriceMinor)
        assertEquals(2125L, historyItem.lineTotalMinor)
        assertTrue(historyItem.isPurchased)
    }

    @Test
    fun testRelationDataClasses() {
        val now = 1700000000000L
        val list = ShoppingListEntity(
            id = "list-1",
            name = "Groceries",
            currencyCode = "USD",
            createdAt = now,
            updatedAt = now
        )
        val item = ShoppingItemEntity(
            id = "item-1",
            listId = "list-1",
            name = "Milk",
            quantity = "1",
            unit = "bottle",
            categoryId = "cat-1",
            createdAt = now,
            updatedAt = now
        )
        val category = CategoryEntity(
            id = "cat-1",
            name = "Dairy"
        )

        val listWithItems = ShoppingListWithItems(list = list, items = listOf(item))
        assertEquals("list-1", listWithItems.list.id)
        assertEquals(1, listWithItems.items.size)
        assertEquals("Milk", listWithItems.items[0].name)

        val itemWithCategory = ShoppingItemWithCategory(item = item, category = category)
        assertEquals("Milk", itemWithCategory.item.name)
        assertEquals("Dairy", itemWithCategory.category?.name)

        val history = ShoppingHistoryEntity(
            id = "hist-1",
            originalListId = "list-1",
            listName = "Groceries",
            currencyCode = "USD",
            totalSpentMinor = 350L,
            purchasedCount = 1,
            unpricedPurchasedCount = 0,
            completedAt = now
        )
        val historyItem = HistoryItemEntity(
            id = "hist-item-1",
            historyId = "hist-1",
            itemName = "Milk",
            quantity = "1",
            unit = "bottle",
            isPurchased = true
        )
        val historyWithItems = HistoryWithItems(history = history, items = listOf(historyItem))
        assertEquals("hist-1", historyWithItems.history.id)
        assertEquals(1, historyWithItems.items.size)
    }
}
