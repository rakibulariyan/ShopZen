package com.barakwebsoft.shopzen.feature.market.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.barakwebsoft.shopzen.core.util.CurrencyFormatter
import com.barakwebsoft.shopzen.core.util.MoneyCalculator
import com.barakwebsoft.shopzen.domain.model.ShoppingItemDetail

/**
 * Fast-access modal bottom sheet for entering actual prices in Market Mode.
 * Supports quick-apply estimated prices, exact integer minor unit calculations,
 * live line total preview, and auto-marking item as purchased.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActualPriceEntryBottomSheet(
    itemDetail: ShoppingItemDetail,
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirmPrice: (actualUnitPriceMinor: Long?, markPurchased: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val item = itemDetail.item
    val currencyInfo = CurrencyFormatter.getCurrencyInfo(currencyCode)
    val isZeroDecimal = currencyInfo.fractionDigits == 0

    // Initialize with existing actual price, or fallback to estimated price for rapid one-tap confirmation
    val initialText = when {
        item.actualUnitPriceMinor != null -> CurrencyFormatter.toMajorUnitsString(item.actualUnitPriceMinor, currencyCode)
        item.estimatedUnitPriceMinor != null -> CurrencyFormatter.toMajorUnitsString(item.estimatedUnitPriceMinor, currencyCode)
        else -> ""
    }

    var priceText by remember { mutableStateOf(initialText) }
    var markPurchased by remember { mutableStateOf(!item.isPurchased) }

    val parsedMinor: Long? = CurrencyFormatter.parseToMinor(priceText, currencyCode)
    val lineTotalMinor: Long? = parsedMinor?.let {
        MoneyCalculator.calculateLineTotal(it, item.quantity)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enter Final Price",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val unitStr = if (item.unit.isNotBlank()) " ${item.unit}" else ""
                    Text(
                        text = "${item.name} (${item.quantity}$unitStr)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close price entry"
                    )
                }
            }

            // Numeric Price Input Field with Currency Prefix
            OutlinedTextField(
                value = priceText,
                onValueChange = { input ->
                    // Filter input to valid price characters (digits, single decimal point or comma)
                    val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
                    val decimalCount = filtered.count { it == '.' || it == ',' }
                    if (isZeroDecimal) {
                        if (filtered.all { it.isDigit() }) priceText = filtered
                    } else if (decimalCount <= 1) {
                        priceText = filtered
                    }
                },
                leadingIcon = {
                    Text(
                        text = currencyInfo.symbol,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                placeholder = {
                    Text(
                        text = if (isZeroDecimal) "0" else "0.00",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isZeroDecimal) KeyboardType.Number else KeyboardType.Decimal
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Quick Preset / Helper Action Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick chip: Use Estimated Price
                if (item.estimatedUnitPriceMinor != null) {
                    val estFormatted = CurrencyFormatter.format(item.estimatedUnitPriceMinor, currencyCode)
                    FilterChip(
                        selected = false,
                        onClick = {
                            priceText = CurrencyFormatter.toMajorUnitsString(item.estimatedUnitPriceMinor, currencyCode)
                        },
                        label = {
                            Text(
                                text = "Use Est. $estFormatted",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    )
                }

                // Quick chip: Clear Price (mark as Unpriced)
                FilterChip(
                    selected = false,
                    onClick = { priceText = "" },
                    label = {
                        Text(
                            text = "Clear (Unpriced)",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }

            // Live Line Total Preview Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Line Total (${item.quantity} ${item.unit})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (lineTotalMinor != null) {
                                CurrencyFormatter.format(lineTotalMinor, currencyCode)
                            } else {
                                "Unpriced in cart"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (lineTotalMinor != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    if (item.estimatedUnitPriceMinor != null && parsedMinor != null) {
                        val diff = parsedMinor - item.estimatedUnitPriceMinor
                        val isCheaper = diff < 0
                        val isMore = diff > 0
                        if (isCheaper || isMore) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCheaper) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = if (isCheaper) {
                                        "${CurrencyFormatter.format(kotlin.math.abs(diff), currencyCode)} lower"
                                    } else {
                                        "${CurrencyFormatter.format(diff, currencyCode)} higher"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCheaper) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Option: Mark as Purchased
            if (!item.isPurchased) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Checkbox(
                        checked = markPurchased,
                        onCheckedChange = { markPurchased = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mark item as purchased in cart",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Primary Confirm Action Button
            Button(
                onClick = {
                    onConfirmPrice(parsedMinor, markPurchased || item.isPurchased)
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val buttonLabel = when {
                    parsedMinor != null -> "Confirm ${CurrencyFormatter.format(parsedMinor, currencyCode)} & Update HUD"
                    markPurchased || item.isPurchased -> "Save as Unpriced & In Cart"
                    else -> "Save Price"
                }
                Text(
                    text = buttonLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

