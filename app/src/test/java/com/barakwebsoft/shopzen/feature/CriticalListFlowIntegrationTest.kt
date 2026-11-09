package com.barakwebsoft.shopzen.feature

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
import com.barakwebsoft.shopzen.data.repository.CategoryRepositoryImpl
import com.barakwebsoft.shopzen.data.repository.HistoryRepositoryImpl
import com.barakwebsoft.shopzen.data.repository.ShoppingItemRepositoryImpl
import com.barakwebsoft.shopzen.data.repository.ShoppingListRepositoryImpl
import com.barakwebsoft.shopzen.domain.model.AppPreferences
import com.barakwebsoft.shopzen.domain.model.Category
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import com.barakwebsoft.shopzen.domain.model.ThemePreference
import com.barakwebsoft.shopzen.domain.repository.UserPreferencesRepository
import com.barakwebsoft.shopzen.feature.home.HomeViewModel
import com.barakwebsoft.shopzen.feature.items.AddEditItemViewModel
import com.barakwebsoft.shopzen.feature.lists.AddEditListViewModel
import com.barakwebsoft.shopzen.feature.lists.details.ItemPurchaseFilter
import com.barakwebsoft.shopzen.feature.lists.details.ItemSortOrder
import com.barakwebsoft.shopzen.feature.lists.details.ListDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * End-to-end integration test validating the critical shopping list and item flows (Task SZ-027).
 * Tests the cohesive lifecycle: List creation -> Rapid item entry -> Live total calculation
 * -> Search and multi-filtering -> Purchase toggle -> Duplication with state reset -> Atomic archive.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CriticalListFlowIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeListDao: FakeShoppingListDao
    private lateinit var fakeItemDao: FakeShoppingItemDao
    private lateinit var fakeCategoryDao: FakeCategoryDao
    private lateinit var fakeHistoryDao: FakeHistoryDao

    private lateinit var listRepository: ShoppingListRepositoryImpl
    private lateinit var itemRepository: ShoppingItemRepositoryImpl
    private lateinit var categoryRepository: CategoryRepositoryImpl
    private lateinit var historyRepository: HistoryRepositoryImpl
    private lateinit var fakePrefsRepository: FakeUserPreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        fakeListDao = FakeShoppingListDao()
        fakeCategoryDao = FakeCategoryDao()
        fakeItemDao = FakeShoppingItemDao(fakeCategoryDao)
        fakeHistoryDao = FakeHistoryDao()

        val dispatcher = testDispatcher
        listRepository = ShoppingListRepositoryImpl(fakeListDao, fakeItemDao, dispatcher)
        itemRepository = ShoppingItemRepositoryImpl(fakeItemDao, dispatcher)
        categoryRepository = CategoryRepositoryImpl(fakeCategoryDao, dispatcher)
        historyRepository = HistoryRepositoryImpl(fakeHistoryDao, fakeListDao, fakeItemDao, dispatcher)
        fakePrefsRepository = FakeUserPreferencesRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun criticalFlow_completeListLifecycle_createItems_searchFilter_duplicateAndArchive() = runTest {
        // 1. Create a shopping list via AddEditListViewModel
        val addEditListVm = AddEditListViewModel(listRepository, fakePrefsRepository)
        addEditListVm.loadList(null)
        advanceUntilIdle()

        addEditListVm.onNameChange("Weekly Market Trip")
        addEditListVm.onCurrencyChange("EUR")
        addEditListVm.onBudgetChange("50.00")
        addEditListVm.saveList()
        advanceUntilIdle()

        val activeLists = listRepository.getActiveLists().first()
        assertEquals(1, activeLists.size)
        val createdList = activeLists.first()
        assertEquals("Weekly Market Trip", createdList.name)
        assertEquals("EUR", createdList.currencyCode)
        assertEquals(5000L, createdList.budgetMinor)

        val listId = createdList.id

        // 2. Add multiple items via AddEditItemViewModel
        val addEditItemVm = AddEditItemViewModel(listRepository, itemRepository, categoryRepository)

        // Item 1: Apples (Produce, 2.5 kg, 3.00 EUR each, notes)
        addEditItemVm.loadItem(listId, null)
        advanceUntilIdle()

        val produceCat = addEditItemVm.uiState.value.categories.find { it.name == "Vegetables" || it.name == "Fruits" }
            ?: addEditItemVm.uiState.value.categories.first()
        val dairyCat = addEditItemVm.uiState.value.categories.find { it.name == "Dairy" }
            ?: addEditItemVm.uiState.value.categories.last()

        addEditItemVm.onNameChange("Honeycrisp Apples")
        addEditItemVm.onQuantityChange("2.5")
        addEditItemVm.onUnitChange("kg")
        addEditItemVm.onCategoryChange(produceCat.id)
        addEditItemVm.onEstimatedPriceChange("3.00")
        addEditItemVm.onNotesChange("Organic only")
        addEditItemVm.saveItem(addAnother = false)
        advanceUntilIdle()
        assertTrue(addEditItemVm.uiState.value.isItemSaved)

        // Item 2: Milk via "Add & Create Another"
        addEditItemVm.loadItem(listId, null)
        advanceUntilIdle()

        addEditItemVm.onNameChange("Whole Milk")
        addEditItemVm.onQuantityChange("2")
        addEditItemVm.onUnitChange("L")
        addEditItemVm.onCategoryChange(dairyCat.id)
        addEditItemVm.onEstimatedPriceChange("1.50")
        addEditItemVm.saveItem(addAnother = true)
        advanceUntilIdle()

        // Verify form reset retains unit & category but resets name & price & sets quantity to 1
        assertFalse(addEditItemVm.uiState.value.isItemSaved)
        assertEquals("", addEditItemVm.uiState.value.name)
        assertEquals("1", addEditItemVm.uiState.value.quantity)
        assertEquals("L", addEditItemVm.uiState.value.unit)
        assertEquals(dairyCat.id, addEditItemVm.uiState.value.categoryId)

        // Item 3: Oatmeal (Unpriced, custom unit)
        addEditItemVm.onNameChange("Rolled Oats")
        addEditItemVm.onQuantityChange("1")
        addEditItemVm.onUnitChange("Custom...")
        addEditItemVm.onCustomUnitChange("tub")
        addEditItemVm.saveItem(addAnother = false)
        advanceUntilIdle()
        assertTrue(addEditItemVm.uiState.value.isItemSaved)

        // Verify 3 items in repository
        val itemsInDb = itemRepository.getItemsForList(listId).first()
        assertEquals(3, itemsInDb.size)

        // 3. Inspect List Details via ListDetailsViewModel
        val listDetailsVm = ListDetailsViewModel(listRepository, itemRepository, historyRepository)
        listDetailsVm.loadList(listId)
        advanceUntilIdle()

        val detailsState = listDetailsVm.uiState.value
        assertEquals(3, detailsState.totalItemsCount)
        assertEquals(3, detailsState.pendingItemsCount)
        assertEquals(0, detailsState.purchasedItemsCount)

        val detailModel = detailsState.listDetail
        assertNotNull(detailModel)
        // 2.5 * 3.00 (750 minor) + 2 * 1.50 (300 minor) = 1050 minor units
        assertEquals(1050L, detailModel?.estimatedTotalMinor)
        assertEquals(0L, detailModel?.actualSpentMinor)
        assertEquals(1, detailModel?.items?.count { it.item.estimatedUnitPriceMinor == null })

        // 4. Test Search and Multi-Filtering
        // Search by name
        listDetailsVm.onSearchQueryChange("Milk")
        assertEquals(1, listDetailsVm.uiState.value.filteredAndSortedItems.size)
        assertEquals("Whole Milk", listDetailsVm.uiState.value.filteredAndSortedItems.first().item.name)

        // Search by notes
        listDetailsVm.onSearchQueryChange("Organic")
        assertEquals(1, listDetailsVm.uiState.value.filteredAndSortedItems.size)
        assertEquals("Honeycrisp Apples", listDetailsVm.uiState.value.filteredAndSortedItems.first().item.name)

        listDetailsVm.clearSearchQuery()
        assertEquals(3, listDetailsVm.uiState.value.filteredAndSortedItems.size)

        // Category filter
        listDetailsVm.setCategoryFilter(produceCat.id)
        assertEquals(1, listDetailsVm.uiState.value.filteredAndSortedItems.size)
        assertEquals("Honeycrisp Apples", listDetailsVm.uiState.value.filteredAndSortedItems.first().item.name)
        listDetailsVm.setCategoryFilter(null)

        // 5. Purchase flow: Toggle item purchased and update actual price
        val applesItem = itemsInDb.find { it.name == "Honeycrisp Apples" }!!
        listDetailsVm.toggleItemPurchased(applesItem.id, false)
        itemRepository.updateActualPrice(applesItem.id, 280L)
        advanceUntilIdle()

        assertEquals(1, listDetailsVm.uiState.value.purchasedItemsCount)
        assertEquals(2, listDetailsVm.uiState.value.pendingItemsCount)

        // Status filtering
        listDetailsVm.setPurchaseFilter(ItemPurchaseFilter.PURCHASED)
        assertEquals(1, listDetailsVm.uiState.value.filteredAndSortedItems.size)
        assertEquals("Honeycrisp Apples", listDetailsVm.uiState.value.filteredAndSortedItems.first().item.name)

        listDetailsVm.setPurchaseFilter(ItemPurchaseFilter.PENDING)
        assertEquals(2, listDetailsVm.uiState.value.filteredAndSortedItems.size)

        listDetailsVm.resetFilters()
        assertEquals(3, listDetailsVm.uiState.value.filteredAndSortedItems.size)

        // 6. Duplicate List with State Reset
        val dupId = listRepository.duplicateList(listId, "Weekly Market Trip (Next Week)")
        advanceUntilIdle()

        val dupList = listRepository.getListById(dupId)
        assertNotNull(dupList)
        assertNotEquals(listId, dupId)
        assertEquals("Weekly Market Trip (Next Week)", dupList?.name)
        assertEquals(ListStatus.ACTIVE, dupList?.status)

        val dupItems = itemRepository.getItemsForList(dupId).first()
        assertEquals(3, dupItems.size)
        // Verify all items have new IDs, reset purchase status, and reset actual price
        assertTrue("All duplicated items must be pending", dupItems.all { !it.isPurchased })
        assertTrue("All duplicated items must have null actual price", dupItems.all { it.actualUnitPriceMinor == null })
        assertTrue("Item IDs must be distinct from originals", dupItems.none { it.id == applesItem.id })

        // 7. Archive Original List to History
        listDetailsVm.archiveList(keepDuplicate = false)
        advanceUntilIdle()

        assertTrue(listDetailsVm.uiState.value.isListArchived)
        val allHistory = historyRepository.getAllHistory().first()
        assertEquals(1, allHistory.size)
        val snapshot = allHistory.first()
        assertEquals("Weekly Market Trip", snapshot.listName)
        assertEquals("EUR", snapshot.currencyCode)
        assertEquals(1, snapshot.purchasedCount)

        // Verify original list is no longer in active lists
        val remainingActive = listRepository.getActiveLists().first()
        assertEquals(1, remainingActive.size)
        assertEquals(dupId, remainingActive.first().id)
    }

    @Test
    fun criticalFlow_itemValidation_and_errorRecovery() = runTest {
        val addEditItemVm = AddEditItemViewModel(listRepository, itemRepository, categoryRepository)
        addEditItemVm.loadItem("dummy-list", null)
        advanceUntilIdle()

        // 1. Empty name validation
        addEditItemVm.onNameChange("")
        addEditItemVm.saveItem(addAnother = false)
        assertNotNull(addEditItemVm.uiState.value.nameError)
        assertFalse(addEditItemVm.uiState.value.isItemSaved)

        // Fix name -> error cleared
        addEditItemVm.onNameChange("Valid Name")
        assertNull(addEditItemVm.uiState.value.nameError)

        // 2. Invalid quantity validation
        addEditItemVm.onQuantityChange("0")
        addEditItemVm.saveItem(addAnother = false)
        assertNotNull(addEditItemVm.uiState.value.quantityError)

        addEditItemVm.onQuantityChange("-5")
        addEditItemVm.saveItem(addAnother = false)
        assertNotNull(addEditItemVm.uiState.value.quantityError)

        addEditItemVm.onQuantityChange("abc")
        addEditItemVm.saveItem(addAnother = false)
        assertNotNull(addEditItemVm.uiState.value.quantityError)

        // Stepper recovery
        addEditItemVm.onQuantityChange("1")
        addEditItemVm.adjustQuantity(2)
        assertEquals("3", addEditItemVm.uiState.value.quantity)
        assertNull(addEditItemVm.uiState.value.quantityError)
    }

    @Test
    fun criticalFlow_deleteItem_cascadesAndUpdatesListDetails() = runTest {
        val list = ShoppingList(id = "list-del", name = "Test Delete", currencyCode = "USD")
        listRepository.createList(list)

        val item1 = com.barakwebsoft.shopzen.domain.model.ShoppingItem(
            id = "item-1",
            listId = "list-del",
            name = "Item 1",
            estimatedUnitPriceMinor = 500L
        )
        val item2 = com.barakwebsoft.shopzen.domain.model.ShoppingItem(
            id = "item-2",
            listId = "list-del",
            name = "Item 2",
            estimatedUnitPriceMinor = 700L
        )
        itemRepository.addItem(item1)
        itemRepository.addItem(item2)
        advanceUntilIdle()

        val listDetailsVm = ListDetailsViewModel(listRepository, itemRepository, historyRepository)
        listDetailsVm.loadList("list-del")
        advanceUntilIdle()

        assertEquals(2, listDetailsVm.uiState.value.totalItemsCount)

        // Delete single item
        val addEditItemVm = AddEditItemViewModel(listRepository, itemRepository, categoryRepository)
        addEditItemVm.loadItem("list-del", "item-1")
        advanceUntilIdle()
        addEditItemVm.deleteItem()
        advanceUntilIdle()

        assertTrue(addEditItemVm.uiState.value.isItemDeleted)
        advanceUntilIdle()

        val remainingItems = itemRepository.getItemsForList("list-del").first()
        assertEquals(1, remainingItems.size)
        assertEquals("item-2", remainingItems.first().id)
    }

    // ---------------- Fake In-Memory DAOs ----------------

    private class FakeShoppingListDao : ShoppingListDao {
        val lists = MutableStateFlow<Map<String, ShoppingListEntity>>(emptyMap())

        override fun getAllLists(): Flow<List<ShoppingListEntity>> =
            lists.map { it.values.toList() }

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

    private class FakeShoppingItemDao(
        private val categoryDao: FakeCategoryDao
    ) : ShoppingItemDao {
        val items = MutableStateFlow<Map<String, ShoppingItemEntity>>(emptyMap())

        override fun getItemsForList(listId: String): Flow<List<ShoppingItemEntity>> =
            items.map { it.values.filter { i -> i.listId == listId }.sortedBy { i -> i.sortOrder } }

        override fun getItemsWithCategoryForList(listId: String): Flow<List<ShoppingItemWithCategory>> =
            items.map { map ->
                map.values.filter { i -> i.listId == listId }.sortedBy { i -> i.sortOrder }.map { itemEntity ->
                    val catEntity = itemEntity.categoryId?.let { cId -> categoryDao.categories.value[cId] }
                    ShoppingItemWithCategory(itemEntity, catEntity)
                }
            }

        override fun getPurchasedItemsForList(listId: String): Flow<List<ShoppingItemEntity>> =
            items.map { it.values.filter { i -> i.listId == listId && i.isPurchased } }

        override fun getPendingItemsForList(listId: String): Flow<List<ShoppingItemEntity>> =
            items.map { it.values.filter { i -> i.listId == listId && !i.isPurchased } }

        override fun observeItemById(id: String): Flow<ShoppingItemEntity?> =
            items.map { it[id] }

        override fun observeItemWithCategory(id: String): Flow<ShoppingItemWithCategory?> =
            items.map { map ->
                map[id]?.let { itemEntity ->
                    val catEntity = itemEntity.categoryId?.let { cId -> categoryDao.categories.value[cId] }
                    ShoppingItemWithCategory(itemEntity, catEntity)
                }
            }

        override fun observeItemCount(listId: String): Flow<Int> =
            items.map { it.values.count { i -> i.listId == listId } }

        override fun observePurchasedItemCount(listId: String): Flow<Int> =
            items.map { it.values.count { i -> i.listId == listId && i.isPurchased } }

        override fun searchItemsInList(listId: String, query: String): Flow<List<ShoppingItemEntity>> =
            items.map { it.values.filter { i -> i.listId == listId && i.name.contains(query, ignoreCase = true) } }

        override fun searchItemsWithCategoryInList(listId: String, query: String): Flow<List<ShoppingItemWithCategory>> =
            items.map { map ->
                map.values.filter { i -> i.listId == listId && i.name.contains(query, ignoreCase = true) }.map { itemEntity ->
                    val catEntity = itemEntity.categoryId?.let { cId -> categoryDao.categories.value[cId] }
                    ShoppingItemWithCategory(itemEntity, catEntity)
                }
            }

        override suspend fun getItemById(id: String): ShoppingItemEntity? = items.value[id]

        override suspend fun getItemWithCategoryById(id: String): ShoppingItemWithCategory? =
            items.value[id]?.let { itemEntity ->
                val catEntity = itemEntity.categoryId?.let { cId -> categoryDao.categories.value[cId] }
                ShoppingItemWithCategory(itemEntity, catEntity)
            }

        override suspend fun getItemsForListSync(listId: String): List<ShoppingItemEntity> =
            items.value.values.filter { it.listId == listId }.sortedBy { it.sortOrder }

        override suspend fun getItemsWithCategoryForListSync(listId: String): List<ShoppingItemWithCategory> =
            items.value.values.filter { it.listId == listId }.sortedBy { it.sortOrder }.map { itemEntity ->
                val catEntity = itemEntity.categoryId?.let { cId -> categoryDao.categories.value[cId] }
                ShoppingItemWithCategory(itemEntity, catEntity)
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
        val categories = MutableStateFlow<Map<String, CategoryEntity>>(DefaultCategories.ALL.associateBy { it.id })

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
        val histories = MutableStateFlow<Map<String, ShoppingHistoryEntity>>(emptyMap())
        val historyItems = MutableStateFlow<Map<String, HistoryItemEntity>>(emptyMap())

        override fun getAllHistory(): Flow<List<ShoppingHistoryEntity>> =
            histories.map { it.values.sortedByDescending { h -> h.completedAt } }

        override fun observeHistoryById(id: String): Flow<ShoppingHistoryEntity?> =
            histories.map { it[id] }

        override fun observeHistoryWithItems(id: String): Flow<HistoryWithItems?> =
            histories.map { map ->
                map[id]?.let { history ->
                    val items = historyItems.value.values.filter { it.historyId == id }
                    HistoryWithItems(history, items)
                }
            }

        override fun searchHistory(query: String): Flow<List<ShoppingHistoryEntity>> =
            histories.map { map ->
                map.values.filter { it.listName.contains(query, ignoreCase = true) }
            }

        override fun observeHistoryCount(): Flow<Int> = histories.map { it.size }
        override suspend fun getHistoryById(id: String): ShoppingHistoryEntity? = histories.value[id]
        override suspend fun getHistoryWithItemsById(id: String): HistoryWithItems? =
            histories.value[id]?.let { HistoryWithItems(it, historyItems.value.values.filter { i -> i.historyId == id }) }

        override suspend fun getHistoryItems(historyId: String): List<HistoryItemEntity> =
            historyItems.value.values.filter { it.historyId == historyId }

        override suspend fun getHistoryCount(): Int = histories.value.size

        override suspend fun insertHistory(history: ShoppingHistoryEntity) {
            histories.value = histories.value + (history.id to history)
        }

        override suspend fun insertHistoryItems(items: List<HistoryItemEntity>) {
            this.historyItems.value = this.historyItems.value + items.associateBy { it.id }
        }

        override suspend fun deleteHistory(history: ShoppingHistoryEntity) {
            histories.value = histories.value - history.id
            historyItems.value = historyItems.value.filterValues { it.historyId != history.id }
        }

        override suspend fun deleteHistoryById(id: String) {
            histories.value = histories.value - id
            historyItems.value = historyItems.value.filterValues { it.historyId != id }
        }

        override suspend fun deleteAllHistory() {
            histories.value = emptyMap()
            historyItems.value = emptyMap()
        }
    }

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        private val _prefs = MutableStateFlow(AppPreferences())

        override fun getPreferences(): Flow<AppPreferences> = _prefs

        override suspend fun setThemePreference(theme: ThemePreference) {
            _prefs.value = _prefs.value.copy(themePreference = theme)
        }

        override suspend fun setDefaultCurrency(currencyCode: String) {
            _prefs.value = _prefs.value.copy(defaultCurrencyCode = currencyCode)
        }

        override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
            _prefs.value = _prefs.value.copy(hapticFeedbackEnabled = enabled)
        }

        override suspend fun setShowCompletedItems(show: Boolean) {
            _prefs.value = _prefs.value.copy(showCompletedItems = show)
        }
    }
}
