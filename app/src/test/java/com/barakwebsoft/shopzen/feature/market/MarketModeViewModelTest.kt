package com.barakwebsoft.shopzen.feature.market

import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.domain.model.Category
import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail
import com.barakwebsoft.shopzen.domain.repository.ShoppingItemRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
class MarketModeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeListRepository: FakeShoppingListRepository
    private lateinit var fakeItemRepository: FakeShoppingItemRepository
    private lateinit var viewModel: MarketModeViewModel

    private val catProduce = Category(id = "cat-produce", name = "Produce", sortOrder = 0)
    private val catDairy = Category(id = "cat-dairy", name = "Dairy", sortOrder = 1)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeListRepository = FakeShoppingListRepository()
        fakeItemRepository = FakeShoppingItemRepository()
        viewModel = MarketModeViewModel(fakeListRepository, fakeItemRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadList_observesListDetailAndCalculatesCounts() = runTest {
        val testDetail = createTestDetail(
            listId = "list-market-1",
            items = listOf(
                createItemDetail("item-1", "Apples", catProduce, isPurchased = true, actualPrice = 280L),
                createItemDetail("item-2", "Bananas", catProduce, isPurchased = false, estPrice = 120L),
                createItemDetail("item-3", "Whole Milk", catDairy, isPurchased = false, estPrice = 250L)
            )
        )
        fakeListRepository.detailFlow.value = testDetail

        viewModel.loadList("list-market-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.listDetail)
        assertEquals(3, state.totalItemsCount)
        assertEquals(1, state.purchasedItemsCount)
        assertEquals(2, state.pendingItemsCount)
        assertEquals(33, state.progressPercent) // 1/3 = 33%
        assertEquals(2, state.availableCategories.size)
    }

    @Test
    fun toggleItemPurchased_updatesRepositoryAndEmitsEvent() = runTest {
        val testDetail = createTestDetail(
            listId = "list-market-1",
            items = listOf(
                createItemDetail("item-1", "Bananas", catProduce, isPurchased = false)
            )
        )
        fakeListRepository.detailFlow.value = testDetail
        viewModel.loadList("list-market-1")
        advanceUntilIdle()

        var emittedEvent: MarketModeEvent? = null
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.events.collect { emittedEvent = it }
        }

        viewModel.toggleItemPurchased("item-1", currentPurchased = false)
        advanceUntilIdle()

        assertEquals(listOf("item-1" to true), fakeItemRepository.toggledItems)

        val lastAction = viewModel.uiState.value.lastPurchaseToggleAction
        assertNotNull(lastAction)
        assertEquals("item-1", lastAction?.itemId)
        assertEquals("Bananas", lastAction?.itemName)
        assertFalse(lastAction?.wasPurchased ?: true)
        assertTrue(lastAction?.newPurchased ?: false)

        assertNotNull(emittedEvent)
        assertTrue(emittedEvent is MarketModeEvent.ItemPurchaseToggled)
        val toggleEvent = emittedEvent as MarketModeEvent.ItemPurchaseToggled
        assertEquals("Purchased Bananas", toggleEvent.message)
    }

    @Test
    fun toggleItemPurchased_whenPurchased_marksAsPending() = runTest {
        val testDetail = createTestDetail(
            listId = "list-market-1",
            items = listOf(
                createItemDetail("item-apples", "Apples", catProduce, isPurchased = true)
            )
        )
        fakeListRepository.detailFlow.value = testDetail
        viewModel.loadList("list-market-1")
        advanceUntilIdle()

        var emittedEvent: MarketModeEvent? = null
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.events.collect { emittedEvent = it }
        }

        viewModel.toggleItemPurchased("item-apples", currentPurchased = true)
        advanceUntilIdle()

        assertEquals(listOf("item-apples" to false), fakeItemRepository.toggledItems)

        val lastAction = viewModel.uiState.value.lastPurchaseToggleAction
        assertNotNull(lastAction)
        assertTrue(lastAction?.wasPurchased ?: false)
        assertFalse(lastAction?.newPurchased ?: true)

        assertNotNull(emittedEvent)
        val toggleEvent = emittedEvent as MarketModeEvent.ItemPurchaseToggled
        assertEquals("Marked Apples as pending", toggleEvent.message)
    }

    @Test
    fun undoLastToggle_revertsItemToPreviousStateAndClearsAction() = runTest {
        val testDetail = createTestDetail(
            listId = "list-market-1",
            items = listOf(
                createItemDetail("item-bread", "Sourdough Bread", null, isPurchased = false)
            )
        )
        fakeListRepository.detailFlow.value = testDetail
        viewModel.loadList("list-market-1")
        advanceUntilIdle()

        viewModel.toggleItemPurchased("item-bread", currentPurchased = false)
        advanceUntilIdle()
        assertEquals(listOf("item-bread" to true), fakeItemRepository.toggledItems)

        viewModel.undoLastToggle()
        advanceUntilIdle()

        assertEquals(listOf("item-bread" to true, "item-bread" to false), fakeItemRepository.toggledItems)
        assertNull(viewModel.uiState.value.lastPurchaseToggleAction)
    }

    @Test
    fun hapticToggle_togglesHapticState() {
        assertTrue("Haptics should be enabled by default", viewModel.uiState.value.isHapticEnabled)

        viewModel.toggleHaptic()
        assertFalse("Haptics should be disabled after toggle", viewModel.uiState.value.isHapticEnabled)

        viewModel.toggleHaptic()
        assertTrue("Haptics should be re-enabled", viewModel.uiState.value.isHapticEnabled)
    }

    @Test
    fun keepScreenOnToggle_togglesKeepScreenOnState() {
        assertTrue("Keep screen on should be enabled by default for market mode", viewModel.uiState.value.isKeepScreenOn)

        viewModel.toggleKeepScreenOn()
        assertFalse(viewModel.uiState.value.isKeepScreenOn)

        viewModel.toggleKeepScreenOn()
        assertTrue(viewModel.uiState.value.isKeepScreenOn)
    }

    @Test
    fun categoryFilter_filtersItemsCorrectly() = runTest {
        val testDetail = createTestDetail(
            listId = "list-cat-1",
            items = listOf(
                createItemDetail("1", "Apples", catProduce),
                createItemDetail("2", "Milk", catDairy)
            )
        )
        fakeListRepository.detailFlow.value = testDetail
        viewModel.loadList("list-cat-1")
        advanceUntilIdle()

        viewModel.setCategoryFilter("cat-produce")
        val filtered = viewModel.uiState.value.filteredItems
        assertEquals(1, filtered.size)
        assertEquals("Apples", filtered.first().item.name)

        viewModel.setCategoryFilter(null)
        assertEquals(2, viewModel.uiState.value.filteredItems.size)
    }

    @Test
    fun unpricedPurchasedItemsCount_accuratelyIdentifiesUnpricedInCart() = runTest {
        val testDetail = createTestDetail(
            listId = "list-unpriced-1",
            items = listOf(
                // Purchased with actual price
                createItemDetail("1", "Paid Item", catProduce, isPurchased = true, actualPrice = 100L),
                // Purchased but NO actual price
                createItemDetail("2", "Unpriced Item", catProduce, isPurchased = true, actualPrice = null),
                // Pending item (not in cart)
                createItemDetail("3", "Pending Item", catDairy, isPurchased = false, actualPrice = null)
            )
        )
        fakeListRepository.detailFlow.value = testDetail
        viewModel.loadList("list-unpriced-1")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.unpricedPurchasedItemsCount)
    }

    @Test
    fun finishShopping_setsFinishedState() {
        assertFalse(viewModel.uiState.value.isFinished)
        viewModel.finishShopping()
        assertTrue(viewModel.uiState.value.isFinished)
    }

    @Test
    fun openPriceEntry_and_closePriceEntry_updatesActivePriceItem() = runTest {
        val testDetail = createTestDetail(
            listId = "list-price-1",
            items = listOf(
                createItemDetail("item-apple", "Honeycrisp Apples", catProduce, isPurchased = false)
            )
        )
        fakeListRepository.detailFlow.value = testDetail
        viewModel.loadList("list-price-1")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.priceEntryItemId)
        assertNull(viewModel.uiState.value.activePriceEntryItem)

        viewModel.openPriceEntry("item-apple")
        assertEquals("item-apple", viewModel.uiState.value.priceEntryItemId)
        assertEquals("Honeycrisp Apples", viewModel.uiState.value.activePriceEntryItem?.item?.name)

        viewModel.closePriceEntry()
        assertNull(viewModel.uiState.value.priceEntryItemId)
        assertNull(viewModel.uiState.value.activePriceEntryItem)
    }

    @Test
    fun saveActualPrice_updatesPriceInRepository_and_marksPurchased() = runTest {
        viewModel.saveActualPrice("item-1", actualUnitPriceMinor = 250L, markPurchased = true)
        advanceUntilIdle()

        assertEquals(listOf("item-1" to 250L), fakeItemRepository.actualPrices)
        assertEquals(listOf("item-1" to true), fakeItemRepository.toggledItems)
        assertNull(viewModel.uiState.value.priceEntryItemId)
    }

    @Test
    fun saveActualPrice_withNullPrice_marksUnpricedInCart() = runTest {
        viewModel.saveActualPrice("item-2", actualUnitPriceMinor = null, markPurchased = true)
        advanceUntilIdle()

        assertEquals(listOf("item-2" to null), fakeItemRepository.actualPrices)
        assertEquals(listOf("item-2" to true), fakeItemRepository.toggledItems)
    }

    @Test
    fun saveActualPrice_withoutMarkingPurchased_onlyUpdatesPrice() = runTest {
        viewModel.saveActualPrice("item-3", actualUnitPriceMinor = 400L, markPurchased = false)
        advanceUntilIdle()

        assertEquals(listOf("item-3" to 400L), fakeItemRepository.actualPrices)
        assertTrue(fakeItemRepository.toggledItems.isEmpty())
    }

    private fun createTestDetail(
        listId: String,
        items: List<ShoppingItemDetail>
    ): ShoppingListDetail {
        val list = ShoppingList(
            id = listId,
            name = "Market Mode List",
            currencyCode = "USD",
            budgetMinor = 1000L,
            status = ListStatus.ACTIVE,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        return ShoppingListDetail(list = list, items = items)
    }

    private fun createItemDetail(
        id: String,
        name: String,
        category: Category?,
        isPurchased: Boolean = false,
        estPrice: Long? = null,
        actualPrice: Long? = null,
        quantity: String = "1",
        unit: String = "pcs",
        notes: String? = null
    ): ShoppingItemDetail {
        return ShoppingItemDetail(
            item = ShoppingItem(
                id = id,
                listId = "list-1",
                name = name,
                quantity = quantity,
                unit = unit,
                categoryId = category?.id,
                estimatedUnitPriceMinor = estPrice,
                actualUnitPriceMinor = actualPrice,
                isPurchased = isPurchased,
                notes = notes,
                sortOrder = 0,
                createdAt = 1000L,
                updatedAt = 1000L
            ),
            category = category
        )
    }

    private class FakeShoppingListRepository : ShoppingListRepository {
        val detailFlow = MutableStateFlow<ShoppingListDetail?>(null)

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
        override suspend fun deleteList(id: String) {}
        override suspend fun duplicateList(listId: String, newName: String?): String = "$listId-copy"
    }

    private class FakeShoppingItemRepository : ShoppingItemRepository {
        val toggledItems = mutableListOf<Pair<String, Boolean>>()
        val actualPrices = mutableListOf<Pair<String, Long?>>()

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
        override suspend fun updateActualPrice(id: String, actualUnitPriceMinor: Long?) {
            actualPrices.add(id to actualUnitPriceMinor)
        }
        override suspend fun updateItemSortOrder(id: String, sortOrder: Int) {}
        override suspend fun deleteItem(id: String) {}
        override suspend fun deleteItemsForList(listId: String) {}
    }
}

