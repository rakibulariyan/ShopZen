package com.barakwebsoft.shopzen.feature.categories

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CategoryVisualsTest {

    @Test
    fun getCategoryIcon_knownIconNames_returnsSpecificIcons() {
        assertEquals(Icons.Default.Eco, CategoryVisuals.getCategoryIcon("eco"))
        assertEquals(Icons.Default.Egg, CategoryVisuals.getCategoryIcon("egg"))
        assertEquals(Icons.Default.Spa, CategoryVisuals.getCategoryIcon("spa"))
    }

    @Test
    fun getCategoryIcon_unknownOrNull_returnsDefaultCategoryIcon() {
        assertEquals(Icons.Default.Category, CategoryVisuals.getCategoryIcon(null))
        assertEquals(Icons.Default.Category, CategoryVisuals.getCategoryIcon("unknown_icon"))
        assertEquals(Icons.Default.Category, CategoryVisuals.getCategoryIcon(""))
    }

    @Test
    fun getCategoryColor_knownTokens_returnsConfiguredColors() {
        assertEquals(Color(0xFF16845B), CategoryVisuals.getCategoryColor("primary"))
        assertEquals(Color(0xFF4A7C59), CategoryVisuals.getCategoryColor("secondary"))
        assertEquals(Color(0xFFD97706), CategoryVisuals.getCategoryColor("tertiary"))
        assertEquals(Color(0xFF65A30D), CategoryVisuals.getCategoryColor("accent"))
    }

    @Test
    fun getCategoryColor_unknownOrNull_returnsDefaultColor() {
        assertEquals(Color(0xFF16845B), CategoryVisuals.getCategoryColor(null))
        assertEquals(Color(0xFF16845B), CategoryVisuals.getCategoryColor("non_existent"))
    }

    @Test
    fun availableCatalogs_containUniqueIdentifiers() {
        val iconNames = CategoryVisuals.AVAILABLE_ICONS.map { it.iconName }
        assertEquals(iconNames.size, iconNames.distinct().size)

        val colorTokens = CategoryVisuals.AVAILABLE_COLORS.map { it.token }
        assertEquals(colorTokens.size, colorTokens.distinct().size)

        assertNotNull(CategoryVisuals.AVAILABLE_ICONS.find { it.iconName == "eco" })
        assertNotNull(CategoryVisuals.AVAILABLE_COLORS.find { it.token == "primary" })
    }
}

