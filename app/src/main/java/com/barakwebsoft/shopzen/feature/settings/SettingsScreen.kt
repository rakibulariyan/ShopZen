package com.barakwebsoft.shopzen.feature.settings

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barakwebsoft.shopzen.core.billing.PurchaseStatus
import com.barakwebsoft.shopzen.core.di.AppViewModelProvider
import com.barakwebsoft.shopzen.feature.lists.components.CurrencySelectionDialog
import com.barakwebsoft.shopzen.feature.settings.components.ThemeSelectionDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Regional & Currency
                Text(
                    text = "Currency & Regional",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                SettingClickableCard(
                    icon = Icons.Outlined.Payments,
                    title = "Default Currency",
                    subtitle = "${uiState.currencyInfo.code} (${uiState.currencyInfo.symbol}) • ${uiState.currencyInfo.name}",
                    caption = "Used as the initial currency for newly created shopping lists",
                    onClick = { viewModel.setCurrencyPickerVisible(true) }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Section 2: Preferences
                Text(
                    text = "General Preferences",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                SettingClickableCard(
                    icon = Icons.Outlined.ColorLens,
                    title = "Appearance",
                    subtitle = when (uiState.themePreference.name) {
                        "LIGHT" -> "Light theme"
                        "DARK" -> "Dark theme"
                        else -> "System default"
                    },
                    caption = "Choose your preferred color theme",
                    onClick = { viewModel.setThemeDialogVisible(true) }
                )

                SettingToggleCard(
                    icon = Icons.Outlined.TouchApp,
                    title = "Tactile Haptics",
                    subtitle = "Vibrate on purchasing items and adjusting quantities",
                    isChecked = uiState.hapticFeedbackEnabled,
                    onCheckedChange = { viewModel.setHapticFeedbackEnabled(it) }
                )

                SettingToggleCard(
                    icon = Icons.Outlined.Visibility,
                    title = "Show Completed Items",
                    subtitle = "Display purchased items in lists and Market Mode",
                    isChecked = uiState.showCompletedItems,
                    onCheckedChange = { viewModel.setShowCompletedItems(it) }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Section 3: Premium & Monetization
                Text(
                    text = "ShopZen Premium",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                PremiumSubscriptionCard(
                    isAdsRemoved = uiState.isAdsRemoved,
                    purchaseStatus = uiState.purchaseStatus,
                    priceFormatted = uiState.subscriptionPriceFormatted,
                    onSubscribeClick = {
                        activity?.let { viewModel.launchSubscription(it) }
                    },
                    onRestoreClick = {
                        viewModel.restorePurchases()
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Section 4: About
                Text(
                    text = "About",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                SettingClickableCard(
                    icon = Icons.Outlined.Info,
                    title = "ShopZen",
                    subtitle = "Version 1.0 (Build 1)",
                    caption = "Offline-first grocery budget planner • Botanical Design System",
                    onClick = {}
                )
            }
        }
    }

    // Currency Selection Dialog
    if (uiState.isCurrencyPickerVisible) {
        CurrencySelectionDialog(
            selectedCurrencyCode = uiState.defaultCurrencyCode,
            onCurrencySelected = { viewModel.setDefaultCurrency(it) },
            onDismiss = { viewModel.setCurrencyPickerVisible(false) }
        )
    }

    // Theme Selection Dialog
    if (uiState.isThemeDialogVisible) {
        ThemeSelectionDialog(
            selectedTheme = uiState.themePreference,
            onThemeSelected = { viewModel.setThemePreference(it) },
            onDismiss = { viewModel.setThemeDialogVisible(false) }
        )
    }
}

@Composable
private fun SettingClickableCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    caption: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "$title, currently set to $subtitle"
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (caption != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingToggleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.semantics {
                    contentDescription = "$title toggle"
                }
            )
        }
    }
}

@Composable
private fun PremiumSubscriptionCard(
    isAdsRemoved: Boolean,
    purchaseStatus: PurchaseStatus,
    priceFormatted: String?,
    onSubscribeClick: () -> Unit,
    onRestoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAdsRemoved) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isAdsRemoved) {
                        MaterialTheme.colorScheme.primary
                    } else if (purchaseStatus == PurchaseStatus.PENDING) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when {
                                isAdsRemoved -> Icons.Outlined.CheckCircle
                                purchaseStatus == PurchaseStatus.PENDING -> Icons.Outlined.HourglassTop
                                else -> Icons.Outlined.WorkspacePremium
                            },
                            contentDescription = null,
                            tint = if (isAdsRemoved) {
                                MaterialTheme.colorScheme.onPrimary
                            } else if (purchaseStatus == PurchaseStatus.PENDING) {
                                MaterialTheme.colorScheme.onTertiary
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            isAdsRemoved -> "ShopZen Premium • Active"
                            purchaseStatus == PurchaseStatus.PENDING -> "Subscription Pending"
                            else -> "Remove Advertisements"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            isAdsRemoved -> "Yearly ad-free entitlement active"
                            purchaseStatus == PurchaseStatus.PENDING -> "Payment is being processed by Google Play"
                            priceFormatted != null -> "$priceFormatted / year"
                            else -> "Fetching localized price..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = when {
                    isAdsRemoved -> "All advertisements are disabled across ShopZen. Thank you for supporting independent, offline-first development!"
                    purchaseStatus == PurchaseStatus.PENDING -> "Google Play is confirming your transaction. Your ad-free status will unlock automatically once payment is finalized."
                    purchaseStatus == PurchaseStatus.UNAVAILABLE -> "Google Play Billing is currently unavailable on this device. Please check your network or Play Store app."
                    else -> "Enjoy an uninterrupted shopping and budgeting experience. Remove all banner ads across the app with an auto-renewing yearly subscription."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isAdsRemoved && purchaseStatus != PurchaseStatus.PENDING) {
                    Button(
                        onClick = onSubscribeClick,
                        enabled = priceFormatted != null && purchaseStatus != PurchaseStatus.UNAVAILABLE,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (priceFormatted != null) "Subscribe ($priceFormatted)" else "Subscribe",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                TextButton(
                    onClick = onRestoreClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (purchaseStatus == PurchaseStatus.PENDING) "Check Status" else "Restore Purchases",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
