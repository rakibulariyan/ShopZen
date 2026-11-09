package com.barakwebsoft.shopzen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.barakwebsoft.shopzen.core.navigation.ShopZenApp
import com.barakwebsoft.shopzen.domain.model.AppPreferences
import com.barakwebsoft.shopzen.domain.model.ThemePreference
import com.barakwebsoft.shopzen.ui.theme.ShopZenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ShopZenApplication
        val preferencesRepository = app.container.userPreferencesRepository

        // Request UMP consent and privacy-safely initialize Google Mobile Ads SDK
        app.container.adConsentManager.requestConsentAndInitAds(this)

        setContent {
            val preferences by preferencesRepository.getPreferences().collectAsState(initial = AppPreferences())

            val isDarkTheme = when (preferences.themePreference) {
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }

            ShopZenTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ShopZenApp()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShopZenAppPreview() {
    ShopZenTheme {
        ShopZenApp()
    }
}
