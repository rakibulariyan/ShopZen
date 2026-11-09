package com.barakwebsoft.shopzen.feature.categories.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.barakwebsoft.shopzen.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    onCreateCategory: ((name: String, iconName: String, colorToken: String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    allowNoneOption: Boolean = true,
    noneLabel: String = "None"
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (allowNoneOption) {
            CategoryChip(
                category = null,
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
                noneLabel = noneLabel,
                noneIcon = Icons.Default.Category
            )
        }

        categories.forEach { category ->
            CategoryChip(
                category = category,
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) }
            )
        }

        if (onCreateCategory != null) {
            AssistChip(
                onClick = { showCreateDialog = true },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(20.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add custom category",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                label = {
                    Text(
                        text = "New",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }

    if (showCreateDialog && onCreateCategory != null) {
        CreateCategoryDialog(
            onDismissRequest = { showCreateDialog = false },
            onConfirm = { name, iconName, colorToken ->
                onCreateCategory(name, iconName, colorToken)
                showCreateDialog = false
            }
        )
    }
}

