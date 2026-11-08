package com.barakwebsoft.shopzen.domain.repository

import com.barakwebsoft.shopzen.domain.model.HistoryDetail
import com.barakwebsoft.shopzen.domain.model.ShoppingHistory
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getAllHistory(): Flow<List<ShoppingHistory>>
    fun observeHistoryById(id: String): Flow<ShoppingHistory?>
    fun observeHistoryDetail(id: String): Flow<HistoryDetail?>
    fun searchHistory(query: String): Flow<List<ShoppingHistory>>
    fun observeHistoryCount(): Flow<Int>

    suspend fun getHistoryById(id: String): ShoppingHistory?
    suspend fun getHistoryDetailById(id: String): HistoryDetail?
    suspend fun getHistoryCount(): Int
    suspend fun archiveListToHistory(listId: String): String
    suspend fun deleteHistory(id: String)
    suspend fun deleteAllHistory()
}
