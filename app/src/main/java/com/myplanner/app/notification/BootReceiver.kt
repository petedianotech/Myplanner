package com.myplanner.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Restores reminder alarms after device boot / package update.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        Log.i(TAG, "Boot/update recovery action=$action")
        val pending = goAsync()
        scope.launch {
            try {
                MissedReminderRecovery.run(context.applicationContext)
            } catch (t: Throwable) {
                Log.e(TAG, "Boot recovery failed", t)
            } finally {
                try {
                    pending.finish()
                } catch (_: Throwable) {
                }
            }
        }
    }

    companion object {
        private const val TAG = "PeteBoot"
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
