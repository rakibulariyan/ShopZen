package com.barakwebsoft.shopzen.feature.lists

import com.barakwebsoft.shopzen.data.local.entity.ListStatus
import com.barakwebsoft.shopzen.domain.model.AppPreferences
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail
import com.barakwebsoft.shopzen.domain.model.ThemePreference
import com.barakwebsoft.shopzen.domain.repository.ShoppingListRepository
import com.barakwebsoft.shopzen.domain.repository.UserPreferencesRepository
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
class AddEditListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeListRepository: FakeShoppingListRepository
    private lateinit var fakePrefsRepository: FakeUserPreferencesRepository
    private lateinit var viewModel: AddEditListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeListRepository = FakeShoppingListRepository()
        fakePrefsRepository = FakeUserPreferencesRepository()
        viewModel = AddEditListViewModel(fakeListRepository, fakePrefsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadList_createMode_setsDefaultCurrencyFromPreferences() = runTest {
        fakePrefsRepository.setDefaultCurrency("EUR")

        viewModel.loadList(null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isEditing)
        assertEquals("EUR", state.currencyCode)
        assertEquals("", state.name)
        assertEquals("", state.budgetString)
    }

    @Test
    fun loadList_editMode_loadsExistingListFields() = runTest {
        val existingList = ShoppingList(
            id = "list-abc",
            name = "Party Groceries",
            description = "Snacks and drinks",
            currencyCode = "GBP",
            budgetMinor = 4550L
        )
        fakeListRepository.storedLists["list-abc"] = existingList

        viewModel.loadList("list-abc")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEditing)
        assertEquals("list-abc", state.listId)
        assertEquals("Party Groceries", state.name)
        assertEquals("Snacks and drinks", state.description)
        assertEquals("GBP", state.currencyCode)
        assertEquals("45.50", state.budgetString)
    }

    @Test
    fun saveList_emptyName_setsNameErrorAndDoesNotSave() = runTest {
        viewModel.loadList(null)
        advanceUntilIdle()

        viewModel.onNameChange("   ")
        viewModel.saveList()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull("Name error must be set for empty name", state.nameError)
        assertFalse(state.isSaved)
        assertTrue(fakeListRepository.createdLists.isEmpty())
    }

    @Test
    fun saveList_invalidBudget_setsBudgetErrorAndDoesNotSave() = runTest {
        viewModel.loadList(null)
        advanceUntilIdle()

        viewModel.onNameChange("Weekly Groceries")
        viewModel.onBudgetChange("not-a-number")
        viewModel.saveList()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull("Budget error must be set for invalid input", state.budgetError)
        assertFalse(state.isSaved)
        assertTrue(fakeListRepository.createdLists.isEmpty())
    }

    @Test
    fun saveList_validNewList_createsListWithExactMinorUnits() = runTest {
        viewModel.loadList(null)
        advanceUntilIdle()

        viewModel.onNameChange("Weekly Groceries")
        viewModel.onDescriptionChange("Weekly fresh produce")
        viewModel.onCurrencyChange("USD")
        viewModel.onBudgetChange("125.50")

        viewModel.saveList()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("List should be marked saved", state.isSaved)
        assertNotNull(state.savedListId)
        assertEquals(1, fakeListRepository.createdLists.size)

        val created = fakeListRepository.createdLists.first()
        assertEquals("Weekly Groceries", created.name)
        assertEquals("Weekly fresh produce", created.description)
        assertEquals("USD", created.currencyCode)
        assertEquals(12550L, created.budgetMinor)
        assertEquals(ListStatus.ACTIVE, created.status)
    }

    @Test
    fun saveList_validExistingList_updatesListWithExactMinorUnits() = runTest {
        val existingList = ShoppingList(
            id = "edit-123",
            name = "Old Name",
            description = "Old Desc",
            currencyCode = "USD",
            budgetMinor = 5000L
        )
        fakeListRepository.storedLists["edit-123"] = existingList

        viewModel.loadList("edit-123")
        advanceUntilIdle()

        viewModel.onNameChange("Updated Name")
        viewModel.onBudgetChange("75.00")

        viewModel.saveList()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("List should be marked saved", state.isSaved)
        assertEquals("edit-123", state.savedListId)
        assertEquals(1, fakeListRepository.updatedLists.size)

        val updated = fakeListRepository.updatedLists.first()
        assertEquals("edit-123", updated.id)
        assertEquals("Updated Name", updated.name)
        assertEquals(7500L, updated.budgetMinor)
    }

    @Test
    fun onNameChange_clearsExistingNameError() = runTest {
        viewModel.loadList(null)
        advanceUntilIdle()

        viewModel.onNameChange("")
        viewModel.saveList()
        assertNotNull(viewModel.uiState.value.nameError)

        viewModel.onNameChange("Valid Name")
        assertNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun currencySelection_perList_overridesDefaultAndSavesCorrectly() = runTest {
        // User preferences default is USD
        fakePrefsRepository.setDefaultCurrency("USD")

        viewModel.loadList(null)
        advanceUntilIdle()
        assertEquals("USD", viewModel.uiState.value.currencyCode)

        // User explicitly changes list currency to JPY
        viewModel.onCurrencyChange("JPY")
        viewModel.onNameChange("Tokyo Trip Groceries")
        viewModel.onBudgetChange("15000") // 15,000 JPY
        viewModel.saveList()
        advanceUntilIdle()

        val created = fakeListRepository.createdLists.first()
        assertEquals("JPY", created.currencyCode)
        assertEquals(15000L, created.budgetMinor)
        assertEquals("Tokyo Trip Groceries", created.name)
    }

    @Test
    fun currencySelection_editExistingList_preservesListSpecificCurrency() = runTest {
        val listJpy = ShoppingList(
            id = "list-jpy",
            name = "Japan List",
            currencyCode = "JPY",
            budgetMinor = 50000L
        )
        fakeListRepository.storedLists["list-jpy"] = listJpy

        // Change global user preference to EUR
        fakePrefsRepository.setDefaultCurrency("EUR")

        viewModel.loadList("list-jpy")
        advanceUntilIdle()

        // Existing list should retain JPY, NOT be overwritten by the EUR global default
        assertEquals("JPY", viewModel.uiState.value.currencyCode)
        assertEquals("50000", viewModel.uiState.value.budgetString)

        // User changes currency from JPY to KRW
        viewModel.onCurrencyChange("KRW")
        viewModel.saveList()
        advanceUntilIdle()

        val updated = fakeListRepository.updatedLists.first()
        assertEquals("KRW", updated.currencyCode)
    }

    private class FakeShoppingListRepository : ShoppingListRepository {
        val storedLists = mutableMapOf<String, ShoppingList>()
        val createdLists = mutableListOf<ShoppingList>()
        val updatedLists = mutableListOf<ShoppingList>()

        override fun getAllLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getListsByStatus(status: ListStatus): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getActiveLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getActiveListDetails(): Flow<List<ShoppingListDetail>> = MutableStateFlow(emptyList())
        override fun getCompletedLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun getArchivedLists(): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun observeListById(id: String): Flow<ShoppingList?> = MutableStateFlow(storedLists[id])
        override fun observeListDetail(id: String): Flow<ShoppingListDetail?> = MutableStateFlow(null)
        override fun searchLists(query: String): Flow<List<ShoppingList>> = MutableStateFlow(emptyList())
        override fun observeActiveListCount(): Flow<Int> = MutableStateFlow(0)
        override fun observeTotalListCount(): Flow<Int> = MutableStateFlow(0)

        override suspend fun getListById(id: String): ShoppingList? = storedLists[id]
        override suspend fun getListDetailById(id: String): ShoppingListDetail? = null
        override suspend fun getActiveListCount(): Int = 0
        override suspend fun getTotalListCount(): Int = 0

        override suspend fun createList(list: ShoppingList) {
            createdLists.add(list)
            storedLists[list.id] = list
        }

        override suspend fun updateList(list: ShoppingList) {
            updatedLists.add(list)
            storedLists[list.id] = list
        }

        override suspend fun updateListStatus(id: String, status: ListStatus) {}
        override suspend fun markListCompleted(id: String, completedAt: Long) {}
        override suspend fun deleteList(id: String) {
            storedLists.remove(id)
        }
        override suspend fun duplicateList(listId: String, newName: String?): String = "$listId-copy"
    }

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        private val prefsFlow = MutableStateFlow(AppPreferences(defaultCurrencyCode = "USD"))

        override fun getPreferences(): Flow<AppPreferences> = prefsFlow

        override suspend fun setThemePreference(theme: ThemePreference) {
            prefsFlow.value = prefsFlow.value.copy(themePreference = theme)
        }

        override suspend fun setDefaultCurrency(currencyCode: String) {
            prefsFlow.value = prefsFlow.value.copy(defaultCurrencyCode = currencyCode)
        }

        override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
            prefsFlow.value = prefsFlow.value.copy(hapticFeedbackEnabled = enabled)
        }

        override suspend fun setShowCompletedItems(show: Boolean) {
            prefsFlow.value = prefsFlow.value.copy(showCompletedItems = show)
        }
    }
}

