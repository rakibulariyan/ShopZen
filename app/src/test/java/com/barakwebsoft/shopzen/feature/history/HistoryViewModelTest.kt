package com.barakwebsoft.shopzen.feature.history

import com.barakwebsoft.shopzen.domain.model.HistoryDetail
import com.barakwebsoft.shopzen.domain.model.ShoppingHistory
import com.barakwebsoft.shopzen.domain.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeHistoryRepository: FakeHistoryRepository
    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeHistoryRepository = FakeHistoryRepository()
        viewModel = HistoryViewModel(
            historyRepository = fakeHistoryRepository,
            sharingStarted = SharingStarted.Eagerly
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLoadingInitially() {
        val vm = HistoryViewModel(
            historyRepository = fakeHistoryRepository,
            sharingStarted = SharingStarted.Lazily
        )
        assertTrue(vm.uiState.value.isLoading)
    }

    @Test
    fun observeTrips_emitsAllHistoryTrips() = runTest(testDispatcher) {
        val trips = listOf(
            ShoppingHistory(id = "h1", listName = "Weekly Market", currencyCode = "USD", totalSpentMinor = 4500L, purchasedCount = 5, unpricedPurchasedCount = 0),
            ShoppingHistory(id = "h2", listName = "Hardware Store", currencyCode = "USD", totalSpentMinor = 12000L, purchasedCount = 3, unpricedPurchasedCount = 1)
        )
        fakeHistoryRepository.emitTrips(trips)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.historyTrips.size)
        assertEquals(2, state.totalTripsCount)
        assertEquals("Weekly Market", state.historyTrips[0].listName)
    }

    @Test
    fun searchQuery_filtersTripsByListNameCaseInsensitively() = runTest(testDispatcher) {
        val trips = listOf(
            ShoppingHistory(id = "h1", listName = "Weekly Market", currencyCode = "USD", totalSpentMinor = 4500L, purchasedCount = 5, unpricedPurchasedCount = 0),
            ShoppingHistory(id = "h2", listName = "Hardware Store", currencyCode = "USD", totalSpentMinor = 12000L, purchasedCount = 3, unpricedPurchasedCount = 1),
            ShoppingHistory(id = "h3", listName = "Farmers Market", currencyCode = "USD", totalSpentMinor = 2500L, purchasedCount = 2, unpricedPurchasedCount = 0)
        )
        fakeHistoryRepository.emitTrips(trips)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("market")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.historyTrips.size)
        assertTrue(state.historyTrips.all { it.listName.contains("market", ignoreCase = true) })
        assertEquals(3, state.totalTripsCount) // total count remains intact
    }

    @Test
    fun clearSearchQuery_restoresAllTrips() = runTest(testDispatcher) {
        val trips = listOf(
            ShoppingHistory(id = "h1", listName = "Weekly Market", currencyCode = "USD", totalSpentMinor = 4500L, purchasedCount = 5, unpricedPurchasedCount = 0),
            ShoppingHistory(id = "h2", listName = "Hardware Store", currencyCode = "USD", totalSpentMinor = 12000L, purchasedCount = 3, unpricedPurchasedCount = 1)
        )
        fakeHistoryRepository.emitTrips(trips)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("hardware")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.historyTrips.size)

        viewModel.clearSearchQuery()
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.historyTrips.size)
        assertEquals("", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun deleteTripFlow_requestsConfirmationAndDeletes() = runTest(testDispatcher) {
        val trip = ShoppingHistory(id = "h1", listName = "Weekly Market", currencyCode = "USD", totalSpentMinor = 4500L, purchasedCount = 5, unpricedPurchasedCount = 0)
        fakeHistoryRepository.emitTrips(listOf(trip))
        advanceUntilIdle()

        viewModel.requestDeleteTrip(trip)
        advanceUntilIdle()
        assertEquals(trip, viewModel.uiState.value.tripToDelete)

        viewModel.dismissDeleteDialog()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.tripToDelete)

        viewModel.requestDeleteTrip(trip)
        advanceUntilIdle()
        viewModel.confirmDeleteTrip()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.tripToDelete)
        assertEquals("h1", fakeHistoryRepository.lastDeletedId)
    }

    private class FakeHistoryRepository : HistoryRepository {
        private val tripsFlow = MutableStateFlow<List<ShoppingHistory>>(emptyList())
        var lastDeletedId: String? = null

        fun emitTrips(trips: List<ShoppingHistory>) {
            tripsFlow.value = trips
        }

        override fun getAllHistory(): Flow<List<ShoppingHistory>> = tripsFlow
        override fun observeHistoryById(id: String): Flow<ShoppingHistory?> = MutableStateFlow(null)
        override fun observeHistoryDetail(id: String): Flow<HistoryDetail?> = MutableStateFlow(null)
        override fun searchHistory(query: String): Flow<List<ShoppingHistory>> = tripsFlow
        override fun observeHistoryCount(): Flow<Int> = MutableStateFlow(tripsFlow.value.size)

        override suspend fun getHistoryById(id: String): ShoppingHistory? = tripsFlow.value.find { it.id == id }
        override suspend fun getHistoryDetailById(id: String): HistoryDetail? = null
        override suspend fun getHistoryCount(): Int = tripsFlow.value.size
        override suspend fun archiveListToHistory(listId: String): String = "dummy"
        override suspend fun deleteHistory(id: String) {
            lastDeletedId = id
            tripsFlow.value = tripsFlow.value.filter { it.id != id }
        }
        override suspend fun deleteAllHistory() {
            tripsFlow.value = emptyList()
        }
    }
}

