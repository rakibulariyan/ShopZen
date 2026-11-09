package com.barakwebsoft.shopzen.feature.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barakwebsoft.shopzen.core.util.CurrencyFormatter
import com.barakwebsoft.shopzen.domain.model.Category
import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.repository.CategoryRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingItemRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID

class AddEditItemViewModel(
    private val shoppingListRepository: ShoppingListRepository,
    private val shoppingItemRepository: ShoppingItemRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditItemUiState())
    val uiState: StateFlow<AddEditItemUiState> = _uiState.asStateFlow()

    private var originalItem: ShoppingItem? = null

    fun loadItem(listId: String, itemId: String?) {
        _uiState.update { it.copy(isLoading = true, listId = listId, itemId = itemId) }

        viewModelScope.launch {
            // Ensure default categories exist and observe them
            categoryRepository.ensureDefaultCategories()
            val categories = categoryRepository.getAllCategoriesSync()

            // Load shopping list to get currency
            val list = shoppingListRepository.getListById(listId)
            val currency = list?.currencyCode ?: "USD"

            if (itemId != null) {
                // Edit mode
                val itemDetail = shoppingItemRepository.getItemDetailById(itemId)
                if (itemDetail != null) {
                    val item = itemDetail.item
                    originalItem = item

                    val isStandardUnit = STANDARD_UNITS.contains(item.unit)
                    val selectedUnit = if (isStandardUnit) item.unit else "Custom..."
                    val customUnitStr = if (isStandardUnit) "" else item.unit

                    val estPriceStr = CurrencyFormatter.toMajorUnitsString(item.estimatedUnitPriceMinor, currency)
                    val actPriceStr = CurrencyFormatter.toMajorUnitsString(item.actualUnitPriceMinor, currency)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            listId = listId,
                            itemId = itemId,
                            name = item.name,
                            quantity = item.quantity,
                            unit = selectedUnit,
                            customUnit = customUnitStr,
                            categoryId = item.categoryId,
                            categories = categories,
                            estimatedPriceString = estPriceStr,
                            actualPriceString = actPriceStr,
                            notes = item.notes ?: "",
                            currencyCode = currency,
                            isPurchased = item.isPurchased
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, categories = categories, currencyCode = currency) }
                }
            } else {
                // Add mode
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        listId = listId,
                        itemId = null,
                        categories = categories,
                        currencyCode = currency
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = if (name.isNotBlank()) null else it.nameError
            )
        }
    }

    fun onQuantityChange(quantity: String) {
        _uiState.update {
            it.copy(
                quantity = quantity,
                quantityError = null
            )
        }
    }

    fun adjustQuantity(delta: Int) {
        val currentStr = _uiState.value.quantity.trim().replace(",", ".")
        val currentDecimal = currentStr.toBigDecimalOrNull() ?: BigDecimal.ONE
        val newDecimal = (currentDecimal + BigDecimal.valueOf(delta.toLong())).coerceAtLeast(BigDecimal.ONE)

        // Strip trailing .0 if integer
        val formatted = if (newDecimal.stripTrailingZeros().scale() <= 0) {
            newDecimal.toBigInteger().toString()
        } else {
            newDecimal.toPlainString()
        }

        _uiState.update {
            it.copy(
                quantity = formatted,
                quantityError = null
            )
        }
    }

    fun onUnitChange(unit: String) {
        _uiState.update { it.copy(unit = unit) }
    }

    fun onCustomUnitChange(customUnit: String) {
        _uiState.update { it.copy(customUnit = customUnit) }
    }

    fun onCategoryChange(categoryId: String?) {
        _uiState.update { it.copy(categoryId = categoryId) }
    }

    fun createCategory(name: String, iconName: String, colorToken: String) {
        viewModelScope.launch {
            val newCategory = Category(
                id = "cat_${UUID.randomUUID()}",
                name = name,
                iconName = iconName,
                colorToken = colorToken,
                isDefault = false,
                sortOrder = (_uiState.value.categories.maxOfOrNull { it.sortOrder } ?: 0) + 1
            )
            categoryRepository.addCategory(newCategory)
            val updatedCategories = categoryRepository.getAllCategoriesSync()
            _uiState.update {
                it.copy(
                    categories = updatedCategories,
                    categoryId = newCategory.id
                )
            }
        }
    }

    fun onEstimatedPriceChange(price: String) {
        _uiState.update {
            it.copy(
                estimatedPriceString = price,
                priceError = null
            )
        }
    }

    fun onActualPriceChange(price: String) {
        _uiState.update { it.copy(actualPriceString = price) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun setDeleteDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showDeleteConfirmDialog = visible) }
    }

    fun saveItem(addAnother: Boolean = false) {
        val state = _uiState.value

        val trimmedName = state.name.trim()
        if (trimmedName.isBlank()) {
            _uiState.update { it.copy(nameError = "Item name is required") }
            return
        }

        val trimmedQty = state.quantity.trim().replace(",", ".")
        val qtyDecimal = trimmedQty.toBigDecimalOrNull()
        if (qtyDecimal == null || qtyDecimal <= BigDecimal.ZERO) {
            _uiState.update { it.copy(quantityError = "Enter a valid positive quantity") }
            return
        }

        val estimatedPriceMinor = if (state.estimatedPriceString.isNotBlank()) {
            val parsed = CurrencyFormatter.parseToMinor(state.estimatedPriceString, state.currencyCode)
            if (parsed == null) {
                _uiState.update { it.copy(priceError = "Enter a valid price") }
                return
            }
            parsed
        } else {
            null
        }

        val actualPriceMinor = if (state.actualPriceString.isNotBlank()) {
            CurrencyFormatter.parseToMinor(state.actualPriceString, state.currencyCode)
        } else {
            null
        }

        val finalUnit = state.effectiveUnit.ifBlank { "pcs" }
        val now = System.currentTimeMillis()

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            if (state.isEditing) {
                val existing = originalItem ?: shoppingItemRepository.getItemById(state.itemId ?: "")
                if (existing != null) {
                    val updated = existing.copy(
                        name = trimmedName,
                        quantity = trimmedQty,
                        unit = finalUnit,
                        categoryId = state.categoryId,
                        estimatedUnitPriceMinor = estimatedPriceMinor,
                        actualUnitPriceMinor = actualPriceMinor,
                        notes = state.notes.trim().ifBlank { null },
                        updatedAt = now
                    )
                    shoppingItemRepository.updateItem(updated)
                    _uiState.update { it.copy(isSaving = false, isItemSaved = true) }
                } else {
                    _uiState.update { it.copy(isSaving = false) }
                }
            } else {
                val newItem = ShoppingItem(
                    id = UUID.randomUUID().toString(),
                    listId = state.listId,
                    name = trimmedName,
                    quantity = trimmedQty,
                    unit = finalUnit,
                    categoryId = state.categoryId,
                    estimatedUnitPriceMinor = estimatedPriceMinor,
                    actualUnitPriceMinor = null,
                    isPurchased = false,
                    notes = state.notes.trim().ifBlank { null },
                    createdAt = now,
                    updatedAt = now
                )
                shoppingItemRepository.addItem(newItem)

                if (addAnother) {
                    // Reset form fields for rapid entry, keeping category and unit
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            name = "",
                            quantity = "1",
                            estimatedPriceString = "",
                            notes = "",
                            nameError = null,
                            quantityError = null,
                            priceError = null
                        )
                    }
                } else {
                    _uiState.update { it.copy(isSaving = false, isItemSaved = true) }
                }
            }
        }
    }

    fun deleteItem() {
        val itemId = _uiState.value.itemId ?: return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            shoppingItemRepository.deleteItem(itemId)
            _uiState.update { it.copy(isSaving = false, isItemDeleted = true, showDeleteConfirmDialog = false) }
        }
    }
}

