package com.barakwebsoft.shopzen.data.repository

import com.barakwebsoft.shopzen.data.local.dao.ShoppingItemDao
import com.barakwebsoft.shopzen.data.local.dao.ShoppingListDao
import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.data.mapper.toDomain
import com.barakwebsoft.shopzen.data.mapper.toDomainDetail
import com.barakwebsoft.shopzen.data.mapper.toEntity
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class ShoppingListRepositoryImpl(
    private val shoppingListDao: ShoppingListDao,
    private val shoppingItemDao: ShoppingItemDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ShoppingListRepository {

    override fun getAllLists(): Flow<List<ShoppingList>> {
        return shoppingListDao.getAllLists()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getListsByStatus(status: ListStatus): Flow<List<ShoppingList>> {
        return shoppingListDao.getListsByStatus(status.name)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getActiveLists(): Flow<List<ShoppingList>> {
        return shoppingListDao.getActiveLists()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getCompletedLists(): Flow<List<ShoppingList>> {
        return shoppingListDao.getCompletedLists()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getArchivedLists(): Flow<List<ShoppingList>> {
        return shoppingListDao.getArchivedLists()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun observeListById(id: String): Flow<ShoppingList?> {
        return shoppingListDao.observeListById(id)
            .map { it?.toDomain() }
            .flowOn(ioDispatcher)
    }

    override fun observeListDetail(id: String): Flow<ShoppingListDetail?> {
        return shoppingListDao.observeListById(id)
            .combine(shoppingItemDao.getItemsWithCategoryForList(id)) { listEntity, itemsWithCategory ->
                listEntity?.let {
                    ShoppingListDetail(
                        list = it.toDomain(),
                        items = itemsWithCategory.map { itemWithCat -> itemWithCat.toDomainDetail() }
                    )
                }
            }
            .flowOn(ioDispatcher)
    }

    override fun searchLists(query: String): Flow<List<ShoppingList>> {
        return shoppingListDao.searchLists(query)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun observeActiveListCount(): Flow<Int> {
        return shoppingListDao.observeActiveListCount()
            .flowOn(ioDispatcher)
    }

    override fun observeTotalListCount(): Flow<Int> {
        return shoppingListDao.observeTotalListCount()
            .flowOn(ioDispatcher)
    }

    override suspend fun getListById(id: String): ShoppingList? = withContext(ioDispatcher) {
        shoppingListDao.getListById(id)?.toDomain()
    }

    override suspend fun getListDetailById(id: String): ShoppingListDetail? = withContext(ioDispatcher) {
        val listEntity = shoppingListDao.getListById(id) ?: return@withContext null
        val itemsWithCategory = shoppingItemDao.getItemsWithCategoryForListSync(id)
        ShoppingListDetail(
            list = listEntity.toDomain(),
            items = itemsWithCategory.map { it.toDomainDetail() }
        )
    }

    override suspend fun getActiveListCount(): Int = withContext(ioDispatcher) {
        shoppingListDao.getActiveListCount()
    }

    override suspend fun getTotalListCount(): Int = withContext(ioDispatcher) {
        shoppingListDao.getTotalListCount()
    }

    override suspend fun createList(list: ShoppingList) = withContext(ioDispatcher) {
        shoppingListDao.insertList(list.toEntity())
    }

    override suspend fun updateList(list: ShoppingList) = withContext(ioDispatcher) {
        shoppingListDao.updateList(list.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun updateListStatus(id: String, status: ListStatus) = withContext(ioDispatcher) {
        shoppingListDao.updateListStatus(id, status.name, System.currentTimeMillis())
    }

    override suspend fun markListCompleted(id: String, completedAt: Long) = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        shoppingListDao.updateListCompletion(id, ListStatus.COMPLETED.name, completedAt, now)
    }

    override suspend fun deleteList(id: String) = withContext(ioDispatcher) {
        shoppingListDao.deleteListById(id)
    }

    override suspend fun duplicateList(listId: String, newName: String?): String = withContext(ioDispatcher) {
        val originalList = shoppingListDao.getListById(listId)
            ?: throw IllegalArgumentException("Cannot duplicate non-existent list with id: $listId")

        val now = System.currentTimeMillis()
        val newListId = UUID.randomUUID().toString()
        val duplicatedList = originalList.copy(
            id = newListId,
            name = newName ?: "${originalList.name} (Copy)",
            status = ListStatus.ACTIVE.name,
            createdAt = now,
            updatedAt = now,
            completedAt = null
        )

        val originalItems = shoppingItemDao.getItemsForListSync(listId)
        val duplicatedItems = originalItems.map { originalItem ->
            originalItem.copy(
                id = UUID.randomUUID().toString(),
                listId = newListId,
                isPurchased = false,
                actualUnitPriceMinor = null,
                createdAt = now,
                updatedAt = now
            )
        }

        shoppingListDao.insertList(duplicatedList)
        if (duplicatedItems.isNotEmpty()) {
            shoppingItemDao.insertItems(duplicatedItems)
        }

        newListId
    }
}
