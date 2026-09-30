package com.myplanner.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myplanner.app.data.local.PreferencesRepository
import com.myplanner.app.notification.ReminderScheduler
import com.myplanner.app.ui.navigation.MyPlannerApp
import com.myplanner.app.ui.theme.AppPalette
import com.myplanner.app.ui.theme.MyPlannerTheme

class MainActivity : ComponentActivity() {

    private var openReminderId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openReminderId = extractReminderId(intent)
        enableEdgeToEdge()
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

    private fun extractReminderId(intent: Intent?): Long? {
        if (intent == null) return null
        val id = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1L)
        return if (id > 0) id else null
    }
}
