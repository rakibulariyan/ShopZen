package com.barakwebsoft.shopzen.feature.categories

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryIconOption(
    val iconName: String,
    val label: String,
    val icon: ImageVector
)

data class CategoryColorOption(
    val token: String,
    val label: String,
    val color: Color
)

object CategoryVisuals {

    val AVAILABLE_ICONS: List<CategoryIconOption> = listOf(
        CategoryIconOption("eco", "Produce & Greens", Icons.Default.Eco),
        CategoryIconOption("nutrition", "Fruits & Nutrition", Icons.Default.Restaurant),
        CategoryIconOption("egg", "Dairy & Eggs", Icons.Default.Egg),
        CategoryIconOption("bakery_dining", "Bakery", Icons.Default.BakeryDining),
        CategoryIconOption("shopping_basket", "Pantry & Groceries", Icons.Default.ShoppingBasket),
        CategoryIconOption("set_meal", "Meat & Seafood", Icons.Default.SetMeal),
        CategoryIconOption("cleaning_services", "Cleaning & Household", Icons.Default.CleaningServices),
        CategoryIconOption("spa", "Personal Care & Beauty", Icons.Default.Spa),
        CategoryIconOption("local_cafe", "Beverages & Coffee", Icons.Default.LocalCafe),
        CategoryIconOption("pets", "Pet Supplies", Icons.Default.Pets),
        CategoryIconOption("medication", "Pharmacy & Health", Icons.Default.Medication),
        CategoryIconOption("wine_bar", "Drinks & Snacks", Icons.Default.WineBar),
        CategoryIconOption("category", "General / Other", Icons.Default.Category)
    )

    val AVAILABLE_COLORS: List<CategoryColorOption> = listOf(
        CategoryColorOption("primary", "Botanical Green", Color(0xFF16845B)),
        CategoryColorOption("secondary", "Sage", Color(0xFF4A7C59)),
        CategoryColorOption("tertiary", "Goldenrod", Color(0xFFD97706)),
        CategoryColorOption("accent", "Fresh Lime", Color(0xFF65A30D)),
        CategoryColorOption("sky", "Sky Blue", Color(0xFF0284C7)),
        CategoryColorOption("violet", "Purple", Color(0xFF7C3AED)),
        CategoryColorOption("rose", "Coral Rose", Color(0xFFE11D48)),
        CategoryColorOption("outline", "Slate Grey", Color(0xFF64748B))
    )

    fun getCategoryIcon(iconName: String?): ImageVector {
        return AVAILABLE_ICONS.firstOrNull { it.iconName == iconName }?.icon ?: Icons.Default.Category
    }

    fun getCategoryColor(colorToken: String?): Color {
        return AVAILABLE_COLORS.firstOrNull { it.token == colorToken }?.color ?: Color(0xFF16845B)
    }
}

