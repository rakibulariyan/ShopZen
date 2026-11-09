package com.barakwebsoft.shopzen.integration

import com.barakwebsoft.shopzen.core.util.MoneyCalculator
import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.data.repository.CategoryRepositoryImpl
import com.barakwebsoft.shopzen.data.repository.HistoryRepositoryImpl
import com.barakwebsoft.shopzen.data.repository.ShoppingItemRepositoryImpl
import com.barakwebsoft.shopzen.data.repository.ShoppingListRepositoryImpl
import com.barakwebsoft.shopzen.domain.model.Category
import com.barakwebsoft.shopzen.domain.model.ShoppingItem
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import com.barakwebsoft.shopzen.testutil.FakeCategoryDao
import com.barakwebsoft.shopzen.testutil.FakeHistoryDao
import com.barakwebsoft.shopzen.testutil.FakeShoppingItemDao
import com.barakwebsoft.shopzen.testutil.FakeShoppingListDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * End-to-end critical journey and regression tests (SZ-060).
 * Validates the core shopping lifecycle:
 * 1. List creation with budget and currency
 * 2. Item management (priced, unpriced, unit variations)
 * 3. Market Mode shopping flow (checkoffs, price updates)
 * 4. Undo accidental purchase toggle
 * 5. Budget calculation, over-budget warning, and unpriced item disclosure
 * 6. Atomic archive to history and immutable snapshot preservation upon list deletion
 * 7. List duplication with fresh IDs and reset purchase state
 */
class CriticalShoppingJourneyTest {

    private lateinit var fakeListDao: FakeShoppingListDao
    private lateinit var fakeItemDao: FakeShoppingItemDao
    private lateinit var fakeCategoryDao: FakeCategoryDao
    private lateinit var fakeHistoryDao: FakeHistoryDao

    private lateinit var listRepository: ShoppingListRepositoryImpl
    private lateinit var itemRepository: ShoppingItemRepositoryImpl
    private lateinit var categoryRepository: CategoryRepositoryImpl
    private lateinit var historyRepository: HistoryRepositoryImpl

    private val produceCategory = Category(
        id = "cat_produce",
        name = "Produce",
        iconName = "Fruit",
        colorToken = "Green",
        isDefault = false,
        sortOrder = 0
    )

    private val pantryCategory = Category(
        id = "cat_pantry",
        name = "Pantry",
        iconName = "Kitchen",
        colorToken = "Amber",
        isDefault = false,
        sortOrder = 1
    )

    @Before
    fun setUp() = runBlocking {
        fakeListDao = FakeShoppingListDao()
        fakeItemDao = FakeShoppingItemDao()
        fakeCategoryDao = FakeCategoryDao()
        fakeHistoryDao = FakeHistoryDao()

        val dispatcher = Dispatchers.Unconfined
        listRepository = ShoppingListRepositoryImpl(fakeListDao, fakeItemDao, dispatcher)
        itemRepository = ShoppingItemRepositoryImpl(fakeItemDao, dispatcher)
        categoryRepository = CategoryRepositoryImpl(fakeCategoryDao, dispatcher)
        historyRepository = HistoryRepositoryImpl(
            historyDao = fakeHistoryDao,
            shoppingListDao = fakeListDao,
            shoppingItemDao = fakeItemDao,
            database = null,
            ioDispatcher = dispatcher
        )

        categoryRepository.addCategory(produceCategory)
        categoryRepository.addCategory(pantryCategory)
    }

