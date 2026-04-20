package org.starter.project.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.starter.project.core.preferences.Preference
import org.starter.project.core.preferences.PreferencesConfig
import org.starter.project.domain.ai.AgentFactory
import org.starter.project.domain.ai.provider.LlmProvider
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChatScreenViewModel(
    private val agentFactory: AgentFactory,
    private val provider: LlmProvider,
    preferencesConfig: PreferencesConfig
) : ViewModel() {

    private val settings: Settings = preferencesConfig.aiPreferences

    private val _state = MutableStateFlow(ChatScreenState())
    val state: StateFlow<ChatScreenState> = _state.asStateFlow()

    init {
        loadApiKey()
    }

    private fun loadApiKey() {
        val savedKey: String = settings[Preference.AiApiKey.key, ""]
        _state.update {
            it.copy(
                apiKey = savedKey,
                isApiKeyConfigured = savedKey.isNotBlank()
            )
        }
    }

    fun updateInputText(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun updateApiKey(key: String) {
        _state.update { it.copy(apiKey = key) }
    }

    fun saveApiKey() {
        val key = _state.value.apiKey.trim()
        settings[Preference.AiApiKey.key] = key
        _state.update {
            it.copy(
                isApiKeyConfigured = key.isNotBlank(),
                showSettings = false
            )
        }
    }

    fun toggleSettings() {
        _state.update { it.copy(showSettings = !it.showSettings) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank()) return
        if (!_state.value.isApiKeyConfigured) {
            _state.update { it.copy(error = "APIキーを設定してください", showSettings = true) }
            return
        }

        val userMessage = ChatMessage(
            id = Uuid.random().toString(),
            content = text,
            role = ChatRole.USER,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )

        _state.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isProcessing = true,
                error = null
            )
        }

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val agent = agentFactory.create(provider, _state.value.apiKey)
                val result = agent.run(text)

                val assistantMessage = ChatMessage(
                    id = Uuid.random().toString(),
                    content = result,
                    role = ChatRole.ASSISTANT,
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )

                _state.update {
                    it.copy(
                        messages = it.messages + assistantMessage,
                        isProcessing = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message ?: "エラーが発生しました"
                    )
                }
            }
        }
    }
}
