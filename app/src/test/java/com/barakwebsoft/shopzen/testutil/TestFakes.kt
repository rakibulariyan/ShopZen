package com.barakwebsoft.shopzen.testutil

import com.barakwebsoft.shopzen.data.local.dao.CategoryDao
import com.barakwebsoft.shopzen.data.local.dao.HistoryDao
import com.barakwebsoft.shopzen.data.local.dao.ShoppingItemDao
import com.barakwebsoft.shopzen.data.local.dao.ShoppingListDao
import com.barakwebsoft.shopzen.data.local.entity.CategoryEntity
import com.barakwebsoft.shopzen.data.local.entity.HistoryItemEntity
import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.data.local.entity.ShoppingHistoryEntity
import com.barakwebsoft.shopzen.data.local.entity.ShoppingItemEntity
import com.barakwebsoft.shopzen.data.local.entity.ShoppingListEntity
import com.barakwebsoft.shopzen.data.local.relation.HistoryWithItems
import com.barakwebsoft.shopzen.data.local.relation.ShoppingItemWithCategory
import com.barakwebsoft.shopzen.data.local.relation.ShoppingListWithItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Shared in-memory fake DAOs for unit and integration testing.
 */
class FakeShoppingListDao : ShoppingListDao {
    private val lists = MutableStateFlow<Map<String, ShoppingListEntity>>(emptyMap())

    override fun getAllLists(): Flow<List<ShoppingListEntity>> =
        lists.map { it.values.sortedByDescending { l -> l.updatedAt } }

    override fun getListsByStatus(status: String): Flow<List<ShoppingListEntity>> =
        lists.map { it.values.filter { l -> l.status == status }.sortedByDescending { l -> l.updatedAt } }

    override fun getActiveLists(): Flow<List<ShoppingListEntity>> =
        getListsByStatus(ListStatus.ACTIVE.name)

    override fun observeActiveListsWithItems(): Flow<List<ShoppingListWithItems>> =
        lists.map { map ->
            map.values
                .filter { it.status == ListStatus.ACTIVE.name }
                .sortedByDescending { it.updatedAt }
                .map { ShoppingListWithItems(it, emptyList()) }
        }

    override fun getCompletedLists(): Flow<List<ShoppingListEntity>> =
        getListsByStatus(ListStatus.COMPLETED.name)

    override fun getArchivedLists(): Flow<List<ShoppingListEntity>> =
        getListsByStatus(ListStatus.ARCHIVED.name)

    override fun observeListById(id: String): Flow<ShoppingListEntity?> =
        lists.map { it[id] }

    override fun observeListWithItems(id: String): Flow<ShoppingListWithItems?> =
        lists.map { it[id]?.let { entity -> ShoppingListWithItems(entity, emptyList()) } }

    override fun searchLists(query: String): Flow<List<ShoppingListEntity>> =
        lists.map { map ->
            map.values.filter { it.name.contains(query, ignoreCase = true) }
                .sortedByDescending { it.updatedAt }
        }

    override fun observeActiveListCount(): Flow<Int> =
        lists.map { it.values.count { l -> l.status == ListStatus.ACTIVE.name } }

    override fun observeTotalListCount(): Flow<Int> =
        lists.map { it.size }

    override suspend fun getListById(id: String): ShoppingListEntity? = lists.value[id]

    override suspend fun getListWithItemsById(id: String): ShoppingListWithItems? =
        lists.value[id]?.let { ShoppingListWithItems(it, emptyList()) }

    override suspend fun getActiveListCount(): Int =
        lists.value.values.count { it.status == ListStatus.ACTIVE.name }

    override suspend fun getTotalListCount(): Int = lists.value.size

    override suspend fun insertList(list: ShoppingListEntity) {
        lists.value = lists.value + (list.id to list)
    }

    override suspend fun insertLists(lists: List<ShoppingListEntity>) {
        this.lists.value = this.lists.value + lists.associateBy { it.id }
    }

    override suspend fun updateList(list: ShoppingListEntity) {
        lists.value = lists.value + (list.id to list)
    }

    override suspend fun updateListStatus(id: String, status: String, updatedAt: Long) {
        lists.value[id]?.let {
            lists.value = lists.value + (id to it.copy(status = status, updatedAt = updatedAt))
        }
    }

    override suspend fun updateListCompletion(id: String, status: String, completedAt: Long?, updatedAt: Long) {
        lists.value[id]?.let {
            lists.value = lists.value + (id to it.copy(status = status, completedAt = completedAt, updatedAt = updatedAt))
        }
    }

