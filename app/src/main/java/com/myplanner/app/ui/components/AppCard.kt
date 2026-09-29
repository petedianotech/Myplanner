package com.myplanner.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.myplanner.app.ui.theme.AppDimens
import com.myplanner.app.ui.theme.AppGradients
import com.myplanner.app.ui.theme.AppShapes
import com.myplanner.app.ui.theme.Spacing

/** Neutral information card — default for content blocks. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
    val elevation = CardDefaults.cardElevation(
        defaultElevation = if (elevated) AppDimens.cardElevationRaised else AppDimens.cardElevation
    )
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = AppShapes.card,
            colors = colors,
            elevation = elevation
        ) {
            Column(
                modifier = Modifier.padding(Spacing.cardPadding),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = AppShapes.card,
            colors = colors,
            elevation = elevation
        ) {
            Column(
                modifier = Modifier.padding(Spacing.cardPadding),
                content = content
            )
        }
    }
}

/** Subtle tinted surface for grouped secondary content. */
@Composable
fun AppSurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.cardPadding),
            content = content
        )
    }
}

/**
 * Hero / summary card with brand gradient.
 * Reserve for important summary moments only — not default card styling.
 */
@Composable
fun AppHeroCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.cardLarge)
            .background(AppGradients.brandHorizontal())
            .padding(Spacing.xl),
        content = content
    )
}

/** Soft primary-tinted card for emphasis without full gradient. */
@Composable
fun AppTintedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.cardPadding),
            content = content
        )
    }
}
