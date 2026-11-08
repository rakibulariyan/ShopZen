package com.barakwebsoft.shopzen.data.repository

import com.barakwebsoft.shopzen.data.local.dao.ShoppingItemDao
import com.barakwebsoft.shopzen.data.mapper.toDomain
import com.barakwebsoft.shopzen.data.mapper.toDomainDetail
import com.barakwebsoft.shopzen.data.mapper.toEntity
import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail
import com.barakwebsoft.shopzen.domain.repository.ShoppingItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ShoppingItemRepositoryImpl(
    private val shoppingItemDao: ShoppingItemDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ShoppingItemRepository {

    override fun getItemsForList(listId: String): Flow<List<ShoppingItem>> {
        return shoppingItemDao.getItemsForList(listId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getItemsWithCategoryForList(listId: String): Flow<List<ShoppingItemDetail>> {
        return shoppingItemDao.getItemsWithCategoryForList(listId)
            .map { list -> list.map { it.toDomainDetail() } }
            .flowOn(ioDispatcher)
    }

    override fun getPurchasedItemsForList(listId: String): Flow<List<ShoppingItem>> {
        return shoppingItemDao.getPurchasedItemsForList(listId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getPendingItemsForList(listId: String): Flow<List<ShoppingItem>> {
        return shoppingItemDao.getPendingItemsForList(listId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun observeItemById(id: String): Flow<ShoppingItem?> {
        return shoppingItemDao.observeItemById(id)
            .map { it?.toDomain() }
            .flowOn(ioDispatcher)
    }

    override fun observeItemDetail(id: String): Flow<ShoppingItemDetail?> {
        return shoppingItemDao.observeItemWithCategory(id)
            .map { it?.toDomainDetail() }
            .flowOn(ioDispatcher)
    }

    override fun observeItemCount(listId: String): Flow<Int> {
        return shoppingItemDao.observeItemCount(listId)
            .flowOn(ioDispatcher)
    }

    override fun observePurchasedItemCount(listId: String): Flow<Int> {
        return shoppingItemDao.observePurchasedItemCount(listId)
            .flowOn(ioDispatcher)
    }

    override fun searchItemsInList(listId: String, query: String): Flow<List<ShoppingItem>> {
        return shoppingItemDao.searchItemsInList(listId, query)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun searchItemsWithCategoryInList(listId: String, query: String): Flow<List<ShoppingItemDetail>> {
        return shoppingItemDao.searchItemsWithCategoryInList(listId, query)
            .map { list -> list.map { it.toDomainDetail() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getItemById(id: String): ShoppingItem? = withContext(ioDispatcher) {
        shoppingItemDao.getItemById(id)?.toDomain()
    }

    override suspend fun getItemDetailById(id: String): ShoppingItemDetail? = withContext(ioDispatcher) {
        shoppingItemDao.getItemWithCategoryById(id)?.toDomainDetail()
    }

    override suspend fun getItemsForListSync(listId: String): List<ShoppingItem> = withContext(ioDispatcher) {
        shoppingItemDao.getItemsForListSync(listId).map { it.toDomain() }
    }

    override suspend fun getItemCount(listId: String): Int = withContext(ioDispatcher) {
        shoppingItemDao.getItemCount(listId)
    }

    override suspend fun getPurchasedItemCount(listId: String): Int = withContext(ioDispatcher) {
        shoppingItemDao.getPurchasedItemCount(listId)
    }

    override suspend fun addItem(item: ShoppingItem) = withContext(ioDispatcher) {
        val nextSortOrder = if (item.sortOrder <= 0) {
            val max = shoppingItemDao.getMaxSortOrder(item.listId) ?: -1
            max + 1
        } else {
            item.sortOrder
        }
        shoppingItemDao.insertItem(item.copy(sortOrder = nextSortOrder).toEntity())
    }

    override suspend fun addItems(items: List<ShoppingItem>) = withContext(ioDispatcher) {
        shoppingItemDao.insertItems(items.map { it.toEntity() })
    }

    override suspend fun updateItem(item: ShoppingItem) = withContext(ioDispatcher) {
        shoppingItemDao.updateItem(item.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun toggleItemPurchased(id: String, isPurchased: Boolean) = withContext(ioDispatcher) {
        shoppingItemDao.updatePurchasedStatus(id, isPurchased, System.currentTimeMillis())
    }

    override suspend fun updateActualPrice(id: String, actualUnitPriceMinor: Long?) = withContext(ioDispatcher) {
        shoppingItemDao.updateActualUnitPrice(id, actualUnitPriceMinor, System.currentTimeMillis())
    }

    override suspend fun updateItemSortOrder(id: String, sortOrder: Int) = withContext(ioDispatcher) {
        shoppingItemDao.updateSortOrder(id, sortOrder, System.currentTimeMillis())
    }

    override suspend fun deleteItem(id: String) = withContext(ioDispatcher) {
        shoppingItemDao.deleteItemById(id)
    }

    override suspend fun deleteItemsForList(listId: String) = withContext(ioDispatcher) {
        shoppingItemDao.deleteItemsForList(listId)
    }
}
