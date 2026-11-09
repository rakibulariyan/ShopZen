package com.barakwebsoft.shopzen.data.repository

import com.barakwebsoft.shopzen.core.util.MoneyCalculator
import com.barakwebsoft.shopzen.data.local.dao.CategoryDao
import com.barakwebsoft.shopzen.data.local.dao.HistoryDao
import com.barakwebsoft.shopzen.data.local.dao.ShoppingItemDao
import com.barakwebsoft.shopzen.data.local.dao.ShoppingListDao
import com.barakwebsoft.shopzen.data.local.entity.CategoryEntity
import com.barakwebsoft.shopzen.data.local.entity.HistoryItemEntity
import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.data.local.entity.ShoppingHistoryEntity
import com.barakwebsoft.shopzen.data.local.entity.ShoppingItemEntity
import com.barakwebsoft.shopzen.data.local.entity.ShoppingListEntity
import com.barakwebsoft.shopzen.data.local.relation.HistoryWithItems
import com.barakwebsoft.shopzen.data.local.relation.ShoppingItemWithCategory
import com.barakwebsoft.shopzen.data.local.relation.ShoppingListWithItems
import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import com.barakwebsoft.shopzen.domain.repository.CategoryRepository
import com.barakwebsoft.shopzen.domain.repository.HistoryRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingItemRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Verification test suite for SZ-045:
 * Validates atomic trip archive, historical snapshot immutability, foreign key independence
 * across active list deletion, cascade deletion of history items upon trip deletion,
 * and safe cloning/restoration into a new active list.
 */
class HistorySnapshotAndDeletionIntegrationTest {

    private lateinit var shoppingListDao: FakeShoppingListDao
    private lateinit var shoppingItemDao: FakeShoppingItemDao
    private lateinit var categoryDao: FakeCategoryDao
    private lateinit var historyDao: FakeHistoryDao

    private lateinit var listRepository: ShoppingListRepository
    private lateinit var itemRepository: ShoppingItemRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var historyRepository: HistoryRepository

    @Before
    fun setUp() {
        shoppingListDao = FakeShoppingListDao()
        categoryDao = FakeCategoryDao()
        shoppingItemDao = FakeShoppingItemDao(categoryDao)
        historyDao = FakeHistoryDao()

        // Simulate Room/SQLite FOREIGN KEY ... ON DELETE CASCADE
        shoppingListDao.onListDeleted = { deletedListId ->
            shoppingItemDao.deleteItemsForList(deletedListId)
        }

        listRepository = ShoppingListRepositoryImpl(
            shoppingListDao = shoppingListDao,
            shoppingItemDao = shoppingItemDao,
            ioDispatcher = Dispatchers.Unconfined
        )
        itemRepository = ShoppingItemRepositoryImpl(
            shoppingItemDao = shoppingItemDao,
            ioDispatcher = Dispatchers.Unconfined
        )
        categoryRepository = CategoryRepositoryImpl(
            categoryDao = categoryDao,
            ioDispatcher = Dispatchers.Unconfined
        )
        historyRepository = HistoryRepositoryImpl(
            historyDao = historyDao,
            shoppingListDao = shoppingListDao,
            shoppingItemDao = shoppingItemDao,
            ioDispatcher = Dispatchers.Unconfined
        )
    }

    // =========================================================================
    // 1. End-to-End Archival & Exact Snapshot Calculations
    // =========================================================================

