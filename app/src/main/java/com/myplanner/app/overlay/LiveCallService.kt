package com.myplanner.app.overlay

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.myplanner.app.MainActivity
import com.myplanner.app.R
import com.myplanner.app.ai.GeminiConfig
import com.myplanner.app.ai.GeminiLiveSession
import com.myplanner.app.ai.LiveAudioEngine
import com.myplanner.app.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Foreground service that keeps a Gemini Live session alive while the user
 * switches apps. Optionally captures the screen (MediaProjection) at ~1 FPS
 * and streams JPEG frames so Pete can see what is on the phone.
 */
class LiveCallService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var session: GeminiLiveSession? = null
    private var audio: LiveAudioEngine? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var frameJob: Job? = null
    private val screenSharing = AtomicBoolean(false)
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val model = intent.getStringExtra(EXTRA_MODEL) ?: GeminiConfig.DEFAULT_LIVE_MODEL
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val data = if (Build.VERSION.SDK_INT >= 33) {
                    intent.getParcelableExtra(EXTRA_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_DATA)
                }
                startForegroundWithNotification(sharing = data != null)
                startLiveSession(model)
                if (resultCode != 0 && data != null) {
                    startScreenShare(resultCode, data)
                }
            }
            ACTION_STOP -> {
                stopEverything()
                stopSelf()
            }
            ACTION_STOP_SHARE -> {
                stopScreenShare()
                updateNotification(sharing = false)
            }
            else -> stopSelf()
        }
        return START_STICKY
    }

    private fun startForegroundWithNotification(sharing: Boolean) {
        val notif = buildNotification(sharing)
        if (Build.VERSION.SDK_INT >= 34) {
            val type = if (sharing) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            ServiceCompat.startForeground(this, NOTIF_ID, notif, type)
        } else {
            startForeground(NOTIF_ID, notif)
        }
    }

    private fun updateNotification(sharing: Boolean) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(NOTIF_ID, buildNotification(sharing))
    }

    private fun buildNotification(sharing: Boolean): Notification {
        NotificationHelper.ensureChannels(this)
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_live", true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1,
            Intent(this, LiveCallService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = if (sharing) "Pete Live · screen sharing" else "Pete Live call"
        val text = if (sharing) "Pete can see your screen. Tap to return or stop."
        else "Talking with Pete. Tap to return or end call."
        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_LIVE)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(open)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, "End call", stop)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun startLiveSession(model: String) {
        if (session != null) return
        val live = GeminiLiveSession(
            apiKey = GeminiConfig.apiKey,
            model = model,
            listener = object : GeminiLiveSession.Listener {
                override fun onSetupComplete() {
                    startAudio()
                    try {
                        startService(Intent(this@LiveCallService, PeteBubbleService::class.java).apply {
                            action = PeteBubbleService.ACTION_SHOW_DURING_LIVE
                        })
                    } catch (_: Exception) {}
                }
                override fun onUserTranscript(text: String) {
                    if (GeminiLiveSession.containsEndPhrase(text)) {
                        stopEverything()
                        stopSelf()
                    }
                }
                override fun onModelTranscript(text: String) {}
                override fun onAudioOut(pcm24k: ByteArray) { audio?.playPcm24k(pcm24k) }
                override fun onInterrupted() {}
                override fun onError(message: String) {
                    Log.e(TAG, "Live error: $message")
                    stopEverything()
                    stopSelf()
                }
                override fun onClosed() {
                    stopEverything()
                    stopSelf()
                }
            }
        )
        session = live
        live.connect()
    }

    private fun startAudio() {
        if (audio != null) return
        val engine = LiveAudioEngine(
            onMicPcm = { pcm -> session?.sendAudioPcm16k(pcm) },
            onMicLevel = { },
            onPlaybackLevel = { }
        )
        audio = engine
        engine.start()
    }

    private fun startScreenShare(resultCode: Int, data: Intent) {
        val mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mgr.getMediaProjection(resultCode, data)
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() { stopScreenShare() }
        }, handler)

        val metrics = resources.displayMetrics
        val width = (metrics.widthPixels * 0.5f).toInt().coerceAtLeast(360)
        val height = (metrics.heightPixels * 0.5f).toInt().coerceAtLeast(640)
        val density = metrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "PeteScreenShare",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, handler
        )
        screenSharing.set(true)
        updateNotification(sharing = true)

        frameJob = scope.launch(Dispatchers.IO) {
            while (isActive && screenSharing.get()) {
                captureAndSendFrame()
                kotlinx.coroutines.delay(1000L)
            }
        }
    }

    private fun captureAndSendFrame() {
        val reader = imageReader ?: return
        val image = reader.acquireLatestImage() ?: return
        try {
            val planes = image.planes
            if (planes.isEmpty()) return
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * image.width
            val bitmap = Bitmap.createBitmap(
                image.width + rowPadding / pixelStride,
                image.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)
            val cropped = Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
            val stream = ByteArrayOutputStream()
            cropped.compress(Bitmap.CompressFormat.JPEG, 55, stream)
            val jpeg = stream.toByteArray()
            session?.sendVideoJpeg(jpeg)
            cropped.recycle()
            bitmap.recycle()
        } catch (e: Exception) {
            Log.w(TAG, "Frame capture: ${e.message}")
        } finally {
            image.close()
        }
    }

    private fun stopScreenShare() {
        screenSharing.set(false)
        frameJob?.cancel()
        frameJob = null
        try { virtualDisplay?.release() } catch (_: Exception) {}
        virtualDisplay = null
        try { imageReader?.close() } catch (_: Exception) {}
        imageReader = null
        try { mediaProjection?.stop() } catch (_: Exception) {}
        mediaProjection = null
    }

    private fun stopEverything() {
        stopScreenShare()
        session?.close()
        session = null
        audio?.stop()
        audio = null
    }

    override fun onDestroy() {
        stopEverything()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "LiveCallService"
        private const val NOTIF_ID = 7101

        const val ACTION_START = "com.myplanner.app.LIVE_START"
        const val ACTION_STOP = "com.myplanner.app.LIVE_STOP"
        const val ACTION_STOP_SHARE = "com.myplanner.app.LIVE_STOP_SHARE"
        const val EXTRA_MODEL = "model"
        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_DATA = "data"

        fun start(context: Context, model: String, resultCode: Int = 0, data: Intent? = null) {
            val i = Intent(context, LiveCallService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_MODEL, model)
                putExtra(EXTRA_RESULT_CODE, resultCode)
                if (data != null) putExtra(EXTRA_DATA, data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i)
            } else {
                context.startService(i)
            }
        }

        fun stop(context: Context) {
            context.startService(Intent(context, LiveCallService::class.java).setAction(ACTION_STOP))
        }
    }
}
