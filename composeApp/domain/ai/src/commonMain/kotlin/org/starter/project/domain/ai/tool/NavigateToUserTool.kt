package org.starter.project.domain.ai.tool

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolArgs
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import kotlinx.serialization.Serializable
import org.starter.project.domain.ai.action.AppAction
import org.starter.project.domain.ai.action.AppActionDispatcher

class NavigateToUserTool(
    private val dispatcher: AppActionDispatcher
) : SimpleTool<NavigateToUserTool.Args>() {

    @Serializable
    data class Args(val username: String) : ToolArgs

    override val argsSerializer = Args.serializer()

    override val descriptor = ToolDescriptor(
        name = "navigate_to_user",
        description = "Navigate to a user's profile page on Zenn.dev. Use this when the user wants to view someone's profile.",
        requiredParameters = listOf(
            ToolParameterDescriptor(
                name = "username",
                description = "The Zenn username to navigate to",
                type = ToolParameterType.String
            )
        )
    )

    override suspend fun execute(args: Args): String {
        dispatcher.dispatch(AppAction.NavigateToUser(args.username))
        return "Navigating to user profile '${args.username}'"
    }
}
