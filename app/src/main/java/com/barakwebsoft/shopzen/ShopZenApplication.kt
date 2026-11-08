package com.barakwebsoft.shopzen

import android.app.Application
import com.barakwebsoft.shopzen.core.di.AppContainer
import com.barakwebsoft.shopzen.core.di.DefaultAppContainer

/**
 * Custom application class initializing the DI container.
 */
class ShopZenApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}

