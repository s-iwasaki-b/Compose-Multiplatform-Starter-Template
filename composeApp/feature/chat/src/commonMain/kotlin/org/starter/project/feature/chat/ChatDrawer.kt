package org.starter.project.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun ChatDrawer(
    drawerState: ChatDrawerState,
    viewModel: ChatScreenViewModel,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val edgeWidthPx = with(density) { 24.dp.toPx() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val drawerWidth = maxWidth * 0.80f

        // Main content with edge swipe detection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(drawerState.isOpen) {
                    if (!drawerState.isOpen) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            val startX = change.position.x - dragAmount
                            if (startX > size.width - edgeWidthPx && dragAmount < -40f) {
                                drawerState.open()
                            }
                        }
                    }
                }
        ) {
            content()
        }

        // Scrim overlay
        if (drawerState.isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        drawerState.close()
                    }
            )
        }

        // Drawer panel from right
        AnimatedVisibility(
            visible = drawerState.isOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            val chatState by viewModel.state.collectAsState()

            Box(
                modifier = Modifier
                    .width(drawerWidth)
                    .fillMaxHeight()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount > 80f) {
                                drawerState.close()
                            }
                        }
                    }
            ) {
                ChatDrawerContent(
                    state = chatState,
                    onInputChanged = viewModel::updateInputText,
                    onSendMessage = viewModel::sendMessage,
                    onToggleSettings = viewModel::toggleSettings,
                    onApiKeyChanged = viewModel::updateApiKey,
                    onSaveApiKey = viewModel::saveApiKey,
                    onDismissError = viewModel::dismissError
                )
            }
        }
    }
}
