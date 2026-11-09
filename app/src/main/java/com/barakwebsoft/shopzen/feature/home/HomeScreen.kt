package com.barakwebsoft.shopzen.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barakwebsoft.shopzen.core.di.AppViewModelProvider
import com.barakwebsoft.shopzen.core.ui.components.SearchNoResultsView
import com.barakwebsoft.shopzen.core.ui.components.ShopZenSearchBar
import com.barakwebsoft.shopzen.core.util.CurrencyFormatter
import com.barakwebsoft.shopzen.domain.model.ShoppingList
import com.barakwebsoft.shopzen.domain.model.ShoppingListDetail
import com.barakwebsoft.shopzen.feature.lists.components.ArchiveListDialog
import com.barakwebsoft.shopzen.feature.lists.components.DeleteListDialog
import com.barakwebsoft.shopzen.feature.lists.components.DuplicateListDialog
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(
    onCreateListClick: () -> Unit = {},
    onListClick: (String) -> Unit = {},
    onEditListClick: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var listToArchive by remember { mutableStateOf<ShoppingListDetail?>(null) }
    var listToDelete by remember { mutableStateOf<ShoppingListDetail?>(null) }
    var listToDuplicate by remember { mutableStateOf<ShoppingListDetail?>(null) }

    HomeScreenContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onClearSearch = viewModel::clearSearchQuery,
        onCreateListClick = onCreateListClick,
        onListClick = onListClick,
        onEditListClick = onEditListClick,
        onArchiveListClick = { listToArchive = it },
        onDeleteListClick = { listToDelete = it },
        onDuplicateListClick = { listToDuplicate = it },
        modifier = modifier
    )

    listToArchive?.let { detail ->
        ArchiveListDialog(
            listDetail = detail,
            onConfirmArchive = { keepDuplicateCopy ->
                viewModel.archiveList(detail.list.id, keepDuplicateCopy)
                listToArchive = null
            },
            onDismiss = { listToArchive = null }
        )
    }

    listToDelete?.let { detail ->
        DeleteListDialog(
            list = detail.list,
            totalItemsCount = detail.totalItemsCount,
            onConfirmDelete = {
                viewModel.deleteList(detail.list.id)
                listToDelete = null
            },
            onDismiss = { listToDelete = null }
        )
    }

    listToDuplicate?.let { detail ->
        DuplicateListDialog(
            list = detail.list,
            totalItemsCount = detail.totalItemsCount,
            onConfirmDuplicate = { newName ->
                viewModel.duplicateList(detail.list.id, newName)
                listToDuplicate = null
            },
            onDismiss = { listToDuplicate = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onCreateListClick: () -> Unit,
    onListClick: (String) -> Unit,
    onEditListClick: (String) -> Unit = {},
    onArchiveListClick: (ShoppingListDetail) -> Unit = {},
    onDeleteListClick: (ShoppingListDetail) -> Unit = {},
    onDuplicateListClick: (ShoppingListDetail) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "ShopZen",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Offline Indicator Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Offline indicator",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "100% Offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateListClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New List", fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.semantics {
                    contentDescription = "Create shopping list"
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                uiState.isTrueEmpty -> {
                    HomeEmptyState(
                        onCreateListClick = onCreateListClick,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    )
                }
                else -> {
                    HomeListsOverview(
                        lists = uiState.filteredLists,
                        totalCount = uiState.totalActiveLists,
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = onSearchQueryChange,
                        onClearSearch = onClearSearch,
                        onListClick = onListClick,
                        onEditListClick = onEditListClick,
                        onArchiveListClick = onArchiveListClick,
                        onDeleteListClick = onDeleteListClick,
                        onDuplicateListClick = onDuplicateListClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Empty state displayed when no shopping lists exist yet.
 */
@Composable
fun HomeEmptyState(
    onCreateListClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Decorative Circular Icon Container
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(108.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No shopping lists yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create your first grocery or market list to easily plan items and stay within budget.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreateListClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Create Shopping List",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy & Offline Reassurance Pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(
                text = "100% Offline • Private & Fast",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

/**
 * Overview showing search box, active lists header, and shopping list cards.
 */
@Composable
fun HomeListsOverview(
    lists: List<ShoppingListDetail>,
    totalCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onListClick: (String) -> Unit,
    onEditListClick: (String) -> Unit = {},
    onArchiveListClick: (ShoppingListDetail) -> Unit = {},
    onDeleteListClick: (ShoppingListDetail) -> Unit = {},
    onDuplicateListClick: (ShoppingListDetail) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header_greeting") {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "My Shopping Lists",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Track items and stay within your budget effortlessly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "search_box") {
            ShopZenSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onClearQuery = onClearSearch,
                placeholder = "Search lists..."
            )
        }

        item(key = "section_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Active Lists",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = lists.size.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        if (lists.isEmpty() && searchQuery.isNotBlank()) {
            item(key = "empty_search") {
                SearchNoResultsView(
                    query = searchQuery,
                    onResetFilters = onClearSearch,
                    message = "We couldn't find any shopping lists matching “$searchQuery”. Check spelling or create a new list."
                )
            }
        } else {
            items(
                items = lists,
                key = { it.list.id }
            ) { listDetail ->
                ShoppingListCard(
                    listDetail = listDetail,
                    onClick = { onListClick(listDetail.list.id) },
                    onEditClick = { onEditListClick(listDetail.list.id) },
                    onArchiveClick = { onArchiveListClick(listDetail) },
                    onDeleteClick = { onDeleteListClick(listDetail) },
                    onDuplicateClick = { onDuplicateListClick(listDetail) }
                )
            }
        }
    }
}

/**
 * Card component displaying shopping list details, progress bar, and budget pills.
 */
@Composable
fun ShoppingListCard(
    listDetail: ShoppingListDetail,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onArchiveClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onDuplicateClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val list: ShoppingList = listDetail.list
    val totalItems = listDetail.totalItemsCount
    val purchasedItems = listDetail.purchasedItemsCount
    val progress = if (totalItems > 0) purchasedItems.toFloat() / totalItems.toFloat() else 0f
    val percentage = (progress * 100).toInt()

    val formattedDate = formatTimestamp(list.updatedAt)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: List Name, Updated Time, Active badge, and 3-dots Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = list.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Updated $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Box {
                        var menuExpanded by remember { mutableStateOf(false) }

                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options for ${list.name}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit list") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEditClick()
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
                                    menuExpanded = false
                                    onArchiveClick()
                                }
                            )
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
                                    menuExpanded = false
                                    onDuplicateClick()
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
                                    menuExpanded = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }

            // Progress Bar & Counts
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (totalItems > 0) "$purchasedItems of $totalItems items purchased" else "No items added yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            }

            // Financial Breakdown Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Budget Pill
                FinancialPill(
                    label = "Budget",
                    value = CurrencyFormatter.format(list.budgetMinor, list.currencyCode, "—"),
                    modifier = Modifier.weight(1f)
                )

                // Est. Total Pill
                FinancialPill(
                    label = "Est. Total",
                    value = CurrencyFormatter.format(listDetail.estimatedTotalMinor, list.currencyCode),
                    modifier = Modifier.weight(1f)
                )

                // Actual Spent Pill
                val isOverBudget = list.budgetMinor != null && listDetail.actualSpentMinor > list.budgetMinor
                FinancialPill(
                    label = "Spent",
                    value = CurrencyFormatter.format(listDetail.actualSpentMinor, list.currencyCode),
                    isHighlighted = isOverBudget,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FinancialPill(
    label: String,
    value: String,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isHighlighted) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isHighlighted) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isHighlighted) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> DateFormat.getDateInstance(DateFormat.SHORT).format(Date(timestamp))
    }
}
