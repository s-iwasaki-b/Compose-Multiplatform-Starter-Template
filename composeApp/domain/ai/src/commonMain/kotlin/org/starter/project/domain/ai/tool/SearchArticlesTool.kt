package org.starter.project.domain.ai.tool

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolArgs
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import kotlinx.serialization.Serializable
import org.starter.project.domain.ai.action.AppAction
import org.starter.project.domain.ai.action.AppActionDispatcher

class SearchArticlesTool(
    private val dispatcher: AppActionDispatcher
) : SimpleTool<SearchArticlesTool.Args>() {

    @Serializable
    data class Args(val keyword: String) : ToolArgs

    override val argsSerializer = Args.serializer()

    override val descriptor = ToolDescriptor(
        name = "search_articles",
        description = "Search for articles on Zenn.dev by keyword. Use this when the user wants to find articles about a topic.",
        requiredParameters = listOf(
            ToolParameterDescriptor(
                name = "keyword",
                description = "The search keyword to find articles",
                type = ToolParameterType.String
            )
        )
    )

    override suspend fun execute(args: Args): String {
        dispatcher.dispatch(AppAction.SearchArticles(args.keyword))
        return "Searching articles for '${args.keyword}'"
    }
}
