package com.barakwebsoft.shopzen.core.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.barakwebsoft.shopzen.ShopZenApplication
import com.barakwebsoft.shopzen.feature.home.HomeViewModel

/**
 * Provides factory instances for ViewModels throughout the ShopZen app.
 */
object AppViewModelProvider {

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = shopZenApplication()
            HomeViewModel(
                shoppingListRepository = app.container.shoppingListRepository
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

