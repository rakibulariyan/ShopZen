package com.barakwebsoft.shopzen.feature.history

import com.barakwebsoft.shopzen.domain.model.ShoppingHistory

data class HistoryUiState(
    val isLoading: Boolean = true,
    val historyTrips: List<ShoppingHistory> = emptyList(),
    val searchQuery: String = "",
    val totalTripsCount: Int = 0,
    val tripToDelete: ShoppingHistory? = null
)

