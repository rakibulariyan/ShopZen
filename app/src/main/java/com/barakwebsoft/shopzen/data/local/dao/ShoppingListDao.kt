package com.barakwebsoft.shopzen.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.barakwebsoft.shopzen.data.local.entity.ShoppingListEntity
import com.barakwebsoft.shopzen.data.local.relation.ShoppingListWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    @Query("SELECT * FROM shopping_lists ORDER BY updated_at DESC")
    fun getAllLists(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists WHERE status = :status ORDER BY updated_at DESC")
    fun getListsByStatus(status: String): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists WHERE status = 'ACTIVE' ORDER BY updated_at DESC")
    fun getActiveLists(): Flow<List<ShoppingListEntity>>

    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE status = 'ACTIVE' ORDER BY updated_at DESC")
    fun observeActiveListsWithItems(): Flow<List<ShoppingListWithItems>>

    @Query("SELECT * FROM shopping_lists WHERE status = 'COMPLETED' ORDER BY updated_at DESC")
    fun getCompletedLists(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists WHERE status = 'ARCHIVED' ORDER BY updated_at DESC")
    fun getArchivedLists(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    fun observeListById(id: String): Flow<ShoppingListEntity?>

    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    fun observeListWithItems(id: String): Flow<ShoppingListWithItems?>

    @Query("SELECT * FROM shopping_lists WHERE name LIKE '%' || :query || '%' OR (description IS NOT NULL AND description LIKE '%' || :query || '%') ORDER BY updated_at DESC")
    fun searchLists(query: String): Flow<List<ShoppingListEntity>>

    @Query("SELECT COUNT(*) FROM shopping_lists WHERE status = 'ACTIVE'")
    fun observeActiveListCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM shopping_lists")
    fun observeTotalListCount(): Flow<Int>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getListById(id: String): ShoppingListEntity?

    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getListWithItemsById(id: String): ShoppingListWithItems?

    @Query("SELECT COUNT(*) FROM shopping_lists WHERE status = 'ACTIVE'")
    suspend fun getActiveListCount(): Int

    @Query("SELECT COUNT(*) FROM shopping_lists")
    suspend fun getTotalListCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShoppingListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLists(lists: List<ShoppingListEntity>)

    @Update
    suspend fun updateList(list: ShoppingListEntity)

    @Query("UPDATE shopping_lists SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateListStatus(id: String, status: String, updatedAt: Long)

    @Query("UPDATE shopping_lists SET status = :status, completed_at = :completedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateListCompletion(id: String, status: String, completedAt: Long?, updatedAt: Long)

    @Delete
    suspend fun deleteList(list: ShoppingListEntity)

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteListById(id: String)
}
