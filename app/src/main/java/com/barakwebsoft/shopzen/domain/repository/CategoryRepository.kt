package com.barakwebsoft.shopzen.domain.repository

import com.barakwebsoft.shopzen.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    fun observeCategoryById(id: String): Flow<Category?>
    suspend fun getCategoryById(id: String): Category?
    suspend fun getCategoryByName(name: String): Category?
    suspend fun getAllCategoriesSync(): List<Category>
    suspend fun getCategoryCount(): Int
    suspend fun addCategory(category: Category)
    suspend fun addCategories(categories: List<Category>)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(id: String)
    suspend fun ensureDefaultCategories()
}
