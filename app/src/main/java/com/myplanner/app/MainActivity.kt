package com.myplanner.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myplanner.app.data.local.PreferencesRepository
import com.myplanner.app.notification.NotificationHelper
import com.myplanner.app.notification.ReminderScheduler
import com.myplanner.app.ui.navigation.MyPlannerApp
import com.myplanner.app.ui.theme.AppPalette
import com.myplanner.app.ui.theme.MyPlannerTheme

class MainActivity : ComponentActivity() {

    private var openReminderId by mutableStateOf<Long?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not — scheduling still attempted */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openReminderId = extractReminderId(intent)
        enableEdgeToEdge()
        NotificationHelper.ensureChannels(this)
        requestNotificationPermissionIfNeeded()
        val prefs = PreferencesRepository(applicationContext)
        setContent {
            val themeMode by prefs.themeMode.collectAsStateWithLifecycle("system")
            val themePalette by prefs.themePalette.collectAsStateWithLifecycle("indigo")
            val dark = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            val palette = AppPalette.fromId(themePalette)
            MyPlannerTheme(darkTheme = dark, palette = palette) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MyPlannerApp(openReminderId = openReminderId)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openReminderId = extractReminderId(intent)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun extractReminderId(intent: Intent?): Long? {
        if (intent == null) return null
        val id = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1L)
        return if (id > 0) id else null
    }
}
