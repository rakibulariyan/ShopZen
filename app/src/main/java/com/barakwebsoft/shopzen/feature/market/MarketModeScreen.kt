package com.barakwebsoft.shopzen.feature.market

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barakwebsoft.shopzen.core.di.AppViewModelProvider
import com.barakwebsoft.shopzen.core.util.CurrencyFormatter
import com.barakwebsoft.shopzen.feature.categories.CategoryVisuals
import com.barakwebsoft.shopzen.feature.categories.components.CategoryChip
import com.barakwebsoft.shopzen.feature.market.components.MarketFinancialHud
import com.barakwebsoft.shopzen.feature.market.components.MarketShoppingItemRow

/**
 * Dedicated in-store Market Mode screen.
 * Optimized for one-handed aisle navigation with 52dp targets, tactile haptics,
 * sticky live spend ticker, keep-screen-on, and guaranteed zero advertisements.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketModeScreen(
    listId: String,
    onNavigateBack: () -> Unit,
    onEditItemPrice: (String) -> Unit = {},
    viewModel: MarketModeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(listId) {
        viewModel.loadList(listId)
    }

    // Keep screen awake while shopping if enabled
    DisposableEffect(uiState.isKeepScreenOn) {
        val activity = context.findActivity()
        if (uiState.isKeepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Observe purchase toggle events for immediate Snackbar with Undo
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MarketModeEvent.ItemPurchaseToggled -> {
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

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            onNavigateBack()
        }
    }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Market Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        Text(
                            text = "${list?.name ?: "List"} • Offline Mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit Market Mode"
                        )
                    }
                },
                actions = {
                    if (list != null) {
                        // Progress Chip
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = "${uiState.purchasedItemsCount}/${uiState.totalItemsCount} (${uiState.progressPercent}%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Haptic feedback toggle
                        IconButton(onClick = { viewModel.toggleHaptic() }) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = if (uiState.isHapticEnabled) "Disable haptics" else "Enable haptics",
                                tint = if (uiState.isHapticEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }
                            )
                        }

                        // Keep screen awake toggle
                        IconButton(onClick = { viewModel.toggleKeepScreenOn() }) {
                            Icon(
                                imageVector = if (uiState.isKeepScreenOn) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (uiState.isKeepScreenOn) "Screen stays on" else "Normal screen timeout",
                                tint = if (uiState.isKeepScreenOn) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (listDetail != null && uiState.totalItemsCount > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Fast Undo Button
                        OutlinedButton(
                            onClick = { viewModel.undoLastToggle() },
                            enabled = uiState.lastPurchaseToggleAction != null,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Undo",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Primary Finish Button
                        Button(
                            onClick = { viewModel.finishShopping() },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCartCheckout,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val currency = list?.currencyCode ?: "USD"
                            val spentFormatted = CurrencyFormatter.format(listDetail.actualSpentMinor, currency)
                            Text(
                                text = "Finish ($spentFormatted)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                val currencyCode = listDetail.list.currencyCode

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Sticky Financial HUD Card
                    item(key = "market_hud") {
                        MarketFinancialHud(
                            listDetail = listDetail,
                            unpricedPurchasedCount = uiState.unpricedPurchasedItemsCount,
                            onUnpricedClick = {
                                val firstUnpriced = listDetail.items.find {
                                    it.item.isPurchased && it.item.actualUnitPriceMinor == null
                                }
                                if (firstUnpriced != null) {
                                    onEditItemPrice(firstUnpriced.item.id)
                                }
                            }
                        )
                    }

                    // 2. Category / Aisle Filters Row
                    if (uiState.availableCategories.size > 1) {
                        item(key = "aisle_filters") {
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
                                    noneLabel = "All Aisles"
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

                    // All Items Purchased Celebration Banner
                    if (uiState.totalItemsCount > 0 && uiState.pendingItemsCount == 0) {
                        item(key = "all_purchased_banner") {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "All items purchased!",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "Ready to finish and review shopping list.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Shopping Items Grouped by Category / Aisle
                    uiState.groupedByCategory.forEach { (category, itemsInCategory) ->
                        val pendingCount = itemsInCategory.count { !it.item.isPurchased }
                        item(key = "aisle_header_${category?.id ?: "uncategorized"}") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = CategoryVisuals.getCategoryIcon(category?.iconName),
                                        contentDescription = null,
                                        tint = CategoryVisuals.getCategoryColor(category?.colorToken),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = category?.name ?: "Uncategorized",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (pendingCount > 0) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    }
                                ) {
                                    Text(
                                        text = if (pendingCount > 0) "$pendingCount left" else "Complete",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (pendingCount > 0) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        items(
                            items = itemsInCategory,
                            key = { it.item.id }
                        ) { itemDetail ->
                            MarketShoppingItemRow(
                                itemDetail = itemDetail,
                                currencyCode = currencyCode,
                                isHapticEnabled = uiState.isHapticEnabled,
                                onTogglePurchased = { current ->
                                    viewModel.toggleItemPurchased(itemDetail.item.id, current)
                                },
                                onPriceClick = {
                                    onEditItemPrice(itemDetail.item.id)
                                }
                            )
                        }
                    }

                    // Spacer at bottom
                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}

/**
 * Traverses context wrappers to find the host [Activity].
 */
private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

