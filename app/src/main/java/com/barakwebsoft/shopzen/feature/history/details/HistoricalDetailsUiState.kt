package com.barakwebsoft.shopzen.feature.history.details

import com.barakwebsoft.shopzen.domain.model.HistoryDetail

data class HistoricalDetailsUiState(
    val isLoading: Boolean = true,
    val historyDetail: HistoryDetail? = null,
    val isDeleted: Boolean = false,
    val clonedListId: String? = null,
    val showDeleteDialog: Boolean = false
)

