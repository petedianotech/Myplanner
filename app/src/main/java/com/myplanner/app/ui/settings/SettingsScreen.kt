package com.myplanner.app.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myplanner.app.data.local.PreferencesRepository
import com.myplanner.app.ui.components.AppCard
import com.myplanner.app.ui.components.AppDivider
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.components.SectionHeader
import com.myplanner.app.ui.theme.AppPalette
import com.myplanner.app.ui.theme.Spacing
import com.myplanner.app.ui.theme.colors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenPrivacy: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val themeMode by prefs.themeMode.collectAsStateWithLifecycle("system")
    val themePalette by prefs.themePalette.collectAsStateWithLifecycle("indigo")
    val reminderNotifs by prefs.reminderNotificationsEnabled.collectAsStateWithLifecycle(true)
    val vibrate by prefs.vibrateOnReminder.collectAsStateWithLifecycle(true)
    val notifyMissed by prefs.notifyMissedReminders.collectAsStateWithLifecycle(true)
    val systemNotificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                PageHeader(
                    title = "Settings",
                    subtitle = "Appearance, notifications and privacy",
                    modifier = Modifier.weight(1f)
                )
            }

            SectionHeader(title = "Color theme")
            AppCard {
                AppPalette.entries.forEachIndexed { index, palette ->
                    if (index > 0) AppDivider()
                    PaletteRow(
                        palette = palette,
                        selected = themePalette == palette.id,
                        onSelect = { scope.launch { prefs.setThemePalette(palette.id) } }
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            SectionHeader(title = "Brightness")
            AppCard {
                ThemeRow("System default", "Follow the device setting", themeMode == "system") {
                    scope.launch { prefs.setThemeMode("system") }
                }
                AppDivider()
                ThemeRow("Light", "Always use the light theme", themeMode == "light") {
                    scope.launch { prefs.setThemeMode("light") }
                }
                AppDivider()
                ThemeRow("Dark", "Always use the dark theme", themeMode == "dark") {
                    scope.launch { prefs.setThemeMode("dark") }
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            SectionHeader(title = "Notifications")
            AppCard {
                if (!systemNotificationsOn) {
                    SettingsRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications are off in Android",
                        subtitle = "Enable them in system settings so reminders can alert you.",
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    )
                    AppDivider()
                }
                SwitchRow(
                    title = "Reminder alerts",
                    subtitle = "Show a notification when a reminder is due.",
                    checked = reminderNotifs,
                    onChecked = { scope.launch { prefs.setReminderNotificationsEnabled(it) } }
                )
                AppDivider()
                SwitchRow(
                    title = "Vibration",
                    subtitle = "Vibrate with reminder notifications.",
                    checked = vibrate,
                    onChecked = { scope.launch { prefs.setVibrateOnReminder(it) } }
                )
                AppDivider()
                SwitchRow(
                    title = "Missed reminders",
                    subtitle = "Still notify if a reminder was due while the app was closed.",
                    checked = notifyMissed,
                    onChecked = { scope.launch { prefs.setNotifyMissedReminders(it) } }
                )
            }

            Spacer(Modifier.height(Spacing.lg))
            SectionHeader(title = "Privacy")
            AppCard {
                SettingsRow(
                    icon = Icons.Outlined.Shield,
                    title = "How MyPlanner stores data",
                    subtitle = "Offline on this device unless you export a backup.",
                    onClick = onOpenPrivacy
                )
            }

            Spacer(Modifier.height(Spacing.lg))
            SectionHeader(title = "About")
            AppCard {
                SettingsRow(
                    icon = Icons.Outlined.Info,
                    title = "MyPlanner",
                    subtitle = "Version 1.2.0 · Offline personal planner"
                )
            }
            Spacer(Modifier.height(Spacing.xxxl))
        }
    }
}

@Composable
private fun PaletteRow(palette: AppPalette, selected: Boolean, onSelect: () -> Unit) {
    val colors = palette.colors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(colors.primary, colors.secondary))),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.padding(start = Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(palette.label, style = MaterialTheme.typography.bodyLarge)
            Text(palette.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun ThemeRow(title: String, subtitle: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect).padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RadioButton(selected = selected, onClick = onSelect)
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.padding(start = Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