    @Test
    fun archive_createsCompleteImmutableSnapshotWithMixedPricingAndDecimals() = runBlocking {
        // 1. Seed categories
        val produceCat = CategoryEntity("cat_prod", "Produce", "icon_apple", null, true, 0)
        val pantryCat = CategoryEntity("cat_pantry", "Pantry", "icon_jar", null, true, 1)
        val dairyCat = CategoryEntity("cat_dairy", "Dairy", "icon_cheese", null, true, 2)
        categoryDao.insertCategories(listOf(produceCat, pantryCat, dairyCat))

        // 2. Create active list
        val listId = "list_grocery"
        val originalList = ShoppingList(
            id = listId,
            name = "Weekly Grocery Trip",
            currencyCode = "EUR",
            budgetMinor = 5000L,
            status = ListStatus.ACTIVE
        )
        listRepository.createList(originalList)

        // 3. Add items with mixed pricing, decimal quantities, and category associations
        // Item 1: Purchased, priced with fractional dot quantity (2.5 kg @ €2.00 -> line total €5.00 = 500L)
        val item1 = ShoppingItem(
            id = "item_apples",
            listId = listId,
            name = "Apples",
            quantity = "2.5",
            unit = "kg",
            categoryId = "cat_prod",
            estimatedUnitPriceMinor = 220L,
            actualUnitPriceMinor = 200L,
            isPurchased = true
        )
        // Item 2: Purchased, priced with European comma quantity (1,5 L @ €8.00 -> line total €12.00 = 1200L)
        val item2 = ShoppingItem(
            id = "item_oil",
            listId = listId,
            name = "Olive Oil",
            quantity = "1,5",
            unit = "L",
            categoryId = "cat_pantry",
            estimatedUnitPriceMinor = 850L,
            actualUnitPriceMinor = 800L,
            isPurchased = true
        )
        // Item 3: Purchased, UNPRICED (disclosed item: actual price null, line total null)
        val item3 = ShoppingItem(
            id = "item_cheese",
            listId = listId,
            name = "Artisan Cheese",
            quantity = "1",
            unit = "wheel",
            categoryId = "cat_dairy",
            estimatedUnitPriceMinor = 700L,
            actualUnitPriceMinor = null,
            isPurchased = true
        )
        // Item 4: NOT PURCHASED (unpurchased item: actual price null, line total null)
        val item4 = ShoppingItem(
            id = "item_chocolate",
            listId = listId,
            name = "Dark Chocolate",
            quantity = "3",
            unit = "bars",
            categoryId = null,
            estimatedUnitPriceMinor = 150L,
            actualUnitPriceMinor = null,
            isPurchased = false
        )
        itemRepository.addItems(listOf(item1, item2, item3, item4))

        // 4. Perform atomic archive
        val historyId = historyRepository.archiveListToHistory(listId)
        assertTrue("Archive must return a non-blank UUID", historyId.isNotBlank())

        // 5. Verify original list status transitioned to ARCHIVED
        val updatedList = listRepository.getListById(listId)
        assertNotNull(updatedList)
        assertEquals(ListStatus.ARCHIVED, updatedList?.status)
        assertNotNull("completedAt must be populated", updatedList?.completedAt)

        // 6. Verify historical header record
        val historyHeader = historyRepository.getHistoryById(historyId)
        assertNotNull(historyHeader)
        assertEquals(historyId, historyHeader?.id)
        assertEquals(listId, historyHeader?.originalListId)
        assertEquals("Weekly Grocery Trip", historyHeader?.listName)
        assertEquals("EUR", historyHeader?.currencyCode)
        assertEquals(5000L, historyHeader?.budgetMinor)
        assertEquals("Total spent must be exactly (2.5*200) + (1.5*800) = 500 + 1200 = 1700L", 1700L, historyHeader?.totalSpentMinor)
        assertEquals("3 items purchased", 3, historyHeader?.purchasedCount)
        assertEquals("1 unpriced purchased item disclosed", 1, historyHeader?.unpricedPurchasedCount)

        // 7. Verify historical detail and item snapshots
        val historyDetail = historyRepository.getHistoryDetailById(historyId)
        assertNotNull(historyDetail)
        assertEquals(4, historyDetail?.items?.size)

        val snapApples = historyDetail?.items?.first { it.itemName == "Apples" }!!
        assertEquals("2.5", snapApples.quantity)
        assertEquals("kg", snapApples.unit)
        assertEquals("Produce", snapApples.categoryName)
        assertEquals(200L, snapApples.actualUnitPriceMinor)
        assertEquals(500L, snapApples.lineTotalMinor)
        assertTrue(snapApples.isPurchased)

        val snapOil = historyDetail.items.first { it.itemName == "Olive Oil" }
        assertEquals("1,5", snapOil.quantity)
        assertEquals("L", snapOil.unit)
        assertEquals("Pantry", snapOil.categoryName)
        assertEquals(800L, snapOil.actualUnitPriceMinor)
        assertEquals(1200L, snapOil.lineTotalMinor)
        assertTrue(snapOil.isPurchased)

        val snapCheese = historyDetail.items.first { it.itemName == "Artisan Cheese" }
        assertEquals("1", snapCheese.quantity)
        assertEquals("wheel", snapCheese.unit)
        assertEquals("Dairy", snapCheese.categoryName)
        assertNull("Unpriced item must have null actual unit price", snapCheese.actualUnitPriceMinor)
        assertNull("Unpriced item must have null line total", snapCheese.lineTotalMinor)
        assertTrue(snapCheese.isPurchased)

        val snapChoc = historyDetail.items.first { it.itemName == "Dark Chocolate" }
        assertEquals("3", snapChoc.quantity)
        assertEquals("bars", snapChoc.unit)
        assertNull("Category was null", snapChoc.categoryName)
        assertNull("Unpurchased item has null actual price", snapChoc.actualUnitPriceMinor)
        assertNull("Unpurchased item has null line total", snapChoc.lineTotalMinor)
        assertFalse("Chocolate was not purchased", snapChoc.isPurchased)
    }

