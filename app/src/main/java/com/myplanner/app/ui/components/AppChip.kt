package com.myplanner.app.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.myplanner.app.ui.theme.AppDimens
import com.myplanner.app.ui.theme.AppShapes

@Composable
fun AppFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
        },
        modifier = modifier.heightIn(min = AppDimens.chipHeight),
        enabled = enabled,
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(AppDimens.iconSm)
                )
            }
        },
        shape = AppShapes.chip,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun AppInputChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null
) {
    InputChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
        },
        modifier = modifier.heightIn(min = AppDimens.chipHeight),
        enabled = enabled,
        trailingIcon = trailingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = if (onTrailingIconClick != null) "Remove" else null,
                    modifier = Modifier.size(AppDimens.iconSm)
                )
            }
        },
        shape = AppShapes.chip,
        colors = InputChipDefaults.inputChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}
