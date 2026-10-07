package com.myplanner.app.ui.pete

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myplanner.app.ai.GeminiApiClient
import com.myplanner.app.ai.GeminiConfig
import com.myplanner.app.ai.PeteCommandRouter
import com.myplanner.app.ai.SpeechHelper
import com.myplanner.app.data.local.AppDatabase
import com.myplanner.app.data.repository.NoteRepository
import com.myplanner.app.data.repository.ReminderRepository
import com.myplanner.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PeteListenState { Idle, Listening, Thinking, Speaking }

data class ChatMessage(
    val id: Long,
    val fromPete: Boolean,
    val text: String
)

data class ConversationUiState(
    val listenState: PeteListenState = PeteListenState.Idle,
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val navigateHint: String? = null,
    val focusMinutes: Int? = null,
    val geminiReady: Boolean = false,
    val selectedModel: String = GeminiConfig.DEFAULT_MODEL,
    val statusHint: String = ""
)

class ConversationViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application
    private val db = AppDatabase.getInstance(application)
    private val taskRepo = TaskRepository(db.taskDao())
    private val reminderRepo = ReminderRepository.create(application, db.reminderDao())
    private val noteRepo = NoteRepository(db.noteDao())
    private val router = PeteCommandRouter(taskRepo, reminderRepo, noteRepo)
    private val gemini = GeminiApiClient()
    private val speech = SpeechHelper(application)
    private val geminiHistory = mutableListOf<GeminiApiClient.Turn>()

    private val initialModel = GeminiConfig.getSelectedModel(application)

    private val _state = MutableStateFlow(
        ConversationUiState(
            geminiReady = GeminiConfig.isConfigured,
            selectedModel = initialModel,
            statusHint = statusLine(GeminiConfig.isConfigured, initialModel),
            messages = listOf(
                ChatMessage(
                    1,
                    true,
                    if (GeminiConfig.isConfigured)
                        "Hey boss. Gemini is live (${GeminiConfig.labelFor(initialModel)}). Type or talk — switch models anytime."
                    else
                        "Hey boss. Local commands work. Add GEMINI_API_KEY for full Gemini chat."
                )
            )
        )
    )
    val state: StateFlow<ConversationUiState> = _state.asStateFlow()

    private var nextId = 2L

    init {
        speech.initTts()
    }

    private fun statusLine(ready: Boolean, model: String): String =
        if (ready) "${GeminiConfig.labelFor(model)} · type or speak"
        else "Local mode · add GEMINI_API_KEY"

    fun selectModel(modelId: String) {
        GeminiConfig.setSelectedModel(app, modelId)
        geminiHistory.clear()
        _state.update {
            it.copy(
                selectedModel = modelId,
                statusHint = statusLine(it.geminiReady, modelId),
                messages = it.messages + ChatMessage(
                    nextId++,
                    true,
                    "Switched to ${GeminiConfig.labelFor(modelId)}."
                )
            )
        }
    }

    fun onInputChange(value: String) {
        _state.update { it.copy(input = value) }
    }

    fun clearNavigateHint() {
        _state.update { it.copy(navigateHint = null, focusMinutes = null) }
    }

    fun sendText(text: String? = null) {
        val payload = (text ?: _state.value.input).trim()
        if (payload.isEmpty()) return
        viewModelScope.launch {
            val userMsg = ChatMessage(nextId++, false, payload)
            _state.update {
                it.copy(
                    messages = it.messages + userMsg,
                    input = "",
                    listenState = PeteListenState.Thinking
                )
            }

            val (reply, nav, focusMins) = runCatching {
                answer(payload)
            }.getOrElse { e ->
                Triple("Sorry boss — ${e.message?.take(200) ?: "something went wrong."}", null, null)
            }

            val peteMsg = ChatMessage(nextId++, true, reply)
            _state.update {
                it.copy(
                    messages = it.messages + peteMsg,
                    listenState = PeteListenState.Speaking,
                    navigateHint = nav,
                    focusMinutes = focusMins
                )
            }
            speech.speak(reply)
            _state.update { it.copy(listenState = PeteListenState.Idle) }
        }
    }

    private suspend fun answer(payload: String): Triple<String, String?, Int?> {
        val local = router.handle(payload)
        val looksLikeCommand = local.navigateTo != null ||
            local.reply.contains("Task added") ||
            local.reply.contains("Reminder set") ||
            local.reply.startsWith("Noted") ||
            local.reply.contains("Focus mode") ||
            payload.lowercase().let {
                it.startsWith("add task") || it.startsWith("remind") ||
                    it.contains("start focus") || it.startsWith("take a note")
            }

        if (looksLikeCommand && local.navigateTo != null) {
            return Triple(local.reply, local.navigateTo, local.focusMinutes)
        }
        if (looksLikeCommand && !GeminiConfig.isConfigured) {
            return Triple(local.reply, local.navigateTo, local.focusMinutes)
        }

        if (GeminiConfig.isConfigured) {
            val model = _state.value.selectedModel
            val text = gemini.chat(payload, geminiHistory.toList(), model = model)
            geminiHistory.add(GeminiApiClient.Turn("user", payload))
            geminiHistory.add(GeminiApiClient.Turn("model", text))
            if (geminiHistory.size > 24) {
                repeat(geminiHistory.size - 24) { geminiHistory.removeAt(0) }
            }
            return Triple(text, local.navigateTo, local.focusMinutes)
        }
        return Triple(local.reply, local.navigateTo, local.focusMinutes)
    }

    fun tapListen() {
        when (_state.value.listenState) {
            PeteListenState.Listening -> {
                speech.stopListening()
                _state.update { it.copy(listenState = PeteListenState.Idle) }
            }
            else -> {
                _state.update { it.copy(listenState = PeteListenState.Listening) }
                speech.startListening(
                    onPartial = {},
                    onResult = { spoken ->
                        _state.update { it.copy(listenState = PeteListenState.Idle, input = spoken) }
                        sendText(spoken)
                    },
                    onError = { err ->
                        _state.update {
                            it.copy(
                                listenState = PeteListenState.Idle,
                                messages = it.messages + ChatMessage(nextId++, true, err)
                            )
                        }
                    }
                )
            }
        }
    }

    fun quickAction(label: String) {
        when (label) {
            "Today" -> sendText("what's on today")
            "Add task" -> _state.update { it.copy(input = "add task ") }
            "Focus 25m" -> sendText("start focus 25 minutes")
            "Snooze all" -> sendText("snooze all")
            else -> sendText(label)
        }
    }

    override fun onCleared() {
        speech.release()
        super.onCleared()
    }
}
