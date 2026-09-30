package com.myplanner.app.ui.idea

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.data.local.IdeaEntity
import com.myplanner.app.ui.components.AppDivider
import com.myplanner.app.ui.components.AppFab
import com.myplanner.app.ui.components.AppFilterChip
import com.myplanner.app.ui.components.AppListItem
import com.myplanner.app.ui.components.EmptyState
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.home.HomeViewModel
import com.myplanner.app.ui.theme.Spacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class IdeasFilter { ACTIVE, ARCHIVED }

@Composable
fun IdeasScreen(
    onOpenIdea: (Long) -> Unit,
    onCreateIdea: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val ideas by viewModel.ideaRepository.observeIdeas().collectAsStateWithLifecycle(emptyList())
    var filter by remember { mutableStateOf(IdeasFilter.ACTIVE) }
    val filtered = remember(ideas, filter) {
        when (filter) {
            IdeasFilter.ACTIVE -> ideas.filter { !it.archived }
            IdeasFilter.ARCHIVED -> ideas.filter { it.archived }
        }.sortedWith(compareByDescending<IdeaEntity> { it.pinned }.thenByDescending { it.updatedAtEpochMillis })
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            AppFab(icon = Icons.Outlined.Add, contentDescription = "New idea", onClick = onCreateIdea)
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).padding(horizontal = Spacing.screenHorizontal),
            contentPadding = PaddingValues(bottom = Spacing.huge)
        ) {
            item {
                PageHeader(title = "Ideas", subtitle = "Capture and develop thoughts")
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    AppFilterChip(label = "Active", selected = filter == IdeasFilter.ACTIVE, onClick = { filter = IdeasFilter.ACTIVE })
                    AppFilterChip(label = "Archived", selected = filter == IdeasFilter.ARCHIVED, onClick = { filter = IdeasFilter.ARCHIVED })
                }
            }
            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        title = if (filter == IdeasFilter.ACTIVE) "No ideas yet" else "No archived ideas",
                        message = "Tap + to capture a thought before it fades.",
                        icon = Icons.Outlined.Lightbulb
                    )
                }
            } else {
                items(filtered, key = { it.id }) { idea ->
                    AppListItem(
                        title = idea.title,
                        supportingText = buildString {
                            append(idea.status.replaceFirstChar { it.uppercase() })
                            if (idea.category != IdeaEntity.CATEGORY_OTHER) append(" · ${idea.category.replaceFirstChar { it.uppercase() }}")
                            formatIdeaDate(idea.updatedAtEpochMillis)?.let { append(" · $it") }
                        },
                        leadingIcon = if (idea.pinned) Icons.Outlined.PushPin else Icons.Outlined.Lightbulb,
                        onClick = { onOpenIdea(idea.id) }
                    )
                    AppDivider()
                }
            }
        }
    }
}

private fun formatIdeaDate(millis: Long): String? {
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
    }
}
