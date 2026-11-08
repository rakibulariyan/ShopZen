package com.barakwebsoft.shopzen.domain.repository

import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail
import kotlinx.coroutines.flow.Flow

interface ShoppingItemRepository {
    fun getItemsForList(listId: String): Flow<List<ShoppingItem>>
    fun getItemsWithCategoryForList(listId: String): Flow<List<ShoppingItemDetail>>
    fun getPurchasedItemsForList(listId: String): Flow<List<ShoppingItem>>
    fun getPendingItemsForList(listId: String): Flow<List<ShoppingItem>>
    fun observeItemById(id: String): Flow<ShoppingItem?>
    fun observeItemDetail(id: String): Flow<ShoppingItemDetail?>
    fun observeItemCount(listId: String): Flow<Int>
    fun observePurchasedItemCount(listId: String): Flow<Int>
    fun searchItemsInList(listId: String, query: String): Flow<List<ShoppingItem>>
    fun searchItemsWithCategoryInList(listId: String, query: String): Flow<List<ShoppingItemDetail>>

    suspend fun getItemById(id: String): ShoppingItem?
    suspend fun getItemDetailById(id: String): ShoppingItemDetail?
    suspend fun getItemsForListSync(listId: String): List<ShoppingItem>
    suspend fun getItemCount(listId: String): Int
    suspend fun getPurchasedItemCount(listId: String): Int
    suspend fun addItem(item: ShoppingItem)
    suspend fun addItems(items: List<ShoppingItem>)
    suspend fun updateItem(item: ShoppingItem)
    suspend fun toggleItemPurchased(id: String, isPurchased: Boolean)
    suspend fun updateActualPrice(id: String, actualUnitPriceMinor: Long?)
    suspend fun updateItemSortOrder(id: String, sortOrder: Int)
    suspend fun deleteItem(id: String)
    suspend fun deleteItemsForList(listId: String)
}