    // =========================================================================
    // 2. Snapshot Integrity Across List Deletion and Post-Archive Edits
    // =========================================================================

    @Test
    fun snapshotIntegrity_permanentDeletionOfOriginalListLeavesHistoryIntact() = runBlocking {
        val listId = "list_for_deletion"
        listRepository.createList(
            ShoppingList(id = listId, name = "Trip to Delete", currencyCode = "USD", status = ListStatus.ACTIVE)
        )
        itemRepository.addItem(
            ShoppingItem(id = "item_tea", listId = listId, name = "Green Tea", quantity = "2", unit = "boxes", actualUnitPriceMinor = 350L, isPurchased = true)
        )

        // Archive to create history
        val historyId = historyRepository.archiveListToHistory(listId)
        assertNotNull(historyRepository.getHistoryById(historyId))

        // Permanently delete original active list (and its active items)
        listRepository.deleteList(listId)

        // Verify active list and items are completely deleted
        assertNull(listRepository.getListById(listId))
        assertTrue(itemRepository.getItemsForListSync(listId).isEmpty())

        // Verify history snapshot remains 100% available and intact
        val history = historyRepository.getHistoryById(historyId)
        assertNotNull("History record must survive active list deletion", history)
        assertEquals("Trip to Delete", history?.listName)
        assertEquals(listId, history?.originalListId)
        assertEquals(700L, history?.totalSpentMinor)

        val historyDetail = historyRepository.getHistoryDetailById(historyId)
        assertNotNull(historyDetail)
        assertEquals(1, historyDetail?.items?.size)
        assertEquals("Green Tea", historyDetail?.items?.first()?.itemName)
        assertEquals(700L, historyDetail?.items?.first()?.lineTotalMinor)
    }

    @Test
    fun snapshotIntegrity_postArchiveMutationsOnActiveItemsDoNotAlterSnapshot() = runBlocking {
        val listId = "list_immutable_test"
        listRepository.createList(
            ShoppingList(id = listId, name = "Original List Name", currencyCode = "GBP", budgetMinor = 3000L, status = ListStatus.ACTIVE)
        )
        val origItemId = "item_butter"
        itemRepository.addItem(
            ShoppingItem(id = origItemId, listId = listId, name = "Salted Butter", quantity = "1", unit = "pack", actualUnitPriceMinor = 250L, isPurchased = true)
        )

        val historyId = historyRepository.archiveListToHistory(listId)

        // Perform extreme post-archive mutations on original active records
        // 1. Rename original shopping list
        val currentList = listRepository.getListById(listId)!!
        listRepository.updateList(currentList.copy(name = "Changed List Name Post Archive", budgetMinor = 99999L))

        // 2. Update price and unmark purchased on original item
        itemRepository.updateActualPrice(origItemId, 12000L)
        itemRepository.toggleItemPurchased(origItemId, false)

        // 3. Add brand new item to the original list
        itemRepository.addItem(
            ShoppingItem(id = "item_cookies", listId = listId, name = "Cookies", quantity = "5", unit = "packs", actualUnitPriceMinor = 500L, isPurchased = true)
        )

        // 4. Verify historical snapshot is completely immutable and reflects the original archived state
        val historyHeader = historyRepository.getHistoryById(historyId)
        assertNotNull(historyHeader)
        assertEquals("Original List Name", historyHeader?.listName)
        assertEquals(3000L, historyHeader?.budgetMinor)
        assertEquals(250L, historyHeader?.totalSpentMinor)
        assertEquals(1, historyHeader?.purchasedCount)

        val historyDetail = historyRepository.getHistoryDetailById(historyId)
        assertNotNull(historyDetail)
        assertEquals("Snapshot must only have the 1 original item; post-archive cookies must not appear", 1, historyDetail?.items?.size)

        val snapshotItem = historyDetail?.items?.first()
        assertEquals("Salted Butter", snapshotItem?.itemName)
        assertEquals(250L, snapshotItem?.actualUnitPriceMinor)
        assertEquals(250L, snapshotItem?.lineTotalMinor)
        assertTrue("Snapshot item must retain purchased state", snapshotItem?.isPurchased == true)
    }

