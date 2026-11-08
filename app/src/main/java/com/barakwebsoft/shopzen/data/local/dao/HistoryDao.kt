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
    suspend fun getHistoryById(id: String): ShoppingHistoryEntity?

    @Transaction
    @Query("SELECT * FROM shopping_history WHERE id = :id")
    fun observeHistoryWithItems(id: String): Flow<HistoryWithItems?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ShoppingHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItems(items: List<HistoryItemEntity>)

    @Delete
    suspend fun deleteHistory(history: ShoppingHistoryEntity)

    @Query("DELETE FROM shopping_history WHERE id = :id")
    suspend fun deleteHistoryById(id: String)
}
