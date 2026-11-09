package com.barakwebsoft.shopzen.feature.lists.details

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import com.barakwebsoft.shopzen.core.ui.components.SearchNoResultsView
import com.barakwebsoft.shopzen.core.ui.components.ShopZenSearchBar
import com.barakwebsoft.shopzen.feature.categories.CategoryVisuals
import com.barakwebsoft.shopzen.feature.categories.components.CategoryChip
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barakwebsoft.shopzen.core.di.AppViewModelProvider
import com.barakwebsoft.shopzen.domain.model.Category
import com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail
import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail
import com.barakwebsoft.shopzen.feature.lists.components.ArchiveListDialog
import com.barakwebsoft.shopzen.feature.lists.components.DeleteListDialog
import com.barakwebsoft.shopzen.feature.lists.components.DuplicateListDialog
import com.barakwebsoft.shopzen.feature.lists.details.components.FinancialMetricsCard
import com.barakwebsoft.shopzen.feature.lists.details.components.ListDetailsEmptyState
import com.barakwebsoft.shopzen.feature.lists.details.components.ShoppingItemRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailsScreen(
    listId: String,
    onNavigateBack: () -> Unit,
    onEditListClick: (String) -> Unit,
    onAddItemClick: (String) -> Unit,
    onStartMarketModeClick: (String) -> Unit,
    onItemClick: (String) -> Unit = {},
    viewModel: ListDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(listId) {
        viewModel.loadList(listId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ListDetailsEvent.ItemPurchaseToggled -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoPurchaseToggle(event.action)
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.isListDeleted, uiState.isListArchived) {
        if (uiState.isListDeleted || uiState.isListArchived) {
            onNavigateBack()
        }
    }

    var showArchiveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }

    val listDetail = uiState.listDetail
    val list = listDetail?.list

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = list?.name ?: "List Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (list != null) {
                            Text(
                                text = "${uiState.totalItemsCount} items • ${list.currencyCode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    if (list != null) {
                        // Search Action Toggle
                        IconButton(onClick = { viewModel.toggleSearchExpanded() }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = if (uiState.isSearchExpanded) "Close search" else "Search items",
                                tint = if (uiState.isSearchExpanded || uiState.searchQuery.isNotEmpty()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        // Quick Edit List Button
                        IconButton(onClick = { onEditListClick(listId) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit list details",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Sort Menu Action
                        var sortMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { sortMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort order",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                ItemSortOrder.values().forEach { order ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = order.displayName,
                                                fontWeight = if (uiState.sortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                                color = if (uiState.sortOrder == order) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        leadingIcon = {
                                            if (uiState.sortOrder == order) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        },
                                        onClick = {
                                            sortMenuExpanded = false
                                            viewModel.setSortOrder(order)
                                        }
                                    )
                                }
                            }
                        }

                        // More Options (Duplicate, Archive, Delete)
                        var optionsMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { optionsMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More list options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = optionsMenuExpanded,
                                onDismissRequest = { optionsMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Duplicate list") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        optionsMenuExpanded = false
                                        showDuplicateDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Archive list") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Archive,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        optionsMenuExpanded = false
                                        showArchiveDialog = true
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Delete list",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        optionsMenuExpanded = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (list != null && uiState.totalItemsCount > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mini Add Item Button
                        Button(
                            onClick = { onAddItemClick(listId) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .size(50.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add item",
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Primary Action: Start Market Mode
                        Button(
                            onClick = { onStartMarketModeClick(listId) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start Market Mode",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            listDetail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "List not found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                val currentList = listDetail.list
                val currencyCode = currentList.currencyCode

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Search Bar (when expanded or search query is present)
                    if (uiState.isSearchExpanded || uiState.searchQuery.isNotEmpty()) {
                        item(key = "search_bar") {
                            ShopZenSearchBar(
                                query = uiState.searchQuery,
                                onQueryChange = viewModel::onSearchQueryChange,
                                onClearQuery = viewModel::clearSearchQuery,
                                placeholder = "Search items in list..."
                            )
                        }
                    }

                    // 1. Financial Metrics Bento Card
                    item(key = "financial_metrics") {
                        FinancialMetricsCard(listDetail = listDetail)
                    }

                    // Purchase Status Filter Chips (All, Pending, Purchased)
                    if (uiState.totalItemsCount > 0) {
                        item(key = "purchase_status_filters") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ItemPurchaseFilter.values().forEach { filter ->
                                    val count = when (filter) {
                                        ItemPurchaseFilter.ALL -> uiState.totalItemsCount
                                        ItemPurchaseFilter.PENDING -> uiState.pendingItemsCount
                                        ItemPurchaseFilter.PURCHASED -> uiState.purchasedItemsCount
                                    }
                                    val selected = uiState.purchaseFilter == filter
                                    FilterChip(
                                        selected = selected,
                                        onClick = { viewModel.setPurchaseFilter(filter) },
                                        shape = RoundedCornerShape(20.dp),
                                        label = {
                                            Text(
                                                text = "${filter.displayName} ($count)",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            labelColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = selected,
                                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                                            selectedBorderColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 2. Category Filter Chips Row
                    if (uiState.availableCategories.size > 1) {
                        item(key = "category_filters") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CategoryChip(
                                    category = null,
                                    selected = uiState.selectedCategoryId == null,
                                    onClick = { viewModel.setCategoryFilter(null) },
                                    count = uiState.totalItemsCount,
                                    noneLabel = "All"
                                )

                                uiState.availableCategories.forEach { (cat, count) ->
                                    val catId = cat?.id
                                    CategoryChip(
                                        category = cat,
                                        selected = uiState.selectedCategoryId == catId,
                                        onClick = { viewModel.setCategoryFilter(catId) },
                                        count = count,
                                        noneLabel = "Uncategorized"
                                    )
                                }
                            }
                        }
                    }

                    // 3. List Content or Empty State
                    if (uiState.totalItemsCount == 0) {
                        item(key = "empty_list") {
                            ListDetailsEmptyState(
                                onAddFirstItemClick = { onAddItemClick(listId) }
                            )
                        }
                    } else if (uiState.isFilterEmpty) {
                        item(key = "search_no_results") {
                            SearchNoResultsView(
                                query = uiState.searchQuery,
                                onResetFilters = { viewModel.resetFilters() },
                                onCreateItemClick = { onAddItemClick(listId) }
                            )
                        }
                    } else if (uiState.sortOrder == ItemSortOrder.CATEGORY) {
                        // Category-Grouped Layout
                        uiState.groupedByCategory.forEach { (category, itemsInCategory) ->
                            item(key = "category_header_${category?.id ?: "uncategorized"}") {
                                CategorySectionHeader(
                                    category = category,
                                    items = itemsInCategory
                                )
                            }

                            items(
                                items = itemsInCategory,
                                key = { it.item.id }
                            ) { itemDetail ->
                                ShoppingItemRow(
                                    itemDetail = itemDetail,
                                    currencyCode = currencyCode,
                                    onTogglePurchased = { current ->
                                        viewModel.toggleItemPurchased(itemDetail.item.id, current)
                                    },
                                    onItemClick = { onItemClick(itemDetail.item.id) }
                                )
                            }
                        }
                    } else {
                        // Flat Ordered Layout
                        items(
                            items = uiState.filteredAndSortedItems,
                            key = { it.item.id }
                        ) { itemDetail ->
                            ShoppingItemRow(
                                itemDetail = itemDetail,
                                currencyCode = currencyCode,
                                onTogglePurchased = { current ->
                                    viewModel.toggleItemPurchased(itemDetail.item.id, current)
                                },
                                onItemClick = { onItemClick(itemDetail.item.id) }
                            )
                        }
                    }

                    // Spacer at bottom for comfortable scrolling above dock
                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    if (showArchiveDialog && listDetail != null) {
        ArchiveListDialog(
            listDetail = listDetail,
            onConfirmArchive = { keepCopy ->
                viewModel.archiveList(keepCopy)
                showArchiveDialog = false
            },
            onDismiss = { showArchiveDialog = false }
        )
    }

    if (showDeleteDialog && list != null) {
        DeleteListDialog(
            list = list,
            totalItemsCount = uiState.totalItemsCount,
            onConfirmDelete = {
                viewModel.deleteList()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showDuplicateDialog && list != null) {
        DuplicateListDialog(
            list = list,
            totalItemsCount = uiState.totalItemsCount,
            onConfirmDuplicate = { newName ->
                viewModel.duplicateList(newName)
                showDuplicateDialog = false
            },
            onDismiss = { showDuplicateDialog = false }
        )
    }
}

@Composable
private fun CategorySectionHeader(
    category: Category?,
    items: List<ShoppingItemDetail>,
    modifier: Modifier = Modifier
) {
    val purchasedCount = items.count { it.item.isPurchased }
    val totalCount = items.size
    val icon = CategoryVisuals.getCategoryIcon(category?.iconName)
    val iconColor = CategoryVisuals.getCategoryColor(category?.colorToken)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category?.name ?: "Uncategorized",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        ) {
            Text(
                text = "$purchasedCount/$totalCount bought",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