    // =========================================================================
    // 3. Deletion Behavior & Cascade
    // =========================================================================

    @Test
    fun deletionBehavior_deleteHistoryTripCascadesAndRemovesChildHistoryItems() = runBlocking {
        // Create Trip 1 with 2 items
        val list1Id = "list_trip_1"
        listRepository.createList(ShoppingList(id = list1Id, name = "Trip 1", currencyCode = "USD", status = ListStatus.ACTIVE))
        itemRepository.addItem(ShoppingItem(id = "i1", listId = list1Id, name = "Item 1", quantity = "1", unit = "ea", actualUnitPriceMinor = 100L, isPurchased = true))
        itemRepository.addItem(ShoppingItem(id = "i2", listId = list1Id, name = "Item 2", quantity = "2", unit = "ea", actualUnitPriceMinor = 200L, isPurchased = true))
        val history1Id = historyRepository.archiveListToHistory(list1Id)

        // Create Trip 2 with 1 item
        val list2Id = "list_trip_2"
        listRepository.createList(ShoppingList(id = list2Id, name = "Trip 2", currencyCode = "USD", status = ListStatus.ACTIVE))
        itemRepository.addItem(ShoppingItem(id = "i3", listId = list2Id, name = "Item 3", quantity = "1", unit = "ea", actualUnitPriceMinor = 300L, isPurchased = true))
        val history2Id = historyRepository.archiveListToHistory(list2Id)

        assertEquals(2, historyRepository.getHistoryCount())

        // Delete Trip 1
        historyRepository.deleteHistory(history1Id)

        // Trip 1 header and child snapshot items must be gone
        assertEquals(1, historyRepository.getHistoryCount())
        assertNull(historyRepository.getHistoryById(history1Id))
        assertNull(historyRepository.getHistoryDetailById(history1Id))

        // Trip 2 must remain completely intact
        val history2 = historyRepository.getHistoryById(history2Id)
        assertNotNull(history2)
        assertEquals("Trip 2", history2?.listName)
        val history2Detail = historyRepository.getHistoryDetailById(history2Id)
        assertEquals(1, history2Detail?.items?.size)

        // Original shopping lists must remain untouched by history deletion
        assertNotNull(listRepository.getListById(list1Id))
        assertNotNull(listRepository.getListById(list2Id))
    }

    @Test
    fun deletionBehavior_deleteAllHistoryCleansUpAllTripsWithoutTouchingActiveLists() = runBlocking {
        // Create 2 trips
        val l1 = "l1"; val l2 = "l2"
        listRepository.createList(ShoppingList(id = l1, name = "List 1", currencyCode = "USD", status = ListStatus.ACTIVE))
        listRepository.createList(ShoppingList(id = l2, name = "List 2", currencyCode = "USD", status = ListStatus.ACTIVE))
        itemRepository.addItem(ShoppingItem(id = "item_1", listId = l1, name = "A", quantity = "1", unit = "ea", actualUnitPriceMinor = 10L, isPurchased = true))
        itemRepository.addItem(ShoppingItem(id = "item_2", listId = l2, name = "B", quantity = "1", unit = "ea", actualUnitPriceMinor = 20L, isPurchased = true))

        historyRepository.archiveListToHistory(l1)
        historyRepository.archiveListToHistory(l2)

        assertEquals(2, historyRepository.getHistoryCount())

        // Call deleteAllHistory
        historyRepository.deleteAllHistory()

        // History count is 0 and Flow emits empty list
        assertEquals(0, historyRepository.getHistoryCount())
        assertTrue(historyRepository.getAllHistory().first().isEmpty())

        // Active/archived shopping lists in ShoppingListRepository remain intact
        assertNotNull(listRepository.getListById(l1))
        assertNotNull(listRepository.getListById(l2))
        assertEquals(2, listRepository.observeTotalListCount().first())
    }

    // =========================================================================
    // 4. Archive Validation & State Machine Guardrails
    // =========================================================================

