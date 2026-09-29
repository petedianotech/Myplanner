package com.myplanner.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.theme.Spacing

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.screenHorizontal)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                PageHeader(title = "Privacy", subtitle = "What this app stores", modifier = Modifier.weight(1f))
            }
            PrivacyBlock("Stored on this device", "Tasks, reminders, notes, ideas and voice recordings stay in MyPlanner’s local database and private app storage. Nothing is uploaded automatically.")
            PrivacyBlock("Notifications", "Reminder alerts are created on the device with Android’s notification system. The app does not send notifications through a server.")
            PrivacyBlock("Microphone", "The microphone is used only when you record a voice note. Audio is saved locally. Speech is not transcribed or sent anywhere.")
            PrivacyBlock("Backups you export", "A backup is a ZIP file you choose to save. Anyone who obtains that file can read the included plans and listen to included recordings. Protect exported files as you would any personal document.")
            PrivacyBlock("Permissions", "Notifications and microphone access are requested only for those features. Document pickers are used for backup and restore, so the app does not need broad storage access.")
            PrivacyBlock("What is not collected", "MyPlanner does not include accounts, analytics, advertising, crash-reporting services, or internet backup. There is no cloud copy unless you create and store one yourself.")
            PrivacyBlock("Limits", "Local storage is private to the app on a typical device setup, but it is not an absolute security guarantee. Another person with the unlocked device, a backup file, or device-level access can reach the same data.")
            Spacer(Modifier.height(Spacing.xxxl))
        }
    }
}

@Composable
private fun PrivacyBlock(title: String, body: String) {
    Spacer(Modifier.height(Spacing.md))
    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    Spacer(Modifier.height(Spacing.xs))
    Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
