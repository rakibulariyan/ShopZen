package com.barakwebsoft.shopzen.data.repository

import com.barakwebsoft.shopzen.data.local.DefaultCategories
import com.barakwebsoft.shopzen.data.local.dao.CategoryDao
import com.barakwebsoft.shopzen.data.mapper.toDomain
import com.barakwebsoft.shopzen.data.mapper.toEntity
import com.barakwebsoft.shopzen.domain.model.Category
import com.barakwebsoft.shopzen.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun observeCategoryById(id: String): Flow<Category?> {
        return categoryDao.observeCategoryById(id)
            .map { it?.toDomain() }
            .flowOn(ioDispatcher)
    }

    override suspend fun getCategoryById(id: String): Category? = withContext(ioDispatcher) {
        categoryDao.getCategoryById(id)?.toDomain()
    }

    override suspend fun getCategoryByName(name: String): Category? = withContext(ioDispatcher) {
        categoryDao.getCategoryByName(name)?.toDomain()
    }

    override suspend fun getAllCategoriesSync(): List<Category> = withContext(ioDispatcher) {
        categoryDao.getAllCategoriesSync().map { it.toDomain() }
    }

    override suspend fun getCategoryCount(): Int = withContext(ioDispatcher) {
        categoryDao.getCategoryCount()
    }

    override suspend fun addCategory(category: Category) = withContext(ioDispatcher) {
        categoryDao.insertCategory(category.toEntity())
    }

    override suspend fun addCategories(categories: List<Category>) = withContext(ioDispatcher) {
        categoryDao.insertCategories(categories.map { it.toEntity() })
    }

    override suspend fun updateCategory(category: Category) = withContext(ioDispatcher) {
        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(id: String) = withContext(ioDispatcher) {
        categoryDao.deleteCategoryById(id)
    }

    override suspend fun ensureDefaultCategories() = withContext(ioDispatcher) {
        if (categoryDao.getCategoryCount() == 0) {
            categoryDao.insertCategories(DefaultCategories.ALL)
        }
    }
}
