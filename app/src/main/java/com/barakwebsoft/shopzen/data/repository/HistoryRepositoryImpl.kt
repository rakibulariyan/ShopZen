package com.barakwebsoft.shopzen.data.repository

import com.barakwebsoft.shopzen.data.local.dao.HistoryDao
import com.barakwebsoft.shopzen.data.local.dao.ShoppingItemDao
import com.barakwebsoft.shopzen.data.local.dao.ShoppingListDao
import com.barakwebsoft.shopzen.data.local.entity.HistoryItemEntity
import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.data.local.entity.ShoppingHistoryEntity
import com.barakwebsoft.shopzen.data.mapper.toDomain
import com.barakwebsoft.shopzen.data.mapper.toDomainDetail
import com.barakwebsoft.shopzen.domain.model.HistoryDetail
import com.barakwebsoft.shopzen.domain.model.ShoppingHistory
import com.barakwebsoft.shopzen.domain.repository.HistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class HistoryRepositoryImpl(
    private val historyDao: HistoryDao,
    private val shoppingListDao: ShoppingListDao,
    private val shoppingItemDao: ShoppingItemDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : HistoryRepository {

    override fun getAllHistory(): Flow<List<ShoppingHistory>> {
        return historyDao.getAllHistory()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun observeHistoryById(id: String): Flow<ShoppingHistory?> {
        return historyDao.observeHistoryById(id)
            .map { it?.toDomain() }
            .flowOn(ioDispatcher)
    }

    override fun observeHistoryDetail(id: String): Flow<HistoryDetail?> {
        return historyDao.observeHistoryWithItems(id)
            .map { it?.toDomainDetail() }
            .flowOn(ioDispatcher)
    }

    override fun searchHistory(query: String): Flow<List<ShoppingHistory>> {
        return historyDao.searchHistory(query)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun observeHistoryCount(): Flow<Int> {
        return historyDao.observeHistoryCount()
            .flowOn(ioDispatcher)
    }

    override suspend fun getHistoryById(id: String): ShoppingHistory? = withContext(ioDispatcher) {
        historyDao.getHistoryById(id)?.toDomain()
    }

    override suspend fun getHistoryDetailById(id: String): HistoryDetail? = withContext(ioDispatcher) {
        historyDao.getHistoryWithItemsById(id)?.toDomainDetail()
    }

    override suspend fun getHistoryCount(): Int = withContext(ioDispatcher) {
        historyDao.getHistoryCount()
    }

    override suspend fun archiveListToHistory(listId: String): String = withContext(ioDispatcher) {
        val originalList = shoppingListDao.getListById(listId)
            ?: throw IllegalArgumentException("Cannot archive non-existent list with id: $listId")

        val now = System.currentTimeMillis()
        val historyId = UUID.randomUUID().toString()
        val itemsWithCategory = shoppingItemDao.getItemsWithCategoryForListSync(listId)

        var totalSpentMinor = 0L
        var purchasedCount = 0
        var unpricedPurchasedCount = 0

        val historyItemEntities = itemsWithCategory.map { withCat ->
            val item = withCat.item
            val category = withCat.category

            val lineTotalMinor: Long? = if (item.isPurchased && item.actualUnitPriceMinor != null) {
                val quantityDecimal = try {
                    BigDecimal(item.quantity.trim())
                } catch (_: Exception) {
                    BigDecimal.ONE
                }
                val lineTotal = quantityDecimal
                    .multiply(BigDecimal(item.actualUnitPriceMinor))
                    .setScale(0, RoundingMode.HALF_UP)
                    .toLong()
                totalSpentMinor += lineTotal
                lineTotal
            } else {
                null
            }

            if (item.isPurchased) {
                purchasedCount++
                if (item.actualUnitPriceMinor == null) {
                    unpricedPurchasedCount++
                }
            }

            HistoryItemEntity(
                id = UUID.randomUUID().toString(),
                historyId = historyId,
                originalItemId = item.id,
                itemName = item.name,
                quantity = item.quantity,
                unit = item.unit,
                categoryName = category?.name,
                actualUnitPriceMinor = item.actualUnitPriceMinor,
                lineTotalMinor = lineTotalMinor,
                isPurchased = item.isPurchased
            )
        }

        val historyEntity = ShoppingHistoryEntity(
            id = historyId,
            originalListId = originalList.id,
            listName = originalList.name,
            currencyCode = originalList.currencyCode,
            budgetMinor = originalList.budgetMinor,
            totalSpentMinor = totalSpentMinor,
            purchasedCount = purchasedCount,
            unpricedPurchasedCount = unpricedPurchasedCount,
            completedAt = now
        )

        // Insert history snapshot atomically
        historyDao.insertHistoryWithItems(historyEntity, historyItemEntities)

        // Update list status to ARCHIVED and mark completion time
        shoppingListDao.updateListCompletion(
            id = listId,
            status = ListStatus.ARCHIVED.name,
            completedAt = now,
            updatedAt = now
        )

        historyId
    }

    override suspend fun deleteHistory(id: String) = withContext(ioDispatcher) {
        historyDao.deleteHistoryById(id)
    }

    override suspend fun deleteAllHistory() = withContext(ioDispatcher) {
        historyDao.deleteAllHistory()
    }
}
