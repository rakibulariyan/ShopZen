package com.barakwebsoft.shopzen.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeColorTokensTest {

    @Test
    fun testBotanicalColorTokensMatchDesignSpec() {
        assertEquals(Color(0xFF16845B), ShopZenPrimary)
        assertEquals(Color(0xFF105C43), ShopZenPrimaryDark)
        assertEquals(Color(0xFFB8E986), ShopZenAccent)
        assertEquals(Color(0xFFF7F9F7), ShopZenLightBackground)
        assertEquals(Color(0xFF121A17), ShopZenDarkBackground)
        assertEquals(Color(0xFF1C2922), ShopZenDarkSurface)
        assertEquals(Color(0xFF17221D), ShopZenMainTextLight)
        assertEquals(Color(0xFF68766E), ShopZenSecondaryTextLight)
        assertEquals(Color(0xFF16845B), ShopZenSuccess)
        assertEquals(Color(0xFFE5A323), ShopZenWarning)
        assertEquals(Color(0xFFD74646), ShopZenError)
    }
}

