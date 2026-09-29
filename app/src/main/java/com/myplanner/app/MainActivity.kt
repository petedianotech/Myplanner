package com.myplanner.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.myplanner.app.notification.ReminderScheduler
import com.myplanner.app.ui.navigation.MyPlannerApp
import com.myplanner.app.ui.theme.MyPlannerTheme

class MainActivity : ComponentActivity() {

    private var openReminderId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openReminderId = extractReminderId(intent)
        enableEdgeToEdge()
        setContent {
            MyPlannerTheme {
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
