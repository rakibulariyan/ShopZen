package com.barakwebsoft.shopzen.feature.lists.details

import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.domain.model.Category
import com.barakwebsoft.shopzen.domain.model.HistoryDetail
import com.barakwebsoft.shopzen.domain.model.ShoppingHistory
import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail
import com.barakwebsoft.shopzen.domain.repository.HistoryRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingItemRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeListRepository: FakeShoppingListRepository
    private lateinit var fakeItemRepository: FakeShoppingItemRepository
    private lateinit var fakeHistoryRepository: FakeHistoryRepository
    private lateinit var viewModel: ListDetailsViewModel

    private val catProduce = Category(id = "cat-produce", name = "Produce", sortOrder = 0)
    private val catDairy = Category(id = "cat-dairy", name = "Dairy", sortOrder = 1)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeListRepository = FakeShoppingListRepository()
        fakeItemRepository = FakeShoppingItemRepository()
        fakeHistoryRepository = FakeHistoryRepository()
        viewModel = ListDetailsViewModel(fakeListRepository, fakeItemRepository, fakeHistoryRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadList_observesListDetailAndCalculatesCounts() = runTest {
        val testDetail = createTestDetail(
            listId = "list-1",
            items = listOf(
                createItemDetail("item-1", "Apples", catProduce, isPurchased = true, actualPrice = 300L),
                createItemDetail("item-2", "Bananas", catProduce, isPurchased = false, estPrice = 150L),
                createItemDetail("item-3", "Milk", catDairy, isPurchased = false, estPrice = 250L)
            )
        )
        fakeListRepository.detailFlow.value = testDetail

        viewModel.loadList("list-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.listDetail)
        assertEquals(3, state.totalItemsCount)
        assertEquals(1, state.purchasedItemsCount)
        assertEquals(2, state.pendingItemsCount)
        assertEquals(2, state.availableCategories.size)
    }

    @Test
    fun toggleItemPurchased_callsRepositoryToggleWithInvertedState() = runTest {
        viewModel.toggleItemPurchased("item-xyz", currentPurchased = false)
        advanceUntilIdle()

        assertEquals(listOf("item-xyz" to true), fakeItemRepository.toggledItems)

        viewModel.toggleItemPurchased("item-xyz", currentPurchased = true)
        advanceUntilIdle()

        assertEquals(listOf("item-xyz" to true, "item-xyz" to false), fakeItemRepository.toggledItems)
    }

    @Test
    fun categoryFilter_filtersItemsCorrectly() = runTest {
        val testDetail = createTestDetail(
            listId = "list-1",
            items = listOf(
                createItemDetail("item-1", "Apples", catProduce),
                createItemDetail("item-2", "Milk", catDairy)
            )
        )
        fakeListRepository.detailFlow.value = testDetail

        viewModel.loadList("list-1")
        advanceUntilIdle()

        // Filter by Produce
        viewModel.setCategoryFilter("cat-produce")
        val produceItems = viewModel.uiState.value.filteredAndSortedItems
        assertEquals(1, produceItems.size)
        assertEquals("Apples", produceItems.first().item.name)

        // Clear filter
        viewModel.setCategoryFilter(null)
        assertEquals(2, viewModel.uiState.value.filteredAndSortedItems.size)
    }

    @Test
    fun sortOrder_NAME_ASC_sortsAlphabetically() = runTest {
        val testDetail = createTestDetail(
            listId = "list-1",
            items = listOf(
                createItemDetail("item-1", "Zucchini", catProduce),
                createItemDetail("item-2", "Apples", catProduce),
                createItemDetail("item-3", "Milk", catDairy)
            )
        )
        fakeListRepository.detailFlow.value = testDetail

        viewModel.loadList("list-1")
        advanceUntilIdle()

        viewModel.setSortOrder(ItemSortOrder.NAME_ASC)
        val sorted = viewModel.uiState.value.filteredAndSortedItems
        assertEquals(listOf("Apples", "Milk", "Zucchini"), sorted.map { it.item.name })
    }

    @Test
    fun sortOrder_PRICE_DESC_sortsHighToLow() = runTest {
        val testDetail = createTestDetail(
            listId = "list-1",
            items = listOf(
                createItemDetail("item-1", "Cheap Item", null, estPrice = 100L),
                createItemDetail("item-2", "Expensive Item", null, estPrice = 5000L),
                createItemDetail("item-3", "Medium Item", null, estPrice = 1200L)
            )
        )
        fakeListRepository.detailFlow.value = testDetail

        viewModel.loadList("list-1")
        advanceUntilIdle()

        viewModel.setSortOrder(ItemSortOrder.PRICE_DESC)
        val sorted = viewModel.uiState.value.filteredAndSortedItems
        assertEquals(listOf("Expensive Item", "Medium Item", "Cheap Item"), sorted.map { it.item.name })
    }

    @Test
    fun sortOrder_PENDING_FIRST_sortsUnpurchasedBeforePurchased() = runTest {
        val testDetail = createTestDetail(
            listId = "list-1",
            items = listOf(
                createItemDetail("item-1", "Purchased 1", null, isPurchased = true),
                createItemDetail("item-2", "Pending 1", null, isPurchased = false),
                createItemDetail("item-3", "Purchased 2", null, isPurchased = true)
            )
        )
        fakeListRepository.detailFlow.value = testDetail

        viewModel.loadList("list-1")
        advanceUntilIdle()

        viewModel.setSortOrder(ItemSortOrder.PENDING_FIRST)
        val sorted = viewModel.uiState.value.filteredAndSortedItems
        assertFalse("First item must be unpurchased", sorted[0].item.isPurchased)
        assertEquals("Pending 1", sorted[0].item.name)
        assertTrue("Second item must be purchased", sorted[1].item.isPurchased)
    }

    @Test
    fun archiveList_callsHistoryAndSetsArchivedState() = runTest {
        viewModel.loadList("list-archive-1")
        advanceUntilIdle()

        viewModel.archiveList(keepDuplicate = false)
        advanceUntilIdle()

        assertEquals(listOf("list-archive-1"), fakeHistoryRepository.archivedListIds)
        assertTrue(viewModel.uiState.value.isListArchived)
    }

    @Test
    fun deleteList_callsRepositoryDeleteAndSetsDeletedState() = runTest {
        viewModel.loadList("list-del-1")
        advanceUntilIdle()

        viewModel.deleteList()
        advanceUntilIdle()

        assertEquals(listOf("list-del-1"), fakeListRepository.deletedListIds)
        assertTrue(viewModel.uiState.value.isListDeleted)
    }

    @Test
    fun duplicateList_callsRepositoryDuplicateAndSetsDuplicatedState() = runTest {
        viewModel.loadList("list-dup-1")
        advanceUntilIdle()

        viewModel.duplicateList("Custom Name Copy")
        advanceUntilIdle()

        assertEquals(listOf("list-dup-1"), fakeListRepository.duplicatedListIds)
        assertTrue(viewModel.uiState.value.isListDuplicated)
        assertEquals("list-dup-1-copy", viewModel.uiState.value.duplicatedListId)
    }

    private fun createTestDetail(
        listId: String,
        items: List<ShoppingItemDetail>
    ): ShoppingListDetail {
        return ShoppingListDetail(
            list = ShoppingList(
                id = listId,
                name = "Test List",
                currencyCode = "USD",
                budgetMinor = 10000L
            ),
            items = items
        )
    }

    private fun createItemDetail(
        id: String,
        name: String,
        category: Category?,
        isPurchased: Boolean = false,
        estPrice: Long? = null,
        actualPrice: Long? = null
    ): ShoppingItemDetail {
        return ShoppingItemDetail(
            item = ShoppingItem(
                id = id,
                listId = "list-1",
                name = name,
                categoryId = category?.id,
                isPurchased = isPurchased,
                estimatedUnitPriceMinor = estPrice,
                actualUnitPriceMinor = actualPrice
            ),
            category = category
        )
    }

    private class FakeShoppingListRepository : ShoppingListRepository {
        val detailFlow = MutableStateFlow<ShoppingListDetail?>(null)
        val deletedListIds = mutableListOf<String>()
        val duplicatedListIds = mutableListOf<String>()

        override fun getAllLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getListsByStatus(status: ListStatus): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getActiveLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getActiveListDetails(): Flow<List<ShoppingListDetail>> = MutableStateFlow(emptyList())
        override fun getCompletedLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getArchivedLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun observeListById(id: String): Flow<ShoppingList?> = MutableStateFlow(null)
        override fun observeListDetail(id: String): Flow<ShoppingListDetail?> = detailFlow
        override fun searchLists(query: String): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun observeActiveListCount(): Flow<Int> = MutableStateFlow(0)
        override fun observeTotalListCount(): Flow<Int> = MutableStateFlow(0)

        override suspend fun getListById(id: String): ShoppingList? = null
        override suspend fun getListDetailById(id: String): ShoppingListDetail? = detailFlow.value
        override suspend fun getActiveListCount(): Int = 0
        override suspend fun getTotalListCount(): Int = 0
        override suspend fun createList(list: ShoppingList) {}
        override suspend fun updateList(list: ShoppingList) {}
        override suspend fun updateListStatus(id: String, status: ListStatus) {}
        override suspend fun markListCompleted(id: String, completedAt: Long) {}
        override suspend fun deleteList(id: String) {
            deletedListIds.add(id)
        }
        override suspend fun duplicateList(listId: String, newName: String?): String {
            duplicatedListIds.add(listId)
            return "$listId-copy"
        }
    }

    private class FakeShoppingItemRepository : ShoppingItemRepository {
        val toggledItems = mutableListOf<Pair<String, Boolean>>()

        override fun getItemsForList(listId: String): Flow<List<ShoppingItem>> = MutableStateFlow(emptyList())
        override fun getItemsWithCategoryForList(listId: String): Flow<List<ShoppingItemDetail>> = MutableStateFlow(emptyList())
        override fun getPurchasedItemsForList(listId: String): Flow<List<ShoppingItem>> = MutableStateFlow(emptyList())
        override fun getPendingItemsForList(listId: String): Flow<List<ShoppingItem>> = MutableStateFlow(emptyList())
        override fun observeItemById(id: String): Flow<ShoppingItem?> = MutableStateFlow(null)
        override fun observeItemDetail(id: String): Flow<ShoppingItemDetail?> = MutableStateFlow(null)
        override fun observeItemCount(listId: String): Flow<Int> = MutableStateFlow(0)
        override fun observePurchasedItemCount(listId: String): Flow<Int> = MutableStateFlow(0)
        override fun searchItemsInList(listId: String, query: String): Flow<List<ShoppingItem>> = MutableStateFlow(emptyList())
        override fun searchItemsWithCategoryInList(listId: String, query: String): Flow<List<ShoppingItemDetail>> = MutableStateFlow(emptyList())

        override suspend fun getItemById(id: String): ShoppingItem? = null
        override suspend fun getItemDetailById(id: String): ShoppingItemDetail? = null
        override suspend fun getItemsForListSync(listId: String): List<ShoppingItem> = emptyList()
        override suspend fun getItemCount(listId: String): Int = 0
        override suspend fun getPurchasedItemCount(listId: String): Int = 0
        override suspend fun addItem(item: ShoppingItem) {}
        override suspend fun addItems(items: List<ShoppingItem>) {}
        override suspend fun updateItem(item: ShoppingItem) {}
        override suspend fun toggleItemPurchased(id: String, isPurchased: Boolean) {
            toggledItems.add(id to isPurchased)
        }
        override suspend fun updateActualPrice(id: String, actualUnitPriceMinor: Long?) {}
        override suspend fun updateItemSortOrder(id: String, sortOrder: Int) {}
        override suspend fun deleteItem(id: String) {}
        override suspend fun deleteItemsForList(listId: String) {}
    }

    private class FakeHistoryRepository : HistoryRepository {
        val archivedListIds = mutableListOf<String>()

        override fun getAllHistory(): Flow<List<ShoppingHistory>> = MutableStateFlow(emptyList())
        override fun observeHistoryById(id: String): Flow<ShoppingHistory?> = MutableStateFlow(null)
        override fun observeHistoryDetail(id: String): Flow<HistoryDetail?> = MutableStateFlow(null)
        override fun searchHistory(query: String): Flow<List<ShoppingHistory>> = MutableStateFlow(emptyList())
        override fun observeHistoryCount(): Flow<Int> = MutableStateFlow(0)

        override suspend fun getHistoryById(id: String): ShoppingHistory? = null
        override suspend fun getHistoryDetailById(id: String): HistoryDetail? = null
        override suspend fun getHistoryCount(): Int = 0

        override suspend fun archiveListToHistory(listId: String): String {
            archivedListIds.add(listId)
            return "history-$listId"
        }

        override suspend fun deleteHistory(id: String) {}
        override suspend fun deleteAllHistory() {}
    }
}

