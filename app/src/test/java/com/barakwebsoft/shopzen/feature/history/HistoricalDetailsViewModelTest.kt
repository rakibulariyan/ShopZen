package com.barakwebsoft.shopzen.feature.history

import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.domain.model.HistoryDetail
import com.barakwebsoft.shopzen.domain.model.HistoryItem
import com.barakwebsoft.shopzen.domain.model.ShoppingHistory
import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail
import com.barakwebsoft.shopzen.domain.repository.HistoryRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingItemRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import com.barakwebsoft.shopzen.feature.history.details.HistoricalDetailsViewModel
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
class HistoricalDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeHistoryRepository: FakeHistoryRepository
    private lateinit var fakeListRepository: FakeShoppingListRepository
    private lateinit var fakeItemRepository: FakeShoppingItemRepository
    private lateinit var viewModel: HistoricalDetailsViewModel

    private val sampleHistory = ShoppingHistory(
        id = "hist_1",
        listName = "Weekly Groceries",
        currencyCode = "USD",
        budgetMinor = 5000L,
        totalSpentMinor = 4250L,
        purchasedCount = 2,
        unpricedPurchasedCount = 0
    )

    private val sampleItems = listOf(
        HistoryItem(id = "hi1", historyId = "hist_1", itemName = "Apples", quantity = "2", unit = "kg", actualUnitPriceMinor = 1500L, lineTotalMinor = 3000L, isPurchased = true),
        HistoryItem(id = "hi2", historyId = "hist_1", itemName = "Milk", quantity = "1", unit = "L", actualUnitPriceMinor = 1250L, lineTotalMinor = 1250L, isPurchased = true)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeHistoryRepository = FakeHistoryRepository()
        fakeListRepository = FakeShoppingListRepository()
        fakeItemRepository = FakeShoppingItemRepository()

        fakeHistoryRepository.emitDetail(HistoryDetail(sampleHistory, sampleItems))

        viewModel = HistoricalDetailsViewModel(
            historyId = "hist_1",
            historyRepository = fakeHistoryRepository,
            shoppingListRepository = fakeListRepository,
            shoppingItemRepository = fakeItemRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsHistoricalDetail_emitsUiState() = runTest(testDispatcher) {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.historyDetail)
        assertEquals("Weekly Groceries", state.historyDetail?.history?.listName)
        assertEquals(2, state.historyDetail?.items?.size)
        assertEquals(4250L, state.historyDetail?.history?.totalSpentMinor)
    }

    @Test
    fun deleteHistory_updatesIsDeletedTrue() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.showDeleteConfirmation()
        assertTrue(viewModel.uiState.value.showDeleteDialog)

        viewModel.dismissDeleteConfirmation()
        assertFalse(viewModel.uiState.value.showDeleteDialog)

        viewModel.deleteHistory()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isDeleted)
        assertEquals("hist_1", fakeHistoryRepository.lastDeletedId)
    }

    @Test
    fun duplicateAsNewList_createsActiveListAndCopiesItemsWithResetPurchasedState() = runTest(testDispatcher) {
        advanceUntilIdle()

        var createdListId: String? = null
        viewModel.duplicateAsNewList { id ->
            createdListId = id
        }
        advanceUntilIdle()

        assertNotNull(createdListId)
        val createdList = fakeListRepository.createdList
        assertNotNull(createdList)
        assertEquals("Weekly Groceries (Copy)", createdList?.name)
        assertEquals(ListStatus.ACTIVE, createdList?.status)
        assertEquals("USD", createdList?.currencyCode)
        assertEquals(5000L, createdList?.budgetMinor)

        // Verify items were created with new IDs and reset purchase state
        val addedItems = fakeItemRepository.addedItems
        assertEquals(2, addedItems.size)
        assertEquals("Apples", addedItems[0].name)
        assertFalse("Duplicated item purchase state must be reset to false", addedItems[0].isPurchased)
        assertNull("Actual price must be reset to null", addedItems[0].actualUnitPriceMinor)
        assertEquals(1500L, addedItems[0].estimatedUnitPriceMinor) // previous actual becomes estimated!

        assertEquals("Milk", addedItems[1].name)
        assertFalse(addedItems[1].isPurchased)
        assertNull(addedItems[1].actualUnitPriceMinor)
        assertEquals(1250L, addedItems[1].estimatedUnitPriceMinor)
    }

    private class FakeHistoryRepository : HistoryRepository {
        private val detailFlow = MutableStateFlow<HistoryDetail?>(null)
        var lastDeletedId: String? = null

        fun emitDetail(detail: HistoryDetail) {
            detailFlow.value = detail
        }

        override fun getAllHistory(): Flow<List<ShoppingHistory>> = MutableStateFlow(emptyList())
        override fun observeHistoryById(id: String): Flow<ShoppingHistory?> = MutableStateFlow(detailFlow.value?.history)
        override fun observeHistoryDetail(id: String): Flow<HistoryDetail?> = detailFlow
        override fun searchHistory(query: String): Flow<List<ShoppingHistory>> = MutableStateFlow(emptyList())
        override fun observeHistoryCount(): Flow<Int> = MutableStateFlow(1)

        override suspend fun getHistoryById(id: String): ShoppingHistory? = detailFlow.value?.history
        override suspend fun getHistoryDetailById(id: String): HistoryDetail? = detailFlow.value
        override suspend fun getHistoryCount(): Int = 1
        override suspend fun archiveListToHistory(listId: String): String = "dummy"
        override suspend fun deleteHistory(id: String) {
            lastDeletedId = id
            detailFlow.value = null
        }
        override suspend fun deleteAllHistory() {}
    }

    private class FakeShoppingListRepository : ShoppingListRepository {
        var createdList: ShoppingList? = null

        override fun getAllLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getListsByStatus(status: ListStatus): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getActiveLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getActiveListDetails(): Flow<List<ShoppingListDetail>> = MutableStateFlow(emptyList())
        override fun getCompletedLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getArchivedLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun observeListById(id: String): Flow<ShoppingList?> = MutableStateFlow(null)
        override fun observeListDetail(id: String): Flow<ShoppingListDetail?> = MutableStateFlow(null)
        override fun searchLists(query: String): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun observeActiveListCount(): Flow<Int> = MutableStateFlow(0)
        override fun observeTotalListCount(): Flow<Int> = MutableStateFlow(0)

        override suspend fun getListById(id: String): ShoppingList? = createdList
        override suspend fun getListDetailById(id: String): ShoppingListDetail? = null
        override suspend fun getActiveListCount(): Int = 0
        override suspend fun getTotalListCount(): Int = 0
        override suspend fun createList(list: ShoppingList) {
            createdList = list
        }
        override suspend fun updateList(list: ShoppingList) {}
        override suspend fun updateListStatus(id: String, status: ListStatus) {}
        override suspend fun markListCompleted(id: String, completedAt: Long) {}
        override suspend fun deleteList(id: String) {}
        override suspend fun duplicateList(listId: String, newName: String?): String = "dup_id"
    }

    private class FakeShoppingItemRepository : ShoppingItemRepository {
        val addedItems = mutableListOf<ShoppingItem>()

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
        override suspend fun addItem(item: ShoppingItem) {
            addedItems.add(item)
        }
        override suspend fun addItems(items: List<ShoppingItem>) {
            addedItems.addAll(items)
        }
        override suspend fun updateItem(item: ShoppingItem) {}
        override suspend fun toggleItemPurchased(id: String, isPurchased: Boolean) {}
        override suspend fun updateActualPrice(id: String, actualUnitPriceMinor: Long?) {}
        override suspend fun updateItemSortOrder(id: String, sortOrder: Int) {}
        override suspend fun deleteItem(id: String) {}
        override suspend fun deleteItemsForList(listId: String) {}
    }
}

