package com.barakwebsoft.shopzen.data.mapper

import com.barakwebsoft.shopzen.data.local.entity.CategoryEntity
import com.barakwebsoft.shopzen.data.local.entity.HistoryItemEntity
import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.data.local.entity.ShoppingHistoryEntity
import com.barakwebsoft.shopzen.data.local.entity.ShoppingItemEntity
import com.barakwebsoft.shopzen.data.local.entity.ShoppingListEntity
import com.barakwebsoft.shopzen.data.local.relation.HistoryWithItems
import com.barakwebsoft.shopzen.data.local.relation.ShoppingItemWithCategory
import com.barakwebsoft.shopzen.domain.model.Category
import com.barakwebsoft.shopzen.domain.model.HistoryItem
import com.barakwebsoft.shopzen.domain.model.ShoppingHistory
import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    @Test
    fun categoryMapper_mapsBothDirectionsAccurately() {
        val entity = CategoryEntity(
            id = "cat_1",
            name = "Produce",
            iconName = "eco",
            colorToken = "primary",
            isDefault = true,
            sortOrder = 2
        )
        val domain = entity.toDomain()
        assertEquals("cat_1", domain.id)
        assertEquals("Produce", domain.name)
        assertEquals("eco", domain.iconName)
        assertEquals("primary", domain.colorToken)
        assertTrue(domain.isDefault)
        assertEquals(2, domain.sortOrder)

        val mappedBack = domain.toEntity()
        assertEquals(entity, mappedBack)
    }

    @Test
    fun shoppingItemMapper_mapsBothDirectionsAccurately() {
        val entity = ShoppingItemEntity(
            id = "item_1",
            listId = "list_1",
            name = "Apples",
            quantity = "2.5",
            unit = "kg",
            categoryId = "cat_1",
            estimatedUnitPriceMinor = 350L,
            actualUnitPriceMinor = 400L,
            isPurchased = true,
            notes = "Honeycrisp",
            sortOrder = 1,
            createdAt = 1000L,
            updatedAt = 2000L
        )
        val domain = entity.toDomain()
        assertEquals("item_1", domain.id)
        assertEquals("list_1", domain.listId)
        assertEquals("Apples", domain.name)
        assertEquals("2.5", domain.quantity)
        assertEquals("kg", domain.unit)
        assertEquals("cat_1", domain.categoryId)
        assertEquals(350L, domain.estimatedUnitPriceMinor)
        assertEquals(400L, domain.actualUnitPriceMinor)
        assertTrue(domain.isPurchased)
        assertEquals("Honeycrisp", domain.notes)
        assertEquals(1, domain.sortOrder)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.updatedAt)

        val mappedBack = domain.toEntity()
        assertEquals(entity, mappedBack)
    }

    @Test
    fun shoppingItemWithCategory_mapsToDomainDetail() {
        val itemEntity = ShoppingItemEntity(
            id = "item_1",
            listId = "list_1",
            name = "Milk",
            quantity = "1",
            unit = "bottle",
            categoryId = "cat_dairy",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val categoryEntity = CategoryEntity(
            id = "cat_dairy",
            name = "Dairy",
            iconName = "egg",
            colorToken = "tertiary"
        )
        val relation = ShoppingItemWithCategory(
            item = itemEntity,
            category = categoryEntity
        )
        val detail = relation.toDomainDetail()
        assertEquals("Milk", detail.item.name)
        assertEquals("Dairy", detail.category?.name)
    }

    @Test
    fun shoppingListMapper_mapsBothDirectionsAccurately() {
        val entity = ShoppingListEntity(
            id = "list_1",
            name = "Weekly Groceries",
            description = "Sunday market run",
            currencyCode = "EUR",
            budgetMinor = 5000L,
            status = ListStatus.ACTIVE.name,
            createdAt = 1000L,
            updatedAt = 2000L,
            completedAt = null
        )
        val domain = entity.toDomain()
        assertEquals("list_1", domain.id)
        assertEquals("Weekly Groceries", domain.name)
        assertEquals("Sunday market run", domain.description)
        assertEquals("EUR", domain.currencyCode)
        assertEquals(5000L, domain.budgetMinor)
        assertEquals(ListStatus.ACTIVE, domain.status)
        assertNull(domain.completedAt)

        val mappedBack = domain.toEntity()
        assertEquals(entity, mappedBack)
    }

    @Test
    fun shoppingListDetail_computesItemCountsAccurately() {
        val list = ShoppingList(id = "list_1", name = "Test")
        val item1 = ShoppingItem(id = "i1", listId = "list_1", name = "Item 1", isPurchased = true)
        val item2 = ShoppingItem(id = "i2", listId = "list_1", name = "Item 2", isPurchased = false)
        val item3 = ShoppingItem(id = "i3", listId = "list_1", name = "Item 3", isPurchased = true)

        val detail = ShoppingListDetail(
            list = list,
            items = listOf(
                com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail(item = item1),
                com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail(item = item2),
                com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail(item = item3)
            )
        )

        assertEquals(3, detail.totalItemsCount)
        assertEquals(2, detail.purchasedItemsCount)
        assertEquals(1, detail.pendingItemsCount)
    }

    @Test
    fun historyMapper_mapsBothDirectionsAccurately() {
        val historyEntity = ShoppingHistoryEntity(
            id = "h_1",
            originalListId = "list_1",
            listName = "Weekend Market",
            currencyCode = "USD",
            budgetMinor = 10000L,
            totalSpentMinor = 8500L,
            purchasedCount = 5,
            unpricedPurchasedCount = 1,
            completedAt = 3000L
        )
        val domainHistory = historyEntity.toDomain()
        assertEquals("h_1", domainHistory.id)
        assertEquals("Weekend Market", domainHistory.listName)
        assertEquals(8500L, domainHistory.totalSpentMinor)
        assertEquals(5, domainHistory.purchasedCount)
        assertEquals(1, domainHistory.unpricedPurchasedCount)

        val mappedBackHistory = domainHistory.toEntity()
        assertEquals(historyEntity, mappedBackHistory)

        val itemEntity = HistoryItemEntity(
            id = "hi_1",
            historyId = "h_1",
            originalItemId = "item_1",
            itemName = "Bread",
            quantity = "2",
            unit = "loaves",
            categoryName = "Bakery",
            actualUnitPriceMinor = 300L,
            lineTotalMinor = 600L,
            isPurchased = true
        )
        val domainItem = itemEntity.toDomain()
        assertEquals("hi_1", domainItem.id)
        assertEquals("Bread", domainItem.itemName)
        assertEquals(600L, domainItem.lineTotalMinor)

        val mappedBackItem = domainItem.toEntity()
        assertEquals(itemEntity, mappedBackItem)

        val relation = HistoryWithItems(
            history = historyEntity,
            items = listOf(itemEntity)
        )
        val detail = relation.toDomainDetail()
        assertEquals(1, detail.totalItemsCount)
        assertEquals(1, detail.purchasedItemsCount)
    }
}
