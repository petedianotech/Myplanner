package com.myplanner.app.ai

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Mic capture at 16 kHz PCM and playback at 24 kHz PCM for Gemini Live.
 */
class LiveAudioEngine(
    private val onMicPcm: (ByteArray) -> Unit,
    private val onMicLevel: (Float) -> Unit,
    private val onPlaybackLevel: (Float) -> Unit
) {
    private val running = AtomicBoolean(false)
    private var recordThread: Thread? = null
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null

    fun start() {
        if (running.getAndSet(true)) return
        startPlayback()
        startCapture()
    }

    fun stop() {
        running.set(false)
        try {
            audioRecord?.stop()
        } catch (_: Exception) {
        }
        try {
            audioRecord?.release()
        } catch (_: Exception) {
        }
        audioRecord = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {
        }
        audioTrack = null
        recordThread = null
        onMicLevel(0f)
        onPlaybackLevel(0f)
    }

    fun playPcm24k(pcm: ByteArray) {
        val track = audioTrack ?: return
        if (pcm.isEmpty()) return
        try {
            track.write(pcm, 0, pcm.size)
            onPlaybackLevel(rmsLevel(pcm))
        } catch (e: Exception) {
            Log.w(TAG, "play error: ${e.message}")
        }
    }

    private fun startPlayback() {
        val minBuf = AudioTrack.getMinBufferSize(
            OUT_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val buf = max(minBuf, OUT_RATE * 2 / 5)
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(OUT_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buf)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        audioTrack?.play()
    }

    private fun startCapture() {
        val minBuf = AudioRecord.getMinBufferSize(
            IN_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val buf = max(minBuf, IN_RATE * 2 / 10) // ~100ms
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            IN_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            buf
        )
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord not initialized")
            running.set(false)
            return
        }
        audioRecord = recorder
        recorder.startRecording()
        recordThread = Thread({
            val chunk = ByteArray(buf)
            while (running.get()) {
                val n = recorder.read(chunk, 0, chunk.size)
                if (n > 0) {
                    val copy = chunk.copyOf(n)
                    onMicLevel(rmsLevel(copy))
                    onMicPcm(copy)
                }
            }
        }, "pete-live-mic").also {
            it.isDaemon = true
            it.start()
        }
    }

    private fun rmsLevel(pcm: ByteArray): Float {
        if (pcm.size < 2) return 0f
        var sum = 0.0
        var count = 0
        var i = 0
        while (i + 1 < pcm.size) {
            val sample = (pcm[i].toInt() and 0xff) or (pcm[i + 1].toInt() shl 8)
            val s = sample.toShort().toInt()
            sum += (s * s).toDouble()
            count++
            i += 2
        }
        if (count == 0) return 0f
        val rms = kotlin.math.sqrt(sum / count) / 32768.0
        return min(1f, abs(rms.toFloat()) * 4f)
    }

    companion object {
        private const val TAG = "LiveAudioEngine"
        const val IN_RATE = 16_000
        const val OUT_RATE = 24_000
    }
}