    override suspend fun deleteList(list: ShoppingListEntity) {
        lists.value = lists.value - list.id
    }

    override suspend fun deleteListById(id: String) {
        lists.value = lists.value - id
    }
}

class FakeShoppingItemDao : ShoppingItemDao {
    private val items = MutableStateFlow<Map<String, ShoppingItemEntity>>(emptyMap())

    override fun getItemsForList(listId: String): Flow<List<ShoppingItemEntity>> =
        items.map { it.values.filter { i -> i.listId == listId }.sortedBy { i -> i.sortOrder } }

    override fun getItemsWithCategoryForList(listId: String): Flow<List<ShoppingItemWithCategory>> =
        items.map {
            it.values.filter { i -> i.listId == listId }
                .sortedBy { i -> i.sortOrder }
                .map { item -> ShoppingItemWithCategory(item, null) }
        }

    override fun getPurchasedItemsForList(listId: String): Flow<List<ShoppingItemEntity>> =
        items.map { it.values.filter { i -> i.listId == listId && i.isPurchased }.sortedBy { i -> i.sortOrder } }

    override fun getPendingItemsForList(listId: String): Flow<List<ShoppingItemEntity>> =
        items.map { it.values.filter { i -> i.listId == listId && !i.isPurchased }.sortedBy { i -> i.sortOrder } }

    override fun observeItemById(id: String): Flow<ShoppingItemEntity?> =
        items.map { it[id] }

    override fun observeItemWithCategory(id: String): Flow<ShoppingItemWithCategory?> =
        items.map { it[id]?.let { entity -> ShoppingItemWithCategory(entity, null) } }

    override fun observeItemCount(listId: String): Flow<Int> =
        items.map { it.values.count { i -> i.listId == listId } }

    override fun observePurchasedItemCount(listId: String): Flow<Int> =
        items.map { it.values.count { i -> i.listId == listId && i.isPurchased } }

    override fun searchItemsInList(listId: String, query: String): Flow<List<ShoppingItemEntity>> =
        items.map {
            it.values.filter { i -> i.listId == listId && i.name.contains(query, ignoreCase = true) }
                .sortedBy { i -> i.sortOrder }
        }

    override fun searchItemsWithCategoryInList(listId: String, query: String): Flow<List<ShoppingItemWithCategory>> =
        items.map {
            it.values.filter { i -> i.listId == listId && i.name.contains(query, ignoreCase = true) }
                .sortedBy { i -> i.sortOrder }
                .map { item -> ShoppingItemWithCategory(item, null) }
        }

    override suspend fun getItemById(id: String): ShoppingItemEntity? = items.value[id]

    override suspend fun getItemWithCategoryById(id: String): ShoppingItemWithCategory? =
        items.value[id]?.let { ShoppingItemWithCategory(it, null) }

    override suspend fun getItemsForListSync(listId: String): List<ShoppingItemEntity> =
        items.value.values.filter { it.listId == listId }.sortedBy { it.sortOrder }

    override suspend fun getItemsWithCategoryForListSync(listId: String): List<ShoppingItemWithCategory> =
        items.value.values.filter { it.listId == listId }.sortedBy { it.sortOrder }.map { ShoppingItemWithCategory(it, null) }

    override suspend fun getItemCount(listId: String): Int =
        items.value.values.count { it.listId == listId }

    override suspend fun getPurchasedItemCount(listId: String): Int =
        items.value.values.count { it.listId == listId && it.isPurchased }

    override suspend fun getMaxSortOrder(listId: String): Int? =
        items.value.values.filter { it.listId == listId }.maxOfOrNull { it.sortOrder }

    override suspend fun insertItem(item: ShoppingItemEntity) {
        items.value = items.value + (item.id to item)
    }

    override suspend fun insertItems(items: List<ShoppingItemEntity>) {
        this.items.value = this.items.value + items.associateBy { it.id }
    }

    override suspend fun updateItem(item: ShoppingItemEntity) {
        items.value = items.value + (item.id to item)
    }

    override suspend fun updateItems(items: List<ShoppingItemEntity>) {
        this.items.value = this.items.value + items.associateBy { it.id }
    }

    override suspend fun updatePurchasedStatus(id: String, isPurchased: Boolean, updatedAt: Long) {
        items.value[id]?.let {
            items.value = items.value + (id to it.copy(isPurchased = isPurchased, updatedAt = updatedAt))
        }
    }

    override suspend fun updateActualUnitPrice(id: String, actualUnitPriceMinor: Long?, updatedAt: Long) {
        items.value[id]?.let {
            items.value = items.value + (id to it.copy(actualUnitPriceMinor = actualUnitPriceMinor, updatedAt = updatedAt))
        }
    }

