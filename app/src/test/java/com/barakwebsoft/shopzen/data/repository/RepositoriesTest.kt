package com.barakwebsoft.shopzen.data.repository

import com.barakwebsoft.shopzen.data.local.DefaultCategories
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
import com.barakwebsoft.shopzen.domain.model.Category
import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RepositoriesTest {

    private lateinit var fakeListDao: FakeShoppingListDao
    private lateinit var fakeItemDao: FakeShoppingItemDao
    private lateinit var fakeCategoryDao: FakeCategoryDao
    private lateinit var fakeHistoryDao: FakeHistoryDao

    private lateinit var listRepository: ShoppingListRepositoryImpl
    private lateinit var itemRepository: ShoppingItemRepositoryImpl
    private lateinit var categoryRepository: CategoryRepositoryImpl
    private lateinit var historyRepository: HistoryRepositoryImpl

    @Before
    fun setUp() {
        fakeListDao = FakeShoppingListDao()
        fakeItemDao = FakeShoppingItemDao()
        fakeCategoryDao = FakeCategoryDao()
        fakeHistoryDao = FakeHistoryDao()

        val dispatcher = Dispatchers.Unconfined
        listRepository = ShoppingListRepositoryImpl(fakeListDao, fakeItemDao, dispatcher)
        itemRepository = ShoppingItemRepositoryImpl(fakeItemDao, dispatcher)
        categoryRepository = CategoryRepositoryImpl(fakeCategoryDao, dispatcher)
        historyRepository = HistoryRepositoryImpl(fakeHistoryDao, fakeListDao, fakeItemDao, dispatcher)
    }

    @Test
    fun shoppingListRepository_createAndObserveLists() = runBlocking {
        val list = ShoppingList(id = "list_1", name = "Groceries", currencyCode = "USD", budgetMinor = 5000L)
        listRepository.createList(list)

        val lists = listRepository.getAllLists().first()
        assertEquals(1, lists.size)
        assertEquals("Groceries", lists[0].name)
        assertEquals(5000L, lists[0].budgetMinor)

        val retrieved = listRepository.getListById("list_1")
        assertNotNull(retrieved)
        assertEquals("Groceries", retrieved?.name)
    }

    @Test
    fun shoppingListRepository_statusUpdatesAndCompletion() = runBlocking {
        val list = ShoppingList(id = "list_1", name = "Active List", status = ListStatus.ACTIVE)
        listRepository.createList(list)

        listRepository.updateListStatus("list_1", ListStatus.ARCHIVED)
        assertEquals(ListStatus.ARCHIVED, listRepository.getListById("list_1")?.status)

        listRepository.markListCompleted("list_1", 5000L)
        val completed = listRepository.getListById("list_1")
        assertEquals(ListStatus.COMPLETED, completed?.status)
        assertEquals(5000L, completed?.completedAt)
    }

    @Test
    fun shoppingListRepository_duplicateList_createsNewIdsAndResetsState() = runBlocking {
        val originalList = ShoppingList(
            id = "orig_list",
            name = "Party Supplies",
            currencyCode = "GBP",
            budgetMinor = 15000L,
            status = ListStatus.COMPLETED
        )
        listRepository.createList(originalList)

        val originalItem = ShoppingItem(
            id = "orig_item",
            listId = "orig_list",
            name = "Balloons",
            quantity = "3",
            actualUnitPriceMinor = 250L,
            isPurchased = true
        )
        itemRepository.addItem(originalItem)

        val duplicatedListId = listRepository.duplicateList("orig_list", "Party Supplies (Spring)")
        assertNotEquals("orig_list", duplicatedListId)

        val duplicatedList = listRepository.getListById(duplicatedListId)
        assertNotNull(duplicatedList)
        assertEquals("Party Supplies (Spring)", duplicatedList?.name)
        assertEquals(ListStatus.ACTIVE, duplicatedList?.status)
        assertNull(duplicatedList?.completedAt)

        val duplicatedItems = itemRepository.getItemsForList(duplicatedListId).first()
        assertEquals(1, duplicatedItems.size)
        assertNotEquals("orig_item", duplicatedItems[0].id)
        assertEquals(duplicatedListId, duplicatedItems[0].listId)
        assertEquals("Balloons", duplicatedItems[0].name)
        assertFalse("Duplicated item purchase state must be reset", duplicatedItems[0].isPurchased)
        assertNull("Duplicated item actual price must be reset", duplicatedItems[0].actualUnitPriceMinor)
    }

    @Test
    fun shoppingListRepository_duplicateList_blankNameUsesDefaultCopyFormat() = runBlocking {
        val originalList = ShoppingList(
            id = "list_blank_name",
            name = "Weekly Groceries",
            currencyCode = "USD"
        )
        listRepository.createList(originalList)

        val dupIdNull = listRepository.duplicateList("list_blank_name", null)
        assertEquals("Weekly Groceries (Copy)", listRepository.getListById(dupIdNull)?.name)

        val dupIdBlank = listRepository.duplicateList("list_blank_name", "   ")
        assertEquals("Weekly Groceries (Copy)", listRepository.getListById(dupIdBlank)?.name)
    }

    @Test
    fun shoppingListRepository_duplicateList_emptyListDuplicatedSuccessfully() = runBlocking {
        val emptyList = ShoppingList(
            id = "list_empty",
            name = "Empty Cart",
            currencyCode = "EUR"
        )
        listRepository.createList(emptyList)

        val dupId = listRepository.duplicateList("list_empty")
        val dupList = listRepository.getListById(dupId)
        assertNotNull(dupList)
        assertEquals("Empty Cart (Copy)", dupList?.name)
        val dupItems = itemRepository.getItemsForList(dupId).first()
        assertTrue("Duplicated empty list must have zero items", dupItems.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun shoppingListRepository_duplicateList_nonExistentListThrowsException() = runBlocking {
        listRepository.duplicateList("completely_unknown_id")
        Unit
    }

    @Test
    fun shoppingListRepository_duplicateList_archivedListResetsStatusToActive() = runBlocking {
        val archivedList = ShoppingList(
            id = "archived_list",
            name = "Thanksgiving Dinner",
            status = ListStatus.ARCHIVED,
            completedAt = 999999L
        )
        listRepository.createList(archivedList)

        val dupId = listRepository.duplicateList("archived_list")
        val dupList = listRepository.getListById(dupId)
        assertNotNull(dupList)
        assertEquals(ListStatus.ACTIVE, dupList?.status)
        assertNull(dupList?.completedAt)
    }

    @Test
    fun shoppingItemRepository_addItem_autoAssignsSortOrder() = runBlocking {
        val item1 = ShoppingItem(id = "item_1", listId = "list_1", name = "First Item", sortOrder = 0)
        itemRepository.addItem(item1)
        val item2 = ShoppingItem(id = "item_2", listId = "list_1", name = "Second Item", sortOrder = 0)
        itemRepository.addItem(item2)

        val items = itemRepository.getItemsForList("list_1").first()
        assertEquals(2, items.size)
        assertEquals(0, items[0].sortOrder)
        assertEquals(1, items[1].sortOrder)
    }

    @Test
    fun shoppingItemRepository_togglePurchasedAndUpdateActualPrice() = runBlocking {
        val item = ShoppingItem(id = "item_1", listId = "list_1", name = "Bread", isPurchased = false)
        itemRepository.addItem(item)

        itemRepository.toggleItemPurchased("item_1", true)
        assertTrue(itemRepository.getItemById("item_1")!!.isPurchased)

        itemRepository.updateActualPrice("item_1", 350L)
        assertEquals(350L, itemRepository.getItemById("item_1")!!.actualUnitPriceMinor)
    }

    @Test
    fun categoryRepository_ensureDefaultCategories() = runBlocking {
        assertEquals(0, categoryRepository.getCategoryCount())
        categoryRepository.ensureDefaultCategories()
        assertEquals(9, categoryRepository.getCategoryCount())

        // Calling again must not duplicate categories
        categoryRepository.ensureDefaultCategories()
        assertEquals(9, categoryRepository.getCategoryCount())
    }

    @Test
    fun historyRepository_archiveListToHistory_calculatesExactTotalsAndCreatesSnapshot() = runBlocking {
        val list = ShoppingList(
            id = "list_archive",
            name = "Supermarket Run",
            currencyCode = "USD",
            budgetMinor = 5000L,
            status = ListStatus.ACTIVE
        )
        listRepository.createList(list)

        // Item 1: Purchased, priced. 2.5 kg @ $4.00 ($10.00 -> 1000 minor)
        val item1 = ShoppingItem(
            id = "i1",
            listId = "list_archive",
            name = "Apples",
            quantity = "2.5",
            unit = "kg",
            actualUnitPriceMinor = 400L,
            isPurchased = true
        )
        // Item 2: Purchased, unpriced.
        val item2 = ShoppingItem(
            id = "i2",
            listId = "list_archive",
            name = "Bananas",
            quantity = "1",
            unit = "bunch",
            actualUnitPriceMinor = null,
            isPurchased = true
        )
        // Item 3: Not purchased
        val item3 = ShoppingItem(
            id = "i3",
            listId = "list_archive",
            name = "Cookies",
            quantity = "1",
            unit = "pack",
            actualUnitPriceMinor = 200L,
            isPurchased = false
        )

        itemRepository.addItems(listOf(item1, item2, item3))

        val historyId = historyRepository.archiveListToHistory("list_archive")
        assertNotNull(historyId)

        // Verify history snapshot
        val history = historyRepository.getHistoryById(historyId)
        assertNotNull(history)
        assertEquals("Supermarket Run", history?.listName)
        assertEquals("list_archive", history?.originalListId)
        // 2.5 * 400 = 1000 minor units
        assertEquals(1000L, history?.totalSpentMinor)
        assertEquals(2, history?.purchasedCount)
        assertEquals(1, history?.unpricedPurchasedCount)

        // Verify active list is archived
        val activeList = listRepository.getListById("list_archive")
        assertEquals(ListStatus.ARCHIVED, activeList?.status)
        assertNotNull(activeList?.completedAt)

        // Verify history detail with items
        val detail = historyRepository.getHistoryDetailById(historyId)
        assertNotNull(detail)
        assertEquals(3, detail?.items?.size)
        val historyItem1 = detail?.items?.find { it.itemName == "Apples" }
        assertEquals(1000L, historyItem1?.lineTotalMinor)
        val historyItem2 = detail?.items?.find { it.itemName == "Bananas" }
        assertNull(historyItem2?.lineTotalMinor)
    }

    // --- In-memory fake DAOs for unit testing ---

    private class FakeShoppingListDao : ShoppingListDao {
        private val lists = MutableStateFlow<Map<String, ShoppingListEntity>>(emptyMap())

        override fun getAllLists(): Flow<List<ShoppingListEntity>> =
            lists.map { it.values.sortedByDescending { l -> l.updatedAt } }

        override fun getListsByStatus(status: String): Flow<List<ShoppingListEntity>> =
            lists.map { it.values.filter { l -> l.status == status }.sortedByDescending { l -> l.updatedAt } }

        override fun getActiveLists(): Flow<List<ShoppingListEntity>> =
            getListsByStatus(ListStatus.ACTIVE.name)

        override fun observeActiveListsWithItems(): Flow<List<ShoppingListWithItems>> =
            lists.map { map ->
                map.values
                    .filter { it.status == ListStatus.ACTIVE.name }
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
        }

        override suspend fun deleteListById(id: String) {
            lists.value = lists.value - id
        }
    }

    private class FakeShoppingItemDao : ShoppingItemDao {
        private val items = MutableStateFlow<Map<String, ShoppingItemEntity>>(emptyMap())

        override fun getItemsForList(listId: String): Flow<List<ShoppingItemEntity>> =
            items.map { it.values.filter { i -> i.listId == listId }.sortedBy { i -> i.sortOrder } }

        override fun getItemsWithCategoryForList(listId: String): Flow<List<ShoppingItemWithCategory>> =
            items.map {
                it.values.filter { i -> i.listId == listId }
                    .sortedBy { i -> i.sortOrder }
                    .map { item -> ShoppingItemWithCategory(item, null) }
            }

        override fun getPurchasedItemsForList(listId: String): Flow<List<ShoppingItemEntity>> =
            items.map { it.values.filter { i -> i.listId == listId && i.isPurchased }.sortedBy { i -> i.sortOrder } }

        override fun getPendingItemsForList(listId: String): Flow<List<ShoppingItemEntity>> =
            items.map { it.values.filter { i -> i.listId == listId && !i.isPurchased }.sortedBy { i -> i.sortOrder } }

        override fun observeItemById(id: String): Flow<ShoppingItemEntity?> =
            items.map { it[id] }

        override fun observeItemWithCategory(id: String): Flow<ShoppingItemWithCategory?> =
            items.map { it[id]?.let { entity -> ShoppingItemWithCategory(entity, null) } }

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
            items.map {
                it.values.filter { i -> i.listId == listId && i.name.contains(query, ignoreCase = true) }
                    .sortedBy { i -> i.sortOrder }
                    .map { item -> ShoppingItemWithCategory(item, null) }
            }

        override suspend fun getItemById(id: String): ShoppingItemEntity? = items.value[id]

        override suspend fun getItemWithCategoryById(id: String): ShoppingItemWithCategory? =
            items.value[id]?.let { ShoppingItemWithCategory(it, null) }

        override suspend fun getItemsForListSync(listId: String): List<ShoppingItemEntity> =
            items.value.values.filter { it.listId == listId }.sortedBy { it.sortOrder }

        override suspend fun getItemsWithCategoryForListSync(listId: String): List<ShoppingItemWithCategory> =
            items.value.values.filter { it.listId == listId }.sortedBy { it.sortOrder }.map { ShoppingItemWithCategory(it, null) }

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
