package com.myplanner.app.ui.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.myplanner.app.R
import com.myplanner.app.ui.components.AppCard
import com.myplanner.app.ui.components.AppPrimaryButton
import com.myplanner.app.ui.components.AppSecondaryButton
import com.myplanner.app.ui.components.AppSurfaceCard
import com.myplanner.app.ui.components.EmptyState
import com.myplanner.app.ui.components.SectionHeader
import com.myplanner.app.ui.theme.GradientEnd
import com.myplanner.app.ui.theme.GradientStart
import com.myplanner.app.ui.theme.MyPlannerTheme
import com.myplanner.app.ui.theme.Spacing

/**
 * Temporary design-system validation screen for Phase 1.
 * Not the final Hello / onboarding experience.
 */
@Composable
fun FoundationScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(bottom = Spacing.xxxl)
        ) {
            Spacer(modifier = Modifier.height(Spacing.lg))
            HeroHeader()
            Spacer(modifier = Modifier.height(Spacing.section))
            SectionHeader(title = stringResource(R.string.section_typography))
            AppCard { TypographySamples() }
            Spacer(modifier = Modifier.height(Spacing.section))
            SectionHeader(title = stringResource(R.string.section_colors))
            AppCard { ColorRoleSamples() }
            Spacer(modifier = Modifier.height(Spacing.section))
            SectionHeader(title = stringResource(R.string.section_components))
            AppCard { ComponentSamples() }
            Spacer(modifier = Modifier.height(Spacing.section))
            SectionHeader(title = stringResource(R.string.section_surfaces))
            AppSurfaceCard {
                Text(
                    text = "Surface variant card — subtle background for grouped content.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            AppCard(elevated = true) {
                Text(
                    text = "Elevated surface card — reserved for emphasis.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(Spacing.section))
            AppCard {
                EmptyState(
                    title = stringResource(R.string.empty_title),
                    message = stringResource(R.string.empty_message)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.xxl))
            StatusRow()
        }
    }
}

@Composable
private fun HeroHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.foundation_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.foundation_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TypographySamples() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = "Headline Small",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Title Medium — section labels",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Body Large — primary reading text for plans and notes.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Body Medium — secondary description and supporting copy.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Label Large — buttons and chips",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ColorRoleSamples() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        ColorSwatchRow(
            label = "Primary",
            color = MaterialTheme.colorScheme.primary,
            onColor = MaterialTheme.colorScheme.onPrimary
        )
        ColorSwatchRow(
            label = "Secondary",
            color = MaterialTheme.colorScheme.secondary,
            onColor = MaterialTheme.colorScheme.onSecondary
        )
        ColorSwatchRow(
            label = "Surface",
            color = MaterialTheme.colorScheme.surface,
            onColor = MaterialTheme.colorScheme.onSurface,
            border = true
        )
        ColorSwatchRow(
            label = "Background",
            color = MaterialTheme.colorScheme.background,
            onColor = MaterialTheme.colorScheme.onBackground,
            border = true
        )
    }
}

@Composable
private fun ColorSwatchRow(
    label: String,
    color: Color,
    onColor: Color,
    border: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            if (border) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = color,
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = 1.dp,
                    shadowElevation = 0.dp
                ) {}
            }
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Semantic role",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ComponentSamples() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppPrimaryButton(
                text = stringResource(R.string.primary_button),
                onClick = {}
            )
            AppSecondaryButton(
                text = stringResource(R.string.secondary_button),
                onClick = {}
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Icon(
                imageVector = Icons.Outlined.TextFields,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Icon(
                imageVector = Icons.Outlined.Widgets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun StatusRow() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(modifier = Modifier.height(Spacing.lg))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(
            text = "Phase 1 foundation ready",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FoundationScreenPreview() {
    MyPlannerTheme {
        FoundationScreen()
    }
}