    @Test
    fun archiveValidation_rejectsAlreadyArchivedList() = runBlocking {
        val listId = "list_archive_twice"
        listRepository.createList(ShoppingList(id = listId, name = "Double Archive", currencyCode = "USD", status = ListStatus.ACTIVE))
        itemRepository.addItem(ShoppingItem(id = "item_x", listId = listId, name = "Eggs", quantity = "12", unit = "pack", actualUnitPriceMinor = 350L, isPurchased = true))

        // First archive succeeds
        historyRepository.archiveListToHistory(listId)

        // Second archive must throw IllegalStateException
        try {
            historyRepository.archiveListToHistory(listId)
            fail("Expected IllegalStateException when archiving an already archived list")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("already archived") == true)
        }
    }

    @Test
    fun archiveValidation_rejectsNonExistentList() = runBlocking {
        try {
            historyRepository.archiveListToHistory("non_existent_id")
            fail("Expected IllegalArgumentException when archiving non-existent list")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Cannot archive non-existent list") == true)
        }
    }

    // =========================================================================
    // 5. Historical Detail to New List Duplication Behavior
    // =========================================================================

    @Test
    fun restorationFlow_startNewListFromHistoricalTrip_createsDecoupledActiveList() = runBlocking {
        // Setup trip
        val originalListId = "trip_for_cloning"
        listRepository.createList(
            ShoppingList(id = originalListId, name = "Holiday Feast", currencyCode = "CAD", budgetMinor = 8000L, status = ListStatus.ACTIVE)
        )
        itemRepository.addItem(
            ShoppingItem(id = "item_turkey", listId = originalListId, name = "Roast Turkey", quantity = "1", unit = "whole", actualUnitPriceMinor = 4500L, isPurchased = true)
        )
        itemRepository.addItem(
            ShoppingItem(id = "item_cranberry", listId = originalListId, name = "Cranberries", quantity = "2", unit = "bags", actualUnitPriceMinor = 400L, isPurchased = true)
        )

        val historyId = historyRepository.archiveListToHistory(originalListId)
        val historyDetail = historyRepository.getHistoryDetailById(historyId)!!

        // Simulate "Start New List from this Trip" action
        val newListId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val clonedList = ShoppingList(
            id = newListId,
            name = "${historyDetail.history.listName} (Copy)",
            currencyCode = historyDetail.history.currencyCode,
            budgetMinor = historyDetail.history.budgetMinor,
            status = ListStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )
        listRepository.createList(clonedList)

        val clonedItems = historyDetail.items.mapIndexed { index, hItem ->
            ShoppingItem(
                id = UUID.randomUUID().toString(),
                listId = newListId,
                name = hItem.itemName,
                quantity = hItem.quantity,
                unit = hItem.unit,
                categoryId = null,
                estimatedUnitPriceMinor = hItem.actualUnitPriceMinor, // Past actual becomes new estimated
                actualUnitPriceMinor = null,                          // Reset actual price
                isPurchased = false,                                   // Reset purchase state
                sortOrder = index,
                createdAt = now + index,
                updatedAt = now + index
            )
        }
        itemRepository.addItems(clonedItems)

        // Verify cloned list in repository
        val loadedNewList = listRepository.getListById(newListId)
        assertNotNull(loadedNewList)
        assertEquals("Holiday Feast (Copy)", loadedNewList?.name)
        assertEquals("CAD", loadedNewList?.currencyCode)
        assertEquals(8000L, loadedNewList?.budgetMinor)
        assertEquals(ListStatus.ACTIVE, loadedNewList?.status)

        // Verify cloned items in repository
        val loadedNewItems = itemRepository.getItemsForListSync(newListId)
        assertEquals(2, loadedNewItems.size)

        val turkey = loadedNewItems.first { it.name == "Roast Turkey" }
        assertFalse("Cloned item must have purchase state reset", turkey.isPurchased)
        assertNull("Cloned item must have actual price reset to null", turkey.actualUnitPriceMinor)
        assertEquals("Cloned item estimated price is seeded from historical actual price", 4500L, turkey.estimatedUnitPriceMinor)
        assertTrue("Cloned item must have a new distinct UUID", turkey.id != "item_turkey")

        // Mutating cloned list does not mutate history
        itemRepository.deleteItem(turkey.id)
        val historyAfterItemDelete = historyRepository.getHistoryDetailById(historyId)
        assertEquals("Historical snapshot must still contain both items", 2, historyAfterItemDelete?.items?.size)
    }

    // =========================================================================
    // In-memory fake DAOs
    // =========================================================================

    private class FakeShoppingListDao : ShoppingListDao {
        private val lists = MutableStateFlow<Map<String, ShoppingListEntity>>(emptyMap())
        var onListDeleted: (suspend (String) -> Unit)? = null

        override fun getAllLists(): Flow<List<ShoppingListEntity>> =
            lists.map { it.values.sortedByDescending { l -> l.updatedAt } }

        override fun getListsByStatus(status: String): Flow<List<ShoppingListEntity>> =
            lists.map { it.values.filter { l -> l.status == status }.sortedByDescending { l -> l.updatedAt } }

        override fun getActiveLists(): Flow<List<ShoppingListEntity>> =
            getListsByStatus(ListStatus.ACTIVE.name)

        override fun observeActiveListsWithItems(): Flow<List<ShoppingListWithItems>> =
            lists.map { map ->
                map.values.filter { it.status == ListStatus.ACTIVE.name }
                    .sortedByDescending { it.updatedAt }
                    .map { ShoppingListWithItems(it, emptyList()) }
            }

        override fun getCompletedLists(): Flow<List<ShoppingListEntity>> =
            getListsByStatus(ListStatus.COMPLETED.name)

        override fun getArchivedLists(): Flow<List<ShoppingListEntity>> =
            getListsByStatus(ListStatus.ARCHIVED.name)

        override fun observeListById(id: String): Flow<ShoppingListEntity?> =
            lists.map { it[id] }

        override fun observeListWithItems(id: String): Flow<ShoppingListWithItems?> =
            lists.map { it[id]?.let { entity -> ShoppingListWithItems(entity, emptyList()) } }

        override fun searchLists(query: String): Flow<List<ShoppingListEntity>> =
            lists.map { map ->
                map.values.filter { it.name.contains(query, ignoreCase = true) }
                    .sortedByDescending { it.updatedAt }
            }

        override fun observeActiveListCount(): Flow<Int> =
            lists.map { it.values.count { l -> l.status == ListStatus.ACTIVE.name } }

        override fun observeTotalListCount(): Flow<Int> =
            lists.map { it.size }

        override suspend fun getListById(id: String): ShoppingListEntity? = lists.value[id]

        override suspend fun getListWithItemsById(id: String): ShoppingListWithItems? =
            lists.value[id]?.let { ShoppingListWithItems(it, emptyList()) }

        override suspend fun getActiveListCount(): Int =
            lists.value.values.count { it.status == ListStatus.ACTIVE.name }

        override suspend fun getTotalListCount(): Int = lists.value.size

        override suspend fun insertList(list: ShoppingListEntity) {
            lists.value = lists.value + (list.id to list)
        }

        override suspend fun insertLists(lists: List<ShoppingListEntity>) {
            this.lists.value = this.lists.value + lists.associateBy { it.id }
        }

        override suspend fun updateList(list: ShoppingListEntity) {
            lists.value = lists.value + (list.id to list)
        }

        override suspend fun updateListStatus(id: String, status: String, updatedAt: Long) {
            lists.value[id]?.let {
                lists.value = lists.value + (id to it.copy(status = status, updatedAt = updatedAt))
            }
        }

        override suspend fun updateListCompletion(id: String, status: String, completedAt: Long?, updatedAt: Long) {
            lists.value[id]?.let {
                lists.value = lists.value + (id to it.copy(status = status, completedAt = completedAt, updatedAt = updatedAt))
            }
        }

        override suspend fun deleteList(list: ShoppingListEntity) {
            lists.value = lists.value - list.id
            onListDeleted?.invoke(list.id)
        }

        override suspend fun deleteListById(id: String) {
            lists.value = lists.value - id
            onListDeleted?.invoke(id)
        }
    }

    private class FakeShoppingItemDao(
        private val categoryDao: FakeCategoryDao
    ) : ShoppingItemDao {
        private val items = MutableStateFlow<Map<String, ShoppingItemEntity>>(emptyMap())

        override fun getItemsForList(listId: String): Flow<List<ShoppingItemEntity>> =
            items.map { it.values.filter { i -> i.listId == listId }.sortedBy { i -> i.sortOrder } }

        override fun getItemsWithCategoryForList(listId: String): Flow<List<ShoppingItemWithCategory>> =
            items.map { map ->
                map.values.filter { i -> i.listId == listId }
                    .sortedBy { i -> i.sortOrder }
                    .map { item ->
                        val cat = item.categoryId?.let { categoryDao.getCategorySync(it) }
                        ShoppingItemWithCategory(item, cat)
                    }
            }

        override fun getPurchasedItemsForList(listId: String): Flow<List<ShoppingItemEntity>> =
            items.map { it.values.filter { i -> i.listId == listId && i.isPurchased }.sortedBy { i -> i.sortOrder } }

        override fun getPendingItemsForList(listId: String): Flow<List<ShoppingItemEntity>> =
            items.map { it.values.filter { i -> i.listId == listId && !i.isPurchased }.sortedBy { i -> i.sortOrder } }

        override fun observeItemById(id: String): Flow<ShoppingItemEntity?> =
            items.map { it[id] }

        override fun observeItemWithCategory(id: String): Flow<ShoppingItemWithCategory?> =
            items.map { map ->
                map[id]?.let { entity ->
                    val cat = entity.categoryId?.let { categoryDao.getCategorySync(it) }
                    ShoppingItemWithCategory(entity, cat)
                }
            }

        override fun observeItemCount(listId: String): Flow<Int> =
            items.map { it.values.count { i -> i.listId == listId } }

        override fun observePurchasedItemCount(listId: String): Flow<Int> =
            items.map { it.values.count { i -> i.listId == listId && i.isPurchased } }

        override fun searchItemsInList(listId: String, query: String): Flow<List<ShoppingItemEntity>> =
            items.map {
                it.values.filter { i -> i.listId == listId && i.name.contains(query, ignoreCase = true) }
                    .sortedBy { i -> i.sortOrder }
            }

        override fun searchItemsWithCategoryInList(listId: String, query: String): Flow<List<ShoppingItemWithCategory>> =
            items.map { map ->
                map.values.filter { i -> i.listId == listId && i.name.contains(query, ignoreCase = true) }
                    .sortedBy { i -> i.sortOrder }
                    .map { item ->
                        val cat = item.categoryId?.let { categoryDao.getCategorySync(it) }
                        ShoppingItemWithCategory(item, cat)
                    }
            }

        override suspend fun getItemById(id: String): ShoppingItemEntity? = items.value[id]

        override suspend fun getItemWithCategoryById(id: String): ShoppingItemWithCategory? =
            items.value[id]?.let { entity ->
                val cat = entity.categoryId?.let { categoryDao.getCategorySync(it) }
                ShoppingItemWithCategory(entity, cat)
            }

        override suspend fun getItemsForListSync(listId: String): List<ShoppingItemEntity> =
            items.value.values.filter { it.listId == listId }.sortedBy { it.sortOrder }

        override suspend fun getItemsWithCategoryForListSync(listId: String): List<ShoppingItemWithCategory> =
            items.value.values.filter { it.listId == listId }.sortedBy { it.sortOrder }.map { item ->
                val cat = item.categoryId?.let { categoryDao.getCategorySync(it) }
                ShoppingItemWithCategory(item, cat)
            }

        override suspend fun getItemCount(listId: String): Int =
            items.value.values.count { it.listId == listId }

        override suspend fun getPurchasedItemCount(listId: String): Int =
            items.value.values.count { it.listId == listId && it.isPurchased }

        override suspend fun getMaxSortOrder(listId: String): Int? =
            items.value.values.filter { it.listId == listId }.maxOfOrNull { it.sortOrder }

        override suspend fun insertItem(item: ShoppingItemEntity) {
            items.value = items.value + (item.id to item)
        }

        override suspend fun insertItems(items: List<ShoppingItemEntity>) {
            this.items.value = this.items.value + items.associateBy { it.id }
        }

        override suspend fun updateItem(item: ShoppingItemEntity) {
            items.value = items.value + (item.id to item)
        }

        override suspend fun updateItems(items: List<ShoppingItemEntity>) {
            this.items.value = this.items.value + items.associateBy { it.id }
        }

        override suspend fun updatePurchasedStatus(id: String, isPurchased: Boolean, updatedAt: Long) {
            items.value[id]?.let {
                items.value = items.value + (id to it.copy(isPurchased = isPurchased, updatedAt = updatedAt))
            }
        }

        override suspend fun updateActualUnitPrice(id: String, actualUnitPriceMinor: Long?, updatedAt: Long) {
            items.value[id]?.let {
                items.value = items.value + (id to it.copy(actualUnitPriceMinor = actualUnitPriceMinor, updatedAt = updatedAt))
            }
        }

        override suspend fun updateSortOrder(id: String, sortOrder: Int, updatedAt: Long) {
            items.value[id]?.let {
                items.value = items.value + (id to it.copy(sortOrder = sortOrder, updatedAt = updatedAt))
            }
        }

        override suspend fun deleteItem(item: ShoppingItemEntity) {
            items.value = items.value - item.id
        }

        override suspend fun deleteItemById(id: String) {
            items.value = items.value - id
        }

        override suspend fun deleteItemsForList(listId: String) {
            items.value = items.value.filterValues { it.listId != listId }
        }
    }

    private class FakeCategoryDao : CategoryDao {
        private val categories = MutableStateFlow<Map<String, CategoryEntity>>(emptyMap())

        fun getCategorySync(id: String): CategoryEntity? = categories.value[id]

        override fun getAllCategories(): Flow<List<CategoryEntity>> =
            categories.map { it.values.sortedBy { c -> c.sortOrder } }

        override fun observeCategoryById(id: String): Flow<CategoryEntity?> =
            categories.map { it[id] }

        override suspend fun getCategoryById(id: String): CategoryEntity? = categories.value[id]

        override suspend fun getCategoryByName(name: String): CategoryEntity? =
            categories.value.values.find { it.name.equals(name, ignoreCase = true) }

        override suspend fun getAllCategoriesSync(): List<CategoryEntity> =
            categories.value.values.sortedBy { it.sortOrder }

        override suspend fun getCategoryCount(): Int = categories.value.size

        override suspend fun insertCategory(category: CategoryEntity) {
            categories.value = categories.value + (category.id to category)
        }

        override suspend fun insertCategories(categories: List<CategoryEntity>) {
            this.categories.value = this.categories.value + categories.associateBy { it.id }
        }

        override suspend fun updateCategory(category: CategoryEntity) {
            categories.value = categories.value + (category.id to category)
        }

        override suspend fun deleteCategory(category: CategoryEntity) {
            categories.value = categories.value - category.id
        }

        override suspend fun deleteCategoryById(id: String) {
            categories.value = categories.value - id
        }
    }

    private class FakeHistoryDao : HistoryDao {
        private val history = MutableStateFlow<Map<String, ShoppingHistoryEntity>>(emptyMap())
        private val historyItems = MutableStateFlow<Map<String, HistoryItemEntity>>(emptyMap())

        override fun getAllHistory(): Flow<List<ShoppingHistoryEntity>> =
            history.map { it.values.sortedByDescending { h -> h.completedAt } }

        override fun observeHistoryById(id: String): Flow<ShoppingHistoryEntity?> =
            history.map { it[id] }

        override fun observeHistoryWithItems(id: String): Flow<HistoryWithItems?> =
            history.map { hMap ->
                hMap[id]?.let { header ->
                    val items = historyItems.value.values.filter { it.historyId == id }
                    HistoryWithItems(header, items)
                }
            }

        override fun searchHistory(query: String): Flow<List<ShoppingHistoryEntity>> =
            history.map { hMap ->
                hMap.values.filter { it.listName.contains(query, ignoreCase = true) }
                    .sortedByDescending { it.completedAt }
            }

        override fun observeHistoryCount(): Flow<Int> = history.map { it.size }

        override suspend fun getHistoryById(id: String): ShoppingHistoryEntity? = history.value[id]

        override suspend fun getHistoryWithItemsById(id: String): HistoryWithItems? =
            history.value[id]?.let { header ->
                val items = historyItems.value.values.filter { it.historyId == id }
                HistoryWithItems(header, items)
            }

        override suspend fun getHistoryItems(historyId: String): List<HistoryItemEntity> =
            historyItems.value.values.filter { it.historyId == historyId }

        override suspend fun getHistoryCount(): Int = history.value.size

        override suspend fun insertHistory(history: ShoppingHistoryEntity) {
            this.history.value = this.history.value + (history.id to history)
        }

        override suspend fun insertHistoryItems(items: List<HistoryItemEntity>) {
            this.historyItems.value = this.historyItems.value + items.associateBy { it.id }
        }

        override suspend fun deleteHistory(history: ShoppingHistoryEntity) {
            this.history.value = this.history.value - history.id
            this.historyItems.value = this.historyItems.value.filterValues { it.historyId != history.id }
        }

        override suspend fun deleteHistoryById(id: String) {
            history.value = history.value - id
            historyItems.value = historyItems.value.filterValues { it.historyId != id }
        }

        override suspend fun deleteAllHistory() {
            history.value = emptyMap()
            historyItems.value = emptyMap()
        }
    }
}

