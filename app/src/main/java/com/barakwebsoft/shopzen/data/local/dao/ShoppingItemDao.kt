package com.barakwebsoft.shopzen.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.barakwebsoft.shopzen.data.local.entity.ShoppingItemEntity
import com.barakwebsoft.shopzen.data.local.relation.ShoppingItemWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {

    @Query("SELECT * FROM shopping_items WHERE list_id = :listId ORDER BY sort_order ASC, created_at ASC")
    fun getItemsForList(listId: String): Flow<List<ShoppingItemEntity>>

    @Transaction
    @Query("SELECT * FROM shopping_items WHERE list_id = :listId ORDER BY sort_order ASC, created_at ASC")
    fun getItemsWithCategoryForList(listId: String): Flow<List<ShoppingItemWithCategory>>

    @Query("SELECT * FROM shopping_items WHERE list_id = :listId AND is_purchased = 1 ORDER BY sort_order ASC, created_at ASC")
    fun getPurchasedItemsForList(listId: String): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE list_id = :listId AND is_purchased = 0 ORDER BY sort_order ASC, created_at ASC")
    fun getPendingItemsForList(listId: String): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE id = :id")
    fun observeItemById(id: String): Flow<ShoppingItemEntity?>

    @Transaction
    @Query("SELECT * FROM shopping_items WHERE id = :id")
    fun observeItemWithCategory(id: String): Flow<ShoppingItemWithCategory?>

    @Query("SELECT COUNT(*) FROM shopping_items WHERE list_id = :listId")
    fun observeItemCount(listId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM shopping_items WHERE list_id = :listId AND is_purchased = 1")
    fun observePurchasedItemCount(listId: String): Flow<Int>

    @Query("SELECT * FROM shopping_items WHERE list_id = :listId AND name LIKE '%' || :query || '%' ORDER BY sort_order ASC, created_at ASC")
    fun searchItemsInList(listId: String, query: String): Flow<List<ShoppingItemEntity>>

    @Transaction
    @Query("SELECT * FROM shopping_items WHERE list_id = :listId AND name LIKE '%' || :query || '%' ORDER BY sort_order ASC, created_at ASC")
    fun searchItemsWithCategoryInList(listId: String, query: String): Flow<List<ShoppingItemWithCategory>>

    @Query("SELECT * FROM shopping_items WHERE id = :id")
    suspend fun getItemById(id: String): ShoppingItemEntity?

    @Transaction
    @Query("SELECT * FROM shopping_items WHERE id = :id")
    suspend fun getItemWithCategoryById(id: String): ShoppingItemWithCategory?

    @Query("SELECT * FROM shopping_items WHERE list_id = :listId ORDER BY sort_order ASC, created_at ASC")
    suspend fun getItemsForListSync(listId: String): List<ShoppingItemEntity>

    @Transaction
    @Query("SELECT * FROM shopping_items WHERE list_id = :listId ORDER BY sort_order ASC, created_at ASC")
    suspend fun getItemsWithCategoryForListSync(listId: String): List<ShoppingItemWithCategory>

    @Query("SELECT COUNT(*) FROM shopping_items WHERE list_id = :listId")
    suspend fun getItemCount(listId: String): Int

    @Query("SELECT COUNT(*) FROM shopping_items WHERE list_id = :listId AND is_purchased = 1")
    suspend fun getPurchasedItemCount(listId: String): Int

    @Query("SELECT MAX(sort_order) FROM shopping_items WHERE list_id = :listId")
    suspend fun getMaxSortOrder(listId: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingItemEntity>)

    @Update
    suspend fun updateItem(item: ShoppingItemEntity)

    @Update
    suspend fun updateItems(items: List<ShoppingItemEntity>)

    @Query("UPDATE shopping_items SET is_purchased = :isPurchased, updated_at = :updatedAt WHERE id = :id")
    suspend fun updatePurchasedStatus(id: String, isPurchased: Boolean, updatedAt: Long)

    @Query("UPDATE shopping_items SET actual_unit_price_minor = :actualUnitPriceMinor, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateActualUnitPrice(id: String, actualUnitPriceMinor: Long?, updatedAt: Long)

    @Query("UPDATE shopping_items SET sort_order = :sortOrder, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int, updatedAt: Long)

    @Delete
    suspend fun deleteItem(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("DELETE FROM shopping_items WHERE list_id = :listId")
    suspend fun deleteItemsForList(listId: String)
}