    override suspend fun updateSortOrder(id: String, sortOrder: Int, updatedAt: Long) {
        items.value[id]?.let {
            items.value = items.value + (id to it.copy(sortOrder = sortOrder, updatedAt = updatedAt))
        }
    }

    override suspend fun deleteItem(item: ShoppingItemEntity) {
        items.value = items.value - item.id
    }

    override suspend fun deleteItemById(id: String) {
        items.value = items.value - id
    }

    override suspend fun deleteItemsForList(listId: String) {
        items.value = items.value.filterValues { it.listId != listId }
    }
}

class FakeCategoryDao : CategoryDao {
    private val categories = MutableStateFlow<Map<String, CategoryEntity>>(emptyMap())

    override fun getAllCategories(): Flow<List<CategoryEntity>> =
        categories.map { it.values.sortedBy { c -> c.sortOrder } }

    override fun observeCategoryById(id: String): Flow<CategoryEntity?> =
        categories.map { it[id] }

    override suspend fun getCategoryById(id: String): CategoryEntity? = categories.value[id]

    override suspend fun getCategoryByName(name: String): CategoryEntity? =
        categories.value.values.find { it.name.equals(name, ignoreCase = true) }

    override suspend fun getAllCategoriesSync(): List<CategoryEntity> =
        categories.value.values.sortedBy { it.sortOrder }

    override suspend fun getCategoryCount(): Int = categories.value.size

    override suspend fun insertCategory(category: CategoryEntity) {
        categories.value = categories.value + (category.id to category)
    }

    override suspend fun insertCategories(categories: List<CategoryEntity>) {
        this.categories.value = this.categories.value + categories.associateBy { it.id }
    }

    override suspend fun updateCategory(category: CategoryEntity) {
        categories.value = categories.value + (category.id to category)
    }

    override suspend fun deleteCategory(category: CategoryEntity) {
        categories.value = categories.value - category.id
    }

    override suspend fun deleteCategoryById(id: String) {
        categories.value = categories.value - id
    }
}

class FakeHistoryDao : HistoryDao {
    private val history = MutableStateFlow<Map<String, ShoppingHistoryEntity>>(emptyMap())
    private val historyItems = MutableStateFlow<Map<String, HistoryItemEntity>>(emptyMap())

    override fun getAllHistory(): Flow<List<ShoppingHistoryEntity>> =
        history.map { it.values.sortedByDescending { h -> h.completedAt } }

    override fun observeHistoryById(id: String): Flow<ShoppingHistoryEntity?> =
        history.map { it[id] }

    override fun observeHistoryWithItems(id: String): Flow<HistoryWithItems?> =
        history.map { hMap ->
            hMap[id]?.let { header ->
                val items = historyItems.value.values.filter { it.historyId == id }
                HistoryWithItems(header, items)
            }
        }

    override fun searchHistory(query: String): Flow<List<ShoppingHistoryEntity>> =
        history.map { hMap ->
            hMap.values.filter { it.listName.contains(query, ignoreCase = true) }
                .sortedByDescending { it.completedAt }
        }

    override fun observeHistoryCount(): Flow<Int> = history.map { it.size }

    override suspend fun getHistoryById(id: String): ShoppingHistoryEntity? = history.value[id]

    override suspend fun getHistoryWithItemsById(id: String): HistoryWithItems? =
        history.value[id]?.let { header ->
            val items = historyItems.value.values.filter { it.historyId == id }
            HistoryWithItems(header, items)
        }

    override suspend fun getHistoryItems(historyId: String): List<HistoryItemEntity> =
        historyItems.value.values.filter { it.historyId == historyId }

    override suspend fun getHistoryCount(): Int = history.value.size

    override suspend fun insertHistory(history: ShoppingHistoryEntity) {
        this.history.value = this.history.value + (history.id to history)
    }

    override suspend fun insertHistoryItems(items: List<HistoryItemEntity>) {
        this.historyItems.value = this.historyItems.value + items.associateBy { it.id }
    }

    override suspend fun deleteHistory(history: ShoppingHistoryEntity) {
        this.history.value = this.history.value - history.id
        this.historyItems.value = this.historyItems.value.filterValues { it.historyId != history.id }
    }

    override suspend fun deleteHistoryById(id: String) {
        history.value = history.value - id
        historyItems.value = historyItems.value.filterValues { it.historyId != id }
    }

    override suspend fun deleteAllHistory() {
        history.value = emptyMap()
        historyItems.value = emptyMap()
    }
}
