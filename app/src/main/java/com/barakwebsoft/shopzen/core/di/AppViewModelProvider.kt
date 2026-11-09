package com.barakwebsoft.shopzen.core.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.barakwebsoft.shopzen.ShopZenApplication
import com.barakwebsoft.shopzen.feature.home.HomeViewModel
import com.barakwebsoft.shopzen.feature.items.AddEditItemViewModel
import com.barakwebsoft.shopzen.feature.lists.AddEditListViewModel
import com.barakwebsoft.shopzen.feature.lists.details.ListDetailsViewModel
import com.barakwebsoft.shopzen.feature.market.MarketModeViewModel

/**
 * Provides factory instances for ViewModels throughout the ShopZen app.
 */
object AppViewModelProvider {

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = shopZenApplication()
            HomeViewModel(
                shoppingListRepository = app.container.shoppingListRepository,
                historyRepository = app.container.historyRepository
            )
        }
        initializer {
            val app = shopZenApplication()
            AddEditListViewModel(
                shoppingListRepository = app.container.shoppingListRepository,
                userPreferencesRepository = app.container.userPreferencesRepository
            )
        }
        initializer {
            val app = shopZenApplication()
            ListDetailsViewModel(
                shoppingListRepository = app.container.shoppingListRepository,
                shoppingItemRepository = app.container.shoppingItemRepository,
                historyRepository = app.container.historyRepository
            )
        }
        initializer {
            val app = shopZenApplication()
            AddEditItemViewModel(
                shoppingListRepository = app.container.shoppingListRepository,
                shoppingItemRepository = app.container.shoppingItemRepository,
                categoryRepository = app.container.categoryRepository
            )
        }
        initializer {
            val app = shopZenApplication()
            MarketModeViewModel(
                shoppingListRepository = app.container.shoppingListRepository,
                shoppingItemRepository = app.container.shoppingItemRepository
            )
        }
    }
}

/**
 * Extension function on [CreationExtras] to access [ShopZenApplication].
 */
fun CreationExtras.shopZenApplication(): ShopZenApplication {
    return (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ShopZenApplication)
}


