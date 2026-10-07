package com.myplanner.app.ui.pete

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myplanner.app.ai.GeminiConfig
import com.myplanner.app.ai.GeminiLiveSession
import com.myplanner.app.ai.LiveAudioEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LiveCallPhase {
    Idle, Connecting, Live, Ending, Error
}

data class LiveCallUiState(
    val phase: LiveCallPhase = LiveCallPhase.Idle,
    val selectedLiveModel: String = GeminiConfig.DEFAULT_LIVE_MODEL,
    val status: String = "Tap Start live call",
    val userLevel: Float = 0f,
    val peteLevel: Float = 0f,
    val userTranscript: String = "",
    val peteTranscript: String = "",
    val error: String? = null
)

class LiveCallViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application

    private val _state = MutableStateFlow(
        LiveCallUiState(selectedLiveModel = GeminiConfig.getSelectedLiveModel(application))
    )
    val state: StateFlow<LiveCallUiState> = _state.asStateFlow()

    private var session: GeminiLiveSession? = null
    private var audio: LiveAudioEngine? = null

    fun selectLiveModel(modelId: String) {
        GeminiConfig.setSelectedLiveModel(app, modelId)
        _state.update { it.copy(selectedLiveModel = modelId) }
    }

    fun startCall() {
        if (!GeminiConfig.isConfigured) {
            _state.update {
                it.copy(phase = LiveCallPhase.Error, error = "Add GEMINI_API_KEY to use Live calling")
            }
            return
        }
        if (_state.value.phase == LiveCallPhase.Connecting || _state.value.phase == LiveCallPhase.Live) return

        val model = _state.value.selectedLiveModel
        _state.update {
            it.copy(
                phase = LiveCallPhase.Connecting,
                status = "Connecting to $model…",
                error = null,
                userTranscript = "",
                peteTranscript = ""
            )
        }

        val live = GeminiLiveSession(
            apiKey = GeminiConfig.apiKey,
            model = model,
            listener = object : GeminiLiveSession.Listener {
                override fun onSetupComplete() {
                    viewModelScope.launch {
                        _state.update {
                            it.copy(phase = LiveCallPhase.Live, status = "Live · say stop or goodbye to end")
                        }
                        startAudio()
                    }
                }

                override fun onUserTranscript(text: String) {
                    viewModelScope.launch {
                        _state.update { it.copy(userTranscript = text) }
                        if (GeminiLiveSession.containsEndPhrase(text)) {
                            endCall(reason = "Ended by voice")
                        }
                    }
                }

                override fun onModelTranscript(text: String) {
                    viewModelScope.launch {
                        _state.update { it.copy(peteTranscript = text) }
                    }
                }

                override fun onAudioOut(pcm24k: ByteArray) {
                    audio?.playPcm24k(pcm24k)
                }

                override fun onInterrupted() {
                    // barge-in — levels will drop naturally
                }

                override fun onError(message: String) {
                    viewModelScope.launch {
                        _state.update {
                            it.copy(phase = LiveCallPhase.Error, error = message, status = "Error")
                        }
                        teardownAudio()
                    }
                }

                override fun onClosed() {
                    viewModelScope.launch {
                        if (_state.value.phase != LiveCallPhase.Error) {
                            _state.update {
                                it.copy(
                                    phase = LiveCallPhase.Idle,
                                    status = "Call ended",
                                    userLevel = 0f,
                                    peteLevel = 0f
                                )
                            }
                        }
                        teardownAudio()
                    }
                }
            }
        )
        session = live
        live.connect()
    }

    fun endCall(reason: String = "Call ended") {
        if (_state.value.phase == LiveCallPhase.Idle || _state.value.phase == LiveCallPhase.Ending) return
        _state.update { it.copy(phase = LiveCallPhase.Ending, status = reason) }
        session?.close()
        session = null
        teardownAudio()
        _state.update {
            it.copy(
                phase = LiveCallPhase.Idle,
                status = reason,
                userLevel = 0f,
                peteLevel = 0f
            )
        }
    }

    private fun startAudio() {
        teardownAudio()
        val engine = LiveAudioEngine(
            onMicPcm = { pcm -> session?.sendAudioPcm16k(pcm) },
            onMicLevel = { level ->
                _state.update { it.copy(userLevel = level) }
            },
            onPlaybackLevel = { level ->
                _state.update { it.copy(peteLevel = level) }
            }
        )
        audio = engine
        engine.start()
        // Greet so the call feels live immediately
        session?.sendText("Hey boss, I'm on the line. How can I help?")
    }

    private fun teardownAudio() {
        audio?.stop()
        audio = null
    }

    override fun onCleared() {
        session?.close()
        teardownAudio()
        super.onCleared()
    }
}
