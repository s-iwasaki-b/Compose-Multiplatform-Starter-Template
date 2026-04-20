package org.starter.project.domain.ai.provider

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLMModel

interface LlmProvider {
    val name: String
    fun createExecutor(apiKey: String): PromptExecutor
    fun defaultModel(): LLMModel
}
