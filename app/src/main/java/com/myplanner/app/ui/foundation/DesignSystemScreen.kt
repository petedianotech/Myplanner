package com.myplanner.app.ui.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.myplanner.app.ui.components.AppDivider
import com.myplanner.app.ui.components.AppFab
import com.myplanner.app.ui.components.AppFilterChip
import com.myplanner.app.ui.components.AppHeroCard
import com.myplanner.app.ui.components.AppIconButton
import com.myplanner.app.ui.components.AppListItem
import com.myplanner.app.ui.components.AppPrimaryButton
import com.myplanner.app.ui.components.AppSecondaryButton
import com.myplanner.app.ui.components.AppSurfaceCard
import com.myplanner.app.ui.components.AppTextButton
import com.myplanner.app.ui.components.AppTextField
import com.myplanner.app.ui.components.AppTintedCard
import com.myplanner.app.ui.components.AppTonalButton
import com.myplanner.app.ui.components.EmptyState
import com.myplanner.app.ui.components.ErrorState
import com.myplanner.app.ui.components.LoadingState
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.components.SectionHeader
import com.myplanner.app.ui.theme.GradientEnd
import com.myplanner.app.ui.theme.GradientStart
import com.myplanner.app.ui.theme.MyPlannerTheme
import com.myplanner.app.ui.theme.Spacing
import com.myplanner.app.ui.theme.Success
import com.myplanner.app.ui.theme.Warning

