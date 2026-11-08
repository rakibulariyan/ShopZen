package com.barakwebsoft.shopzen.core.di

import android.content.Context
import com.barakwebsoft.shopzen.data.local.database.ShopZenDatabase
import com.barakwebsoft.shopzen.data.preferences.UserPreferencesDataStore
import com.barakwebsoft.shopzen.data.preferences.userPreferencesDataStore
import com.barakwebsoft.shopzen.data.repository.CategoryRepositoryImpl
import com.barakwebsoft.shopzen.data.repository.HistoryRepositoryImpl
import com.barakwebsoft.shopzen.data.repository.ShoppingItemRepositoryImpl
import com.barakwebsoft.shopzen.data.repository.ShoppingListRepositoryImpl
import com.barakwebsoft.shopzen.data.repository.UserPreferencesRepositoryImpl
import com.barakwebsoft.shopzen.domain.repository.CategoryRepository
import com.barakwebsoft.shopzen.domain.repository.HistoryRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingItemRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import com.barakwebsoft.shopzen.domain.repository.UserPreferencesRepository

/**
 * Dependency injection container for application-level singletons and repositories.
 */
interface AppContainer {
    val shoppingListRepository: ShoppingListRepository
    val shoppingItemRepository: ShoppingItemRepository
    val categoryRepository: CategoryRepository
    val historyRepository: HistoryRepository
    val userPreferencesRepository: UserPreferencesRepository
}

/**
 * Default implementation of [AppContainer] managing instances scoped to application context.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: ShopZenDatabase by lazy {
        ShopZenDatabase.getInstance(context)
    }

    override val shoppingListRepository: ShoppingListRepository by lazy {
        ShoppingListRepositoryImpl(database.shoppingListDao(), database.shoppingItemDao())
    }

    override val shoppingItemRepository: ShoppingItemRepository by lazy {
        ShoppingItemRepositoryImpl(database.shoppingItemDao())
    }

    override val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(database.categoryDao())
    }

    override val historyRepository: HistoryRepository by lazy {
        HistoryRepositoryImpl(database.historyDao(), database.shoppingListDao(), database.shoppingItemDao())
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepositoryImpl(UserPreferencesDataStore(context.userPreferencesDataStore))
    }
}