    @Test
    fun completeShoppingJourney_createList_addItems_marketMode_undo_overBudget_archive_andVerifySnapshotIsolation() = runBlocking {
        // Step 1: Create a shopping list with budget ($40.00 = 4000 minor units) and currency (USD)
        val listId = UUID.randomUUID().toString()
        val originalList = ShoppingList(
            id = listId,
            name = "Weekend Farmers Market",
            currencyCode = "USD",
            budgetMinor = 4000L,
            status = ListStatus.ACTIVE
        )
        listRepository.createList(originalList)

        val retrievedList = listRepository.getListById(listId)
        assertNotNull(retrievedList)
        assertEquals("Weekend Farmers Market", retrievedList?.name)
        assertEquals(4000L, retrievedList?.budgetMinor)
        assertEquals("USD", retrievedList?.currencyCode)

        // Step 2: Add items to list
        // - Item 1: Strawberries (2.0 boxes @ $4.50 = 450L -> estimated line total: 900L)
        val itemStrawberries = ShoppingItem(
            id = UUID.randomUUID().toString(),
            listId = listId,
            categoryId = produceCategory.id,
            name = "Strawberries",
            quantity = "2",
            unit = "box",
            estimatedUnitPriceMinor = 450L,
            sortOrder = 0
        )
        itemRepository.addItem(itemStrawberries)

        // - Item 2: Avocados (4.0 pcs @ $1.50 = 150L -> estimated line total: 600L)
        val itemAvocados = ShoppingItem(
            id = UUID.randomUUID().toString(),
            listId = listId,
            categoryId = produceCategory.id,
            name = "Avocados",
            quantity = "4",
            unit = "pcs",
            estimatedUnitPriceMinor = 150L,
            sortOrder = 1
        )
        itemRepository.addItem(itemAvocados)

        // - Item 3: Fresh Mint (1.0 bunch @ unpriced -> estimated line total: null)
        val itemMint = ShoppingItem(
            id = UUID.randomUUID().toString(),
            listId = listId,
            categoryId = produceCategory.id,
            name = "Fresh Mint",
            quantity = "1",
            unit = "bunch",
            estimatedUnitPriceMinor = null,
            sortOrder = 2
        )
        itemRepository.addItem(itemMint)

        // Step 3: Validate initial list calculations
        var items = itemRepository.getItemsForList(listId).first()
        assertEquals(3, items.size)

        var metrics = MoneyCalculator.calculateBudgetMetrics(retrievedList?.budgetMinor, items)
        assertEquals(3, metrics.totalItemsCount)
        assertEquals(0, metrics.purchasedCount)
        assertEquals(3, metrics.pendingCount)
        assertEquals(1500L, metrics.estimatedTotalMinor) // 900L + 600L
        assertEquals(0L, metrics.actualSpentMinor)
        assertFalse(metrics.isOverBudget)
        assertEquals(1, metrics.unpricedItemsCount) // Fresh Mint

        // Step 4: Market Mode Shopping Flow
        // Mark Strawberries as purchased with a discounted actual price ($4.00 = 400L -> actual line total: 800L)
        itemRepository.toggleItemPurchased(itemStrawberries.id, true)
        itemRepository.updateActualPrice(itemStrawberries.id, 400L)

        // Mark Fresh Mint as purchased with no actual price entered (unpriced item)
        itemRepository.toggleItemPurchased(itemMint.id, true)

        items = itemRepository.getItemsForList(listId).first()
        metrics = MoneyCalculator.calculateBudgetMetrics(retrievedList?.budgetMinor, items)

        assertEquals(2, metrics.purchasedCount)
        assertEquals(1, metrics.pendingCount)
        assertEquals(800L, metrics.actualSpentMinor)
        assertEquals(1, metrics.countPurchasedLackingActualPrice)
        assertTrue(metrics.isSpendEstimated) // Mandated by PRD: must disclose that spend is incomplete

        // Step 5: Undo accidental purchase toggle
        // User accidentally toggled Fresh Mint; undo it
        itemRepository.toggleItemPurchased(itemMint.id, false)

        items = itemRepository.getItemsForList(listId).first()
        metrics = MoneyCalculator.calculateBudgetMetrics(retrievedList?.budgetMinor, items)

        assertEquals(1, metrics.purchasedCount)
        assertEquals(2, metrics.pendingCount)
        assertEquals(800L, metrics.actualSpentMinor)
        assertEquals(0, metrics.countPurchasedLackingActualPrice)
        assertFalse(metrics.isSpendEstimated)

        // Step 6: Over-budget Warning Flow
        // Add a high-cost gourmet item
        val itemOliveOil = ShoppingItem(
            id = UUID.randomUUID().toString(),
            listId = listId,
            categoryId = pantryCategory.id,
            name = "Artisan Olive Oil",
            quantity = "1",
            unit = "bottle",
            estimatedUnitPriceMinor = 2000L,
            sortOrder = 3
        )
        itemRepository.addItem(itemOliveOil)

        // Purchase Avocados at expected price ($1.50 -> 600L)
        itemRepository.toggleItemPurchased(itemAvocados.id, true)
        itemRepository.updateActualPrice(itemAvocados.id, 150L)

        // Purchase Olive Oil with actual price spike ($35.00 = 3500L)
        itemRepository.toggleItemPurchased(itemOliveOil.id, true)
        itemRepository.updateActualPrice(itemOliveOil.id, 3500L)

        // Actual spend: 800L (Strawberries) + 600L (Avocados) + 3500L (Olive Oil) = 4900L ($49.00)
        // Budget: 4000L ($40.00) -> Over-budget by 900L ($9.00)
        items = itemRepository.getItemsForList(listId).first()
        metrics = MoneyCalculator.calculateBudgetMetrics(retrievedList?.budgetMinor, items)

        assertTrue("Should be flagged as over-budget", metrics.isOverBudget)
        assertEquals(4900L, metrics.actualSpentMinor)
        assertEquals(900L, metrics.overBudgetMinor)
        assertEquals(-900L, metrics.remainingBudgetMinor)
        assertEquals(3, metrics.purchasedCount)
        assertEquals(1, metrics.pendingCount) // Mint remains pending

        // Step 7: Atomic Archive to History
        val historyId = historyRepository.archiveListToHistory(listId)
        assertNotNull("History ID should be generated upon archive", historyId)

        // Verify history snapshot content
        val historyDetail = historyRepository.getHistoryDetailById(historyId)
        assertNotNull("History detail must be retrievable", historyDetail)
        assertEquals("Weekend Farmers Market", historyDetail?.history?.listName)
        assertEquals(4900L, historyDetail?.history?.totalSpentMinor)
        assertEquals("USD", historyDetail?.history?.currencyCode)
        assertEquals(4, historyDetail?.items?.size)

        // Step 8: Absolute Snapshot Isolation and Immutability
        // Delete the original shopping list and its items completely from the database
        listRepository.deleteList(listId)
        itemRepository.deleteItemsForList(listId)
        assertNull("Original list must be deleted", listRepository.getListById(listId))
        val remainingItems = itemRepository.getItemsForList(listId).first()
        assertTrue("Original items should be cleared", remainingItems.isEmpty())

        // Verify history snapshot remains completely intact, unchanged, and retrievable
        val persistentHistory = historyRepository.getHistoryDetailById(historyId)
        assertNotNull("Historical snapshot must persist after original list deletion", persistentHistory)
        assertEquals(4, persistentHistory?.items?.size)
        assertEquals(4900L, persistentHistory?.history?.totalSpentMinor)
        assertEquals("USD", persistentHistory?.history?.currencyCode)

        val snapStrawberries = persistentHistory?.items?.find { it.itemName == "Strawberries" }
        assertNotNull(snapStrawberries)
        assertEquals(400L, snapStrawberries?.actualUnitPriceMinor)
        assertEquals(800L, snapStrawberries?.lineTotalMinor)
        assertTrue(snapStrawberries?.isPurchased == true)
    }