/**
 * Development-only design system gallery.
 * Not part of final product navigation — validates the visual language.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesignSystemScreen(
    modifier: Modifier = Modifier
) {
    var taskTitle by remember { mutableStateOf("") }
    var chipToday by remember { mutableStateOf(true) }
    var chipUpcoming by remember { mutableStateOf(false) }
    var chipNotes by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            AppFab(
                icon = Icons.Outlined.Add,
                contentDescription = "Add",
                onClick = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(bottom = Spacing.huge)
        ) {
            PageHeader(
                title = stringResource(R.string.design_system_title),
                subtitle = stringResource(R.string.design_system_subtitle)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(GradientStart, GradientEnd))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "Indigo · Teal · Calm productivity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Spacing.section))

            SectionHeader(title = stringResource(R.string.section_typography))
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text("Headline — Plan your day", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text("Title — Today's priorities", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("Body — Capture tasks, notes, and ideas in one calm place.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text("Supporting — Due at 9:30 AM · Work", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Label — Save plan", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.section))

            SectionHeader(title = stringResource(R.string.section_colors))
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    ColorRow("Primary", MaterialTheme.colorScheme.primary)
                    ColorRow("Secondary", MaterialTheme.colorScheme.secondary)
                    ColorRow("Surface", MaterialTheme.colorScheme.surface)
                    ColorRow("Background", MaterialTheme.colorScheme.background)
                    ColorRow("Success", Success)
                    ColorRow("Warning", Warning)
                    ColorRow("Error", MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.section))

            SectionHeader(title = stringResource(R.string.section_buttons))
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        AppPrimaryButton(text = stringResource(R.string.primary_button), onClick = {})
                        AppSecondaryButton(text = stringResource(R.string.secondary_button), onClick = {})
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        AppTonalButton(text = stringResource(R.string.tonal_button), onClick = {})
                        AppTextButton(text = stringResource(R.string.text_button), onClick = {})
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        AppIconButton(icon = Icons.Outlined.Edit, contentDescription = "Edit", onClick = {})
                        AppIconButton(icon = Icons.Outlined.Delete, contentDescription = "Delete", onClick = {})
                        AppIconButton(icon = Icons.Outlined.MoreVert, contentDescription = "More", onClick = {})
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.section))

            SectionHeader(title = stringResource(R.string.section_cards))
            AppHeroCard {
                Text(stringResource(R.string.hero_summary), style = MaterialTheme.typography.titleLarge, color = Color.White)
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(stringResource(R.string.hero_detail), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            AppTintedCard {
                Text("Tinted card — soft emphasis without a full gradient.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            AppSurfaceCard {
                Text("Surface card — grouped secondary content.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            AppCard(elevated = true) {
                Text("Elevated card — use sparingly for emphasis.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(Spacing.section))

            SectionHeader(title = stringResource(R.string.section_fields))
            AppCard {
                AppTextField(value = taskTitle, onValueChange = { taskTitle = it }, label = stringResource(R.string.field_label_task), placeholder = stringResource(R.string.field_placeholder_task), supportingText = stringResource(R.string.field_supporting))
                Spacer(modifier = Modifier.height(Spacing.md))
                AppTextField(value = "", onValueChange = {}, label = "Note", placeholder = "Capture a quick thought…", isError = true, supportingText = "This field is required", singleLine = false)
            }

            Spacer(modifier = Modifier.height(Spacing.section))

            SectionHeader(title = stringResource(R.string.section_chips))
            AppCard {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    AppFilterChip(label = stringResource(R.string.chip_today), selected = chipToday, onClick = { chipToday = true; chipUpcoming = false; chipNotes = false }, leadingIcon = Icons.Outlined.TaskAlt)
                    AppFilterChip(label = stringResource(R.string.chip_upcoming), selected = chipUpcoming, onClick = { chipToday = false; chipUpcoming = true; chipNotes = false }, leadingIcon = Icons.Outlined.Event)
                    AppFilterChip(label = stringResource(R.string.chip_notes), selected = chipNotes, onClick = { chipToday = false; chipUpcoming = false; chipNotes = true }, leadingIcon = Icons.Outlined.Notes)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.section))

            SectionHeader(title = stringResource(R.string.section_list))
            AppCard {
                AppListItem(title = stringResource(R.string.list_sample_title), supportingText = stringResource(R.string.list_sample_support), leadingIcon = Icons.Outlined.TaskAlt, trailingText = stringResource(R.string.list_sample_time), onClick = {})
                AppDivider()
                AppListItem(title = "Morning standup notes", supportingText = "Notes · Updated 2 hours ago", leadingIcon = Icons.Outlined.Notes, onClick = {})
                AppDivider()
                AppListItem(title = "Product launch idea", supportingText = "Ideas · Uncategorized", leadingIcon = Icons.Outlined.Lightbulb, trailingContent = { AppIconButton(icon = Icons.Outlined.MoreVert, contentDescription = "More options", onClick = {}) }, onClick = {})
            }

            Spacer(modifier = Modifier.height(Spacing.section))

            SectionHeader(title = stringResource(R.string.section_icons))
            AppCard {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xl), verticalAlignment = Alignment.CenterVertically) {
                    listOf(Icons.Outlined.TaskAlt, Icons.Outlined.Notes, Icons.Outlined.Lightbulb, Icons.Outlined.Notifications, Icons.Outlined.Event, Icons.Outlined.Search).forEach { icon ->
                        androidx.compose.material3.Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.section))

            SectionHeader(title = stringResource(R.string.section_states))
            AppCard {
                EmptyState(title = stringResource(R.string.empty_title), message = stringResource(R.string.empty_message), action = { AppPrimaryButton(text = "Add task", onClick = {}, leadingIcon = Icons.Outlined.Add) })
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            AppCard { LoadingState(message = stringResource(R.string.loading_message)) }
            Spacer(modifier = Modifier.height(Spacing.md))
            AppCard { ErrorState(title = stringResource(R.string.error_title), message = stringResource(R.string.error_message), onRetry = {}, retryLabel = stringResource(R.string.retry)) }

            Spacer(modifier = Modifier.height(Spacing.xxl))
            AppDivider()
            Spacer(modifier = Modifier.height(Spacing.lg))
            Text(text = "Phase 2 design system · Ready for feature screens", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun ColorRow(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Surface(modifier = Modifier.size(36.dp), shape = MaterialTheme.shapes.small, color = color, tonalElevation = 1.dp) {}
        Column {
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "Semantic role", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DesignSystemScreenPreview() {
    MyPlannerTheme {
        DesignSystemScreen()
    }
}
