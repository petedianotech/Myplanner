package com.myplanner.app.ui.habit

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.data.local.AppDatabase
import com.myplanner.app.data.local.HabitEntity
import com.myplanner.app.ui.components.PeteAmbientBackground
import com.myplanner.app.ui.components.PeteGlassCard
import com.myplanner.app.ui.theme.PeteColors
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitsViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getInstance(app).habitDao()
    val habits = dao.observeActive().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(title: String, emoji: String = "✅") {
        val t = title.trim()
        if (t.isEmpty()) return
        viewModelScope.launch {
            dao.insert(HabitEntity(title = t, emoji = emoji.ifBlank { "✅" }))
        }
    }

    fun toggleToday(habit: HabitEntity) {
        viewModelScope.launch {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val already = habit.lastCompletedDay == today
            val updated = if (already) {
                habit.copy(
                    lastCompletedDay = null,
                    streak = (habit.streak - 1).coerceAtLeast(0),
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
            } else {
                val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                val nextStreak = if (habit.lastCompletedDay == yesterday) habit.streak + 1 else 1
                habit.copy(
                    lastCompletedDay = today,
                    streak = nextStreak,
                    bestStreak = maxOf(habit.bestStreak, nextStreak),
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
            }
            dao.update(updated)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { dao.deleteById(id) }
    }
}

@Composable
fun HabitsScreen(
    onBack: () -> Unit,
    viewModel: HabitsViewModel = viewModel()
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf("") }
    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    Box(Modifier.fillMaxSize()) {
        PeteAmbientBackground()
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = PeteColors.OnDark)
                }
                Text(
                    "Habits",
                    color = PeteColors.OnDark,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                "Tap a habit to mark it done today. Streaks update automatically.",
                color = PeteColors.OnDarkMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            PeteGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = { Text("New habit…", color = PeteColors.OnDarkMuted) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PeteColors.OnDark,
                            unfocusedTextColor = PeteColors.OnDark,
                            focusedBorderColor = PeteColors.Cyan,
                            unfocusedBorderColor = PeteColors.OnDarkMuted.copy(alpha = 0.3f),
                            cursorColor = PeteColors.Cyan
                        )
                    )
                    IconButton(
                        onClick = {
                            viewModel.add(draft)
                            draft = ""
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PeteColors.Cyan.copy(alpha = 0.25f))
                    ) {
                        Icon(Icons.Outlined.Add, "Add", tint = PeteColors.Cyan)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (habits.isEmpty()) {
                PeteGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No habits yet. Add one above — e.g. Stretch, Read 10 pages, Drink water.",
                        color = PeteColors.OnDarkSoft,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        val doneToday = habit.lastCompletedDay == today
                        PeteGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleToday(habit) }
                            ) {
                                Text(habit.emoji, fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        habit.title,
                                        color = PeteColors.OnDark,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "Streak ${habit.streak} · Best ${habit.bestStreak}",
                                        color = PeteColors.OnDarkMuted,
                                        fontSize = 12.sp
                                    )
                                }
                                Icon(
                                    Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = if (doneToday) Color(0xFF34D399) else PeteColors.OnDarkMuted.copy(alpha = 0.35f),
                                    modifier = Modifier.size(28.dp)
                                )
                                IconButton(onClick = { viewModel.delete(habit.id) }) {
                                    Icon(Icons.Outlined.Delete, "Delete", tint = PeteColors.OnDarkMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
