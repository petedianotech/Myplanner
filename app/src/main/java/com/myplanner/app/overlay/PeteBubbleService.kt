package com.myplanner.app.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.myplanner.app.MainActivity
import com.myplanner.app.R
import com.myplanner.app.ai.GeminiApiClient
import com.myplanner.app.ai.GeminiConfig
import com.myplanner.app.ai.SpeechHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PeteBubbleService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var windowManager: WindowManager? = null
    private var bubbleView: View? = null
    private var panelView: View? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var expanded = false
    private lateinit var speech: SpeechHelper
    private val history = mutableListOf<GeminiApiClient.Turn>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        speech = SpeechHelper(this)
        speech.initTts()
        startForeground(NOTIF_ID, buildNotification())
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showBubble()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        speech.release()
        removeViews()
        super.onDestroy()
    }

    private fun showBubble() {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        bubbleParams = WindowManager.LayoutParams(
            dp(56), dp(56),
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = dp(12)
            y = dp(120)
        }

        val bubble = TextView(this).apply {
            text = "P"
            textSize = 20f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            setBackgroundColor(0xFF4F46E5.toInt())
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setOnTouchListener(dragTouchListener())
            setOnClickListener {
                if (expanded) collapsePanel() else expandPanel()
            }
        }
        bubbleView = bubble
        windowManager?.addView(bubble, bubbleParams)
    }

    private fun expandPanel() {
        if (expanded) return
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val panelParams = WindowManager.LayoutParams(
            dp(300),
            dp(380),
            type,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = dp(12)
            y = dp(180)
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADAPT_RESIZE
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xF00D0F14.toInt())
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        val title = TextView(this).apply {
            text = "Pete · Assistant"
            setTextColor(0xFFF1F5F9.toInt())
            textSize = 16f
            setPadding(0, 0, 0, dp(8))
        }
        root.addView(title)

        val scroll = ScrollView(this)
        val log = TextView(this).apply {
            setTextColor(0xFFCBD5E1.toInt())
            textSize = 13f
            text = "Hey boss. Type or speak — I'm here.\n"
            setPadding(0, 0, 0, dp(8))
        }
        scroll.addView(log)
        root.addView(scroll, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        val inputRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val input = EditText(this).apply {
            hint = "Message Pete…"
            setHintTextColor(0xFF64748B.toInt())
            setTextColor(0xFFF1F5F9.toInt())
            setBackgroundColor(0xFF1E293B.toInt())
            setPadding(dp(10), dp(10), dp(10), dp(10))
            isSingleLine = true
        }
        val send = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_send)
            setBackgroundColor(0xFF4F46E5.toInt())
            setOnClickListener {
                val text = input.text?.toString()?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    input.text.clear()
                    appendLog(log, "You: $text")
                    handleUserText(text, log)
                }
            }
        }
        val mic = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_btn_speak_now)
            setBackgroundColor(0xFF0F766E.toInt())
            setOnClickListener {
                appendLog(log, "Listening…")
                speech.startListening(
                    onResult = { spoken ->
                        appendLog(log, "You: $spoken")
                        handleUserText(spoken, log)
                    },
                    onError = { err -> appendLog(log, err) }
                )
            }
        }
        inputRow.addView(input, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        inputRow.addView(mic, LinearLayout.LayoutParams(dp(44), dp(44)))
        inputRow.addView(send, LinearLayout.LayoutParams(dp(44), dp(44)))
        root.addView(inputRow)

        val close = TextView(this).apply {
            text = "Close panel"
            setTextColor(0xFF94A3B8.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, 0)
            setOnClickListener { collapsePanel() }
        }
        root.addView(close)

        panelView = root
        windowManager?.addView(root, panelParams)
        expanded = true
    }

    private fun collapsePanel() {
        panelView?.let {
            try { windowManager?.removeView(it) } catch (_: Exception) {}
        }
        panelView = null
        expanded = false
        speech.stopListening()
    }

    private fun handleUserText(text: String, log: TextView) {
        scope.launch {
            val reply = withContext(Dispatchers.IO) { generateReply(text) }
            history.add(GeminiApiClient.Turn("user", text))
            history.add(GeminiApiClient.Turn("model", reply))
            if (history.size > 20) {
                repeat(history.size - 20) { history.removeAt(0) }
            }
            appendLog(log, "Pete: $reply")
            speech.speak(reply)
        }
    }

    private suspend fun generateReply(text: String): String {
        return try {
            if (GeminiConfig.isConfigured) {
                GeminiApiClient().chat(text, history.toList())
            } else {
                "Got it, boss: \"$text\". Add GEMINI_API_KEY for full AI replies."
            }
        } catch (e: Exception) {
            "Something went wrong: ${e.message?.take(120) ?: "error"}"
        }
    }

    private fun appendLog(log: TextView, line: String) {
        log.append(line + "\n")
    }

    private fun dragTouchListener() = object : View.OnTouchListener {
        private var lastX = 0
        private var lastY = 0
        private var initX = 0
        private var initY = 0
        private var moved = false
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val params = bubbleParams ?: return false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()
                    initX = params.x
                    initY = params.y
                    moved = false
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = lastX - event.rawX.toInt()
                    val dy = event.rawY.toInt() - lastY
                    if (kotlin.math.abs(dx) > 4 || kotlin.math.abs(dy) > 4) moved = true
                    params.x = initX + (lastX - event.rawX.toInt())
                    params.y = initY + (event.rawY.toInt() - lastY)
                    windowManager?.updateViewLayout(bubbleView, params)
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) v.performClick()
                    return true
                }
            }
            return false
        }
    }

    private fun removeViews() {
        collapsePanel()
        bubbleView?.let {
            try { windowManager?.removeView(it) } catch (_: Exception) {}
        }
        bubbleView = null
    }

    private fun buildNotification(): Notification {
        val channelId = "pete_bubble"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(channelId, "Pete bubble", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1,
            Intent(this, PeteBubbleService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Pete is available")
            .setContentText("Tap the bubble to chat over any app")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(open)
            .addAction(0, "Stop", stop)
            .setOngoing(true)
            .build()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    companion object {
        const val ACTION_STOP = "com.myplanner.app.overlay.STOP"
        private const val NOTIF_ID = 42

        fun start(context: Context) {
            val i = Intent(context, PeteBubbleService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i)
            } else {
                context.startService(i)
            }
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, PeteBubbleService::class.java).setAction(ACTION_STOP)
            )
        }

        fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)
    }
}
