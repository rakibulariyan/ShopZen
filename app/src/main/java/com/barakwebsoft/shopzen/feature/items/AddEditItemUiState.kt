package com.barakwebsoft.shopzen.feature.items

import com.barakwebsoft.shopzen.core.util.CurrencyFormatter
import com.barakwebsoft.shopzen.domain.model.Category
import java.math.BigDecimal

/**
 * Standard units available for selection in the item form.
 */
val STANDARD_UNITS = listOf(
    "pcs",
    "kg",
    "g",
    "lb",
    "oz",
    "L",
    "ml",
    "pack",
    "dozen",
    "bundle",
    "box",
    "can",
    "bottle",
    "Custom..."
)

/**
 * UI State for the Add/Edit Shopping Item screen.
 */
data class AddEditItemUiState(
    val listId: String = "",
    val itemId: String? = null,
    val name: String = "",
    val quantity: String = "1",
    val unit: String = "pcs",
    val customUnit: String = "",
    val categoryId: String? = null,
    val categories: List<Category> = emptyList(),
    val estimatedPriceString: String = "",
    val notes: String = "",
    val currencyCode: String = "USD",
    val isPurchased: Boolean = false,
    val actualPriceString: String = "",
    val nameError: String? = null,
    val quantityError: String? = null,
    val priceError: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isItemSaved: Boolean = false,
    val isItemDeleted: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false
) {
    val isEditing: Boolean get() = itemId != null

    val effectiveUnit: String
        get() = if (unit == "Custom...") customUnit.trim() else unit

    /**
     * Estimated line total in minor units: quantity * estimatedUnitPriceMinor.
     * Uses exact BigDecimal math with no floating-point inaccuracies.
     */
    val estimatedLineTotalMinor: Long?
        get() {
            val priceMinor = CurrencyFormatter.parseToMinor(estimatedPriceString, currencyCode) ?: return null
            return com.barakwebsoft.shopzen.core.util.MoneyCalculator.calculateLineTotal(priceMinor, quantity)
        }

    val formattedLineTotal: String
        get() = CurrencyFormatter.format(estimatedLineTotalMinor, currencyCode)
}

