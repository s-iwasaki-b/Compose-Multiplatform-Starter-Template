package org.starter.project.domain.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import org.starter.project.domain.ai.action.AppActionDispatcher
import org.starter.project.domain.ai.provider.LlmProvider
import org.starter.project.domain.ai.tool.NavigateToUserTool
import org.starter.project.domain.ai.tool.SearchArticlesTool

class AgentFactory(
    private val dispatcher: AppActionDispatcher
) {
    fun create(provider: LlmProvider, apiKey: String): AIAgent {
        val searchTool = SearchArticlesTool(dispatcher)
        val navigateToUserTool = NavigateToUserTool(dispatcher)

        return AIAgent(
            promptExecutor = provider.createExecutor(apiKey),
            systemPrompt = """
                You are an AI assistant for a Zenn.dev article viewer app.
                You can help users by performing app operations:
                - Search for articles by keyword using the search_articles tool
                - Navigate to a user's profile using the navigate_to_user tool

                When the user asks to find or search articles, use the search_articles tool.
                When the user asks to view a user's profile, use the navigate_to_user tool.
                Always confirm the actions you've taken in your response.
                Respond in the same language as the user's message.
            """.trimIndent(),
            llmModel = provider.defaultModel(),
            toolRegistry = ToolRegistry {
                tool(searchTool)
                tool(navigateToUserTool)
            }
        )
    }
}
