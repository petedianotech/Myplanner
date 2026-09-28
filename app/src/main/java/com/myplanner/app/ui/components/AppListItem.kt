package com.myplanner.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.myplanner.app.ui.theme.AppDimens
import com.myplanner.app.ui.theme.Spacing

@Composable
fun AppListItem(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingText: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val rowModifier = modifier
        .fillMaxWidth()
        .heightIn(min = AppDimens.listItemMinHeight)
        .then(
            if (onClick != null && enabled) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        )
        .padding(
            horizontal = Spacing.listItemHorizontal,
            vertical = Spacing.listItemVertical
        )

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(AppDimens.iconMd),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(Spacing.md))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (trailingText != null) {
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(Spacing.sm))
            trailingContent()
        }
    }
}
