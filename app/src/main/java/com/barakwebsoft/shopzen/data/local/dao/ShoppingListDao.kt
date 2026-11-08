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

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getListById(id: String): ShoppingListEntity?

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    fun observeListById(id: String): Flow<ShoppingListEntity?>

    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    fun observeListWithItems(id: String): Flow<ShoppingListWithItems?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShoppingListEntity)

    @Update
    suspend fun updateList(list: ShoppingListEntity)

    @Delete
    suspend fun deleteList(list: ShoppingListEntity)

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteListById(id: String)
}