    @Test
    fun duplicateShoppingList_createsFreshIds_andResetsPurchasedAndActualPrices() = runBlocking {
        // Arrange: Create a list with purchased and priced items
        val origListId = UUID.randomUUID().toString()
        val origList = ShoppingList(
            id = origListId,
            name = "Weekly Essentials",
            currencyCode = "EUR",
            budgetMinor = 6000L,
            status = ListStatus.ACTIVE
        )
        listRepository.createList(origList)

        val item1 = ShoppingItem(
            id = UUID.randomUUID().toString(),
            listId = origListId,
            name = "Oat Milk",
            quantity = "2",
            unit = "pcs",
            estimatedUnitPriceMinor = 250L,
            actualUnitPriceMinor = 240L,
            isPurchased = true
        )
        val item2 = ShoppingItem(
            id = UUID.randomUUID().toString(),
            listId = origListId,
            name = "Coffee Beans",
            quantity = "1",
            unit = "bag",
            estimatedUnitPriceMinor = 1200L,
            actualUnitPriceMinor = 1200L,
            isPurchased = true
        )
        itemRepository.addItem(item1)
        itemRepository.addItem(item2)

        // Act: Duplicate list
        val duplicatedListId = listRepository.duplicateList(origListId, "Weekly Essentials (Copy)")

        // Assert: Duplicated list has unique ID, correct name, and ACTIVE status
        assertNotNull(duplicatedListId)
        assertNotEquals(origListId, duplicatedListId)

        val dupList = listRepository.getListById(duplicatedListId)
        assertNotNull(dupList)
        assertEquals("Weekly Essentials (Copy)", dupList?.name)
        assertEquals("EUR", dupList?.currencyCode)
        assertEquals(6000L, dupList?.budgetMinor)
        assertEquals(ListStatus.ACTIVE, dupList?.status)

        // Assert: Duplicated items have new IDs, reset purchase state (false), and reset actual prices (null)
        val dupItems = itemRepository.getItemsForList(duplicatedListId).first()
        assertEquals(2, dupItems.size)
        assertTrue(dupItems.none { it.id == item1.id || it.id == item2.id })
        assertTrue("Duplicated items must have isPurchased == false", dupItems.all { !it.isPurchased })
        assertTrue("Duplicated items must have actualUnitPriceMinor == null", dupItems.all { it.actualUnitPriceMinor == null })
        assertEquals("2", dupItems.find { it.name == "Oat Milk" }?.quantity)
        assertEquals(250L, dupItems.find { it.name == "Oat Milk" }?.estimatedUnitPriceMinor)

        // Original list and items remain unmodified
        val origItems = itemRepository.getItemsForList(origListId).first()
        assertTrue(origItems.all { it.isPurchased })
    }

    @Test
    fun budgetHealthMetrics_unpricedPurchasedItems_strictlyDisclosedAndNeverTreatedAsZeroSpend() {
        val itemPriced = ShoppingItem(
            id = "1",
            listId = "list_test",
            name = "Bread",
            quantity = "1",
            unit = "loaf",
            estimatedUnitPriceMinor = 300L,
            actualUnitPriceMinor = 300L,
            isPurchased = true
        )
        val itemUnpriced = ShoppingItem(
            id = "2",
            listId = "list_test",
            name = "Mystery Produce",
            quantity = "1",
            unit = "pcs",
            estimatedUnitPriceMinor = null,
            actualUnitPriceMinor = null,
            isPurchased = true
        )

        val metrics = MoneyCalculator.calculateBudgetMetrics(1000L, listOf(itemPriced, itemUnpriced))

        assertEquals(2, metrics.totalItemsCount)
        assertEquals(2, metrics.purchasedCount)
        assertEquals(0, metrics.pendingCount)
        assertEquals(300L, metrics.actualSpentMinor)
        assertEquals(1, metrics.countPurchasedLackingActualPrice)
        assertTrue("PRD mandate: UI must clearly disclose spend is incomplete", metrics.isSpendEstimated)
    }
}
