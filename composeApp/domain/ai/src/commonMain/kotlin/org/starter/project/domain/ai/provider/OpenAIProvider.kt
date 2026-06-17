package org.starter.project.domain.ai.provider

import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.clients.openai.simpleOpenAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLMModel

class OpenAIProvider : LlmProvider {
    override val name: String = "OpenAI"

    override fun createExecutor(apiKey: String): PromptExecutor {
        return simpleOpenAIExecutor(apiKey)
    }

    override fun defaultModel(): LLMModel {
        return OpenAIModels.Chat.GPT4o
    }
}
