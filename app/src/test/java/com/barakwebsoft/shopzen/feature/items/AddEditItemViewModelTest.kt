package com.barakwebsoft.shopzen.feature.items

import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.domain.model.Category
import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail
import com.barakwebsoft.shopzen.domain.repository.CategoryRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingItemRepository
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditItemViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeItemRepository: FakeShoppingItemRepository
    private lateinit var fakeListRepository: FakeShoppingListRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var viewModel: AddEditItemViewModel

    private val catProduce = Category(id = "cat-produce", name = "Produce", sortOrder = 0)
    private val catDairy = Category(id = "cat-dairy", name = "Dairy", sortOrder = 1)

    private val sampleList = ShoppingList(
        id = "list-1",
        name = "Weekly Groceries",
        currencyCode = "EUR",
        budgetMinor = 5000L,
        status = ListStatus.ACTIVE,
        createdAt = 1000L,
        updatedAt = 1000L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCategoryRepository = FakeCategoryRepository()
        fakeItemRepository = FakeShoppingItemRepository(fakeCategoryRepository)
        fakeListRepository = FakeShoppingListRepository()

        fakeListRepository.lists["list-1"] = sampleList
        fakeCategoryRepository.categories.addAll(listOf(catProduce, catDairy))

        viewModel = AddEditItemViewModel(
            shoppingListRepository = fakeListRepository,
            shoppingItemRepository = fakeItemRepository,
            categoryRepository = fakeCategoryRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadItem_createMode_loadsCurrencyAndCategories() = runTest {
        viewModel.loadItem(listId = "list-1", itemId = null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isEditing)
        assertEquals("EUR", state.currencyCode)
        assertEquals(2, state.categories.size)
        assertEquals("pcs", state.unit)
        assertEquals("1", state.quantity)
        assertEquals("", state.name)
    }

    @Test
    fun loadItem_editMode_loadsItemDetails() = runTest {
        val existingItem = ShoppingItem(
            id = "item-1",
            listId = "list-1",
            categoryId = "cat-dairy",
            name = "Organic Milk",
            quantity = "2",
            unit = "L",
            estimatedUnitPriceMinor = 250L,
            actualUnitPriceMinor = 275L,
            isPurchased = true,
            sortOrder = 0,
            notes = "Low fat, 1.5%",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        fakeItemRepository.items["item-1"] = existingItem

        viewModel.loadItem(listId = "list-1", itemId = "item-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEditing)
        assertEquals("Organic Milk", state.name)
        assertEquals("2", state.quantity)
        assertEquals("L", state.unit)
        assertEquals("cat-dairy", state.categoryId)
        assertEquals("2.50", state.estimatedPriceString)
        assertEquals("2.75", state.actualPriceString)
        assertEquals("Low fat, 1.5%", state.notes)
        assertTrue(state.isPurchased)
    }

    @Test
    fun loadItem_editMode_customUnit_detectedCorrectly() = runTest {
        val existingItem = ShoppingItem(
            id = "item-custom",
            listId = "list-1",
            name = "Eggs",
            quantity = "1",
            unit = "carton",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        fakeItemRepository.items["item-custom"] = existingItem

        viewModel.loadItem(listId = "list-1", itemId = "item-custom")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Custom...", state.unit)
        assertEquals("carton", state.customUnit)
        assertEquals("carton", state.effectiveUnit)
    }

    @Test
    fun onNameChange_clearsNameError() = runTest {
        viewModel.loadItem(listId = "list-1", itemId = null)
        advanceUntilIdle()

        viewModel.saveItem(addAnother = false)
        assertNotNull(viewModel.uiState.value.nameError)

        viewModel.onNameChange("Bananas")
        assertEquals("Bananas", viewModel.uiState.value.name)
        assertNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun adjustQuantity_incrementsAndDecrements() = runTest {
        viewModel.loadItem(listId = "list-1", itemId = null)
        advanceUntilIdle()

        assertEquals("1", viewModel.uiState.value.quantity)

        viewModel.adjustQuantity(1)
        assertEquals("2", viewModel.uiState.value.quantity)

        viewModel.adjustQuantity(1)
        assertEquals("3", viewModel.uiState.value.quantity)

        viewModel.adjustQuantity(-1)
        assertEquals("2", viewModel.uiState.value.quantity)

        viewModel.adjustQuantity(-1)
        assertEquals("1", viewModel.uiState.value.quantity)

        // Decrementing at 1 stays at 1
        viewModel.adjustQuantity(-1)
        assertEquals("1", viewModel.uiState.value.quantity)
    }

    @Test
    fun estimatedLineTotal_calculatesExactMinorTotal() = runTest {
        viewModel.loadItem(listId = "list-1", itemId = null)
        advanceUntilIdle()

        viewModel.onQuantityChange("3")
        viewModel.onEstimatedPriceChange("2.50")

        val state = viewModel.uiState.value
        assertEquals(750L, state.estimatedLineTotalMinor)
    }

    @Test
    fun saveItem_createMode_persistsItemAndSetsSaved() = runTest {
        viewModel.loadItem(listId = "list-1", itemId = null)
        advanceUntilIdle()

        viewModel.onNameChange("Apples")
        viewModel.onQuantityChange("2.5")
        viewModel.onUnitChange("kg")
        viewModel.onCategoryChange("cat-produce")
        viewModel.onEstimatedPriceChange("3.00")
        viewModel.onNotesChange("Honeycrisp preferred")

        viewModel.saveItem(addAnother = false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isItemSaved)
        assertFalse(state.isSaving)

        assertEquals(1, fakeItemRepository.items.size)
        val savedItem = fakeItemRepository.items.values.first()
        assertEquals("Apples", savedItem.name)
        assertEquals("2.5", savedItem.quantity)
        assertEquals("kg", savedItem.unit)
        assertEquals("cat-produce", savedItem.categoryId)
        assertEquals(300L, savedItem.estimatedUnitPriceMinor)
        assertEquals("Honeycrisp preferred", savedItem.notes)
    }

    @Test
    fun saveItem_createMode_addAnother_retainsCategoryAndUnit_resetsForm() = runTest {
        viewModel.loadItem(listId = "list-1", itemId = null)
        advanceUntilIdle()

        viewModel.onCategoryChange("cat-produce")
        viewModel.onUnitChange("kg")
        viewModel.onNameChange("Carrots")
        viewModel.onQuantityChange("1.5")
        viewModel.onEstimatedPriceChange("1.20")

        viewModel.saveItem(addAnother = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isItemSaved)
        assertEquals(1, fakeItemRepository.items.size)
        assertEquals("Carrots", fakeItemRepository.items.values.first().name)

        // Form reset checks
        assertEquals("", state.name)
        assertEquals("1", state.quantity)
        assertEquals("", state.estimatedPriceString)
        assertEquals("cat-produce", state.categoryId)
        assertEquals("kg", state.unit)
    }

    @Test
    fun saveItem_editMode_updatesItemAndSetsSaved() = runTest {
        val existing = ShoppingItem(
            id = "item-1",
            listId = "list-1",
            name = "Coffee",
            quantity = "1",
            unit = "pcs",
            estimatedUnitPriceMinor = 800L,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        fakeItemRepository.items["item-1"] = existing

        viewModel.loadItem(listId = "list-1", itemId = "item-1")
        advanceUntilIdle()

        viewModel.onQuantityChange("2")
        viewModel.onActualPriceChange("8.50")
        viewModel.saveItem(addAnother = false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isItemSaved)

        val updated = fakeItemRepository.items["item-1"]
        assertNotNull(updated)
        assertEquals("2", updated!!.quantity)
        assertEquals(850L, updated.actualUnitPriceMinor)
    }

    @Test
    fun deleteItem_removesItemAndSetsDeleted() = runTest {
        val existing = ShoppingItem(
            id = "item-to-delete",
            listId = "list-1",
            name = "Old item",
            quantity = "1",
            unit = "pcs",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        fakeItemRepository.items["item-to-delete"] = existing

        viewModel.loadItem(listId = "list-1", itemId = "item-to-delete")
        advanceUntilIdle()

        viewModel.deleteItem()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isItemDeleted)
        assertFalse(fakeItemRepository.items.containsKey("item-to-delete"))
    }

    @Test
    fun createCategory_persistsAndSelectsNewCategory() = runTest {
        viewModel.loadItem(listId = "list-1", itemId = null)
        advanceUntilIdle()

        val initialCategoryCount = viewModel.uiState.value.categories.size

        viewModel.createCategory(
            name = "Frozen Foods",
            iconName = "local_cafe",
            colorToken = "sky"
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(initialCategoryCount + 1, state.categories.size)

        val createdCat = state.categories.find { it.name == "Frozen Foods" }
        assertNotNull(createdCat)
        assertEquals("local_cafe", createdCat!!.iconName)
        assertEquals("sky", createdCat.colorToken)
        assertEquals(createdCat.id, state.categoryId)
    }

    // ---------------- Fake Implementations ----------------

    private class FakeShoppingItemRepository(
        private val categoryRepo: FakeCategoryRepository
    ) : ShoppingItemRepository {
        val items = mutableMapOf<String, ShoppingItem>()

        override fun getItemsForList(listId: String): Flow<List<ShoppingItem>> =
            MutableStateFlow(items.values.filter { it.listId == listId })

        override fun getItemsWithCategoryForList(listId: String): Flow<List<ShoppingItemDetail>> =
            MutableStateFlow(emptyList())

        override fun getPurchasedItemsForList(listId: String): Flow<List<ShoppingItem>> =
            MutableStateFlow(emptyList())

        override fun getPendingItemsForList(listId: String): Flow<List<ShoppingItem>> =
            MutableStateFlow(emptyList())

        override fun observeItemById(id: String): Flow<ShoppingItem?> =
            MutableStateFlow(items[id])

        override fun observeItemDetail(id: String): Flow<ShoppingItemDetail?> =
            MutableStateFlow(null)

        override fun observeItemCount(listId: String): Flow<Int> =
            MutableStateFlow(items.values.count { it.listId == listId })

        override fun observePurchasedItemCount(listId: String): Flow<Int> =
            MutableStateFlow(0)

        override fun searchItemsInList(listId: String, query: String): Flow<List<ShoppingItem>> =
            MutableStateFlow(emptyList())

        override fun searchItemsWithCategoryInList(listId: String, query: String): Flow<List<ShoppingItemDetail>> =
            MutableStateFlow(emptyList())

        override suspend fun getItemById(id: String): ShoppingItem? = items[id]
        override suspend fun getItemDetailById(id: String): ShoppingItemDetail? {
            val item = items[id] ?: return null
            val category = categoryRepo.categories.find { it.id == item.categoryId }
            return ShoppingItemDetail(item = item, category = category)
        }
        override suspend fun getItemsForListSync(listId: String): List<ShoppingItem> =
            items.values.filter { it.listId == listId }

        override suspend fun getItemCount(listId: String): Int = items.values.count { it.listId == listId }
        override suspend fun getPurchasedItemCount(listId: String): Int = 0

        override suspend fun addItem(item: ShoppingItem) {
            items[item.id] = item
        }

        override suspend fun addItems(items: List<ShoppingItem>) {
            items.forEach { this.items[it.id] = it }
        }

        override suspend fun updateItem(item: ShoppingItem) {
            items[item.id] = item
        }

        override suspend fun toggleItemPurchased(id: String, isPurchased: Boolean) {
            items[id]?.let { items[id] = it.copy(isPurchased = isPurchased) }
        }

        override suspend fun updateActualPrice(id: String, actualUnitPriceMinor: Long?) {
            items[id]?.let { items[id] = it.copy(actualUnitPriceMinor = actualUnitPriceMinor) }
        }

        override suspend fun updateItemSortOrder(id: String, sortOrder: Int) {
            items[id]?.let { items[id] = it.copy(sortOrder = sortOrder) }
        }

        override suspend fun deleteItem(id: String) {
            items.remove(id)
        }

        override suspend fun deleteItemsForList(listId: String) {
            items.entries.removeAll { it.value.listId == listId }
        }
    }

    private class FakeShoppingListRepository : ShoppingListRepository {
        val lists = mutableMapOf<String, ShoppingList>()

        override fun getAllLists(): Flow<List<ShoppingList>> = MutableStateFlow(lists.values.toList())
        override fun getListsByStatus(status: ListStatus): Flow<List<ShoppingList>> =
            MutableStateFlow(lists.values.filter { it.status == status })
        override fun getActiveLists(): Flow<List<ShoppingList>> =
            MutableStateFlow(lists.values.filter { it.status == ListStatus.ACTIVE })
        override fun getActiveListDetails(): Flow<List<ShoppingListDetail>> = MutableStateFlow(emptyList())
        override fun getCompletedLists(): Flow<List<ShoppingList>> =
            MutableStateFlow(lists.values.filter { it.status == ListStatus.COMPLETED })
        override fun getArchivedLists(): Flow<List<ShoppingList>> =
            MutableStateFlow(lists.values.filter { it.status == ListStatus.ARCHIVED })
        override fun observeListById(id: String): Flow<ShoppingList?> = MutableStateFlow(lists[id])
        override fun observeListDetail(id: String): Flow<ShoppingListDetail?> = MutableStateFlow(null)
        override fun searchLists(query: String): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun observeActiveListCount(): Flow<Int> = MutableStateFlow(0)
        override fun observeTotalListCount(): Flow<Int> = MutableStateFlow(0)

        override suspend fun getListById(id: String): ShoppingList? = lists[id]
        override suspend fun getListDetailById(id: String): ShoppingListDetail? = null
        override suspend fun getActiveListCount(): Int = 0
        override suspend fun getTotalListCount(): Int = 0
        override suspend fun createList(list: ShoppingList) { lists[list.id] = list }
        override suspend fun updateList(list: ShoppingList) { lists[list.id] = list }
        override suspend fun updateListStatus(id: String, status: ListStatus) {}
        override suspend fun markListCompleted(id: String, completedAt: Long) {}
        override suspend fun deleteList(id: String) { lists.remove(id) }
        override suspend fun duplicateList(listId: String, newName: String?): String = "$listId-copy"
    }

    private class FakeCategoryRepository : CategoryRepository {
        val categories = mutableListOf<Category>()

        override fun getAllCategories(): Flow<List<Category>> = MutableStateFlow(categories)
        override fun observeCategoryById(id: String): Flow<Category?> =
            MutableStateFlow(categories.find { it.id == id })

        override suspend fun getCategoryById(id: String): Category? = categories.find { it.id == id }
        override suspend fun getCategoryByName(name: String): Category? = categories.find { it.name == name }
        override suspend fun getAllCategoriesSync(): List<Category> = categories.toList()
        override suspend fun getCategoryCount(): Int = categories.size
        override suspend fun addCategory(category: Category) { categories.add(category) }
        override suspend fun addCategories(categories: List<Category>) { this.categories.addAll(categories) }
        override suspend fun updateCategory(category: Category) {}
        override suspend fun deleteCategory(id: String) { categories.removeAll { it.id == id } }
        override suspend fun ensureDefaultCategories() {}
    }
}
