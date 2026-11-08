package com.barakwebsoft.shopzen.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.barakwebsoft.shopzen.data.local.entity.HistoryItemEntity
import com.barakwebsoft.shopzen.data.local.entity.ShoppingHistoryEntity
import com.barakwebsoft.shopzen.data.local.relation.HistoryWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM shopping_history ORDER BY completed_at DESC")
    fun getAllHistory(): Flow<List<ShoppingHistoryEntity>>

    @Query("SELECT * FROM shopping_history WHERE id = :id")
    fun observeHistoryById(id: String): Flow<ShoppingHistoryEntity?>

    @Transaction
    @Query("SELECT * FROM shopping_history WHERE id = :id")
    fun observeHistoryWithItems(id: String): Flow<HistoryWithItems?>

    @Query("SELECT * FROM shopping_history WHERE list_name LIKE '%' || :query || '%' ORDER BY completed_at DESC")
    fun searchHistory(query: String): Flow<List<ShoppingHistoryEntity>>

    @Query("SELECT COUNT(*) FROM shopping_history")
    fun observeHistoryCount(): Flow<Int>

    @Query("SELECT * FROM shopping_history WHERE id = :id")
    suspend fun getHistoryById(id: String): ShoppingHistoryEntity?

    @Transaction
    @Query("SELECT * FROM shopping_history WHERE id = :id")
    suspend fun getHistoryWithItemsById(id: String): HistoryWithItems?

    @Query("SELECT * FROM history_items WHERE history_id = :historyId")
    suspend fun getHistoryItems(historyId: String): List<HistoryItemEntity>

    @Query("SELECT COUNT(*) FROM shopping_history")
    suspend fun getHistoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ShoppingHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItems(items: List<HistoryItemEntity>)

    @Transaction
    suspend fun insertHistoryWithItems(
        history: ShoppingHistoryEntity,
        items: List<HistoryItemEntity>
    ) {
        insertHistory(history)
        insertHistoryItems(items)
    }

    @Delete
    suspend fun deleteHistory(history: ShoppingHistoryEntity)

    @Query("DELETE FROM shopping_history WHERE id = :id")
    suspend fun deleteHistoryById(id: String)

    @Query("DELETE FROM shopping_history")
    suspend fun deleteAllHistory()
}
