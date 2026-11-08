package com.barakwebsoft.shopzen.domain.repository

import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    fun getAllLists(): Flow<List<ShoppingList>>
    fun getListsByStatus(status: ListStatus): Flow<List<ShoppingList>>
    fun getActiveLists(): Flow<List<ShoppingList>>
    fun getCompletedLists(): Flow<List<ShoppingList>>
    fun getArchivedLists(): Flow<List<ShoppingList>>
    fun observeListById(id: String): Flow<ShoppingList?>
    fun observeListDetail(id: String): Flow<ShoppingListDetail?>
    fun searchLists(query: String): Flow<List<ShoppingList>>
    fun observeActiveListCount(): Flow<Int>
    fun observeTotalListCount(): Flow<Int>

    suspend fun getListById(id: String): ShoppingList?
    suspend fun getListDetailById(id: String): ShoppingListDetail?
    suspend fun getActiveListCount(): Int
    suspend fun getTotalListCount(): Int
    suspend fun createList(list: ShoppingList)
    suspend fun updateList(list: ShoppingList)
    suspend fun updateListStatus(id: String, status: ListStatus)
    suspend fun markListCompleted(id: String, completedAt: Long = System.currentTimeMillis())
    suspend fun deleteList(id: String)
    suspend fun duplicateList(listId: String, newName: String? = null): String
}
