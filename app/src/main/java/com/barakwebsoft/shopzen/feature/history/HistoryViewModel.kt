package com.barakwebsoft.shopzen.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barakwebsoft.shopzen.domain.model.ShoppingHistory
import com.barakwebsoft.shopzen.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository,
    sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5000)
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _tripToDelete = MutableStateFlow<ShoppingHistory?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        historyRepository.getAllHistory(),
        _searchQuery,
        _tripToDelete
    ) { trips, query, toDelete ->
        val filtered = if (query.isBlank()) {
            trips
        } else {
            trips.filter { it.listName.contains(query, ignoreCase = true) }
        }
        HistoryUiState(
            isLoading = false,
            historyTrips = filtered,
            searchQuery = query,
            totalTripsCount = trips.size,
            tripToDelete = toDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = sharingStarted,
        initialValue = HistoryUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun requestDeleteTrip(trip: ShoppingHistory) {
        _tripToDelete.value = trip
    }

    fun dismissDeleteDialog() {
        _tripToDelete.value = null
    }

    fun confirmDeleteTrip() {
        val trip = _tripToDelete.value ?: return
        viewModelScope.launch {
            historyRepository.deleteHistory(trip.id)
            _tripToDelete.value = null
        }
    }
}
