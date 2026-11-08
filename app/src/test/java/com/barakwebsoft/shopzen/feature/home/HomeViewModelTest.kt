package com.barakwebsoft.shopzen.feature.home

import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.domain.model.HistoryDetail
import com.barakwebsoft.shopzen.domain.model.ShoppingHistory
import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail
import com.barakwebsoft.shopzen.domain.repository.HistoryRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeShoppingListRepository
    private lateinit var fakeHistoryRepository: FakeHistoryRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeShoppingListRepository()
        fakeHistoryRepository = FakeHistoryRepository()
        viewModel = HomeViewModel(fakeRepository, fakeHistoryRepository, SharingStarted.Eagerly)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLoadingInitially() {
        assertTrue("Initial state must be loading", viewModel.uiState.value.isLoading)
    }

    @Test
    fun emptyListsEmitted_stateTransitionsToTrueEmpty() = runTest(testDispatcher) {
        fakeRepository.emit(emptyList())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Loading must be false after emission", state.isLoading)
        assertTrue("isTrueEmpty must be true when no lists exist", state.isTrueEmpty)
        assertEquals(0, state.totalActiveLists)
        assertEquals(emptyList<ShoppingListDetail>(), state.filteredLists)
    }

    @Test
    fun activeListsEmitted_stateLoadsLists() = runTest(testDispatcher) {
        val lists = listOf(
            createTestListDetail("list-1", "Weekly Groceries", 5000L, 10, 5),
            createTestListDetail("list-2", "Hardware Supplies", null, 3, 0)
        )
        fakeRepository.emit(lists)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Loading must be false after emission", state.isLoading)
        assertFalse("isTrueEmpty must be false when lists exist", state.isTrueEmpty)
        assertEquals(2, state.totalActiveLists)
        assertEquals(2, state.filteredLists.size)
        assertEquals("Weekly Groceries", state.filteredLists[0].list.name)
    }

    @Test
    fun searchQuery_filtersListsByNameCaseInsensitively() = runTest(testDispatcher) {
        val lists = listOf(
            createTestListDetail("list-1", "Weekly Groceries", 5000L, 10, 5),
            createTestListDetail("list-2", "Weekend BBQ", 8000L, 6, 2),
            createTestListDetail("list-3", "Monthly Pharmacy", 2000L, 4, 4)
        )
        fakeRepository.emit(lists)
        advanceUntilIdle()

        // Search for "groceries" (lowercase against mixed case)
        viewModel.onSearchQueryChange("groceries")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.filteredLists.size)
        assertEquals("Weekly Groceries", state.filteredLists[0].list.name)
        assertEquals("groceries", state.searchQuery)
    }

    @Test
    fun searchQuery_clearedQuery_returnsAllLists() = runTest(testDispatcher) {
        val lists = listOf(
            createTestListDetail("list-1", "Weekly Groceries", 5000L, 10, 5),
            createTestListDetail("list-2", "Weekend BBQ", 8000L, 6, 2)
        )
        fakeRepository.emit(lists)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("BBQ")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredLists.size)

        // Clear query
        viewModel.clearSearchQuery()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertEquals(2, state.filteredLists.size)
    }

    @Test
    fun searchQuery_noMatches_returnsEmptyFilteredList() = runTest(testDispatcher) {
        val lists = listOf(
            createTestListDetail("list-1", "Weekly Groceries", 5000L, 10, 5)
        )
        fakeRepository.emit(lists)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("NonExistentItem")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Filtered lists must be empty for unmatched search", state.filteredLists.isEmpty())
        assertEquals(1, state.totalActiveLists)
    }

    @Test
    fun archiveList_withoutDuplicate_archivesSuccessfully() = runTest {
        viewModel.archiveList("list-123", keepDuplicateCopy = false)
        advanceUntilIdle()

        assertEquals(listOf("list-123"), fakeHistoryRepository.archivedListIds)
        assertTrue(fakeRepository.duplicatedListIds.isEmpty())
    }

    @Test
    fun archiveList_withDuplicate_duplicatesAndArchives() = runTest {
        viewModel.archiveList("list-123", keepDuplicateCopy = true)
        advanceUntilIdle()

        assertEquals(listOf("list-123"), fakeHistoryRepository.archivedListIds)
        assertEquals(listOf("list-123"), fakeRepository.duplicatedListIds)
    }

    @Test
    fun deleteList_callsRepositoryDelete() = runTest {
        viewModel.deleteList("list-delete-99")
        advanceUntilIdle()

        assertEquals(listOf("list-delete-99"), fakeRepository.deletedListIds)
    }

    private fun createTestListDetail(
        id: String,
        name: String,
        budget: Long?,
        totalItems: Int,
        purchasedItems: Int
    ): ShoppingListDetail {
        val items = (1..totalItems).map { idx ->
            ShoppingItemDetail(
                item = ShoppingItem(
                    id = "$id-item-$idx",
                    listId = id,
                    name = "Item $idx",
                    isPurchased = idx <= purchasedItems
                )
            )
        }
        return ShoppingListDetail(
            list = ShoppingList(
                id = id,
                name = name,
                currencyCode = "USD",
                budgetMinor = budget,
                status = ListStatus.ACTIVE,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            items = items
        )
    }

    private class FakeShoppingListRepository : ShoppingListRepository {
        private val activeListsFlow = MutableStateFlow<List<ShoppingListDetail>>(emptyList())
        val deletedListIds = mutableListOf<String>()
        val duplicatedListIds = mutableListOf<String>()

        fun emit(lists: List<ShoppingListDetail>) {
            activeListsFlow.value = lists
        }

        override fun getAllLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getListsByStatus(status: ListStatus): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getActiveLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getActiveListDetails(): Flow<List<ShoppingListDetail>> = activeListsFlow
        override fun getCompletedLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getArchivedLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun observeListById(id: String): Flow<ShoppingList?> = MutableStateFlow(null)
        override fun observeListDetail(id: String): Flow<ShoppingListDetail?> = MutableStateFlow(null)
        override fun searchLists(query: String): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun observeActiveListCount(): Flow<Int> = MutableStateFlow(0)
        override fun observeTotalListCount(): Flow<Int> = MutableStateFlow(0)

        override suspend fun getListById(id: String): ShoppingList? = null
        override suspend fun getListDetailById(id: String): ShoppingListDetail? = null
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

