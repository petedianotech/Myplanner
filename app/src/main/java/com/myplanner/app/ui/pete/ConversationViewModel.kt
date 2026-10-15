package com.myplanner.app.ui.pete

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myplanner.app.ai.GeminiApiClient
import com.myplanner.app.ai.GeminiConfig
import com.myplanner.app.ai.GeminiLiveSession
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
    val text: String,
    val sources: List<GeminiApiClient.Source> = emptyList(),
    val imageBase64: String? = null,
    val usedSearch: Boolean = false,
    val isError: Boolean = false
)

data class ConversationUiState(
    val listenState: PeteListenState = PeteListenState.Idle,
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val navigateHint: String? = null,
    val focusMinutes: Int? = null,
    val geminiReady: Boolean = false,
    val selectedModel: String = GeminiConfig.DEFAULT_MODEL,
    val statusHint: String = "",
    val searchEnabled: Boolean = true,
    val isGenerating: Boolean = false
)

class ConversationViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application
    private val db = AppDatabase.getInstance(application)
    private val router = PeteCommandRouter(
        TaskRepository(db.taskDao()),
        ReminderRepository.create(application, db.reminderDao()),
        NoteRepository(db.noteDao())
    )
    private val gemini = GeminiApiClient()
    private val speech = SpeechHelper(application)
    private val history = mutableListOf<GeminiApiClient.Turn>()
    private var nextId = 2L
    private val model0 = GeminiConfig.getSelectedModel(application)
    private val search0 = GeminiConfig.isSearchEnabled(application)

    private val _state = MutableStateFlow(
        ConversationUiState(
            geminiReady = GeminiConfig.isConfigured,
            selectedModel = model0,
            searchEnabled = search0,
            statusHint = status(GeminiConfig.isConfigured, model0, search0),
            messages = listOf(
                ChatMessage(
                    1, true,
                    if (GeminiConfig.isConfigured)
                        "Hey — I'm Pete. Ask anything with web search, or say draw a… for images."
                    else
                        "Add GEMINI_API_KEY for real AI. Local commands still work."
                )
            )
        )
    )
    val state: StateFlow<ConversationUiState> = _state.asStateFlow()

    init { speech.initTts() }

    private fun status(ready: Boolean, model: String, search: Boolean): String {
        val m = GeminiConfig.labelFor(model)
        return if (!ready) "Offline · no API key" else if (search) "$m · Search on" else "$m · Search off"
    }

    fun selectModel(modelId: String) {
        GeminiConfig.setSelectedModel(app, modelId)
        history.clear()
        _state.update {
            it.copy(
                selectedModel = modelId,
                statusHint = status(it.geminiReady, modelId, it.searchEnabled),
                messages = it.messages + ChatMessage(nextId++, true, "Switched to ${GeminiConfig.labelFor(modelId)}.")
            )
        }
    }

    fun toggleSearch() {
        val next = !_state.value.searchEnabled
        GeminiConfig.setSearchEnabled(app, next)
        _state.update {
            it.copy(searchEnabled = next, statusHint = status(it.geminiReady, it.selectedModel, next))
        }
    }

    fun clearChat() {
        history.clear()
        nextId = 2L
        _state.update { it.copy(messages = listOf(ChatMessage(1, true, "Fresh start. What next?"))) }
    }

    fun onInputChange(value: String) { _state.update { it.copy(input = value) } }
    fun clearNavigateHint() { _state.update { it.copy(navigateHint = null, focusMinutes = null) } }

    fun sendText(text: String? = null) {
        val payload = (text ?: _state.value.input).trim()
        if (payload.isEmpty() || _state.value.isGenerating) return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    messages = it.messages + ChatMessage(nextId++, false, payload),
                    input = "", listenState = PeteListenState.Thinking, isGenerating = true
                )
            }
            if (GeminiLiveSession.containsStartPhrase(payload)) {
                _state.update {
                    it.copy(
                        messages = it.messages + ChatMessage(nextId++, true, "Starting live call…"),
                        listenState = PeteListenState.Idle, isGenerating = false, navigateHint = "live"
                    )
                }
                return@launch
            }
            try {
                val ans = answer(payload)
                _state.update {
                    it.copy(
                        messages = it.messages + ChatMessage(
                            nextId++, true, ans.text, ans.sources, ans.imageBase64, ans.usedSearch, ans.isError
                        ),
                        listenState = PeteListenState.Speaking, isGenerating = false,
                        navigateHint = ans.nav, focusMinutes = ans.focus
                    )
                }
                if (!ans.isError) speech.speak(ans.text.take(400))
                _state.update { it.copy(listenState = PeteListenState.Idle) }
            } catch (e: Throwable) {
                _state.update {
                    it.copy(
                        messages = it.messages + ChatMessage(
                            nextId++, true, e.message?.take(240) ?: "Error", isError = true
                        ),
                        listenState = PeteListenState.Idle, isGenerating = false
                    )
                }
            }
        }
    }

    private data class Ans(
        val text: String, val nav: String? = null, val focus: Int? = null,
        val sources: List<GeminiApiClient.Source> = emptyList(),
        val imageBase64: String? = null, val usedSearch: Boolean = false, val isError: Boolean = false
    )

    private suspend fun answer(payload: String): Ans {
        val local = router.handle(payload)
        val lower = payload.lowercase()
        val planner = local.navigateTo != null ||
            local.reply.contains("Task added") || local.reply.contains("Reminder set") ||
            local.reply.startsWith("Noted") || local.reply.contains("Focus mode") ||
            lower.startsWith("add task") || lower.startsWith("remind") ||
            lower.contains("start focus") || lower.startsWith("take a note")
        if (planner && (local.navigateTo != null || local.reply.contains("Task added") ||
                local.reply.contains("Reminder set") || local.reply.startsWith("Noted") ||
                local.reply.contains("Focus mode"))
        ) {
            return Ans(local.reply, local.navigateTo, local.focusMinutes)
        }
        if (!GeminiConfig.isConfigured) {
            return Ans(local.reply + "\n\n(Set GEMINI_API_KEY for full AI.)", local.navigateTo, local.focusMinutes)
        }
        if (listOf("draw ", "draw a", "generate image", "create an image", "make an image", "image of ", "picture of ")
                .any { lower.startsWith(it) || lower.contains(it) }
        ) {
            return try {
                val r = gemini.generateImage(payload, GeminiConfig.getImageModel(app))
                history.add(GeminiApiClient.Turn("user", payload))
                history.add(GeminiApiClient.Turn("model", r.text))
                trim()
                Ans(r.text, sources = r.sources, imageBase64 = r.imageBase64, usedSearch = r.usedSearch)
            } catch (t: Throwable) {
                Ans("Image gen needs a billed Gemini project.\n\n${t.message}", isError = true)
            }
        }
        val r = gemini.chat(
            payload, history.toList(), _state.value.selectedModel,
            enableSearch = _state.value.searchEnabled
        )
        history.add(GeminiApiClient.Turn("user", payload))
        history.add(GeminiApiClient.Turn("model", r.text))
        trim()
        return Ans(r.text, local.navigateTo, local.focusMinutes, r.sources, r.imageBase64, r.usedSearch)
    }

    private fun trim() {
        while (history.size > 24) history.removeAt(0)
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
                    onResult = { s ->
                        _state.update { it.copy(listenState = PeteListenState.Idle, input = s) }
                        sendText(s)
                    },
                    onError = { err ->
                        _state.update {
                            it.copy(
                                listenState = PeteListenState.Idle,
                                messages = it.messages + ChatMessage(nextId++, true, err, isError = true)
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
            "Search news" -> sendText("What are the top technology news headlines today?")
            "Draw something" -> _state.update { it.copy(input = "draw a ") }
            else -> sendText(label)
        }
    }

    override fun onCleared() {
        speech.release()
        super.onCleared()
    }
}
